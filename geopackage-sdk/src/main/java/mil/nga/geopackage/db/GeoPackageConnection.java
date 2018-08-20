package mil.nga.geopackage.db;

import android.database.Cursor;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import java.util.List;

import mil.nga.geopackage.GeoPackageException;

/**
 * GeoPackage Android Connection wrapper
 *
 * @author osbornb
 */
public class GeoPackageConnection extends GeoPackageCoreConnection {

    /**
     * Name column
     */
    private static final String NAME_COLUMN = "name";

    /**
     * Database connection
     */
    private final GeoPackageDatabase db;

    /**
     * Connection source
     */
    private final ConnectionSource connectionSource;

    /**
     * Constructor
     *
     * @param db GeoPackage connection
     */
    public GeoPackageConnection(GeoPackageDatabase db) {
        this.db = db;
        this.connectionSource = new AndroidConnectionSource(db.getDb());
    }

    /**
     * Get the database connection
     *
     * @return GeoPackage database
     */
    public GeoPackageDatabase getDb() {
        return db;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execSQL(String sql) {
        db.execSQL(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return db.delete(table, whereClause, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(String table, String where, String[] args) {

        StringBuilder countQuery = new StringBuilder();
        countQuery.append("select count(*) from ").append(CoreSQLUtils.quoteWrap(table));
        if (where != null) {
            countQuery.append(" where ").append(where);
        }
        String sql = countQuery.toString();

        int count = querySingleInteger(sql, args, true);

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer min(String table, String column, String where, String[] args) {

        Integer min = null;
        if (count(table, where, args) > 0) {
            StringBuilder minQuery = new StringBuilder();
            minQuery.append("select min(").append(CoreSQLUtils.quoteWrap(column)).append(") from ")
                    .append(CoreSQLUtils.quoteWrap(table));
            if (where != null) {
                minQuery.append(" where ").append(where);
            }
            String sql = minQuery.toString();

            min = querySingleInteger(sql, args, false);
        }

        return min;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer max(String table, String column, String where, String[] args) {

        Integer max = null;
        if (count(table, where, args) > 0) {
            StringBuilder maxQuery = new StringBuilder();
            maxQuery.append("select max(").append(CoreSQLUtils.quoteWrap(column)).append(") from ")
                    .append(CoreSQLUtils.quoteWrap(table));
            if (where != null) {
                maxQuery.append(" where ").append(where);
            }
            String sql = maxQuery.toString();

            max = querySingleInteger(sql, args, false);
        }

        return max;
    }

    /**
     * Query the SQL for a single integer result
     *
     * @param sql               SQL
     * @param args              arguments
     * @param allowEmptyResults true to accept empty results as a 0 return
     * @return int result
     */
    private int querySingleInteger(String sql, String[] args, boolean allowEmptyResults) {

        int result = 0;

        Object value = querySingleResult(sql, args, 0,
                GeoPackageDataType.MEDIUMINT);
        if (value != null) {
            result = ((Number) value).intValue();
        } else if (!allowEmptyResults) {
            throw new GeoPackageException(
                    "Failed to query for single result. SQL: " + sql);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        connectionSource.closeQuietly();
        db.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean columnExists(String tableName, String columnName) {

        boolean exists = false;

        Cursor cursor = rawQuery("PRAGMA table_info(" + CoreSQLUtils.quoteWrap(tableName) + ")", null);
        try {
            int nameIndex = cursor.getColumnIndex(NAME_COLUMN);
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                if (columnName.equals(name)) {
                    exists = true;
                    break;
                }
            }
        } finally {
            cursor.close();
        }

        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object querySingleResult(String sql, String[] args, int column,
                                    GeoPackageDataType dataType) {
        CursorResult result = wrapQuery(sql, args);
        Object value = ResultUtils.buildSingleResult(result, column, dataType);
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> querySingleColumnResults(String sql, String[] args,
                                                 int column, GeoPackageDataType dataType, Integer limit) {
        CursorResult result = wrapQuery(sql, args);
        List<Object> results = ResultUtils.buildSingleColumnResults(result,
                column, dataType, limit);
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<Object>> queryResults(String sql, String[] args,
                                           GeoPackageDataType[] dataTypes, Integer limit) {
        CursorResult result = wrapQuery(sql, args);
        List<List<Object>> results = ResultUtils.buildResults(result,
                dataTypes, limit);
        return results;
    }

    /**
     * Perform a raw database query
     *
     * @param sql  sql command
     * @param args arguments
     * @return cursor
     * @since 1.2.1
     */
    public Cursor rawQuery(String sql, String[] args) {
        return db.rawQuery(sql, args);
    }

    /**
     * Perform the query and wrap as a result
     *
     * @param sql           sql statement
     * @param selectionArgs selection arguments
     * @return result
     * @since 3.0.3
     */
    public CursorResult wrapQuery(String sql,
                                  String[] selectionArgs) {
        return new CursorResult(rawQuery(sql, selectionArgs));
    }

}
