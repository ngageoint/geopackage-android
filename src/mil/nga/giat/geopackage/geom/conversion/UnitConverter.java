package mil.nga.giat.geopackage.geom.conversion;

/**
 * Unit converter interface
 * 
 * @author osbornb
 */
public interface UnitConverter {

	/**
	 * Convert from the value in the units to degrees
	 * 
	 * @param value
	 * @return
	 */
	public double toDegrees(double value);

	/**
	 * Convert from degrees to the unit value
	 * 
	 * @param degrees
	 * @return
	 */
	public double degreesToUnits(double degrees);

}
