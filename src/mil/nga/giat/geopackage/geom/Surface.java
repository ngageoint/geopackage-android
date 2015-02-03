package mil.nga.giat.geopackage.geom;

/**
 * Surface
 * 
 * @author osbornb
 */
public abstract class Surface extends Geometry {

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected Surface(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

}
