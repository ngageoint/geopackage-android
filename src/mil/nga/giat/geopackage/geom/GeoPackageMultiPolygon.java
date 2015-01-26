package mil.nga.giat.geopackage.geom;

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
		super(GeometryType.MULTIPOLYGON, hasZ, hasM);
	}

}
