package mil.nga.giat.geopackage.db;

import java.util.Locale;

import mil.nga.giat.geopackage.geom.GeometryType;

/**
 * GeoPackage Data Types (non Geometry) for database columns as defined in
 * Requirement 5 of the GeoPackage spec. See {@link GeometryType} for Geometry
 * Types
 * 
 * @author osbornb
 */
public enum GeoPackageDataType {

	/**
	 * A boolean value representing true or false. Stored as SQLite INTEGER with
	 * value 0 for false or 1 for true
	 */
	BOOLEAN(Boolean.class),

	/**
	 * 8-bit signed two’s complement integer. Stored as SQLite INTEGER with
	 * values in the range [-128, 127]
	 */
	TINYINT(Byte.class),

	/**
	 * 16-bit signed two’s complement integer. Stored as SQLite INTEGER with
	 * values in the range [-32768, 32767]
	 */
	SMALLINT(Short.class),

	/**
	 * 32-bit signed two’s complement integer. Stored as SQLite INTEGER with
	 * values in the range [-2147483648, 2147483647]
	 */
	MEDIUMINT(Integer.class),

	/**
	 * 64-bit signed two’s complement integer. Stored as SQLite INTEGER with
	 * values in the range [-9223372036854775808, 9223372036854775807]
	 */
	INT(Long.class),

	/**
	 * 64-bit signed two’s complement integer. Stored as SQLite INTEGER with
	 * values in the range [-9223372036854775808, 9223372036854775807]
	 */
	INTEGER(Long.class),

	/**
	 * 32-bit IEEE floating point number. Stored as SQLite REAL limited to
	 * values that can be represented as a 4-byte IEEE floating point number
	 */
	FLOAT(Float.class),

	/**
	 * 64-bit IEEE floating point number. Stored as SQLite REAL
	 */
	DOUBLE(Double.class),

	/**
	 * 64-bit IEEE floating point number. Stored as SQLite REAL
	 */
	REAL(Double.class),

	/**
	 * TEXT{(maxchar_count)}: Variable length string encoded in either UTF-8 or
	 * UTF-16, determined by PRAGMA encoding; see
	 * http://www.sqlite.org/pragma.html#pragma_encoding. The optional
	 * maxchar_count defines the maximum number of characters in the string. If
	 * not specified, the length is unbounded. The count is provided for
	 * informational purposes, and applications MAY choose to truncate longer
	 * strings if encountered. When present, it is best practice for
	 * applications to adhere to the character count. Stored as SQLite TEXT
	 */
	TEXT(String.class),

	/**
	 * BLOB{(max_size)}: Variable length binary data. The optional max_size
	 * defines the maximum number of bytes in the blob. If not specified, the
	 * length is unbounded. The size is provided for informational purposes.
	 * When present, it is best practice for applications adhere to the maximum
	 * blob size. Stored as SQLite BLOB
	 */
	BLOB(byte[].class),

	/**
	 * ISO-8601 date string in the form YYYY-MM-DD encoded in either UTF-8 or
	 * UTF-16. See TEXT. Stored as SQLite TEXT
	 */
	DATE(String.class),

	/**
	 * ISO-8601 date/time string in the form YYYY-MM-DDTHH:MM:SS.SSSZ with T
	 * separator character and Z suffix for coordinated universal time (UTC)
	 * encoded in either UTF-8 or UTF-16. See TEXT. Stored as SQLite TEXT
	 */
	DATETIME(String.class);

	/**
	 * Java class type
	 */
	private final Class<?> classType;

	/**
	 * Constructor
	 * 
	 * @param classType
	 */
	private GeoPackageDataType(Class<?> classType) {
		this.classType = classType;
	}

	/**
	 * Get the Java class type
	 * 
	 * @return
	 */
	public Class<?> getClassType() {
		return classType;
	}

	/**
	 * Get the Data Type from the name, ignoring case
	 * 
	 * @param name
	 * @return
	 */
	public static GeoPackageDataType fromName(String name) {
		return valueOf(name.toUpperCase(Locale.US));
	}

}
