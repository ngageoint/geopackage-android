package mil.nga.giat.geopackage.geom.unit;

import android.content.Context;
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
	 * Context
	 */
	private final Context context;

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public AndroidProjectionParameterRetriever(Context context) {
		this.context = context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProjection(long epsg) {
		String projectionString = null;
		if (epsg > 0) {
			try {
				projectionString = context.getString(context.getResources()
						.getIdentifier(RESOURCE_PREFIX + epsg, "string",
								context.getPackageName()));
			} catch (Exception e) {
				Log.w(AndroidProjectionParameterRetriever.class.getSimpleName(),
						"No Projection Found for EPSG Code: " + epsg);
			}
		}
		if (projectionString == null) {
			projectionString = context
					.getString(context
							.getResources()
							.getIdentifier(
									RESOURCE_PREFIX
											+ ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM,
									"string", context.getPackageName()));
		}
		return projectionString;
	}

}
