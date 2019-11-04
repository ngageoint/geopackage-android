package mil.nga.geopackage.extension;

import android.database.Cursor;

import org.sqlite.database.sqlite.SQLiteDatabase;

import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.ResultUtils;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomRow;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionTransform;

/**
 * RTree Index Table DAO for reading geometry index ranges
 *
 * @author osbornb
 * @since 3.1.0
 */
public class RTreeIndexTableDao extends UserCustomDao {

    /**
     * RTree index extension
     */
    private final RTreeIndexExtension rTree;

    /**
     * Feature DAO
     */
    private final FeatureDao featureDao;

    /**
     * Progress
     */
    protected GeoPackageProgress progress;

    /**
     * Query range tolerance
     */
    protected double tolerance = .00000000000001;

    /**
     * SQLite Android Bindings connection
     */
    private final SQLiteDatabase database;

    /**
     * Constructor
     *
     * @param rTree      RTree extension
     * @param dao        user custom data access object
     * @param featureDao feature DAO
     */
    RTreeIndexTableDao(RTreeIndexExtension rTree, UserCustomDao dao,
                       FeatureDao featureDao) {
        super(dao, dao.getTable());
        this.rTree = rTree;
        this.featureDao = featureDao;
        this.projection = featureDao.getProjection();

        // Connect to the GeoPackage using the SQLite Android Bindings
        database = dao.getDatabaseConnection().openOrGetBindingsDb();
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
     * Determine if this feature table has the RTree extension
     *
     * @return true if has extension
     */
    public boolean has() {
        return rTree.has(featureDao.getTable());
    }

    /**
     * Create the RTree extension for the feature table
     *
     * @return extension
     */
    public Extensions create() {
        Extensions extension = null;
        if (!has()) {
            extension = rTree.create(featureDao.getTable());
            if (progress != null) {
                progress.addProgress(count());
            }
        }
        return extension;
    }

    /**
     * Delete the RTree extension for the feature table
     */
    public void delete() {
        rTree.delete(featureDao.getTable());
    }

    /**
     * Get the RTree index extension
     *
     * @return RTree index extension
     */
    public RTreeIndexExtension getRTreeIndexExtension() {
        return rTree;
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
     * Get the RTree Index Table row from the current result set location
     *
     * @param cursor result cursor
     * @return RTree Index Table row
     */
    public RTreeIndexTableRow getRow(UserCustomCursor cursor) {
        return getRow(cursor.getRow());
    }

    /**
     * Get the RTree Index Table row from the user custom row
     *
     * @param row custom row
     * @return RTree Index Table row
     */
    public RTreeIndexTableRow getRow(UserCustomRow row) {
        return new RTreeIndexTableRow(row);
    }

    /**
     * Get the feature row from the RTree Index Table row
     *
     * @param row RTree Index Table row
     * @return feature row
     */
    public FeatureRow getFeatureRow(RTreeIndexTableRow row) {
        return featureDao.queryForIdRow(row.getId());
    }

    /**
     * Get the feature row from the RTree Index Table row
     *
     * @param cursor result cursor
     * @return feature row
     */
    public FeatureRow getFeatureRow(UserCustomCursor cursor) {
        RTreeIndexTableRow row = getRow(cursor);
        return getFeatureRow(row);
    }

    /**
     * Get the feature row from the user custom row
     *
     * @param row custom row
     * @return feature row
     */
    public FeatureRow getFeatureRow(UserCustomRow row) {
        return getFeatureRow(getRow(row));
    }

    /**
     * Perform a raw query
     *
     * @param sql           sql statement
     * @param selectionArgs selection arguments
     * @return result cursor
     */
    public UserCustomCursor rawQuery(String sql, String[] selectionArgs) {
        validateRTree();

        Cursor cursor = database.rawQuery(sql, selectionArgs);
        UserCustomCursor customCursor = new UserCustomCursor(getTable(), cursor);
        return customCursor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomCursor queryForAll() {
        return query(null, new String[]{});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomCursor query(String where, String[] whereArgs) {
        validateRTree();

        StringBuilder query = new StringBuilder();
        query.append("select * from ").append(CoreSQLUtils.quoteWrap(getTableName()));
        if (where != null) {
            query.append(" where ").append(where);
        }
        String sql = query.toString();

        UserCustomCursor customCursor = rawQuery(sql, whereArgs);

        return customCursor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(String where, String[] args) {
        validateRTree();

        int count = 0;

        StringBuilder countQuery = new StringBuilder();
        countQuery.append("select count(*) from ").append(CoreSQLUtils.quoteWrap(getTableName()));
        if (where != null) {
            countQuery.append(" where ").append(where);
        }
        String sql = countQuery.toString();

        UserCustomCursor customCursor = rawQuery(sql, args);

        Object value = ResultUtils.buildSingleResult(customCursor, 0, GeoPackageDataType.MEDIUMINT);
        if (value != null) {
            count = ((Number) value).intValue();
        }

        return count;
    }

    /**
     * Query for all features
     *
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures() {
        validateRTree();
        return featureDao.queryIn(queryIdsSQL());
    }

    /**
     * Query for features
     *
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(Map<String, Object> fieldValues) {
        validateRTree();
        return featureDao.queryIn(queryIdsSQL(), fieldValues);
    }

    /**
     * Count features
     *
     * @param fieldValues field values
     * @return count
     * @since 3.3.1
     */
    public int countFeatures(Map<String, Object> fieldValues) {
        validateRTree();
        return featureDao.countIn(queryIdsSQL(), fieldValues);
    }

    /**
     * Query for features
     *
     * @param where where clause
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(String where) {
        return queryFeatures(where, null);
    }

    /**
     * Count features
     *
     * @param where where clause
     * @return count
     * @since 3.3.1
     */
    public int countFeatures(String where) {
        return countFeatures(where, null);
    }

    /**
     * Query for features
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(String where, String[] whereArgs) {
        validateRTree();
        return featureDao.queryIn(queryIdsSQL(), where, whereArgs);
    }

    /**
     * Count features
     *
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 3.3.1
     */
    public int countFeatures(String where, String[] whereArgs) {
        validateRTree();
        return featureDao.countIn(queryIdsSQL(), where, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingBox getBoundingBox() {

        BoundingBox boundingBox = null;

        String sql = "SELECT MIN(" + RTreeIndexExtension.COLUMN_MIN_X + "), MIN("
                + RTreeIndexExtension.COLUMN_MIN_Y + "), MAX("
                + RTreeIndexExtension.COLUMN_MAX_X + "), MAX("
                + RTreeIndexExtension.COLUMN_MAX_Y + ") FROM "
                + CoreSQLUtils.quoteWrap(getTableName());
        UserCustomCursor customCursor = rawQuery(sql, null);

        List<List<Object>> results = ResultUtils.buildResults(customCursor,
                new GeoPackageDataType[]{
                        GeoPackageDataType.DOUBLE, GeoPackageDataType.DOUBLE, GeoPackageDataType.DOUBLE, GeoPackageDataType.DOUBLE},
                1);

        if (!results.isEmpty()) {
            List<Object> resultRow = results.get(0);
            boundingBox = new BoundingBox((Double) resultRow.get(0), (Double) resultRow.get(1),
                    (Double) resultRow.get(2), (Double) resultRow.get(3));
        }

        return boundingBox;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * Query for rows within the bounding box
     *
     * @param boundingBox bounding box
     * @return cursor results
     */
    public UserCustomCursor query(BoundingBox boundingBox) {
        return query(boundingBox.buildEnvelope());
    }

    /**
     * Count the rows within the bounding box
     *
     * @param boundingBox bounding box
     * @return count
     */
    public int count(BoundingBox boundingBox) {
        return count(boundingBox.buildEnvelope());
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox) {
        return queryFeatures(boundingBox.buildEnvelope());
    }

    /**
     * Count the features within the bounding box
     *
     * @param boundingBox bounding box
     * @return count
     * @since 3.3.1
     */
    public int countFeatures(BoundingBox boundingBox) {
        return countFeatures(boundingBox.buildEnvelope());
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.3.1
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
     * @since 3.3.1
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
     * @return feature cursor
     * @since 3.3.1
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
     * @since 3.3.1
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
     * @return feature cursor
     * @since 3.3.1
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
     * @since 3.3.1
     */
    public int countFeatures(BoundingBox boundingBox, String where,
                             String[] whereArgs) {
        return countFeatures(boundingBox.buildEnvelope(), where, whereArgs);
    }

    /**
     * Query for rows within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return cursor results
     */
    public UserCustomCursor query(BoundingBox boundingBox,
                                  Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return query(featureBoundingBox);
    }

    /**
     * Count the rows within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     */
    public int count(BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return count(featureBoundingBox);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return queryFeatures(featureBoundingBox);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     * @since 3.3.1
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return countFeatures(featureBoundingBox);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection, Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 3.3.1
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection,
                             Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return countFeatures(featureBoundingBox, fieldValues);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return feature cursor
     * @since 3.3.1
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
     * @since 3.3.1
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
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection, String where, String[] whereArgs) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 3.3.1
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection,
                             String where, String[] whereArgs) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return countFeatures(featureBoundingBox, where, whereArgs);
    }

    /**
     * Query for rows within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return cursor results
     */
    public UserCustomCursor query(GeometryEnvelope envelope) {
        return query(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Count the rows within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return count
     */
    public int count(GeometryEnvelope envelope) {
        return count(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope) {
        return queryFeatures(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return count
     * @since 3.3.1
     */
    public int countFeatures(GeometryEnvelope envelope) {
        return countFeatures(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope,
                                       Map<String, Object> fieldValues) {
        return queryFeatures(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), fieldValues);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return count
     * @since 3.3.1
     */
    public int countFeatures(GeometryEnvelope envelope,
                             Map<String, Object> fieldValues) {
        return countFeatures(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), fieldValues);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @return feature cursor
     * @since 3.3.1
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
     * @since 3.3.1
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
     * @return feature cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope,
                                       String where, String[] whereArgs) {
        return queryFeatures(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), where, whereArgs);
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 3.3.1
     */
    public int countFeatures(GeometryEnvelope envelope, String where,
                             String[] whereArgs) {
        return countFeatures(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), where, whereArgs);
    }

    /**
     * Query for rows within the bounds
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return cursor results
     */
    public UserCustomCursor query(double minX, double minY, double maxX,
                                  double maxY) {
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return query(where, whereArgs);
    }

    /**
     * Count the rows within the bounds
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return count
     */
    public int count(double minX, double minY, double maxX, double maxY) {
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return count(where, whereArgs);
    }

    /**
     * Query for features within the bounds
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(double minX, double minY, double maxX,
                                       double maxY) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryIn(queryIdsSQL(where), whereArgs);
    }

    /**
     * Count the features within the bounds
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return results
     * @since 3.3.1
     */
    public int countFeatures(double minX, double minY, double maxX,
                             double maxY) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.countIn(queryIdsSQL(where), whereArgs);
    }

    /**
     * Query for features within the bounds
     *
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(double minX, double minY, double maxX,
                                       double maxY, Map<String, Object> fieldValues) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryIn(queryIdsSQL(where), whereArgs, fieldValues);
    }

    /**
     * Count the features within the bounds
     *
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return results
     * @since 3.3.1
     */
    public int countFeatures(double minX, double minY, double maxX, double maxY,
                             Map<String, Object> fieldValues) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.countIn(queryIdsSQL(where), whereArgs, fieldValues);
    }

    /**
     * Query for features within the bounds
     *
     * @param minX  min x
     * @param minY  min y
     * @param maxX  max x
     * @param maxY  max y
     * @param where where clause
     * @return cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(double minX, double minY, double maxX,
                                       double maxY, String where) {
        return queryFeatures(minX, minY, maxX, maxY, where, null);
    }

    /**
     * Count the features within the bounds
     *
     * @param minX  min x
     * @param minY  min y
     * @param maxX  max x
     * @param maxY  max y
     * @param where where clause
     * @return results
     * @since 3.3.1
     */
    public int countFeatures(double minX, double minY, double maxX, double maxY,
                             String where) {
        return countFeatures(minX, minY, maxX, maxY, where, null);
    }

    /**
     * Query for features within the bounds
     *
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @return cursor
     * @since 3.3.1
     */
    public FeatureCursor queryFeatures(double minX, double minY, double maxX,
                                       double maxY, String where, String[] whereArgs) {
        validateRTree();
        String whereBounds = buildWhere(minX, minY, maxX, maxY);
        String[] whereBoundsArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryIn(queryIdsSQL(whereBounds), whereBoundsArgs,
                where, whereArgs);
    }

    /**
     * Count the features within the bounds
     *
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @return results
     * @since 3.3.1
     */
    public int countFeatures(double minX, double minY, double maxX, double maxY,
                             String where, String[] whereArgs) {
        validateRTree();
        String whereBounds = buildWhere(minX, minY, maxX, maxY);
        String[] whereBoundsArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.countIn(queryIdsSQL(whereBounds), whereBoundsArgs,
                where, whereArgs);
    }

    /**
     * Validate that the RTree extension exists for the table and column
     */
    private void validateRTree() {
        if (!has()) {
            throw new GeoPackageException(
                    "RTree Extension not found for feature table: "
                            + featureDao.getTableName());
        }
    }

    /**
     * Build a where clause from the bounds for overlapping ranges
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return where clause
     */
    private String buildWhere(double minX, double minY, double maxX, double maxY) {

        StringBuilder where = new StringBuilder();
        where.append(buildWhere(RTreeIndexExtension.COLUMN_MIN_X, maxX, "<="));
        where.append(" AND ");
        where.append(buildWhere(RTreeIndexExtension.COLUMN_MIN_Y, maxY, "<="));
        where.append(" AND ");
        where.append(buildWhere(RTreeIndexExtension.COLUMN_MAX_X, minX, ">="));
        where.append(" AND ");
        where.append(buildWhere(RTreeIndexExtension.COLUMN_MAX_Y, minY, ">="));

        return where.toString();
    }

    /**
     * Build where arguments from the bounds to match the order in
     * {@link #buildWhereArgs(double, double, double, double)}
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return where clause args
     */
    private String[] buildWhereArgs(double minX, double minY, double maxX,
                                    double maxY) {

        minX -= tolerance;
        maxX += tolerance;
        minY -= tolerance;
        maxY += tolerance;

        return buildWhereArgs(new Object[]{maxX, maxY, minX, minY});
    }

}
