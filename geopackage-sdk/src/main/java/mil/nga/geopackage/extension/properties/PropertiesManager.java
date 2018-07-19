package mil.nga.geopackage.extension.properties;

import java.util.Collection;

import mil.nga.geopackage.GeoPackage;

/**
 * Properties Manager, utilizes the Properties Extension on a collection of
 * GeoPackages
 *
 * @author osbornb
 * @since 3.0.2
 */
public class PropertiesManager extends PropertiesManagerCore<GeoPackage> {

    /**
     * Constructor
     */
    public PropertiesManager() {

    }

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     */
    public PropertiesManager(GeoPackage geoPackage) {
        super(geoPackage);
    }

    /**
     * Constructor
     *
     * @param geoPackages collection of GeoPackages
     */
    public PropertiesManager(Collection<GeoPackage> geoPackages) {
        super(geoPackages);
    }

    /**
     * Create a properties extension from the GeoPackage
     *
     * @param geoPackage GeoPackage
     * @return properties extension
     */
    public PropertiesCoreExtension<GeoPackage, ?, ?, ?> getPropertiesExtension(
            GeoPackage geoPackage) {
        return new PropertiesExtension(geoPackage);
    }

}
