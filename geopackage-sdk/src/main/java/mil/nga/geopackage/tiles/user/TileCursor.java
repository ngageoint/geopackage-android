package mil.nga.geopackage.tiles.user;

import mil.nga.geopackage.user.UserCursor;
import android.database.Cursor;

/**
 * Tile Cursor to wrap a database cursor for tile queries
 * 
 * @author osbornb
 */
public class TileCursor extends UserCursor<TileColumn, TileTable, TileRow> {

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param cursor
	 */
	public TileCursor(TileTable table, Cursor cursor) {
		super(table, cursor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public TileRow getRow(int[] columnTypes, Object[] values) {
		TileRow row = new TileRow(getTable(), columnTypes, values);
		return row;
	}

}
