package mil.nga.giat.geopackage.geom;

/**
 * TIN
 * 
 * @author osbornb
 */
public class TIN extends PolyhedralSurface {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public TIN(boolean hasZ, boolean hasM) {
		super(GeometryType.TIN, hasZ, hasM);
	}

}
