package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Multi Curve
 * 
 * @author osbornb
 */
public abstract class GeoPackageMultiCurve<T extends GeoPackageCurve> extends
		GeoPackageGeometryCollection<T> {

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected GeoPackageMultiCurve(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

}
