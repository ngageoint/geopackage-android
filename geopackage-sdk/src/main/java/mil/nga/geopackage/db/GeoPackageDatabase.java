package mil.nga.geopackage.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * GeoPackage database wrapper around SQLiteDatabase to quote table and column names
 *
 * @author osbornb
 * @since 1.3.1
 */
public class GeoPackageDatabase {

    /**
     * Database connection
     */
    private final SQLiteDatabase db;

    /**
     * Constructor
     *
     * @param db database
     */
    public GeoPackageDatabase(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Get the SQLite database connection
     *
     * @return connection
     */
    public SQLiteDatabase getDb() {
        return db;
    }

    /**
     * @see SQLiteDatabase#execSQL(String)
     */
    public void execSQL(String sql) throws SQLException {
        db.execSQL(sql);
    }

    /**
     * @see SQLiteDatabase#delete(String, String, String[])
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        return db.delete(CoreSQLUtils.quoteWrap(table), whereClause, whereArgs);
    }

    /**
     * @see SQLiteDatabase#rawQuery(String, String[])
     */
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }

    /**
     * @see SQLiteDatabase#close()
     */
    public void close() {
        db.close();
    }

    /**
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String)
     */
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        return db.query(CoreSQLUtils.quoteWrap(table), CoreSQLUtils.quoteWrap(columns), selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String)
     * @since 2.0.0
     */
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        String[] wrappedColumns = CoreSQLUtils.quoteWrap(columns);
        String[] wrappedColumnsAs = CoreSQLUtils.buildColumnsAs(wrappedColumns, columnsAs);
        return db.query(CoreSQLUtils.quoteWrap(table), wrappedColumnsAs, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        return db.query(CoreSQLUtils.quoteWrap(table), CoreSQLUtils.quoteWrap(columns), selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     * @since 2.0.0
     */
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        String[] wrappedColumns = CoreSQLUtils.quoteWrap(columns);
        String[] wrappedColumnsAs = CoreSQLUtils.buildColumnsAs(wrappedColumns, columnsAs);
        return db.query(CoreSQLUtils.quoteWrap(table), wrappedColumnsAs, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * @see SQLiteDatabase#update(String, ContentValues, String, String[])
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return db.update(CoreSQLUtils.quoteWrap(table), SQLUtils.quoteWrap(values), whereClause, whereArgs);
    }

    /**
     * @see SQLiteDatabase#insertOrThrow(String, String, ContentValues)
     */
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return db.insertOrThrow(CoreSQLUtils.quoteWrap(table), nullColumnHack, SQLUtils.quoteWrap(values));
    }

    /**
     * @see SQLiteDatabase#insert(String, String, ContentValues)
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return db.insert(CoreSQLUtils.quoteWrap(table), nullColumnHack, SQLUtils.quoteWrap(values));
    }

}
