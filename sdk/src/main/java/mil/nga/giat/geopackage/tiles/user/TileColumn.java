package mil.nga.giat.geopackage.tiles.user;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.db.GeoPackageDataType;
import mil.nga.giat.geopackage.user.UserColumn;

/**
 * Tile column
 * 
 * @author osbornb
 */
public class TileColumn extends UserColumn {

	/**
	 * Create an id column
	 * 
	 * @param index
	 * @return
	 */
	public static TileColumn createIdColumn(int index) {
		return new TileColumn(index, TileTable.COLUMN_ID,
				GeoPackageDataType.INTEGER, null, false, null, true);
	}

	/**
	 * Create a zoom level column
	 * 
	 * @param index
	 * @return
	 */
	public static TileColumn createZoomLevelColumn(int index) {
		return new TileColumn(index, TileTable.COLUMN_ZOOM_LEVEL,
				GeoPackageDataType.INTEGER, null, true, 0, false);
	}

	/**
	 * Create a tile column column
	 * 
	 * @param index
	 * @return
	 */
	public static TileColumn createTileColumnColumn(int index) {
		return new TileColumn(index, TileTable.COLUMN_TILE_COLUMN,
				GeoPackageDataType.INTEGER, null, true, 0, false);
	}

	/**
	 * Create a tile row column
	 * 
	 * @param index
	 * @return
	 */
	public static TileColumn createTileRowColumn(int index) {
		return new TileColumn(index, TileTable.COLUMN_TILE_ROW,
				GeoPackageDataType.INTEGER, null, true, 0, false);
	}

	/**
	 * Create a tile data column
	 * 
	 * @param index
	 * @return
	 */
	public static TileColumn createTileDataColumn(int index) {
		return new TileColumn(index, TileTable.COLUMN_TILE_DATA,
				GeoPackageDataType.BLOB, null, true, null, false);
	}

	/**
	 * Create a new column
	 * 
	 * @param index
	 * @param name
	 * @param type
	 * @param notNull
	 * @param defaultValue
	 * @return
	 */
	public static TileColumn createColumn(int index, String name,
			GeoPackageDataType type, boolean notNull, Object defaultValue) {
		return createColumn(index, name, type, null, notNull, defaultValue);
	}

	/**
	 * Create a new column
	 * 
	 * @param index
	 * @param name
	 * @param type
	 * @param max
	 * @param notNull
	 * @param defaultValue
	 * @return
	 */
	public static TileColumn createColumn(int index, String name,
			GeoPackageDataType type, Long max, boolean notNull,
			Object defaultValue) {
		return new TileColumn(index, name, type, max, notNull, defaultValue,
				false);
	}

	/**
	 * Constructor
	 * 
	 * @param index
	 * @param name
	 * @param dataType
	 * @param max
	 * @param notNull
	 * @param defaultValue
	 * @param primaryKey
	 */
	TileColumn(int index, String name, GeoPackageDataType dataType, Long max,
			boolean notNull, Object defaultValue, boolean primaryKey) {
		super(index, name, dataType, max, notNull, defaultValue, primaryKey);
		if (dataType == null) {
			throw new GeoPackageException(
					"Data Type is required to create column: " + name);
		}
	}

}
