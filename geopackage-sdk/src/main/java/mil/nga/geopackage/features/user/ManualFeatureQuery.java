package mil.nga.geopackage.features.user;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.proj.Projection;

/**
 * Performs manual brute force queries against feature rows. See
 * {@link FeatureIndexManager} for performing indexed queries.
 *
 * @author osbornb
 * @since 3.0.3
 */
public class ManualFeatureQuery {

    /**
     * Feature DAO
     */
    private final FeatureDao featureDao;

    /**
     * Constructor
     *
     * @param featureDao feature DAO
     */
    public ManualFeatureQuery(FeatureDao featureDao) {
        this.featureDao = featureDao;
    }

    /**
     * Get the feature DAO
     *
     * @return feature DAO
     */
    public FeatureDao getFeatureDao() {
        return featureDao;
    }

    /**
     * Manually query for rows within the bounding box
     *
     * @param boundingBox bounding box
     * @return results
     */
    public ManualFeatureQueryResults query(BoundingBox boundingBox) {
        return query(boundingBox.buildEnvelope());
    }

    /**
     * Manually query for rows within the bounding box in the provided
     * projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return results
     */
    public ManualFeatureQueryResults query(BoundingBox boundingBox,
                                           Projection projection) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(
                boundingBox, projection);
        return query(featureBoundingBox);
    }

    /**
     * Manually count the rows within the bounding box
     *
     * @param boundingBox bounding box
     * @return count
     */
    public long count(BoundingBox boundingBox) {
        return count(boundingBox.buildEnvelope());
    }

    /**
     * Manually count the rows within the bounding box in the provided
     * projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     */
    public long count(BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = featureDao.projectBoundingBox(
                boundingBox, projection);
        return count(featureBoundingBox);
    }

    /**
     * Manually query for rows within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return results
     */
    public ManualFeatureQueryResults query(GeometryEnvelope envelope) {
        return query(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Manually count the rows within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return count
     */
    public long count(GeometryEnvelope envelope) {
        return count(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Manually query for rows within the bounds
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return results
     */
    public ManualFeatureQueryResults query(double minX, double minY,
                                           double maxX, double maxY) {

        List<Long> featureIds = new ArrayList<>();

        FeatureCursor cursor = featureDao.queryForAll();
        try {
            while (cursor.moveToNext()) {
                FeatureRow featureRow = cursor.getRow();
                GeometryEnvelope envelope = featureRow.getGeometryEnvelope();
                if (envelope != null) {

                    double minXMax = Math.max(minX, envelope.getMinX());
                    double maxXMin = Math.min(maxX, envelope.getMaxX());
                    double minYMax = Math.max(minY, envelope.getMinY());
                    double maxYMin = Math.min(maxY, envelope.getMaxY());

                    if (minXMax <= maxXMin && minYMax <= maxYMin) {
                        featureIds.add(featureRow.getId());
                    }

                }
            }
        } finally {
            cursor.close();
        }

        ManualFeatureQueryResults results = new ManualFeatureQueryResults(
                featureDao, featureIds);

        return results;
    }

    /**
     * Manually count the rows within the bounds
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return count
     */
    public long count(double minX, double minY, double maxX, double maxY) {
        return query(minX, minY, maxX, maxY).count();
    }

}
