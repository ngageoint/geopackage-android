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

	public List<T> get() {
		return geometries;
	}

	public void set(List<T> geometries) {
		this.geometries = geometries;
	}

	public void add(T geometry) {
		geometries.add(geometry);
	}

}
