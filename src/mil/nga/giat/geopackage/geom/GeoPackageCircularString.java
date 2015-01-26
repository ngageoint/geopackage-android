package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Circular String
 * 
 * @author osbornb
 */
public class GeoPackageCircularString extends GeoPackageLineString {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageCircularString(boolean hasZ, boolean hasM) {
		super(GeometryType.CIRCULARSTRING, hasZ, hasM);
	}

}
