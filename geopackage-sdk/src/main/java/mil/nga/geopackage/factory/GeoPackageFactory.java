package mil.nga.geopackage.factory;

import android.content.Context;

import mil.nga.geopackage.GeoPackageManager;

/**
 * GeoPackage Factory to get a GeoPackage Manager
 *
 * @author osbornb
 */
public class GeoPackageFactory {

    /**
     * Get a GeoPackage Manager
     *
     * @param context
     * @return
     */
    public static GeoPackageManager getManager(Context context) {
        Thread.currentThread().setContextClassLoader(GeoPackageManager.class.getClassLoader());
        return new GeoPackageManagerImpl(context);
    }

}
