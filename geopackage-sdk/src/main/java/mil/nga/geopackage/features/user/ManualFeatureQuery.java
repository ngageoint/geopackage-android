package mil.nga.geopackage.features.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionTransform;

/**
 * Performs manual brute force queries against feature rows. See
 * {@link FeatureIndexManager} for performing indexed queries.
 *
 * @author osbornb
 * @since 3.1.0
 */
public class ManualFeatureQuery {

    /**
     * Feature DAO
     */
    private final FeatureDao featureDao;

    /**
     * Query single chunk limit
     */
    protected int chunkLimit = 1000;

    /**
     * Query range tolerance
     */
    protected double tolerance = .00000000000001;

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
     * Get the SQL query chunk limit
     *
     * @return chunk limit
     */
    public int getChunkLimit() {
        return chunkLimit;
    }

    /**
     * Set the SQL query chunk limit
     *
     * @param chunkLimit chunk limit
     */
    public void setChunkLimit(int chunkLimit) {
        this.chunkLimit = chunkLimit;
    }

    /**
     * Get the query range tolerance
     *
     * @return query range tolerance
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * Set the query range tolerance
     *
     * @param tolerance query range tolerance
     */
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * Query for features
     *
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor query() {
        return featureDao.queryForAll();
    }

    /**
     * Get the count of features
     *
     * @return count
     */
    public int count() {
        return featureDao.count();
    }

    /**
     * Get the count of features with non null geometries
     *
     * @return count
     */
    public int countWithGeometries() {
        return featureDao.count(featureDao.getGeometryColumnName() + " IS NOT NULL", null);
    }

    /**
     * Query for features
     *
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor query(Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return featureDao.query(where, whereArgs);
    }

    /**
     * Count features
     *
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int count(Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return featureDao.count(where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param where where clause
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor query(String where) {
        return featureDao.query(where);
    }

    /**
     * Count features
     *
     * @param where where clause
     * @return count
     * @since 3.4.0
     */
    public int count(String where) {
        return featureDao.count(where);
    }

    /**
     * Query for features
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor query(String where, String[] whereArgs) {
        return featureDao.query(where, whereArgs);
    }

    /**
     * Count features
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 3.4.0
     */
    public int count(String where, String[] whereArgs) {
        return featureDao.count(where, whereArgs);
    }

    /**
     * Manually build the bounds of the feature table
     *
     * @return bounding box
     */
    public BoundingBox getBoundingBox() {

        GeometryEnvelope envelope = null;

        long offset = 0;
        boolean hasResults = true;

        while (hasResults) {

            hasResults = false;

            FeatureCursor featureCursor = featureDao.queryForChunk(chunkLimit,
                    offset);
            try {
                while (featureCursor.moveToNext()) {
                    hasResults = true;

                    FeatureRow featureRow = featureCursor.getRow();
                    GeometryEnvelope featureEnvelope = featureRow
                            .getGeometryEnvelope();
                    if (featureEnvelope != null) {

                        if (envelope == null) {
                            envelope = featureEnvelope;
                        } else {
                            envelope = envelope.union(featureEnvelope);
                        }

                    }
                }
            } finally {
                featureCursor.close();
            }

            offset += chunkLimit;
        }

        BoundingBox boundingBox = null;
        if (envelope != null) {
            boundingBox = new BoundingBox(envelope);
        }

        return boundingBox;
    }

