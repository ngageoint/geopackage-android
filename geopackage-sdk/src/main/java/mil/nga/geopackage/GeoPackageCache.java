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
     * @param manager
     */
    public GeoPackageCache(GeoPackageManager manager) {
        this.manager = manager;
    }

    /**
     * Get the cached GeoPackage or open and cache the GeoPackage
     *
     * @param name
     * @return
     */
    public GeoPackage getOrOpen(String name) {
        GeoPackage geoPackage = get(name);
        if (geoPackage == null) {
            geoPackage = manager.open(name);
            add(geoPackage);
        }
        return geoPackage;
    }

}
