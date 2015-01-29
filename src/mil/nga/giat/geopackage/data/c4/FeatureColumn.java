package mil.nga.giat.geopackage.data.c4;

/**
 * Metadata about a single column from a feature table
 * 
 * @author osbornb
 */
public class FeatureColumn {

	/**
	 * Column index
	 */
	private final int index;

	/**
	 * Column name
	 */
	private final String name;

	/**
	 * Column database type
	 */
	private final String type;

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
	 * True if a geometry column
	 */
	private final boolean geometry;

	/**
	 * Create a new primary key column
	 * 
	 * @param index
	 * @param name
	 * @return
	 */
	public static FeatureColumn createPrimaryKeyColumn(int index, String name) {
		return new FeatureColumn(index, name, "INTEGER", true, null, true,
				false);
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
			String type, boolean notNull, Object defaultValue) {
		return new FeatureColumn(index, name, type, notNull, defaultValue,
				false, true);
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
			String type, boolean notNull, Object defaultValue) {
		return new FeatureColumn(index, name, type, notNull, defaultValue,
				false, false);
	}

	/**
	 * Constructor
	 * 
	 * @param index
	 * @param name
	 * @param type
	 * @param notNull
	 * @param defaultValue
	 * @param primaryKey
	 * @param geometry
	 */
	FeatureColumn(int index, String name, String type, boolean notNull,
			Object defaultValue, boolean primaryKey, boolean geometry) {
		this.index = index;
		this.name = name;
		this.type = type;
		this.notNull = notNull;
		this.defaultValue = defaultValue;
		this.primaryKey = primaryKey;
		this.geometry = geometry;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
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

	public boolean isGeometry() {
		return geometry;
	}

}
