package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Point
 * 
 * @author osbornb
 */
public class GeoPackagePoint extends GeoPackageGeometry {

	/**
	 * X coordinate
	 */
	private double x;

	/**
	 * Y coordinate
	 */
	private double y;

	/**
	 * Constructor
	 * 
	 * @param x
	 * @param y
	 */
	public GeoPackagePoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

}
