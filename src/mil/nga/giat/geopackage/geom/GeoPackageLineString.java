package mil.nga.giat.geopackage.geom;

import java.util.ArrayList;
import java.util.List;

/**
 * GeoPackage Line String
 * 
 * @author osbornb
 */
public class GeoPackageLineString extends GeoPackageCurve {

	/**
	 * List of points
	 */
	private List<GeoPackagePoint> points = new ArrayList<GeoPackagePoint>();

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageLineString(boolean hasZ, boolean hasM) {
		super(GeometryType.LINESTRING, hasZ, hasM);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected GeoPackageLineString(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

	public List<GeoPackagePoint> getPoints() {
		return points;
	}

	public void setPoints(List<GeoPackagePoint> points) {
		this.points = points;
	}

	public void addPoint(GeoPackagePoint point) {
		points.add(point);
	}

}
