package mil.nga.geopackage.test.extension.contents;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.extension.GeoPackageExtensions;
import mil.nga.geopackage.extension.contents.ContentsId;
import mil.nga.geopackage.extension.contents.ContentsIdExtension;

public class ContentsIdUtils {

    /**
     * Test contents id
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testContentsId(GeoPackage geoPackage) {

        GeoPackageExtensions.deleteExtensions(geoPackage);

        ContentsIdExtension contentsIdExtension = new ContentsIdExtension(
                geoPackage);
        TestCase.assertNotNull(contentsIdExtension.getExtensionsDao());
        TestCase.assertFalse(contentsIdExtension.has());
        TestCase.assertNull(contentsIdExtension.getExtension());

        TestCase.assertEquals(geoPackage.getTables().size(),
                contentsIdExtension.getMissing().size());
        TestCase.assertEquals(0, contentsIdExtension.getIds().size());

        for (ContentsDataType type : ContentsDataType.values()) {
            TestCase.assertEquals(0, contentsIdExtension.getIds(type).size());
            TestCase.assertEquals(geoPackage.getTables(type).size(),
                    contentsIdExtension.getMissing(type).size());
        }

        for (String tableName : geoPackage.getTables()) {
            Contents contents = geoPackage.getTableContents(tableName);
            TestCase.assertNull(contentsIdExtension.get(tableName));
            TestCase.assertNull(contentsIdExtension.get(contents));
            TestCase.assertNull(contentsIdExtension.getId(tableName));
            TestCase.assertNull(contentsIdExtension.getId(contents));
        }

        // Create all content ids
        TestCase.assertEquals(geoPackage.getTables().size(),
                contentsIdExtension.createIds());
        TestCase.assertTrue(contentsIdExtension.has());
        TestCase.assertNotNull(contentsIdExtension.getExtension());
        TestCase.assertEquals(0, contentsIdExtension.getMissing().size());
        List<ContentsId> contentsIds = contentsIdExtension.getIds();
        TestCase.assertEquals(geoPackage.getTables().size(), contentsIds.size());

        Set<Long> uniqueIds = new HashSet<>();
        for (ContentsId contentsId : contentsIds) {
            TestCase.assertTrue(contentsId.getId() >= 0);
            TestCase.assertFalse(uniqueIds.contains(contentsId.getId()));
            uniqueIds.add(contentsId.getId());
            TestCase.assertNotNull(contentsId.getContents());
            TestCase.assertNotNull(contentsId.getTableName());
            TestCase.assertEquals(contentsId.getTableName(), contentsId
                    .getContents().getId());
        }

        // Delete all content ids
        TestCase.assertEquals(geoPackage.getTables().size(),
                contentsIdExtension.deleteIds());
        TestCase.assertEquals(0, contentsIdExtension.getIds().size());
        TestCase.assertTrue(contentsIdExtension.has());

        // Create contents ids for each contents data type
        int currentCount = 0;
        for (ContentsDataType type : ContentsDataType.values()) {
            int created = contentsIdExtension.createIds(type);
            currentCount += created;
            TestCase.assertEquals(geoPackage.getTables(type).size(), created);
            TestCase.assertEquals(created, contentsIdExtension.getIds(type)
                    .size());
            TestCase.assertEquals(geoPackage.getTables().size() - currentCount,
                    contentsIdExtension.getMissing().size());
            TestCase.assertEquals(0, contentsIdExtension.getMissing(type)
                    .size());
            TestCase.assertEquals(currentCount, contentsIdExtension.getIds()
                    .size());
        }

        // Delete contents ids for each contents data type
        for (ContentsDataType type : ContentsDataType.values()) {
            int deleted = contentsIdExtension.deleteIds(type);
            currentCount -= deleted;
            TestCase.assertEquals(geoPackage.getTables(type).size(), deleted);
            TestCase.assertEquals(0, contentsIdExtension.getIds(type).size());
            TestCase.assertEquals(geoPackage.getTables().size() - currentCount,
                    contentsIdExtension.getMissing().size());
            TestCase.assertEquals(geoPackage.getTables(type).size(),
                    contentsIdExtension.getMissing(type).size());
            TestCase.assertEquals(currentCount, contentsIdExtension.getIds()
                    .size());
        }

        TestCase.assertEquals(0, contentsIdExtension.getIds().size());
        TestCase.assertTrue(contentsIdExtension.has());

        // Delete the extension
        contentsIdExtension.removeExtension();
        TestCase.assertFalse(contentsIdExtension.has());
        TestCase.assertNull(contentsIdExtension.getExtension());

        // Create contents id's for each table one by one
        uniqueIds.clear();
        for (String tableName : geoPackage.getTables()) {

            Contents contents = geoPackage.getTableContents(tableName);

            TestCase.assertNull(contentsIdExtension.get(tableName));
            TestCase.assertNull(contentsIdExtension.get(contents));
            TestCase.assertNull(contentsIdExtension.getId(tableName));
            TestCase.assertNull(contentsIdExtension.getId(contents));

            TestCase.assertTrue(contentsIdExtension.getMissing().contains(
                    tableName));
            TestCase.assertFalse(containsTable(contentsIdExtension.getIds(),
                    tableName, contentsIdExtension));
            TestCase.assertTrue(contentsIdExtension.getMissing(
                    contents.getDataTypeString()).contains(tableName));
            TestCase.assertFalse(containsTable(
                    contentsIdExtension.getIds(contents.getDataTypeString()),
                    tableName, contentsIdExtension));

            ContentsId contentsId = null;
            Long contentsIdNumber = null;

            int random = (int) (Math.random() * 8);
            switch (random) {
                case 0:
                    contentsId = contentsIdExtension.create(contents);
                    break;
                case 1:
                    contentsId = contentsIdExtension.create(tableName);
                    break;
                case 2:
                    contentsId = contentsIdExtension.getOrCreate(contents);
                    break;
                case 3:
                    contentsId = contentsIdExtension.getOrCreate(tableName);
                    break;
                case 4:
                    contentsIdNumber = contentsIdExtension.createId(contents);
                    break;
                case 5:
                    contentsIdNumber = contentsIdExtension.createId(tableName);
                    break;
                case 6:
                    contentsIdNumber = contentsIdExtension.getOrCreateId(contents);
                    break;
                default:
                    contentsIdNumber = contentsIdExtension.getOrCreateId(tableName);
                    break;
            }

            if (random < 4) {
                TestCase.assertNotNull(contentsId);
                contentsIdNumber = contentsId.getId();
                TestCase.assertEquals(tableName, contentsId.getTableName());
                TestCase.assertNotNull(contentsId.getContents());
                TestCase.assertEquals(tableName, contentsId.getContents()
                        .getTableName());
            }

            TestCase.assertNotNull(contentsIdNumber);
            TestCase.assertTrue(contentsIdNumber >= 0);
            TestCase.assertFalse(uniqueIds.contains(contentsIdNumber));
            uniqueIds.add(contentsIdNumber);

            TestCase.assertFalse(contentsIdExtension.getMissing().contains(
                    tableName));
            TestCase.assertTrue(containsTable(contentsIdExtension.getIds(),
                    tableName, contentsIdExtension));
            TestCase.assertFalse(contentsIdExtension.getMissing(
                    contents.getDataTypeString()).contains(tableName));
            TestCase.assertTrue(containsTable(
                    contentsIdExtension.getIds(contents.getDataTypeString()),
                    tableName, contentsIdExtension));

            TestCase.assertEquals(contentsIdNumber,
                    contentsIdExtension.getId(tableName));
            TestCase.assertEquals(contentsIdNumber,
                    contentsIdExtension.getId(contents));
            TestCase.assertEquals((long) contentsIdNumber, contentsIdExtension
                    .get(tableName).getId());
            TestCase.assertEquals((long) contentsIdNumber, contentsIdExtension
                    .get(contents).getId());
        }

        // Delete contents id's one by one
        uniqueIds.clear();
        for (String tableName : geoPackage.getTables()) {

            Contents contents = geoPackage.getTableContents(tableName);

            TestCase.assertTrue(contentsIdExtension.delete(contents));

            TestCase.assertNull(contentsIdExtension.get(tableName));
            TestCase.assertNull(contentsIdExtension.get(contents));
            TestCase.assertNull(contentsIdExtension.getId(tableName));
            TestCase.assertNull(contentsIdExtension.getId(contents));

            TestCase.assertTrue(contentsIdExtension.getMissing().contains(
                    tableName));
            TestCase.assertFalse(containsTable(contentsIdExtension.getIds(),
                    tableName, contentsIdExtension));
            TestCase.assertTrue(contentsIdExtension.getMissing(
                    contents.getDataTypeString()).contains(tableName));
            TestCase.assertFalse(containsTable(
                    contentsIdExtension.getIds(contents.getDataTypeString()),
                    tableName, contentsIdExtension));

        }

        TestCase.assertEquals(0, contentsIdExtension.getIds().size());
        TestCase.assertTrue(contentsIdExtension.has());

        // Delete the extension
        contentsIdExtension.removeExtension();
        TestCase.assertFalse(contentsIdExtension.has());
        TestCase.assertNull(contentsIdExtension.getExtension());
    }

    /**
     * Check if the contents ids contain the table
     *
     * @param contentsIds         contents ids
     * @param tableName           table name
     * @param contentsIdExtension contents id extension
     * @return true if contains
     */
    private static boolean containsTable(List<ContentsId> contentsIds,
                                         String tableName, ContentsIdExtension contentsIdExtension) {

        boolean contains = false;

        for (ContentsId contentsId : contentsIds) {
            contains = contentsId.getTableName().equals(tableName);
            if (contains) {
                break;
            }
        }

        return contains;
    }

}
