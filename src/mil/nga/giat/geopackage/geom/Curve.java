package mil.nga.giat.geopackage.geom;

/**
 * Curve
 * 
 * @author osbornb
 */
public abstract class Curve extends Geometry {

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected Curve(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

}
