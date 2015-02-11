package mil.nga.giat.geopackage.geom.unit;

/**
 * Degree Converter
 * 
 * @author osbornb
 */
public class DegreeConverter implements CoordinateConverter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double toDegrees(double value) {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double degreesToUnits(double degrees) {
		return degrees;
	}

}
