package mil.nga.giat.geopackage.geom;

/**
 * A restricted form of GeometryCollection where each Geometry in the collection
 * must be of type Surface.
 * 
 * @author osbornb
 */
public abstract class MultiSurface<T extends Surface> extends
		GeometryCollection<T> {

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected MultiSurface(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

}
