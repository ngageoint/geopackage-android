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
     * @since 3.1.0
     */
    public GeoPackage getOrOpen(String name, boolean writable) {
        return getOrOpen(name, writable, true);
    }

    /**
     * Get the cached GeoPackage or open the GeoPackage file without caching it
     *
     * @param name GeoPackage name
     * @return GeoPackage
     * @since 3.1.0
     */
    public GeoPackage getOrNoCacheOpen(String name) {
        return getOrNoCacheOpen(name, true);
    }

    /**
     * Get the cached GeoPackage or open the GeoPackage file without caching it
     *
     * @param name     GeoPackage name
     * @param writable writable true to open as writable, false as read only
     * @return GeoPackage
     * @since 3.1.0
     */
    public GeoPackage getOrNoCacheOpen(String name, boolean writable) {
        return getOrOpen(name, writable, false);
    }

    /**
     * Get the cached GeoPackage or open and cache the GeoPackage
     *
     * @param name     GeoPackage name
     * @param writable writable true to open as writable, false as read only
     * @param cache    true to cache opened GeoPackages
     * @return GeoPackage
     */
    private GeoPackage getOrOpen(String name, boolean writable, boolean cache) {
        GeoPackage geoPackage = get(name);
        if (geoPackage == null) {
            geoPackage = manager.open(name, writable);
            if (cache) {
                add(geoPackage);
            }
        }
        return geoPackage;
    }

}
