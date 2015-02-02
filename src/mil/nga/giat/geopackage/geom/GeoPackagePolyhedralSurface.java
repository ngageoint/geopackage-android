package mil.nga.giat.geopackage.geom;

import java.util.ArrayList;
import java.util.List;

/**
 * GeoPackage Polyhedral Surface
 * 
 * @author osbornb
 */
public class GeoPackagePolyhedralSurface extends GeoPackageSurface {

	/**
	 * List of polygons
	 */
	private List<GeoPackagePolygon> polygons = new ArrayList<GeoPackagePolygon>();

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackagePolyhedralSurface(boolean hasZ, boolean hasM) {
		super(GeoPackageGeometryType.POLYHEDRALSURFACE, hasZ, hasM);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected GeoPackagePolyhedralSurface(GeoPackageGeometryType type, boolean hasZ,
			boolean hasM) {
		super(type, hasZ, hasM);
	}

	public List<GeoPackagePolygon> getPolygons() {
		return polygons;
	}

	public void setPolygons(List<GeoPackagePolygon> polygons) {
		this.polygons = polygons;
	}

	public void addPolygon(GeoPackagePolygon polygon) {
		polygons.add(polygon);
	}

	/**
	 * Get the number of polygons
	 * 
	 * @return
	 */
	public int numPolygons() {
		return polygons.size();
	}

}
