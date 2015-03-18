package mil.nga.giat.geopackage.factory;

import mil.nga.giat.geopackage.GeoPackageException;
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
	 * Initialize the GeoPackage SDK
	 */
	public static void initialize(Context context) {
		if (!initialized) {
			// Initialize the projection factory for retrieving projections
			AndroidProjectionParameterRetriever projectionRetriever = new AndroidProjectionParameterRetriever(
					context);
			ProjectionFactory.initialize(projectionRetriever);
			initialized = true;
		}
	}

	/**
	 * Get a GeoPackage Manager
	 * 
	 * @param context
	 * @return
	 */
	public static GeoPackageManager getManager(Context context) {
		if (!initialized) {
			throw new GeoPackageException(
					GeoPackageFactory.class.getSimpleName()
							+ " has not been initialized");
		}
		return new GeoPackageManagerImpl(context);
	}

}
