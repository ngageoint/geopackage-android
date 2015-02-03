package mil.nga.giat.geopackage.features.user;

import java.util.List;

import mil.nga.giat.geopackage.db.GeoPackageDataType;
import mil.nga.giat.geopackage.db.GeoPackageDatabaseUtils;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.user.UserTableReader;
import android.database.Cursor;

/**
 * Reads the metadata from an existing feature table
 * 
 * @author osbornb
 */
public class FeatureTableReader extends
		UserTableReader<FeatureColumn, FeatureTable> {

	/**
	 * Geometry columns
	 */
	private GeometryColumns geometryColumns;

	/**
	 * Constructor
	 * 
	 * @param geometryColumns
	 */
	public FeatureTableReader(GeometryColumns geometryColumns) {
		super(geometryColumns.getTableName());
		this.geometryColumns = geometryColumns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected FeatureTable createTable(String tableName,
			List<FeatureColumn> columnList) {
		return new FeatureTable(tableName, columnList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected FeatureColumn createColumn(Cursor cursor, int index, String name,
			String type, Long max, boolean notNull, int defaultValueIndex,
			boolean primaryKey) {

		boolean geometry = name.equals(geometryColumns.getColumnName());

		GeometryType geometryType = null;
		GeoPackageDataType dataType = null;
		if (geometry) {
			geometryType = GeometryType.valueOf(type);
		} else {
			dataType = GeoPackageDataType.valueOf(type);
		}
		Object defaultValue = GeoPackageDatabaseUtils.getValue(cursor,
				defaultValueIndex, dataType);

		FeatureColumn column = new FeatureColumn(index, name, dataType, max,
				notNull, defaultValue, primaryKey, geometryType);

		return column;
	}

}
