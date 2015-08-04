package mil.nga.giat.geopackage.user;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import mil.nga.giat.geopackage.db.GeoPackageConnection;

/**
 * GeoPackage Connection used to define common functionality within different
 * connection types. Wraps Cursor results.
 *
 * @param <TColumn>
 * @param <TTable>
 * @param <TRow>
 * @param <TResult>
 * @author osbornb
 */
public abstract class UserWrapperConnection<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserCursor<TColumn, TTable, TRow>>
        extends UserCoreConnection<TColumn, TTable, TRow, TResult> {

    /**
     * Database connection
     */
    private final SQLiteDatabase database;

    /**
     * Constructor
     *
     * @param database
     */
    protected UserWrapperConnection(GeoPackageConnection database) {
        this.database = database.getDb();
    }

    /**
     * Wrap the cursor in a result
     *
     * @param cursor
     * @return
     */
    protected abstract TResult wrapCursor(Cursor cursor);

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult rawQuery(String sql, String[] selectionArgs) {
        Cursor cursor = database.rawQuery(sql, selectionArgs);
        return wrapCursor(cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy) {
        Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return wrapCursor(cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy, String limit) {
        Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        return wrapCursor(cursor);
    }

}
