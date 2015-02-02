package mil.nga.giat.geopackage.geom;

import java.util.List;

/**
 * GeoPackage Multi Polygon
 * 
 * @author osbornb
 */
public class GeoPackageMultiPolygon extends
		GeoPackageMultiSurface<GeoPackagePolygon> {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageMultiPolygon(boolean hasZ, boolean hasM) {
		super(GeoPackageGeometryType.MULTIPOLYGON, hasZ, hasM);
	}

	/**
	 * Get the polygons
	 * 
	 * @return
	 */
	public List<GeoPackagePolygon> getPolygons() {
		return getGeometries();
	}

	/**
	 * Set the polygons
	 * 
	 * @param polygons
	 */
	public void setPolygons(List<GeoPackagePolygon> polygons) {
		setGeometries(polygons);
	}

	/**
	 * Add a polygon
	 * 
	 * @param polygon
	 */
	public void addPolygon(GeoPackagePolygon polygon) {
		addGeometry(polygon);
	}

	/**
	 * Get the number of polygons
	 * 
	 * @return
	 */
	public int numPolygons() {
		return numGeometries();
	}

}
