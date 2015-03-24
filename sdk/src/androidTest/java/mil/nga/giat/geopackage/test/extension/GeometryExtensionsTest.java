package mil.nga.giat.geopackage.test.extension;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.extension.GeometryExtensions;
import mil.nga.giat.wkb.geom.GeometryType;
import mil.nga.giat.geopackage.test.BaseTestCase;

/**
 * Geometry Extensions Tests
 * 
 * @author osbornb
 */
public class GeometryExtensionsTest extends BaseTestCase {

	/**
	 * Test the is extension check
	 */
	public void testIsExtension() {

		assertFalse(GeometryExtensions.isExtension(GeometryType.GEOMETRY));
		assertFalse(GeometryExtensions.isExtension(GeometryType.POINT));
		assertFalse(GeometryExtensions.isExtension(GeometryType.LINESTRING));
		assertFalse(GeometryExtensions.isExtension(GeometryType.POLYGON));
		assertFalse(GeometryExtensions.isExtension(GeometryType.MULTIPOINT));
		assertFalse(GeometryExtensions
				.isExtension(GeometryType.MULTILINESTRING));
		assertFalse(GeometryExtensions.isExtension(GeometryType.MULTIPOLYGON));
		assertFalse(GeometryExtensions
				.isExtension(GeometryType.GEOMETRYCOLLECTION));

		assertTrue(GeometryExtensions.isExtension(GeometryType.CIRCULARSTRING));
		assertTrue(GeometryExtensions.isExtension(GeometryType.COMPOUNDCURVE));
		assertTrue(GeometryExtensions.isExtension(GeometryType.CURVEPOLYGON));
		assertTrue(GeometryExtensions.isExtension(GeometryType.MULTICURVE));
		assertTrue(GeometryExtensions.isExtension(GeometryType.MULTISURFACE));
		assertTrue(GeometryExtensions.isExtension(GeometryType.CURVE));
		assertTrue(GeometryExtensions.isExtension(GeometryType.SURFACE));

		assertTrue(GeometryExtensions
				.isExtension(GeometryType.POLYHEDRALSURFACE));
		assertTrue(GeometryExtensions.isExtension(GeometryType.TIN));
		assertTrue(GeometryExtensions.isExtension(GeometryType.TRIANGLE));

	}

