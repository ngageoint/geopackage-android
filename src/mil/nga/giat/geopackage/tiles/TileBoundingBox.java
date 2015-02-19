package mil.nga.giat.geopackage.tiles;

/**
 * Tile Bounding Box with longitude and latitude ranges in degrees
 * 
 * @author osbornb
 */
public class TileBoundingBox {

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
	public TileBoundingBox() {
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
	public TileBoundingBox(double minLongitude, double maxLongitude,
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

}
