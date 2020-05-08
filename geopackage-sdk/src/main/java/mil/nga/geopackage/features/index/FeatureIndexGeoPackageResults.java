package mil.nga.geopackage.features.index;

import com.j256.ormlite.dao.CloseableIterator;

import java.util.Iterator;

import mil.nga.geopackage.extension.nga.index.FeatureTableIndex;
import mil.nga.geopackage.extension.nga.index.GeometryIndex;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Iterable Feature Index Results to iterate on feature rows
 * retrieved from GeoPackage index extension results
 *
 * @author osbornb
 * @since 1.1.0
 */
public class FeatureIndexGeoPackageResults implements FeatureIndexResults {

    /**
     * Feature Table Index, for indexing within a GeoPackage extension
     */
    private final FeatureTableIndex featureTableIndex;

    /**
     * Total count of the results
     */
    private final long count;

    /**
     * Iterator of Geometry Index results
     */
    private final CloseableIterator<GeometryIndex> geometryIndices;

    /**
     * Constructor
     *
     * @param featureTableIndex feature table index
     * @param count             count
     * @param geometryIndices   geometry indices
     */
    public FeatureIndexGeoPackageResults(FeatureTableIndex featureTableIndex, long count, CloseableIterator<GeometryIndex> geometryIndices) {
        this.featureTableIndex = featureTableIndex;
        this.count = count;
        this.geometryIndices = geometryIndices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<FeatureRow> iterator() {
        return new Iterator<FeatureRow>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return geometryIndices.hasNext();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public FeatureRow next() {
                GeometryIndex geometryIndex = geometryIndices.next();
                FeatureRow featureRow = featureTableIndex.getFeatureRow(geometryIndex);
                return featureRow;
            }
        };
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
        geometryIndices.closeQuietly();
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

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean hasNext() {
                        return geometryIndices.hasNext();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public Long next() {
                        return geometryIndices.next().getGeomId();
                    }

                };
            }
        };
    }

}
