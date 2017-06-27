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
     * @param columnTypes column types
     * @param values      values
     */
    AttributesRow(AttributesTable table, int[] columnTypes, Object[] values) {
        super(table, columnTypes, values);
    }

    /**
     * Constructor to create an empty row
     *
     * @param table
     */
    AttributesRow(AttributesTable table) {
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

}
