package mil.nga.geopackage.db;

import android.database.Cursor;

import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureTable;

/**
 * Feature Indexer Id cursor to filter on matching queried ids
 *
 * @author osbornb
 * @since 3.4.0
 */
public class FeatureIndexerIdCursor extends FeatureCursor {

    /**
     * Feature indexer id query
     */
    private final FeatureIndexerIdQuery idQuery;

    /**
     * Constructor
     *
     * @param cursor  feature cursor
     * @param idQuery id query
     */
    public FeatureIndexerIdCursor(FeatureCursor cursor, FeatureIndexerIdQuery idQuery) {
        this(cursor.getTable(), cursor.getWrappedCursor(), idQuery);
    }

    /**
     * Constructor
     *
     * @param columns columns
     * @param cursor  feature cursor
     * @param idQuery id query
     * @since 3.5.0
     */
    public FeatureIndexerIdCursor(String[] columns, FeatureCursor cursor, FeatureIndexerIdQuery idQuery) {
        this(cursor.getTable(), columns, cursor.getWrappedCursor(), idQuery);
    }

    /**
     * Constructor
     *
     * @param table   feature table
     * @param cursor  cursor
     * @param idQuery id query
     */
    public FeatureIndexerIdCursor(FeatureTable table, Cursor cursor, FeatureIndexerIdQuery idQuery) {
        super(table, cursor);
        this.idQuery = idQuery;
    }

    /**
     * Constructor
     *
     * @param table   feature table
     * @param columns columns
     * @param cursor  cursor
     * @param idQuery id query
     * @since 3.5.0
     */
    public FeatureIndexerIdCursor(FeatureTable table, String[] columns, Cursor cursor, FeatureIndexerIdQuery idQuery) {
        super(table, columns, cursor);
        this.idQuery = idQuery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToNext() {
        boolean hasNext = super.moveToNext();
        while (hasNext) {
            if (idQuery.hasId(getId())) {
                break;
            }
            hasNext = super.moveToNext();
        }
        return hasNext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        // Not exact, but best option without iterating through the features
        return Math.min(super.getCount(), idQuery.getCount());
    }

}
