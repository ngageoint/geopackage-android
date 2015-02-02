package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage TIN
 * 
 * @author osbornb
 */
public class GeoPackageTIN extends GeoPackagePolyhedralSurface {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageTIN(boolean hasZ, boolean hasM) {
		super(GeoPackageGeometryType.TIN, hasZ, hasM);
	}

}
