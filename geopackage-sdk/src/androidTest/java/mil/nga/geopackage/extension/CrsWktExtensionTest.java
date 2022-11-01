package mil.nga.geopackage.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

import mil.nga.geopackage.CreateGeoPackageTestCase;
import mil.nga.geopackage.property.GeoPackageProperties;
import mil.nga.geopackage.property.PropertyConstants;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.srs.SpatialReferenceSystemDao;
import mil.nga.proj.ProjectionConstants;

/**
 * Geometry Extensions Tests
 *
 * @author osbornb
 */
public class CrsWktExtensionTest extends CreateGeoPackageTestCase {

    /**
     * Test the Extension version 1
     *
     * @throws Exception upon failure
     */
    @Test
    public void testVersion1() throws Exception {
        testExtension(CrsWktExtensionVersion.V_1);
    }

    /**
     * Test the Extension version 1.1
     *
     * @throws Exception upon failure
     */
    @Test
    public void testVersion1_1() throws Exception {
        testExtension(CrsWktExtensionVersion.V_1_1);
    }

    /**
     * Test the Extension latest version
     *
     * @throws Exception upon failure
     */
    @Test
    public void testLatestVersion() throws Exception {
        testExtension(null);
    }

    /**
     * Test the extension for the version
     *
     * @param version extension version
     * @throws Exception upon failure
     */
    private void testExtension(CrsWktExtensionVersion version)
            throws Exception {

        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();

        CrsWktExtension wktExtension = new CrsWktExtension(geoPackage);
        assertFalse(wktExtension.has());
        if (version != null) {
            assertFalse(wktExtension.has(version));
            assertFalse(wktExtension.hasMinimum(version));
        }

        // Test querying and setting the definitions before the column exists
        SpatialReferenceSystem wgs84Srs = srsDao
                .queryForId((long) ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        assertNotNull(wgs84Srs);
        assertNull(wgs84Srs.getDefinition_12_063());
        assertNull(wgs84Srs.getEpoch());
        srsDao.setExtension(wgs84Srs);
        assertNull(wgs84Srs.getDefinition_12_063());
        assertNull(wgs84Srs.getEpoch());

        SpatialReferenceSystem undefinedCartesianSrs = srsDao
                .queryForId((long) ProjectionConstants.UNDEFINED_CARTESIAN);
        assertNotNull(undefinedCartesianSrs);
        assertNull(undefinedCartesianSrs.getDefinition_12_063());
        assertNull(undefinedCartesianSrs.getEpoch());
        srsDao.setExtension(undefinedCartesianSrs);
        assertNull(undefinedCartesianSrs.getDefinition_12_063());
        assertNull(undefinedCartesianSrs.getEpoch());

        SpatialReferenceSystem undefinedGeographicSrs = srsDao
                .queryForId((long) ProjectionConstants.UNDEFINED_GEOGRAPHIC);
        assertNotNull(undefinedGeographicSrs);
        assertNull(undefinedGeographicSrs.getDefinition_12_063());
        assertNull(undefinedGeographicSrs.getEpoch());
        srsDao.setExtension(undefinedGeographicSrs);
        assertNull(undefinedGeographicSrs.getDefinition_12_063());
        assertNull(undefinedGeographicSrs.getEpoch());

        // Create a new SRS
        SpatialReferenceSystem newSrs = new SpatialReferenceSystem();
        newSrs.setSrsName("name");
        newSrs.setSrsId(1234);
        newSrs.setOrganization("organization");
        newSrs.setOrganizationCoordsysId(1234);
        newSrs.setDefinition("definition");
        newSrs.setDescription("description");
        srsDao.create(newSrs);
        newSrs = srsDao.queryForId(newSrs.getSrsId());
        assertNotNull(newSrs);
        assertNull(newSrs.getDefinition_12_063());
        assertNull(newSrs.getEpoch());
        srsDao.setExtension(newSrs);
        assertNull(newSrs.getDefinition_12_063());
        assertNull(newSrs.getEpoch());

        // Create the extension
        List<Extensions> extensions = null;
        if (version != null) {
            extensions = wktExtension.getOrCreate(version);
        } else {
            extensions = wktExtension.getOrCreate();
        }
        assertTrue(wktExtension.has());
        if (version != null) {
            assertTrue(wktExtension.has(version));
            assertTrue(wktExtension.hasMinimum(version));
        }
        Extensions extension = extensions.get(0);
        assertNotNull(extension);
        assertEquals(extension.getExtensionName(), "gpkg_crs_wkt");
        assertEquals(extension.getAuthor(), "gpkg");
        assertEquals(extension.getExtensionNameNoAuthor(), "crs_wkt");
        assertEquals(extension.getTableName(), "gpkg_spatial_ref_sys");
        assertEquals(extension.getColumnName(), "definition_12_063");
        assertEquals(extension.getScope(), ExtensionScopeType.READ_WRITE);
        assertEquals(extension.getDefinition(), CrsWktExtension.DEFINITION_V_1);
        if (version == null
                || version.isMinimum(CrsWktExtensionVersion.V_1_1)) {
            extension = extensions.get(1);
            assertNotNull(extension);
            assertEquals(extension.getExtensionName(), "gpkg_crs_wkt_1_1");
            assertEquals(extension.getAuthor(), "gpkg");
            assertEquals(extension.getExtensionNameNoAuthor(), "crs_wkt_1_1");
            assertEquals(extension.getTableName(), "gpkg_spatial_ref_sys");
            assertEquals(extension.getColumnName(), "definition_12_063");
            assertEquals(extension.getScope(), ExtensionScopeType.READ_WRITE);
            assertEquals(extension.getDefinition(),
                    CrsWktExtension.DEFINITION_V_1_1);
            extension = extensions.get(2);
            assertNotNull(extension);
            assertEquals(extension.getExtensionName(), "gpkg_crs_wkt_1_1");
            assertEquals(extension.getAuthor(), "gpkg");
            assertEquals(extension.getExtensionNameNoAuthor(), "crs_wkt_1_1");
            assertEquals(extension.getTableName(), "gpkg_spatial_ref_sys");
            assertEquals(extension.getColumnName(), "epoch");
            assertEquals(extension.getScope(), ExtensionScopeType.READ_WRITE);
            assertEquals(extension.getDefinition(),
                    CrsWktExtension.DEFINITION_V_1_1);
        }

        // Test querying and setting the definitions after the column exists
        wgs84Srs = srsDao
                .queryForId((long) ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        assertNotNull(wgs84Srs);
        assertNotNull(wgs84Srs.getDefinition_12_063());
        assertEquals(wgs84Srs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(PropertyConstants.WGS_84,
                        PropertyConstants.DEFINITION_12_063));
        assertNull(wgs84Srs.getEpoch());

        undefinedCartesianSrs = srsDao
                .queryForId((long) ProjectionConstants.UNDEFINED_CARTESIAN);
        assertNotNull(undefinedCartesianSrs);
        assertNotNull(undefinedCartesianSrs.getDefinition_12_063());
        assertEquals(undefinedCartesianSrs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(
                        PropertyConstants.UNDEFINED_CARTESIAN,
                        PropertyConstants.DEFINITION_12_063));
        assertNull(undefinedCartesianSrs.getEpoch());

        undefinedGeographicSrs = srsDao
                .queryForId((long) ProjectionConstants.UNDEFINED_GEOGRAPHIC);
        assertNotNull(undefinedGeographicSrs);
        assertNotNull(undefinedGeographicSrs.getDefinition_12_063());
        assertEquals(undefinedGeographicSrs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(
                        PropertyConstants.UNDEFINED_GEOGRAPHIC,
                        PropertyConstants.DEFINITION_12_063));
        assertNull(undefinedGeographicSrs.getEpoch());

        newSrs = srsDao.queryForId(newSrs.getSrsId());
        assertNotNull(newSrs);
        assertNotNull(newSrs.getDefinition_12_063());
        assertEquals(newSrs.getDefinition_12_063(), "");
        assertNull(newSrs.getEpoch());

        // Test the get or create auto set
        wgs84Srs = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        assertNotNull(wgs84Srs);
        assertNotNull(wgs84Srs.getDefinition_12_063());
        assertEquals(wgs84Srs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(PropertyConstants.WGS_84,
                        PropertyConstants.DEFINITION_12_063));
        assertNull(wgs84Srs.getEpoch());

        undefinedCartesianSrs = srsDao
                .getOrCreateCode(ProjectionConstants.AUTHORITY_NONE,
                        ProjectionConstants.UNDEFINED_CARTESIAN);
        assertNotNull(undefinedCartesianSrs);
        assertNotNull(undefinedCartesianSrs.getDefinition_12_063());
        assertEquals(undefinedCartesianSrs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(
                        PropertyConstants.UNDEFINED_CARTESIAN,
                        PropertyConstants.DEFINITION_12_063));
        assertNull(undefinedCartesianSrs.getEpoch());

        undefinedGeographicSrs = srsDao
                .getOrCreateCode(ProjectionConstants.AUTHORITY_NONE,
                        ProjectionConstants.UNDEFINED_GEOGRAPHIC);
        assertNotNull(undefinedGeographicSrs);
        assertNotNull(undefinedGeographicSrs.getDefinition_12_063());
        assertEquals(undefinedGeographicSrs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(
                        PropertyConstants.UNDEFINED_GEOGRAPHIC,
                        PropertyConstants.DEFINITION_12_063));
        assertNull(undefinedGeographicSrs.getEpoch());

        // Create the web mercator srs and test
        SpatialReferenceSystem webMercator = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WEB_MERCATOR);
        assertNotNull(webMercator.getDefinition_12_063());
        assertNull(webMercator.getEpoch());

        // Read the web mercator srs and test
        SpatialReferenceSystem webMercator2 = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WEB_MERCATOR);
        assertNotNull(webMercator2.getDefinition_12_063());
        assertEquals(webMercator.getDefinition_12_063(),
                webMercator2.getDefinition_12_063());
        assertNull(webMercator2.getEpoch());

        // Create a new SRS with new definition
        SpatialReferenceSystem newSrs2 = new SpatialReferenceSystem();
        newSrs2.setSrsName("name");
        newSrs2.setSrsId(4321);
        newSrs2.setOrganization("organization");
        newSrs2.setOrganizationCoordsysId(4321);
        newSrs2.setDefinition("definition");
        newSrs2.setDescription("description");
        newSrs2.setDefinition_12_063("definition_12_063");
        srsDao.create(newSrs2);
        newSrs2 = srsDao.queryForId(newSrs2.getSrsId());
        assertNotNull(newSrs2);
        assertNotNull(newSrs2.getDefinition_12_063());
        assertEquals(newSrs2.getDefinition_12_063(), "definition_12_063");
        assertNull(newSrs2.getEpoch());
        newSrs2.setDefinition_12_063(null);
        srsDao.updateExtension(newSrs2);
        newSrs2 = srsDao.queryForId(newSrs2.getSrsId());
        assertNotNull(newSrs2);
        assertNotNull(newSrs2.getDefinition_12_063());
        assertEquals(newSrs2.getDefinition_12_063(), "");
        newSrs2.setDefinition_12_063("definition_12_063 2");
        srsDao.updateExtension(newSrs2);
        newSrs2 = srsDao.queryForId(newSrs2.getSrsId());
        assertNotNull(newSrs2);
        assertNotNull(newSrs2.getDefinition_12_063());
        assertEquals(newSrs2.getDefinition_12_063(), "definition_12_063 2");

        // Create a new SRS without specifying new definition
        SpatialReferenceSystem newSrs3 = new SpatialReferenceSystem();
        newSrs3.setSrsName("name");
        newSrs3.setSrsId(1324);
        newSrs3.setOrganization("organization");
        newSrs3.setOrganizationCoordsysId(1324);
        newSrs3.setDefinition("definition");
        newSrs3.setDescription("description");
        srsDao.create(newSrs3);
        newSrs3 = srsDao.queryForId(newSrs3.getSrsId());
        assertNotNull(newSrs3);
        assertNotNull(newSrs3.getDefinition_12_063());
        assertEquals(newSrs3.getDefinition_12_063(), "");

        // Create a new SRS with new definition and epoch
        SpatialReferenceSystem newSrs4 = new SpatialReferenceSystem();
        newSrs4.setSrsName("name");
        newSrs4.setSrsId(5678);
        newSrs4.setOrganization("organization");
        newSrs4.setOrganizationCoordsysId(5678);
        newSrs4.setDefinition("definition");
        newSrs4.setDescription("description");
        newSrs4.setDefinition_12_063("definition_12_063");
        newSrs4.setEpoch(12.345);
        srsDao.create(newSrs4);
        newSrs4 = srsDao.queryForId(newSrs4.getSrsId());
        assertNotNull(newSrs4);
        assertNotNull(newSrs4.getDefinition_12_063());
        assertEquals(newSrs4.getDefinition_12_063(), "definition_12_063");
        if (version == null
                || version.isMinimum(CrsWktExtensionVersion.V_1_1)) {
            assertNotNull(newSrs4.getEpoch());
            assertEquals(newSrs4.getEpoch(), 12.345, 0.0);
            newSrs4.setEpoch(null);
            srsDao.updateExtension(newSrs4);
            newSrs4 = srsDao.queryForId(newSrs4.getSrsId());
            assertNotNull(newSrs4);
            assertNull(newSrs4.getEpoch());
            newSrs4.setEpoch(543.21);
            srsDao.updateExtension(newSrs4);
            newSrs4 = srsDao.queryForId(newSrs4.getSrsId());
            assertNotNull(newSrs4);
            assertNotNull(newSrs4.getEpoch());
            assertEquals(newSrs4.getEpoch(), 543.21, 0.0);
        } else {
            assertNull(newSrs4.getEpoch());
        }

    }

}
