package mil.nga.giat.geopackage.geom.unit;

/**
 * Projection constants
 * 
 * @author osbornb
 */
public class ProjectionConstants {

	/**
	 * EPSG world geodetic system
	 */
	public static final long EPSG_WORLD_GEODETIC_SYSTEM = 4326;

	/**
	 * EPSG code for web mercator
	 */
	public static final long EPSG_WEB_MERCATOR = 3857;

	/**
	 * Web Mercator Latitude Range (+ and -)
	 */
	public static final double WEB_MERCATOR_LAT_RANGE = 85.0511287798066;

	/**
	 * Half the world distance in either direction
	 */
	public static double WEB_MERCATOR_HALF_WORLD_WIDTH = 20037508.342789244;

}
