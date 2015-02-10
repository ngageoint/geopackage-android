package mil.nga.giat.geopackage.geom;

import java.util.List;

/**
 * A restricted form of MultiCurve where each Curve in the collection must be of
 * type LineString.
 * 
 * @author osbornb
 */
public class MultiLineString extends MultiCurve<LineString> {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public MultiLineString(boolean hasZ, boolean hasM) {
		super(GeometryType.MULTILINESTRING, hasZ, hasM);
	}

	/**
	 * Get the line strings
	 * 
	 * @return
	 */
	public List<LineString> getLineStrings() {
		return getGeometries();
	}

	/**
	 * Set the line string
	 * 
	 * @param lineStrings
	 */
	public void setLineStrings(List<LineString> lineStrings) {
		setGeometries(lineStrings);
	}

	/**
	 * Add a line string
	 * 
	 * @param lineString
	 */
	public void addLineString(LineString lineString) {
		addGeometry(lineString);
	}

	/**
	 * Get the number of line strings
	 * 
	 * @return
	 */
	public int numLineStrings() {
		return numGeometries();
	}

}
