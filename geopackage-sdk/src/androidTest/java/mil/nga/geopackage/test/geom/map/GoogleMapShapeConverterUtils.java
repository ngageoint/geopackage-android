package mil.nga.geopackage.test.geom.map;

import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.wkb.geom.CircularString;
import mil.nga.wkb.geom.CompoundCurve;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.MultiLineString;
import mil.nga.wkb.geom.MultiPoint;
import mil.nga.wkb.geom.MultiPolygon;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;
import mil.nga.wkb.geom.PolyhedralSurface;
import mil.nga.wkb.geom.TIN;
import mil.nga.wkb.geom.Triangle;
import mil.nga.geopackage.geom.map.GoogleMapShapeConverter;
import mil.nga.geopackage.geom.map.MultiLatLng;
import mil.nga.geopackage.geom.map.MultiPolygonOptions;
import mil.nga.geopackage.geom.map.MultiPolylineOptions;
import mil.nga.geopackage.geom.GeoPackageGeometryData;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Google Map Shape Converter Utility test methods
 * 
 * @author osbornb
 */
public class GoogleMapShapeConverterUtils {

	/**
	 * Test shapes
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testShapes(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();

		if (geometryColumnsDao.isTableExists()) {
			List<GeometryColumns> results = geometryColumnsDao.queryForAll();

			for (GeometryColumns geometryColumns : results) {

				FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

				GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
						dao.getProjection());

				// Query for all
				FeatureCursor cursor = dao.queryForAll();
				while (cursor.moveToNext()) {
					FeatureRow featureRow = cursor.getRow();

					GeoPackageGeometryData geometryData = featureRow
							.getGeometry();

					if (geometryData != null) {
						Geometry geometry = geometryData.getGeometry();
						GeometryType geometryType = geometry.getGeometryType();

						switch (geometryType) {
						case POINT:
							convertPoint(converter, (Point) geometry);
							break;
						case LINESTRING:
							convertLineString(converter, (LineString) geometry);
							break;
						case POLYGON:
							convertPolygon(converter, (Polygon) geometry);
							break;
						case MULTIPOINT:
							convertMultiPoint(converter, (MultiPoint) geometry);
							break;
						case MULTILINESTRING:
							convertMultiLineString(converter,
									(MultiLineString) geometry);
							break;
						case MULTIPOLYGON:
							convertMultiPolygon(converter,
									(MultiPolygon) geometry);
							break;
						case CIRCULARSTRING:
							convertLineString(converter,
									(CircularString) geometry);
							break;
						case COMPOUNDCURVE:
							convertCompoundCurve(converter,
									(CompoundCurve) geometry);
							break;
						case POLYHEDRALSURFACE:
							convertMultiPolygon(converter,
									(PolyhedralSurface) geometry);
							break;
						case TIN:
							convertMultiPolygon(converter, (TIN) geometry);
							break;
						case TRIANGLE:
							convertPolygon(converter, (Triangle) geometry);
							break;
						default:
						}
					}

				}
				cursor.close();

			}
		}

	}

	/**
	 * Test the Point conversion
	 * 
	 * @param converter
	 * @param point
	 */
	private static void convertPoint(GoogleMapShapeConverter converter,
			Point point) {

		LatLng latLng = converter.toLatLng(point);
		TestCase.assertNotNull(latLng);

		comparePointAndLatLng(converter, point, latLng);

		Point point2 = converter.toPoint(latLng, point.hasZ(), point.hasM());
		TestCase.assertNotNull(point2);

		comparePoints(point, point2);

	}

	/**
	 * Compare Point with LatLng
	 *
     * @param converter
	 * @param point
	 * @param latLng
	 */
	private static void comparePointAndLatLng(GoogleMapShapeConverter converter, Point point, LatLng latLng) {
        if(converter != null){
            point = converter.toWgs84(point);
        }
		TestCase.assertEquals(point.getX(), latLng.longitude, 0.001);
		TestCase.assertEquals(point.getY(), latLng.latitude, 0.001);
	}

	/**
	 * Compare two Points
	 * 
	 * @param point
	 * @param point2
	 */
	private static void comparePoints(Point point, Point point2) {
		TestCase.assertEquals(point.getX(), point2.getX(), 0.001);
		TestCase.assertEquals(point.getY(), point2.getY(), 0.001);
	}

	/**
	 * Test the LineString conversion
	 * 
	 * @param converter
	 * @param lineString
	 */
	private static void convertLineString(GoogleMapShapeConverter converter,
			LineString lineString) {

		PolylineOptions polyline = converter.toPolyline(lineString);
		TestCase.assertNotNull(polyline);

		compareLineStringAndPolyline(converter, lineString, polyline);

		LineString lineString2 = converter.toLineString(polyline);
		compareLineStrings(lineString, lineString2);
	}

