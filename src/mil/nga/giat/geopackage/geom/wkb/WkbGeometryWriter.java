package mil.nga.giat.geopackage.geom.wkb;

import java.io.IOException;
import java.nio.ByteOrder;

import mil.nga.giat.geopackage.geom.GeoPackageCircularString;
import mil.nga.giat.geopackage.geom.GeoPackageCompoundCurve;
import mil.nga.giat.geopackage.geom.GeoPackageCurve;
import mil.nga.giat.geopackage.geom.GeoPackageCurvePolygon;
import mil.nga.giat.geopackage.geom.GeoPackageGeometry;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryCollection;
import mil.nga.giat.geopackage.geom.GeoPackageLineString;
import mil.nga.giat.geopackage.geom.GeoPackageMultiLineString;
import mil.nga.giat.geopackage.geom.GeoPackageMultiPoint;
import mil.nga.giat.geopackage.geom.GeoPackageMultiPolygon;
import mil.nga.giat.geopackage.geom.GeoPackagePoint;
import mil.nga.giat.geopackage.geom.GeoPackagePolygon;
import mil.nga.giat.geopackage.geom.GeoPackagePolyhedralSurface;
import mil.nga.giat.geopackage.geom.GeoPackageTIN;
import mil.nga.giat.geopackage.geom.GeoPackageTriangle;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryType;
import mil.nga.giat.geopackage.util.ByteWriter;
import mil.nga.giat.geopackage.util.GeoPackageException;

/**
 * Well Known Binary writer
 * 
 * @author osbornb
 */
public class WkbGeometryWriter {

	/**
	 * Write a geometry to the byte writer
	 * 
	 * @param writer
	 * @param geometry
	 * @throws IOException
	 */
	public static void writeGeometry(ByteWriter writer,
			GeoPackageGeometry geometry) throws IOException {

		// Write the single byte order byte
		byte byteOrder = writer.getByteOrder() == ByteOrder.BIG_ENDIAN ? (byte) 0
				: (byte) 1;
		writer.writeByte(byteOrder);

		// Write the geometry type integer
		writer.writeInt(geometry.getWkbCode());

		GeoPackageGeometryType geometryType = geometry.getGeometryType();

		switch (geometryType) {

		case GEOMETRY:
			throw new GeoPackageException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case POINT:
			writePoint(writer, (GeoPackagePoint) geometry);
			break;
		case LINESTRING:
			writeLineString(writer, (GeoPackageLineString) geometry);
			break;
		case POLYGON:
			writePolygon(writer, (GeoPackagePolygon) geometry);
			break;
		case MULTIPOINT:
			writeMultiPoint(writer, (GeoPackageMultiPoint) geometry);
			break;
		case MULTILINESTRING:
			writeMultiLineString(writer, (GeoPackageMultiLineString) geometry);
			break;
		case MULTIPOLYGON:
			writeMultiPolygon(writer, (GeoPackageMultiPolygon) geometry);
			break;
		case GEOMETRYCOLLECTION:
			writeGeometryCollection(writer,
					(GeoPackageGeometryCollection<?>) geometry);
			break;
		case CIRCULARSTRING:
			writeCircularString(writer, (GeoPackageCircularString) geometry);
			break;
		case COMPOUNDCURVE:
			writeCompoundCurve(writer, (GeoPackageCompoundCurve) geometry);
			break;
		case CURVEPOLYGON:
			writeCurvePolygon(writer, (GeoPackageCurvePolygon<?>) geometry);
			break;
		case MULTICURVE:
			throw new GeoPackageException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case MULTISURFACE:
			throw new GeoPackageException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case CURVE:
			throw new GeoPackageException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case SURFACE:
			throw new GeoPackageException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case POLYHEDRALSURFACE:
			writePolyhedralSurface(writer,
					(GeoPackagePolyhedralSurface) geometry);
			break;
		case TIN:
			writeTIN(writer, (GeoPackageTIN) geometry);
			break;
		case TRIANGLE:
			writeTriangle(writer, (GeoPackageTriangle) geometry);
			break;
		default:
			throw new GeoPackageException("Geometry Type not supported: "
					+ geometryType);
		}

	}

	/**
	 * Write a Point
	 * 
	 * @param writer
	 * @param point
	 * @throws IOException
	 */
	public static void writePoint(ByteWriter writer, GeoPackagePoint point)
			throws IOException {

		writer.writeDouble(point.getX());
		writer.writeDouble(point.getY());

		if (point.hasZ()) {
			writer.writeDouble(point.getZ());
		}

		if (point.hasM()) {
			writer.writeDouble(point.getM());
		}
	}

	/**
	 * Write a Line String
	 * 
	 * @param writer
	 * @param lineString
	 * @throws IOException
	 */
	public static void writeLineString(ByteWriter writer,
			GeoPackageLineString lineString) throws IOException {

		writer.writeInt(lineString.numPoints());

		for (GeoPackagePoint point : lineString.getPoints()) {
			writePoint(writer, point);
		}
	}

