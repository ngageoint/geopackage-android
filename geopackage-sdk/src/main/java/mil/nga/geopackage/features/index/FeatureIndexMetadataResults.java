package mil.nga.geopackage.features.index;

import android.database.Cursor;

import java.util.Iterator;

import mil.nga.geopackage.db.FeatureIndexer;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Iterable Feature Index Results to iterate on feature rows
 * retrieved from Metadata index results
 *
 * @author osbornb
 * @since 1.1.0
 */
public class FeatureIndexMetadataResults implements FeatureIndexResults {

    /**
     * Feature Indexer, for indexing within Android metadata
     */
    private final FeatureIndexer featureIndexer;

    /**
     * Cursor of Geometry Metadata results
     */
    private final Cursor geometryMetadata;

    /**
     * Constructor
     *
     * @param featureIndexer
     * @param geometryMetadata
     */
    public FeatureIndexMetadataResults(FeatureIndexer featureIndexer, Cursor geometryMetadata) {
        this.featureIndexer = featureIndexer;
        this.geometryMetadata = geometryMetadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<FeatureRow> iterator() {
        Iterator<FeatureRow> iterator = new Iterator<FeatureRow>() {

            @Override
            public boolean hasNext() {
                return !geometryMetadata.isLast();
            }

            @Override
            public FeatureRow next() {
                geometryMetadata.moveToNext();
                FeatureRow featureRow = featureIndexer.getFeatureRow(geometryMetadata);
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
    public long count(){
        return geometryMetadata.getCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        geometryMetadata.close();
    }

}
