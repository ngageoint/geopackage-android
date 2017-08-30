package mil.nga.geopackage.user;

import android.database.Cursor;

import mil.nga.geopackage.db.GeoPackageConnection;

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
        extends UserConnection<TColumn, TTable, TRow, TResult> {

    /**
     * Constructor
     *
     * @param database
     */
    protected UserWrapperConnection(GeoPackageConnection database) {
        super(database);
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
    protected TResult convertCursor(Cursor cursor) {
        return wrapCursor(cursor);
    }

}
