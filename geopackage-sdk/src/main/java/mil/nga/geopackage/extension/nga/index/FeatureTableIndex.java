package mil.nga.geopackage.extension.nga.index;

import android.util.Log;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Callable;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureRowSync;
import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.proj.Projection;
import mil.nga.sf.GeometryEnvelope;

/**
 * Feature Table Index NGA Extension implementation. This extension is used to
 * index Geometries within a feature table by their minimum bounding box for
 * bounding box queries. This extension is required to provide an index
 * implementation when a SQLite version is used before SpatialLite support
 * (Android).
 * <p>
 * http://ngageoint.github.io/GeoPackage/docs/extensions/geometry-index.html
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
     * {@inheritDoc}
     */
    @Override
    public Projection getProjection() {
        return featureDao.getProjection();
    }

    /**
     * Get the primary key column name
     *
     * @return primary key column name
     * @since 6.2.0
     */
    public String getPkColumnName() {
        return featureDao.getPkColumnName();
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

        int count = 0;

        long offset = 0;
        int chunkCount = 0;

        String[] columns = featureDao.getIdAndGeometryColumnNames();

        while (chunkCount >= 0) {

            final long chunkOffset = offset;

            try {
                // Iterate through each row and index as a single transaction
                ConnectionSource connectionSource = getGeoPackage().getDatabase()
                        .getConnectionSource();
                chunkCount = TransactionManager.callInTransaction(connectionSource,
                        new Callable<Integer>() {
                            public Integer call() throws Exception {

                                FeatureCursor cursor = featureDao.queryForChunk(columns, chunkLimit, chunkOffset);
                                int count = indexRows(tableIndex, cursor);

                                return count;
                            }
                        });
                if (chunkCount > 0) {
                    count += chunkCount;
                }
            } catch (SQLException e) {
                throw new GeoPackageException("Failed to Index Table. GeoPackage: "
                        + getGeoPackage().getName() + ", Table: " + getTableName(),
                        e);
            }

            offset += chunkLimit;
        }

        // Update the last indexed time
        if (progress == null || progress.isActive()) {
            updateLastIndexed();
        }

        return count;
    }

    /**
     * Index the feature rows in the cursor
     *
     * @param tableIndex table index
     * @param cursor     feature cursor
     * @return count, -1 if no results or canceled
     */
    private int indexRows(TableIndex tableIndex, FeatureCursor cursor) {

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

    /**
     * Query for all Features
     *
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures() {
        return featureDao.queryIn(queryIdsSQL());
    }

    /**
     * Query for all Features
     *
     * @param distinct distinct rows
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct) {
        return featureDao.queryIn(distinct, queryIdsSQL());
    }

    /**
     * Query for all Features
     *
     * @param columns columns
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns) {
        return featureDao.queryIn(columns, queryIdsSQL());
    }

    /**
     * Query for all Features
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns) {
        return featureDao.queryIn(distinct, columns, queryIdsSQL());
    }

    /**
     * Count features
     *
     * @return count
     * @since 4.0.0
     */
    public int countFeatures() {
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
        return featureDao.countIn(queryIdsSQL(), fieldValues);
    }

    /**
     * Count features
     *
     * @param column      count column name
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, Map<String, Object> fieldValues) {
        return featureDao.countIn(column, queryIdsSQL(), fieldValues);
    }

    /**
     * Count features
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column,
                             Map<String, Object> fieldValues) {
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
        return featureDao.queryIn(queryIdsSQL(), where);
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
        return featureDao.queryIn(distinct, queryIdsSQL(), where);
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
        return featureDao.queryIn(columns, queryIdsSQL(), where);
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
        return featureDao.queryIn(distinct, columns, queryIdsSQL(), where);
    }

    /**
     * Count features
     *
     * @param where where clause
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(String where) {
        return featureDao.countIn(queryIdsSQL(), where);
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
        return featureDao.countIn(column, queryIdsSQL(), where);
    }

    /**
     * Count features
     *
     * @param column   count column name
     * @param distinct distinct column values
     * @param where    where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column, String where) {
        return featureDao.countIn(distinct, column, queryIdsSQL(), where);
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
        return featureDao.countIn(distinct, column, queryIdsSQL(), where,
                whereArgs);
    }

    /**
     * Query for Features within the bounding box, projected correctly
     *
     * @param boundingBox bounding box
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox) {
        return queryFeatures(false, boundingBox);
    }

    /**
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Count the Features within the bounding box, projected correctly
     *
     * @param boundingBox bounding box
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox) {
        return countFeatures(false, null, boundingBox);
    }

    /**
     * Count the Features within the bounding box, projected correctly
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox) {
        return countFeatures(false, column, boundingBox);
    }

    /**
     * Count the Features within the bounding box, projected correctly
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox) {
        return countFeatures(distinct, column, boundingBox.buildEnvelope());
    }

    /**
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Count the Features within the bounding box, projected correctly
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
     * Count the Features within the bounding box, projected correctly
     *
     * @param column      count column
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
     * Count the Features within the bounding box, projected correctly
     *
     * @param distinct    distinct column values
     * @param column      count column
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
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Count the Features within the bounding box, projected correctly
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
     * Count the Features within the bounding box, projected correctly
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
     * Count the Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box, projected correctly
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
     * Count the Features within the bounding box, projected correctly
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
     * Count the Features within the bounding box, projected correctly
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
     * Count the Features within the bounding box, projected correctly
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection) {
        return queryFeatures(false, boundingBox, projection);
    }

    /**
     * Query for Features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox, Projection projection) {
        return queryFeatures(false, columns, boundingBox, projection);
    }

    /**
     * Query for Features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Count the Features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection) {
        return countFeatures(false, null, boundingBox, projection);
    }

    /**
     * Count the Features within the bounding box in the provided projection
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox,
                             Projection projection) {
        return countFeatures(false, column, boundingBox, projection);
    }

    /**
     * Count the Features within the bounding box in the provided projection
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection, Map<String, Object> fieldValues) {
        return queryFeatures(false, boundingBox, projection, fieldValues);
    }

    /**
     * Query for Features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Count the Features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection,
                             Map<String, Object> fieldValues) {
        return countFeatures(false, null, boundingBox, projection, fieldValues);
    }

    /**
     * Count the Features within the bounding box in the provided projection
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Count the Features within the bounding box in the provided projection
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @param where       where clause
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(BoundingBox boundingBox,
                                       Projection projection, String where) {
        return queryFeatures(false, boundingBox, projection, where);
    }

    /**
     * Query for Features within the bounding box in the provided projection
     *
     * @param distinct    distinct row
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @param where       where clause
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, Projection projection, String where) {
        return queryFeatures(distinct, boundingBox, projection, where, null);
    }

    /**
     * Query for Features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @param where       where clause
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       BoundingBox boundingBox, Projection projection, String where) {
        return queryFeatures(false, columns, boundingBox, projection, where);
    }

    /**
     * Query for Features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Count the Features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @param where       where clause
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(BoundingBox boundingBox, Projection projection,
                             String where) {
        return countFeatures(false, null, boundingBox, projection, where);
    }

    /**
     * Count the Features within the bounding box in the provided projection
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
     * @param where       where clause
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, BoundingBox boundingBox,
                             Projection projection, String where) {
        return countFeatures(false, column, boundingBox, projection, where);
    }

    /**
     * Count the Features within the bounding box in the provided projection
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the bounding box in the provided projection
     *
     * @param distinct    distinct rows
     * @param columns     columns
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Count the Features within the bounding box in the provided projection
     *
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Count the Features within the bounding box in the provided projection
     *
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Count the Features within the bounding box in the provided projection
     *
     * @param distinct    distinct column values
     * @param column      count column name
     * @param boundingBox bounding box
     * @param projection  projection of the provided bounding box
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
     * Query for Features within the Geometry Envelope
     *
     * @param envelope geometry envelope
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope) {
        return featureDao.queryIn(queryIdsSQL(envelope));
    }

    /**
     * Query for Features within the Geometry Envelope
     *
     * @param distinct distinct rows
     * @param envelope geometry envelope
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       GeometryEnvelope envelope) {
        return featureDao.queryIn(distinct, queryIdsSQL(envelope));
    }

    /**
     * Query for Features within the Geometry Envelope
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       GeometryEnvelope envelope) {
        return featureDao.queryIn(columns, queryIdsSQL(envelope));
    }

    /**
     * Query for Features within the Geometry Envelope
     *
     * @param distinct distinct rows
     * @param columns  columns
     * @param envelope geometry envelope
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       GeometryEnvelope envelope) {
        return featureDao.queryIn(distinct, columns, queryIdsSQL(envelope));
    }

    /**
     * Count the Features within the Geometry Envelope
     *
     * @param envelope geometry envelope
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(GeometryEnvelope envelope) {
        return featureDao.countIn(queryIdsSQL(envelope));
    }

    /**
     * Count the Features within the Geometry Envelope
     *
     * @param column   count column name
     * @param envelope geometry envelope
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, GeometryEnvelope envelope) {
        return featureDao.countIn(column, queryIdsSQL(envelope));
    }

    /**
     * Count the Features within the Geometry Envelope
     *
     * @param distinct distinct column values
     * @param column   count column name
     * @param envelope geometry envelope
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column,
                             GeometryEnvelope envelope) {
        return featureDao.countIn(distinct, column, queryIdsSQL(envelope));
    }

    /**
     * Query for Features within the Geometry Envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope,
                                       Map<String, Object> fieldValues) {
        return featureDao.queryIn(queryIdsSQL(envelope), fieldValues);
    }

    /**
     * Query for Features within the Geometry Envelope
     *
     * @param distinct    distinct rows
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature cursor
     * @since 4.0.0
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       GeometryEnvelope envelope, Map<String, Object> fieldValues) {
        return featureDao.queryIn(distinct, queryIdsSQL(envelope), fieldValues);
    }

    /**
     * Query for Features within the Geometry Envelope
     *
     * @param columns     columns
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return feature cursor
     * @since 3.5.0
     */
    public FeatureCursor queryFeatures(String[] columns,
                                       GeometryEnvelope envelope, Map<String, Object> fieldValues) {
        return featureDao.queryIn(columns, queryIdsSQL(envelope), fieldValues);
    }

    /**
     * Query for Features within the Geometry Envelope
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
        return featureDao.queryIn(distinct, columns, queryIdsSQL(envelope),
                fieldValues);
    }

    /**
     * Count the Features within the Geometry Envelope
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(GeometryEnvelope envelope,
                             Map<String, Object> fieldValues) {
        return featureDao.countIn(queryIdsSQL(envelope), fieldValues);
    }

    /**
     * Count the Features within the Geometry Envelope
     *
     * @param column      count column names
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(String column, GeometryEnvelope envelope,
                             Map<String, Object> fieldValues) {
        return featureDao.countIn(column, queryIdsSQL(envelope), fieldValues);
    }

    /**
     * Count the Features within the Geometry Envelope
     *
     * @param distinct    distinct column values
     * @param column      count column names
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @return count
     * @since 4.0.0
     */
    public int countFeatures(boolean distinct, String column,
                             GeometryEnvelope envelope, Map<String, Object> fieldValues) {
        return featureDao.countIn(distinct, column, queryIdsSQL(envelope),
                fieldValues);
    }

    /**
     * Query for Features within the Geometry Envelope
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
     * Query for Features within the Geometry Envelope
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
     * Query for Features within the Geometry Envelope
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
     * Query for Features within the Geometry Envelope
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
     * Count the Features within the Geometry Envelope
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
     * Count the Features within the Geometry Envelope
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
     * Count the Features within the Geometry Envelope
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
     * Query for Features within the Geometry Envelope
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return feature cursor
     * @since 3.4.0
     */
    public FeatureCursor queryFeatures(GeometryEnvelope envelope,
                                       String where, String[] whereArgs) {
        return featureDao.queryIn(queryIdsSQL(envelope), where, whereArgs);
    }

    /**
     * Query for Features within the Geometry Envelope
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
        return featureDao.queryIn(distinct, queryIdsSQL(envelope), where,
                whereArgs);
    }

    /**
     * Query for Features within the Geometry Envelope
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
        return featureDao.queryIn(columns, queryIdsSQL(envelope), where,
                whereArgs);
    }

    /**
     * Query for Features within the Geometry Envelope
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
        return featureDao.queryIn(distinct, columns, queryIdsSQL(envelope),
                where, whereArgs);
    }

    /**
     * Count the Features within the Geometry Envelope
     *
     * @param envelope  geometry envelope
     * @param where     where clause
     * @param whereArgs where arguments
     * @return count
     * @since 3.4.0
     */
    public int countFeatures(GeometryEnvelope envelope, String where,
                             String[] whereArgs) {
        return featureDao.countIn(queryIdsSQL(envelope), where, whereArgs);
    }

    /**
     * Count the Features within the Geometry Envelope
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
        return featureDao.countIn(column, queryIdsSQL(envelope), where,
                whereArgs);
    }

    /**
     * Count the Features within the Geometry Envelope
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
        return featureDao.countIn(distinct, column, queryIdsSQL(envelope),
                where, whereArgs);
    }

    /**
     * Query for all features ordered by id, starting at the offset and
     * returning no more than the limit
     *
     * @param limit chunk limit
     * @return feature cursor
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String orderBy, int limit,
                                               long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String orderBy, int limit, long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String orderBy, int limit, long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String orderBy, int limit, long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(
            Map<String, Object> fieldValues, String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(
            Map<String, Object> fieldValues, String orderBy, int limit,
            long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, Map<String, Object> fieldValues, String orderBy,
                                               int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, Map<String, Object> fieldValues, String orderBy,
                                               int limit, long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String where,
                                               String[] whereArgs, String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String[] whereArgs, String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String[] whereArgs, String orderBy, int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(String[] columns,
                                               String where, String[] whereArgs, String orderBy, int limit,
                                               long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String[] whereArgs, String orderBy,
                                               int limit) {
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, String where, String[] whereArgs, String orderBy,
                                               int limit, long offset) {
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public UserCustomCursor queryForChunk(boolean distinct,
                                          GeometryEnvelope envelope, String orderBy, int limit) {
        return queryForChunk(distinct, envelope, orderBy, limit);
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
     * @since 6.2.0
     */
    public UserCustomCursor queryForChunk(boolean distinct,
                                          GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryForChunk(distinct, envelope, orderBy, limit, offset);
    }

    /**
     * Query for rows within the geometry envelope ordered by id, starting at
     * the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return cursor
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          GeometryEnvelope envelope, String orderBy, int limit) {
        return queryForChunk(distinct, columns, envelope, orderBy, limit);
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
     * @since 6.2.0
     */
    public UserCustomCursor queryForChunk(boolean distinct, String[] columns,
                                          GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryForChunk(distinct, columns, envelope, orderBy, limit,
                offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, envelope, orderBy, limit);
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope, orderBy, limit,
                offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param columns  columns
     * @param envelope geometry envelope
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String orderBy,
                                               int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope, orderBy,
                limit);
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String orderBy,
                                               int limit, long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope, orderBy,
                limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope    geometry envelope
     * @param fieldValues field values
     * @param limit       chunk limit
     * @return feature cursor
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, envelope, fieldValues, orderBy,
                limit);
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, Map<String, Object> fieldValues,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope, fieldValues, orderBy,
                limit, offset);
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope, fieldValues,
                orderBy, limit);
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope,
                                               Map<String, Object> fieldValues, String orderBy, int limit,
                                               long offset) {
        return queryFeaturesForChunk(distinct, columns, envelope, fieldValues,
                orderBy, limit, offset);
    }

    /**
     * Query for features within the geometry envelope ordered by id, starting
     * at the offset and returning no more than the limit
     *
     * @param envelope geometry envelope
     * @param where    where clause
     * @param limit    chunk limit
     * @return feature cursor
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, envelope, where, whereArgs,
                orderBy, limit);
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               GeometryEnvelope envelope, String where, String[] whereArgs,
                                               String orderBy, int limit, long offset) {
        return queryFeaturesForChunk(distinct, envelope, where, whereArgs,
                orderBy, limit, offset);
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String[] whereArgs, String orderBy, int limit) {
        return queryFeaturesForChunk(distinct, columns, envelope, where,
                whereArgs, orderBy, limit);
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
     * @since 6.2.0
     */
    public FeatureCursor queryFeaturesForChunk(boolean distinct,
                                               String[] columns, GeometryEnvelope envelope, String where,
                                               String[] whereArgs, String orderBy, int limit, long offset) {
        return featureDao.queryInForChunk(distinct, columns,
                queryIdsSQL(envelope), where, whereArgs, orderBy, limit,
                offset);
    }

}
