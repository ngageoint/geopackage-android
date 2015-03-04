package mil.nga.giat.geopackage.geom.util;

import java.util.List;

import mil.nga.giat.geopackage.geom.CircularString;
import mil.nga.giat.geopackage.geom.CompoundCurve;
import mil.nga.giat.geopackage.geom.Geometry;
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.geom.LineString;
import mil.nga.giat.geopackage.geom.MultiLineString;
import mil.nga.giat.geopackage.geom.MultiPoint;
import mil.nga.giat.geopackage.geom.MultiPolygon;
import mil.nga.giat.geopackage.geom.Point;
import mil.nga.giat.geopackage.geom.Polygon;
import mil.nga.giat.geopackage.geom.PolyhedralSurface;
import mil.nga.giat.geopackage.geom.TIN;
import mil.nga.giat.geopackage.geom.Triangle;

/**
 * String representation of a Geometry
 * 
 * @author osbornb
 */
public class GeometryPrinter {

	/**
	 * Get Geometry Information as a String
	 * 
	 * @param geometry
	 * @return
	 */
	public static String getGeometryString(Geometry geometry) {

		StringBuilder message = new StringBuilder();

		GeometryType geometryType = geometry.getGeometryType();
		switch (geometryType) {
		case POINT:
			addPointMessage(message, (Point) geometry);
			break;
		case LINESTRING:
			addLineStringMessage(message, (LineString) geometry);
			break;
		case POLYGON:
			addPolygonMessage(message, (Polygon) geometry);
			break;
		case MULTIPOINT:
			addMultiPointMessage(message, (MultiPoint) geometry);
			break;
		case MULTILINESTRING:
			addMultiLineStringMessage(message, (MultiLineString) geometry);
			break;
		case MULTIPOLYGON:
			addMultiPolygonMessage(message, (MultiPolygon) geometry);
			break;
		case CIRCULARSTRING:
			addLineStringMessage(message, (CircularString) geometry);
			break;
		case COMPOUNDCURVE:
			addCompoundCurveMessage(message, (CompoundCurve) geometry);
			break;
		case POLYHEDRALSURFACE:
			addPolyhedralSurfaceMessage(message, (PolyhedralSurface) geometry);
			break;
		case TIN:
			addPolyhedralSurfaceMessage(message, (TIN) geometry);
			break;
		case TRIANGLE:
			addPolygonMessage(message, (Triangle) geometry);
			break;
		default:
		}

		return message.toString();
	}

	/**
	 * Add Point message
	 * 
	 * @param message
	 * @param point
	 */
	private static void addPointMessage(StringBuilder message, Point point) {
		message.append("Latitude: ").append(point.getY());
		message.append("\nLongitude: ").append(point.getX());
	}

	/**
	 * Add MultiPoint message
	 * 
	 * @param message
	 * @param multiPoint
	 */
	private static void addMultiPointMessage(StringBuilder message,
			MultiPoint multiPoint) {
		message.append(Point.class.getSimpleName() + "s: "
				+ multiPoint.numPoints());
		List<Point> points = multiPoint.getPoints();
		for (int i = 0; i < points.size(); i++) {
			Point point = points.get(i);
			message.append("\n\n");
			message.append(Point.class.getSimpleName() + " " + (i + 1));
			message.append("\n");
			addPointMessage(message, point);
		}
	}

	/**
	 * Add LineString message
	 * 
	 * @param message
	 * @param lineString
	 */
	private static void addLineStringMessage(StringBuilder message,
			LineString lineString) {
		message.append(Point.class.getSimpleName() + "s: "
				+ lineString.numPoints());
		for (Point point : lineString.getPoints()) {
			message.append("\n\n");
			addPointMessage(message, point);
		}
	}

	/**
	 * Add MultiLineString message
	 * 
	 * @param message
	 * @param multiLineString
	 */
	private static void addMultiLineStringMessage(StringBuilder message,
			MultiLineString multiLineString) {
		message.append(LineString.class.getSimpleName() + "s: "
				+ multiLineString.numLineStrings());
		List<LineString> lineStrings = multiLineString.getLineStrings();
		for (int i = 0; i < lineStrings.size(); i++) {
			LineString lineString = lineStrings.get(i);
			message.append("\n\n");
			message.append(LineString.class.getSimpleName() + " " + (i + 1));
			message.append("\n");
			addLineStringMessage(message, lineString);
		}
	}

	/**
	 * Add Polygon message
	 * 
	 * @param message
	 * @param polygon
	 */
	private static void addPolygonMessage(StringBuilder message, Polygon polygon) {
		message.append("Rings: " + polygon.numRings());
		List<LineString> rings = polygon.getRings();
		for (int i = 0; i < rings.size(); i++) {
			LineString ring = rings.get(i);
			message.append("\n\n");
			if (i > 0) {
				message.append("Hole " + i);
				message.append("\n");
			}
			addLineStringMessage(message, ring);
		}
	}

	/**
	 * Add MultiPolygon message
	 * 
	 * @param message
	 * @param multiPolygon
	 */
	private static void addMultiPolygonMessage(StringBuilder message,
			MultiPolygon multiPolygon) {
		message.append(Polygon.class.getSimpleName() + "s: "
				+ multiPolygon.numPolygons());
		List<Polygon> polygons = multiPolygon.getPolygons();
		for (int i = 0; i < polygons.size(); i++) {
			Polygon polygon = polygons.get(i);
			message.append("\n\n");
			message.append(Polygon.class.getSimpleName() + " " + (i + 1));
			message.append("\n");
			addPolygonMessage(message, polygon);
		}
	}

	/**
	 * Add CompoundCurve message
	 * 
	 * @param message
	 * @param compoundCurve
	 */
	private static void addCompoundCurveMessage(StringBuilder message,
			CompoundCurve compoundCurve) {
		message.append(LineString.class.getSimpleName() + "s: "
				+ compoundCurve.numLineStrings());
		List<LineString> lineStrings = compoundCurve.getLineStrings();
		for (int i = 0; i < lineStrings.size(); i++) {
			LineString lineString = lineStrings.get(i);
			message.append("\n\n");
			message.append(LineString.class.getSimpleName() + " " + (i + 1));
			message.append("\n");
			addLineStringMessage(message, lineString);
		}
	}

	/**
	 * Add PolyhedralSurface message
	 * 
	 * @param message
	 * @param polyhedralSurface
	 */
	private static void addPolyhedralSurfaceMessage(StringBuilder message,
			PolyhedralSurface polyhedralSurface) {
		message.append(Polygon.class.getSimpleName() + "s: "
				+ polyhedralSurface.numPolygons());
		List<Polygon> polygons = polyhedralSurface.getPolygons();
		for (int i = 0; i < polygons.size(); i++) {
			Polygon polygon = polygons.get(i);
			message.append("\n\n");
			message.append(Polygon.class.getSimpleName() + " " + (i + 1));
			message.append("\n");
			addPolygonMessage(message, polygon);
		}
	}

}
