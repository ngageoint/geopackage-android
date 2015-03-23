package mil.nga.giat.geopackage.user;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.db.GeoPackageDataType;

/**
 * Metadata about a single column from a user table
 * 
 * @author osbornb
 */
public abstract class UserColumn implements Comparable<UserColumn> {

	/**
	 * Column index
	 */
	private final int index;

	/**
	 * Column name
	 */
	private final String name;

	/**
	 * Max size
	 */
	private final Long max;

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
	 * Data type if not a geometry column
	 */
	private final GeoPackageDataType dataType;

	/**
	 * Constructor
	 * 
	 * @param index
	 * @param name
	 * @param max
	 * @param notNull
	 * @param defaultValue
	 * @param primaryKey
	 * @param dataType
	 */
	protected UserColumn(int index, String name, GeoPackageDataType dataType,
			Long max, boolean notNull, Object defaultValue, boolean primaryKey) {
		this.index = index;
		this.name = name;
		this.max = max;
		this.notNull = notNull;
		this.defaultValue = defaultValue;
		this.primaryKey = primaryKey;
		this.dataType = dataType;

		validateMax();
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public Long getMax() {
		return max;
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
	 * When not a geometry column, gets the data type
	 * 
	 * @return
	 */
	public GeoPackageDataType getDataType() {
		return dataType;
	}

	/**
	 * Get the database type name
	 * 
	 * @return
	 */
	public String getTypeName() {
		String type = null;
		if (dataType != null) {
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
	public int compareTo(UserColumn another) {
		return index - another.index;
	}

	/**
	 * Validate that if max is set, the data type is text or blob
	 */
	private void validateMax() {

		if (max != null) {
			if (dataType == null) {
				throw new GeoPackageException(
						"Column max is only supported for data typed columns. column: "
								+ name + ", max: " + max);
			} else if (dataType != GeoPackageDataType.TEXT
					&& dataType != GeoPackageDataType.BLOB) {
				throw new GeoPackageException(
						"Column max is only supported for "
								+ GeoPackageDataType.TEXT.name() + " and "
								+ GeoPackageDataType.BLOB.name()
								+ " columns. column: " + name + ", max: " + max
								+ ", type: " + dataType.name());
			}
		}

	}

}
