package mil.nga.geopackage.user;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.db.GeoPackageDataType;

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
     * Invalid cursor positions due to large sized blobs
     */
    private Set<Integer> invalidPositions = new LinkedHashSet<>();

    /**
     * User query arguments
     */
    private UserQuery query;

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
     * Get the invalid positions found when retrieving rows
     *
     * @return invalid positions
     * @since 1.4.2
     */
    public List<Integer> getInvalidPositions() {
        return new ArrayList<>(invalidPositions);
    }

    /**
     * Determine if invalid positions were found when retrieving rows
     *
     * @return true if invalid positions
     * @since 1.4.2
     */
    public boolean hasInvalidPositions() {
        return !invalidPositions.isEmpty();
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

            boolean valid = true;

            for (TColumn column : table.getColumns()) {

                int index = column.getIndex();
                int columnType = getType(index);

                if (column.isPrimaryKey() && columnType == FIELD_TYPE_NULL) {
                    valid = false;
                }

                columnTypes[index] = columnType;
                values[index] = getValue(column);

            }

            row = getRow(columnTypes, values);

            if (!valid) {
                invalidPositions.add(getPosition());
                row.setValid(false);
            }
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

    /**
     * Set the user query
     *
     * @param query user query
     * @since 1.4.2
     */
    public void setQuery(UserQuery query) {
        this.query = query;
    }

    /**
     * Get the user query
     *
     * @return user query
     * @since 1.4.2
     */
    public UserQuery getQuery() {
        return query;
    }

}
