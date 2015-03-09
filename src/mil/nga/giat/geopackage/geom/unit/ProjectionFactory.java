package mil.nga.giat.geopackage.geom.unit;

import java.util.HashMap;
import java.util.Map;

import mil.nga.giat.geopackage.GeoPackageException;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;

/**
 * Projection factory for coordinate projections and transformations
 * 
 * @author osbornb
 */
public class ProjectionFactory {

	/**
	 * Used to retrieve the projection parameters for an EPSG code
	 */
	private static ProjectionParameterRetriever parameterRetriever;

	/**
	 * Mapping of EPSG projection codes to projections
	 */
	private static Map<Long, Projection> projections = new HashMap<Long, Projection>();

	/**
	 * CRS Factory
	 */
	private static final CRSFactory csFactory = new CRSFactory();

	/**
	 * Initialize the projection factory
	 * 
	 * @param parameterRetriever
	 */
	public static void initialize(ProjectionParameterRetriever retriever) {
		parameterRetriever = retriever;
	}

	/**
	 * Get the projection for the EPSG code
	 * 
	 * @param epsg
	 * @return
	 */
	public static Projection getProjection(long epsg) {
		Projection projection = projections.get(epsg);
		if (projection == null) {

			if (parameterRetriever == null) {
				throw new GeoPackageException(
						ProjectionFactory.class.getSimpleName()
								+ " not initialized");
			}

			String parameters = parameterRetriever.getProjection(epsg);

			CoordinateReferenceSystem crs = csFactory.createFromParameters(
					String.valueOf(epsg), parameters);
			projection = new Projection(epsg, crs);

			projections.put(epsg, projection);
		}
		return projection;
	}

	// public static void test(Activity activity) {
	//
	// CRSFactory csFactory = new CRSFactory();
	// CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
	//
	// CoordinateReferenceSystem crs3395 = csFactory.createFromParameters(
	// "EPSG:3395", getProjection(activity, 3395));
	// // CoordinateReferenceSystem crs3395 = csFactory
	// // .createFromParameters("EPSG:3395",
	// //
	// "+proj=merc +lon_0=0 +k=1 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs");
	//
	// CoordinateReferenceSystem crs4326 = csFactory.createFromParameters(
	// "EPSG:4326", getProjection(activity, 4326));
	// // CoordinateReferenceSystem crs4326 = csFactory.createFromParameters(
	// // "EPSG:4326", "+proj=longlat +datum=WGS84 +no_defs");
	//
	// CoordinateReferenceSystem crs3857 = csFactory.createFromParameters(
	// "EPSG:3857", getProjection(activity, 3857));
	// // CoordinateReferenceSystem crs3857 = csFactory
	// // .createFromParameters(
	// // "EPSG:3857",
	// //
	// "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs");
	//
	// CoordinateTransform crs4326_to_crs3395 = ctFactory.createTransform(
	// crs4326, crs3395);
	// CoordinateTransform crs3395_to_crs4326 = ctFactory.createTransform(
	// crs3395, crs4326);
	// CoordinateTransform crs4326_to_crs3857 = ctFactory.createTransform(
	// crs4326, crs3857);
	// CoordinateTransform crs3857_to_crs4326 = ctFactory.createTransform(
	// crs3857, crs4326);
	//
	// ProjCoordinate wgs84 = new ProjCoordinate(120, 45);
	// ProjCoordinate wgs84Merc = new ProjCoordinate();
	// crs4326_to_crs3395.transform(wgs84, wgs84Merc);
	//
	// ProjCoordinate wgs84Merc2 = new ProjCoordinate(wgs84Merc.x, wgs84Merc.y);
	// ProjCoordinate wgs84_2 = new ProjCoordinate();
	// crs3395_to_crs4326.transform(wgs84Merc2, wgs84_2);
	//
	// ProjCoordinate wgs84_3 = new ProjCoordinate(wgs84_2.x, wgs84_2.y);
	// ProjCoordinate merc = new ProjCoordinate();
	// crs4326_to_crs3857.transform(wgs84_3, merc);
	//
	// ProjCoordinate merc2 = new ProjCoordinate(merc.x, merc.y);
	// ProjCoordinate wgs84_4 = new ProjCoordinate();
	// crs3857_to_crs4326.transform(merc2, wgs84_4);
	//
	// }
	//
	// private static String getProjection(Activity activity, int epsg) {
	// return activity
	// .getString(activity.getResources().getIdentifier(
	// "projection_epsg_" + epsg, "string",
	// activity.getPackageName()));
	// }

}
