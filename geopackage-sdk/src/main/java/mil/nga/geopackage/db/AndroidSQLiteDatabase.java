package mil.nga.geopackage.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Android SQLiteDatabase
 *
 * @author osbornb
 * @since 3.4.0
 */
public class AndroidSQLiteDatabase implements GeoPackageSQLiteDatabase {

    /**
     * Database connection
     */
    private final SQLiteDatabase db;

    /**
     * Constructor
     *
     * @param db database
     */
    public AndroidSQLiteDatabase(SQLiteDatabase db) {
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
     * {@inheritDoc}
     */
    @Override
    public void execSQL(String sql) throws SQLException {
        db.execSQL(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginTransaction() {
        db.beginTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTransaction() {
        endTransaction(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTransaction(boolean successful) {
        if (successful) {
            db.setTransactionSuccessful();
            db.endTransaction();
        } else if (db.inTransaction()) {
            db.endTransaction();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endAndBeginTransaction() {
        endTransaction();
        beginTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean inTransaction() {
        return db.inTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return db.delete(CoreSQLUtils.quoteWrap(table), whereClause, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        db.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        return db.query(CoreSQLUtils.quoteWrap(table), CoreSQLUtils.quoteWrap(columns), selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        String[] wrappedColumns = CoreSQLUtils.quoteWrap(columns);
        String[] wrappedColumnsAs = CoreSQLUtils.buildColumnsAs(wrappedColumns, columnsAs);
        return db.query(CoreSQLUtils.quoteWrap(table), wrappedColumnsAs, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        return db.query(CoreSQLUtils.quoteWrap(table), CoreSQLUtils.quoteWrap(columns), selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(String table, String[] columns, String[] columnsAs, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        String[] wrappedColumns = CoreSQLUtils.quoteWrap(columns);
        String[] wrappedColumnsAs = CoreSQLUtils.buildColumnsAs(wrappedColumns, columnsAs);
        return db.query(CoreSQLUtils.quoteWrap(table), wrappedColumnsAs, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return db.update(CoreSQLUtils.quoteWrap(table), SQLUtils.quoteWrap(values), whereClause, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return db.insertOrThrow(CoreSQLUtils.quoteWrap(table), nullColumnHack, SQLUtils.quoteWrap(values));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return db.insert(CoreSQLUtils.quoteWrap(table), nullColumnHack, SQLUtils.quoteWrap(values));
    }

}
