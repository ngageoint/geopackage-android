package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Multi Surface
 * 
 * @author osbornb
 */
public abstract class GeoPackageMultiSurface<T extends GeoPackageSurface>
		extends GeoPackageGeometryCollection<T> {

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected GeoPackageMultiSurface(GeoPackageGeometryType type, boolean hasZ,
			boolean hasM) {
		super(type, hasZ, hasM);
	}

}
