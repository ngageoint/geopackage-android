package mil.nga.geopackage.tiles.user;

import android.database.Cursor;

import java.util.List;

import mil.nga.geopackage.user.UserCursor;
import mil.nga.geopackage.user.UserDao;
import mil.nga.geopackage.user.UserInvalidCursor;

/**
 * Tile Cursor to wrap a database cursor for tile queries
 *
 * @author osbornb
 */
public class TileCursor extends UserCursor<TileColumn, TileTable, TileRow> {

    /**
     * Constructor
     *
     * @param table  tile table
     * @param cursor cursor
     */
    public TileCursor(TileTable table, Cursor cursor) {
        super(table, cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TileRow getRow(int[] columnTypes, Object[] values) {
        return new TileRow(getTable(), columnTypes, values);
    }

    /**
     * Enable requery attempt of invalid rows after iterating through original query rows.
     * Only supported for {@link #moveToNext()} and {@link #getRow()} usage.
     *
     * @param dao data access object used to perform requery
     * @since 2.0.0
     */
    public void enableInvalidRequery(TileDao dao) {
        super.enableInvalidRequery(dao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserInvalidCursor<TileColumn, TileTable, TileRow, ? extends UserCursor<TileColumn, TileTable, TileRow>, ? extends UserDao<TileColumn, TileTable, TileRow, ? extends UserCursor<TileColumn, TileTable, TileRow>>> createInvalidCursor(UserDao dao, UserCursor cursor, List<Integer> invalidPositions, List<TileColumn> blobColumns) {
        return new TileInvalidCursor((TileDao) dao, (TileCursor) cursor, invalidPositions, blobColumns);
    }

}
