package mil.nga.giat.geopackage.geom.unit;

/**
 * Meter Converter
 * 
 * @author osbornb
 */
public class MeterConverter implements DistanceConverter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double toMeters(double value) {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double metersToUnits(double meters) {
		return meters;
	}

}
