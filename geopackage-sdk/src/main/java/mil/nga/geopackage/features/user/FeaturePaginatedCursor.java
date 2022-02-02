package mil.nga.geopackage.features.user;

import mil.nga.geopackage.db.Pagination;
import mil.nga.geopackage.user.UserPaginatedCursor;

/**
 * Feature Paginated Cursor for iterating and querying through features in
 * chunks
 *
 * @author osbornb
 * @since 6.1.4
 */
public class FeaturePaginatedCursor extends
        UserPaginatedCursor<FeatureColumn, FeatureTable, FeatureRow, FeatureCursor> {

    /**
     * Determine if the cursor is paginated
     *
     * @param cursor feature cursor
     * @return true if paginated
     */
    public static boolean isPaginated(FeatureCursor cursor) {
        return getPagination(cursor) != null;
    }

    /**
     * Get the pagination offset and limit
     *
     * @param cursor feature cursor
     * @return pagination or null if not paginated
     */
    public static Pagination getPagination(FeatureCursor cursor) {
        return Pagination.find(cursor.getSql());
    }

    /**
     * Create a paginated cursor
     *
     * @param dao    feature dao
     * @param cursor feature cursor
     * @return feature paginated cursor
     */
    public static FeaturePaginatedCursor create(FeatureDao dao,
                                                FeatureCursor cursor) {
        return new FeaturePaginatedCursor(dao, cursor);
    }

    /**
     * Constructor
     *
     * @param dao    feature dao
     * @param cursor feature cursor
     */
    public FeaturePaginatedCursor(FeatureDao dao, FeatureCursor cursor) {
        super(dao, cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureDao getDao() {
        return (FeatureDao) super.getDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureCursor getResults() {
        return (FeatureCursor) super.getResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureCursor getCursor() {
        return getResults();
    }

}
