package mil.nga.geopackage.features.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Iterable Feature Index Results to iterate on feature rows from a combination of multiple
 * Feature Index Results
 *
 * @author osbornb
 * @since 2.0.0
 */
public class MultipleFeatureIndexResults implements FeatureIndexResults {

    /**
     * List of multiple Feature Index Results
     */
    private final List<FeatureIndexResults> results = new ArrayList<>();

    /**
     * Total feature row result count
     */
    private final int count;

    /**
     * Constructor
     *
     * @param results multiple results
     */
    public MultipleFeatureIndexResults(FeatureIndexResults... results) {
        this(Arrays.asList(results));
    }

    /**
     * Constructor
     *
     * @param results multiple results
     */
    public MultipleFeatureIndexResults(Collection<FeatureIndexResults> results) {
        this.results.addAll(results);
        int totalCount = 0;
        for (FeatureIndexResults result : results) {
            totalCount += result.count();
        }
        count = totalCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        for (FeatureIndexResults result : results) {
            result.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<FeatureRow> iterator() {

        return new Iterator<FeatureRow>() {

            int index = -1;
            private Iterator<FeatureRow> currentResults = null;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                boolean hasNext = false;

                if (currentResults != null) {
                    hasNext = currentResults.hasNext();
                }

                if (!hasNext) {

                    while (!hasNext && ++index < results.size()) {

                        // Get an iterator from the next feature index results
                        currentResults = results.get(index).iterator();
                        hasNext = currentResults.hasNext();

                    }

                }

                return hasNext;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public FeatureRow next() {
                FeatureRow row = null;
                if (currentResults != null) {
                    row = currentResults.next();
                }
                return row;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Long> ids() {
        return new Iterable<Long>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Iterator<Long> iterator() {
                return new Iterator<Long>() {

                    int index = -1;
                    private Iterator<Long> currentResults = null;

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean hasNext() {
                        boolean hasNext = false;

                        if (currentResults != null) {
                            hasNext = currentResults.hasNext();
                        }

                        if (!hasNext) {

                            while (!hasNext && ++index < results.size()) {

                                // Get an iterator from the next feature index results
                                currentResults = results.get(index).ids().iterator();
                                hasNext = currentResults.hasNext();

                            }

                        }

                        return hasNext;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public Long next() {
                        Long id = null;
                        if (currentResults != null) {
                            id = currentResults.next();
                        }
                        return id;
                    }

                };
            }
        };
    }

}
