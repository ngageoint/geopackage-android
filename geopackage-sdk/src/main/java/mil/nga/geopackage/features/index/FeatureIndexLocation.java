package mil.nga.geopackage.features.index;

import java.util.Iterator;

/**
 * Feature Index Location to iterate over indexed feature index types
 *
 * @author osbornb
 * @since 3.4.0
 */
public class FeatureIndexLocation implements Iterable<FeatureIndexType> {

    /**
     * Feature Index Manager
     */
    private final FeatureIndexManager manager;

    /**
     * Constructor
     *
     * @param manager feature index manager
     */
    public FeatureIndexLocation(FeatureIndexManager manager) {
        this.manager = manager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<FeatureIndexType> iterator() {
        return new Iterator<FeatureIndexType>() {

            /**
             * Feature index type query order
             */
            private Iterator<FeatureIndexType> order = manager.getIndexLocationQueryOrder().iterator();

            /**
             * Current feature index type
             */
            private FeatureIndexType type;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {

                if (type == null) {
                    // Find the next indexed type
                    while (order.hasNext()) {
                        FeatureIndexType nextType = order.next();
                        if (manager.isIndexed(nextType)) {
                            type = nextType;
                            break;
                        }
                    }
                }

                return type != null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public FeatureIndexType next() {
                FeatureIndexType nextType = type;
                type = null;
                return nextType;
            }

        };
    }

}
