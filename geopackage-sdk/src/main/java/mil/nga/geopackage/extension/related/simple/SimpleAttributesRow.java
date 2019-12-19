package mil.nga.geopackage.extension.related.simple;

import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * User Simple Attributes Row containing the values from a single cursor row
 *
 * @author osbornb
 * @since 3.0.1
 */
public class SimpleAttributesRow extends UserCustomRow {

    /**
     * Constructor to create an empty row
     *
     * @param table simple attributes table
     */
    protected SimpleAttributesRow(SimpleAttributesTable table) {
        super(table);
    }

    /**
     * Constructor
     *
     * @param userCustomRow user custom row
     */
    public SimpleAttributesRow(UserCustomRow userCustomRow) {
        super(userCustomRow.getTable(), userCustomRow.getColumns(), userCustomRow.getRowColumnTypes(),
                userCustomRow.getValues());
    }

    /**
     * Copy Constructor
     *
     * @param simpleAttributesRow simple attributes row to copy
     */
    public SimpleAttributesRow(SimpleAttributesRow simpleAttributesRow) {
        super(simpleAttributesRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleAttributesTable getTable() {
        return (SimpleAttributesTable) super.getTable();
    }

    /**
     * Get the id column index
     *
     * @return id column index
     */
    public int getIdColumnIndex() {
        return getColumns().getPkColumnIndex();
    }

    /**
     * Get the id column
     *
     * @return id column
     */
    public UserCustomColumn getIdColumn() {
        return getColumns().getPkColumn();
    }

    /**
     * Get the id
     *
     * @return id
     */
    public long getId() {
        return ((Number) getValue(getIdColumnIndex())).longValue();
    }

    /**
     * Copy the row
     *
     * @return row copy
     */
    public SimpleAttributesRow copy() {
        return new SimpleAttributesRow(this);
    }

}
