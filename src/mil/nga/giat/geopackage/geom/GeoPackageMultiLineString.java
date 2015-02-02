package mil.nga.giat.geopackage.geom;

import java.util.List;

/**
 * GeoPackage Multi Line String
 * 
 * @author osbornb
 */
public class GeoPackageMultiLineString extends
		GeoPackageMultiCurve<GeoPackageLineString> {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageMultiLineString(boolean hasZ, boolean hasM) {
		super(GeoPackageGeometryType.MULTILINESTRING, hasZ, hasM);
	}

	/**
	 * Get the line strings
	 * 
	 * @return
	 */
	public List<GeoPackageLineString> getLineStrings() {
		return getGeometries();
	}

	/**
	 * Set the line string
	 * 
	 * @param lineStrings
	 */
	public void setLineStrings(List<GeoPackageLineString> lineStrings) {
		setGeometries(lineStrings);
	}

	/**
	 * Add a line string
	 * 
	 * @param lineString
	 */
	public void addLineString(GeoPackageLineString lineString) {
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
