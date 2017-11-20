package mil.nga.geopackage.extension.index;

import android.util.Log;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureRowSync;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionTransform;

/**
 * Feature Table Index NGA Extension implementation. This extension is used to
 * index Geometries within a feature table by their minimum bounding box for
 * bounding box queries. This extension is required to provide an index
 * implementation when a SQLite version is used before SpatialLite support
 * (Android).
 *
 * @author osbornb
 * @since 1.1.0
 */
public class FeatureTableIndex extends FeatureTableCoreIndex {

    /**
     * Feature DAO
     */
    private final FeatureDao featureDao;

    /**
     * Feature Row Sync for simultaneous same row queries
     */
    private final FeatureRowSync featureRowSync = new FeatureRowSync();

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     * @param featureDao feature dao
     */
    public FeatureTableIndex(GeoPackage geoPackage, FeatureDao featureDao) {
        super(geoPackage, featureDao.getTableName(), featureDao
                .getGeometryColumnName());
        this.featureDao = featureDao;
    }

    /**
     * Close the table index
     */
    public void close() {
        // Don't close anything, leave the GeoPackage connection open
    }

    /**
     * Index the feature row. This method assumes that indexing has been
     * completed and maintained as the last indexed time is updated.
     *
     * @param row feature row
     * @return true if indexed
     */
    public boolean index(FeatureRow row) {
        TableIndex tableIndex = getTableIndex();
        if (tableIndex == null) {
            throw new GeoPackageException(
                    "GeoPackage table is not indexed. GeoPackage: "
                            + getGeoPackage().getName() + ", Table: "
                            + getTableName());
        }
        boolean indexed = index(tableIndex, row.getId(), row.getGeometry());

        // Update the last indexed time
        updateLastIndexed();

        return indexed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int indexTable(final TableIndex tableIndex) {

        int count;

        try {
            // Iterate through each row and index as a single transaction
            ConnectionSource connectionSource = getGeoPackage().getDatabase()
                    .getConnectionSource();
            count = TransactionManager.callInTransaction(connectionSource,
                    new Callable<Integer>() {
                        public Integer call() throws Exception {

                            FeatureCursor cursor = featureDao.queryForAll();

                            int count = indexRows(tableIndex, cursor);

                            // Update the last indexed time
                            if (progress == null || progress.isActive()) {
                                updateLastIndexed();
                            }
                            return count;
                        }
                    });
        } catch (SQLException e) {
            throw new GeoPackageException("Failed to Index Table. GeoPackage: "
                    + getGeoPackage().getName() + ", Table: " + getTableName(),
                    e);
        }

        return count;
    }

    /**
     * Index the feature rows in the cursor
     *
     * @param tableIndex table index
     * @param cursor     feature cursor
     * @return count
     */
    private int indexRows(TableIndex tableIndex, FeatureCursor cursor) {

        int count = 0;

        try {
            while ((progress == null || progress.isActive())
                    && cursor.moveToNext()) {
                try {
                    FeatureRow row = cursor.getRow();
                    if (row.isValid()) {
                        boolean indexed = index(tableIndex,
                                row.getId(), row.getGeometry());
                        if (indexed) {
                            count++;
                        }
                        if (progress != null) {
                            progress.addProgress(1);
                        }
                    }
                } catch (Exception e) {
                    Log.e(FeatureTableIndex.class.getSimpleName(), "Failed to index feature. Table: "
                            + tableIndex.getTableName() + ", Position: " + cursor.getPosition(), e);
                }
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    /**
     * Delete the index for the feature row
     *
     * @param row feature row
     * @return deleted rows, should be 0 or 1
     */
    public int deleteIndex(FeatureRow row) {
        return deleteIndex(row.getId());
    }

    /**
     * Query for Geometry Index objects within the bounding box in
     * the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return geometry indices iterator
     */
    public CloseableIterator<GeometryIndex> query(BoundingBox boundingBox,
                                                  Projection projection) {

        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
                projection);

        CloseableIterator<GeometryIndex> geometryIndices = query(featureBoundingBox);

        return geometryIndices;
    }

    /**
     * Query for Geometry Index count within the bounding box in
     * the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return count
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
     * @param boundingBox bounding box
     * @param projection  projection
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
     * Get the feature row for the Geometry Index
     *
     * @param geometryIndex geometry index
     * @return feature row
     */
    public FeatureRow getFeatureRow(GeometryIndex geometryIndex) {

        long geomId = geometryIndex.getGeomId();

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
