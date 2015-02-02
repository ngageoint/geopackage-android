package mil.nga.giat.geopackage.geom;

import java.util.List;

/**
 * GeoPackage Multi Point
 * 
 * @author osbornb
 */
public class GeoPackageMultiPoint extends
		GeoPackageGeometryCollection<GeoPackagePoint> {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageMultiPoint(boolean hasZ, boolean hasM) {
		super(GeoPackageGeometryType.MULTIPOINT, hasZ, hasM);
	}

	/**
	 * Get the points
	 * 
	 * @return
	 */
	public List<GeoPackagePoint> getPoints() {
		return getGeometries();
	}

	/**
	 * Set the points
	 * 
	 * @param points
	 */
	public void setPoints(List<GeoPackagePoint> points) {
		setGeometries(points);
	}

	/**
	 * Add a point
	 * 
	 * @param point
	 */
	public void addPoint(GeoPackagePoint point) {
		addGeometry(point);
	}

	/**
	 * Get the number of points
	 * 
	 * @return
	 */
	public int numPoints() {
		return numGeometries();
	}

}
