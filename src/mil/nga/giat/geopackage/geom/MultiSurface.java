package mil.nga.giat.geopackage.geom;

/**
 * Multi Surface
 * 
 * @author osbornb
 */
public abstract class MultiSurface<T extends Surface>
		extends GeometryCollection<T> {

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected MultiSurface(GeometryType type, boolean hasZ,
			boolean hasM) {
		super(type, hasZ, hasM);
	}

}
