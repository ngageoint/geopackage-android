package mil.nga.geopackage.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import mil.nga.geopackage.factory.GeoPackageCursorFactory;

/**
 * GeoPackage SQLiteDatabase interface
 *
 * @author osbornb
 * @since 3.4.0
 */
public interface GeoPackageSQLiteDatabase {

    /**
     * Execute SQL
     *
     * @param sql sql command
     * @see SQLiteDatabase#execSQL(String)
     */
    public void execSQL(String sql) throws SQLException;

    /**
     * Begin a transaction
     */
    public void beginTransaction();

    /**
     * End a transaction as successful
     */
    public void endTransaction();

    /**
     * End a transaction
     *
     * @param successful true to commit, false to rollback
     */
    public void endTransaction(boolean successful);

    /**
     * End a transaction as successful and begin a new transaction
     */
    public void endAndBeginTransaction();

    /**
     * Determine if currently within a transaction
     *
     * @return true if in transaction
     */
    public boolean inTransaction();

    /**
     * Delete from table
     *
     * @param table       table name
     * @param whereClause where clause
     * @param whereArgs   where arguments
     * @return deleted rows
     * @see SQLiteDatabase#delete(String, String, String[])
     */
    public int delete(String table, String whereClause, String[] whereArgs);

    /**
     * Raw query
     *
     * @param sql           sql command
     * @param selectionArgs selection arguments
     * @return cursor
     * @see SQLiteDatabase#rawQuery(String, String[])
     */
    public Cursor rawQuery(String sql, String[] selectionArgs);

    /**
     * Raw query with factory
     *
     * @param cursorFactory cursor factory
     * @param sql           sql command
     * @param selectionArgs selection arguments
     * @param editTable     edit table name
     * @return cursor
     * @see SQLiteDatabase#rawQueryWithFactory(SQLiteDatabase.CursorFactory, String, String[], String)
     * @since 3.5.1
     */
    public Cursor rawQueryWithFactory(GeoPackageCursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable);

    /**
     * Close the connection
     *
     * @see SQLiteDatabase#close()
     */
    public void close();

    /**
     * Query the table
     *
     * @param table         table name
     * @param columns       columns
     * @param selection     selection
     * @param selectionArgs selection arguments
     * @param groupBy       group by
     * @param having        having
     * @param orderBy       order by
     * @return cursor
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String)
     */
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy);

    /**
     * Query the table
     *
     * @param table         table name
     * @param columns       columns
     * @param columnsAs     columns as
     * @param selection     selection
     * @param selectionArgs selection arguments
     * @param groupBy       group by
     * @param having        having
     * @param orderBy       order by
     * @return cursor
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String)
     */
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy);

    /**
     * Query the table
     *
     * @param table         table name
     * @param columns       columns
     * @param selection     selection
     * @param selectionArgs selection arguments
     * @param groupBy       group by
     * @param having        having
     * @param orderBy       order by
     * @param limit         limit
     * @return cursor
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit);

    /**
     * Query the table
     *
     * @param table         table name
     * @param columns       columns
     * @param columnsAs     columns as
     * @param selection     selection
     * @param selectionArgs selection arguments
     * @param groupBy       group by
     * @param having        having
     * @param orderBy       order by
     * @param limit         limit
     * @return cursor
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
     */
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit);

    /**
     * Query the table
     *
     * @param distinct      distinct flag
     * @param table         table name
     * @param columns       columns
     * @param selection     selection
     * @param selectionArgs selection arguments
     * @param groupBy       group by
     * @param having        having
     * @param orderBy       order by
     * @param limit         limit
     * @return cursor
     * @see SQLiteDatabase#query(boolean, String, String[], String, String[], String, String, String, String)
     * @since 3.5.1
     */
    public Cursor query(boolean distinct, String table, String[] columns,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit);

    /**
     * Update the table
     *
     * @param table       table name
     * @param values      content values
     * @param whereClause where clause
     * @param whereArgs   where arguments
     * @return updated rows
     * @see SQLiteDatabase#update(String, ContentValues, String, String[])
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs);

    /**
     * Insert into a table
     *
     * @param table          table name
     * @param nullColumnHack null column hack
     * @param values         content values
     * @return row id
     * @see SQLiteDatabase#insertOrThrow(String, String, ContentValues)
     */
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException;

    /**
     * Insert into a table
     *
     * @param table          table name
     * @param nullColumnHack null column hack
     * @param values         content values
     * @return row id
     * @see SQLiteDatabase#insert(String, String, ContentValues)
     */
    public long insert(String table, String nullColumnHack, ContentValues values);

}
