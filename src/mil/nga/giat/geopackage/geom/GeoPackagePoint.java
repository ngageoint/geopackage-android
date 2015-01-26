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
	 * Z coordinate
	 */
	private Double z;

	/**
	 * M value
	 */
	private Double m;

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 * @param x
	 * @param y
	 */
	public GeoPackagePoint(boolean hasZ, boolean hasM, double x, double y) {
		super(GeometryType.POINT, hasZ, hasM);
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

	public Double getZ() {
		return z;
	}

	public void setZ(Double z) {
		this.z = z;
	}

	public Double getM() {
		return m;
	}

	public void setM(Double m) {
		this.m = m;
	}

}
