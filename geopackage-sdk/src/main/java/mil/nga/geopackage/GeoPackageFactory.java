package mil.nga.geopackage;

import android.content.Context;

import com.j256.ormlite.logger.LocalLog;

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

}
