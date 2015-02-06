package mil.nga.giat.geopackage.schema.constraints;

/**
 * Enumeration of Data Column Constraint Types
 * 
 * @author osbornb
 * 
 */
public enum DataColumnConstraintType {

	/**
	 * Value range
	 */
	RANGE,

	/**
	 * Enumerated values
	 */
	ENUM,

	/**
	 * Pattern matching
	 */
	GLOB;

}
