package mil.nga.geopackage.user;

import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageDataType;

/**
 * Abstract User Invalid Cursor for handling failed rows due to large blobs
 *
 * @param <TColumn>  column type
 * @param <TTable>   table type
 * @param <TRow>     row type
 * @param <TCursor>  cursor type
 * @param <TUserDao> user dao type
 * @since 2.0.0
 */
public abstract class UserInvalidCursor<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TCursor extends UserCursor<TColumn, TTable, TRow>, TUserDao extends UserDao<TColumn, TTable, TRow, TCursor>>
        implements UserCoreResult<TColumn, TTable, TRow> {

    /**
     * User DAO
     */
    private TUserDao dao;

    /**
     * Requery cursor
     */
    private TCursor cursor;

    /**
     * Invalid positions from initial query cursor
     */
    private List<Integer> invalidPositions;

    /**
     * Blob columns to query in chunks
     */
    private List<TColumn> blobColumns;

    /**
     * Current position within the cursor
     */
    private int currentPosition = -1;

    /**
     * Constructor
     *
     * @param dao              user dao
     * @param cursor           requery cursor
     * @param invalidPositions invalid first cursor positions
     * @param blobColumns      blob columns
     */
    protected UserInvalidCursor(TUserDao dao, TCursor cursor, List<Integer> invalidPositions, List<TColumn> blobColumns) {
        this.dao = dao;
        this.cursor = cursor;
        this.invalidPositions = invalidPositions;
        this.blobColumns = blobColumns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToNext() {
        boolean hasNext = false;
        currentPosition++;
        if (invalidPositions != null) {
            if (currentPosition < invalidPositions.size()) {
                int invalidPosition = invalidPositions.get(currentPosition);
                hasNext = cursor.moveToPosition(invalidPosition);
            }
        } else {
            hasNext = cursor.moveToNext();
        }
        return hasNext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TRow getRow() {
        TRow row = cursor.getRow();
        if (row.hasId()) {
            for (UserColumn column : blobColumns) {
                readBlobValue(row, column);
            }
        }
        return row;
    }

    /**
     * Read the blob column value in chunks
     *
     * @param row    user row
     * @param column user blob column
     */
    private void readBlobValue(UserRow row, UserColumn column) {
        UserCursor.readBlobValue(dao, this, row, column);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        cursor.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPosition() {
        return cursor.getPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TRow getRow(int[] columnTypes, Object[] values) {
        return cursor.getRow(columnTypes, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(TColumn column) {
        return cursor.getValue(column);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(int index, GeoPackageDataType dataType) {
        return cursor.getValue(index, dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TTable getTable() {
        return cursor.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTableName() {
        return getTable().getTableName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserColumns<TColumn> getColumns() {
        return cursor.getColumns();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        int count;
        if (invalidPositions != null) {
            count = invalidPositions.size();
        } else {
            count = cursor.getCount();
        }

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToFirst() {
        currentPosition = -1;
        return moveToNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToPosition(int position) {
        boolean moved = false;
        if (invalidPositions != null) {
            if (position < invalidPositions.size()) {
                currentPosition = position;
                int invalidPosition = invalidPositions.get(currentPosition);
                moved = cursor.moveToPosition(invalidPosition);
            }
        } else {
            cursor.moveToPosition(position);
        }
        return moved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(int index) {
        return getValue(getColumns().getColumn(index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String columnName) {
        return getValue(getColumns().getColumn(columnName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getId() {
        long id = -1;

        TColumn pkColumn = getColumns().getPkColumn();
        if (pkColumn == null) {
            StringBuilder error = new StringBuilder(
                    "No primary key column in ");
            if (getColumns().isCustom()) {
                error.append("custom specified table columns. ");
            }
            error.append("table: " + getColumns().getTableName());
            if (getColumns().isCustom()) {
                error.append(", columns: " + getColumns().getColumnNames());
            }
            throw new GeoPackageException(error.toString());
        }

        Object objectValue = getValue(pkColumn);
        if (objectValue instanceof Number) {
            id = ((Number) objectValue).longValue();
        } else {
            throw new GeoPackageException(
                    "Primary Key value was not a number. table: "
                            + getColumns().getTableName() + ", index: "
                            + pkColumn.getIndex() + ", name: "
                            + pkColumn.getName() + ", value: " + objectValue);
        }

        return id;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return cursor.getColumnCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType(int columnIndex) {
        return cursor.getType(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnIndex(String columnName) {
        return cursor.getColumnIndex(columnName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(int columnIndex) {
        return cursor.getString(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLong(int columnIndex) {
        return cursor.getLong(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(int columnIndex) {
        return cursor.getInt(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(int columnIndex) {
        return cursor.getShort(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDouble(int columnIndex) {
        return cursor.getDouble(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloat(int columnIndex) {
        return cursor.getFloat(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getBlob(int columnIndex) {
        return cursor.getBlob(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wasNull() {
        return cursor.wasNull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<TRow> iterator() {
        return new Iterator<TRow>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return moveToNext();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public TRow next() {
                return getRow();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSql() {
        return cursor.getSql();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSelectionArgs() {
        return cursor.getSelectionArgs();
    }

}
