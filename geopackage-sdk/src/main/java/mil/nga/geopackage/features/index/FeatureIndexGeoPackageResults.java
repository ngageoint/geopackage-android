package mil.nga.geopackage.features.index;

import com.j256.ormlite.dao.CloseableIterator;

import java.util.Iterator;

import mil.nga.geopackage.extension.index.FeatureTableIndex;
import mil.nga.geopackage.extension.index.GeometryIndex;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Iterable Feature Index Results to iterate on feature rows
 * retrieved from GeoPackage index extension results
 *
 * @author osbornb
 * @since 1.1.0
 */
class FeatureIndexGeoPackageResults implements FeatureIndexResults {

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
     * @param featureTableIndex
     * @param count
     * @param geometryIndices
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
        Iterator<FeatureRow> iterator = new Iterator<FeatureRow>() {

            @Override
            public boolean hasNext() {
                return geometryIndices.hasNext();
            }

            @Override
            public FeatureRow next() {
                GeometryIndex geometryIndex = geometryIndices.next();
                FeatureRow featureRow = featureTableIndex.getFeatureRow(geometryIndex);
                return featureRow;
            }

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
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        geometryIndices.closeQuietly();
    }

}
