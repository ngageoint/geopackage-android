package mil.nga.giat.geopackage.test.geom;

import java.io.IOException;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.giat.geopackage.features.user.FeatureCursor;
import mil.nga.giat.geopackage.features.user.FeatureDao;
import mil.nga.giat.geopackage.geom.CircularString;
import mil.nga.giat.geopackage.geom.CompoundCurve;
import mil.nga.giat.geopackage.geom.CurvePolygon;
import mil.nga.giat.geopackage.geom.Geometry;
import mil.nga.giat.geopackage.geom.GeometryCollection;
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
import mil.nga.giat.geopackage.geom.data.GeoPackageGeometryData;
import mil.nga.giat.geopackage.geom.data.GeoPackageGeometryEnvelope;

/**
 * GeoPackage Geometry Data test utils
 * 
 * @author osbornb
 */
public class GeoPackageGeometryDataUtils {

	/**
	 * Test reading and writing (and comparing) geometry bytes
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testReadWriteBytes(GeoPackage geoPackage)
			throws SQLException, IOException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();

		if (geometryColumnsDao.isTableExists()) {
			List<GeometryColumns> results = geometryColumnsDao.queryForAll();

			for (GeometryColumns geometryColumns : results) {

				FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
				TestCase.assertNotNull(dao);

				FeatureCursor cursor = dao.queryForAll();

				while (cursor.moveToNext()) {

					GeoPackageGeometryData geometryData = cursor.getGeometry();
					if (geometryData != null) {

						byte[] geometryDataToBytes = geometryData.toBytes();
						compareByteArrays(geometryDataToBytes,
								geometryData.getBytes());

						GeoPackageGeometryData geometryDataAfterToBytes = geometryData;

						// Re-retrieve the original geometry data
						geometryData = cursor.getGeometry();

						// Compare the original with the toBytes geometry data
						compareGeometryData(geometryData,
								geometryDataAfterToBytes);

						// Create a new geometry data from the bytes and compare
						// with original
						GeoPackageGeometryData geometryDataFromBytes = new GeoPackageGeometryData(
								geometryDataToBytes);
						compareGeometryData(geometryData, geometryDataFromBytes);

						// Set the geometry empty flag and verify the geometry
						// was
						// not written / read
						geometryDataAfterToBytes = cursor.getGeometry();
						geometryDataAfterToBytes.setEmpty(true);
						geometryDataToBytes = geometryDataAfterToBytes
								.toBytes();
						geometryDataFromBytes = new GeoPackageGeometryData(
								geometryDataToBytes);
						TestCase.assertNull(geometryDataFromBytes.getGeometry());
						compareByteArrays(
								geometryDataAfterToBytes.getHeaderBytes(),
								geometryDataFromBytes.getHeaderBytes());

						// Flip the byte order and verify the header and bytes
						// no
						// longer matches the original, but the geometries still
						// do
						geometryDataAfterToBytes = cursor.getGeometry();
						geometryDataAfterToBytes
								.setByteOrder(geometryDataAfterToBytes
										.getByteOrder() == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN
										: ByteOrder.BIG_ENDIAN);
						geometryDataToBytes = geometryDataAfterToBytes
								.toBytes();
						geometryDataFromBytes = new GeoPackageGeometryData(
								geometryDataToBytes);
						compareGeometryData(geometryDataAfterToBytes,
								geometryDataFromBytes);
						TestCase.assertFalse(equalByteArrays(
								geometryDataAfterToBytes.getHeaderBytes(),
								geometryData.getHeaderBytes()));
						TestCase.assertFalse(equalByteArrays(
								geometryDataAfterToBytes.getWkbBytes(),
								geometryData.getWkbBytes()));
						TestCase.assertFalse(equalByteArrays(
								geometryDataAfterToBytes.getBytes(),
								geometryData.getBytes()));
						compareGeometries(geometryData.getGeometry(),
								geometryDataAfterToBytes.getGeometry());
					}

				}
				cursor.close();
			}
		}

	}

	/**
	 * Compare two geometry datas and verify they are equal
	 * 
	 * @param expected
	 * @param actual
	 */
	public static void compareGeometryData(GeoPackageGeometryData expected,
			GeoPackageGeometryData actual) {

		// Compare geometry data attributes
		TestCase.assertEquals(expected.isExtended(), actual.isExtended());
		TestCase.assertEquals(expected.isEmpty(), actual.isEmpty());
		TestCase.assertEquals(expected.getByteOrder(), actual.getByteOrder());
		TestCase.assertEquals(expected.getSrsId(), actual.getSrsId());
		compareEnvelopes(expected.getEnvelope(), actual.getEnvelope());
		TestCase.assertEquals(expected.getWkbGeometryIndex(),
				actual.getWkbGeometryIndex());

		// Compare header bytes
		compareByteArrays(expected.getHeaderBytes(), actual.getHeaderBytes());

		// Compare geometries
		compareGeometries(expected.getGeometry(), actual.getGeometry());

		// Compare well-known binary geometries
		compareByteArrays(expected.getWkbBytes(), actual.getWkbBytes());

		// Compare all bytes
		compareByteArrays(expected.getBytes(), actual.getBytes());

	}

