package mil.nga.geopackage.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import mil.nga.geopackage.factory.GeoPackageCursorFactory;

/**
 * GeoPackage database wrapper around SQLiteDatabase to quote table and column names
 *
 * @author osbornb
 * @since 1.3.1
 */
public class GeoPackageDatabase implements GeoPackageSQLiteDatabase {

    /**
     * Database connection
     */
    private final AndroidSQLiteDatabase db;

    /**
     * SQLite Android Bindings connection
     */
    private final AndroidBindingsSQLiteDatabase bindingsDb;

    /**
     * Use SQLite Android Bindings flag
     */
    private boolean useBindings = false;

    /**
     * Cursor factory
     */
    private final GeoPackageCursorFactory cursorFactory;

    /**
     * Database Connection Writable connection flag
     */
    private final boolean writable;

    /**
     * SQLite Android Bindings Connection Writable connection flag
     */
    private boolean bindingsWritable = false;

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
     * @since 3.4.0
     */
    public GeoPackageDatabase(SQLiteDatabase db, GeoPackageCursorFactory cursorFactory) {
        this(db, true, cursorFactory);
    }

    /**
     * Constructor
     *
     * @param db            database
     * @param writable      writable flag
     * @param cursorFactory cursor factory
     * @since 3.4.0
     */
    public GeoPackageDatabase(SQLiteDatabase db, boolean writable, GeoPackageCursorFactory cursorFactory) {
        this.db = new AndroidSQLiteDatabase(db);
        this.bindingsDb = new AndroidBindingsSQLiteDatabase();
        this.writable = writable;
        this.bindingsWritable = writable;
        this.cursorFactory = cursorFactory;
    }

    /**
     * Copy constructor
     *
     * @param database GeoPackage database
     * @since 3.4.0
     */
    public GeoPackageDatabase(GeoPackageDatabase database) {
        this.db = database.db;
        this.bindingsDb = database.bindingsDb;
        this.writable = database.writable;
        this.bindingsWritable = database.bindingsWritable;
        this.cursorFactory = database.cursorFactory;
        this.useBindings = database.useBindings;
    }

    /**
     * Get the active connection
     *
     * @return active connection
     * @since 3.4.0
     */
    public GeoPackageSQLiteDatabase getActive() {
        GeoPackageSQLiteDatabase active = null;
        if (useBindings) {
            active = getAndroidBindingsSQLiteDatabase();
        } else {
            active = getAndroidSQLiteDatabase();
        }
        return active;
    }

    /**
     * Get the Android SQLite Database connection
     *
     * @return connection
     * @since 3.4.0
     */
    public AndroidSQLiteDatabase getAndroidSQLiteDatabase() {
        return db;
    }

    /**
     * Get the Android Bindings SQLite Database connection
     *
     * @return connection
     * @since 3.4.0
     */
    public AndroidBindingsSQLiteDatabase getAndroidBindingsSQLiteDatabase() {
        if (bindingsDb.getDb() == null) {
            synchronized (db) {
                if (bindingsDb.getDb() == null) {

                    System.loadLibrary("sqliteX");

                    org.sqlite.database.sqlite.SQLiteDatabase.CursorFactory bindingsCursorFactory = null;
                    if (cursorFactory != null) {
                        bindingsCursorFactory = cursorFactory.getBindingsCursorFactory();
                    }

                    org.sqlite.database.sqlite.SQLiteDatabase sqLiteDatabase = null;
                    String database = getDb().getPath();
                    if (bindingsWritable) {
                        try {
                            sqLiteDatabase = org.sqlite.database.sqlite.SQLiteDatabase.openDatabase(
                                    database,
                                    bindingsCursorFactory,
                                    org.sqlite.database.sqlite.SQLiteDatabase.OPEN_READWRITE | org.sqlite.database.sqlite.SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                        } catch (Exception e) {
                            Log.e(GeoPackageDatabase.class.getSimpleName(), "Failed to open database as writable: " + database, e);
                        }
                    }

                    if (sqLiteDatabase == null) {
                        sqLiteDatabase = org.sqlite.database.sqlite.SQLiteDatabase.openDatabase(
                                database,
                                bindingsCursorFactory,
                                org.sqlite.database.sqlite.SQLiteDatabase.OPEN_READONLY | org.sqlite.database.sqlite.SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                        bindingsWritable = false;
                    }

                    bindingsDb.setDb(sqLiteDatabase);
                }
            }
        }
        return bindingsDb;
    }

    /**
     * Get the SQLite database connection
     *
     * @return connection
     */
    public SQLiteDatabase getDb() {
        return getAndroidSQLiteDatabase().getDb();
    }

    /**
     * Get the SQLite bindings database connection
     *
     * @return connection
     * @since 3.4.0
     */
    public org.sqlite.database.sqlite.SQLiteDatabase getBindingsDb() {
        return getAndroidBindingsSQLiteDatabase().getDb();
    }

    /**
     * Is SQLite Android Bindings connection enabled
     *
     * @return true if using bindings
     * @since 3.4.0
     */
    public boolean isUseBindings() {
        return useBindings;
    }

    /**
     * Is the SQLite database connection writable
     *
     * @return true if writable
     * @since 3.4.0
     */
    public boolean isWritable() {
        return writable;
    }

    /**
     * Is the SQLite bindings database connection writable
     *
     * @return true if writable
     * @since 3.4.0
     */
    public boolean isBindingsWritable() {
        return bindingsWritable;
    }

    /**
     * Set the active SQLite connection as the bindings or standard
     *
     * @param useBindings true to use bindings connection, false for standard
     * @return previous bindings value
     * @since 3.4.0
     */
    public boolean setUseBindings(boolean useBindings) {
        boolean previous = this.useBindings;
        this.useBindings = useBindings;
        return previous;
    }

    /**
     * Copy the database, maintaining the same connections but with the ability to change the active used connection
     *
     * @return database
     * @since 3.4.0
     */
    public GeoPackageDatabase copy() {
        return new GeoPackageDatabase(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execSQL(String sql) throws SQLException {
        getActive().execSQL(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginTransaction() {
        getActive().beginTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTransaction() {
        getActive().endTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTransaction(boolean successful) {
        getActive().endTransaction(successful);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endAndBeginTransaction() {
        getActive().endAndBeginTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean inTransaction() {
        return getActive().inTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return getActive().delete(table, whereClause, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return getActive().rawQuery(sql, selectionArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        db.close();
        bindingsDb.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        return getActive().query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        return getActive().query(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        return getActive().query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        return getActive().query(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return getActive().update(table, values, whereClause, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return getActive().insertOrThrow(table, nullColumnHack, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return getActive().insert(table, nullColumnHack, values);
    }

}
