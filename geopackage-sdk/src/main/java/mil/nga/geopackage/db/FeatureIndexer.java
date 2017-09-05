package mil.nga.geopackage.db;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.Date;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.db.metadata.GeoPackageMetadataDb;
import mil.nga.geopackage.db.metadata.GeometryMetadata;
import mil.nga.geopackage.db.metadata.GeometryMetadataDataSource;
import mil.nga.geopackage.db.metadata.TableMetadata;
import mil.nga.geopackage.db.metadata.TableMetadataDataSource;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureRowSync;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryEnvelope;
import mil.nga.wkb.util.GeometryEnvelopeBuilder;

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
     * Constructor
     *
     * @param context
     * @param featureDao
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
     * @param progress
     */
    public void setProgress(GeoPackageProgress progress) {
        this.progress = progress;
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
     * @param row
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

        // Index all features
        FeatureCursor cursor = featureDao.queryForAll();
        try {
            while ((progress == null || progress.isActive()) && cursor.moveToNext()) {
                try {
                    FeatureRow row = cursor.getRow();
                    boolean indexed = index(metadata.getGeoPackageId(), row, false);
                    if (indexed) {
                        count++;
                    }
                    if (progress != null) {
                        progress.addProgress(1);
                    }
                } catch (Exception e) {
                    Log.e(FeatureIndexer.class.getSimpleName(), "Failed to index feature. Table: "
                            + featureDao.getTableName() + ", Position: " + cursor.getPosition(), e);
                }
            }
        } finally {
            cursor.close();
        }

        // Update the last indexed time
        if (progress == null || progress.isActive()) {
            updateLastIndexed(db, metadata.getGeoPackageId());
        }

        return count;
    }

    /**
     * Index the feature row
     *
     * @param geoPackageId
     * @param row
     * @param possibleUpdate
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
     * @param db
     * @param geoPackageId
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
     * @param row
     * @return true if deleted
     * @since 1.1.0
     */
    public boolean deleteIndex(FeatureRow row) {
        return deleteIndex(row.getId());
    }

    /**
     * Delete the index for the geometry id
     *
     * @param geomId
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
        Cursor cursor = geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName());
        return cursor;
    }

    /**
     * Query for all Geometry Metadata count
     *
     * @return count
     * @since 1.1.0
     */
    public int count() {
        int count = geometryMetadataDataSource.count(featureDao.getDatabase(), featureDao.getTableName());
        return count;
    }

    /**
     * Query for Geometry Metadata within the bounding box, projected
     * correctly
     *
     * @param boundingBox
     * @return geometry metadata cursor
     * @since 1.1.0
     */
    public Cursor query(BoundingBox boundingBox) {
        Cursor cursor = geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName(), boundingBox);
        return cursor;
    }

    /**
     * Query for Geometry Metadata count within the bounding box, projected
     * correctly
     *
     * @param boundingBox
     * @return count
     * @since 1.1.0
     */
    public int count(BoundingBox boundingBox) {
        int count = geometryMetadataDataSource.count(featureDao.getDatabase(), featureDao.getTableName(), boundingBox);
        return count;
    }

    /**
     * Query for Geometry Metadata within the Geometry Envelope
     *
     * @param envelope
     * @return geometry metadata cursor
     * @since 1.1.0
     */
    public Cursor query(GeometryEnvelope envelope) {
        Cursor cursor = geometryMetadataDataSource.query(featureDao.getDatabase(), featureDao.getTableName(), envelope);
        return cursor;
    }

    /**
     * Query for Geometry Metadata count within the Geometry Envelope
     *
     * @param envelope
     * @return count
     * @since 1.1.0
     */
    public int count(GeometryEnvelope envelope) {
        int count = geometryMetadataDataSource.count(featureDao.getDatabase(), featureDao.getTableName(), envelope);
        return count;
    }

    /**
     * Query for Geometry Metadata within the bounding box in
     * the provided projection
     *
     * @param boundingBox
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
     * Query for Geometry Metadata count within the bounding box in
     * the provided projection
     *
     * @param boundingBox
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
     * Get the bounding box in the feature projection from the bounding box in
     * the provided projection
     *
     * @param boundingBox
     * @param projection
     * @return feature projected bounding box
     */
    private BoundingBox getFeatureBoundingBox(BoundingBox boundingBox,
                                              Projection projection) {
        ProjectionTransform projectionTransform = projection
                .getTransformation(featureDao.getProjection());
        BoundingBox featureBoundingBox = projectionTransform
                .transform(boundingBox);
        return featureBoundingBox;
    }

    /**
     * Get the Geometry Metadata for the current place in the cursor
     *
     * @param cursor
     * @return geometry metadata
     * @since 1.1.0
     */
    public GeometryMetadata getGeometryMetadata(Cursor cursor) {
        GeometryMetadata geometryMetadata = GeometryMetadataDataSource.createGeometryMetadata(cursor);
        return geometryMetadata;
    }

    /**
     * Get the feature row for the current place in the cursor
     *
     * @param cursor
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
     * @param geometryMetadata
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

}
