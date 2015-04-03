package mil.nga.giat.geopackage.tiles.user;

import java.util.List;

import mil.nga.giat.geopackage.db.CursorDatabaseUtils;
import mil.nga.giat.geopackage.db.GeoPackageDataType;
import mil.nga.giat.geopackage.user.UserTableReader;
import android.database.Cursor;

/**
 * Reads the metadata from an existing tile table
 * 
 * @author osbornb
 */
public class TileTableReader extends UserTableReader<TileColumn, TileTable> {

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
	protected TileColumn createColumn(Cursor cursor, int index, String name,
			String type, Long max, boolean notNull, int defaultValueIndex,
			boolean primaryKey) {

		GeoPackageDataType dataType = GeoPackageDataType.fromName(type);

		Object defaultValue = CursorDatabaseUtils.getValue(cursor,
                defaultValueIndex, dataType);

		TileColumn column = new TileColumn(index, name, dataType, max, notNull,
				defaultValue, primaryKey);

		return column;
	}

}
