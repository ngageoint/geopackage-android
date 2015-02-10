package mil.nga.giat.geopackage.geom.conversion;

/**
 * Degrees Converter
 * 
 * @author osbornb
 */
public class DegreesConverter implements UnitConverter {

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
