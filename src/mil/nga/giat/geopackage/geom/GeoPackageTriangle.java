package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Triangle
 * 
 * @author osbornb
 */
public class GeoPackageTriangle extends GeoPackagePolygon {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageTriangle(boolean hasZ, boolean hasM) {
		super(GeometryType.TRIANGLE, hasZ, hasM);
	}

}