	/**
	 * Compare LineString with Polyline
	 *
     * @param converter
	 * @param lineString
	 * @param polyline
	 */
	private static void compareLineStringAndPolyline(GoogleMapShapeConverter converter, LineString lineString,
			PolylineOptions polyline) {
		compareLineStringAndLatLngs(converter, lineString, polyline.getPoints());
	}

	/**
	 * Compare LineString with LatLng points
	 *
     * @param converter
	 * @param lineString
	 * @param points
	 */
	private static void compareLineStringAndLatLngs(GoogleMapShapeConverter converter, LineString lineString,
			List<LatLng> points) {
		comparePointsAndLatLngs(converter, lineString.getPoints(), points);
	}

	/**
	 * Compare list of points and lat longs
	 *
     * @param converter
	 * @param points
	 * @param points2
	 */
	private static void comparePointsAndLatLngs(GoogleMapShapeConverter converter, List<Point> points,
			List<LatLng> points2) {
		TestCase.assertEquals(points.size(), points2.size());

		for (int i = 0; i < points.size(); i++) {
			comparePointAndLatLng(converter, points.get(i), points2.get(i));
		}
	}

	/**
	 * Compare two LineStrings
	 * 
	 * @param lineString
	 * @param lineString2
	 */
	private static void compareLineStrings(LineString lineString,
			LineString lineString2) {
		comparePoints(lineString.getPoints(), lineString2.getPoints());
	}

	/**
	 * Compare two lists of points
	 * 
	 * @param points
	 * @param points2
	 */
	private static void comparePoints(List<Point> points, List<Point> points2) {
		TestCase.assertEquals(points.size(), points2.size());

		for (int i = 0; i < points.size(); i++) {
			comparePoints(points.get(i), points2.get(i));
		}
	}

	/**
	 * Test the Polygon conversion
	 * 
	 * @param converter
	 * @param polygon
	 */
	private static void convertPolygon(GoogleMapShapeConverter converter,
			Polygon polygon) {

		PolygonOptions polygonOptions = converter.toPolygon(polygon);
		TestCase.assertNotNull(polygonOptions);

		comparePolygonAndMapPolygon(converter, polygon, polygonOptions);

		Polygon polygon2 = converter.toPolygon(polygonOptions);
		comparePolygons(polygon, polygon2);
	}

	/**
	 * Compare Polygon with Map Polygon
	 *
     * @param converter
	 * @param polygon
	 * @param polygon2
	 */
	private static void comparePolygonAndMapPolygon(GoogleMapShapeConverter converter,
            Polygon polygon,
			PolygonOptions polygon2) {
		List<LineString> rings = polygon.getRings();
		List<LatLng> points = polygon2.getPoints();
		List<List<LatLng>> holes = polygon2.getHoles();

		TestCase.assertEquals(polygon.numRings(), 1 + holes.size());

		LineString polygonRing = rings.get(0);
		compareLineStringAndLatLngs(converter, polygonRing, points);

		for (int i = 1; i < rings.size(); i++) {
			LineString ring = rings.get(i);
			List<LatLng> hole = holes.get(i - 1);
			compareLineStringAndLatLngs(converter, ring, hole);
		}
	}

	/**
	 * Compare two Polygons
	 * 
	 * @param polygon
	 * @param polygon2
	 */
	private static void comparePolygons(Polygon polygon, Polygon polygon2) {
		List<LineString> rings = polygon.getRings();
		List<LineString> rings2 = polygon2.getRings();

		TestCase.assertEquals(polygon.numRings(), polygon2.numRings());

		for (int i = 0; i < polygon.numRings(); i++) {
			compareLineStrings(rings.get(i), rings2.get(i));
		}
	}

	/**
	 * Test the MultiPoint conversion
	 * 
	 * @param converter
	 * @param multiPoint
	 */
	private static void convertMultiPoint(GoogleMapShapeConverter converter,
			MultiPoint multiPoint) {

		MultiLatLng latLngs = converter.toLatLngs(multiPoint);
		TestCase.assertNotNull(latLngs);
		TestCase.assertFalse(latLngs.getLatLngs().isEmpty());

		List<Point> points = multiPoint.getPoints();
		comparePointsAndLatLngs(converter, points, latLngs.getLatLngs());

		MultiPoint multiPoint2 = converter.toMultiPoint(latLngs);
		comparePoints(multiPoint.getPoints(), multiPoint2.getPoints());
	}

	/**
	 * Test the MultiLineString conversion
	 * 
	 * @param converter
	 * @param multiLineString
	 */
	private static void convertMultiLineString(
			GoogleMapShapeConverter converter, MultiLineString multiLineString) {

		MultiPolylineOptions polylines = converter.toPolylines(multiLineString);
		TestCase.assertNotNull(polylines);
		TestCase.assertFalse(polylines.getPolylineOptions().isEmpty());

		List<LineString> lineStrings = multiLineString.getLineStrings();
		compareLineStringsAndPolylines(converter, lineStrings,
				polylines.getPolylineOptions());

		MultiLineString multiLineString2 = converter
				.toMultiLineStringFromOptions(polylines);
		compareLineStrings(lineStrings, multiLineString2.getLineStrings());
	}

