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
     * Id only results
     */
    private boolean idOnly = false;

    /**
     * Constructor
     *
     * @param featureIndexer   feature indexer
     * @param geometryMetadata geometry metadata
     */
    public FeatureIndexMetadataResults(FeatureIndexer featureIndexer, Cursor geometryMetadata) {
        this.featureIndexer = featureIndexer;
        this.geometryMetadata = geometryMetadata;
        this.idOnly = geometryMetadata.getColumnCount() == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<FeatureRow> iterator() {
        return new Iterator<FeatureRow>() {

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
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return geometryMetadata.getCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        geometryMetadata.close();
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
                        return !geometryMetadata.isLast();
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public Long next() {
                        geometryMetadata.moveToNext();
                        long id;
                        if (idOnly) {
                            id = geometryMetadata.getLong(0);
                        } else {
                            id = featureIndexer.getGeometryId(geometryMetadata);
                        }
                        return id;
                    }

                };
            }
        };
    }

}
