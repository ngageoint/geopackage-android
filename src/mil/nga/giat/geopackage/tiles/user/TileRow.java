package mil.nga.giat.geopackage.tiles.user;

import mil.nga.giat.geopackage.user.UserRow;

/**
 * Tile Row containing the values from a single cursor row
 * 
 * @author osbornb
 */
public class TileRow extends UserRow<TileColumn, TileTable> {

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param columnTypes
	 * @param values
	 */
	TileRow(TileTable table, int[] columnTypes, Object[] values) {
		super(table, columnTypes, values);
	}

	/**
	 * Constructor to create an empty row
	 * 
	 * @param columns
	 */
	TileRow(TileTable table) {
		super(table);
	}

	/**
	 * Get the zoom level column index
	 * 
	 * @return
	 */
	public int getZoomLevelColumnIndex() {
		return getTable().getZoomLevelColumnIndex();
	}

	/**
	 * Get the zoom level column
	 * 
	 * @return
	 */
	public TileColumn getZoomLevelColumn() {
		return getTable().getZoomLevelColumn();
	}

	/**
	 * Get the zoom level
	 * 
	 * @return
	 */
	public int getZoomLevel() {
		return (Integer) getValue(getZoomLevelColumnIndex());
	}

	/**
	 * Set the zoom level
	 * 
	 * @param zoomLevel
	 */
	public void setZoomLevel(int zoomLevel) {
		setValue(getZoomLevelColumnIndex(), zoomLevel);
	}

	/**
	 * Get the tile column column index
	 * 
	 * @return
	 */
	public int getTileColumnColumnIndex() {
		return getTable().getTileColumnColumnIndex();
	}

	/**
	 * Get the tile column column
	 * 
	 * @return
	 */
	public TileColumn getTileColumnColumn() {
		return getTable().getTileColumnColumn();
	}

	/**
	 * Get the tile column
	 * 
	 * @return
	 */
	public int getTileColumn() {
		return (Integer) getValue(getTileColumnColumnIndex());
	}

	/**
	 * Set the tile column
	 * 
	 * @param tileColumn
	 */
	public void setTileColumn(int tileColumn) {
		setValue(getTileColumnColumnIndex(), tileColumn);
	}

	/**
	 * Get the tile row column index
	 * 
	 * @return
	 */
	public int getTileRowColumnIndex() {
		return getTable().getTileRowColumnIndex();
	}

	/**
	 * Get the tile row column
	 * 
	 * @return
	 */
	public TileColumn getTileRowColumn() {
		return getTable().getTileRowColumn();
	}

	/**
	 * Get the tile row
	 * 
	 * @return
	 */
	public int getTileRow() {
		return (Integer) getValue(getTileRowColumnIndex());
	}

	/**
	 * Set the tile row
	 * 
	 * @param tileRow
	 */
	public void setTileRow(int tileRow) {
		setValue(getTileRowColumnIndex(), tileRow);
	}

	/**
	 * Get the tile data column index
	 * 
	 * @return
	 */
	public int getTileDataColumnIndex() {
		return getTable().getTileDataColumnIndex();
	}

	/**
	 * Get the tile data column
	 * 
	 * @return
	 */
	public TileColumn getTileDataColumn() {
		return getTable().getTileDataColumn();
	}

	/**
	 * Get the tile data
	 * 
	 * @return
	 */
	public byte[] getTileData() {
		return (byte[]) getValue(getTileDataColumnIndex());
	}

	/**
	 * Set the tile data
	 * 
	 * @param tileData
	 */
	public void setTileData(byte[] tileData) {
		setValue(getTileDataColumnIndex(), tileData);
	}

}