	/**
	 * Compare list of line strings with list of polylines
	 *
     * @param converter
	 * @param lineStrings
	 * @param polylines
	 */
	private static void compareLineStringsAndPolylines(
            GoogleMapShapeConverter converter,
			List<LineString> lineStrings, List<PolylineOptions> polylines) {

		TestCase.assertEquals(lineStrings.size(), polylines.size());
		for (int i = 0; i < lineStrings.size(); i++) {
			compareLineStringAndPolyline(converter, lineStrings.get(i), polylines.get(i));
		}

	}

	/**
	 * Compare two lists of line strings
	 * 
	 * @param lineStrings
	 * @param lineStrings2
	 */
	private static void compareLineStrings(List<LineString> lineStrings,
			List<LineString> lineStrings2) {

		TestCase.assertEquals(lineStrings.size(), lineStrings2.size());
		for (int i = 0; i < lineStrings.size(); i++) {
			compareLineStrings(lineStrings.get(i), lineStrings2.get(i));
		}

	}

	/**
	 * Test the MultiPolygon conversion
	 * 
	 * @param converter
	 * @param multiPolygon
	 */
	private static void convertMultiPolygon(GoogleMapShapeConverter converter,
			MultiPolygon multiPolygon) {

		MultiPolygonOptions mapPolygons = converter.toPolygons(multiPolygon);
		TestCase.assertNotNull(mapPolygons);
		TestCase.assertFalse(mapPolygons.getPolygonOptions().isEmpty());

		List<Polygon> polygons = multiPolygon.getPolygons();
		comparePolygonsAndMapPolygons(converter, polygons, mapPolygons.getPolygonOptions());

		MultiPolygon multiPolygon2 = converter
				.toMultiPolygonFromOptions(mapPolygons);
		comparePolygons(polygons, multiPolygon2.getPolygons());
	}

	/**
	 * Compare list of polygons with list of map polygons
	 *
     * @param converter
	 * @param polygons
	 * @param mapPolygons
	 */
	private static void comparePolygonsAndMapPolygons(GoogleMapShapeConverter converter, List<Polygon> polygons,
			List<PolygonOptions> mapPolygons) {

		TestCase.assertEquals(polygons.size(), mapPolygons.size());
		for (int i = 0; i < polygons.size(); i++) {
			comparePolygonAndMapPolygon(converter, polygons.get(i), mapPolygons.get(i));
		}

	}

	/**
	 * Compare two lists of polygons
	 * 
	 * @param polygons
	 * @param polygons2
	 */
	private static void comparePolygons(List<Polygon> polygons,
			List<Polygon> polygons2) {

		TestCase.assertEquals(polygons.size(), polygons2.size());
		for (int i = 0; i < polygons.size(); i++) {
			comparePolygons(polygons.get(i), polygons2.get(i));
		}

	}

	/**
	 * Test the CompoundCurve conversion
	 * 
	 * @param converter
	 * @param compoundCurve
	 */
	private static void convertCompoundCurve(GoogleMapShapeConverter converter,
			CompoundCurve compoundCurve) {

		MultiPolylineOptions polylines = converter.toPolylines(compoundCurve);
		TestCase.assertNotNull(polylines);
		TestCase.assertFalse(polylines.getPolylineOptions().isEmpty());

		List<LineString> lineStrings = compoundCurve.getLineStrings();
		compareLineStringsAndPolylines(converter, lineStrings,
				polylines.getPolylineOptions());

		CompoundCurve compoundCurve2 = converter
				.toCompoundCurveWithOptions(polylines);
		compareLineStrings(lineStrings, compoundCurve2.getLineStrings());
	}

	/**
	 * Test the PolyhedralSurface conversion
	 * 
	 * @param converter
	 * @param polyhedralSurface
	 */
	private static void convertMultiPolygon(GoogleMapShapeConverter converter,
			PolyhedralSurface polyhedralSurface) {

		MultiPolygonOptions mapPolygons = converter
				.toPolygons(polyhedralSurface);
		TestCase.assertNotNull(mapPolygons);
		TestCase.assertFalse(mapPolygons.getPolygonOptions().isEmpty());

		List<Polygon> polygons = polyhedralSurface.getPolygons();
		comparePolygonsAndMapPolygons(converter, polygons, mapPolygons.getPolygonOptions());

		PolyhedralSurface polyhedralSurface2 = converter
				.toPolyhedralSurfaceWithOptions(mapPolygons);
		comparePolygons(polygons, polyhedralSurface.getPolygons());
	}

}
