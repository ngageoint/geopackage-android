package mil.nga.giat.geopackage.tiles.user;

import java.util.List;

import mil.nga.giat.geopackage.db.GeoPackageDataType;
import mil.nga.giat.geopackage.user.UserTableReader;

/**
 * Reads the metadata from an existing tile table
 * 
 * @author osbornb
 */
public class TileTableReader extends
        UserTableReader<TileColumn, TileTable, TileRow, TileCursor> {

	/**
	 * Constructor
	 * 
	 * @param tableName
	 */
	public TileTableReader(String tableName) {
		super(tableName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TileTable createTable(String tableName,
			List<TileColumn> columnList) {
		return new TileTable(tableName, columnList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TileColumn createColumn(TileCursor cursor, int index, String name,
			String type, Long max, boolean notNull, int defaultValueIndex,
			boolean primaryKey) {

		GeoPackageDataType dataType = GeoPackageDataType.fromName(type);

		Object defaultValue = cursor.getValue(
                defaultValueIndex, dataType);

		TileColumn column = new TileColumn(index, name, dataType, max, notNull,
				defaultValue, primaryKey);

		return column;
	}

}
