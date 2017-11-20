package mil.nga.geopackage.features.user;

import java.util.List;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.user.UserTableReader;
import mil.nga.wkb.geom.GeometryType;

/**
 * Reads the metadata from an existing feature table
 * 
 * @author osbornb
 */
public class FeatureTableReader extends
        UserTableReader<FeatureColumn, FeatureTable, FeatureRow, FeatureCursor> {

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
	protected FeatureColumn createColumn(FeatureCursor cursor, int index, String name,
			String type, Long max, boolean notNull, int defaultValueIndex,
			boolean primaryKey) {

		boolean geometry = name.equals(geometryColumns.getColumnName());

		GeometryType geometryType = null;
		GeoPackageDataType dataType = null;
		if (geometry) {
			geometryType = GeometryType.fromName(type);
			dataType = GeoPackageDataType.BLOB;
		} else {
			dataType = GeoPackageDataType.fromName(type);
		}
		Object defaultValue = cursor.getValue(
                defaultValueIndex, dataType);

		FeatureColumn column = new FeatureColumn(index, name, dataType, max,
				notNull, defaultValue, primaryKey, geometryType);

		return column;
	}

}
