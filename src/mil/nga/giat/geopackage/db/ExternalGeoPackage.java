package mil.nga.giat.geopackage.db;

/**
 * External GeoPackage
 * 
 * @author osbornb
 */
public class ExternalGeoPackage {

	/**
	 * GeoPackage name
	 */
	public String name;

	/**
	 * GeoPackage path
	 */
	public String path;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
