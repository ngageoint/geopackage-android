package mil.nga.geopackage.test.extension;

import org.junit.Test;

import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.GeoPackageExtensions;
import mil.nga.geopackage.extension.WebPExtension;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
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

        GeoPackageExtensions.deleteTableExtensions(geoPackage, tableName);
        assertFalse(webpExtension.has(tableName));

    }

}
