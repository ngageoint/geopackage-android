package mil.nga.geopackage.db;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.Date;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.db.metadata.GeoPackageMetadataDb;
import mil.nga.geopackage.db.metadata.GeometryMetadata;
import mil.nga.geopackage.db.metadata.GeometryMetadataDataSource;
import mil.nga.geopackage.db.metadata.TableMetadata;
import mil.nga.geopackage.db.metadata.TableMetadataDataSource;
import mil.nga.geopackage.features.index.FeatureIndexMetadataResults;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureRowSync;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.proj.Projection;
import mil.nga.proj.ProjectionTransform;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;

/**
 * Feature Indexer, indexes feature geometries into a table for searching
 */
public class FeatureIndexer {

    /**
     * Context
     */
    private final Context context;

    /**
     * Feature DAO
     */
    private final FeatureDao featureDao;

    /**
     * Feature Row Sync for simultaneous same row queries
     */
    private final FeatureRowSync featureRowSync = new FeatureRowSync();

    /**
     * Database connection to the metadata
     */
    private final GeoPackageMetadataDb db;

    /**
     * Geometry Metadata Data Source
     */
    private final GeometryMetadataDataSource geometryMetadataDataSource;

    /**
     * Progress
     */
    private GeoPackageProgress progress;

    /**
     * Query single chunk limit
     */
    protected int chunkLimit = 1000;

    /**
     * Constructor
     *
     * @param context    context
     * @param featureDao feature dao
     */
    public FeatureIndexer(Context context, FeatureDao featureDao) {
        this.context = context;
        this.featureDao = featureDao;
        db = new GeoPackageMetadataDb(context);
        db.open();
        geometryMetadataDataSource = new GeometryMetadataDataSource(db);
    }

    /**
     * Get the primary key column name
     *
     * @return primary key column name
     * @since 6.2.0
     */
    public String getPkColumnName() {
        return featureDao.getPkColumnName();
    }

    /**
     * Close the database connection in the feature indexer
     *
     * @since 1.1.0
     */
    public void close() {
        db.close();
    }

    /**
     * Set the progress tracker
     *
     * @param progress progress tracker
     */
    public void setProgress(GeoPackageProgress progress) {
        this.progress = progress;
    }

    /**
     * Get the SQL query chunk limit
     *
     * @return chunk limit
     * @since 3.1.0
     */
    public int getChunkLimit() {
        return chunkLimit;
    }

    /**
     * Set the SQL query chunk limit
     *
     * @param chunkLimit chunk limit
     * @since 3.1.0
     */
    public void setChunkLimit(int chunkLimit) {
        this.chunkLimit = chunkLimit;
    }

    /**
     * Index the feature table if needed
     *
     * @return count
     */
    public int index() {
        return index(false);
    }

    /**
     * Index the feature table
     *
     * @param force true to force re-indexing
     * @return count
     */
    public int index(boolean force) {
        int count = 0;
        if (force || !isIndexed()) {
            count = indexTable();
        }
        return count;
    }

    /**
     * Index the feature row. This method assumes that indexing has been completed and
     * maintained as the last indexed time is updated.
     *
     * @param row feature row
     * @return true if indexed
     */
    public boolean index(FeatureRow row) {

        long geoPackageId = geometryMetadataDataSource.getGeoPackageId(featureDao.getDatabase());
        boolean indexed = index(geoPackageId, row, true);

        // Update the last indexed time
        updateLastIndexed(db, geoPackageId);

        return indexed;
    }

    /**
     * Index the feature table
     *
     * @return count
     */
    private int indexTable() {

        int count = 0;

        // Get or create the table metadata
        TableMetadataDataSource tableDs = new TableMetadataDataSource(db);
        TableMetadata metadata = tableDs.getOrCreate(featureDao.getDatabase(), featureDao.getTableName());

        // Delete existing index rows
        geometryMetadataDataSource.delete(featureDao.getDatabase(), featureDao.getTableName());

        long offset = 0;
        int chunkCount = 0;

        // Index all features
        while (chunkCount >= 0) {

            FeatureCursor cursor = featureDao.queryForChunk(chunkLimit, offset);
            chunkCount = indexRows(metadata.getGeoPackageId(), cursor);

            if (chunkCount > 0) {
                count += chunkCount;
            }

            offset += chunkLimit;
        }

        // Update the last indexed time
        if (progress == null || progress.isActive()) {
            updateLastIndexed(db, metadata.getGeoPackageId());
        }

        return count;
    }

