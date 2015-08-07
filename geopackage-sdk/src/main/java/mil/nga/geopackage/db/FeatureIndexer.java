package mil.nga.geopackage.db;

import android.content.Context;

import java.util.Date;

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
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.GeoPackageProgress;
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
     */
    public void index(FeatureRow row) {

        GeoPackageMetadataDb db = new GeoPackageMetadataDb(context);
        db.open();
        try {
            GeometryMetadataDataSource geomDs = new GeometryMetadataDataSource(db);
            long geoPackageId = geomDs.getGeoPackageId(featureDao.getDatabase());
            index(geomDs, geoPackageId, row, true);

            // Update the last indexed time
            updateLastIndexed(db, geoPackageId);
        } finally {
            db.close();
        }
    }

    /**
     * Index the feature table
     *
     * @return count
     */
    private int indexTable() {

        int count = 0;

        GeoPackageMetadataDb db = new GeoPackageMetadataDb(context);
        db.open();
        try {
            // Get or create the table metadata
            TableMetadataDataSource tableDs = new TableMetadataDataSource(db);
            TableMetadata metadata = tableDs.getOrCreate(featureDao.getDatabase(), featureDao.getTableName());

            // Delete existing index rows
            GeometryMetadataDataSource geomDs = new GeometryMetadataDataSource(db);
            geomDs.delete(featureDao.getDatabase(), featureDao.getTableName());

            // Index all features
            FeatureCursor cursor = featureDao.queryForAll();
            try {
                while ((progress == null || progress.isActive()) && cursor.moveToNext()) {
                    count++;
                    FeatureRow row = cursor.getRow();
                    index(geomDs, metadata.getGeoPackageId(), row, false);
                    if (progress != null) {
                        progress.addProgress(1);
                    }
                }
            } finally {
                cursor.close();
            }

            // Update the last indexed time
            if (progress == null || progress.isActive()) {
                updateLastIndexed(db, metadata.getGeoPackageId());
            }
        } finally {
            db.close();
        }

        return count;
    }

    /**
     * Index the feature row
     *
     * @param geomDs
     * @param geoPackageId
     * @param row
     * @param possibleUpdate
     */
    private void index(GeometryMetadataDataSource geomDs, long geoPackageId, FeatureRow row, boolean possibleUpdate) {
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
                GeometryMetadata metadata = geomDs.populate(geoPackageId, featureDao.getTableName(), row.getId(), envelope);
                if (possibleUpdate) {
                    geomDs.createOrUpdate(metadata);
                } else {
                    geomDs.create(metadata);
                }
            }
        }
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
     * Determine if the database table is indexed after database modifications
     *
     * @return
     */
    public boolean isIndexed() {

        boolean indexed = false;

        Contents contents = featureDao.getGeometryColumns().getContents();
        Date lastChange = contents.getLastChange();

        GeoPackageMetadataDb db = new GeoPackageMetadataDb(context);
        db.open();
        try {
            TableMetadataDataSource ds = new TableMetadataDataSource(db);
            TableMetadata metadata = ds.get(featureDao.getDatabase(), featureDao.getTableName());
            if (metadata != null) {
                Long lastIndexed = metadata.getLastIndexed();
                indexed = lastIndexed != null && lastIndexed >= lastChange.getTime();
            }
        } finally {
            db.close();
        }

        return indexed;
    }

}
