package mil.nga.geopackage.test.extension;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.ExtensionsDao;
import mil.nga.geopackage.extension.GeometryExtensions;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.wkb.geom.GeometryType;

/**
 * Geometry Extensions Tests
 *
 * @author osbornb
 */
public class GeometryExtensionsTest extends CreateGeoPackageTestCase {

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
     * Test the Geometry Extension creation
     */
    public void testGeometryExtension() throws Exception {

        GeometryExtensions extensions = new GeometryExtensions(geoPackage);
        ExtensionsDao extensionsDao = extensions.getExtensionsDao();

        // Test non extension geometries
        for (int i = GeometryType.GEOMETRY.getCode(); i <= GeometryType.GEOMETRYCOLLECTION
                .getCode(); i++) {

            GeometryType geometryType = GeometryType.fromCode(i);
            try {
                extensions.getOrCreate("table_name", "column_name",
                        geometryType);
                fail("Geometry Extension was created for " + geometryType);
            } catch (GeoPackageException e) {
                // Expected
            }
        }

        // Test user created extension geometries
        for (int i = GeometryType.POLYHEDRALSURFACE.getCode(); i <= GeometryType.TRIANGLE
                .getCode(); i++) {

            GeometryType geometryType = GeometryType.fromCode(i);
            try {
                extensions.getOrCreate("table_name", "column_name",
                        geometryType);
                fail("Geometry Extension was created for " + geometryType);
            } catch (GeoPackageException e) {
                // Expected
            }
        }

        // Test geometry extensions
        long count = extensionsDao.countOf();
        for (int i = GeometryType.CIRCULARSTRING.getCode(); i <= GeometryType.SURFACE
                .getCode(); i++) {

            GeometryType geometryType = GeometryType.fromCode(i);
            String tableName = "table_" + geometryType.name();
            String columnName = "geom";
            Extensions extension = extensions.getOrCreate(tableName,
                    columnName, geometryType);
            assertNotNull(extension);
            assertTrue(extensions.has(tableName, columnName, geometryType));
            assertEquals(++count, extensionsDao.countOf());

            assertEquals(extension.getExtensionName(),
                    expectedGeoPackageExtensionName(geometryType));
            assertEquals(extension.getAuthor(),
                    expectedGeoPackageExtensionAuthor());
            assertEquals(extension.getExtensionNameNoAuthor(),
                    expectedGeoPackageExtensionNameNoAuthor(geometryType));
            assertEquals(extension.getTableName(), tableName);
            assertEquals(extension.getColumnName(), columnName);
            assertEquals(extension.getScope(), ExtensionScopeType.READ_WRITE);
            assertEquals(extension.getDefinition(),
                    GeometryExtensions.GEOMETRY_TYPES_EXTENSION_DEFINITION);
        }
    }

    /**
     * Test the User Geometry Extension creation
     */
    public void testUserGeometryExtension() throws Exception {

        GeometryExtensions extensions = new GeometryExtensions(geoPackage);
        ExtensionsDao extensionsDao = extensions.getExtensionsDao();

        String author = "nga";

        // Test non extension geometries
        for (int i = GeometryType.GEOMETRY.getCode(); i <= GeometryType.GEOMETRYCOLLECTION
                .getCode(); i++) {

            GeometryType geometryType = GeometryType.fromCode(i);
            try {
                extensions.getOrCreate("table_name", "column_name", author,
                        geometryType);
                fail("Geometry Extension was created for " + geometryType);
            } catch (GeoPackageException e) {
                // Expected
            }
        }

        // Test geometry extensions and user created extensions with author
        long count = extensionsDao.countOf();
        for (int i = GeometryType.CIRCULARSTRING.getCode(); i <= GeometryType.TRIANGLE
                .getCode(); i++) {

            GeometryType geometryType = GeometryType.fromCode(i);
            String tableName = "table_" + geometryType.name();
            String columnName = "geom";
            Extensions extension = extensions.getOrCreate(tableName,
                    columnName, author, geometryType);
            assertNotNull(extension);
            assertTrue(extensions.has(tableName, columnName, author,
                    geometryType));
            assertEquals(++count, extensionsDao.countOf());

            assertEquals(extension.getExtensionNameNoAuthor(),
                    expectedGeoPackageExtensionNameNoAuthor(geometryType));
            assertEquals(extension.getTableName(), tableName);
            assertEquals(extension.getColumnName(), columnName);
            assertEquals(extension.getScope(), ExtensionScopeType.READ_WRITE);

            if (i <= GeometryType.SURFACE.getCode()) {
                assertEquals(extension.getExtensionName(),
                        expectedGeoPackageExtensionName(geometryType));
                assertEquals(extension.getAuthor(),
                        expectedGeoPackageExtensionAuthor());
                assertEquals(extension.getDefinition(),
                        GeometryExtensions.GEOMETRY_TYPES_EXTENSION_DEFINITION);
            } else {
                assertEquals(extension.getExtensionName(),
                        expectedUserDefinedExtensionName(author, geometryType));
                assertEquals(extension.getAuthor(), author);
                assertEquals(
                        extension.getDefinition(),
                        GeometryExtensions.USER_GEOMETRY_TYPES_EXTENSION_DEFINITION);
            }
        }

    }

    /**
     * Get the expected GeoPackage extension name
     *
     * @param type
     * @return
     */
    private String expectedGeoPackageExtensionName(GeometryType type) {
        return expectedGeoPackageExtensionAuthor() + "_"
                + expectedGeoPackageExtensionNameNoAuthor(type);
    }

    /**
     * Get the expected GeoPackage extension author
     *
     * @return
     */
    private String expectedGeoPackageExtensionAuthor() {
        return "gpkg";
    }

    /**
     * Get the expected GeoPackage extension name with no author
     *
     * @param type
     * @return
     */
    private String expectedGeoPackageExtensionNameNoAuthor(GeometryType type) {
        return "geom_" + type.getName();
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
