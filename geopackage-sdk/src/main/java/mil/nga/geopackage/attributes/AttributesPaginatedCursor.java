package mil.nga.geopackage.attributes;

import mil.nga.geopackage.db.Pagination;
import mil.nga.geopackage.user.UserPaginatedCursor;

/**
 * Attributes Paginated Cursor for iterating and querying through attributes in
 * chunks
 *
 * @author osbornb
 * @since 6.2.0
 */
public class AttributesPaginatedCursor extends
        UserPaginatedCursor<AttributesColumn, AttributesTable, AttributesRow, AttributesCursor> {

    /**
     * Determine if the cursor is paginated
     *
     * @param cursor attributes cursor
     * @return true if paginated
     */
    public static boolean isPaginated(AttributesCursor cursor) {
        return getPagination(cursor) != null;
    }

    /**
     * Get the pagination offset and limit
     *
     * @param cursor attributes cursor
     * @return pagination or null if not paginated
     */
    public static Pagination getPagination(AttributesCursor cursor) {
        return Pagination.find(cursor.getSql());
    }

    /**
     * Create a paginated cursor
     *
     * @param dao    attributes dao
     * @param cursor attributes cursor
     * @return attributes paginated cursor
     */
    public static AttributesPaginatedCursor create(AttributesDao dao,
                                                   AttributesCursor cursor) {
        return new AttributesPaginatedCursor(dao, cursor);
    }

    /**
     * Constructor
     *
     * @param dao    attributes dao
     * @param cursor attributes cursor
     */
    public AttributesPaginatedCursor(AttributesDao dao,
                                     AttributesCursor cursor) {
        super(dao, cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesDao getDao() {
        return (AttributesDao) super.getDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesCursor getResults() {
        return (AttributesCursor) super.getResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesCursor getCursor() {
        return getResults();
    }

}
