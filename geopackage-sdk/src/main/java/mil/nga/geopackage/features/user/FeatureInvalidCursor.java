package mil.nga.geopackage.features.user;

import java.util.List;

import mil.nga.geopackage.user.UserInvalidCursor;

/**
 * Feature Invalid Cursor wrapper for feature requery to handle failed rows due to large blobs
 *
 * @author osbornb
 * @since 2.0.0
 */
public class FeatureInvalidCursor extends UserInvalidCursor<FeatureColumn, FeatureTable, FeatureRow, FeatureCursor, FeatureDao> {

    /**
     * Constructor
     *
     * @param dao              feature dao
     * @param cursor           feature cursor
     * @param invalidPositions invalid positions from a previous cursor
     * @param blobColumns      blob columns
     */
    public FeatureInvalidCursor(FeatureDao dao, FeatureCursor cursor, List<Integer> invalidPositions, List<FeatureColumn> blobColumns) {
        super(dao, cursor, invalidPositions, blobColumns);
    }

}
