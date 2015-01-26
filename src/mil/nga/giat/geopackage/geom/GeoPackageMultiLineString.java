package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Multi Line String
 * 
 * @author osbornb
 */
public class GeoPackageMultiLineString extends
		GeoPackageMultiCurve<GeoPackageLineString> {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageMultiLineString(boolean hasZ, boolean hasM) {
		super(GeometryType.MULTILINESTRING, hasZ, hasM);
	}

}