    /**
     * Index the feature rows in the cursor
     *
     * @param geoPackageId GeoPackage id
     * @param cursor       feature cursor
     * @return count, -1 if no results or canceled
     */
    private int indexRows(long geoPackageId, FeatureCursor cursor) {

        int count = -1;

        try {
            while ((progress == null || progress.isActive())
                    && cursor.moveToNext()) {
                if (count < 0) {
                    count++;
                }
                try {
                    FeatureRow row = cursor.getRow();
                    if (row.isValid()) {
                        boolean indexed = index(geoPackageId, row, false);
                        if (indexed) {
                            count++;
                        }
                        if (progress != null) {
                            progress.addProgress(1);
                        }
                    }
                } catch (Exception e) {
                    Log.e(FeatureIndexer.class.getSimpleName(), "Failed to index feature. Table: "
                            + featureDao.getTableName() + ", Position: " + cursor.getPosition(), e);
                }
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    /**
     * Index the feature row
     *
     * @param geoPackageId   GeoPackage id
     * @param row            feature row
     * @param possibleUpdate possible update flag
     * @return true if indexed
     */
    private boolean index(long geoPackageId, FeatureRow row, boolean possibleUpdate) {

        boolean indexed = false;

        GeoPackageGeometryData geomData = row.getGeometry();
        if (geomData != null) {

            // Get the envelope
            GeometryEnvelope envelope = geomData.getEnvelope();

            // If no envelope, build one from the geometry
            if (envelope == null) {
                Geometry geometry = geomData.getGeometry();
                if (geometry != null) {
                    envelope = geometry.getEnvelope();
                }
            }

            // Create the new index row
            if (envelope != null) {
                GeometryMetadata metadata = geometryMetadataDataSource.populate(geoPackageId, featureDao.getTableName(), row.getId(), envelope);
                if (possibleUpdate) {
                    geometryMetadataDataSource.createOrUpdate(metadata);
                } else {
                    geometryMetadataDataSource.create(metadata);
                }
                indexed = true;
            }
        }

        return indexed;
    }

    /**
     * Update the least indexed time
     *
     * @param db           metadata db
     * @param geoPackageId GeoPackage id
     */
    private void updateLastIndexed(GeoPackageMetadataDb db, long geoPackageId) {

        long indexedTime = (new Date()).getTime();

        TableMetadataDataSource ds = new TableMetadataDataSource(db);
        if (!ds.updateLastIndexed(geoPackageId, featureDao.getTableName(), indexedTime)) {
            throw new GeoPackageException("Failed to update last indexed time. Table: GeoPackage Id: "
                    + geoPackageId + ", Table: " + featureDao.getTableName() + ", Last Indexed: " + indexedTime);
        }

    }

    /**
     * Delete the feature table index
     *
     * @return true if index deleted
     * @since 1.1.0
     */
    public boolean deleteIndex() {
        TableMetadataDataSource tableMetadataDataSource = new TableMetadataDataSource(db);
        boolean deleted = tableMetadataDataSource.delete(featureDao.getDatabase(), featureDao.getTableName());
        return deleted;
    }

    /**
     * Delete the index for the feature row
     *
     * @param row feature row
     * @return true if deleted
     * @since 1.1.0
     */
    public boolean deleteIndex(FeatureRow row) {
        return deleteIndex(row.getId());
    }

    /**
     * Delete the index for the geometry id
     *
     * @param geomId geometry id
     * @return true if deleted
     */
    public boolean deleteIndex(long geomId) {
        boolean deleted = geometryMetadataDataSource.delete(
                featureDao.getDatabase(), featureDao.getTableName(), geomId);
        return deleted;
    }

    /**
     * Determine if the database table is indexed after database modifications
     *
     * @return true if indexed
     */
    public boolean isIndexed() {

        boolean indexed = false;

        Date lastIndexed = getLastIndexed();
        if (lastIndexed != null) {
            Contents contents = featureDao.getGeometryColumns().getContents();
            Date lastChange = contents.getLastChange();
            indexed = lastIndexed.equals(lastChange) || lastIndexed.after(lastChange);
        }

        return indexed;
    }

    /**
     * Get the date last indexed
     *
     * @return last indexed date or null
     * @since 1.1.0
     */
    public Date getLastIndexed() {
        Date date = null;
        TableMetadataDataSource ds = new TableMetadataDataSource(db);
        TableMetadata metadata = ds.get(featureDao.getDatabase(), featureDao.getTableName());
        if (metadata != null) {
            Long lastIndexed = metadata.getLastIndexed();
            if (lastIndexed != null) {
                date = new Date(lastIndexed);
            }
        }
        return date;
    }

    /**
     * Get the query range tolerance
     *
     * @return query range tolerance
     * @since 3.1.0
     */
    public double getTolerance() {
        return geometryMetadataDataSource.getTolerance();
    }

    /**
     * Set the query range tolerance
     *
     * @param tolerance query range tolerance
     * @since 3.1.0
     */
    public void setTolerance(double tolerance) {
        geometryMetadataDataSource.setTolerance(tolerance);
    }

    /**
     * Query for all Geometry Metadata
     *
     * @return geometry metadata cursor
     * @since 1.1.0
     */
    public Cursor query() {
        return geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName());
    }

    /**
     * Query for all Geometry Metadata
     *
     * @param columns columns
     * @return geometry metadata cursor
     * @since 3.5.0
     */
    public Cursor query(String[] columns) {
        return geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName(), columns);
    }

    /**
     * Query for all Geometry Metadata ids
     *
     * @return geometry metadata cursor
     * @since 3.4.0
     */
    public Cursor queryIds() {
        return geometryMetadataDataSource.queryIds(featureDao.getDatabase(), featureDao.getTableName());
    }

    /**
     * Query for all Geometry Metadata count
     *
     * @return count
     * @since 3.4.0
     */
    public long count() {
        return geometryMetadataDataSource.count(featureDao.getDatabase(), featureDao.getTableName());
    }

    /**
     * Query for all features
     *
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures() {
        return queryFeatures(false);
    }

    /**
     * Query for all features
     *
     * @param distinct distinct rows
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return query(distinct, idQuery);
    }

    /**
     * Query for all features
     *
     * @param columns columns
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns) {
        return queryFeatures(false, columns);
    }

    /**
     * Query for all features
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return query(distinct, columns, idQuery);
    }

    /**
     * Count features
     *
     * @return count
     * @since 4.0.0
     */
    public int countFeatures() {
        return countFeatures(false, null);
    }

    /**
     * Count features
     *
     * @param column count column name
     * @return count
     * @since 4.0.0
     */
    public int countColumnFeatures(String column) {
        return countFeatures(false, column);
    }

    /**
     * Count features
     *
     * @param distinct distinct column values
     * @param column   count column name
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return count(distinct, column, idQuery, null, null);
    }

    /**
     * Query for features
     *
     * @param fieldValues field values
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(Map<String, Object> fieldValues) {
        return queryFeatures(false, fieldValues);
    }

    /**
     * Query for features
     *
     * @param distinct    distinct rows
     * @param fieldValues field values
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeatures(distinct, where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param columns     columns
     * @param fieldValues field values
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, Map<String, Object> fieldValues) {
        return queryFeatures(false, columns, fieldValues);
    }

    /**
     * Query for features
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param fieldValues field values
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeatures(distinct, columns, where, whereArgs);
    }

    /**
     * Count features
     *
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(Map<String, Object> fieldValues) {
        return countFeatures(false, null, fieldValues);
    }

    /**
     * Count features
     *
     * @param column      count column name
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, Map<String, Object> fieldValues) {
        return countFeatures(false, column, fieldValues);
    }

    /**
     * Count features
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return countFeatures(distinct, column, where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param where where clause
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(String where) {
        return queryFeatures(false, where);
    }

    /**
     * Query for features
     *
     * @param distinct distinct rows
     * @param where    where clause
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String where) {
        return queryFeatures(distinct, where, null);
    }

    /**
     * Query for features
     *
     * @param columns columns
     * @param where   where clause
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, String where) {
        return queryFeatures(false, columns, where);
    }

    /**
     * Query for features
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param where    where clause
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, String where) {
        return queryFeatures(distinct, columns, where, null);
    }

    /**
     * Count features
     *
     * @param where where clause
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(String where) {
        return countFeatures(false, null, where);
    }

    /**
     * Count features
     *
     * @param column count column name
     * @param where  where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, String where) {
        return countFeatures(false, column, where);
    }

    /**
     * Count features
     *
     * @param distinct distinct column values
     * @param column   count column name
     * @param where    where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, String where) {
        return countFeatures(distinct, column, where, null);
    }

    /**
     * Query for features
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(String where, String[] whereArgs) {
        return queryFeatures(false, where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param distinct  distinct rows
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String where, String[] whereArgs) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return query(distinct, idQuery, where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, String where, String[] whereArgs) {
        return queryFeatures(false, columns, where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, String where, String[] whereArgs) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return query(distinct, columns, idQuery, where, whereArgs);
    }

    /**
     * Count features
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(String where, String[] whereArgs) {
        return countFeatures(false, null, where, whereArgs);
    }

    /**
     * Count features
     *
     * @param column    count column name
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, String where, String[] whereArgs) {
        return countFeatures(false, column, where, whereArgs);
    }

    /**
     * Count features
     *
     * @param distinct  distinct column values
     * @param column    count column name
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, String where, String[] whereArgs) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return count(distinct, column, idQuery, where, whereArgs);
    }

    /**
     * Query for the bounds of the feature table index
     *
     * @return bounding box
     * @since 3.1.0
     */
    public BoundingBox getBoundingBox() {
        return geometryMetadataDataSource.getBoundingBox(featureDao.getDatabase(), featureDao.getTableName());
    }

    /**
     * Query for the feature index bounds and return in the provided projection
     *
     * @param projection desired projection
     * @return bounding box
     * @since 3.1.0
     */
    public BoundingBox getBoundingBox(Projection projection) {
        BoundingBox boundingBox = getBoundingBox();
        if (boundingBox != null && projection != null) {
            ProjectionTransform projectionTransform = featureDao.getProjection()
                    .getTransformation(projection);
            boundingBox = boundingBox.transform(projectionTransform);
        }
        return boundingBox;
    }

    /**
     * Query for Geometry Metadata within the bounding box, projected
     * correctly
     *
     * @param boundingBox bounding box
     * @return geometry metadata cursor
     * @since 1.1.0
     */
    public Cursor query(BoundingBox boundingBox) {
        return geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName(), boundingBox);
    }

