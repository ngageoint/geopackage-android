package mil.nga.giat.geopackage.factory;

import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.geom.unit.AndroidProjectionParameterRetriever;
import mil.nga.giat.geopackage.geom.unit.ProjectionFactory;
import android.content.Context;

/**
 * GeoPackage Factory to get a GeoPackage Manager
 * 
 * @author osbornb
 */
public class GeoPackageFactory {

	/**
	 * Initialized flag
	 */
	private static boolean initialized = false;

	/**
	 * Get a GeoPackage Manager
	 * 
	 * @param context
	 * @return
	 */
	public static GeoPackageManager getManager(Context context) {
		initialize(context);
		return new GeoPackageManagerImpl(context);
	}

	/**
	 * Initialize the GeoPackage SDK
	 */
	private static void initialize(Context context) {
		if (!initialized) {
			// Initialize the projection factory for retrieving projections
			AndroidProjectionParameterRetriever projectionRetriever = new AndroidProjectionParameterRetriever(
					context);
			ProjectionFactory.initialize(projectionRetriever);
			initialized = true;
		}
	}

}
