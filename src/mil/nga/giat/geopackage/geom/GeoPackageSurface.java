package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Surface
 * 
 * @author osbornb
 */
public abstract class GeoPackageSurface extends GeoPackageGeometry {

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected GeoPackageSurface(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

}
