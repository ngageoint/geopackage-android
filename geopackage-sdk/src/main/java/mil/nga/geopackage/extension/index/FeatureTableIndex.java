package mil.nga.geopackage.extension.index;

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
     * Constructor
     *
     * @param geoPackage
     * @param featureDao
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
     * @param row
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

        int count = 0;

        try {
            // Iterate through each row and index as a single transaction
            ConnectionSource connectionSource = getGeoPackage().getDatabase()
                    .getConnectionSource();
            count = TransactionManager.callInTransaction(connectionSource,
                    new Callable<Integer>() {
                        public Integer call() throws Exception {
                            int count = 0;
                            FeatureCursor cursor = featureDao
                                    .queryForAll();
                            try {
                                while ((progress == null || progress.isActive())
                                        && cursor.moveToNext()) {
                                    FeatureRow row = cursor.getRow();
                                    boolean indexed = index(tableIndex,
                                            row.getId(), row.getGeometry());
                                    if (indexed) {
                                        count++;
                                    }
                                    if (progress != null) {
                                        progress.addProgress(1);
                                    }
                                }
                            } finally {
                                cursor.close();
                            }

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
     * Delete the index for the feature row
     *
     * @param row
     * @return deleted rows, should be 0 or 1
     */
    public int deleteIndex(FeatureRow row) {
        return deleteIndex(row.getId());
    }

    /**
     * Query for Geometry Index objects within the bounding box in
     * the provided projection
     *
     * @param boundingBox
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
     * @param boundingBox
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
     * Get the feature row for the Geometry Index
     *
     * @param geometryIndex
     * @return feature row
     */
    public FeatureRow getFeatureRow(GeometryIndex geometryIndex) {
        return featureDao.queryForIdRow(geometryIndex.getGeomId());
    }

}
