package mil.nga.geopackage.extension;

import java.util.Iterator;

import mil.nga.geopackage.extension.RTreeIndexTableDao;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.user.custom.UserCustomCursor;

/**
 * Iterable Feature Index Results to iterate on feature rows retrieved from
 * RTree results
 *
 * @author osbornb
 * @since 3.0.3
 */
public class FeatureIndexRTreeResults implements FeatureIndexResults {

    /**
     * RTree Index Table DAO
     */
    private final RTreeIndexTableDao dao;

    /**
     * Result Cursor
     */
    private final UserCustomCursor cursor;

    /**
     * Constructor
     *
     * @param dao    RTree Index Table DAO
     * @param cursor result cursor
     */
    public FeatureIndexRTreeResults(RTreeIndexTableDao dao,
                                    UserCustomCursor cursor) {
        this.dao = dao;
        this.cursor = cursor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<FeatureRow> iterator() {
        Iterator<FeatureRow> iterator = new Iterator<FeatureRow>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return cursor.moveToNext();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public FeatureRow next() {
                return dao.getFeatureRow(cursor);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return iterator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return cursor.getCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        cursor.close();
    }

}