	/**
	 * Write a Polygon
	 * 
	 * @param writer
	 * @param polygon
	 * @throws IOException
	 */
	public static void writePolygon(ByteWriter writer, GeoPackagePolygon polygon)
			throws IOException {

		writer.writeInt(polygon.numRings());

		for (GeoPackageLineString ring : polygon.getRings()) {
			writeLineString(writer, ring);
		}
	}

	/**
	 * Write a Multi Point
	 * 
	 * @param writer
	 * @param multiPoint
	 * @throws IOException
	 */
	public static void writeMultiPoint(ByteWriter writer,
			GeoPackageMultiPoint multiPoint) throws IOException {

		writer.writeInt(multiPoint.numPoints());

		for (GeoPackagePoint point : multiPoint.getPoints()) {
			writeGeometry(writer, point);
		}
	}

	/**
	 * Write a Multi Line String
	 * 
	 * @param writer
	 * @param multiLineString
	 * @throws IOException
	 */
	public static void writeMultiLineString(ByteWriter writer,
			GeoPackageMultiLineString multiLineString) throws IOException {

		writer.writeInt(multiLineString.numLineStrings());

		for (GeoPackageLineString lineString : multiLineString.getLineStrings()) {
			writeGeometry(writer, lineString);
		}
	}

	/**
	 * Write a Multi Polygon
	 * 
	 * @param writer
	 * @param multiPolygon
	 * @throws IOException
	 */
	public static void writeMultiPolygon(ByteWriter writer,
			GeoPackageMultiPolygon multiPolygon) throws IOException {

		writer.writeInt(multiPolygon.numPolygons());

		for (GeoPackagePolygon polygon : multiPolygon.getPolygons()) {
			writeGeometry(writer, polygon);
		}
	}

	/**
	 * Write a Geometry Collection
	 * 
	 * @param writer
	 * @param geometryCollection
	 * @throws IOException
	 */
	public static void writeGeometryCollection(ByteWriter writer,
			GeoPackageGeometryCollection<?> geometryCollection)
			throws IOException {

		writer.writeInt(geometryCollection.numGeometries());

		for (GeoPackageGeometry geometry : geometryCollection.getGeometries()) {
			writeGeometry(writer, geometry);
		}
	}

	/**
	 * Write a Circular String
	 * 
	 * @param writer
	 * @param circularString
	 * @throws IOException
	 */
	public static void writeCircularString(ByteWriter writer,
			GeoPackageCircularString circularString) throws IOException {

		writer.writeInt(circularString.numPoints());

		for (GeoPackagePoint point : circularString.getPoints()) {
			writePoint(writer, point);
		}
	}

	/**
	 * Write a Compound Curve
	 * 
	 * @param writer
	 * @param compoundCurve
	 * @throws IOException
	 */
	public static void writeCompoundCurve(ByteWriter writer,
			GeoPackageCompoundCurve compoundCurve) throws IOException {

		writer.writeInt(compoundCurve.numLineStrings());

		for (GeoPackageLineString lineString : compoundCurve.getLineStrings()) {
			writeGeometry(writer, lineString);
		}
	}

	/**
	 * Write a Curve Polygon
	 * 
	 * @param writer
	 * @param curvePolygon
	 * @throws IOException
	 */
	public static void writeCurvePolygon(ByteWriter writer,
			GeoPackageCurvePolygon<?> curvePolygon) throws IOException {

		writer.writeInt(curvePolygon.numRings());

		for (GeoPackageCurve ring : curvePolygon.getRings()) {
			writeGeometry(writer, ring);
		}
	}

	/**
	 * Write a Polyhedral Surface
	 * 
	 * @param writer
	 * @param polyhedralSurface
	 * @throws IOException
	 */
	public static void writePolyhedralSurface(ByteWriter writer,
			GeoPackagePolyhedralSurface polyhedralSurface) throws IOException {

		writer.writeInt(polyhedralSurface.numPolygons());

		for (GeoPackagePolygon polygon : polyhedralSurface.getPolygons()) {
			writeGeometry(writer, polygon);
		}
	}

	/**
	 * Write a TIN
	 * 
	 * @param writer
	 * @param tin
	 * @throws IOException
	 */
	public static void writeTIN(ByteWriter writer, GeoPackageTIN tin)
			throws IOException {

		writer.writeInt(tin.numPolygons());

		for (GeoPackagePolygon polygon : tin.getPolygons()) {
			writeGeometry(writer, polygon);
		}
	}

	/**
	 * Write a Triangle
	 * 
	 * @param writer
	 * @param triangle
	 * @throws IOException
	 */
	public static void writeTriangle(ByteWriter writer,
			GeoPackageTriangle triangle) throws IOException {

		writer.writeInt(triangle.numRings());

		for (GeoPackageLineString ring : triangle.getRings()) {
			writeLineString(writer, ring);
		}
	}

}
