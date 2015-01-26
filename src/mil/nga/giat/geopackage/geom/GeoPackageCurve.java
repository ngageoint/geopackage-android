package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Curve
 * 
 * @author osbornb
 */
public abstract class GeoPackageCurve extends GeoPackageGeometry {

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected GeoPackageCurve(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

}
