package mil.nga.giat.geopackage.geom;

/**
 * Multi Curve
 * 
 * @author osbornb
 */
public abstract class MultiCurve<T extends Curve> extends
		GeometryCollection<T> {

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected MultiCurve(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

}
