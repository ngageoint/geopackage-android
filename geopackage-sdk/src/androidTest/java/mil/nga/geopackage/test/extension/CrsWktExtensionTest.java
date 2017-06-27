package mil.nga.geopackage.test.extension;

import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.CrsWktExtension;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.property.GeoPackageProperties;
import mil.nga.geopackage.property.PropertyConstants;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Geometry Extensions Tests
 *
 * @author osbornb
 */
public class CrsWktExtensionTest extends CreateGeoPackageTestCase {

    /**
     * Test the Extension creation
     */
    public void testExtension() throws Exception {

        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();

        CrsWktExtension wktExtension = new CrsWktExtension(geoPackage);
        assertFalse(wktExtension.has());

        // Test querying and setting the definitions before the column exists
        SpatialReferenceSystem wgs84Srs = srsDao
                .queryForId((long) ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        assertNotNull(wgs84Srs);
        assertNull(wgs84Srs.getDefinition_12_063());
        srsDao.setDefinition_12_063(wgs84Srs);
        assertNull(wgs84Srs.getDefinition_12_063());

        SpatialReferenceSystem undefinedCartesianSrs = srsDao
                .queryForId((long) ProjectionConstants.UNDEFINED_CARTESIAN);
        assertNotNull(undefinedCartesianSrs);
        assertNull(undefinedCartesianSrs.getDefinition_12_063());
        srsDao.setDefinition_12_063(undefinedCartesianSrs);
        assertNull(undefinedCartesianSrs.getDefinition_12_063());

        SpatialReferenceSystem undefinedGeographicSrs = srsDao
                .queryForId((long) ProjectionConstants.UNDEFINED_GEOGRAPHIC);
        assertNotNull(undefinedGeographicSrs);
        assertNull(undefinedGeographicSrs.getDefinition_12_063());
        srsDao.setDefinition_12_063(undefinedGeographicSrs);
        assertNull(undefinedGeographicSrs.getDefinition_12_063());

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
        srsDao.setDefinition_12_063(newSrs);
        assertNull(newSrs.getDefinition_12_063());

        // Create the extension
        Extensions extension = wktExtension.getOrCreate();
        assertNotNull(extension);
        assertTrue(wktExtension.has());
        assertEquals(extension.getExtensionName(), "gpkg_crs_wkt");
        assertEquals(extension.getAuthor(), "gpkg");
        assertEquals(extension.getExtensionNameNoAuthor(), "crs_wkt");
        assertEquals(extension.getTableName(), null);
        assertEquals(extension.getColumnName(), null);
        assertEquals(extension.getScope(), ExtensionScopeType.READ_WRITE);
        assertEquals(extension.getDefinition(), CrsWktExtension.DEFINITION);

        // Test querying and setting the definitions after the column exists
        wgs84Srs = srsDao
                .queryForId((long) ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        assertNotNull(wgs84Srs);
        assertNotNull(wgs84Srs.getDefinition_12_063());
        assertEquals(wgs84Srs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(PropertyConstants.WGS_84,
                        PropertyConstants.DEFINITION_12_063));

        undefinedCartesianSrs = srsDao
                .queryForId((long) ProjectionConstants.UNDEFINED_CARTESIAN);
        assertNotNull(undefinedCartesianSrs);
        assertNotNull(undefinedCartesianSrs.getDefinition_12_063());
        assertEquals(undefinedCartesianSrs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(
                        PropertyConstants.UNDEFINED_CARTESIAN,
                        PropertyConstants.DEFINITION_12_063));

        undefinedGeographicSrs = srsDao
                .queryForId((long) ProjectionConstants.UNDEFINED_GEOGRAPHIC);
        assertNotNull(undefinedGeographicSrs);
        assertNotNull(undefinedGeographicSrs.getDefinition_12_063());
        assertEquals(undefinedGeographicSrs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(
                        PropertyConstants.UNDEFINED_GEOGRAPHIC,
                        PropertyConstants.DEFINITION_12_063));

        newSrs = srsDao.queryForId(newSrs.getSrsId());
        assertNotNull(newSrs);
        assertNotNull(newSrs.getDefinition_12_063());
        assertEquals(newSrs.getDefinition_12_063(), "undefined");

        // Test the get or create auto set
        wgs84Srs = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM) ;
        assertNotNull(wgs84Srs);
        assertNotNull(wgs84Srs.getDefinition_12_063());
        assertEquals(wgs84Srs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(PropertyConstants.WGS_84,
                        PropertyConstants.DEFINITION_12_063));

        undefinedCartesianSrs = srsDao
                .getOrCreateCode(ProjectionConstants.AUTHORITY_NONE,
                        ProjectionConstants.UNDEFINED_CARTESIAN);
        assertNotNull(undefinedCartesianSrs);
        assertNotNull(undefinedCartesianSrs.getDefinition_12_063());
        assertEquals(undefinedCartesianSrs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(
                        PropertyConstants.UNDEFINED_CARTESIAN,
                        PropertyConstants.DEFINITION_12_063));

        undefinedGeographicSrs = srsDao
                .getOrCreateCode(ProjectionConstants.AUTHORITY_NONE,
                        ProjectionConstants.UNDEFINED_GEOGRAPHIC);
        assertNotNull(undefinedGeographicSrs);
        assertNotNull(undefinedGeographicSrs.getDefinition_12_063());
        assertEquals(undefinedGeographicSrs.getDefinition_12_063(),
                GeoPackageProperties.getProperty(
                        PropertyConstants.UNDEFINED_GEOGRAPHIC,
                        PropertyConstants.DEFINITION_12_063));

        // Create the web mercator srs and test
        SpatialReferenceSystem webMercator = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WEB_MERCATOR);
        assertNotNull(webMercator.getDefinition_12_063());

        // Read the web mercator srs and test
        SpatialReferenceSystem webMercator2 = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WEB_MERCATOR);
        assertNotNull(webMercator2.getDefinition_12_063());
        assertEquals(webMercator.getDefinition_12_063(),
                webMercator2.getDefinition_12_063());

        // Create a new SRS with new definition
        SpatialReferenceSystem newSrs2 = new SpatialReferenceSystem();
        newSrs2.setSrsName("name");
        newSrs2.setSrsId(4321);
        newSrs2.setOrganization("organization");
        newSrs2.setOrganizationCoordsysId(1234);
        newSrs2.setDefinition("definition");
        newSrs2.setDescription("description");
        newSrs2.setDefinition_12_063("definition_12_063");
        srsDao.create(newSrs2);
        newSrs2 = srsDao.queryForId(newSrs2.getSrsId());
        assertNotNull(newSrs2);
        assertNotNull(newSrs2.getDefinition_12_063());
        assertEquals(newSrs2.getDefinition_12_063(), "definition_12_063");

        // Create a new SRS without specifying new definition
        SpatialReferenceSystem newSrs3 = new SpatialReferenceSystem();
        newSrs3.setSrsName("name");
        newSrs3.setSrsId(1324);
        newSrs3.setOrganization("organization");
        newSrs3.setOrganizationCoordsysId(1234);
        newSrs3.setDefinition("definition");
        newSrs3.setDescription("description");
        srsDao.create(newSrs3);
        newSrs3 = srsDao.queryForId(newSrs3.getSrsId());
        assertNotNull(newSrs3);
        assertNotNull(newSrs3.getDefinition_12_063());
        assertEquals(newSrs3.getDefinition_12_063(), "undefined");

    }

}
