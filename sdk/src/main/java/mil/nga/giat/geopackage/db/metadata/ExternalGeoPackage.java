package mil.nga.giat.geopackage.db.metadata;

/**
 * External GeoPackage. A GeoPackage that is linked instead of locally copied into the app space.
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

    /**
     * Get the name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the path
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the path
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

}
