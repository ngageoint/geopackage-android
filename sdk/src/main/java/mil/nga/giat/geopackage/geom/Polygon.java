package mil.nga.giat.geopackage.geom;

/**
 * A restricted form of CurvePolygon where each ring is defined as a simple,
 * closed LineString.
 * 
 * @author osbornb
 */
public class Polygon extends CurvePolygon<LineString> {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public Polygon(boolean hasZ, boolean hasM) {
		super(GeometryType.POLYGON, hasZ, hasM);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected Polygon(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

}
