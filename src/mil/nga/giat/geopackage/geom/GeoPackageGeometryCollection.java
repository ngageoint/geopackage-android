package mil.nga.giat.geopackage.geom;

import java.util.ArrayList;
import java.util.List;

/**
 * GeoPackage Geometry Collection
 * 
 * @author osbornb
 */
public class GeoPackageGeometryCollection<T extends GeoPackageGeometry> extends
		GeoPackageGeometry {

	/**
	 * List of geometries
	 */
	private List<T> geometries = new ArrayList<T>();

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageGeometryCollection(boolean hasZ, boolean hasM) {
		super(GeometryType.GEOMETRYCOLLECTION, hasZ, hasM);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected GeoPackageGeometryCollection(GeometryType type, boolean hasZ,
			boolean hasM) {
		super(type, hasZ, hasM);
	}

	/**
	 * Get the list of geometries
	 * 
	 * @return
	 */
	public List<T> getGeometries() {
		return geometries;
	}

	/**
	 * Set the geometries
	 * 
	 * @param geometries
	 */
	public void setGeometries(List<T> geometries) {
		this.geometries = geometries;
	}

	/**
	 * Add a geometry
	 * 
	 * @param geometry
	 */
	public void addGeometry(T geometry) {
		geometries.add(geometry);
	}

	/**
	 * Get the number of geometries in the collection
	 * 
	 * @return
	 */
	public int numGeometries() {
		return geometries.size();
	}

}
