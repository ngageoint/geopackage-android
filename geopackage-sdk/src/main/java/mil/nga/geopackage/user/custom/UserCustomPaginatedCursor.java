package mil.nga.geopackage.user.custom;

import mil.nga.geopackage.db.Pagination;
import mil.nga.geopackage.user.UserPaginatedCursor;

/**
 * User Custom Paginated Cursor for iterating and querying through user customs
 * in chunks
 *
 * @author osbornb
 * @since 6.2.0
 */
public class UserCustomPaginatedCursor extends
        UserPaginatedCursor<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomCursor> {

    /**
     * Determine if the cursor is paginated
     *
     * @param cursor user custom cursor
     * @return true if paginated
     */
    public static boolean isPaginated(UserCustomCursor cursor) {
        return getPagination(cursor) != null;
    }

    /**
     * Get the pagination offset and limit
     *
     * @param cursor user custom cursor
     * @return pagination or null if not paginated
     */
    public static Pagination getPagination(UserCustomCursor cursor) {
        return Pagination.find(cursor.getSql());
    }

    /**
     * Create a paginated cursor
     *
     * @param dao    user custom dao
     * @param cursor user custom cursor
     * @return user custom paginated cursor
     */
    public static UserCustomPaginatedCursor create(UserCustomDao dao,
                                                   UserCustomCursor cursor) {
        return new UserCustomPaginatedCursor(dao, cursor);
    }

    /**
     * Constructor
     *
     * @param dao    user custom dao
     * @param cursor user custom cursor
     */
    public UserCustomPaginatedCursor(UserCustomDao dao,
                                     UserCustomCursor cursor) {
        super(dao, cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomDao getDao() {
        return (UserCustomDao) super.getDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomCursor getResults() {
        return (UserCustomCursor) super.getResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomCursor getCursor() {
        return getResults();
    }

}
