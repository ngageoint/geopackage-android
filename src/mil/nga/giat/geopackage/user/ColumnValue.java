package mil.nga.giat.geopackage.user;

/**
 * Column Value wrapper to specify additional value attributes, such as a range
 * tolerance for floating point numbers
 * 
 * @author osbornb
 */
public class ColumnValue {

	/**
	 * Value
	 */
	private final Object value;

	/**
	 * Value tolerance
	 */
	private final Double tolerance;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	public ColumnValue(Object value) {
		this(value, null);
	}

	/**
	 * Constructor
	 * 
	 * @param value
	 * @param tolerance
	 */
	public ColumnValue(Object value, Double tolerance) {
		this.value = value;
		this.tolerance = tolerance;
	}

	public Object getValue() {
		return value;
	}

	public Double getTolerance() {
		return tolerance;
	}

}
