package mil.nga.geopackage.extension.rtree;

import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * RTree Index Table Row containing the values from a single result set row
 *
 * @author osbornb
 * @since 3.1.0
 */
public class RTreeIndexTableRow extends UserCustomRow {

    /**
     * Constructor
     *
     * @param userCustomRow user custom row
     */
    RTreeIndexTableRow(UserCustomRow userCustomRow) {
        super(userCustomRow.getTable(), userCustomRow.getColumns(), userCustomRow.getRowColumnTypes(),
                userCustomRow.getValues());
    }

    /**
     * Get the ID
     *
     * @return ID
     */
    public long getId() {
        return ((Number) getValue(getColumnIndex(RTreeIndexExtension.COLUMN_ID)))
                .longValue();
    }

    /**
     * Get the min x
     *
     * @return min x
     */
    public double getMinX() {
        return ((Number) getValue(getColumnIndex(RTreeIndexExtension.COLUMN_MIN_X)))
                .doubleValue();
    }

    /**
     * Get the max x
     *
     * @return max x
     */
    public double getMaxX() {
        return ((Number) getValue(getColumnIndex(RTreeIndexExtension.COLUMN_MAX_X)))
                .doubleValue();
    }

    /**
     * Get the min y
     *
     * @return min y
     */
    public double getMinY() {
        return ((Number) getValue(getColumnIndex(RTreeIndexExtension.COLUMN_MIN_Y)))
                .doubleValue();
    }

    /**
     * Get the max y
     *
     * @return max y
     */
    public double getMaxY() {
        return ((Number) getValue(getColumnIndex(RTreeIndexExtension.COLUMN_MAX_Y)))
                .doubleValue();
    }

}
