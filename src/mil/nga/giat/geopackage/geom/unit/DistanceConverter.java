package mil.nga.giat.geopackage.geom.unit;

/**
 * Distance converter interface
 * 
 * @author osbornb
 */
public interface DistanceConverter {

	/**
	 * Convert from the value in the units to meters
	 * 
	 * @param value
	 * @return
	 */
	public double toMeters(double value);

	/**
	 * Convert from meters to the unit value
	 * 
	 * @param meters
	 * @return
	 */
	public double metersToUnits(double meters);

}
