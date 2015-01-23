package mil.nga.giat.geopackage.factory;

import mil.nga.giat.geopackage.GeoPackageManager;
import android.content.Context;

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
		return new GeoPackageManagerImpl(context);
	}

}
