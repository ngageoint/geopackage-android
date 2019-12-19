package mil.nga.geopackage.user.custom;

import android.database.Cursor;

import java.util.List;

import mil.nga.geopackage.user.UserCursor;
import mil.nga.geopackage.user.UserDao;
import mil.nga.geopackage.user.UserInvalidCursor;

/**
 * User Custom Cursor to wrap a database Cursor for tile queries
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomCursor extends
        UserCursor<UserCustomColumn, UserCustomTable, UserCustomRow> {

    /**
     * Constructor
     *
     * @param table  user custom table
     * @param cursor cursor
     */
    public UserCustomCursor(UserCustomTable table, Cursor cursor) {
        this(table, null, cursor);
    }

    /**
     * Constructor
     *
     * @param table   user custom table
     * @param columns columns
     * @param cursor  cursor
     * @since 3.5.0
     */
    public UserCustomCursor(UserCustomTable table, String[] columns,
                            Cursor cursor) {
        super(table, columns, cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomRow getRow(int[] columnTypes, Object[] values) {
        return new UserCustomRow(getTable(), getColumns(), columnTypes, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomColumns getColumns() {
        return (UserCustomColumns) super.getColumns();
    }

    /**
     * Enable requery attempt of invalid rows after iterating through original query rows.
     * Only supported for {@link #moveToNext()} and {@link #getRow()} usage.
     *
     * @param dao data access object used to perform requery
     */
    public void enableInvalidRequery(UserCustomDao dao) {
        super.enableInvalidRequery(dao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserInvalidCursor<UserCustomColumn, UserCustomTable, UserCustomRow, ? extends UserCursor<UserCustomColumn, UserCustomTable, UserCustomRow>, ? extends UserDao<UserCustomColumn, UserCustomTable, UserCustomRow, ? extends UserCursor<UserCustomColumn, UserCustomTable, UserCustomRow>>> createInvalidCursor(UserDao dao, UserCursor cursor, List<Integer> invalidPositions, List<UserCustomColumn> blobColumns) {
        return new UserCustomInvalidCursor((UserCustomDao) dao, (UserCustomCursor) cursor, invalidPositions, blobColumns);
    }

}
