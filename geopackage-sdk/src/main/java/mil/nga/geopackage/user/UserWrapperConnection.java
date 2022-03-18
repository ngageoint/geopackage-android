package mil.nga.geopackage.user;

import android.database.Cursor;

import mil.nga.geopackage.db.GeoPackageConnection;

/**
 * GeoPackage Connection used to define common functionality within different
 * connection types. Wraps Cursor results.
 *
 * @param <TColumn> column type
 * @param <TTable>  table type
 * @param <TRow>    row type
 * @param <TResult> result type
 * @author osbornb
 * @deprecated use {@link UserDao} to query user tables
 */
public abstract class UserWrapperConnection<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserCursor<TColumn, TTable, TRow>>
        extends UserConnection<TColumn, TTable, TRow, TResult> {

    /**
     * Constructor
     *
     * @param database GeoPackage connection
     */
    protected UserWrapperConnection(GeoPackageConnection database) {
        super(database);
    }

    /**
     * Wrap the cursor in a result
     *
     * @param cursor cursor
     * @return result
     */
    protected abstract TResult wrapCursor(Cursor cursor);

    /**
     * {@inheritDoc}
     */
    @Override
    protected TResult convertCursor(Cursor cursor) {
        return wrapCursor(cursor);
    }

}
