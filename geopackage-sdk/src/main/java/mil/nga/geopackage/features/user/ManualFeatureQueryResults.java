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
     * Feature columns
     */
    private final String[] columns;

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
        this(featureDao, featureDao.getColumnNames(), featureIds);
    }

    /**
     * Constructor
     *
     * @param featureDao feature DAO
     * @param columns    columns
     * @param featureIds feature ids
     * @since 3.5.0
     */
    public ManualFeatureQueryResults(FeatureDao featureDao, String[] columns,
                                     List<Long> featureIds) {
        this.featureDao = featureDao;
        this.columns = columns;
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
     * Get the feature columns
     *
     * @return columns
     * @since 3.5.0
     */
    public String[] getColumns() {
        return columns;
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
        return new Iterator<FeatureRow>() {

            int index = 0;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return index < featureIds.size();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public FeatureRow next() {
                return featureDao.queryForIdRow(columns,
                        featureIds.get(index++));
            }
        };
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
                return featureIds.iterator();
            }
        };
    }

}
