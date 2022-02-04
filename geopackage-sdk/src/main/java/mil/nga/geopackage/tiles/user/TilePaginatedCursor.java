package mil.nga.geopackage.tiles.user;

import mil.nga.geopackage.db.Pagination;
import mil.nga.geopackage.user.UserPaginatedCursor;

/**
 * Tile Paginated Cursor for iterating and querying through tiles in chunks
 *
 * @author osbornb
 * @since 6.2.0
 */
public class TilePaginatedCursor extends
        UserPaginatedCursor<TileColumn, TileTable, TileRow, TileCursor> {

    /**
     * Determine if the cursor is paginated
     *
     * @param cursor tile cursor
     * @return true if paginated
     */
    public static boolean isPaginated(TileCursor cursor) {
        return getPagination(cursor) != null;
    }

    /**
     * Get the pagination offset and limit
     *
     * @param cursor tile cursor
     * @return pagination or null if not paginated
     */
    public static Pagination getPagination(TileCursor cursor) {
        return Pagination.find(cursor.getSql());
    }

    /**
     * Create a paginated cursor
     *
     * @param dao    tile dao
     * @param cursor tile cursor
     * @return tile paginated cursor
     */
    public static TilePaginatedCursor create(TileDao dao,
                                             TileCursor cursor) {
        return new TilePaginatedCursor(dao, cursor);
    }

    /**
     * Constructor
     *
     * @param dao    tile dao
     * @param cursor tile cursor
     */
    public TilePaginatedCursor(TileDao dao, TileCursor cursor) {
        super(dao, cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TileDao getDao() {
        return (TileDao) super.getDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TileCursor getResults() {
        return (TileCursor) super.getResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TileCursor getCursor() {
        return getResults();
    }

}
