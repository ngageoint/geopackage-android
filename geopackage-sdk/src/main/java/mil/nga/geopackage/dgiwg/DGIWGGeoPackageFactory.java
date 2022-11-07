package mil.nga.geopackage.dgiwg;

import android.content.Context;

import java.io.File;

import mil.nga.geopackage.GeoPackageCreator;
import mil.nga.geopackage.GeoPackageFactory;
import mil.nga.geopackage.GeoPackageManager;

/**
 * DGIWG GeoPackage Factory to get a DGIWG GeoPackage Manager
 *
 * @author osbornb
 * @since 6.7.0
 */
public class DGIWGGeoPackageFactory extends GeoPackageFactory {

    /**
     * Get a GeoPackage Manager
     *
     * @param context context
     * @return GeoPackage manager
     */
    public static DGIWGGeoPackageManager getManager(Context context) {
        Thread.currentThread().setContextClassLoader(GeoPackageManager.class.getClassLoader());
        return new DGIWGGeoPackageManager(context);
    }

    /**
     * Get a GeoPackage Manager for operating only on external GeoPackages
     *
     * @return GeoPackage manager
     */
    public static DGIWGGeoPackageManager getExternalManager() {
        return getManager(null);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path full file path
     * @return open GeoPackage
     */
    public static DGIWGGeoPackage openExternal(File path) {
        return openExternal(path, true, true);
    }

    /**
     * Open an external GeoPackage
     *
     * @param validate DGIWG validate
     * @param path     full file path
     * @return open GeoPackage
     */
    public static DGIWGGeoPackage openExternal(boolean validate, File path) {
        return openExternal(path, true, validate);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param writable true to open as writable, false as read only
     * @return open GeoPackage
     */
    public static DGIWGGeoPackage openExternal(File path, boolean writable) {
        return openExternal(path, writable, true);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param writable true to open as writable, false as read only
     * @param validate DGIWG validate
     * @return open GeoPackage
     */
    public static DGIWGGeoPackage openExternal(File path, boolean writable, boolean validate) {
        return openExternal(path.getAbsolutePath(), path.getName(), writable, validate);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path full file path
     * @return open GeoPackage
     */
    public static DGIWGGeoPackage openExternal(String path) {
        return openExternal(path, true, true);
    }

    /**
     * Open an external GeoPackage
     *
     * @param validate DGIWG validate
     * @param path     full file path
     * @return open GeoPackage
     */
    public static DGIWGGeoPackage openExternal(boolean validate, String path) {
        return openExternal(path, true, validate);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param writable true to open as writable, false as read only
     * @return open GeoPackage
     */
    public static DGIWGGeoPackage openExternal(String path, boolean writable) {
        return openExternal(path, writable, true);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param writable true to open as writable, false as read only
     * @param validate DGIWG validate
     * @return open GeoPackage
     */
    public static DGIWGGeoPackage openExternal(String path, boolean writable, boolean validate) {
        return openExternal(path, null, writable, validate);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param database database name
     * @param writable true to open as writable, false as read only
     * @param validate DGIWG validate
     * @return GeoPackage
     */
    private static DGIWGGeoPackage openExternal(String path, String database, boolean writable, boolean validate) {
        DGIWGGeoPackage geoPackage = new DGIWGGeoPackage(
                (new GeoPackageCreator()).openExternal(path, database, writable));

        if (validate) {
            DGIWGGeoPackageManager.validate(geoPackage);
        }

        return geoPackage;
    }

}
