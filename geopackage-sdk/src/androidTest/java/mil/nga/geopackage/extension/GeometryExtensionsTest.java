package mil.nga.geopackage.extension;

import org.junit.Test;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.CreateGeoPackageTestCase;
import mil.nga.sf.GeometryType;
import mil.nga.sf.wkb.GeometryCodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Geometry Extensions Tests
 *
 * @author osbornb
 */
public class GeometryExtensionsTest extends CreateGeoPackageTestCase {

    /**
     * Test the is extension check
     */
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
    public void testGeometryExtension() throws Exception {

        GeometryExtensions extensions = new GeometryExtensions(geoPackage);
        ExtensionsDao extensionsDao = extensions.getExtensionsDao();

        // Test non extension geometries
        for (int i = GeometryCodes.getCode(GeometryType.GEOMETRY); i <= GeometryCodes.getCode(GeometryType.GEOMETRYCOLLECTION); i++) {

            GeometryType geometryType = GeometryCodes.getGeometryType(i);
            try {
                extensions.getOrCreate("table_name", "column_name",
                        geometryType);
                fail("Geometry Extension was created for " + geometryType);
            } catch (GeoPackageException e) {
                // Expected
            }
        }

        // Test user created extension geometries
        for (int i = GeometryCodes.getCode(GeometryType.POLYHEDRALSURFACE); i <= GeometryCodes.getCode(GeometryType.TRIANGLE); i++) {

            GeometryType geometryType = GeometryCodes.getGeometryType(i);
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
        for (int i = GeometryCodes.getCode(GeometryType.CIRCULARSTRING); i <= GeometryCodes.getCode(GeometryType.SURFACE); i++) {

            GeometryType geometryType = GeometryCodes.getGeometryType(i);
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
    @Test
    public void testUserGeometryExtension() throws Exception {

        GeometryExtensions extensions = new GeometryExtensions(geoPackage);
        ExtensionsDao extensionsDao = extensions.getExtensionsDao();

        String author = "nga";

        // Test non extension geometries
        for (int i = GeometryCodes.getCode(GeometryType.GEOMETRY); i <= GeometryCodes.getCode(GeometryType.GEOMETRYCOLLECTION); i++) {

            GeometryType geometryType = GeometryCodes.getGeometryType(i);
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
        for (int i = GeometryCodes.getCode(GeometryType.CIRCULARSTRING); i <= GeometryCodes.getCode(GeometryType.TRIANGLE); i++) {

            GeometryType geometryType = GeometryCodes.getGeometryType(i);
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

            if (i <= GeometryCodes.getCode(GeometryType.SURFACE)) {
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
