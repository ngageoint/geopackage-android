package mil.nga.giat.geopackage;

import android.content.Context;

/**
 * GeoPackage factory for retrieving a manager
 * 
 * @author osbornb
 */
public class GeoPackageFactory {

	/**
	 * Get a GeoPackage manager
	 * 
	 * @param context
	 * @return
	 */
	public static GeoPackageManager getManager(Context context) {
		return new GeoPackageManagerImpl(context);
	}

}
