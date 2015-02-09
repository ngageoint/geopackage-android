package mil.nga.giat.geopackage.extension;

import java.util.Locale;

/**
 * Extension Scope Type
 * 
 * @author osbornb
 */
public enum ExtensionScopeType {

	/**
	 * Read and Write
	 */
	READ_WRITE("read-write"),

	/**
	 * Write Only
	 */
	WRITE_ONLY("write-only");

	/**
	 * Query value
	 */
	private final String value;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	private ExtensionScopeType(String value) {
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
	public static ExtensionScopeType fromValue(String value) {
		value = value.replace("-", "_");
		return ExtensionScopeType.valueOf(value.toUpperCase(Locale.US));
	}

}
