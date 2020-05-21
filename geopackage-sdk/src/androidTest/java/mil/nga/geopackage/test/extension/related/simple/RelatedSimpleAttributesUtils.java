package mil.nga.geopackage.test.extension.related.simple;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;
import mil.nga.geopackage.attributes.AttributesTable;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.contents.ContentsDataType;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.related.ExtendedRelation;
import mil.nga.geopackage.extension.related.ExtendedRelationsDao;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.extension.related.UserMappingDao;
import mil.nga.geopackage.extension.related.UserMappingRow;
import mil.nga.geopackage.extension.related.UserMappingTable;
import mil.nga.geopackage.extension.related.simple.SimpleAttributesDao;
import mil.nga.geopackage.extension.related.simple.SimpleAttributesRow;
import mil.nga.geopackage.extension.related.simple.SimpleAttributesTable;
import mil.nga.geopackage.extension.related.simple.SimpleAttributesTableMetadata;
import mil.nga.geopackage.test.extension.related.RelatedTablesUtils;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomCursor;

public class RelatedSimpleAttributesUtils {

    /**
     * Test related simple attributes tables
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testSimpleAttributes(GeoPackage geoPackage)
            throws Exception {

        // Create a related tables extension
        RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);

        if (rte.has()) {
            rte.removeExtension();
        }

        TestCase.assertFalse(rte.has());
        TestCase.assertTrue(rte.getRelationships().isEmpty());

        // Choose a random attributes table
        List<String> attributesTables = geoPackage.getAttributesTables();
        if (attributesTables.isEmpty()) {
            return; // pass with no testing
        }
        final String baseTableName = attributesTables
                .get((int) (Math.random() * attributesTables.size()));

        // Validate nullable non simple columns
        try {
            SimpleAttributesTable.create(
                    SimpleAttributesTableMetadata.create("simple_table", RelatedTablesUtils
                            .createAdditionalUserColumns()));
            TestCase.fail("Simple Attributes Table created with nullable non simple columns");
        } catch (Exception e) {
            // pass
        }
        // Validate non nullable non simple columns
        try {
            SimpleAttributesTable.create(
                    SimpleAttributesTableMetadata.create("simple_table", RelatedTablesUtils
                            .createAdditionalUserColumns(true)));
            TestCase.fail("Simple Attributes Table created with non nullable non simple columns");
        } catch (Exception e) {
            // pass
        }
        // Validate nullable simple columns
        try {
            SimpleAttributesTable.create(
                    SimpleAttributesTableMetadata.create("simple_table", RelatedTablesUtils
                            .createSimpleUserColumns(false)));
            TestCase.fail("Simple Attributes Table created with nullable simple columns");
        } catch (Exception e) {
            // pass
        }

        // Populate and validate a simple attributes table
        List<UserCustomColumn> simpleUserColumns = RelatedTablesUtils
                .createSimpleUserColumns();
        SimpleAttributesTable simpleTable = SimpleAttributesTable.create(
                SimpleAttributesTableMetadata.create("simple_table", simpleUserColumns));
        String[] simpleColumns = simpleTable.getColumnNames();
        TestCase.assertEquals(SimpleAttributesTable.numRequiredColumns()
                + simpleUserColumns.size(), simpleColumns.length);
        UserCustomColumn idColumn = simpleTable.getIdColumn();
        TestCase.assertNotNull(idColumn);
        TestCase.assertTrue(idColumn.isNamed(SimpleAttributesTable.COLUMN_ID));
        TestCase.assertEquals(GeoPackageDataType.INTEGER,
                idColumn.getDataType());
        TestCase.assertTrue(idColumn.isNotNull());
        TestCase.assertTrue(idColumn.isPrimaryKey());

        // Create and validate a mapping table
        List<UserCustomColumn> additionalMappingColumns = RelatedTablesUtils
                .createAdditionalUserColumns();
        final String mappingTableName = "attributes_simple_attributes";
        UserMappingTable userMappingTable = UserMappingTable.create(
                mappingTableName, additionalMappingColumns);
        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        TestCase.assertEquals(UserMappingTable.numRequiredColumns()
                + additionalMappingColumns.size(), userMappingTable
                .getColumns().size());
        UserCustomColumn baseIdColumn = userMappingTable.getBaseIdColumn();
        TestCase.assertNotNull(baseIdColumn);
        TestCase.assertTrue(baseIdColumn
                .isNamed(UserMappingTable.COLUMN_BASE_ID));
        TestCase.assertEquals(GeoPackageDataType.INTEGER,
                baseIdColumn.getDataType());
        TestCase.assertTrue(baseIdColumn.isNotNull());
        TestCase.assertFalse(baseIdColumn.isPrimaryKey());
        UserCustomColumn relatedIdColumn = userMappingTable
                .getRelatedIdColumn();
        TestCase.assertNotNull(relatedIdColumn);
        TestCase.assertTrue(relatedIdColumn
                .isNamed(UserMappingTable.COLUMN_RELATED_ID));
        TestCase.assertEquals(GeoPackageDataType.INTEGER,
                relatedIdColumn.getDataType());
        TestCase.assertTrue(relatedIdColumn.isNotNull());
        TestCase.assertFalse(relatedIdColumn.isPrimaryKey());
        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));

        // Create the simple attributes table, content row, and relationship
        // between the attributes table and simple attributes table
        ContentsDao contentsDao = geoPackage.getContentsDao();
        TestCase.assertFalse(contentsDao.getTables().contains(
                simpleTable.getTableName()));
        ExtendedRelation extendedRelation = rte
                .addSimpleAttributesRelationship(baseTableName, simpleTable,
                        userMappingTable);
        validateContents(simpleTable, simpleTable.getContents());
        TestCase.assertTrue(rte.has());
        TestCase.assertTrue(rte.has(userMappingTable.getTableName()));
        TestCase.assertNotNull(extendedRelation);
        List<ExtendedRelation> extendedRelations = rte.getRelationships();
        TestCase.assertEquals(1, extendedRelations.size());
        TestCase.assertTrue(geoPackage.isTable(mappingTableName));
        TestCase.assertTrue(geoPackage.isTable(simpleTable.getTableName()));
        TestCase.assertTrue(contentsDao.getTables().contains(
                simpleTable.getTableName()));
        validateContents(simpleTable,
                contentsDao.queryForId(simpleTable.getTableName()));
        TestCase.assertEquals(SimpleAttributesTable.RELATION_TYPE.getDataType(),
                geoPackage.getTableType(simpleTable.getTableName()));
        TestCase.assertTrue(geoPackage.isTableType(simpleTable.getTableName(),
                SimpleAttributesTable.RELATION_TYPE.getDataType()));

        // Validate the simple attributes DAO
        SimpleAttributesDao simpleDao = rte.getSimpleAttributesDao(simpleTable);
        TestCase.assertNotNull(simpleDao);
        simpleTable = simpleDao.getTable();
        TestCase.assertNotNull(simpleTable);
        validateContents(simpleTable, simpleTable.getContents());

        // Insert simple attributes table rows
        int simpleCount = 2 + (int) (Math.random() * 9);
        long simpleRowId = 0;
        // Create and insert the first simpleCount - 1 rows
        for (int i = 0; i < simpleCount - 1; i++) {
            SimpleAttributesRow simpleRow = simpleDao.newRow();
            RelatedTablesUtils.populateUserRow(simpleTable, simpleRow,
                    SimpleAttributesTable.requiredColumns());
            simpleRowId = simpleDao.create(simpleRow);
            TestCase.assertTrue(simpleRowId > 0);
        }
        // Copy the last row insert and insert the final simple attributes row
        SimpleAttributesRow simpleRowToCopy = new SimpleAttributesRow(
                simpleDao.queryForIdRow(simpleRowId));
        SimpleAttributesRow simpleRowCopy = simpleRowToCopy.copy();
        long copySimpleRowId = simpleDao.create(simpleRowCopy);
        TestCase.assertTrue(copySimpleRowId > 0);
        TestCase.assertEquals(simpleRowId + 1, copySimpleRowId);
        TestCase.assertEquals(simpleCount, simpleDao.count());

        // Build the Attributes ids
        AttributesDao attributesDao = geoPackage
                .getAttributesDao(baseTableName);
        AttributesCursor attributesCursor = attributesDao.queryForAll();
        int attributesCount = attributesCursor.getCount();
        List<Long> attributeIds = new ArrayList<>();
        while (attributesCursor.moveToNext()) {
            attributeIds.add(attributesCursor.getRow().getId());
        }
        attributesCursor.close();

        // Build the Simple Attribute ids
        UserCustomCursor simpleCursor = simpleDao.queryForAll();
        simpleCount = simpleCursor.getCount();
        List<Long> simpleIds = new ArrayList<>();
        while (simpleCursor.moveToNext()) {
            simpleIds.add(simpleCursor.getRow().getId());
        }
        simpleCursor.close();

        // Insert user mapping rows between attribute ids and simple attribute
        // ids
        UserMappingDao dao = rte.getMappingDao(mappingTableName);
        UserMappingRow userMappingRow = null;
        for (int i = 0; i < 10; i++) {
            userMappingRow = dao.newRow();
            userMappingRow
                    .setBaseId(attributeIds.get((int) (Math.random() * attributesCount)));
            userMappingRow
                    .setRelatedId(simpleIds.get((int) (Math.random() * simpleCount)));
            RelatedTablesUtils.populateUserRow(userMappingTable,
                    userMappingRow, UserMappingTable.requiredColumns());
            TestCase.assertTrue(dao.create(userMappingRow) > 0);
        }
        TestCase.assertEquals(10, dao.count());

        // Validate the user mapping rows
        userMappingTable = dao.getTable();
        String[] mappingColumns = userMappingTable.getColumnNames();
        UserCustomCursor cursor = dao.queryForAll();
        int count = cursor.getCount();
        TestCase.assertEquals(10, count);
        int manualCount = 0;
        while (cursor.moveToNext()) {

            UserMappingRow resultRow = dao.getRow(cursor);
            TestCase.assertFalse(resultRow.hasId());
            TestCase.assertTrue(attributeIds.contains(resultRow.getBaseId()));
            TestCase.assertTrue(simpleIds.contains(resultRow.getRelatedId()));
            RelatedTablesUtils.validateUserRow(mappingColumns, resultRow);
            RelatedTablesUtils.validateDublinCoreColumns(resultRow);

            manualCount++;
        }
        TestCase.assertEquals(count, manualCount);
        cursor.close();

        ExtendedRelationsDao extendedRelationsDao = rte
                .getExtendedRelationsDao();

        // Get the relations starting from the attributes table
        List<ExtendedRelation> attributesExtendedRelations = extendedRelationsDao
                .getBaseTableRelations(attributesDao.getTableName());
        List<ExtendedRelation> attributesExtendedRelations2 = extendedRelationsDao
                .getTableRelations(attributesDao.getTableName());
        TestCase.assertEquals(1, attributesExtendedRelations.size());
        TestCase.assertEquals(1, attributesExtendedRelations2.size());
        TestCase.assertEquals(attributesExtendedRelations.get(0).getId(),
                attributesExtendedRelations2.get(0).getId());
        TestCase.assertTrue(extendedRelationsDao.getRelatedTableRelations(
                attributesDao.getTableName()).isEmpty());

        // Test the attributes table relations
        for (ExtendedRelation attributesRelation : attributesExtendedRelations) {

            // Test the relation
            TestCase.assertTrue(attributesRelation.getId() >= 0);
            TestCase.assertEquals(attributesDao.getTableName(),
                    attributesRelation.getBaseTableName());
            TestCase.assertEquals(attributesDao.getTable().getPkColumn()
                    .getName(), attributesRelation.getBasePrimaryColumn());
            TestCase.assertEquals(simpleDao.getTableName(),
                    attributesRelation.getRelatedTableName());
            TestCase.assertEquals(simpleDao.getTable().getPkColumn().getName(),
                    attributesRelation.getRelatedPrimaryColumn());
            TestCase.assertEquals(
                    SimpleAttributesTable.RELATION_TYPE.getName(),
                    attributesRelation.getRelationName());
            TestCase.assertEquals(mappingTableName,
                    attributesRelation.getMappingTableName());

            // Test the user mappings from the relation
            UserMappingDao userMappingDao = rte
                    .getMappingDao(attributesRelation);
            int totalMappedCount = userMappingDao.count();
            UserCustomCursor mappingCursor = userMappingDao.queryForAll();
            while (mappingCursor.moveToNext()) {
                userMappingRow = userMappingDao.getRow(mappingCursor);
                TestCase.assertTrue(attributeIds.contains(userMappingRow
                        .getBaseId()));
                TestCase.assertTrue(simpleIds.contains(userMappingRow
                        .getRelatedId()));
                RelatedTablesUtils.validateUserRow(mappingColumns,
                        userMappingRow);
                RelatedTablesUtils.validateDublinCoreColumns(userMappingRow);
            }
            mappingCursor.close();

            // Get and test the simple attributes DAO
            simpleDao = rte.getSimpleAttributesDao(attributesRelation);
            TestCase.assertNotNull(simpleDao);
            simpleTable = simpleDao.getTable();
            TestCase.assertNotNull(simpleTable);
            validateContents(simpleTable, simpleTable.getContents());

            // Get and test the Simple Attributes Rows mapped to each
            // Attributes Row
            attributesCursor = attributesDao.queryForAll();
            int totalMapped = 0;
            while (attributesCursor.moveToNext()) {
                AttributesRow attributesRow = attributesCursor.getRow();
                List<Long> mappedIds = rte.getMappingsForBase(
                        attributesRelation, attributesRow.getId());
                List<SimpleAttributesRow> simpleRows = simpleDao
                        .getRows(mappedIds);
                TestCase.assertEquals(mappedIds.size(), simpleRows.size());

                for (SimpleAttributesRow simpleRow : simpleRows) {
                    TestCase.assertTrue(simpleRow.hasId());
                    TestCase.assertTrue(simpleRow.getId() >= 0);
                    TestCase.assertTrue(simpleIds.contains(simpleRow.getId()));
                    TestCase.assertTrue(mappedIds.contains(simpleRow.getId()));
                    RelatedTablesUtils
                            .validateUserRow(simpleColumns, simpleRow);
                    RelatedTablesUtils
                            .validateSimpleDublinCoreColumns(simpleRow);
                }

                totalMapped += mappedIds.size();
            }
            attributesCursor.close();
            TestCase.assertEquals(totalMappedCount, totalMapped);
        }

        // Get the relations starting from the simple attributes table
        List<ExtendedRelation> simpleExtendedRelations = extendedRelationsDao
                .getRelatedTableRelations(simpleTable.getTableName());
        List<ExtendedRelation> simpleExtendedRelations2 = extendedRelationsDao
                .getTableRelations(simpleTable.getTableName());
        TestCase.assertEquals(1, simpleExtendedRelations.size());
        TestCase.assertEquals(1, simpleExtendedRelations2.size());
        TestCase.assertEquals(simpleExtendedRelations.get(0).getId(),
                simpleExtendedRelations2.get(0).getId());
        TestCase.assertTrue(extendedRelationsDao.getBaseTableRelations(
                simpleTable.getTableName()).isEmpty());

        // Test the simple attributes table relations
        for (ExtendedRelation simpleRelation : simpleExtendedRelations) {

            // Test the relation
            TestCase.assertTrue(simpleRelation.getId() >= 0);
            TestCase.assertEquals(attributesDao.getTableName(),
                    simpleRelation.getBaseTableName());
            TestCase.assertEquals(attributesDao.getTable().getPkColumn()
                    .getName(), simpleRelation.getBasePrimaryColumn());
            TestCase.assertEquals(simpleDao.getTableName(),
                    simpleRelation.getRelatedTableName());
            TestCase.assertEquals(simpleDao.getTable().getPkColumn().getName(),
                    simpleRelation.getRelatedPrimaryColumn());
            TestCase.assertEquals(
                    SimpleAttributesTable.RELATION_TYPE.getName(),
                    simpleRelation.getRelationName());
            TestCase.assertEquals(mappingTableName,
                    simpleRelation.getMappingTableName());

            // Test the user mappings from the relation
            UserMappingDao userMappingDao = rte.getMappingDao(simpleRelation);
            int totalMappedCount = userMappingDao.count();
            UserCustomCursor mappingCursor = userMappingDao.queryForAll();
            while (mappingCursor.moveToNext()) {
                userMappingRow = userMappingDao.getRow(mappingCursor);
                TestCase.assertTrue(attributeIds.contains(userMappingRow
                        .getBaseId()));
                TestCase.assertTrue(simpleIds.contains(userMappingRow
                        .getRelatedId()));
                RelatedTablesUtils.validateUserRow(mappingColumns,
                        userMappingRow);
                RelatedTablesUtils.validateDublinCoreColumns(userMappingRow);
            }
            mappingCursor.close();

            // Get and test the attributes DAO
            attributesDao = geoPackage.getAttributesDao(attributesDao
                    .getTableName());
            TestCase.assertNotNull(attributesDao);
            AttributesTable attributesTable = attributesDao.getTable();
            TestCase.assertNotNull(attributesTable);
            Contents attributesContents = attributesTable.getContents();
            TestCase.assertNotNull(attributesContents);
            TestCase.assertEquals(ContentsDataType.ATTRIBUTES,
                    attributesContents.getDataType());
            TestCase.assertEquals(ContentsDataType.ATTRIBUTES.getName(),
                    attributesContents.getDataTypeName());
            TestCase.assertEquals(attributesTable.getTableName(),
                    attributesContents.getTableName());
            TestCase.assertNotNull(attributesContents.getLastChange());

            // Get and test the Attributes Rows mapped to each Simple Attributes
            // Row
            simpleCursor = simpleDao.queryForAll();
            int totalMapped = 0;
            while (simpleCursor.moveToNext()) {
                SimpleAttributesRow simpleRow = simpleDao
                        .getRow(simpleCursor);
                List<Long> mappedIds = rte.getMappingsForRelated(
                        simpleRelation, simpleRow.getId());
                for (long mappedId : mappedIds) {
                    AttributesRow attributesRow = attributesDao
                            .queryForIdRow(mappedId);
                    TestCase.assertNotNull(attributesRow);

                    TestCase.assertTrue(attributesRow.hasId());
                    TestCase.assertTrue(attributesRow.getId() >= 0);
                    TestCase.assertTrue(attributeIds.contains(attributesRow
                            .getId()));
                    TestCase.assertTrue(mappedIds.contains(attributesRow
                            .getId()));
                }

                totalMapped += mappedIds.size();
            }
            simpleCursor.close();
            TestCase.assertEquals(totalMappedCount, totalMapped);
        }

        // Delete a single mapping
        int countOfIds = dao.countByIds(userMappingRow);
        TestCase.assertEquals(countOfIds, dao.deleteByIds(userMappingRow));
        TestCase.assertEquals(10 - countOfIds, dao.count());

        // Delete the relationship and user mapping table
        rte.removeRelationship(extendedRelation);
        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        extendedRelations = rte.getRelationships();
        TestCase.assertEquals(0, extendedRelations.size());
        TestCase.assertFalse(geoPackage.isTable(
                mappingTableName));

        // Delete the simple attributes table and contents row
        TestCase.assertTrue(geoPackage.isTable(simpleTable.getTableName()));
        TestCase.assertNotNull(contentsDao.queryForId(simpleTable
                .getTableName()));
        geoPackage.deleteTable(simpleTable.getTableName());
        TestCase.assertFalse(geoPackage.isTable(simpleTable.getTableName()));
        TestCase.assertNull(contentsDao.queryForId(simpleTable.getTableName()));

        // Delete the related tables extension
        rte.removeExtension();
        TestCase.assertFalse(rte.has());

    }

    /**
     * Validate contents
     *
     * @param simpleAttributesTable simple attributes table
     * @param contents              contents
     */
    private static void validateContents(
            SimpleAttributesTable simpleAttributesTable, Contents contents) {
        TestCase.assertNotNull(contents);
        TestCase.assertNotNull(contents.getDataType());
        TestCase.assertEquals(
                SimpleAttributesTable.RELATION_TYPE.getDataType(), contents
                        .getDataType().getName());
        TestCase.assertEquals(
                SimpleAttributesTable.RELATION_TYPE.getDataType(),
                contents.getDataTypeName());
        TestCase.assertEquals(simpleAttributesTable.getTableName(),
                contents.getTableName());
        TestCase.assertNotNull(contents.getLastChange());
    }

}
