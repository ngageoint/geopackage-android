package mil.nga.geopackage.features.index;

import java.util.Iterator;

import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Iterable Feature Index Results to iterate on feature results from a feature
 * DAO
 *
 * @author osbornb
 * @since 3.1.0
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
        return cursor.iterator();
    }

    /**
     * Get the cursor
     *
     * @return feature cursor
     * @since 6.2.0
     */
    public FeatureCursor getCursor() {
        return cursor;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Long> ids() {
        return cursor.ids();
    }

}
