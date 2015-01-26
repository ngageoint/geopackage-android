package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Geometry
 * 
 * @author osbornb
 */
public abstract class GeoPackageGeometry {

	/**
	 * Geometry type
	 */
	private final GeometryType geometryType;

	/**
	 * Has z coordinates
	 */
	private final boolean hasZ;

	/**
	 * Has m values
	 */
	private final boolean hasM;

	/**
	 * Constructor
	 * 
	 * @param geometryType
	 * @param hasZ
	 * @param hasM
	 */
	protected GeoPackageGeometry(GeometryType geometryType, boolean hasZ,
			boolean hasM) {
		this.geometryType = geometryType;
		this.hasZ = hasZ;
		this.hasM = hasM;
	}

	public GeometryType getGeometryType() {
		return geometryType;
	}

	public boolean hasZ() {
		return hasZ;
	}

	public boolean hasM() {
		return hasM;
	}

}
