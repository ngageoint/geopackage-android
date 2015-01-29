package mil.nga.giat.geopackage.geom.wkb;

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
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.util.ByteReader;
import mil.nga.giat.geopackage.util.GeoPackageException;

/**
 * Well Known Binary reader
 * 
 * @author osbornb
 */
public class WkbGeometryReader {

	/**
	 * Read a geometry from the byte reader
	 * 
	 * @param reader
	 * @return
	 */
	public static GeoPackageGeometry readGeometry(ByteReader reader) {
		GeoPackageGeometry geometry = readGeometry(reader, null);
		return geometry;
	}

	/**
	 * Read a geometry from the byte reader
	 * 
	 * @param reader
	 * @param expectedType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends GeoPackageGeometry> T readGeometry(
			ByteReader reader, Class<T> expectedType) {

		// Read the single byte order byte
		byte byteOrderValue = reader.readByte();
		ByteOrder byteOrder = byteOrderValue == 0 ? ByteOrder.BIG_ENDIAN
				: ByteOrder.LITTLE_ENDIAN;
		ByteOrder originalByteOrder = reader.getByteOrder();
		reader.setByteOrder(byteOrder);

		// Read the geometry type integer
		int geometryTypeWkbCode = reader.readInt();

		// Look at the last 2 digits to find the geometry type code (1 - 14)
		int geometryTypeCode = geometryTypeWkbCode % 1000;

		// Look at the first digit to find the options (z when 1 or 3, m when 2
		// or 3)
		int geometryTypeMode = geometryTypeWkbCode / 1000;

		// Determine if the geometry has a z (3d) or m (linear referencing
		// system) value
		boolean hasZ = false;
		boolean hasM = false;
		switch (geometryTypeMode) {
		case 0:
			break;

		case 1:
			hasZ = true;
			break;

		case 2:
			hasM = true;
			break;

		case 3:
			hasZ = true;
			hasM = true;
			break;
		}

		GeometryType geometryType = GeometryType.fromCode(geometryTypeCode);

		GeoPackageGeometry geometry = null;

		switch (geometryType) {

		case GEOMETRY:
			throw new GeoPackageException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case POINT:
			geometry = readPoint(reader, hasZ, hasM);
			break;
		case LINESTRING:
			geometry = readLineString(reader, hasZ, hasM);
			break;
		case POLYGON:
			geometry = readPolygon(reader, hasZ, hasM);
			break;
		case MULTIPOINT:
			geometry = readMultiPoint(reader, hasZ, hasM);
			break;
		case MULTILINESTRING:
			geometry = readMultiLineString(reader, hasZ, hasM);
			break;
		case MULTIPOLYGON:
			geometry = readMultiPolygon(reader, hasZ, hasM);
			break;
		case GEOMETRYCOLLECTION:
			geometry = readGeometryCollection(reader, hasZ, hasM);
			break;
		case CIRCULARSTRING:
			geometry = readCircularString(reader, hasZ, hasM);
			break;
		case COMPOUNDCURVE:
			geometry = readCompoundCurve(reader, hasZ, hasM);
			break;
		case CURVEPOLYGON:
			geometry = readCurvePolygon(reader, hasZ, hasM);
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
			geometry = readPolyhedralSurface(reader, hasZ, hasM);
			break;
		case TIN:
			geometry = readTIN(reader, hasZ, hasM);
			break;
		case TRIANGLE:
			geometry = readTriangle(reader, hasZ, hasM);
			break;
		default:
			throw new GeoPackageException("Geometry Type not supported: "
					+ geometryType);
		}

		// If there is an expected type, verify the geometry if of that type
		if (expectedType != null && geometry != null
				&& !expectedType.isAssignableFrom(geometry.getClass())) {
			throw new GeoPackageException(
					"Unexpected Geometry Type. Expected: "
							+ expectedType.getSimpleName() + ", Actual: "
							+ geometry.getClass().getSimpleName());
		}

		// Restore the byte order
		reader.setByteOrder(originalByteOrder);

		return (T) geometry;
	}

	/**
	 * Read a Point
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackagePoint readPoint(ByteReader reader, boolean hasZ,
			boolean hasM) {

		double x = reader.readDouble();
		double y = reader.readDouble();

		GeoPackagePoint point = new GeoPackagePoint(hasZ, hasM, x, y);

		if (hasZ) {
			double z = reader.readDouble();
			point.setZ(z);
		}

		if (hasM) {
			double m = reader.readDouble();
			point.setM(m);
		}

		return point;
	}

	/**
	 * Read a Line String
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageLineString readLineString(ByteReader reader,
			boolean hasZ, boolean hasM) {

		GeoPackageLineString lineString = new GeoPackageLineString(hasZ, hasM);

		int numPoints = reader.readInt();

		for (int i = 0; i < numPoints; i++) {
			GeoPackagePoint point = readPoint(reader, hasZ, hasM);
			lineString.addPoint(point);

		}

		return lineString;
	}

	/**
	 * Read a Polygon
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackagePolygon readPolygon(ByteReader reader,
			boolean hasZ, boolean hasM) {

		GeoPackagePolygon polygon = new GeoPackagePolygon(hasZ, hasM);

		int numRings = reader.readInt();

		for (int i = 0; i < numRings; i++) {
			GeoPackageLineString ring = readLineString(reader, hasZ, hasM);
			polygon.addRing(ring);

		}

		return polygon;
	}

	/**
	 * Read a Multi Point
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageMultiPoint readMultiPoint(ByteReader reader,
			boolean hasZ, boolean hasM) {

		GeoPackageMultiPoint multiPoint = new GeoPackageMultiPoint(hasZ, hasM);

		int numPoints = reader.readInt();

		for (int i = 0; i < numPoints; i++) {
			GeoPackagePoint point = readGeometry(reader, GeoPackagePoint.class);
			multiPoint.addPoint(point);

		}

		return multiPoint;
	}

	/**
	 * Read a Multi Line String
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageMultiLineString readMultiLineString(
			ByteReader reader, boolean hasZ, boolean hasM) {

		GeoPackageMultiLineString multiLineString = new GeoPackageMultiLineString(
				hasZ, hasM);

		int numLineStrings = reader.readInt();

		for (int i = 0; i < numLineStrings; i++) {
			GeoPackageLineString lineString = readGeometry(reader,
					GeoPackageLineString.class);
			multiLineString.addLineString(lineString);

		}

		return multiLineString;
	}

	/**
	 * Read a Multi Polygon
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageMultiPolygon readMultiPolygon(ByteReader reader,
			boolean hasZ, boolean hasM) {

		GeoPackageMultiPolygon multiPolygon = new GeoPackageMultiPolygon(hasZ,
				hasM);

		int numPolygons = reader.readInt();

		for (int i = 0; i < numPolygons; i++) {
			GeoPackagePolygon polygon = readGeometry(reader,
					GeoPackagePolygon.class);
			multiPolygon.addPolygon(polygon);

		}

		return multiPolygon;
	}

	/**
	 * Read a Geometry Collection
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageGeometryCollection<GeoPackageGeometry> readGeometryCollection(
			ByteReader reader, boolean hasZ, boolean hasM) {

		GeoPackageGeometryCollection<GeoPackageGeometry> geometryCollection = new GeoPackageGeometryCollection<GeoPackageGeometry>(
				hasZ, hasM);

		int numGeometries = reader.readInt();

		for (int i = 0; i < numGeometries; i++) {
			GeoPackageGeometry geometry = readGeometry(reader,
					GeoPackageGeometry.class);
			geometryCollection.addGeometry(geometry);

		}

		return geometryCollection;
	}

	/**
	 * Read a Circular String
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageCircularString readCircularString(
			ByteReader reader, boolean hasZ, boolean hasM) {

		GeoPackageCircularString circularString = new GeoPackageCircularString(
				hasZ, hasM);

		int numPoints = reader.readInt();

		for (int i = 0; i < numPoints; i++) {
			GeoPackagePoint point = readPoint(reader, hasZ, hasM);
			circularString.addPoint(point);

		}

		return circularString;
	}

	/**
	 * Read a Compound Curve
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageCompoundCurve readCompoundCurve(ByteReader reader,
			boolean hasZ, boolean hasM) {

		GeoPackageCompoundCurve compoundCurve = new GeoPackageCompoundCurve(
				hasZ, hasM);

		int numLineStrings = reader.readInt();

		for (int i = 0; i < numLineStrings; i++) {
			GeoPackageLineString lineString = readGeometry(reader,
					GeoPackageLineString.class);
			compoundCurve.addLineString(lineString);

		}

		return compoundCurve;
	}

	/**
	 * Read a Curve Polygon
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageCurvePolygon<GeoPackageCurve> readCurvePolygon(
			ByteReader reader, boolean hasZ, boolean hasM) {

		GeoPackageCurvePolygon<GeoPackageCurve> curvePolygon = new GeoPackageCurvePolygon<GeoPackageCurve>(
				hasZ, hasM);

		int numRings = reader.readInt();

		for (int i = 0; i < numRings; i++) {
			GeoPackageCurve ring = readGeometry(reader, GeoPackageCurve.class);
			curvePolygon.addRing(ring);

		}

		return curvePolygon;
	}

	/**
	 * Read a Polyhedral Surface
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackagePolyhedralSurface readPolyhedralSurface(
			ByteReader reader, boolean hasZ, boolean hasM) {

		GeoPackagePolyhedralSurface polyhedralSurface = new GeoPackagePolyhedralSurface(
				hasZ, hasM);

		int numPolygons = reader.readInt();

		for (int i = 0; i < numPolygons; i++) {
			GeoPackagePolygon polygon = readGeometry(reader,
					GeoPackagePolygon.class);
			polyhedralSurface.addPolygon(polygon);

		}

		return polyhedralSurface;
	}

	/**
	 * Read a TIN
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageTIN readTIN(ByteReader reader, boolean hasZ,
			boolean hasM) {

		GeoPackageTIN tin = new GeoPackageTIN(hasZ, hasM);

		int numPolygons = reader.readInt();

		for (int i = 0; i < numPolygons; i++) {
			GeoPackagePolygon polygon = readGeometry(reader,
					GeoPackagePolygon.class);
			tin.addPolygon(polygon);

		}

		return tin;
	}

	/**
	 * Read a Triangle
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static GeoPackageTriangle readTriangle(ByteReader reader,
			boolean hasZ, boolean hasM) {

		GeoPackageTriangle triangle = new GeoPackageTriangle(hasZ, hasM);

		int numRings = reader.readInt();

		for (int i = 0; i < numRings; i++) {
			GeoPackageLineString ring = readLineString(reader, hasZ, hasM);
			triangle.addRing(ring);

		}

		return triangle;
	}

}
