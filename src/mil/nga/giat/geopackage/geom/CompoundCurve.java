package mil.nga.giat.geopackage.geom;

import java.util.ArrayList;
import java.util.List;

/**
 * Compound Curve, Curve sub type
 * 
 * @author osbornb
 */
public class CompoundCurve extends Curve {

	/**
	 * List of line strings
	 */
	private List<LineString> lineStrings = new ArrayList<LineString>();

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public CompoundCurve(boolean hasZ, boolean hasM) {
		super(GeometryType.COMPOUNDCURVE, hasZ, hasM);
	}

	public List<LineString> getLineStrings() {
		return lineStrings;
	}

	public void setLineStrings(List<LineString> lineStrings) {
		this.lineStrings = lineStrings;
	}

	public void addLineString(LineString lineString) {
		lineStrings.add(lineString);
	}

	/**
	 * Get the number of line strings
	 * 
	 * @return
	 */
	public int numLineStrings() {
		return lineStrings.size();
	}

}
