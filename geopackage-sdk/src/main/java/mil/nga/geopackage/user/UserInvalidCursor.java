package mil.nga.geopackage.user;

import android.database.Cursor;
import android.util.Log;

import com.j256.ormlite.misc.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.db.GeoPackageDataType;

/**
 * Abstract User Invalid Cursor for handling failed rows due to large blobs
 *
 * @param <TColumn>
 * @param <TTable>
 * @param <TRow>
 * @param <TCursor>
 * @param <TUserDao>
 * @since 2.0.0
 */
public abstract class UserInvalidCursor<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TCursor extends UserCursor<TColumn, TTable, TRow>, TUserDao extends UserDao<TColumn, TTable, TRow, TCursor>>
        implements UserCoreResult<TColumn, TTable, TRow> {

    /**
     * Chunk size to read large blobs. Max supported Android cursor window size is currently 2 mb, using 1 mb to ensure space
     */
    private static final int CHUNK_SIZE = 1048576;

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
        if (currentPosition < invalidPositions.size()) {
            int invalidPosition = invalidPositions.get(currentPosition);
            hasNext = cursor.moveToPosition(invalidPosition);
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

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {

            byte[] blobChunk = new byte[]{0};
            for (int i = 1; blobChunk.length > 0; i += CHUNK_SIZE) {
                if (i > 1) {
                    byteStream.write(blobChunk);
                }
                blobChunk = new byte[]{};
                String query = "select substr(" +
                        CoreSQLUtils.quoteWrap(column.getName()) + ", " + i + ", " + CHUNK_SIZE + ") from "
                        + CoreSQLUtils.quoteWrap(dao.getTableName()) + " where "
                        + CoreSQLUtils.quoteWrap(row.getPkColumn().getName()) + " = " + row.getId();
                Cursor blobCursor = dao.getDatabaseConnection().getDb().rawQuery(query, null);
                try {
                    if (blobCursor.moveToNext()) {
                        blobChunk = blobCursor.getBlob(0);
                    }
                } finally {
                    blobCursor.close();
                }
            }
            byte[] blob = byteStream.toByteArray();
            row.setValue(column.getIndex(), blob);

        } catch (IOException e) {
            Log.e(UserInvalidCursor.class.getSimpleName(), "Failed to read large blob value. Table: "
                    + dao.getTableName() + ", Column: " + column.getName() + ", Position: " + getPosition(), e);
        } finally {
            IOUtils.closeQuietly(byteStream);
        }
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
    public int getCount() {
        return invalidPositions.size();
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
        if (position < invalidPositions.size()) {
            currentPosition = position;
            int invalidPosition = invalidPositions.get(currentPosition);
            moved = cursor.moveToPosition(invalidPosition);
        }
        return moved;
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

}
