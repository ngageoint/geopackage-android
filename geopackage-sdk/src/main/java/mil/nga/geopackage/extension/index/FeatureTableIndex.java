package mil.nga.geopackage.extension.index;

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
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.proj.Projection;

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
     * {@inheritDoc}
     */
    @Override
    public Projection getProjection() {
        return featureDao.getProjection();
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
     * @since 3.5.1
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
     * @since 3.5.1
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns) {
        return featureDao.queryIn(distinct, columns, queryIdsSQL());
    }

    /**
     * Count features
     *
     * @return count
     * @since 3.5.1
     */
    public int countFeatures() {
        return featureDao.countIn(queryIdsSQL());
    }

    /**
     * Count features
     *
     * @param column count column name
     * @return count
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
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
     * @since 3.5.1
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
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
     * @since 3.5.1
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
     * @since 3.5.1
     */
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, Projection projection) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
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
     * @since 3.5.1
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, Projection projection,
                                       Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
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
     * @since 3.5.1
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, Projection projection,
                                       Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
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
     * @since 3.5.1
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
     * @since 3.5.1
     */
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, Projection projection,
                             Map<String, Object> fieldValues) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
     */
    public FeatureCursor queryFeatures(boolean distinct,
                                       BoundingBox boundingBox, Projection projection, String where,
                                       String[] whereArgs) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
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
     * @since 3.5.1
     */
    public FeatureCursor queryFeatures(boolean distinct, String[] columns,
                                       BoundingBox boundingBox, Projection projection, String where,
                                       String[] whereArgs) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
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
     * @since 3.5.1
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
     * @since 3.5.1
     */
    public int countFeatures(boolean distinct, String column,
                             BoundingBox boundingBox, Projection projection, String where,
                             String[] whereArgs) {
        BoundingBox featureBoundingBox = getFeatureBoundingBox(boundingBox,
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
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
     * @since 3.5.1
     */
    public int countFeatures(boolean distinct, String column,
                             GeometryEnvelope envelope, String where, String[] whereArgs) {
        return featureDao.countIn(distinct, column, queryIdsSQL(envelope),
                where, whereArgs);
    }

}
