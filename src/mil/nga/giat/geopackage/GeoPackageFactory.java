package mil.nga.giat.geopackage;

import android.content.Context;

/**
 * Geo Package factory for retrieving a manager
 * 
 * @author osbornb
 */
public class GeoPackageFactory {

	/**
	 * Get a Geo Package manager
	 * 
	 * @param context
	 * @return
	 */
	public static GeoPackageManager getManager(Context context) {
		return new GeoPackageManagerImpl(context);
	}

}
