package mil.nga.giat.geopackage.geom;

import java.util.ArrayList;
import java.util.List;

/**
 * GeoPackage Compound Curve
 * 
 * @author osbornb
 */
public class GeoPackageCompoundCurve extends GeoPackageCurve {

	/**
	 * List of line strings
	 */
	private List<GeoPackageLineString> lineStrings = new ArrayList<GeoPackageLineString>();

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageCompoundCurve(boolean hasZ, boolean hasM) {
		super(GeoPackageGeometryType.COMPOUNDCURVE, hasZ, hasM);
	}

	public List<GeoPackageLineString> getLineStrings() {
		return lineStrings;
	}

	public void setLineStrings(List<GeoPackageLineString> lineStrings) {
		this.lineStrings = lineStrings;
	}

	public void addLineString(GeoPackageLineString lineString) {
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
