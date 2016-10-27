package mil.nga.geopackage.attributes;

import android.database.Cursor;

import mil.nga.geopackage.user.UserCursor;

/**
 * Attributes Cursor to wrap a database cursor for attributes queries
 *
 * @author osbornb
 * @since 1.3.1
 */
public class AttributesCursor extends
        UserCursor<AttributesColumn, AttributesTable, AttributesRow> {

    /**
     * Constructor
     *
     * @param table  attributes table
     * @param cursor cursor
     */
    public AttributesCursor(AttributesTable table, Cursor cursor) {
        super(table, cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesRow getRow(int[] columnTypes, Object[] values) {
        AttributesRow row = new AttributesRow(getTable(), columnTypes, values);
        return row;
    }

}
