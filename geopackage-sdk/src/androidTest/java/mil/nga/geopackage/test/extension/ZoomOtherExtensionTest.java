package mil.nga.geopackage.test.extension;

import org.junit.Test;

import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.ZoomOtherExtension;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.tiles.user.TileTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Zoom Other Extension Tests
 *
 * @author osbornb
 */
public class ZoomOtherExtensionTest extends CreateGeoPackageTestCase {

    /**
     * Test the Zoom Other Extension creation
     */
    @Test
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
                GeoPackageConstants.EXTENSION_AUTHOR);
        assertEquals(extension.getExtensionNameNoAuthor(),
                ZoomOtherExtension.NAME);
        assertEquals(extension.getTableName(), tableName);
        assertEquals(extension.getColumnName(), TileTable.COLUMN_TILE_DATA);
        assertEquals(extension.getScope(), ExtensionScopeType.READ_WRITE);
        assertEquals(extension.getDefinition(), ZoomOtherExtension.DEFINITION);

        geoPackage.getExtensionManager().deleteTableExtensions(tableName);
        assertFalse(zoomOtherExtension.has(tableName));

    }

}
