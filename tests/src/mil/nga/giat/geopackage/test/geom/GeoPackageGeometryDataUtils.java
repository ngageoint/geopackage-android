package mil.nga.giat.geopackage.test.geom;

import java.io.IOException;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.data.c4.FeatureCursor;
import mil.nga.giat.geopackage.data.c4.FeatureDao;
import mil.nga.giat.geopackage.geom.GeoPackageCircularString;
import mil.nga.giat.geopackage.geom.GeoPackageCompoundCurve;
import mil.nga.giat.geopackage.geom.GeoPackageCurvePolygon;
import mil.nga.giat.geopackage.geom.GeoPackageGeometry;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryCollection;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryEnvelope;
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
import mil.nga.giat.geopackage.util.GeoPackageException;

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
					compareGeometryData(geometryData, geometryDataAfterToBytes);

					// Create a new geometry data from the bytes and compare
					// with original
					GeoPackageGeometryData geometryDataFromBytes = new GeoPackageGeometryData(
							geometryDataToBytes);
					compareGeometryData(geometryData, geometryDataFromBytes);

					// Set the geometry empty flag and verify the geometry was
					// not written / read
					geometryDataAfterToBytes = cursor.getGeometry();
					geometryDataAfterToBytes.setEmpty(true);
					geometryDataToBytes = geometryDataAfterToBytes.toBytes();
					geometryDataFromBytes = new GeoPackageGeometryData(
							geometryDataToBytes);
					TestCase.assertNull(geometryDataFromBytes.getGeometry());
					compareByteArrays(
							geometryDataAfterToBytes.getHeaderBytes(),
							geometryDataFromBytes.getHeaderBytes());

					// Flip the byte order and verify the header and bytes no
					// longer matches the original, but the geometries still do
					geometryDataAfterToBytes = cursor.getGeometry();
					geometryDataAfterToBytes
							.setByteOrder(geometryDataAfterToBytes
									.getByteOrder() == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN
									: ByteOrder.BIG_ENDIAN);
					geometryDataToBytes = geometryDataAfterToBytes.toBytes();
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
	public static void compareGeometries(GeoPackageGeometry expected,
			GeoPackageGeometry actual) {
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
				comparePoint((GeoPackagePoint) actual,
						(GeoPackagePoint) expected);
				break;
			case LINESTRING:
				compareLineString((GeoPackageLineString) expected,
						(GeoPackageLineString) actual);
				break;
			case POLYGON:
				comparePolygon((GeoPackagePolygon) expected,
						(GeoPackagePolygon) actual);
				break;
			case MULTIPOINT:
				compareMultiPoint((GeoPackageMultiPoint) expected,
						(GeoPackageMultiPoint) actual);
				break;
			case MULTILINESTRING:
				compareMultiLineString((GeoPackageMultiLineString) expected,
						(GeoPackageMultiLineString) actual);
				break;
			case MULTIPOLYGON:
				compareMultiPolygon((GeoPackageMultiPolygon) expected,
						(GeoPackageMultiPolygon) actual);
				break;
			case GEOMETRYCOLLECTION:
				compareGeometryCollection(
						(GeoPackageGeometryCollection<?>) expected,
						(GeoPackageGeometryCollection<?>) actual);
				break;
			case CIRCULARSTRING:
				compareCircularString((GeoPackageCircularString) expected,
						(GeoPackageCircularString) actual);
				break;
			case COMPOUNDCURVE:
				compareCompoundCurve((GeoPackageCompoundCurve) expected,
						(GeoPackageCompoundCurve) actual);
				break;
			case CURVEPOLYGON:
				compareCurvePolygon((GeoPackageCurvePolygon<?>) expected,
						(GeoPackageCurvePolygon<?>) actual);
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
				comparePolyhedralSurface(
						(GeoPackagePolyhedralSurface) expected,
						(GeoPackagePolyhedralSurface) actual);
				break;
			case TIN:
				compareTIN((GeoPackageTIN) expected, (GeoPackageTIN) actual);
				break;
			case TRIANGLE:
				compareTriangle((GeoPackageTriangle) expected,
						(GeoPackageTriangle) actual);
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
	private static void compareBaseGeometryAttributes(
			GeoPackageGeometry expected, GeoPackageGeometry actual) {
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
	private static void comparePoint(GeoPackagePoint expected,
			GeoPackagePoint actual) {

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
	private static void compareLineString(GeoPackageLineString expected,
			GeoPackageLineString actual) {

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
	private static void comparePolygon(GeoPackagePolygon expected,
			GeoPackagePolygon actual) {

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
	private static void compareMultiPoint(GeoPackageMultiPoint expected,
			GeoPackageMultiPoint actual) {

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
	private static void compareMultiLineString(
			GeoPackageMultiLineString expected, GeoPackageMultiLineString actual) {

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
	private static void compareMultiPolygon(GeoPackageMultiPolygon expected,
			GeoPackageMultiPolygon actual) {

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
			GeoPackageGeometryCollection<?> expected,
			GeoPackageGeometryCollection<?> actual) {

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
	private static void compareCircularString(
			GeoPackageCircularString expected, GeoPackageCircularString actual) {

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
	private static void compareCompoundCurve(GeoPackageCompoundCurve expected,
			GeoPackageCompoundCurve actual) {

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
	private static void compareCurvePolygon(GeoPackageCurvePolygon<?> expected,
			GeoPackageCurvePolygon<?> actual) {

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
	private static void comparePolyhedralSurface(
			GeoPackagePolyhedralSurface expected,
			GeoPackagePolyhedralSurface actual) {

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
	private static void compareTIN(GeoPackageTIN expected, GeoPackageTIN actual) {

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
	private static void compareTriangle(GeoPackageTriangle expected,
			GeoPackageTriangle actual) {

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
