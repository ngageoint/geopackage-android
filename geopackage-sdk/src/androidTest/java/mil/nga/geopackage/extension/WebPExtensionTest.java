package mil.nga.geopackage.extension;

import org.junit.Test;

import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.CreateGeoPackageTestCase;
import mil.nga.geopackage.tiles.user.TileTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * WebP Extension Tests
 *
 * @author osbornb
 */
public class WebPExtensionTest extends CreateGeoPackageTestCase {

    /**
     * Test the WebP Extension creation
     */
    @Test
    public void testWebPExtension() throws Exception {

        WebPExtension webpExtension = new WebPExtension(geoPackage);

        String tableName = "table";

        Extensions extension = webpExtension.getOrCreate(tableName);
        assertNotNull(extension);
        assertTrue(webpExtension.has(tableName));

        assertEquals(extension.getExtensionName(), WebPExtension.EXTENSION_NAME);
        assertEquals(extension.getAuthor(),
                GeoPackageConstants.EXTENSION_AUTHOR);
        assertEquals(extension.getExtensionNameNoAuthor(), WebPExtension.NAME);
        assertEquals(extension.getTableName(), tableName);
        assertEquals(extension.getColumnName(), TileTable.COLUMN_TILE_DATA);
        assertEquals(extension.getScope(), ExtensionScopeType.READ_WRITE);
        assertEquals(extension.getDefinition(), WebPExtension.DEFINITION);

        geoPackage.getExtensionManager().deleteTableExtensions(tableName);
        assertFalse(webpExtension.has(tableName));

    }

}
