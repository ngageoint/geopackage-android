package mil.nga.giat.geopackage.features.user;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.db.GeoPackageDataType;
import mil.nga.giat.wkb.geom.GeometryType;
import mil.nga.giat.geopackage.user.UserColumn;

/**
 * Feature column
 * 
 * @author osbornb
 */
public class FeatureColumn extends UserColumn {

	/**
	 * Geometry type if a geometry column
	 */
	private final GeometryType geometryType;

	/**
	 * Create a new primary key column
	 * 
	 * @param index
	 * @param name
	 * @return
	 */
	public static FeatureColumn createPrimaryKeyColumn(int index, String name) {
		return new FeatureColumn(index, name, GeoPackageDataType.INTEGER, null,
				true, null, true, null);
	}

	/**
	 * Create a new geometry column
	 * 
	 * @param index
	 * @param name
	 * @param type
	 * @param notNull
	 * @param defaultValue
	 * @return
	 */
	public static FeatureColumn createGeometryColumn(int index, String name,
			GeometryType type, boolean notNull, Object defaultValue) {
		if (type == null) {
			throw new GeoPackageException(
					"Geometry Type is required to create geometry column: "
							+ name);
		}
		return new FeatureColumn(index, name, null, null, notNull,
				defaultValue, false, type);
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
	public static FeatureColumn createColumn(int index, String name,
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
	public static FeatureColumn createColumn(int index, String name,
			GeoPackageDataType type, Long max, boolean notNull,
			Object defaultValue) {
		return new FeatureColumn(index, name, type, max, notNull, defaultValue,
				false, null);
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
	 * @param geometryType
	 */
	FeatureColumn(int index, String name, GeoPackageDataType dataType,
			Long max, boolean notNull, Object defaultValue, boolean primaryKey,
			GeometryType geometryType) {
		super(index, name, dataType, max, notNull, defaultValue, primaryKey);
		this.geometryType = geometryType;
		if (geometryType == null && dataType == null) {
			throw new GeoPackageException(
					"Data Type is required to create column: " + name);
		}
	}

	/**
	 * Determine if this column is a geometry
	 * 
	 * @return
	 */
	public boolean isGeometry() {
		return geometryType != null;
	}

	/**
	 * When a geometry column, gets the geometry type
	 * 
	 * @return
	 */
	public GeometryType getGeometryType() {
		return geometryType;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Either the geometry or data type
	 * 
	 * @return
	 */
	@Override
	public String getTypeName() {
		String type;
		if (isGeometry()) {
			type = geometryType.name();
		} else {
			type = super.getTypeName();
		}
		return type;
	}

}
