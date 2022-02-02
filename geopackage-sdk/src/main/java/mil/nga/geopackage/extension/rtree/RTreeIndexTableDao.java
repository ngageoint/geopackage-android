package mil.nga.geopackage.extension.rtree;

import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomRow;
import mil.nga.proj.Projection;
import mil.nga.proj.ProjectionTransform;
import mil.nga.sf.GeometryEnvelope;

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
        setUseBindings(true);
        featureDao.setUseBindings(true);
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
     * Get the RTree Index Table row from the current cursor location
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
     * {@inheritDoc}
     */
    @Override
    public UserCustomCursor query(boolean distinct, String[] columns) {
        validateRTree();
        return super.query(distinct, columns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomCursor query(boolean distinct, String[] columns,
                                  String[] columnsAs) {
        validateRTree();
        return super.query(distinct, columns, columnsAs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomCursor query(boolean distinct, String[] columns,
                                  String where, String[] whereArgs) {
        validateRTree();
        return super.query(distinct, columns, where, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomCursor query(boolean distinct, String[] columns,
                                  String where, String[] whereArgs, String groupBy, String having,
                                  String orderBy) {
        validateRTree();
        return super.query(distinct, columns, where, whereArgs, groupBy, having,
                orderBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomCursor query(boolean distinct, String[] columns,
                                  String where, String[] whereArgs, String groupBy, String having,
                                  String orderBy, String limit) {
        validateRTree();
        return super.query(distinct, columns, where, whereArgs, groupBy, having,
                orderBy, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(boolean distinct, String column, String where,
                     String[] args) {
        validateRTree();
        return super.count(distinct, column, where, args);
    }

    /**
     * Query for all features
     *
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures() {
        validateRTree();
        return featureDao.queryIn(queryIdsSQL());
    }

    /**
     * Query for all features
     *
     * @param distinct distinct rows
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct) {
        validateRTree();
        return featureDao.queryIn(distinct, queryIdsSQL());
    }

    /**
     * Query for all features
     *
     * @param columns columns
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns) {
        validateRTree();
        return featureDao.queryIn(columns, queryIdsSQL());
    }

    /**
     * Query for all features
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns) {
        validateRTree();
        return featureDao.queryIn(distinct, columns, queryIdsSQL());
    }

    /**
     * Count features
     *
     * @return count
     * @since 4.0.0
     */
    public int countFeatures() {
        validateRTree();
        return featureDao.countIn(queryIdsSQL());
    }

    /**
     * Count features
     *
     * @param column count column name
     * @return count
     * @since 4.0.0
     */
    public int countColumnFeatures(String column) {
        validateRTree();
        return featureDao.countIn(column, queryIdsSQL());
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
        validateRTree();
        return featureDao.countIn(distinct, column, queryIdsSQL());
    }

    /**
     * Query for features
     *
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(Map<String, Object> fieldValues) {
        validateRTree();
        return featureDao.queryIn(queryIdsSQL(), fieldValues);
    }

    /**
     * Query for features
     *
     * @param distinct    distinct rows
     * @param fieldValues field values
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       Map<String, Object> fieldValues) {
        validateRTree();
        return featureDao.queryIn(distinct, queryIdsSQL(), fieldValues);
    }

    /**
     * Query for features
     *
     * @param columns     columns
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       Map<String, Object> fieldValues) {
        validateRTree();
        return featureDao.queryIn(columns, queryIdsSQL(), fieldValues);
    }

    /**
     * Query for features
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param fieldValues field values
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       Map<String, Object> fieldValues) {
        validateRTree();
        return featureDao.queryIn(distinct, columns, queryIdsSQL(),
                fieldValues);
    }

    /**
     * Count features
     *
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(Map<String, Object> fieldValues) {
        validateRTree();
        return featureDao.countIn(queryIdsSQL(), fieldValues);
    }

    /**
     * Count features
     *
     * @param column      count column value
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, Map<String, Object> fieldValues) {
        validateRTree();
        return featureDao.countIn(column, queryIdsSQL(), fieldValues);
    }

    /**
     * Count features
     *
     * @param distinct    distinct column values
     * @param column      count column value
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column,
                             Map<String, Object> fieldValues) {
        validateRTree();
        return featureDao.countIn(distinct, column, queryIdsSQL(), fieldValues);
    }

    /**
     * Query for features
     *
     * @param where where clause
     * @return feature cursor
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
     * @return feature cursor
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
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       String where) {
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
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(String where, String[] whereArgs) {
        validateRTree();
        return featureDao.queryIn(queryIdsSQL(), where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param distinct  distinct rows
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String where,
                                       String[] whereArgs) {
        validateRTree();
        return featureDao.queryIn(distinct, queryIdsSQL(), where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, String where,
                                       String[] whereArgs) {
        validateRTree();
        return featureDao.queryIn(columns, queryIdsSQL(), where, whereArgs);
    }

    /**
     * Query for features
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       String where, String[] whereArgs) {
        validateRTree();
        return featureDao.queryIn(distinct, columns, queryIdsSQL(), where,
                whereArgs);
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
        validateRTree();
        return featureDao.countIn(queryIdsSQL(), where, whereArgs);
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
        validateRTree();
        return featureDao.countIn(column, queryIdsSQL(), where, whereArgs);
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
    public int countFeatures(boolean distinct, String column, String where,
                             String[] whereArgs) {
        validateRTree();
        return featureDao.countIn(distinct, column, queryIdsSQL(), where,
                whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingBox getBoundingBox() {
        List<Double> values = querySingleRowTypedResults(
                "SELECT MIN(" + RTreeIndexExtension.COLUMN_MIN_X + "), MIN("
                        + RTreeIndexExtension.COLUMN_MIN_Y + "), MAX("
                        + RTreeIndexExtension.COLUMN_MAX_X + "), MAX("
                        + RTreeIndexExtension.COLUMN_MAX_Y + ") FROM "
                        + CoreSQLUtils.quoteWrap(getTableName()),
                null);
        BoundingBox boundingBox = new BoundingBox(values.get(0), values.get(1),
                values.get(2), values.get(3));
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
     * @return cursor
     */
    public UserCustomCursor query(BoundingBox boundingBox) {
        return query(false, boundingBox);
    }

    /**
     * Query for rows within the bounding box
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @return cursor
     * @since 4.0.0
     */
    public UserCustomCursor query(boolean distinct,
                                  BoundingBox boundingBox) {
        return query(distinct, boundingBox.buildEnvelope());
    }

    /**
     * Query for rows within the bounding box
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @return cursor
     * @since 3.5.0
     */
    public UserCustomCursor query(String[] columns,
                                  BoundingBox boundingBox) {
        return query(false, columns, boundingBox);
    }

    /**
     * Query for rows within the bounding box
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @return cursor
     * @since 4.0.0
     */
    public UserCustomCursor query(boolean distinct, String[] columns,
                                  BoundingBox boundingBox) {
        return query(distinct, columns, boundingBox.buildEnvelope());
    }

    /**
     * Count the rows within the bounding box
     *
     * @param boundingBox bounding box
     * @return count
     */
    public int count(BoundingBox boundingBox) {
        return count(false, null, boundingBox);
    }

    /**
     * Count the rows within the bounding box
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @return count
     * @since 4.0.0
     */
    public int count(String column, BoundingBox boundingBox) {
        return count(false, column, boundingBox);
    }

    /**
     * Count the rows within the bounding box
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @return count
     * @since 4.0.0
     */
    public int count(boolean distinct, String column, BoundingBox boundingBox) {
        return count(distinct, column, boundingBox.buildEnvelope());
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox) {
        return queryFeatures(distinct, boundingBox.buildEnvelope());
    }

    /**
     * Query for features within the bounding box
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox) {
        return queryFeatures(false, columns, boundingBox);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox) {
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
     * @param column      count column values
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
     * @param column      count column values
     * @param boundingBox bounding box
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox) {
        return countFeatures(distinct, column, boundingBox.buildEnvelope());
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, Map<String, Object> fieldValues) {
        return queryFeatures(distinct, boundingBox.buildEnvelope(),
                fieldValues);
    }

    /**
     * Query for features within the bounding box
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox, Map<String, Object> fieldValues) {
        return queryFeatures(false, columns, boundingBox, fieldValues);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param fieldValues field values
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, Map<String, Object> fieldValues) {
        return queryFeatures(distinct, columns, boundingBox.buildEnvelope(),
                fieldValues);
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
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, Map<String, Object> fieldValues) {
        return countFeatures(distinct, column, boundingBox.buildEnvelope(),
                fieldValues);
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, String where) {
        return queryFeatures(distinct, boundingBox, where, null);
    }

    /**
     * Query for features within the bounding box
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox, String where) {
        return queryFeatures(false, columns, boundingBox, where);
    }

    /**
     * Query for features within the bounding box
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, String where) {
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
    public int countFeatures(String column, BoundingBox boundingBox,
                             String where) {
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
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, String where) {
        return countFeatures(distinct, column, boundingBox, where, null);
    }

    /**
     * Query for features within the bounding box
     *
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, String where, String[] whereArgs) {
        return queryFeatures(distinct, boundingBox.buildEnvelope(), where,
                whereArgs);
    }

    /**
     * Query for features within the bounding box
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox, String where, String[] whereArgs) {
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, String where, String[] whereArgs) {
        return queryFeatures(distinct, columns, boundingBox.buildEnvelope(),
                where, whereArgs);
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
    public int countFeatures(String column, BoundingBox boundingBox,
                             String where, String[] whereArgs) {
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
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, String where, String[] whereArgs) {
        return countFeatures(distinct, column, boundingBox.buildEnvelope(),
                where, whereArgs);
    }

    /**
     * Query for rows within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return cursor
     */
    public UserCustomCursor query(BoundingBox boundingBox,
                                  Projection projection) {
        return query(false, boundingBox, projection);
    }

    /**
     * Query for rows within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection
     * @return cursor
     * @since 4.0.0
     */
    public UserCustomCursor query(boolean distinct, BoundingBox boundingBox,
                                  Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return query(distinct, featureBoundingBox);
    }

    /**
     * Query for rows within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @return cursor
     * @since 3.5.0
     */
    public UserCustomCursor query(String[] columns, BoundingBox boundingBox,
                                  Projection projection) {
        return query(false, columns, boundingBox, projection);
    }

    /**
     * Query for rows within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @return cursor
     * @since 4.0.0
     */
    public UserCustomCursor query(boolean distinct, String[] columns,
                                  BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return query(distinct, columns, featureBoundingBox);
    }

    /**
     * Count the rows within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     */
    public int count(BoundingBox boundingBox, Projection projection) {
        return count(false, null, boundingBox, projection);
    }

    /**
     * Count the rows within the bounding box in the provided projection
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     * @since 4.0.0
     */
    public int count(String column, BoundingBox boundingBox,
                     Projection projection) {
        return count(false, column, boundingBox, projection);
    }

    /**
     * Count the rows within the bounding box in the provided projection
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @return count
     * @since 4.0.0
     */
    public int count(boolean distinct, String column, BoundingBox boundingBox,
                     Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return count(distinct, column, featureBoundingBox);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return queryFeatures(distinct, featureBoundingBox);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox, Projection projection) {
        return queryFeatures(false, columns, boundingBox, projection);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
    public int countFeatures(String column, BoundingBox boundingBox,
                             Projection projection) {
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
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return countFeatures(distinct, column, featureBoundingBox);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, Projection projection,
                                       Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox, Projection projection,
                                       Map<String, Object> fieldValues) {
        return queryFeatures(false, columns, boundingBox, projection,
                fieldValues);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, Projection projection,
                                       Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return queryFeatures(distinct, columns, featureBoundingBox,
                fieldValues);
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
    public int countFeatures(String column, BoundingBox boundingBox,
                             Projection projection, Map<String, Object> fieldValues) {
        return countFeatures(false, column, boundingBox, projection,
                fieldValues);
    }

    /**
     * Count the features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, Projection projection,
                             Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return countFeatures(distinct, column, featureBoundingBox, fieldValues);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, Projection projection, String where) {
        return queryFeatures(distinct, boundingBox, projection, where, null);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox, Projection projection, String where) {
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, Projection projection, String where) {
        return queryFeatures(distinct, columns, boundingBox, projection, where,
                null);
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
    public int countFeatures(String column, BoundingBox boundingBox,
                             Projection projection, String where) {
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
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, Projection projection, String where) {
        return countFeatures(distinct, column, boundingBox, projection, where,
                null);
    }

    /**
     * Query for features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection
     * @param where       where clause
     * @param whereArgs   where arguments
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, Projection projection, String where,
                                       String[] whereArgs) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox, Projection projection, String where,
                                       String[] whereArgs) {
        return queryFeatures(false, columns, boundingBox, projection, where,
                whereArgs);
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, Projection projection, String where,
                                       String[] whereArgs) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return queryFeatures(distinct, columns, featureBoundingBox, where,
                whereArgs);
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
        return countFeatures(false, null, boundingBox, projection, where,
                whereArgs);
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
    public int countFeatures(String column, BoundingBox boundingBox,
                             Projection projection, String where, String[] whereArgs) {
        return countFeatures(false, column, boundingBox, projection, where,
                whereArgs);
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
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, Projection projection, String where,
                             String[] whereArgs) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
                projection);
        return countFeatures(distinct, column, featureBoundingBox, where,
                whereArgs);
    }

    /**
     * Query for rows within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return cursor
     */
    public UserCustomCursor query(GeometryEnvelope envelope) {
        return query(false, envelope);
    }

    /**
     * Query for rows within the geometry envelope
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @return cursor
     * @since 4.0.0
     */
    public UserCustomCursor query(boolean distinct,
                                  GeometryEnvelope envelope) {
        return query(distinct, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Query for rows within the geometry envelope
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @return cursor
     * @since 3.5.0
     */
    public UserCustomCursor query(String[] columns,
                                  GeometryEnvelope envelope) {
        return query(false, columns, envelope);
    }

    /**
     * Query for rows within the geometry envelope
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @return cursor
     * @since 4.0.0
     */
    public UserCustomCursor query(boolean distinct, String[] columns,
                                  GeometryEnvelope envelope) {
        return query(distinct, columns, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Count the rows within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return count
     */
    public int count(GeometryEnvelope envelope) {
        return count(false, null, envelope);
    }

    /**
     * Count the rows within the geometry envelope
     *
     * @param column   count column name
     * @param envelope geometry envelope
     * @return count
     * @since 4.0.0
     */
    public int count(String column, GeometryEnvelope envelope) {
        return count(false, column, envelope);
    }

    /**
     * Count the rows within the geometry envelope
     *
     * @param distinct distinct column values
     * @param column   count column name
     * @param envelope geometry envelope
     * @return count
     * @since 4.0.0
     */
    public int count(boolean distinct, String column,
                     GeometryEnvelope envelope) {
        return count(distinct, column, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope) {
        return queryFeatures(false, envelope);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       GeometryEnvelope envelope) {
        return queryFeatures(distinct, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       GeometryEnvelope envelope) {
        return queryFeatures(false, columns, envelope);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       GeometryEnvelope envelope) {
        return queryFeatures(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Count the features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(GeometryEnvelope envelope) {
        return countFeatures(false, null, envelope);
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
    public int countFeatures(boolean distinct, String column,
                             GeometryEnvelope envelope) {
        return countFeatures(distinct, column, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       GeometryEnvelope envelope, Map<String, Object> fieldValues) {
        return queryFeatures(distinct, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), fieldValues);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       GeometryEnvelope envelope, Map<String, Object> fieldValues) {
        return queryFeatures(false, columns, envelope, fieldValues);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       GeometryEnvelope envelope, Map<String, Object> fieldValues) {
        return queryFeatures(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                fieldValues);
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
        return countFeatures(envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), fieldValues);
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
    public int countFeatures(boolean distinct, String column,
                             GeometryEnvelope envelope, Map<String, Object> fieldValues) {
        return countFeatures(distinct, column, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                fieldValues);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       GeometryEnvelope envelope, String where) {
        return queryFeatures(distinct, envelope, where, null);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       GeometryEnvelope envelope, String where) {
        return queryFeatures(false, columns, envelope, where);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @param where    where clause
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       GeometryEnvelope envelope, String where) {
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
        return countFeatures(false, null, envelope, where);
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
    public int countFeatures(String column, GeometryEnvelope envelope,
                             String where) {
        return countFeatures(false, column, envelope, where);
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
    public int countFeatures(boolean distinct, String column,
                             GeometryEnvelope envelope, String where) {
        return countFeatures(distinct, column, envelope, where, null);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature cursor
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       GeometryEnvelope envelope, String where, String[] whereArgs) {
        return queryFeatures(distinct, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), where, whereArgs);
    }

    /**
     * Query for features within the geometry envelope
     *
     * @param columns   columns
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       GeometryEnvelope envelope, String where, String[] whereArgs) {
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
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       GeometryEnvelope envelope, String where, String[] whereArgs) {
        return queryFeatures(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                where, whereArgs);
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
    public int countFeatures(String column, GeometryEnvelope envelope,
                             String where, String[] whereArgs) {
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
    public int countFeatures(boolean distinct, String column,
                             GeometryEnvelope envelope, String where, String[] whereArgs) {
        return countFeatures(distinct, column, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                where, whereArgs);
    }

    /**
     * Query for rows within the bounds
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return cursor
     */
    public UserCustomCursor query(double minX, double minY, double maxX,
                                  double maxY) {
        return query(false, minX, minY, maxX, maxY);
    }

    /**
     * Query for rows within the bounds
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @return cursor
     * @since 4.0.0
     */
    public UserCustomCursor query(boolean distinct, double minX, double minY,
                                  double maxX, double maxY) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return query(distinct, where, whereArgs);
    }

    /**
     * Query for rows within the bounds
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @return cursor
     * @since 3.5.0
     */
    public UserCustomCursor query(String[] columns, double minX, double minY,
                                  double maxX, double maxY) {
        return query(false, columns, minX, minY, maxX, maxY);
    }

    /**
     * Query for rows within the bounds
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @return cursor
     * @since 4.0.0
     */
    public UserCustomCursor query(boolean distinct, String[] columns,
                                  double minX, double minY, double maxX, double maxY) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return query(distinct, columns, where, whereArgs);
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
        return count(false, null, minX, minY, maxX, maxY);
    }

    /**
     * Count the rows within the bounds
     *
     * @param column count column name
     * @param minX   min x
     * @param minY   min y
     * @param maxX   max x
     * @param maxY   max y
     * @return count
     * @since 4.0.0
     */
    public int count(String column, double minX, double minY, double maxX,
                     double maxY) {
        return count(false, column, minX, minY, maxX, maxY);
    }

    /**
     * Count the rows within the bounds
     *
     * @param distinct distinct column values
     * @param column   count column name
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @return count
     * @since 4.0.0
     */
    public int count(boolean distinct, String column, double minX, double minY,
                     double maxX, double maxY) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return count(distinct, column, where, whereArgs);
    }

    /**
     * Query for features within the bounds
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(double minX, double minY, double maxX,
                                       double maxY) {
        return queryFeatures(false, minX, minY, maxX, maxY);
    }

    /**
     * Query for features within the bounds
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @return cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, double minX,
                                       double minY, double maxX, double maxY) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryIn(distinct, queryIdsSQL(where), whereArgs);
    }

    /**
     * Query for features within the bounds
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @return cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, double minX,
                                       double minY, double maxX, double maxY) {
        return queryFeatures(false, columns, minX, minY, maxX, maxY);
    }

    /**
     * Query for features within the bounds
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @return cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       double minX, double minY, double maxX, double maxY) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryIn(distinct, columns, queryIdsSQL(where),
                whereArgs);
    }

    /**
     * Count the features within the bounds
     *
     * @param minX min x
     * @param minY min y
     * @param maxX max x
     * @param maxY max y
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(double minX, double minY, double maxX,
                             double maxY) {
        return countFeatures(false, null, minX, minY, maxX, maxY);
    }

    /**
     * Count the features within the bounds
     *
     * @param column count column name
     * @param minX   min x
     * @param minY   min y
     * @param maxX   max x
     * @param maxY   max y
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, double minX, double minY,
                             double maxX, double maxY) {
        return countFeatures(false, column, minX, minY, maxX, maxY);
    }

    /**
     * Count the features within the bounds
     *
     * @param distinct distinct column values
     * @param column   count column name
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, double minX,
                             double minY, double maxX, double maxY) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.countIn(distinct, column, queryIdsSQL(where),
                whereArgs);
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
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(double minX, double minY, double maxX,
                                       double maxY, Map<String, Object> fieldValues) {
        return queryFeatures(false, minX, minY, maxX, maxY, fieldValues);
    }

    /**
     * Query for features within the bounds
     *
     * @param distinct    distinct rows
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, double minX,
                                       double minY, double maxX, double maxY,
                                       Map<String, Object> fieldValues) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryIn(distinct, queryIdsSQL(where), whereArgs,
                fieldValues);
    }

    /**
     * Query for features within the bounds
     *
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, double minX,
                                       double minY, double maxX, double maxY,
                                       Map<String, Object> fieldValues) {
        return queryFeatures(false, columns, minX, minY, maxX, maxY,
                fieldValues);
    }

    /**
     * Query for features within the bounds
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       double minX, double minY, double maxX, double maxY,
                                       Map<String, Object> fieldValues) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryIn(distinct, columns, queryIdsSQL(where),
                whereArgs, fieldValues);
    }

    /**
     * Count the features within the bounds
     *
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(double minX, double minY, double maxX, double maxY,
                             Map<String, Object> fieldValues) {
        return countFeatures(false, null, minX, minY, maxX, maxY, fieldValues);
    }

    /**
     * Count the features within the bounds
     *
     * @param column      count column name
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, double minX, double minY,
                             double maxX, double maxY, Map<String, Object> fieldValues) {
        return countFeatures(false, column, minX, minY, maxX, maxY,
                fieldValues);
    }

    /**
     * Count the features within the bounds
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, double minX,
                             double minY, double maxX, double maxY,
                             Map<String, Object> fieldValues) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.countIn(distinct, column, queryIdsSQL(where),
                whereArgs, fieldValues);
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
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(double minX, double minY, double maxX,
                                       double maxY, String where) {
        return queryFeatures(false, minX, minY, maxX, maxY, where);
    }

    /**
     * Query for features within the bounds
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @return cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, double minX,
                                       double minY, double maxX, double maxY, String where) {
        return queryFeatures(distinct, minX, minY, maxX, maxY, where, null);
    }

    /**
     * Query for features within the bounds
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param where   where clause
     * @return cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, double minX,
                                       double minY, double maxX, double maxY, String where) {
        return queryFeatures(false, columns, minX, minY, maxX, maxY, where);
    }

    /**
     * Query for features within the bounds
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @return cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       double minX, double minY, double maxX, double maxY, String where) {
        return queryFeatures(distinct, columns, minX, minY, maxX, maxY, where,
                null);
    }

    /**
     * Count the features within the bounds
     *
     * @param minX  min x
     * @param minY  min y
     * @param maxX  max x
     * @param maxY  max y
     * @param where where clause
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(double minX, double minY, double maxX, double maxY,
                             String where) {
        return countFeatures(false, null, minX, minY, maxX, maxY, where);
    }

    /**
     * Count the features within the bounds
     *
     * @param column count column name
     * @param minX   min x
     * @param minY   min y
     * @param maxX   max x
     * @param maxY   max y
     * @param where  where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, double minX, double minY,
                             double maxX, double maxY, String where) {
        return countFeatures(false, column, minX, minY, maxX, maxY, where);
    }

    /**
     * Count the features within the bounds
     *
     * @param distinct distinct column values
     * @param column   count column name
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, double minX,
                             double minY, double maxX, double maxY, String where) {
        return countFeatures(distinct, column, minX, minY, maxX, maxY, where,
                null);
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
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(double minX, double minY, double maxX,
                                       double maxY, String where, String[] whereArgs) {
        return queryFeatures(false, minX, minY, maxX, maxY, where, whereArgs);
    }

    /**
     * Query for features within the bounds
     *
     * @param distinct  distinct rows
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @return cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, double minX,
                                       double minY, double maxX, double maxY, String where,
                                       String[] whereArgs) {
        validateRTree();
        String whereBounds = buildWhere(minX, minY, maxX, maxY);
        String[] whereBoundsArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryIn(distinct, queryIdsSQL(whereBounds),
                whereBoundsArgs, where, whereArgs);
    }

    /**
     * Query for features within the bounds
     *
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @return cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns, double minX,
                                       double minY, double maxX, double maxY, String where,
                                       String[] whereArgs) {
        return queryFeatures(false, columns, minX, minY, maxX, maxY, where,
                whereArgs);
    }

    /**
     * Query for features within the bounds
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @return cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       double minX, double minY, double maxX, double maxY, String where,
                                       String[] whereArgs) {
        validateRTree();
        String whereBounds = buildWhere(minX, minY, maxX, maxY);
        String[] whereBoundsArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryIn(distinct, columns, queryIdsSQL(whereBounds),
                whereBoundsArgs, where, whereArgs);
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
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(double minX, double minY, double maxX, double maxY,
                             String where, String[] whereArgs) {
        return countFeatures(false, null, minX, minY, maxX, maxY, where,
                whereArgs);
    }

    /**
     * Count the features within the bounds
     *
     * @param column    count column name
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, double minX, double minY,
                             double maxX, double maxY, String where, String[] whereArgs) {
        return countFeatures(false, column, minX, minY, maxX, maxY, where,
                whereArgs);
    }

    /**
     * Count the features within the bounds
     *
     * @param distinct  distinct column values
     * @param column    count column name
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, double minX,
                             double minY, double maxX, double maxY, String where,
                             String[] whereArgs) {
        validateRTree();
        String whereBounds = buildWhere(minX, minY, maxX, maxY);
        String[] whereBoundsArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.countIn(distinct, column, queryIdsSQL(whereBounds),
                whereBoundsArgs, where, whereArgs);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param limit chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(queryIdsSQL(), orderBy, limit);
    }

    /**
     * Query for all features, starting at the offset and returning no more than
     * the limit
     *
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return feature cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String orderBy, int limit,
                                               long offset) {
        validateRTree();
        return featureDao.queryInForChunk(queryIdsSQL(), orderBy, limit,
                offset);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, queryIdsSQL(), orderBy,
                limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String orderBy, int limit, long offset) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, queryIdsSQL(), orderBy,
                limit, offset);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param columns columns
     * @param limit   chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(columns, queryIdsSQL(), orderBy,
                limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String orderBy, int limit, long offset) {
        validateRTree();
        return featureDao.queryInForChunk(columns, queryIdsSQL(), orderBy,
                limit, offset);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(),
                orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String orderBy, int limit, long offset) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(),
                orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(
            Map<String, Object> fieldValues, String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(queryIdsSQL(), fieldValues, orderBy,
                limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(
            Map<String, Object> fieldValues, String orderBy, int limit,
            long offset) {
        validateRTree();
        return featureDao.queryInForChunk(queryIdsSQL(), fieldValues, orderBy,
                limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct    distinct rows
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, queryIdsSQL(), fieldValues,
                orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, queryIdsSQL(), fieldValues,
                orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param columns     columns
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(columns, queryIdsSQL(), fieldValues,
                orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        validateRTree();
        return featureDao.queryInForChunk(columns, queryIdsSQL(), fieldValues,
                orderBy, limit, offset);
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, Map<String, Object> fieldValues, String orderBy,
                                               int limit) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(),
                fieldValues, orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, Map<String, Object> fieldValues, String orderBy,
                                               int limit, long offset) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(),
                fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features ordered by id, starting at the offset and returning no
     * more than the limit
     *
     * @param where where clause
     * @param limit chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String where,
                                               String[] whereArgs, String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(queryIdsSQL(), where, whereArgs,
                orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        validateRTree();
        return featureDao.queryInForChunk(queryIdsSQL(), where, whereArgs,
                orderBy, limit, offset);
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String[] whereArgs, String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, queryIdsSQL(), where,
                whereArgs, orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, queryIdsSQL(), where,
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String[] whereArgs, String orderBy, int limit) {
        validateRTree();
        return featureDao.queryInForChunk(columns, queryIdsSQL(), where,
                whereArgs, orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
        validateRTree();
        return featureDao.queryInForChunk(columns, queryIdsSQL(), where,
                whereArgs, orderBy, limit, offset);
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String[] whereArgs, String orderBy,
                                               int limit) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(),
                where, whereArgs, orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String[] whereArgs, String orderBy,
                                               int limit, long offset) {
        validateRTree();
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(),
                where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for rows within the bounding box ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param boundingBox bounding box
     * @param limit       chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(BoundingBox boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(BoundingBox boundingBox, int limit,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(BoundingBox boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(BoundingBox boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(BoundingBox boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(BoundingBox boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(BoundingBox boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(BoundingBox boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
                                          BoundingBox boundingBox, Projection projection, String orderBy,
                                          int limit) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
                                          BoundingBox boundingBox, Projection projection, String orderBy,
                                          int limit, long offset) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          BoundingBox boundingBox, Projection projection, String orderBy,
                                          int limit) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          BoundingBox boundingBox, Projection projection, String orderBy,
                                          int limit, long offset) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String orderBy,
                                               int limit) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String orderBy,
                                               int limit, long offset) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String orderBy, int limit) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String orderBy, int limit, long offset) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, String orderBy, int limit) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               BoundingBox boundingBox, Projection projection, String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String where, String[] whereArgs, String orderBy, int limit) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, BoundingBox boundingBox, Projection projection,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
        BoundingBox featureBoundingBox = projectBoundingBox(boundingBox,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(GeometryEnvelope envelope,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(GeometryEnvelope envelope,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(GeometryEnvelope envelope,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(GeometryEnvelope envelope,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
                                          GeometryEnvelope envelope, String orderBy, int limit) {
        return queryForChunk(distinct, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), orderBy, limit);
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct,
                                          GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryForChunk(distinct, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), orderBy, limit, offset);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          GeometryEnvelope envelope, String orderBy, int limit) {
        return queryForChunk(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                orderBy, limit);
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
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryForChunk(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String orderBy,
                                               int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                fieldValues, orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                fieldValues, orderBy, limit, offset);
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                fieldValues, orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                where, whereArgs, orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                where, whereArgs, orderBy, limit, offset);
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String[] whereArgs, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                where, whereArgs, orderBy, limit);
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
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(),
                where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for rows within the bounds ordered by id, starting at the offset
     * and returning no more than the limit
     *
     * @param minX  min x
     * @param minY  min y
     * @param maxX  max x
     * @param maxY  max y
     * @param limit chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(double minX, double minY,
                                          double maxX, double maxY, int limit) {
        return queryForChunk(minX, minY, maxX, maxY, getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounds ordered by id, starting at the offset
     * and returning no more than the limit
     *
     * @param minX   min x
     * @param minY   min y
     * @param maxX   max x
     * @param maxY   max y
     * @param limit  chunk limit
     * @param offset chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(double minX, double minY,
                                          double maxX, double maxY, int limit, long offset) {
        return queryForChunk(minX, minY, maxX, maxY, getPkColumnName(), limit,
                offset);
    }

    /**
     * Query for rows within the bounds, starting at the offset and returning no
     * more than the limit
     *
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param orderBy order by
     * @param limit   chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(double minX, double minY,
                                          double maxX, double maxY, String orderBy, int limit) {
        return queryForChunk(false, minX, minY, maxX, maxY, orderBy, limit);
    }

    /**
     * Query for rows within the bounds, starting at the offset and returning no
     * more than the limit
     *
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(double minX, double minY,
                                          double maxX, double maxY, String orderBy, int limit, long offset) {
        return queryForChunk(false, minX, minY, maxX, maxY, orderBy, limit,
                offset);
    }

    /**
     * Query for rows within the bounds ordered by id, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, double minX,
                                          double minY, double maxX, double maxY, int limit) {
        return queryForChunk(distinct, minX, minY, maxX, maxY,
                getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounds ordered by id, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, double minX,
                                          double minY, double maxX, double maxY, int limit, long offset) {
        return queryForChunk(distinct, minX, minY, maxX, maxY,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for rows within the bounds, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, double minX,
                                          double minY, double maxX, double maxY, String orderBy, int limit) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return queryForChunk(distinct, where, whereArgs, orderBy, limit);
    }

    /**
     * Query for rows within the bounds, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, double minX,
                                          double minY, double maxX, double maxY, String orderBy, int limit,
                                          long offset) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return queryForChunk(distinct, where, whereArgs, orderBy, limit,
                offset);
    }

    /**
     * Query for rows within the bounds ordered by id, starting at the offset
     * and returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param limit   chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns, double minX,
                                          double minY, double maxX, double maxY, int limit) {
        return queryForChunk(columns, minX, minY, maxX, maxY, getPkColumnName(),
                limit);
    }

    /**
     * Query for rows within the bounds ordered by id, starting at the offset
     * and returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns, double minX,
                                          double minY, double maxX, double maxY, int limit, long offset) {
        return queryForChunk(columns, minX, minY, maxX, maxY, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for rows within the bounds, starting at the offset and returning no
     * more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param orderBy order by
     * @param limit   chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns, double minX,
                                          double minY, double maxX, double maxY, String orderBy, int limit) {
        return queryForChunk(false, columns, minX, minY, maxX, maxY, orderBy,
                limit);
    }

    /**
     * Query for rows within the bounds, starting at the offset and returning no
     * more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(String[] columns, double minX,
                                          double minY, double maxX, double maxY, String orderBy, int limit,
                                          long offset) {
        return queryForChunk(false, columns, minX, minY, maxX, maxY, orderBy,
                limit, offset);
    }

    /**
     * Query for rows within the bounds ordered by id, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          double minX, double minY, double maxX, double maxY, int limit) {
        return queryForChunk(distinct, columns, minX, minY, maxX, maxY,
                getPkColumnName(), limit);
    }

    /**
     * Query for rows within the bounds ordered by id, starting at the offset
     * and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          double minX, double minY, double maxX, double maxY, int limit,
                                          long offset) {
        return queryForChunk(distinct, columns, minX, minY, maxX, maxY,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for rows within the bounds, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          double minX, double minY, double maxX, double maxY, String orderBy,
                                          int limit) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return queryForChunk(distinct, columns, where, whereArgs, orderBy,
                limit);
    }

    /**
     * Query for rows within the bounds, starting at the offset and returning no
     * more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          double minX, double minY, double maxX, double maxY, String orderBy,
                                          int limit, long offset) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return queryForChunk(distinct, columns, where, whereArgs, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param minX  min x
     * @param minY  min y
     * @param maxX  max x
     * @param maxY  max y
     * @param limit chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, int limit) {
        return queryFeaturesForChunk(minX, minY, maxX, maxY, getPkColumnName(),
                limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param minX   min x
     * @param minY   min y
     * @param maxX   max x
     * @param maxY   max y
     * @param limit  chunk limit
     * @param offset chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, int limit, long offset) {
        return queryFeaturesForChunk(minX, minY, maxX, maxY, getPkColumnName(),
                limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param orderBy order by
     * @param limit   chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, String orderBy, int limit) {
        return queryFeaturesForChunk(false, minX, minY, maxX, maxY, orderBy,
                limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, minX, minY, maxX, maxY, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, int limit) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, int limit, long offset) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, String orderBy, int limit) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, queryIdsSQL(where),
                whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, String orderBy, int limit,
                                               long offset) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, queryIdsSQL(where),
                whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param limit   chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, int limit) {
        return queryFeaturesForChunk(columns, minX, minY, maxX, maxY,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, int limit, long offset) {
        return queryFeaturesForChunk(columns, minX, minY, maxX, maxY,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param orderBy order by
     * @param limit   chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, minX, minY, maxX, maxY,
                orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, columns, minX, minY, maxX, maxY,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, int limit) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, String orderBy, int limit) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(where),
                whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, String orderBy, int limit, long offset) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(where),
                whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, Map<String, Object> fieldValues,
                                               int limit) {
        return queryFeaturesForChunk(minX, minY, maxX, maxY, fieldValues,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, Map<String, Object> fieldValues,
                                               int limit, long offset) {
        return queryFeaturesForChunk(minX, minY, maxX, maxY, fieldValues,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, Map<String, Object> fieldValues,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, minX, minY, maxX, maxY, fieldValues,
                orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, Map<String, Object> fieldValues,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, minX, minY, maxX, maxY, fieldValues,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY,
                fieldValues, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY,
                fieldValues, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, queryIdsSQL(where),
                whereArgs, fieldValues, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, queryIdsSQL(where),
                whereArgs, fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY,
                                               Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(columns, minX, minY, maxX, maxY,
                fieldValues, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY,
                                               Map<String, Object> fieldValues, int limit, long offset) {
        return queryFeaturesForChunk(columns, minX, minY, maxX, maxY,
                fieldValues, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, minX, minY, maxX, maxY,
                fieldValues, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, columns, minX, minY, maxX, maxY,
                fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, Map<String, Object> fieldValues, int limit) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                fieldValues, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, Map<String, Object> fieldValues, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                fieldValues, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, Map<String, Object> fieldValues, String orderBy,
                                               int limit) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(where),
                whereArgs, fieldValues, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param minX        min x
     * @param minY        min y
     * @param maxX        max x
     * @param maxY        max y
     * @param fieldValues field values
     * @param orderBy     order by
     * @param limit       chunk limit
     * @param offset      chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, Map<String, Object> fieldValues, String orderBy,
                                               int limit, long offset) {
        validateRTree();
        String where = buildWhere(minX, minY, maxX, maxY);
        String[] whereArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, columns, queryIdsSQL(where),
                whereArgs, fieldValues, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param minX  min x
     * @param minY  min y
     * @param maxX  max x
     * @param maxY  max y
     * @param where where clause
     * @param limit chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(double minX,
                                                      double minY, double maxX, double maxY, String where, int limit) {
        return queryFeaturesForChunk(minX, minY, maxX, maxY, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param minX   min x
     * @param minY   min y
     * @param maxX   max x
     * @param maxY   max y
     * @param where  where clause
     * @param limit  chunk limit
     * @param offset chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(double minX,
                                                      double minY, double maxX, double maxY, String where, int limit,
                                                      long offset) {
        return queryFeaturesForChunk(minX, minY, maxX, maxY, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param where   where clause
     * @param orderBy order by
     * @param limit   chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, String where, String orderBy, int limit) {
        return queryFeaturesForChunk(false, minX, minY, maxX, maxY, where,
                orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param where   where clause
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, String where, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(false, minX, minY, maxX, maxY, where,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      double minX, double minY, double maxX, double maxY, String where,
                                                      int limit) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      double minX, double minY, double maxX, double maxY, String where,
                                                      int limit, long offset) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, String where, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY, where,
                null, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, String where, String orderBy,
                                               int limit, long offset) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY, where,
                null, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param where   where clause
     * @param limit   chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      double minX, double minY, double maxX, double maxY, String where,
                                                      int limit) {
        return queryFeaturesForChunk(columns, minX, minY, maxX, maxY, where,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param where   where clause
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(String[] columns,
                                                      double minX, double minY, double maxX, double maxY, String where,
                                                      int limit, long offset) {
        return queryFeaturesForChunk(columns, minX, minY, maxX, maxY, where,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param where   where clause
     * @param orderBy order by
     * @param limit   chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, String where, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(false, columns, minX, minY, maxX, maxY,
                where, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param columns columns
     * @param minX    min x
     * @param minY    min y
     * @param maxX    max x
     * @param maxY    max y
     * @param where   where clause
     * @param orderBy order by
     * @param limit   chunk limit
     * @param offset  chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, String where, String orderBy,
                                               int limit, long offset) {
        return queryFeaturesForChunk(false, columns, minX, minY, maxX, maxY,
                where, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, double minX, double minY, double maxX,
                                                      double maxY, String where, int limit) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                where, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunkIdOrder(boolean distinct,
                                                      String[] columns, double minX, double minY, double maxX,
                                                      double maxY, String where, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                where, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, String where, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                where, null, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param minX     min x
     * @param minY     min y
     * @param maxX     max x
     * @param maxY     max y
     * @param where    where clause
     * @param orderBy  order by
     * @param limit    chunk limit
     * @param offset   chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, String where, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                where, null, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, String where, String[] whereArgs,
                                               int limit) {
        return queryFeaturesForChunk(minX, minY, maxX, maxY, where, whereArgs,
                getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, String where, String[] whereArgs,
                                               int limit, long offset) {
        return queryFeaturesForChunk(minX, minY, maxX, maxY, where, whereArgs,
                getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, String where, String[] whereArgs,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(false, minX, minY, maxX, maxY, where,
                whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(double minX, double minY,
                                               double maxX, double maxY, String where, String[] whereArgs,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, minX, minY, maxX, maxY, where,
                whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, String where,
                                               String[] whereArgs, int limit) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY, where,
                whereArgs, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, String where,
                                               String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(distinct, minX, minY, maxX, maxY, where,
                whereArgs, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, String where,
                                               String[] whereArgs, String orderBy, int limit) {
        validateRTree();
        String whereBounds = buildWhere(minX, minY, maxX, maxY);
        String[] whereBoundsArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, queryIdsSQL(whereBounds),
                whereBoundsArgs, where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct, double minX,
                                               double minY, double maxX, double maxY, String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        validateRTree();
        String whereBounds = buildWhere(minX, minY, maxX, maxY);
        String[] whereBoundsArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, queryIdsSQL(whereBounds),
                whereBoundsArgs, where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, String where,
                                               String[] whereArgs, int limit) {
        return queryFeaturesForChunk(columns, minX, minY, maxX, maxY, where,
                whereArgs, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, String where,
                                               String[] whereArgs, int limit, long offset) {
        return queryFeaturesForChunk(columns, minX, minY, maxX, maxY, where,
                whereArgs, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, String where,
                                               String[] whereArgs, String orderBy, int limit) {
        return queryFeaturesForChunk(false, columns, minX, minY, maxX, maxY,
                where, whereArgs, orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns, double minX,
                                               double minY, double maxX, double maxY, String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(false, columns, minX, minY, maxX, maxY,
                where, whereArgs, orderBy, limit, offset);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, String where, String[] whereArgs, int limit) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                where, whereArgs, getPkColumnName(), limit);
    }

    /**
     * Query for features within the bounds ordered by id, starting at the
     * offset and returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, String where, String[] whereArgs, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, columns, minX, minY, maxX, maxY,
                where, whereArgs, getPkColumnName(), limit, offset);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, String where, String[] whereArgs, String orderBy,
                                               int limit) {
        validateRTree();
        String whereBounds = buildWhere(minX, minY, maxX, maxY);
        String[] whereBoundsArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, columns,
                queryIdsSQL(whereBounds), whereBoundsArgs, where, whereArgs,
                orderBy, limit);
    }

    /**
     * Query for features within the bounds, starting at the offset and
     * returning no more than the limit
     *
     * @param distinct  distinct rows
     * @param columns   columns
     * @param minX      min x
     * @param minY      min y
     * @param maxX      max x
     * @param maxY      max y
     * @param where     where clause
     * @param whereArgs where arguments
     * @param orderBy   order by
     * @param limit     chunk limit
     * @param offset    chunk query offset
     * @return cursor
     * @since 6.1.4
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, double minX, double minY, double maxX,
                                               double maxY, String where, String[] whereArgs, String orderBy,
                                               int limit, long offset) {
        validateRTree();
        String whereBounds = buildWhere(minX, minY, maxX, maxY);
        String[] whereBoundsArgs = buildWhereArgs(minX, minY, maxX, maxY);
        return featureDao.queryInForChunk(distinct, columns,
                queryIdsSQL(whereBounds), whereBoundsArgs, where, whereArgs,
                orderBy, limit, offset);
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
