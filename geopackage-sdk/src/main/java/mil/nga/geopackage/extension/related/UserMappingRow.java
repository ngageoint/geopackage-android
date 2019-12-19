package mil.nga.geopackage.extension.related;

import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * User Mapping Row containing the values from a single cursor row
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserMappingRow extends UserCustomRow {

    /**
     * Constructor to create an empty row
     *
     * @param table user mapping table
     */
    protected UserMappingRow(UserMappingTable table) {
        super(table);
    }

    /**
     * Constructor
     *
     * @param userCustomRow user custom row
     */
    public UserMappingRow(UserCustomRow userCustomRow) {
        super(userCustomRow.getTable(), userCustomRow.getColumns(), userCustomRow.getRowColumnTypes(),
                userCustomRow.getValues());
    }

    /**
     * Copy Constructor
     *
     * @param userMappingRow user mapping row to copy
     */
    public UserMappingRow(UserMappingRow userMappingRow) {
        super(userMappingRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserMappingTable getTable() {
        return (UserMappingTable) super.getTable();
    }

    /**
     * Get the base ID column index
     *
     * @return base ID column index
     */
    public int getBaseIdColumnIndex() {
        return getColumns().getColumnIndex(UserMappingTable.COLUMN_BASE_ID);
    }

    /**
     * Get the base ID column
     *
     * @return base ID column
     */
    public UserCustomColumn getBaseIdColumn() {
        return getColumns().getColumn(UserMappingTable.COLUMN_BASE_ID);
    }

    /**
     * Get the base ID
     *
     * @return base ID
     */
    public long getBaseId() {
        return ((Number) getValue(getBaseIdColumnIndex())).longValue();
    }

    /**
     * Set the base ID
     *
     * @param baseId base ID
     */
    public void setBaseId(long baseId) {
        setValue(getBaseIdColumnIndex(), baseId);
    }

    /**
     * Get the related ID column index
     *
     * @return related ID column index
     */
    public int getRelatedIdColumnIndex() {
        return getColumns().getColumnIndex(UserMappingTable.COLUMN_RELATED_ID);
    }

    /**
     * Get the related ID column
     *
     * @return related ID column
     */
    public UserCustomColumn getRelatedIdColumn() {
        return getColumns().getColumn(UserMappingTable.COLUMN_RELATED_ID);
    }

    /**
     * Get the related ID
     *
     * @return related ID
     */
    public long getRelatedId() {
        return ((Number) getValue(getRelatedIdColumnIndex())).longValue();
    }

    /**
     * Set the related ID
     *
     * @param relatedId related ID
     */
    public void setRelatedId(long relatedId) {
        setValue(getRelatedIdColumnIndex(), relatedId);
    }

    /**
     * Copy the row
     *
     * @return row copy
     */
    public UserMappingRow copy() {
        return new UserMappingRow(this);
    }

}
