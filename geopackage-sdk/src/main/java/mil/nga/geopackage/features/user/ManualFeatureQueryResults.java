package mil.nga.geopackage.features.user;

import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.features.index.FeatureIndexResults;

/**
 * Manual Feature Query Results which includes the ids used to read each row
 *
 * @author osbornb
 * @since 3.1.0
 */
public class ManualFeatureQueryResults implements FeatureIndexResults {

    /**
     * Feature DAO
     */
    private final FeatureDao featureDao;

    /**
     * Feature ids
     */
    private final List<Long> featureIds;

    /**
     * Constructor
     *
     * @param featureDao feature DAO
     * @param featureIds feature ids
     */
    public ManualFeatureQueryResults(FeatureDao featureDao,
                                     List<Long> featureIds) {
        this.featureDao = featureDao;
        this.featureIds = featureIds;
    }

    /**
     * Get the feature DAO
     *
     * @return feature DAO
     */
    public FeatureDao getFeatureDao() {
        return featureDao;
    }

    /**
     * Get the feature ids
     *
     * @return feature ids
     */
    public List<Long> getFeatureIds() {
        return featureIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<FeatureRow> iterator() {
        Iterator<FeatureRow> iterator = new Iterator<FeatureRow>() {

            int index = 0;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return index <= featureIds.size();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public FeatureRow next() {
                return featureDao.queryForIdRow(featureIds.get(index++));
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
        return featureIds.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

    }

}
