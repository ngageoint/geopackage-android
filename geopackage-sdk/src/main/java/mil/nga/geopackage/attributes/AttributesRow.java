package mil.nga.geopackage.attributes;

import mil.nga.geopackage.user.UserRow;

/**
 * Attributes Row containing the values from a single cursor row
 *
 * @author osbornb
 * @since 1.3.1
 */
public class AttributesRow extends UserRow<AttributesColumn, AttributesTable> {

    /**
     * Constructor
     *
     * @param table       attributes table
     * @param columns     columns
     * @param columnTypes column types
     * @param values      values
     * @since 3.5.0
     */
    AttributesRow(AttributesTable table, AttributesColumns columns, int[] columnTypes, Object[] values) {
        super(table, columns, columnTypes, values);
    }

    /**
     * Constructor to create an empty row
     *
     * @param table attributes table
     */
    protected AttributesRow(AttributesTable table) {
        super(table);
    }

    /**
     * Copy Constructor
     *
     * @param attributesRow attributes row to copy
     * @since 1.4.0
     */
    public AttributesRow(AttributesRow attributesRow) {
        super(attributesRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesColumns getColumns() {
        return (AttributesColumns) super.getColumns();
    }

    /**
     * Copy the row
     *
     * @return row copy
     * @since 3.0.1
     */
    public AttributesRow copy() {
        return new AttributesRow(this);
    }

}
