package mil.nga.geopackage.extension.nga.properties;

import java.util.Collection;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageCache;

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
     * Constructor
     *
     * @param cache GeoPackage cache
     */
    public PropertiesManager(GeoPackageCache cache) {
        super(cache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertiesExtension getPropertiesExtension(
            GeoPackage geoPackage) {
        return new PropertiesExtension(geoPackage);
    }

}
