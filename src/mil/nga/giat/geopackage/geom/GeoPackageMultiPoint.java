package mil.nga.giat.geopackage.geom;

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
		super(GeometryType.MULTIPOINT, hasZ, hasM);
	}

}
