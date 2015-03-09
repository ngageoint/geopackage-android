package mil.nga.giat.geopackage.geom.unit;

import android.app.Activity;
import android.util.Log;

/**
 * Retrieves the EPSG projection parameter String from resources
 * 
 * @author osbornb
 */
public class AndroidProjectionParameterRetriever implements
		ProjectionParameterRetriever {

	/**
	 * Resource prefix
	 */
	private static final String RESOURCE_PREFIX = "projection_epsg_";

	/**
	 * Activity
	 */
	private final Activity activity;

	/**
	 * Constructor
	 * 
	 * @param activity
	 */
	public AndroidProjectionParameterRetriever(Activity activity) {
		this.activity = activity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProjection(long epsg) {
		String projectionString = null;
		if (epsg > 0) {
			try {
				projectionString = activity.getString(activity.getResources()
						.getIdentifier(RESOURCE_PREFIX + epsg, "string",
								activity.getPackageName()));
			} catch (Exception e) {
				Log.w(AndroidProjectionParameterRetriever.class.getSimpleName(),
						"No Projection Found for EPSG Code: " + epsg);
			}
		}
		if (projectionString == null) {
			projectionString = activity
					.getString(activity
							.getResources()
							.getIdentifier(
									RESOURCE_PREFIX
											+ ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM,
									"string", activity.getPackageName()));
		}
		return projectionString;
	}

}
