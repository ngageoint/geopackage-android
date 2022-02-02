package mil.nga.geopackage.user;

/**
 * User Paginated Cursor for iterating and querying through chunks
 *
 * @param <TColumn> column type
 * @param <TTable>  table type
 * @param <TRow>    row type
 * @param <TResult> result type
 * @author osbornb
 * @since 6.1.4
 */
public abstract class UserPaginatedCursor<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserCursor<TColumn, TTable, TRow>>
        extends UserCorePaginatedResults<TColumn, TTable, TRow, TResult> {

    /**
     * Constructor
     *
     * @param dao     user dao
     * @param results user cursor
     */
    protected UserPaginatedCursor(UserDao<TColumn, TTable, TRow, TResult> dao,
                                  UserCursor<TColumn, TTable, TRow> results) {
        super(dao, results);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDao<TColumn, TTable, TRow, TResult> getDao() {
        return (UserDao<TColumn, TTable, TRow, TResult>) super.getDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCursor<TColumn, TTable, TRow> getResults() {
        return (UserCursor<TColumn, TTable, TRow>) super.getResults();
    }

    /**
     * Get the current paginated cursor
     *
     * @return current cursor
     */
    public UserCursor<TColumn, TTable, TRow> getCursor() {
        return getResults();
    }

}