    /**
     * Query for Geometry Metadata within the bounding box, projected
     * correctly
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @return geometry metadata cursor
     * @since 3.5.0
     */
    public Cursor query(String[] columns, BoundingBox boundingBox) {
        return geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName(), columns, boundingBox);
    }

    /**
     * Query for Geometry Metadata ids within the bounding box, projected
     * correctly
     *
     * @param boundingBox bounding box
     * @return geometry metadata cursor
     * @since 3.4.0
     */
    public Cursor queryIds(BoundingBox boundingBox) {
        return geometryMetadataDataSource.queryIds(featureDao.getDatabase(), featureDao.getTableName(), boundingBox);
    }

    /**
     * Query for Geometry Metadata count within the bounding box, projected
     * correctly
     *
     * @param boundingBox bounding box
     * @return count
     * @since 3.4.0
     */
    public long count(BoundingBox boundingBox) {
        return geometryMetadataDataSource.count(featureDao.getDatabase(), featureDao.getTableName(), boundingBox);
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox) {
        return queryFeatures(false, boundingBox);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, BoundingBox boundingBox) {
        return queryFeatures(distinct, boundingBox.buildEnvelope());
    }

    /**
     * Query for features within the bounding box
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, BoundingBox boundingBox) {
        return queryFeatures(false, columns, boundingBox);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, BoundingBox boundingBox) {
        return queryFeatures(distinct, columns, boundingBox.buildEnvelope());
    }

    /**
     * Count the features within the bounding box
     *
     * @param boundingBox bounding box
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox) {
        return countFeatures(false, null, boundingBox);
    }

    /**
     * Count the features within the bounding box
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox) {
        return countFeatures(false, column, boundingBox);
    }

    /**
     * Count the features within the bounding box
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, BoundingBox boundingBox) {
        return countFeatures(distinct, column, boundingBox.buildEnvelope());
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Map<String, Object> fieldValues) {
        return queryFeatures(false, boundingBox, fieldValues);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, BoundingBox boundingBox,
                                       Map<String, Object> fieldValues) {
        return queryFeatures(distinct, boundingBox.buildEnvelope(), fieldValues);
    }

    /**
     * Query for features within the bounding box
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, BoundingBox boundingBox,
                                       Map<String, Object> fieldValues) {
        return queryFeatures(false, columns, boundingBox, fieldValues);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, BoundingBox boundingBox,
                                       Map<String, Object> fieldValues) {
        return queryFeatures(distinct, columns, boundingBox.buildEnvelope(), fieldValues);
    }

    /**
     * Count the features within the bounding box
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox,
                             Map<String, Object> fieldValues) {
        return countFeatures(false, null, boundingBox, fieldValues);
    }

    /**
     * Count the features within the bounding box
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox,
                             Map<String, Object> fieldValues) {
        return countFeatures(false, column, boundingBox, fieldValues);
    }

    /**
     * Count the features within the bounding box
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, BoundingBox boundingBox,
                             Map<String, Object> fieldValues) {
        return countFeatures(distinct, column, boundingBox.buildEnvelope(), fieldValues);
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       String where) {
        return queryFeatures(false, boundingBox, where);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, BoundingBox boundingBox,
                                       String where) {
        return queryFeatures(distinct, boundingBox, where, null);
    }

    /**
     * Query for features within the bounding box
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, BoundingBox boundingBox,
                                       String where) {
        return queryFeatures(false, columns, boundingBox, where);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, BoundingBox boundingBox,
                                       String where) {
        return queryFeatures(distinct, columns, boundingBox, where, null);
    }

    /**
     * Count the features within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox, String where) {
        return countFeatures(false, null, boundingBox, where);
    }

    /**
     * Count the features within the bounding box
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param where       where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox, String where) {
        return countFeatures(false, column, boundingBox, where);
    }

    /**
     * Count the features within the bounding box
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param where       where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, BoundingBox boundingBox, String where) {
        return countFeatures(distinct, column, boundingBox, where, null);
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox, String where,
                                       String[] whereArgs) {
        return queryFeatures(false, boundingBox, where, whereArgs);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, BoundingBox boundingBox, String where,
                                       String[] whereArgs) {
        return queryFeatures(distinct, boundingBox.buildEnvelope(), where, whereArgs);
    }

    /**
     * Query for features within the bounding box
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, BoundingBox boundingBox, String where,
                                       String[] whereArgs) {
        return queryFeatures(false, columns, boundingBox, where, whereArgs);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, BoundingBox boundingBox, String where,
                                       String[] whereArgs) {
        return queryFeatures(distinct, columns, boundingBox.buildEnvelope(), where, whereArgs);
    }

    /**
     * Count the features within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox, String where,
                             String[] whereArgs) {
        return countFeatures(false, null, boundingBox, where, whereArgs);
    }

    /**
     * Count the features within the bounding box
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox, String where,
                             String[] whereArgs) {
        return countFeatures(false, column, boundingBox, where, whereArgs);
    }

    /**
     * Count the features within the bounding box
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, BoundingBox boundingBox, String where,
                             String[] whereArgs) {
        return countFeatures(distinct, column, boundingBox.buildEnvelope(), where, whereArgs);
    }

    /**
     * Query for Geometry Metadata within the bounding box in
     * the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return geometry metadata cursor
     * @since 1.1.0
     */
    public Cursor query(BoundingBox boundingBox,
                        Projection projection) {

        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);

        Cursor cursor = query(featureBoundingBox);

        return cursor;
    }

    /**
     * Query for Geometry Metadata within the bounding box in
     * the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return geometry metadata cursor
     * @since 3.5.0
     */
    public Cursor query(String[] columns, BoundingBox boundingBox,
                        Projection projection) {

        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);

        Cursor cursor = query(columns, featureBoundingBox);

        return cursor;
    }

    /**
     * Query for Geometry Metadata ids within the bounding box in
     * the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return geometry metadata cursor
     * @since 3.4.0
     */
    public Cursor queryIds(BoundingBox boundingBox,
                           Projection projection) {

        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);

        Cursor cursor = queryIds(featureBoundingBox);

        return cursor;
    }

    /**
     * Query for Geometry Metadata count within the bounding box in
     * the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return count
     * @since 1.1.0
     */
    public long count(BoundingBox boundingBox, Projection projection) {

        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);

        long count = count(featureBoundingBox);

        return count;
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection) {
        return queryFeatures(false, boundingBox, projection);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, BoundingBox boundingBox,
                                       Projection projection) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return queryFeatures(distinct, featureBoundingBox);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, BoundingBox boundingBox,
                                       Projection projection) {
        return queryFeatures(false, columns, boundingBox, projection);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, BoundingBox boundingBox,
                                       Projection projection) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return queryFeatures(distinct, columns, featureBoundingBox);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection) {
        return countFeatures(false, null, boundingBox, projection);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox, Projection projection) {
        return countFeatures(false, column, boundingBox, projection);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return countFeatures(distinct, column, featureBoundingBox);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection, Map<String, Object> fieldValues) {
        return queryFeatures(false, boundingBox, projection, fieldValues);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, BoundingBox boundingBox,
                                       Projection projection, Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return queryFeatures(distinct, featureBoundingBox, fieldValues);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, BoundingBox boundingBox,
                                       Projection projection, Map<String, Object> fieldValues) {
        return queryFeatures(false, columns, boundingBox, projection, fieldValues);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, BoundingBox boundingBox,
                                       Projection projection, Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return queryFeatures(distinct, columns, featureBoundingBox, fieldValues);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection,
                             Map<String, Object> fieldValues) {
        return countFeatures(false, null, boundingBox, projection, fieldValues);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox, Projection projection,
                             Map<String, Object> fieldValues) {
        return countFeatures(false, column, boundingBox, projection, fieldValues);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, BoundingBox boundingBox, Projection projection,
                             Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return countFeatures(distinct, column, featureBoundingBox, fieldValues);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection, String where) {
        return queryFeatures(false, boundingBox, projection, where);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, BoundingBox boundingBox,
                                       Projection projection, String where) {
        return queryFeatures(distinct, boundingBox, projection, where, null);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, BoundingBox boundingBox,
                                       Projection projection, String where) {
        return queryFeatures(false, columns, boundingBox, projection, where);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, BoundingBox boundingBox,
                                       Projection projection, String where) {
        return queryFeatures(distinct, columns, boundingBox, projection, where, null);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection,
                             String where) {
        return countFeatures(false, null, boundingBox, projection, where);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox, Projection projection,
                             String where) {
        return countFeatures(false, column, boundingBox, projection, where);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, BoundingBox boundingBox, Projection projection,
                             String where) {
        return countFeatures(distinct, column, boundingBox, projection, where, null);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection, String where, String[] whereArgs) {
        return queryFeatures(false, boundingBox, projection, where, whereArgs);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, BoundingBox boundingBox,
                                       Projection projection, String where, String[] whereArgs) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return queryFeatures(distinct, featureBoundingBox, where, whereArgs);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, BoundingBox boundingBox,
                                       Projection projection, String where, String[] whereArgs) {
        return queryFeatures(false, columns, boundingBox, projection, where, whereArgs);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, BoundingBox boundingBox,
                                       Projection projection, String where, String[] whereArgs) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return queryFeatures(distinct, columns, featureBoundingBox, where, whereArgs);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection,
                             String where, String[] whereArgs) {
        return countFeatures(false, null, boundingBox, projection, where, whereArgs);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox, Projection projection,
                             String where, String[] whereArgs) {
        return countFeatures(false, column, boundingBox, projection, where, whereArgs);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, BoundingBox boundingBox, Projection projection,
                             String where, String[] whereArgs) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return countFeatures(distinct, column, featureBoundingBox, where, whereArgs);
    }

    /**
     * Query for Geometry Metadata within the Geometry Envelope
     *
     * @param envelope geometry envelope
     * @return geometry metadata cursor
     * @since 1.1.0
     */
    public Cursor query(GeometryEnvelope envelope) {
        return geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName(), envelope);
    }

    /**
     * Query for Geometry Metadata within the Geometry Envelope
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @return geometry metadata cursor
     * @since 3.5.0
     */
    public Cursor query(String[] columns, GeometryEnvelope envelope) {
        return geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName(), columns, envelope);
    }

    /**
     * Query for Geometry Metadata idswithin the Geometry Envelope
     *
     * @param envelope geometry envelope
     * @return geometry metadata cursor
     * @since 1.1.0
     */
    public Cursor queryIds(GeometryEnvelope envelope) {
        return geometryMetadataDataSource.queryIds(featureDao.getDatabase(), featureDao.getTableName(), envelope);
    }

    /**
     * Query for Geometry Metadata count within the Geometry Envelope
     *
     * @param envelope geometry envelope
     * @return count
     * @since 3.4.0
     */
    public long count(GeometryEnvelope envelope) {
        return geometryMetadataDataSource.count(featureDao.getDatabase(), featureDao.getTableName(), envelope);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope) {
        return queryFeatures(false, envelope);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct distinct column values
     * @param envelope geometry envelope
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, GeometryEnvelope envelope) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return query(distinct, idQuery);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, GeometryEnvelope envelope) {
        return queryFeatures(false, columns, envelope);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct distinct column values
     * @param columns  columns
     * @param envelope geometry envelope
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, GeometryEnvelope envelope) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return query(distinct, columns, idQuery);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(GeometryEnvelope envelope) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return count(idQuery);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param column   count column name
     * @param envelope geometry envelope
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, GeometryEnvelope envelope) {
        return countFeatures(false, column, envelope);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param distinct distinct column values
     * @param column   count column name
     * @param envelope geometry envelope
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, GeometryEnvelope envelope) {
        return countFeatures(distinct, column, envelope, null, null);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope,
                                       Map<String, Object> fieldValues) {
        return queryFeatures(false, envelope, fieldValues);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct    distinct rows
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, GeometryEnvelope envelope,
                                       Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeatures(distinct, envelope, where, whereArgs);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, GeometryEnvelope envelope,
                                       Map<String, Object> fieldValues) {
        return queryFeatures(false, columns, envelope, fieldValues);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, GeometryEnvelope envelope,
                                       Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeatures(distinct, columns, envelope, where, whereArgs);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(GeometryEnvelope envelope,
                             Map<String, Object> fieldValues) {
        return countFeatures(false, null, envelope, fieldValues);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param column      count column name
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, GeometryEnvelope envelope,
                             Map<String, Object> fieldValues) {
        return countFeatures(false, column, envelope, fieldValues);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, GeometryEnvelope envelope,
                             Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return countFeatures(distinct, column, envelope, where, whereArgs);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope,
                                       String where) {
        return queryFeatures(false, envelope, where);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param where    where clause
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, GeometryEnvelope envelope,
                                       String where) {
        return queryFeatures(distinct, envelope, where, null);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, GeometryEnvelope envelope,
                                       String where) {
        return queryFeatures(false, columns, envelope, where);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, GeometryEnvelope envelope,
                                       String where) {
        return queryFeatures(distinct, columns, envelope, where, null);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(GeometryEnvelope envelope, String where) {
        return countFeatures(false, null, envelope, where, null);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param column   count column name
     * @param envelope geometry envelope
     * @param where    where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, GeometryEnvelope envelope, String where) {
        return countFeatures(false, column, envelope, where, null);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param distinct distinct column values
     * @param column   count column name
     * @param envelope geometry envelope
     * @param where    where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, GeometryEnvelope envelope, String where) {
        return countFeatures(distinct, column, envelope, where, null);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope,
                                       String where, String[] whereArgs) {
        return queryFeatures(false, envelope, where, whereArgs);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct  distinct rows
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, GeometryEnvelope envelope,
                                       String where, String[] whereArgs) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return query(distinct, idQuery, where, whereArgs);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature results
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, GeometryEnvelope envelope,
                                       String where, String[] whereArgs) {
        return queryFeatures(false, columns, envelope, where, whereArgs);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature results
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns, GeometryEnvelope envelope,
                                       String where, String[] whereArgs) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return query(distinct, columns, idQuery, where, whereArgs);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(GeometryEnvelope envelope, String where,
                             String[] whereArgs) {
        return countFeatures(false, null, envelope, where, whereArgs);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param column    count column name
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, GeometryEnvelope envelope, String where,
                             String[] whereArgs) {
        return countFeatures(false, column, envelope, where, whereArgs);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param distinct  distinct column values
     * @param column    count column name
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, GeometryEnvelope envelope, String where,
                             String[] whereArgs) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return count(distinct, column, idQuery, where, whereArgs);
    }

    /**
     * Get the bounding box in the feature projection from the bounding box in
     * the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature projected bounding box
     */
    private BoundingBox getFeatureBoundingBox(BoundingBox boundingBox,
                                              Projection projection) {
        ProjectionTransform projectionTransform = projection
                .getTransformation(featureDao.getProjection());
        BoundingBox featureBoundingBox = boundingBox
                .transform(projectionTransform);
        return featureBoundingBox;
    }

    /**
     * Get the Geometry Metadata for the current place in the cursor
     *
     * @param cursor cursor
     * @return geometry metadata
     * @since 1.1.0
     */
    public GeometryMetadata getGeometryMetadata(Cursor cursor) {
        return GeometryMetadataDataSource.createGeometryMetadata(cursor);
    }

    /**
     * Get the Geometry Metadata id for the current place in the cursor
     *
     * @param cursor cursor
     * @return geometry id
     * @since 3.4.0
     */
    public long getGeometryId(Cursor cursor) {
        return GeometryMetadataDataSource.getId(cursor);
    }

    /**
     * Get the feature row for the current place in the cursor
     *
     * @param cursor cursor
     * @return feature row
     * @since 1.1.0
     */
    public FeatureRow getFeatureRow(Cursor cursor) {
        GeometryMetadata geometryMetadata = getGeometryMetadata(cursor);
        FeatureRow featureRow = getFeatureRow(geometryMetadata);
        return featureRow;
    }

    /**
     * Get the feature row for the Geometry Metadata
     *
     * @param geometryMetadata geometry metadata
     * @return feature row
     * @since 1.1.0
     */
    public FeatureRow getFeatureRow(GeometryMetadata geometryMetadata) {

        long geomId = geometryMetadata.getId();

        // Get the row or lock for reading
        FeatureRow row = featureRowSync.getRowOrLock(geomId);
        if (row == null) {
            // Query for the row and set in the sync
            try {
                row = featureDao.queryForIdRow(geomId);
            } finally {
                featureRowSync.setRow(geomId, row);
            }
        }

        return row;
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param limit chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(int limit) {
        return queryFeaturesForChunk(getPkColumnName(), limit);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param limit  chunk limit
     * @param offset chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(int limit, long offset) {
        return queryFeaturesForChunk(getPkColumnName(), limit, offset);
    }

    /**
     * Query for all features, starting at the offset and returning no more than
     * the limit
     *
     * @param orderBy order by
     * @param limit   chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String orderBy, int limit) {
        return queryFeaturesForChunk(false, orderBy, limit);
    }

    /**
     * Query for all features, starting at the offset and returning no more than
     * the limit
     *
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, orderBy, limit, offset);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, int limit) {
        return queryFeaturesForChunk(distinct, getPkColumnName(), limit);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for all features, starting at the offset and returning no more than
     * the limit
     *
     * @param distinct distinct rows
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String orderBy, int limit) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return queryForChunk(distinct, idQuery, orderBy, limit, null);
    }

    /**
     * Query for all features, starting at the offset and returning no more than
     * the limit
     *
     * @param distinct distinct rows
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String orderBy, int limit, long offset) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return queryForChunk(distinct, idQuery, orderBy, limit, offset);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param columns columns
     * @param limit   chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, int limit) {
        return queryFeaturesForChunk(columns, getPkColumnName(), limit);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param columns columns
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, int limit,
                                               long offset) {
        return queryFeaturesForChunk(columns, getPkColumnName(), limit, offset);
    }

    /**
     * Query for all features, starting at the offset and returning no more than
     * the limit
     *
     * @param columns columns
     * @param orderBy order by
     * @param limit   chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, orderBy, limit);
    }

    /**
     * Query for all features, starting at the offset and returning no more than
     * the limit
     *
     * @param columns columns
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, orderBy, limit, offset);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, int limit) {
        return queryFeaturesForChunk(distinct, columns, getPkColumnName(),
                limit);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for all features, starting at the offset and returning no more than
     * the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String orderBy, int limit) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return queryForChunk(distinct, columns, idQuery,
                orderBy, limit, null);
    }

    /**
     * Query for all features, starting at the offset and returning no more than
     * the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String orderBy, int limit, long offset) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return queryForChunk(distinct, columns, idQuery,
                orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(
            Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(fieldValues, getPkColumnName(), limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(
            Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(fieldValues, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(
            Map<String, Object> fieldValues, String orderBy, int limit) {
        return queryFeaturesForChunk(false, fieldValues, orderBy, limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(
            Map<String, Object> fieldValues, String orderBy, int limit,
            long offset) {
        return queryFeaturesForChunk(false, fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct    distinct rows
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(distinct, fieldValues, getPkColumnName(),
                limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct    distinct rows
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(distinct, fieldValues, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeaturesForChunk(distinct, where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeaturesForChunk(distinct, where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param columns     columns
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(columns, fieldValues, getPkColumnName(),
                limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param columns     columns
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(columns, fieldValues, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, fieldValues, orderBy, limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, columns, fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(distinct, columns, fieldValues,
                getPkColumnName(), limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, Map<String, Object> fieldValues, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, columns, fieldValues,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, Map<String, Object> fieldValues, String orderBy,
                                               int limit) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeaturesForChunk(distinct, columns, where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, Map<String, Object> fieldValues, String orderBy,
                                               int limit, long offset) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeaturesForChunk(distinct, columns, where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param where where clause
     * @param limit chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String where,
                                                      int limit) {
        return queryFeaturesForChunk(where, getPkColumnName(), limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param where  where clause
     * @param limit  chunk limit
     * @param offset chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String where,
                                                      int limit, long offset) {
        return queryFeaturesForChunk(where, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param where   where clause
     * @param orderBy order by
     * @param limit   chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String where, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(false, where, orderBy, limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param where   where clause
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String where, String orderBy,
                                               int limit, long offset) {
        return queryFeaturesForChunk(false, where, orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct distinct rows
     * @param where    where clause
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String where, int limit) {
        return queryFeaturesForChunk(distinct, where, getPkColumnName(), limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct distinct rows
     * @param where    where clause
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String where, int limit, long offset) {
        return queryFeaturesForChunk(distinct, where, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct distinct rows
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, where, null, orderBy, limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct distinct rows
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, where, null, orderBy, limit,
                offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param columns columns
     * @param where   where clause
     * @param limit   chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      String where, int limit) {
        return queryFeaturesForChunk(columns, where, getPkColumnName(), limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param columns columns
     * @param where   where clause
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      String where, int limit, long offset) {
        return queryFeaturesForChunk(columns, where, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param columns columns
     * @param where   where clause
     * @param orderBy order by
     * @param limit   chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, where, orderBy, limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param columns columns
     * @param where   where clause
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, where, orderBy, limit,
                offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param where    where clause
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, String where, int limit) {
        return queryFeaturesForChunk(distinct, columns, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param where    where clause
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, String where, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns, where, null, orderBy,
                limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, columns, where, null, orderBy,
                limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String where,
                                               String[] whereArgs, int limit) {
        return queryFeaturesForChunk(where, whereArgs, getPkColumnName(),
                limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String where,
                                               String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(where, whereArgs, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String where,
                                               String[] whereArgs, String orderBy, int limit) {
        return queryFeaturesForChunk(false, where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct  distinct rows
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String[] whereArgs, int limit) {
        return queryFeaturesForChunk(distinct, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct  distinct rows
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(distinct, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct  distinct rows
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String[] whereArgs, String orderBy, int limit) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return queryForChunk(distinct, idQuery, where,
                whereArgs, orderBy, limit, null);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct  distinct rows
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return queryForChunk(distinct, idQuery, where,
                whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String[] whereArgs, int limit) {
        return queryFeaturesForChunk(columns, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(columns, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String[] whereArgs, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, columns, where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String[] whereArgs, int limit) {
        return queryFeaturesForChunk(distinct, columns, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String[] whereArgs, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, columns, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String[] whereArgs, String orderBy,
                                               int limit) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return queryForChunk(distinct, columns, idQuery, where, whereArgs, orderBy, limit, null);
    }

    /**
     * Query for features, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String[] whereArgs, String orderBy,
                                               int limit, long offset) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return queryForChunk(distinct, columns, idQuery, where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for rows within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(BoundingBox boundingBox,
                                int limit) {
        return queryForChunk(boundingBox, getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(BoundingBox boundingBox, int limit,
                                long offset) {
        return queryForChunk(boundingBox, getPkColumnName(), limit, offset);
    }

    /**
     * Query for rows within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(BoundingBox boundingBox,
                                String orderBy, int limit) {
        return queryForChunk(false, boundingBox, orderBy, limit);
    }

    /**
     * Query for rows within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(BoundingBox boundingBox,
                                String orderBy, int limit, long offset) {
        return queryForChunk(false, boundingBox, orderBy, limit, offset);
    }

    /**
     * Query for rows within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                BoundingBox boundingBox, int limit) {
        return queryForChunk(distinct, boundingBox, getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                BoundingBox boundingBox, int limit, long offset) {
        return queryForChunk(distinct, boundingBox, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for rows within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                BoundingBox boundingBox, String orderBy, int limit) {
        return queryForChunk(distinct, boundingBox.buildEnvelope(), orderBy,
                limit);
    }

    /**
     * Query for rows within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                BoundingBox boundingBox, String orderBy, int limit, long offset) {
        return queryForChunk(distinct, boundingBox.buildEnvelope(), orderBy,
                limit, offset);
    }

    /**
     * Query for rows within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                BoundingBox boundingBox, int limit) {
        return queryForChunk(columns, boundingBox, getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                BoundingBox boundingBox, int limit, long offset) {
        return queryForChunk(columns, boundingBox, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for rows within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                BoundingBox boundingBox, String orderBy, int limit) {
        return queryForChunk(false, columns, boundingBox, orderBy, limit);
    }

    /**
     * Query for rows within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                BoundingBox boundingBox, String orderBy, int limit, long offset) {
        return queryForChunk(false, columns, boundingBox, orderBy, limit,
                offset);
    }

    /**
     * Query for rows within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                BoundingBox boundingBox, int limit) {
        return queryForChunk(distinct, columns, boundingBox, getPkColumnName(),
                limit);
    }

    /**
     * Query for rows within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                BoundingBox boundingBox, int limit, long offset) {
        return queryForChunk(distinct, columns, boundingBox, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for rows within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                BoundingBox boundingBox, String orderBy, int limit) {
        return queryForChunk(distinct, columns, boundingBox.buildEnvelope(),
                orderBy, limit);
    }

    /**
     * Query for rows within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                BoundingBox boundingBox, String orderBy, int limit, long offset) {
        return queryForChunk(distinct, columns, boundingBox.buildEnvelope(),
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               int limit) {
        return queryFeaturesForChunk(boundingBox, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               int limit, long offset) {
        return queryFeaturesForChunk(boundingBox, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, boundingBox, orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, boundingBox, orderBy, limit,
                offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, getPkColumnName(),
                limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox.buildEnvelope(),
                orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox.buildEnvelope(),
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, int limit) {
        return queryFeaturesForChunk(columns, boundingBox, getPkColumnName(),
                limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, int limit, long offset) {
        return queryFeaturesForChunk(columns, boundingBox, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, boundingBox, orderBy,
                limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, boundingBox, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(distinct, columns,
                boundingBox.buildEnvelope(), orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, String orderBy,
                                               int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns,
                boundingBox.buildEnvelope(), orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(boundingBox, fieldValues,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(boundingBox, fieldValues,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        return queryFeaturesForChunk(false, boundingBox, fieldValues, orderBy,
                limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, boundingBox, fieldValues, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Map<String, Object> fieldValues,
                                               int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, fieldValues,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Map<String, Object> fieldValues, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, fieldValues,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Map<String, Object> fieldValues,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox.buildEnvelope(),
                fieldValues, orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Map<String, Object> fieldValues,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox.buildEnvelope(),
                fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Map<String, Object> fieldValues,
                                               int limit) {
        return queryFeaturesForChunk(columns, boundingBox, fieldValues,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Map<String, Object> fieldValues, int limit,
                                               long offset) {
        return queryFeaturesForChunk(columns, boundingBox, fieldValues,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Map<String, Object> fieldValues,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, boundingBox, fieldValues,
                orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Map<String, Object> fieldValues,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, boundingBox, fieldValues,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox,
                fieldValues, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox,
                fieldValues, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns,
                boundingBox.buildEnvelope(), fieldValues, orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, columns,
                boundingBox.buildEnvelope(), fieldValues, orderBy, limit,
                offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(
            BoundingBox boundingBox, String where, int limit) {
        return queryFeaturesForChunk(boundingBox, where, getPkColumnName(),
                limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(
            BoundingBox boundingBox, String where, int limit, long offset) {
        return queryFeaturesForChunk(boundingBox, where, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               String where, String orderBy, int limit) {
        return queryFeaturesForChunk(false, boundingBox, where, orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               String where, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, boundingBox, where, orderBy, limit,
                offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      BoundingBox boundingBox, String where, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      BoundingBox boundingBox, String where, int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, String where, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, where, null,
                orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, String where, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, where, null,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      BoundingBox boundingBox, String where, int limit) {
        return queryFeaturesForChunk(columns, boundingBox, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      BoundingBox boundingBox, String where, int limit, long offset) {
        return queryFeaturesForChunk(columns, boundingBox, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, String where, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, boundingBox, where,
                orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, String where, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, columns, boundingBox, where,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, BoundingBox boundingBox, String where,
                                                      int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, BoundingBox boundingBox, String where, int limit,
                                                      long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, String where,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, where,
                null, orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, String where,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, where,
                null, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               String where, String[] whereArgs, int limit) {
        return queryFeaturesForChunk(boundingBox, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               String where, String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(boundingBox, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               String where, String[] whereArgs, String orderBy, int limit) {
        return queryFeaturesForChunk(false, boundingBox, where, whereArgs,
                orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, boundingBox, where, whereArgs,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, String where, String[] whereArgs,
                                               int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, String where, String[] whereArgs,
                                               int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, String where, String[] whereArgs,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox.buildEnvelope(),
                where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, String where, String[] whereArgs,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox.buildEnvelope(),
                where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, String where, String[] whereArgs,
                                               int limit) {
        return queryFeaturesForChunk(columns, boundingBox, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, String where, String[] whereArgs,
                                               int limit, long offset) {
        return queryFeaturesForChunk(columns, boundingBox, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, String where, String[] whereArgs,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, boundingBox, where,
                whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, String where, String[] whereArgs,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, boundingBox, where,
                whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, String where,
                                               String[] whereArgs, int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, where,
                whereArgs, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, String where,
                                               String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, where,
                whereArgs, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, String where,
                                               String[] whereArgs, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns,
                boundingBox.buildEnvelope(), where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounding box, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns,
                boundingBox.buildEnvelope(), where, whereArgs, orderBy, limit,
                offset);
    }

    /**
     * Query for rows within the bounding box in the provided projection ordered
     * by id, starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(BoundingBox boundingBox,
                                Projection projection, int limit) {
        return queryForChunk(boundingBox, projection, getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounding box in the provided projection ordered
     * by id, starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(BoundingBox boundingBox,
                                Projection projection, int limit, long offset) {
        return queryForChunk(boundingBox, projection, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for rows within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(BoundingBox boundingBox,
                                Projection projection, String orderBy, int limit) {
        return queryForChunk(false, boundingBox, projection, orderBy, limit);
    }

    /**
     * Query for rows within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(BoundingBox boundingBox,
                                Projection projection, String orderBy, int limit, long offset) {
        return queryForChunk(false, boundingBox, projection, orderBy, limit,
                offset);
    }

    /**
     * Query for rows within the bounding box in the provided projection ordered
     * by id, starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                BoundingBox boundingBox, Projection projection, int limit) {
        return queryForChunk(distinct, boundingBox, projection,
                getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounding box in the provided projection ordered
     * by id, starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                BoundingBox boundingBox, Projection projection, int limit,
                                long offset) {
        return queryForChunk(distinct, boundingBox, projection,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for rows within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                BoundingBox boundingBox, Projection projection, String orderBy,
                                int limit) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryForChunk(distinct, featureBoundingBox, orderBy, limit);
    }

    /**
     * Query for rows within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                BoundingBox boundingBox, Projection projection, String orderBy,
                                int limit, long offset) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryForChunk(distinct, featureBoundingBox, orderBy, limit,
                offset);
    }

    /**
     * Query for rows within the bounding box in the provided projection ordered
     * by id, starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                BoundingBox boundingBox, Projection projection, int limit) {
        return queryForChunk(columns, boundingBox, projection,
                getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounding box in the provided projection ordered
     * by id, starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                BoundingBox boundingBox, Projection projection, int limit,
                                long offset) {
        return queryForChunk(columns, boundingBox, projection,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for rows within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                BoundingBox boundingBox, Projection projection, String orderBy,
                                int limit) {
        return queryForChunk(false, columns, boundingBox, projection, orderBy,
                limit);
    }

    /**
     * Query for rows within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                BoundingBox boundingBox, Projection projection, String orderBy,
                                int limit, long offset) {
        return queryForChunk(false, columns, boundingBox, projection, orderBy,
                limit, offset);
    }

    /**
     * Query for rows within the bounding box in the provided projection ordered
     * by id, starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                BoundingBox boundingBox, Projection projection, int limit) {
        return queryForChunk(distinct, columns, boundingBox, projection,
                getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounding box in the provided projection ordered
     * by id, starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                BoundingBox boundingBox, Projection projection, int limit,
                                long offset) {
        return queryForChunk(distinct, columns, boundingBox, projection,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for rows within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                BoundingBox boundingBox, Projection projection, String orderBy,
                                int limit) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryForChunk(distinct, columns, featureBoundingBox, orderBy,
                limit);
    }

    /**
     * Query for rows within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                BoundingBox boundingBox, Projection projection, String orderBy,
                                int limit, long offset) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryForChunk(distinct, columns, featureBoundingBox, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, int limit) {
        return queryFeaturesForChunk(boundingBox, projection, getPkColumnName(),
                limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, int limit, long offset) {
        return queryFeaturesForChunk(boundingBox, projection, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, String orderBy, int limit) {
        return queryFeaturesForChunk(false, boundingBox, projection, orderBy,
                limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, boundingBox, projection, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, projection,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, projection,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String orderBy,
                                               int limit) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, featureBoundingBox, orderBy,
                limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String orderBy,
                                               int limit, long offset) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, featureBoundingBox, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, int limit) {
        return queryFeaturesForChunk(columns, boundingBox, projection,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, int limit,
                                               long offset) {
        return queryFeaturesForChunk(columns, boundingBox, projection,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(false, columns, boundingBox, projection,
                orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, String orderBy,
                                               int limit, long offset) {
        return queryFeaturesForChunk(false, columns, boundingBox, projection,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String orderBy, int limit) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, columns, featureBoundingBox,
                orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String orderBy, int limit, long offset) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, columns, featureBoundingBox,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(boundingBox, projection, fieldValues,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, Map<String, Object> fieldValues, int limit,
                                               long offset) {
        return queryFeaturesForChunk(boundingBox, projection, fieldValues,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, Map<String, Object> fieldValues,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, boundingBox, projection,
                fieldValues, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, Map<String, Object> fieldValues,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, boundingBox, projection,
                fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, projection,
                fieldValues, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, projection,
                fieldValues, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, featureBoundingBox, fieldValues,
                orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, featureBoundingBox, fieldValues,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(columns, boundingBox, projection,
                fieldValues, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(columns, boundingBox, projection,
                fieldValues, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, boundingBox, projection,
                fieldValues, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, columns, boundingBox, projection,
                fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                fieldValues, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                fieldValues, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, columns, featureBoundingBox,
                fieldValues, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, columns, featureBoundingBox,
                fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(
            BoundingBox boundingBox, Projection projection, String where,
            int limit) {
        return queryFeaturesForChunk(boundingBox, projection, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(
            BoundingBox boundingBox, Projection projection, String where,
            int limit, long offset) {
        return queryFeaturesForChunk(boundingBox, projection, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, String where, String orderBy, int limit) {
        return queryFeaturesForChunk(false, boundingBox, projection, where,
                orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, String where, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, boundingBox, projection, where,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      BoundingBox boundingBox, Projection projection, String where,
                                                      int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, projection, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      BoundingBox boundingBox, Projection projection, String where,
                                                      int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, projection, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, projection, where,
                null, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, projection, where,
                null, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      BoundingBox boundingBox, Projection projection, String where,
                                                      int limit) {
        return queryFeaturesForChunk(columns, boundingBox, projection, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      BoundingBox boundingBox, Projection projection, String where,
                                                      int limit, long offset) {
        return queryFeaturesForChunk(columns, boundingBox, projection, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, boundingBox, projection,
                where, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, boundingBox, projection,
                where, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, BoundingBox boundingBox, Projection projection,
                                                      String where, int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                where, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, BoundingBox boundingBox, Projection projection,
                                                      String where, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                where, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String where, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                where, null, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String where, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                where, null, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, String where, String[] whereArgs,
                                               int limit) {
        return queryFeaturesForChunk(boundingBox, projection, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, String where, String[] whereArgs, int limit,
                                               long offset) {
        return queryFeaturesForChunk(boundingBox, projection, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, String where, String[] whereArgs,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, boundingBox, projection, where,
                whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(BoundingBox boundingBox,
                                               Projection projection, String where, String[] whereArgs,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, boundingBox, projection, where,
                whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, int limit) {
        return queryFeaturesForChunk(distinct, boundingBox, projection, where,
                whereArgs, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(distinct, boundingBox, projection, where,
                whereArgs, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, String orderBy, int limit) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, featureBoundingBox, where,
                whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, featureBoundingBox, where,
                whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, int limit) {
        return queryFeaturesForChunk(columns, boundingBox, projection, where,
                whereArgs, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(columns, boundingBox, projection, where,
                whereArgs, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, boundingBox, projection,
                where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, boundingBox, projection,
                where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String where, String[] whereArgs, int limit) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                where, whereArgs, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounding box in the provided projection
     * ordered by id, starting at the offset and returning no more than the
     * limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String where, String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, boundingBox, projection,
                where, whereArgs, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String where, String[] whereArgs, String orderBy, int limit) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, columns, featureBoundingBox,
                where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounding box in the provided projection,
     * starting at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(boundingBox,
                projection);
        return queryFeaturesForChunk(distinct, columns, featureBoundingBox,
                where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(GeometryEnvelope envelope,
                                int limit) {
        return queryForChunk(envelope, getPkColumnName(), limit);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(GeometryEnvelope envelope,
                                int limit, long offset) {
        return queryForChunk(envelope, getPkColumnName(), limit, offset);
    }

    /**
     * Query for rows within the geometry envelope, starting at the offset and
     * returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(GeometryEnvelope envelope,
                                String orderBy, int limit) {
        return queryForChunk(false, envelope, orderBy, limit);
    }

    /**
     * Query for rows within the geometry envelope, starting at the offset and
     * returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(GeometryEnvelope envelope,
                                String orderBy, int limit, long offset) {
        return queryForChunk(false, envelope, orderBy, limit, offset);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                GeometryEnvelope envelope, int limit) {
        return queryForChunk(distinct, envelope, getPkColumnName(), limit);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                GeometryEnvelope envelope, int limit, long offset) {
        return queryForChunk(distinct, envelope, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for rows within the geometry envelope, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                GeometryEnvelope envelope, String orderBy, int limit) {
        return queryForChunk(distinct, null, envelope, orderBy, limit);
    }

    /**
     * Query for rows within the geometry envelope, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct,
                                GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryForChunk(distinct, null, envelope, orderBy, limit, offset);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                GeometryEnvelope envelope, int limit) {
        return queryForChunk(columns, envelope, getPkColumnName(), limit);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                GeometryEnvelope envelope, int limit, long offset) {
        return queryForChunk(columns, envelope, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for rows within the geometry envelope, starting at the offset and
     * returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                GeometryEnvelope envelope, String orderBy, int limit) {
        return queryForChunk(false, columns, envelope, orderBy, limit);
    }

    /**
     * Query for rows within the geometry envelope, starting at the offset and
     * returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(String[] columns,
                                GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryForChunk(false, columns, envelope, orderBy, limit, offset);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                GeometryEnvelope envelope, int limit) {
        return queryForChunk(distinct, columns, envelope, getPkColumnName(),
                limit);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                GeometryEnvelope envelope, int limit, long offset) {
        return queryForChunk(distinct, columns, envelope, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for rows within the geometry envelope, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                GeometryEnvelope envelope, String orderBy, int limit) {
        return geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName(),
                distinct, columns, envelope, orderBy, String.valueOf(limit));
    }

    /**
     * Query for rows within the geometry envelope, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.2.1
     */
    public Cursor queryForChunk(boolean distinct, String[] columns,
                                GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName(),
                distinct, columns, envelope, orderBy, featureDao.buildLimit(limit, offset));
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               int limit) {
        return queryFeaturesForChunk(envelope, getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               int limit, long offset) {
        return queryFeaturesForChunk(envelope, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, envelope, orderBy, limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, envelope, orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, int limit) {
        return queryFeaturesForChunk(distinct, envelope, getPkColumnName(),
                limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, null, envelope, orderBy, limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, null, envelope, orderBy, limit,
                offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, int limit) {
        return queryFeaturesForChunk(columns, envelope, getPkColumnName(),
                limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, int limit, long offset) {
        return queryFeaturesForChunk(columns, envelope, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, envelope, orderBy, limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, envelope, orderBy, limit,
                offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope, null, null, orderBy,
                limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String orderBy,
                                               int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope, null, null, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(envelope, fieldValues, getPkColumnName(),
                limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(envelope, fieldValues, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        return queryFeaturesForChunk(false, envelope, fieldValues, orderBy,
                limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, envelope, fieldValues, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               int limit) {
        return queryFeaturesForChunk(distinct, envelope, fieldValues,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope, fieldValues,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, null, envelope, fieldValues, orderBy,
                limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, null, envelope, fieldValues, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               int limit) {
        return queryFeaturesForChunk(columns, envelope, fieldValues,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               int limit, long offset) {
        return queryFeaturesForChunk(columns, envelope, fieldValues,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, envelope, fieldValues,
                orderBy, limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, envelope, fieldValues,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope, fieldValues,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope, fieldValues,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeaturesForChunk(distinct, columns, envelope, where, whereArgs,
                orderBy, limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeaturesForChunk(distinct, columns, envelope, where, whereArgs,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(
            GeometryEnvelope envelope, String where, int limit) {
        return queryFeaturesForChunk(envelope, where, getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(
            GeometryEnvelope envelope, String where, int limit, long offset) {
        return queryFeaturesForChunk(envelope, where, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               String where, String orderBy, int limit) {
        return queryFeaturesForChunk(false, envelope, where, orderBy, limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               String where, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, envelope, where, orderBy, limit,
                offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      GeometryEnvelope envelope, String where, int limit) {
        return queryFeaturesForChunk(distinct, envelope, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      GeometryEnvelope envelope, String where, int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(distinct, envelope, where, null, orderBy,
                limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, envelope, where, null, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      GeometryEnvelope envelope, String where, int limit) {
        return queryFeaturesForChunk(columns, envelope, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      GeometryEnvelope envelope, String where, int limit, long offset) {
        return queryFeaturesForChunk(columns, envelope, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, String where, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(false, columns, envelope, where, orderBy,
                limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, String where, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, columns, envelope, where, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, GeometryEnvelope envelope, String where,
                                                      int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, GeometryEnvelope envelope, String where,
                                                      int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope, where, null,
                orderBy, limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope, where, null,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               String where, String[] whereArgs, int limit) {
        return queryFeaturesForChunk(envelope, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               String where, String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(envelope, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               String where, String[] whereArgs, String orderBy, int limit) {
        return queryFeaturesForChunk(false, envelope, where, whereArgs, orderBy,
                limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(GeometryEnvelope envelope,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, envelope, where, whereArgs, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               int limit) {
        return queryFeaturesForChunk(distinct, envelope, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, null, envelope, where, whereArgs,
                orderBy, limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, null, envelope, where, whereArgs,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               int limit) {
        return queryFeaturesForChunk(columns, envelope, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               int limit, long offset) {
        return queryFeaturesForChunk(columns, envelope, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, envelope, where, whereArgs,
                orderBy, limit);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, envelope, where, whereArgs,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String[] whereArgs, int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope, where,
                whereArgs, getPkColumnName(), limit);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope, where,
                whereArgs, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String[] whereArgs, String orderBy, int limit) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return queryForChunk(distinct, columns,
                idQuery, where, whereArgs, orderBy, limit,
                null);
    }

    /**
     * Query for features within the geometry envelope, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return queryForChunk(distinct, columns,
                idQuery, where, whereArgs, orderBy, limit,
                offset);
    }

    /**
     * Build a feature indexer nested id query from the cursor
     *
     * @param cursor cursor
     * @return id query
     */
    private FeatureIndexerIdQuery buildIdQuery(Cursor cursor) {
        FeatureIndexerIdQuery query = null;
        FeatureIndexMetadataResults results = new FeatureIndexMetadataResults(this, cursor);
        try {
            query = new FeatureIndexerIdQuery();
            for (long id : results.ids()) {
                query.addArgument(id);
            }
        } finally {
            results.close();
        }
        return query;
    }

    /**
     * Query using the id query
     *
     * @param distinct distinct rows
     * @param idQuery  id query
     * @return feature cursor
     */
    private FeatureCursor query(boolean distinct, FeatureIndexerIdQuery idQuery) {
        return query(distinct, idQuery, null, null);
    }

    /**
     * Query using the id query
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param idQuery  id query
     * @return feature cursor
     */
    private FeatureCursor query(boolean distinct, String[] columns, FeatureIndexerIdQuery idQuery) {
        return query(distinct, columns, idQuery, null, null);
    }

    /**
     * Count using the id query
     *
     * @param idQuery id query
     * @return feature count
     */
    private int count(FeatureIndexerIdQuery idQuery) {
        return idQuery.getCount();
    }

    /**
     * Query using the id query and criteria
     *
     * @param distinct  distinct rows
     * @param idQuery   id query
     * @param where     where statement
     * @param whereArgs where args
     * @return feature cursor
     */
    private FeatureCursor query(boolean distinct, FeatureIndexerIdQuery idQuery, String where, String[] whereArgs) {
        return query(distinct, null, idQuery, where, whereArgs);
    }

    /**
     * Query using the id query and criteria
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param idQuery   id query
     * @param where     where statement
     * @param whereArgs where args
     * @return feature cursor
     */
    private FeatureCursor query(boolean distinct, String[] columns, FeatureIndexerIdQuery idQuery, String where, String[] whereArgs) {
        FeatureCursor cursor = null;
        if (columns == null) {
            columns = featureDao.getColumnNames();
        }
        if (idQuery.aboveMaxArguments(whereArgs)) {
            cursor = new FeatureIndexerIdCursor(columns, featureDao.query(distinct, columns, where, whereArgs), idQuery);
        } else {
            cursor = featureDao.queryIn(distinct, columns, idQuery.getSql(), idQuery.getArgs(), where, whereArgs);
        }
        return cursor;
    }

    /**
     * Query using the id query
     *
     * @param distinct distinct rows
     * @param idQuery  id query
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     */
    private FeatureCursor queryForChunk(boolean distinct, FeatureIndexerIdQuery idQuery, String orderBy, int limit,
                                        Long offset) {
        return queryForChunk(distinct, idQuery, null, null, orderBy, limit, offset);
    }

    /**
     * Query using the id query
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param idQuery  id query
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return feature cursor
     */
    private FeatureCursor queryForChunk(boolean distinct, String[] columns, FeatureIndexerIdQuery idQuery, String orderBy, int limit,
                                        Long offset) {
        return queryForChunk(distinct, columns, idQuery, null, null, orderBy, limit, offset);
    }

    /**
     * Query using the id query and criteria
     *
     * @param distinct  distinct rows
     * @param idQuery   id query
     * @param where     where statement
     * @param whereArgs where args
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     */
    private FeatureCursor queryForChunk(boolean distinct, FeatureIndexerIdQuery idQuery, String where, String[] whereArgs, String orderBy, int limit,
                                        Long offset) {
        return queryForChunk(distinct, null, idQuery, where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query using the id query and criteria
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param idQuery   id query
     * @param where     where statement
     * @param whereArgs where args
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return feature cursor
     */
    private FeatureCursor queryForChunk(boolean distinct, String[] columns, FeatureIndexerIdQuery idQuery, String where, String[] whereArgs, String orderBy, int limit,
                                        Long offset) {
        FeatureCursor cursor = null;
        if (columns == null) {
            columns = featureDao.getColumnNames();
        }
        if (orderBy == null) {
            orderBy = featureDao.getPkColumnName();
        }
        String limitValue = null;
        if (offset == null) {
            limitValue = String.valueOf(limit);
        } else {
            limitValue = featureDao.buildLimit(limit, offset);
        }
        if (idQuery.aboveMaxArguments(whereArgs)) {
            cursor = new FeatureIndexerIdCursor(columns, featureDao.query(distinct, columns, where, whereArgs, orderBy, limitValue), idQuery);
        } else {
            cursor = featureDao.queryInForChunk(distinct, columns, idQuery.getSql(), idQuery.getArgs(), where, whereArgs, orderBy, limitValue);
        }
        return cursor;
    }

    /**
     * Count using the id query and criteria
     *
     * @param distinct  distinct column values
     * @param column    count column name
     * @param idQuery   id query
     * @param where     where statement
     * @param whereArgs where args
     * @return feature count
     */
    private int count(boolean distinct, String column, FeatureIndexerIdQuery idQuery, String where, String[] whereArgs) {
        int count = 0;
        if (idQuery.aboveMaxArguments(whereArgs)) {
            if (column != null) {
                throw new GeoPackageException("Unable to count column with too many query arguments. column: " + column);
            }
            FeatureCursor cursor = featureDao.query(where, whereArgs);
            try {
                while (cursor.moveToNext()) {
                    FeatureRow featureRow = cursor.getRow();
                    if (idQuery.hasId(featureRow.getId())) {
                        count++;
                    }
                }
            } finally {
                cursor.close();
            }
        } else {
            count = featureDao.countIn(distinct, column, idQuery.getSql(), idQuery.getArgs(), where, whereArgs);
        }
        return count;
    }

}
