package mil.nga.giat.geopackage.tiles.user;

import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.geopackage.db.GeoPackageDataType;
import mil.nga.giat.geopackage.user.UserTable;
import mil.nga.giat.geopackage.user.UserUniqueConstraint;

/**
 * Represents a user tile table
 * 
 * @author osbornb
 */
public class TileTable extends UserTable<TileColumn> {

	/**
	 * Id column name, Requirement 52
	 */
	public static final String COLUMN_ID = "id";

	/**
	 * Zoom level column name, Requirement 53
	 */
	public static final String COLUMN_ZOOM_LEVEL = "zoom_level";

	/**
	 * Tile column column name, Requirement 54
	 */
	public static final String COLUMN_TILE_COLUMN = "tile_column";

	/**
	 * Tile row column name, Requirement 55
	 */
	public static final String COLUMN_TILE_ROW = "tile_row";

	/**
	 * Tile ID column name, implied requirement
	 */
	public static final String COLUMN_TILE_DATA = "tile_data";

	/**
	 * Zoom level column index
	 */
	private final int zoomLevelIndex;

	/**
	 * Tile column column index
	 */
	private final int tileColumnIndex;

	/**
	 * Tile row column index
	 */
	private final int tileRowIndex;

	/**
	 * Tile data column index
	 */
	private final int tileDataIndex;

	/**
	 * Constructor
	 * 
	 * @param tableName
	 * @param columns
	 */
	public TileTable(String tableName, List<TileColumn> columns) {
		super(tableName, columns);

		Integer zoomLevel = null;
		Integer tileColumn = null;
		Integer tileRow = null;
		Integer tileData = null;

		// Build a unique constraint on zoom level, tile column, and tile data
		UserUniqueConstraint<TileColumn> uniqueConstraint = new UserUniqueConstraint<TileColumn>();

		// Find the required columns
		for (TileColumn column : columns) {

			String columnName = column.getName();
			int columnIndex = column.getIndex();

			if (columnName.equals(COLUMN_ZOOM_LEVEL)) {
				duplicateCheck(columnIndex, zoomLevel, COLUMN_ZOOM_LEVEL);
				typeCheck(GeoPackageDataType.INTEGER, column);
				zoomLevel = columnIndex;
				uniqueConstraint.add(column);
			} else if (columnName.equals(COLUMN_TILE_COLUMN)) {
				duplicateCheck(columnIndex, tileColumn, COLUMN_TILE_COLUMN);
				typeCheck(GeoPackageDataType.INTEGER, column);
				tileColumn = columnIndex;
				uniqueConstraint.add(column);
			} else if (columnName.equals(COLUMN_TILE_ROW)) {
				duplicateCheck(columnIndex, tileRow, COLUMN_TILE_ROW);
				typeCheck(GeoPackageDataType.INTEGER, column);
				tileRow = columnIndex;
				uniqueConstraint.add(column);
			} else if (columnName.equals(COLUMN_TILE_DATA)) {
				duplicateCheck(columnIndex, tileData, COLUMN_TILE_DATA);
				typeCheck(GeoPackageDataType.BLOB, column);
				tileData = columnIndex;
			}

		}

		// Verify the required columns were found
		missingCheck(zoomLevel, COLUMN_ZOOM_LEVEL);
		zoomLevelIndex = zoomLevel;

		missingCheck(tileColumn, COLUMN_TILE_COLUMN);
		tileColumnIndex = tileColumn;

		missingCheck(tileRow, COLUMN_TILE_ROW);
		tileRowIndex = tileRow;

		missingCheck(tileData, COLUMN_TILE_DATA);
		tileDataIndex = tileData;

		// Add the unique constraint
		addUniqueConstraint(uniqueConstraint);

	}

	/**
	 * Get the zoom level column index
	 * 
	 * @return
	 */
	public int getZoomLevelColumnIndex() {
		return zoomLevelIndex;
	}

	/**
	 * Get the zoom level column
	 * 
	 * @return
	 */
	public TileColumn getZoomLevelColumn() {
		return getColumn(zoomLevelIndex);
	}

	/**
	 * Get the tile column column index
	 * 
	 * @return
	 */
	public int getTileColumnColumnIndex() {
		return tileColumnIndex;
	}

	/**
	 * Get the tile column column
	 * 
	 * @return
	 */
	public TileColumn getTileColumnColumn() {
		return getColumn(tileColumnIndex);
	}

	/**
	 * Get the tile row column index
	 * 
	 * @return
	 */
	public int getTileRowColumnIndex() {
		return tileRowIndex;
	}

	/**
	 * Get the tile row column
	 * 
	 * @return
	 */
	public TileColumn getTileRowColumn() {
		return getColumn(tileRowIndex);
	}

	/**
	 * Get the tile data column index
	 * 
	 * @return
	 */
	public int getTileDataColumnIndex() {
		return tileDataIndex;
	}

	/**
	 * Get the tile data column
	 * 
	 * @return
	 */
	public TileColumn getTileDataColumn() {
		return getColumn(tileDataIndex);
	}

	/**
	 * Create the required table columns, starting at index 0
	 * 
	 * @return
	 */
	public static List<TileColumn> createRequiredColumns() {
		return createRequiredColumns(0);
	}

	/**
	 * Create the required table columns, starting at the provided index
	 * 
	 * @param startingIndex
	 * @return
	 */
	public static List<TileColumn> createRequiredColumns(int startingIndex) {

		List<TileColumn> columns = new ArrayList<TileColumn>();
		columns.add(TileColumn.createIdColumn(startingIndex++));
		columns.add(TileColumn.createZoomLevelColumn(startingIndex++));
		columns.add(TileColumn.createTileColumnColumn(startingIndex++));
		columns.add(TileColumn.createTileRowColumn(startingIndex++));
		columns.add(TileColumn.createTileDataColumn(startingIndex++));

		return columns;
	}

}
