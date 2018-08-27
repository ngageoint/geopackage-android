package mil.nga.geopackage.extension;

import android.database.Cursor;

import org.sqlite.database.sqlite.SQLiteDatabase;

import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.ResultUtils;
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
 * @since 3.0.3
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
    public int count() {
        return count(null, new String[]{});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(String where, String[] args) {

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
     * Count the rows within the bounding box
     *
     * @param boundingBox bounding box
     * @return count
     */
    public long count(BoundingBox boundingBox) {
        return count(boundingBox.buildEnvelope());
    }

    /**
     * Count the rows within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     */
    public long count(BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return count(featureBoundingBox);
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
    public long count(GeometryEnvelope envelope) {
        return count(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
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
    public long count(double minX, double minY, double maxX, double maxY) {
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return count(where, whereArgs);
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
        return buildWhereArgs(new Object[]{maxX, maxY, minX, minY});
    }

}
