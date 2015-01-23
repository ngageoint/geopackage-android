package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Geometry
 * 
 * @author osbornb
 */
public class GeoPackageGeometry {

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
	 */
	public GeoPackageGeometry() {
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
