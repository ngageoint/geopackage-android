package mil.nga.giat.geopackage.metadata.reference;

import java.util.Locale;

/**
 * Reference Scope Type
 * 
 * @author osbornb
 */
public enum ReferenceScopeType {

	/**
	 * Geopackage
	 */
	GEOPACKAGE("geopackage"),

	/**
	 * Table
	 */
	TABLE("table"),

	/**
	 * Column
	 */
	COLUMN("column"),

	/**
	 * Row
	 */
	ROW("row"),

	/**
	 * Row and column
	 */
	ROW_COL("row/col");

	/**
	 * Query value
	 */
	private final String value;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	private ReferenceScopeType(String value) {
		this.value = value;
	}

	/**
	 * Get the value
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get the type from the value
	 * 
	 * @param value
	 * @return
	 */
	public static ReferenceScopeType fromValue(String value) {
		value = value.replace("/", "_");
		return ReferenceScopeType.valueOf(value.toUpperCase(Locale.US));
	}

}
