package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Polygon
 * 
 * @author osbornb
 */
public class GeoPackagePolygon extends
		GeoPackageCurvePolygon<GeoPackageLineString> {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackagePolygon(boolean hasZ, boolean hasM) {
		super(GeoPackageGeometryType.POLYGON, hasZ, hasM);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected GeoPackagePolygon(GeoPackageGeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

}
