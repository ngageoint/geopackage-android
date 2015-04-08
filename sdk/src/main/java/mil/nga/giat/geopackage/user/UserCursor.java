package mil.nga.giat.geopackage.user;

import android.database.Cursor;
import android.database.CursorWrapper;

import mil.nga.giat.geopackage.db.GeoPackageDataType;

/**
 * Abstract User Cursor
 *
 * @param <TColumn>
 * @param <TTable>
 * @param <TRow>
 * @author osbornb
 */
public abstract class UserCursor<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>>
        extends CursorWrapper implements UserCoreResult<TColumn, TTable, TRow> {

    /**
     * Table
     */
    private final TTable table;

    /**
     * Constructor
     *
     * @param table
     * @param cursor
     */
    protected UserCursor(TTable table, Cursor cursor) {
        super(cursor);
        this.table = table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(TColumn column) {
        Object value = getValue(column.getIndex(), column.getDataType());
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TTable getTable() {
        return table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TRow getRow() {

        TRow row = null;

        if (table != null) {

            int[] columnTypes = new int[table.columnCount()];
            Object[] values = new Object[table.columnCount()];

            for (TColumn column : table.getColumns()) {

                int index = column.getIndex();

                columnTypes[index] = getType(index);

                values[index] = getValue(column);

            }

            row = getRow(columnTypes, values);
        }

        return row;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(int index,
                           GeoPackageDataType dataType) {
        Object value = UserCoreResultUtils.getValue(this, index, dataType);
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wasNull() {
        return false;
    }

}