	/**
	 * Test the is GeoPackage extension check
	 */
	public void testIsGeoPackageExtension() {

		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.GEOMETRY));
		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.POINT));
		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.LINESTRING));
		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.POLYGON));
		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.MULTIPOINT));
		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.MULTILINESTRING));
		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.MULTIPOLYGON));
		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.GEOMETRYCOLLECTION));

		assertTrue(GeometryExtensions
				.isGeoPackageExtension(GeometryType.CIRCULARSTRING));
		assertTrue(GeometryExtensions
				.isGeoPackageExtension(GeometryType.COMPOUNDCURVE));
		assertTrue(GeometryExtensions
				.isGeoPackageExtension(GeometryType.CURVEPOLYGON));
		assertTrue(GeometryExtensions
				.isGeoPackageExtension(GeometryType.MULTICURVE));
		assertTrue(GeometryExtensions
				.isGeoPackageExtension(GeometryType.MULTISURFACE));
		assertTrue(GeometryExtensions.isGeoPackageExtension(GeometryType.CURVE));
		assertTrue(GeometryExtensions
				.isGeoPackageExtension(GeometryType.SURFACE));

		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.POLYHEDRALSURFACE));
		assertFalse(GeometryExtensions.isGeoPackageExtension(GeometryType.TIN));
		assertFalse(GeometryExtensions
				.isGeoPackageExtension(GeometryType.TRIANGLE));

	}

	/**
	 * Test the GeoPackage get extension name
	 */
	public void testGeoPackageExtensionName() {

		try {
			GeometryExtensions.getExtensionName(GeometryType.GEOMETRY);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(GeometryType.POINT);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(GeometryType.LINESTRING);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(GeometryType.POLYGON);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(GeometryType.MULTIPOINT);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(GeometryType.MULTILINESTRING);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(GeometryType.MULTIPOLYGON);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions
					.getExtensionName(GeometryType.GEOMETRYCOLLECTION);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}

		assertEquals(
				expectedGeoPackageExtensionName(GeometryType.CIRCULARSTRING),
				GeometryExtensions
						.getExtensionName(GeometryType.CIRCULARSTRING));
		assertEquals(
				expectedGeoPackageExtensionName(GeometryType.COMPOUNDCURVE),
				GeometryExtensions.getExtensionName(GeometryType.COMPOUNDCURVE));
		assertEquals(
				expectedGeoPackageExtensionName(GeometryType.CURVEPOLYGON),
				GeometryExtensions.getExtensionName(GeometryType.CURVEPOLYGON));
		assertEquals(expectedGeoPackageExtensionName(GeometryType.MULTICURVE),
				GeometryExtensions.getExtensionName(GeometryType.MULTICURVE));
		assertEquals(
				expectedGeoPackageExtensionName(GeometryType.MULTISURFACE),
				GeometryExtensions.getExtensionName(GeometryType.MULTISURFACE));
		assertEquals(expectedGeoPackageExtensionName(GeometryType.CURVE),
				GeometryExtensions.getExtensionName(GeometryType.CURVE));
		assertEquals(expectedGeoPackageExtensionName(GeometryType.SURFACE),
				GeometryExtensions.getExtensionName(GeometryType.SURFACE));

		try {
			GeometryExtensions.getExtensionName(GeometryType.POLYHEDRALSURFACE);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(GeometryType.TIN);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(GeometryType.TRIANGLE);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}

	}

	/**
	 * Test the get extension name
	 */
	public void testExtensionName() {

		String author = "nga";

		try {
			GeometryExtensions.getExtensionName(author, GeometryType.GEOMETRY);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(author, GeometryType.POINT);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions
					.getExtensionName(author, GeometryType.LINESTRING);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(author, GeometryType.POLYGON);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions
					.getExtensionName(author, GeometryType.MULTIPOINT);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(author,
					GeometryType.MULTILINESTRING);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(author,
					GeometryType.MULTIPOLYGON);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}
		try {
			GeometryExtensions.getExtensionName(author,
					GeometryType.GEOMETRYCOLLECTION);
			fail();
		} catch (GeoPackageException e) {
			// expected
		}

		assertEquals(
				expectedGeoPackageExtensionName(GeometryType.CIRCULARSTRING),
				GeometryExtensions.getExtensionName(author,
						GeometryType.CIRCULARSTRING));
		assertEquals(
				expectedGeoPackageExtensionName(GeometryType.COMPOUNDCURVE),
				GeometryExtensions.getExtensionName(author,
						GeometryType.COMPOUNDCURVE));
		assertEquals(
				expectedGeoPackageExtensionName(GeometryType.CURVEPOLYGON),
				GeometryExtensions.getExtensionName(author,
						GeometryType.CURVEPOLYGON));
		assertEquals(expectedGeoPackageExtensionName(GeometryType.MULTICURVE),
				GeometryExtensions.getExtensionName(author,
						GeometryType.MULTICURVE));
		assertEquals(
				expectedGeoPackageExtensionName(GeometryType.MULTISURFACE),
				GeometryExtensions.getExtensionName(author,
						GeometryType.MULTISURFACE));
		assertEquals(expectedGeoPackageExtensionName(GeometryType.CURVE),
				GeometryExtensions.getExtensionName(author, GeometryType.CURVE));
		assertEquals(expectedGeoPackageExtensionName(GeometryType.SURFACE),
				GeometryExtensions.getExtensionName(author,
						GeometryType.SURFACE));

		assertEquals(
				expectedUserDefinedExtensionName(author,
						GeometryType.POLYHEDRALSURFACE),
				GeometryExtensions.getExtensionName(author,
						GeometryType.POLYHEDRALSURFACE));
		assertEquals(
				expectedUserDefinedExtensionName(author, GeometryType.TIN),
				GeometryExtensions.getExtensionName(author, GeometryType.TIN));
		assertEquals(
				expectedUserDefinedExtensionName(author, GeometryType.TRIANGLE),
				GeometryExtensions.getExtensionName(author,
						GeometryType.TRIANGLE));

	}

	/**
	 * Get the expected GeoPackage extension name
	 * 
	 * @param type
	 * @return
	 */
	private String expectedGeoPackageExtensionName(GeometryType type) {
		return "gpkg_geom_" + type.getName();
	}

	/**
	 * Get the expected User-Defined extension name
	 * 
	 * @param author
	 * @param type
	 * @return
	 */
	private String expectedUserDefinedExtensionName(String author,
			GeometryType type) {
		return author + "_geom_" + type.getName();
	}

}
