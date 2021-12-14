package mil.nga.geopackage.extension.nga.scale;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.Extensions;

/**
 * Test scaling extension
 *
 * @author osbornb
 */
public class TileTableScalingUtils {

    /**
     * Test scaling extension
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testScaling(GeoPackage geoPackage) throws SQLException {

        geoPackage.getExtensionManager().deleteExtensions();

        List<String> tileTables = geoPackage.getTileTables();

        if (!tileTables.isEmpty()) {

            for (String tableName : tileTables) {

                TileTableScaling tableScaling = new TileTableScaling(
                        geoPackage, tableName);
                TestCase.assertNull(tableScaling.getExtension());
                TestCase.assertEquals(tableName, tableScaling.getTableName());
                TileScalingDao dao = tableScaling.getDao();

                TestCase.assertFalse(tableScaling.has());
                TestCase.assertNull(tableScaling.get());

                long count = 0;
                if (dao.isTableExists()) {
                    count = dao.countOf();
                }

                TileScaling newTileScaling = new TileScaling();
                newTileScaling.setScalingType(TileScalingType.IN_OUT);
                newTileScaling.setZoomIn(2l);
                newTileScaling.setZoomOut(2l);
                TestCase.assertTrue(tableScaling.createOrUpdate(newTileScaling));

                Extensions extension = tableScaling.getExtension();
                TestCase.assertEquals(TileTableScaling.EXTENSION_NAME,
                        extension.getExtensionName());
                TestCase.assertEquals(TileTableScaling.EXTENSION_AUTHOR,
                        extension.getAuthor());
                TestCase.assertEquals(
                        TileTableScaling.EXTENSION_NAME_NO_AUTHOR,
                        extension.getExtensionNameNoAuthor());
                TestCase.assertEquals(TileTableScaling.EXTENSION_DEFINITION,
                        extension.getDefinition());
                TestCase.assertEquals(tableName, extension.getTableName());
                TestCase.assertNull(extension.getColumnName());

                TestCase.assertTrue(tableScaling.has());
                TileScaling createdTileScaling = tableScaling.get();
                TestCase.assertNotNull(createdTileScaling);
                TestCase.assertEquals(count + 1, dao.countOf());

                TestCase.assertEquals(newTileScaling.getScalingType(),
                        createdTileScaling.getScalingType());
                TestCase.assertEquals(newTileScaling.getScalingTypeString(),
                        createdTileScaling.getScalingTypeString());
                TestCase.assertEquals(newTileScaling.getZoomIn(),
                        createdTileScaling.getZoomIn());
                TestCase.assertEquals(newTileScaling.getZoomOut(),
                        createdTileScaling.getZoomOut());

                createdTileScaling.setScalingType(TileScalingType.OUT_IN);
                createdTileScaling.setZoomIn(3l);
                createdTileScaling.setZoomOut(null);
                TestCase.assertTrue(tableScaling
                        .createOrUpdate(createdTileScaling));

                TestCase.assertTrue(tableScaling.has());
                TileScaling updatedTileScaling = tableScaling.get();
                TestCase.assertNotNull(updatedTileScaling);
                TestCase.assertEquals(count + 1, dao.countOf());

                TestCase.assertEquals(createdTileScaling.getScalingType(),
                        updatedTileScaling.getScalingType());
                TestCase.assertEquals(
                        createdTileScaling.getScalingTypeString(),
                        updatedTileScaling.getScalingTypeString());
                TestCase.assertEquals(createdTileScaling.getZoomIn(),
                        updatedTileScaling.getZoomIn());
                TestCase.assertEquals(createdTileScaling.getZoomOut(),
                        updatedTileScaling.getZoomOut());

                TestCase.assertTrue(tableScaling.delete());

                TestCase.assertNull(tableScaling.getExtension());
                TestCase.assertFalse(tableScaling.has());
                TestCase.assertNull(tableScaling.get());
                TestCase.assertEquals(count, dao.countOf());
                TestCase.assertTrue(dao.isTableExists());

                // Test deleting all NGA extensions
                geoPackage.getExtensionManager().deleteExtensions();

                TestCase.assertFalse(dao.isTableExists());
                TestCase.assertNull(tableScaling.getExtension());
                TestCase.assertFalse(tableScaling.has());
                TestCase.assertNull(tableScaling.get());

            }

        }

    }

}
