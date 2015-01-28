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
import mil.nga.giat.geopackage.geom.GeoPackageGeometry;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryEnvelope;

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
					// longer matches the original
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
	private static void compareGeometryData(GeoPackageGeometryData expected,
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
	private static void compareGeometries(GeoPackageGeometry expected,
			GeoPackageGeometry actual) {
		if (expected == null) {
			TestCase.assertNull(actual);
		} else {
			TestCase.assertNotNull(actual);
			TestCase.assertEquals(expected.getGeometryType(),
					actual.getGeometryType());

			// TODO
		}
	}

	/**
	 * Compare two byte arrays and verify they are equal
	 * 
	 * @param expected
	 * @param actual
	 */
	private static void compareByteArrays(byte[] expected, byte[] actual) {

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
	private static boolean equalByteArrays(byte[] expected, byte[] actual) {

		boolean equal = expected.length == actual.length;

		for (int i = 0; equal && i < expected.length; i++) {
			equal = expected[i] == actual[i];
		}

		return equal;
	}

}
