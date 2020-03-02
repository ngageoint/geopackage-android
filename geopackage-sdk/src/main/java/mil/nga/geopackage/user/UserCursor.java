package mil.nga.geopackage.user;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.CursorResult;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.ResultUtils;

/**
 * Abstract User Cursor
 *
 * @param <TColumn> column type
 * @param <TTable>  table type
 * @param <TRow>    row type
 * @author osbornb
 */
public abstract class UserCursor<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>>
        extends CursorResult implements UserCoreResult<TColumn, TTable, TRow> {

    /**
     * Table
     */
    private TTable table;

    /**
     * Columns
     */
    private UserColumns<TColumn> columns;

    /**
     * Invalid cursor positions due to large sized blobs
     */
    private Set<Integer> invalidPositions = new LinkedHashSet<>();

    /**
     * User query arguments
     */
    private UserQuery query;

    /**
     * Data access object used for requeries
     */
    private UserDao<TColumn, TTable, TRow, ? extends UserCursor<TColumn, TTable, TRow>> dao;

    /**
     * User invalid cursor for iterating through the invalid requeried rows
     */
    private UserInvalidCursor<TColumn, TTable, TRow, ? extends UserCursor<TColumn, TTable, TRow>, ? extends UserDao<TColumn, TTable, TRow, ? extends UserCursor<TColumn, TTable, TRow>>> invalidCursor;

    /**
     * Constructor
     *
     * @param table  table
     * @param cursor cursor
     */
    protected UserCursor(TTable table, Cursor cursor) {
        this(table, table.getUserColumns(), cursor);
    }

    /**
     * Constructor
     *
     * @param table   table
     * @param columns columns
     * @param cursor  cursor
     * @since 3.5.0
     */
    protected UserCursor(TTable table, String[] columns, Cursor cursor) {
        super(cursor);
        UserColumns<TColumn> userColumns = null;
        if (columns != null) {
            userColumns = table.createUserColumns(columns);
        } else {
            userColumns = table.getUserColumns();
        }
        this.table = table;
        this.columns = userColumns;
    }

    /**
     * Constructor
     *
     * @param table   table
     * @param columns columns
     * @param cursor  cursor
     * @since 3.5.0
     */
    protected UserCursor(TTable table, UserColumns<TColumn> columns,
                         Cursor cursor) {
        super(cursor);
        this.table = table;
        this.columns = columns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(TColumn column) {
        return getValue(columns.getColumnIndex(column.getName()), column.getDataType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(int index) {
        return getValue(index, columns.getColumn(index).getDataType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String columnName) {
        return getValue(columns.getColumnIndex(columnName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getId() {
        long id = -1;

        TColumn pkColumn = columns.getPkColumn();
        if (pkColumn == null) {
            StringBuilder error = new StringBuilder(
                    "No primary key column in ");
            if (columns.isCustom()) {
                error.append("custom specified table columns. ");
            }
            error.append("table: " + columns.getTableName());
            if (columns.isCustom()) {
                error.append(", columns: " + columns.getColumnNames());
            }
            throw new GeoPackageException(error.toString());
        }

        Object objectValue = getValue(pkColumn);
        if (objectValue instanceof Number) {
            id = ((Number) objectValue).longValue();
        } else {
            throw new GeoPackageException(
                    "Primary Key value was not a number. table: "
                            + columns.getTableName() + ", index: "
                            + pkColumn.getIndex() + ", name: "
                            + pkColumn.getName() + ", value: " + objectValue);
        }

        return id;
    }

    /**
     * Set the table
     *
     * @param table table
     * @since 3.2.0
     */
    public void setTable(TTable table) {
        this.table = table;
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
    public String getTableName() {
        return table.getTableName();
    }

    /**
     * Set the columns
     *
     * @param columns columns
     * @since 3.5.0
     */
    public void setColumns(UserColumns<TColumn> columns) {
        this.columns = columns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserColumns<TColumn> getColumns() {
        return columns;
    }

    /**
     * Get the invalid positions found when retrieving rows
     *
     * @return invalid positions
     * @since 2.0.0
     */
    public List<Integer> getInvalidPositions() {
        return new ArrayList<>(invalidPositions);
    }

    /**
     * Determine if invalid positions were found when retrieving rows
     *
     * @return true if invalid positions
     * @since 2.0.0
     */
    public boolean hasInvalidPositions() {
        return !invalidPositions.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToNext() {
        boolean hasNext = super.moveToNext();
        if (!hasNext) {
            hasNext = moveToNextInvalid();
        }
        return hasNext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TRow getRow() {

        TRow row;
        if (invalidCursor == null) {
            row = getCurrentRow();
        } else {
            row = invalidCursor.getRow();
        }

        return row;
    }

    /**
     * Get the current row
     *
     * @return row
     */
    private TRow getCurrentRow() {

        int[] columnTypes = new int[columns.columnCount()];
        Object[] values = new Object[columns.columnCount()];

        boolean valid = true;

        for (int index = 0; index < columns.columnCount(); index++) {
            TColumn column = columns.getColumn(index);

            int columnType = getType(index);

            if (column.isPrimaryKey() && columnType == FIELD_TYPE_NULL) {
                valid = false;
            }

            columnTypes[index] = columnType;
            values[index] = getValue(column);

        }

        TRow row = getRow(columnTypes, values);

        if (!valid) {
            invalidPositions.add(getPosition());
            row.setValid(false);
        }

        return row;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(int index, GeoPackageDataType dataType) {
        return ResultUtils.getValue(invalidCursor == null ? this : invalidCursor, index, dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        super.close();
        if (invalidCursor != null) {
            invalidCursor.close();
        }
    }

    /**
     * Set the user query
     *
     * @param query user query
     * @since 2.0.0
     */
    public void setQuery(UserQuery query) {
        this.query = query;
    }

    /**
     * Get the user query
     *
     * @return user query
     * @since 2.0.0
     */
    public UserQuery getQuery() {
        return query;
    }

    /**
     * Enable requery attempt of invalid rows after iterating through original query rows.
     * Only supported for {@link #moveToNext()} and {@link #getRow()} usage.
     *
     * @param dao data access object used to perform requery
     * @since 2.0.0
     */
    protected void enableInvalidRequery(UserDao<TColumn, TTable, TRow, ? extends UserCursor<TColumn, TTable, TRow>> dao) {
        this.dao = dao;
    }

    /**
     * Move to the next position of invalid rows to requery.  Perform the requery the first time.
     *
     * @return true if invalid rows are left
     */
    private boolean moveToNextInvalid() {

        boolean hasNext = false;

        // If requery has not been performed, a requery dao has been set, and there are invalid positions
        if (invalidCursor == null && dao != null && hasInvalidPositions()) {

            // Close the original cursor when performing an invalid cursor query
            super.close();

            // Set the blob columns to return as null
            List<TColumn> blobColumns = columns.columnsOfType(GeoPackageDataType.BLOB);
            String[] columnsAs = dao.buildColumnsAsNull(blobColumns);
            query.set(UserQueryParamType.COLUMNS_AS, columnsAs);

            // Query without blob columns and create an invalid cursor
            UserCursor<TColumn, TTable, TRow> requeryCursor = dao.query(query);
            invalidCursor = createInvalidCursor(dao, requeryCursor, getInvalidPositions(), blobColumns);
        }

        if (invalidCursor != null) {
            hasNext = invalidCursor.moveToNext();
        }

        return hasNext;
    }

    /**
     * Create an invalid cursor
     *
     * @param dao              data access object
     * @param cursor           user cursor
     * @param invalidPositions invalid positions
     * @param blobColumns      blob columns
     * @return invalid cursor
     */
    protected abstract UserInvalidCursor<TColumn, TTable, TRow, ? extends UserCursor<TColumn, TTable, TRow>, ? extends UserDao<TColumn, TTable, TRow, ? extends UserCursor<TColumn, TTable, TRow>>> createInvalidCursor(
            UserDao dao, UserCursor cursor, List<Integer> invalidPositions, List<TColumn> blobColumns);

}
