package mil.nga.giat.geopackage;

/**
 * Bounding Box with longitude and latitude ranges in degrees
 * 
 * @author osbornb
 */
public class BoundingBox {

	/**
	 * Min longitude in degrees
	 */
	private double minLongitude;

	/**
	 * Max longitude in degrees
	 */
	private double maxLongitude;

	/**
	 * Min latitude in degrees
	 */
	private double minLatitude;

	/**
	 * Max latitude in degrees
	 */
	private double maxLatitude;

	/**
	 * Constructor
	 */
	public BoundingBox() {
		this(-180.0, 180.0, -90.0, 90.0);
	}

	/**
	 * Constructor
	 * 
	 * @param minLongitude
	 * @param maxLongitude
	 * @param minLatitude
	 * @param maxLatitude
	 */
	public BoundingBox(double minLongitude, double maxLongitude,
			double minLatitude, double maxLatitude) {
		this.minLongitude = minLongitude;
		this.maxLongitude = maxLongitude;
		this.minLatitude = minLatitude;
		this.maxLatitude = maxLatitude;
	}

	public double getMinLongitude() {
		return minLongitude;
	}

	public void setMinLongitude(double minLongitude) {
		this.minLongitude = minLongitude;
	}

	public double getMaxLongitude() {
		return maxLongitude;
	}

	public void setMaxLongitude(double maxLongitude) {
		this.maxLongitude = maxLongitude;
	}

	public double getMinLatitude() {
		return minLatitude;
	}

	public void setMinLatitude(double minLatitude) {
		this.minLatitude = minLatitude;
	}

	public double getMaxLatitude() {
		return maxLatitude;
	}

	public void setMaxLatitude(double maxLatitude) {
		this.maxLatitude = maxLatitude;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(maxLatitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxLongitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minLatitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minLongitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoundingBox other = (BoundingBox) obj;
		if (Double.doubleToLongBits(maxLatitude) != Double
				.doubleToLongBits(other.maxLatitude))
			return false;
		if (Double.doubleToLongBits(maxLongitude) != Double
				.doubleToLongBits(other.maxLongitude))
			return false;
		if (Double.doubleToLongBits(minLatitude) != Double
				.doubleToLongBits(other.minLatitude))
			return false;
		if (Double.doubleToLongBits(minLongitude) != Double
				.doubleToLongBits(other.minLongitude))
			return false;
		return true;
	}

}