	/**
	 * Compare two geometry envelopes and verify they are equal
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareEnvelopes(GeoPackageGeometryEnvelope expected,
			GeoPackageGeometryEnvelope actual) {

		if (expected == null) {
			TestCase.assertNull(actual);
		} else {
			TestCase.assertNotNull(actual);

			TestCase.assertEquals(expected.getIndicator(),
					actual.getIndicator());
			TestCase.assertEquals(expected.getMinX(), actual.getMinX());
			TestCase.assertEquals(expected.getMaxX(), actual.getMaxX());
			TestCase.assertEquals(expected.getMinY(), actual.getMinY());
			TestCase.assertEquals(expected.getMaxY(), actual.getMaxY());
			TestCase.assertEquals(expected.hasZ(), actual.hasZ());
			TestCase.assertEquals(expected.getMinZ(), actual.getMinZ());
			TestCase.assertEquals(expected.getMaxZ(), actual.getMaxZ());
			TestCase.assertEquals(expected.hasM(), actual.hasM());
			TestCase.assertEquals(expected.getMinM(), actual.getMinM());
			TestCase.assertEquals(expected.getMaxM(), actual.getMaxM());
		}

	}

	/**
	 * Compare two geometries and verify they are equal
	 * 
	 * @param expected
	 * @param actual
	 */
	public static void compareGeometries(Geometry expected, Geometry actual) {
		if (expected == null) {
			TestCase.assertNull(actual);
		} else {
			TestCase.assertNotNull(actual);

			GeometryType geometryType = expected.getGeometryType();
			switch (geometryType) {

			case GEOMETRY:
				TestCase.fail("Unexpected Geometry Type of "
						+ geometryType.name() + " which is abstract");
			case POINT:
				comparePoint((Point) actual, (Point) expected);
				break;
			case LINESTRING:
				compareLineString((LineString) expected, (LineString) actual);
				break;
			case POLYGON:
				comparePolygon((Polygon) expected, (Polygon) actual);
				break;
			case MULTIPOINT:
				compareMultiPoint((MultiPoint) expected, (MultiPoint) actual);
				break;
			case MULTILINESTRING:
				compareMultiLineString((MultiLineString) expected,
						(MultiLineString) actual);
				break;
			case MULTIPOLYGON:
				compareMultiPolygon((MultiPolygon) expected,
						(MultiPolygon) actual);
				break;
			case GEOMETRYCOLLECTION:
				compareGeometryCollection((GeometryCollection<?>) expected,
						(GeometryCollection<?>) actual);
				break;
			case CIRCULARSTRING:
				compareCircularString((CircularString) expected,
						(CircularString) actual);
				break;
			case COMPOUNDCURVE:
				compareCompoundCurve((CompoundCurve) expected,
						(CompoundCurve) actual);
				break;
			case CURVEPOLYGON:
				compareCurvePolygon((CurvePolygon<?>) expected,
						(CurvePolygon<?>) actual);
				break;
			case MULTICURVE:
				TestCase.fail("Unexpected Geometry Type of "
						+ geometryType.name() + " which is abstract");
			case MULTISURFACE:
				TestCase.fail("Unexpected Geometry Type of "
						+ geometryType.name() + " which is abstract");
			case CURVE:
				TestCase.fail("Unexpected Geometry Type of "
						+ geometryType.name() + " which is abstract");
			case SURFACE:
				TestCase.fail("Unexpected Geometry Type of "
						+ geometryType.name() + " which is abstract");
			case POLYHEDRALSURFACE:
				comparePolyhedralSurface((PolyhedralSurface) expected,
						(PolyhedralSurface) actual);
				break;
			case TIN:
				compareTIN((TIN) expected, (TIN) actual);
				break;
			case TRIANGLE:
				compareTriangle((Triangle) expected, (Triangle) actual);
				break;
			default:
				throw new GeoPackageException("Geometry Type not supported: "
						+ geometryType);
			}
		}
	}

