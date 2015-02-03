package mil.nga.giat.geopackage.features.user;

/**
 * Feature Value wrapper to specify additional value attributes, such as a range
 * tolerance for floating point numbers
 * 
 * @author osbornb
 */
public class FeatureValue {

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
	public FeatureValue(Object value) {
		this(value, null);
	}

	/**
	 * Constructor
	 * 
	 * @param value
	 * @param tolerance
	 */
	public FeatureValue(Object value, Double tolerance) {
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