    /**
     * Manually build the bounds of the feature table in the provided projection
     *
     * @param projection desired projection
     * @return bounding box
     */
    public BoundingBox getBoundingBox(Projection projection) {
        BoundingBox boundingBox = getBoundingBox();
        if (boundingBox != null && projection != null) {
            ProjectionTransform projectionTransform = featureDao
                    .getProjection().getTransformation(projection);
            boundingBox = boundingBox.transform(projectionTransform);
        }
        return boundingBox;
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
     * Manually count the rows within the bounding box
     *
     * @param boundingBox bounding box
     * @return count
     */
    public long count(BoundingBox boundingBox) {
        return count(boundingBox.buildEnvelope());
    }

    /**
     * Manually query for rows within the bounding box
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(BoundingBox boundingBox,
                                           Map<String, Object> fieldValues) {
        return query(boundingBox.buildEnvelope(), fieldValues);
    }

    /**
     * Manually count the rows within the bounding box
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public long count(BoundingBox boundingBox,
                      Map<String, Object> fieldValues) {
        return count(boundingBox.buildEnvelope(), fieldValues);
    }

    /**
     * Manually query for rows within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       were clause
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(BoundingBox boundingBox,
                                           String where) {
        return query(boundingBox, where, null);
    }

    /**
     * Manually count the rows within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       were clause
     * @return count
     * @since 3.4.0
     */
    public long count(BoundingBox boundingBox, String where) {
        return count(boundingBox, where, null);
    }

    /**
     * Manually query for rows within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       were clause
     * @param whereArgs   where arguments
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(BoundingBox boundingBox,
                                           String where, String[] whereArgs) {
        return query(boundingBox.buildEnvelope(), where, whereArgs);
    }

    /**
     * Manually count the rows within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       were clause
     * @param whereArgs   where arguments
     * @return count
     * @since 3.4.0
     */
    public long count(BoundingBox boundingBox, String where,
                      String[] whereArgs) {
        return count(boundingBox.buildEnvelope(), where, whereArgs);
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
     * Manually query for rows within the bounding box in the provided
     * projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(BoundingBox boundingBox,
                                           Projection projection, Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = featureDao
                .projectBoundingBox(boundingBox, projection);
        return query(featureBoundingBox, fieldValues);
    }

    /**
     * Manually count the rows within the bounding box in the provided
     * projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public long count(BoundingBox boundingBox, Projection projection,
                      Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = featureDao
                .projectBoundingBox(boundingBox, projection);
        return count(featureBoundingBox, fieldValues);
    }

    /**
     * Manually query for rows within the bounding box in the provided
     * projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       were clause
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(BoundingBox boundingBox,
                                           Projection projection, String where) {
        return query(boundingBox, projection, where, null);
    }

    /**
     * Manually count the rows within the bounding box in the provided
     * projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       were clause
     * @return count
     * @since 3.4.0
     */
    public long count(BoundingBox boundingBox, Projection projection,
                      String where) {
        return count(boundingBox, projection, where, null);
    }

    /**
     * Manually query for rows within the bounding box in the provided
     * projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       were clause
     * @param whereArgs   where arguments
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(BoundingBox boundingBox,
                                           Projection projection, String where, String[] whereArgs) {
        BoundingBox featureBoundingBox = featureDao
                .projectBoundingBox(boundingBox, projection);
        return query(featureBoundingBox, where, whereArgs);
    }

    /**
     * Manually count the rows within the bounding box in the provided
     * projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       were clause
     * @param whereArgs   where arguments
     * @return count
     * @since 3.4.0
     */
    public long count(BoundingBox boundingBox, Projection projection,
                      String where, String[] whereArgs) {
        BoundingBox featureBoundingBox = featureDao
                .projectBoundingBox(boundingBox, projection);
        return count(featureBoundingBox, where, whereArgs);
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
     * Manually query for rows within the geometry envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(GeometryEnvelope envelope,
                                           Map<String, Object> fieldValues) {
        return query(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(),
                envelope.getMaxY(), fieldValues);
    }

    /**
     * Manually count the rows within the geometry envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public long count(GeometryEnvelope envelope,
                      Map<String, Object> fieldValues) {
        return count(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(),
                envelope.getMaxY(), fieldValues);
    }

    /**
     * Manually query for rows within the geometry envelope
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(GeometryEnvelope envelope,
                                           String where) {
        return query(envelope, where, null);
    }

    /**
     * Manually count the rows within the geometry envelope
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @return count
     * @since 3.4.0
     */
    public long count(GeometryEnvelope envelope, String where) {
        return count(envelope, where, null);
    }

    /**
     * Manually query for rows within the geometry envelope
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(GeometryEnvelope envelope,
                                           String where, String[] whereArgs) {
        return query(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(),
                envelope.getMaxY(), where, whereArgs);
    }

    /**
     * Manually count the rows within the geometry envelope
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 3.4.0
     */
    public long count(GeometryEnvelope envelope, String where,
                      String[] whereArgs) {
        return count(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(),
                envelope.getMaxY(), where, whereArgs);
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
        return query(minX, minY, maxX, maxY, null, null);
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

    /**
     * Manually query for rows within the bounds
     *
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(double minX, double minY,
                                           double maxX, double maxY, Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return query(minX, minY, maxX, maxY, where, whereArgs);
    }

    /**
     * Manually count the rows within the bounds
     *
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public long count(double minX, double minY, double maxX, double maxY,
                      Map<String, Object> fieldValues) {
        String where = featureDao.buildWhere(fieldValues.entrySet());
        String[] whereArgs = featureDao.buildWhereArgs(fieldValues.values());
        return count(minX, minY, maxX, maxY, where, whereArgs);
    }

    /**
     * Manually query for rows within the bounds
     *
     * @param minX  min x
     * @param minY  min y
     * @param maxX  max x
     * @param maxY  max y
     * @param where where clause
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(double minX, double minY,
                                           double maxX, double maxY, String where) {
        return query(minX, minY, maxX, maxY, where, null);
    }

    /**
     * Manually count the rows within the bounds
     *
     * @param minX  min x
     * @param minY  min y
     * @param maxX  max x
     * @param maxY  max y
     * @param where where clause
     * @return count
     * @since 3.4.0
     */
    public long count(double minX, double minY, double maxX, double maxY,
                      String where) {
        return count(minX, minY, maxX, maxY, where, null);
    }

    /**
     * Manually query for rows within the bounds
     *
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where args
     * @return results
     * @since 3.4.0
     */
    public ManualFeatureQueryResults query(double minX, double minY,
                                           double maxX, double maxY, String where, String[] whereArgs) {

        List<Long> featureIds = new ArrayList<>();

        long offset = 0;
        boolean hasResults = true;

        minX -= tolerance;
        maxX += tolerance;
        minY -= tolerance;
        maxY += tolerance;

        while (hasResults) {

            hasResults = false;

            FeatureCursor featureCursor = featureDao.queryForChunk(where,
                    whereArgs, chunkLimit, offset);
            try {
                while (featureCursor.moveToNext()) {
                    hasResults = true;

                    FeatureRow featureRow = featureCursor.getRow();
                    GeometryEnvelope envelope = featureRow
                            .getGeometryEnvelope();
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
                featureCursor.close();
            }

            offset += chunkLimit;
        }

        ManualFeatureQueryResults results = new ManualFeatureQueryResults(
                featureDao, featureIds);

        return results;
    }

    /**
     * Manually count the rows within the bounds
     *
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where args
     * @return count
     * @since 3.4.0
     */
    public long count(double minX, double minY, double maxX, double maxY,
                      String where, String[] whereArgs) {
        return query(minX, minY, maxX, maxY, where, whereArgs).count();
    }

}
