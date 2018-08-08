package mil.nga.geopackage.features.index;

import java.util.Iterator;

import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Iterable Feature Index Results to iterate on feature results from a feature
 * DAO
 *
 * @author osbornb
 * @since 3.0.3
 */
public class FeatureIndexFeatureResults implements FeatureIndexResults {

    /**
     * Cursor
     */
    private final FeatureCursor cursor;

    /**
     * Constructor
     *
     * @param cursor result cursor
     */
    public FeatureIndexFeatureResults(FeatureCursor cursor) {
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
                return cursor.getRow();
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
