package mil.nga.geopackage;

import android.content.Context;

import com.j256.ormlite.logger.LocalLog;

import java.io.File;

/**
 * GeoPackage Factory to get a GeoPackage Manager
 *
 * @author osbornb
 */
public class GeoPackageFactory {

    static {
        // Change the ORMLite log level
        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
    }

    /**
     * Get a GeoPackage Manager
     *
     * @param context context
     * @return GeoPackage manager
     */
    public static GeoPackageManager getManager(Context context) {
        Thread.currentThread().setContextClassLoader(GeoPackageManager.class.getClassLoader());
        return new GeoPackageManagerImpl(context);
    }

    /**
     * Get a GeoPackage Manager for operating only on external GeoPackages
     *
     * @return GeoPackage manager
     * @since 5.1.0
     */
    public static GeoPackageManager getExternalManager() {
        return getManager(null);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path full file path
     * @return open GeoPackage
     * @since 5.1.0
     */
    public static GeoPackage openExternal(File path) {
        return openExternal(path, true);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param writable true to open as writable, false as read only
     * @return 5.1.0
     */
    public static GeoPackage openExternal(File path, boolean writable) {
        return openExternal(path.getAbsolutePath(), path.getName(), writable);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path full file path
     * @return open GeoPackage
     * @since 5.1.0
     */
    public static GeoPackage openExternal(String path) {
        return openExternal(path, true);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param writable true to open as writable, false as read only
     * @return 5.1.0
     */
    public static GeoPackage openExternal(String path, boolean writable) {
        return openExternal(path, null, writable);
    }

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param database database name
     * @param writable true to open as writable, false as read only
     * @return GeoPackage
     */
    private static GeoPackage openExternal(String path, String database, boolean writable) {
        return (new GeoPackageCreator()).openExternal(path, database, writable);
    }

}
