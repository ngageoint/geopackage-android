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
     * Active connection
     */
    private GeoPackageSQLiteDatabase active;

    /**
     * Cursor factory
     */
    private final GeoPackageCursorFactory cursorFactory;

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
        this.db = new AndroidSQLiteDatabase(db);
        this.bindingsDb = new AndroidBindingsSQLiteDatabase();
        this.cursorFactory = cursorFactory;
        active = this.db;
    }

    /**
     * Copy constructor
     *
     * @param database GeoPackage database
     * @since 3.3.1
     */
    public GeoPackageDatabase(GeoPackageDatabase database) {
        this.db = database.db;
        this.bindingsDb = database.bindingsDb;
        this.cursorFactory = database.cursorFactory;
        this.active = database.active;
    }

    /**
     * Get the active connection
     *
     * @return active connection
     * @since 3.3.1
     */
    public GeoPackageSQLiteDatabase getActive() {
        return active;
    }

    /**
     * Get the Android SQLite Database connection
     *
     * @return connection
     * @since 3.3.1
     */
    public AndroidSQLiteDatabase getAndroidSQLiteDatabase() {
        return db;
    }

    /**
     * Get the Android Bindings SQLite Database connection
     *
     * @return connection
     * @since 3.3.1
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
                    org.sqlite.database.sqlite.SQLiteDatabase sqLiteDatabase = org.sqlite.database.sqlite.SQLiteDatabase.openDatabase(
                            getDb().getPath(),
                            bindingsCursorFactory,
                            org.sqlite.database.sqlite.SQLiteDatabase.OPEN_READWRITE);
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
     * @since 3.3.1
     */
    public org.sqlite.database.sqlite.SQLiteDatabase getBindingsDb() {
        return getAndroidBindingsSQLiteDatabase().getDb();
    }

    /**
     * Is SQLite Android Bindings connection enabled
     *
     * @return true if using bindings
     * @since 3.3.1
     */
    public boolean isUseBindings() {
        return active == bindingsDb;
    }

    /**
     * Set the active SQLite connection as the bindings or standard
     *
     * @param useBindings true to use bindings connection, false for standard
     * @return previous bindings value
     * @since 3.3.1
     */
    public boolean setUseBindings(boolean useBindings) {
        boolean previous = isUseBindings();
        if (useBindings) {
            active = getAndroidBindingsSQLiteDatabase();
        } else {
            active = getAndroidSQLiteDatabase();
        }
        return previous;
    }

    /**
     * Copy the database, maintaining the same connections but with the ability to change the active used connection
     *
     * @return database
     * @since 3.3.1
     */
    public GeoPackageDatabase copy() {
        return new GeoPackageDatabase(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execSQL(String sql) throws SQLException {
        active.execSQL(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginTransaction() {
        active.beginTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTransaction() {
        active.endTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTransaction(boolean successful) {
        active.endTransaction(successful);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endAndBeginTransaction() {
        active.endAndBeginTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean inTransaction() {
        return active.inTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return active.delete(table, whereClause, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return active.rawQuery(sql, selectionArgs);
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
        return active.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        return active.query(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        return active.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        return active.query(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return active.update(table, values, whereClause, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return active.insertOrThrow(table, nullColumnHack, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return active.insert(table, nullColumnHack, values);
    }

}
