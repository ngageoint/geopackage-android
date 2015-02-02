package mil.nga.giat.geopackage.data.c4;

import mil.nga.giat.geopackage.db.GeoPackageDataType;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryType;

/**
 * Metadata about a single column from a feature table
 * 
 * @author osbornb
 */
public class FeatureColumn implements Comparable<FeatureColumn> {

	/**
	 * Column index
	 */
	private final int index;

	/**
	 * Column name
	 */
	private final String name;

	/**
	 * Max count or size
	 */
	private final Long typeMax;

	/**
	 * True if a not null column
	 */
	private final boolean notNull;

	/**
	 * Default column value
	 */
	private final Object defaultValue;

	/**
	 * True if a primary key column
	 */
	private final boolean primaryKey;

	/**
	 * Geometry type if a geometry column
	 */
	private final GeoPackageGeometryType geometryType;

	/**
	 * Data type if not a geometry column
	 */
	private final GeoPackageDataType dataType;

	/**
	 * Create a new primary key column
	 * 
	 * @param index
	 * @param name
	 * @return
	 */
	public static FeatureColumn createPrimaryKeyColumn(int index, String name) {
		return new FeatureColumn(index, name, null, true, null, true, null,
				GeoPackageDataType.INTEGER);
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
			GeoPackageGeometryType type, boolean notNull, Object defaultValue) {
		return new FeatureColumn(index, name, null, notNull, defaultValue,
				false, type, null);
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
		return new FeatureColumn(index, name, null, notNull, defaultValue,
				false, null, type);
	}

	/**
	 * Create a new text column with optional max character count
	 * 
	 * @param index
	 * @param name
	 * @param maxCharCount
	 * @param notNull
	 * @param defaultValue
	 * @return
	 */
	public static FeatureColumn createTextColumn(int index, String name,
			Long maxCharCount, boolean notNull, Object defaultValue) {
		return new FeatureColumn(index, name, maxCharCount, notNull,
				defaultValue, false, null, GeoPackageDataType.TEXT);
	}

	/**
	 * Create a new column
	 * 
	 * @param index
	 * @param name
	 * @param maxSize
	 * @param notNull
	 * @param defaultValue
	 * @return
	 */
	public static FeatureColumn createBlobColumn(int index, String name,
			Long maxSize, boolean notNull, Object defaultValue) {
		return new FeatureColumn(index, name, maxSize, notNull, defaultValue,
				false, null, GeoPackageDataType.BLOB);
	}

	/**
	 * Constructor
	 * 
	 * @param index
	 * @param name
	 * @param typeMax
	 * @param notNull
	 * @param defaultValue
	 * @param primaryKey
	 * @param geometryType
	 * @param dataType
	 */
	FeatureColumn(int index, String name, Long typeMax, boolean notNull,
			Object defaultValue, boolean primaryKey,
			GeoPackageGeometryType geometryType, GeoPackageDataType dataType) {
		this.index = index;
		this.name = name;
		this.typeMax = typeMax;
		this.notNull = notNull;
		this.defaultValue = defaultValue;
		this.primaryKey = primaryKey;
		this.geometryType = geometryType;
		this.dataType = dataType;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public Long getTypeMax() {
		return typeMax;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
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
	public GeoPackageGeometryType getGeometryType() {
		return geometryType;
	}

	/**
	 * When not a geometry column, gets the data type
	 * 
	 * @return
	 */
	public GeoPackageDataType getDataType() {
		return dataType;
	}

	/**
	 * Get the database type, either the geometry or data type
	 * 
	 * @return
	 */
	public String getTypeName() {
		String type;
		if (isGeometry()) {
			type = geometryType.name();
		} else {
			type = dataType.name();
		}
		return type;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Sort by index
	 */
	@Override
	public int compareTo(FeatureColumn another) {
		return index - another.index;
	}

}
