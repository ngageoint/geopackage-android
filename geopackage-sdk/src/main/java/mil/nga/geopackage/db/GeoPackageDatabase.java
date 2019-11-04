package mil.nga.geopackage.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import mil.nga.geopackage.factory.GeoPackageCursorFactory;

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
     * SQLite Android Bindings connection
     */
    private org.sqlite.database.sqlite.SQLiteDatabase bindingsDb;

    /**
     * Cursor factory
     */
    private GeoPackageCursorFactory cursorFactory;

    /**
     * Constructor
     *
     * @param db database
     */
    public GeoPackageDatabase(SQLiteDatabase db) {
        this(db, null);
    }

    /**
     * Constructor
     *
     * @param db            database
     * @param cursorFactory cursor factory
     * @since 3.3.1
     */
    public GeoPackageDatabase(SQLiteDatabase db, GeoPackageCursorFactory cursorFactory) {
        this.db = db;
        this.cursorFactory = cursorFactory;
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
     * Open or get a connection using the SQLite Android Bindings connection
     *
     * @return bindings connection
     */
    public org.sqlite.database.sqlite.SQLiteDatabase openOrGetBindingsDb() {
        if (bindingsDb == null) {
            synchronized (db) {
                if (bindingsDb == null) {
                    System.loadLibrary("sqliteX");
                    org.sqlite.database.sqlite.SQLiteDatabase.CursorFactory bindingsCursorFactory = null;
                    if (cursorFactory != null) {
                        bindingsCursorFactory = cursorFactory.getBindingsCursorFactory();
                    }
                    bindingsDb = org.sqlite.database.sqlite.SQLiteDatabase.openDatabase(
                            db.getPath(),
                            bindingsCursorFactory,
                            org.sqlite.database.sqlite.SQLiteDatabase.OPEN_READWRITE);
                }
            }
        }
        return bindingsDb;
    }

    /**
     * @param sql sql command
     * @see SQLiteDatabase#execSQL(String)
     */
    public void execSQL(String sql) throws SQLException {
        db.execSQL(sql);
    }

    /**
     * Begin a transaction
     *
     * @since 3.3.0
     */
    public void beginTransaction() {
        db.beginTransaction();
    }

    /**
     * End a transaction as successful
     *
     * @since 3.3.0
     */
    public void endTransaction() {
        endTransaction(true);
    }

    /**
     * End a transaction
     *
     * @param successful true to commit, false to rollback
     * @since 3.3.0
     */
    public void endTransaction(boolean successful) {
        if (successful) {
            db.setTransactionSuccessful();
            db.endTransaction();
        } else if (db.inTransaction()) {
            db.endTransaction();
        }
    }

    /**
     * End a transaction as successful and begin a new transaction
     *
     * @since 3.3.0
     */
    public void endAndBeginTransaction() {
        endTransaction();
        beginTransaction();
    }

    /**
     * Determine if currently within a transaction
     *
     * @return true if in transaction
     * @since 3.3.0
     */
    public boolean inTransaction() {
        return db.inTransaction();
    }

    /**
     * @param table       table name
     * @param whereClause where clause
     * @param whereArgs   where arguments
     * @return deleted rows
     * @see SQLiteDatabase#delete(String, String, String[])
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        return db.delete(CoreSQLUtils.quoteWrap(table), whereClause, whereArgs);
    }

    /**
     * @param sql           sql command
     * @param selectionArgs selection arguments
     * @return cursor
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
        if (bindingsDb != null) {
            bindingsDb.close();
        }
    }

    /**
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
                        String orderBy) {
        return db.query(CoreSQLUtils.quoteWrap(table), CoreSQLUtils.quoteWrap(columns), selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
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
                        String orderBy, String limit) {
        return db.query(CoreSQLUtils.quoteWrap(table), CoreSQLUtils.quoteWrap(columns), selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
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
     * @param table       table name
     * @param values      content values
     * @param whereClause where clause
     * @param whereArgs   where arguments
     * @return updated rows
     * @see SQLiteDatabase#update(String, ContentValues, String, String[])
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return db.update(CoreSQLUtils.quoteWrap(table), SQLUtils.quoteWrap(values), whereClause, whereArgs);
    }

    /**
     * @param table          table name
     * @param nullColumnHack null column hack
     * @param values         content values
     * @return row id
     * @see SQLiteDatabase#insertOrThrow(String, String, ContentValues)
     */
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return db.insertOrThrow(CoreSQLUtils.quoteWrap(table), nullColumnHack, SQLUtils.quoteWrap(values));
    }

    /**
     * @param table          table name
     * @param nullColumnHack null column hack
     * @param values         content values
     * @return row id
     * @see SQLiteDatabase#insert(String, String, ContentValues)
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return db.insert(CoreSQLUtils.quoteWrap(table), nullColumnHack, SQLUtils.quoteWrap(values));
    }

}
