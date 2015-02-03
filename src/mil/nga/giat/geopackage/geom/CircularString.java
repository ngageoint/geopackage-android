package mil.nga.giat.geopackage.geom;

/**
 * Circular String
 * 
 * @author osbornb
 */
public class CircularString extends LineString {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public CircularString(boolean hasZ, boolean hasM) {
		super(GeometryType.CIRCULARSTRING, hasZ, hasM);
	}

}
