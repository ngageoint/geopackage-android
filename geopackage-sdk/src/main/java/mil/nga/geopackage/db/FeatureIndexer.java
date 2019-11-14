package mil.nga.geopackage.db;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.Date;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
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
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionTransform;
import mil.nga.sf.util.GeometryEnvelopeBuilder;

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
                    envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
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
     * Query for all Geometry Metadata
     *
     * @return geometry metadata cursor
     * @since 1.1.0
     */
    public Cursor query() {
        return geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName());
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
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return query(idQuery);
    }

    /**
     * Query for features
     *
     * @param fieldValues field values
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeatures(where, whereArgs);
    }

    /**
     * Count features
     *
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return countFeatures(where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param where where clause
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(String where) {
        return queryFeatures(where, null);
    }

    /**
     * Count features
     *
     * @param where where clause
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(String where) {
        return countFeatures(where, null);
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
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return query(idQuery, where, whereArgs);
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
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds());
        return count(idQuery, where, whereArgs);
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
        return queryFeatures(boundingBox.buildEnvelope());
    }

    /**
     * Count the features within the bounding box
     *
     * @param boundingBox bounding box
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox) {
        return countFeatures(boundingBox.buildEnvelope());
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
        return queryFeatures(boundingBox.buildEnvelope(), fieldValues);
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
        return countFeatures(boundingBox.buildEnvelope(), fieldValues);
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
        return queryFeatures(boundingBox, where, null);
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
        return countFeatures(boundingBox, where, null);
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
        return queryFeatures(boundingBox.buildEnvelope(), where, whereArgs);
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
        return countFeatures(boundingBox.buildEnvelope(), where, whereArgs);
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
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return queryFeatures(featureBoundingBox);
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
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return countFeatures(featureBoundingBox);
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
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return queryFeatures(featureBoundingBox, fieldValues);
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
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return countFeatures(featureBoundingBox, fieldValues);
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
        return queryFeatures(boundingBox, projection, where, null);
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
        return countFeatures(boundingBox, projection, where, null);
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
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return queryFeatures(featureBoundingBox, where, whereArgs);
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
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);
        return countFeatures(featureBoundingBox, where, whereArgs);
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
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return query(idQuery);
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
     * Query for features within the geometry envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature results
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope,
                                       Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return queryFeatures(envelope, where, whereArgs);
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
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return countFeatures(envelope, where, whereArgs);
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
        return queryFeatures(envelope, where, null);
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
        return countFeatures(envelope, where, null);
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
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return query(idQuery, where, whereArgs);
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
        FeatureIndexerIdQuery idQuery = buildIdQuery(queryIds(envelope));
        return count(idQuery, where, whereArgs);
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
        GeometryMetadata geometryMetadata = GeometryMetadataDataSource.createGeometryMetadata(cursor);
        return geometryMetadata;
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
     * @param idQuery id query
     * @return feature cursor
     */
    private FeatureCursor query(FeatureIndexerIdQuery idQuery) {
        return query(idQuery, null, null);
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
     * @param idQuery   id query
     * @param where     where statement
     * @param whereArgs where args
     * @return feature cursor
     */
    private FeatureCursor query(FeatureIndexerIdQuery idQuery, String where, String[] whereArgs) {
        FeatureCursor cursor = null;
        if (idQuery.aboveMaxArguments(whereArgs)) {
            cursor = new FeatureIndexerIdCursor(featureDao.query(where, whereArgs), idQuery);
        } else {
            cursor = featureDao.queryIn(idQuery.getSql(), idQuery.getArgs(), where, whereArgs);
        }
        return cursor;
    }

    /**
     * Count using the id query and criteria
     *
     * @param idQuery   id query
     * @param where     where statement
     * @param whereArgs where args
     * @return feature count
     */
    private int count(FeatureIndexerIdQuery idQuery, String where, String[] whereArgs) {
        int count = 0;
        if (idQuery.aboveMaxArguments(whereArgs)) {
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
            count = featureDao.countIn(idQuery.getSql(), idQuery.getArgs(), where, whereArgs);
        }
        return count;
    }

}
