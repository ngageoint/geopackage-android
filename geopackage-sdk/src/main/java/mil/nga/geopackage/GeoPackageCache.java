package mil.nga.geopackage;

/**
 * GeoPackage Cache
 *
 * @author osbornb
 */
public class GeoPackageCache extends GeoPackageCoreCache<GeoPackage> {

    /**
     * GeoPackage manager
     */
    private GeoPackageManager manager;

    /**
     * Constructor
     *
     * @param manager GeoPackage manager
     */
    public GeoPackageCache(GeoPackageManager manager) {
        this.manager = manager;
    }

    /**
     * Get the cached GeoPackage or open and cache the GeoPackage
     *
     * @param name GeoPackage name
     * @return GeoPackage
     */
    public GeoPackage getOrOpen(String name) {
        return getOrOpen(name, true);
    }

    /**
     * Get the cached GeoPackage or open and cache the GeoPackage
     *
     * @param name     GeoPackage name
     * @param writable writable true to open as writable, false as read only
     * @return GeoPackage
     * @since 3.0.3
     */
    public GeoPackage getOrOpen(String name, boolean writable) {
        GeoPackage geoPackage = get(name);
        if (geoPackage == null) {
            geoPackage = manager.open(name, writable);
            add(geoPackage);
        }
        return geoPackage;
    }

}
