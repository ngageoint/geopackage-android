package mil.nga.giat.geopackage.geom;

import java.util.ArrayList;
import java.util.List;

/**
 * A planar surface defined by an exterior ring and zero or more interior ring.
 * Each ring is defined by a Curve instance.
 * 
 * @author osbornb
 */
public class CurvePolygon<T extends Curve> extends Surface {

	/**
	 * List of rings
	 */
	private List<T> rings = new ArrayList<T>();

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public CurvePolygon(boolean hasZ, boolean hasM) {
		super(GeometryType.CURVEPOLYGON, hasZ, hasM);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param hasZ
	 * @param hasM
	 */
	protected CurvePolygon(GeometryType type, boolean hasZ, boolean hasM) {
		super(type, hasZ, hasM);
	}

	public List<T> getRings() {
		return rings;
	}

	public void setRings(List<T> rings) {
		this.rings = rings;
	}

	public void addRing(T ring) {
		rings.add(ring);
	}

	/**
	 * Get the number of rings
	 * 
	 * @return
	 */
	public int numRings() {
		return rings.size();
	}

}
