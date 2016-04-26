package mil.nga.geopackage.test.extension;

import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.GeoPackageExtensions;
import mil.nga.geopackage.extension.ZoomOtherExtension;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.tiles.user.TileTable;

/**
 * Zoom Other Extension Tests
 *
 * @author osbornb
 */
public class ZoomOtherExtensionTest extends CreateGeoPackageTestCase {

    /**
     * Test the Zoom Other Extension creation
     */
    public void testZoomOtherExtension() throws Exception {

        ZoomOtherExtension zoomOtherExtension = new ZoomOtherExtension(
                geoPackage);

        String tableName = "table";

        Extensions extension = zoomOtherExtension.getOrCreate(tableName);
        assertNotNull(extension);
        assertTrue(zoomOtherExtension.has(tableName));

        assertEquals(extension.getExtensionName(),
                ZoomOtherExtension.EXTENSION_NAME);
        assertEquals(extension.getAuthor(),
                GeoPackageConstants.GEO_PACKAGE_EXTENSION_AUTHOR);
        assertEquals(extension.getExtensionNameNoAuthor(),
                ZoomOtherExtension.NAME);
        assertEquals(extension.getTableName(), tableName);
        assertEquals(extension.getColumnName(), TileTable.COLUMN_TILE_ROW);
        assertEquals(extension.getScope(), ExtensionScopeType.READ_WRITE);
        assertEquals(extension.getDefinition(), ZoomOtherExtension.DEFINITION);

        GeoPackageExtensions.deleteTableExtensions(geoPackage, tableName);
        assertFalse(zoomOtherExtension.has(tableName));

    }

}