	/**
	 * Compare to the base attribiutes of two geometries
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareBaseGeometryAttributes(Geometry expected,
			Geometry actual) {
		TestCase.assertEquals(expected.getGeometryType(),
				actual.getGeometryType());
		TestCase.assertEquals(expected.hasZ(), actual.hasZ());
		TestCase.assertEquals(expected.hasM(), actual.hasM());
		TestCase.assertEquals(expected.getWkbCode(), actual.getWkbCode());
	}

	/**
	 * Compare the two points for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void comparePoint(Point expected, Point actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.getX(), actual.getX());
		TestCase.assertEquals(expected.getY(), actual.getY());
		TestCase.assertEquals(expected.getZ(), actual.getZ());
		TestCase.assertEquals(expected.getM(), actual.getM());
	}

	/**
	 * Compare the two line strings for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareLineString(LineString expected, LineString actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numPoints(), actual.numPoints());
		for (int i = 0; i < expected.numPoints(); i++) {
			comparePoint(expected.getPoints().get(i), actual.getPoints().get(i));
		}
	}

	/**
	 * Compare the two polygons for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void comparePolygon(Polygon expected, Polygon actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numRings(), actual.numRings());
		for (int i = 0; i < expected.numRings(); i++) {
			compareLineString(expected.getRings().get(i), actual.getRings()
					.get(i));
		}
	}

	/**
	 * Compare the two multi points for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareMultiPoint(MultiPoint expected, MultiPoint actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numPoints(), actual.numPoints());
		for (int i = 0; i < expected.numPoints(); i++) {
			comparePoint(expected.getPoints().get(i), actual.getPoints().get(i));
		}
	}

	/**
	 * Compare the two multi line strings for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareMultiLineString(MultiLineString expected,
			MultiLineString actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numLineStrings(),
				actual.numLineStrings());
		for (int i = 0; i < expected.numLineStrings(); i++) {
			compareLineString(expected.getLineStrings().get(i), actual
					.getLineStrings().get(i));
		}
	}

	/**
	 * Compare the two multi polygons for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareMultiPolygon(MultiPolygon expected,
			MultiPolygon actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numPolygons(), actual.numPolygons());
		for (int i = 0; i < expected.numPolygons(); i++) {
			comparePolygon(expected.getPolygons().get(i), actual.getPolygons()
					.get(i));
		}
	}

	/**
	 * Compare the two geometry collections for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareGeometryCollection(
			GeometryCollection<?> expected, GeometryCollection<?> actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numGeometries(), actual.numGeometries());
		for (int i = 0; i < expected.numGeometries(); i++) {
			compareGeometries(expected.getGeometries().get(i), actual
					.getGeometries().get(i));
		}
	}

	/**
	 * Compare the two circular strings for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareCircularString(CircularString expected,
			CircularString actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numPoints(), actual.numPoints());
		for (int i = 0; i < expected.numPoints(); i++) {
			comparePoint(expected.getPoints().get(i), actual.getPoints().get(i));
		}
	}

	/**
	 * Compare the two compound curves for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareCompoundCurve(CompoundCurve expected,
			CompoundCurve actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numLineStrings(),
				actual.numLineStrings());
		for (int i = 0; i < expected.numLineStrings(); i++) {
			compareLineString(expected.getLineStrings().get(i), actual
					.getLineStrings().get(i));
		}
	}

	/**
	 * Compare the two curve polygons for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareCurvePolygon(CurvePolygon<?> expected,
			CurvePolygon<?> actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numRings(), actual.numRings());
		for (int i = 0; i < expected.numRings(); i++) {
			compareGeometries(expected.getRings().get(i), actual.getRings()
					.get(i));
		}
	}

	/**
	 * Compare the two polyhedral surfaces for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void comparePolyhedralSurface(PolyhedralSurface expected,
			PolyhedralSurface actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numPolygons(), actual.numPolygons());
		for (int i = 0; i < expected.numPolygons(); i++) {
			compareGeometries(expected.getPolygons().get(i), actual
					.getPolygons().get(i));
		}
	}

	/**
	 * Compare the two TINs for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareTIN(TIN expected, TIN actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numPolygons(), actual.numPolygons());
		for (int i = 0; i < expected.numPolygons(); i++) {
			compareGeometries(expected.getPolygons().get(i), actual
					.getPolygons().get(i));
		}
	}

	/**
	 * Compare the two triangles for equality
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareTriangle(Triangle expected, Triangle actual) {

		compareBaseGeometryAttributes(expected, actual);
		TestCase.assertEquals(expected.numRings(), actual.numRings());
		for (int i = 0; i < expected.numRings(); i++) {
			compareLineString(expected.getRings().get(i), actual.getRings()
					.get(i));
		}
	}

	/**
	 * Compare two byte arrays and verify they are equal
	 * 
	 * @param expected
	 * @param actual
	 */
	public static void compareByteArrays(byte[] expected, byte[] actual) {

		TestCase.assertEquals(expected.length, actual.length);

		for (int i = 0; i < expected.length; i++) {
			TestCase.assertEquals("Byte: " + i, expected[i], actual[i]);
		}

	}

	/**
	 * Compare two byte arrays and verify they are equal
	 * 
	 * @param expected
	 * @param actual
	 * @return true if equal
	 */
	public static boolean equalByteArrays(byte[] expected, byte[] actual) {

		boolean equal = expected.length == actual.length;

		for (int i = 0; equal && i < expected.length; i++) {
			equal = expected[i] == actual[i];
		}

		return equal;
	}

}
