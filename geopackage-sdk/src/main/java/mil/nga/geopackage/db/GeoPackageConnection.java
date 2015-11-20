package mil.nga.geopackage.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * GeoPackage Android Connection wrapper
 *
 * @author osbornb
 */
public class GeoPackageConnection extends GeoPackageCoreConnection {

    /**
     * Database connection
     */
    private final SQLiteDatabase db;

    /**
     * Connection source
     */
    private final ConnectionSource connectionSource;

    /**
     * Constructor
     *
     * @param db
     */
    public GeoPackageConnection(SQLiteDatabase db) {
        this.db = db;
        this.connectionSource = new AndroidConnectionSource(db);
    }

    /**
     * Get the database connection
     *
     * @return
     */
    public SQLiteDatabase getDb() {
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
        countQuery.append("select count(*) from " + table);
        if (where != null) {
            countQuery.append(" where ").append(where);
        }
        String sql = countQuery.toString();

        int count = singleResultQuery(sql, args);

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
            minQuery.append("select min(").append(column).append(") from ")
                    .append(table);
            if (where != null) {
                minQuery.append(" where ").append(where);
            }
            String sql = minQuery.toString();

            min = singleResultQuery(sql, args);
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
            maxQuery.append("select max(").append(column).append(") from ")
                    .append(table);
            if (where != null) {
                maxQuery.append(" where ").append(where);
            }
            String sql = maxQuery.toString();

            max = singleResultQuery(sql, args);
        }

        return max;
    }

    /**
     * Query the SQL for a single integer result
     *
     * @param sql
     * @param args
     * @return int result
     */
    private int singleResultQuery(String sql, String[] args) {

        Cursor countCursor = db.rawQuery(sql, args);

        int result = 0;
        try {
            if (countCursor.moveToFirst()) {
                result = countCursor.getInt(0);
            }
        } finally {
            countCursor.close();
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

}
