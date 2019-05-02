package mil.nga.geopackage.test.extension.related;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.related.ExtendedRelation;
import mil.nga.geopackage.extension.related.ExtendedRelationsDao;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.extension.related.RelationType;
import mil.nga.geopackage.extension.related.UserMappingDao;
import mil.nga.geopackage.extension.related.UserMappingRow;
import mil.nga.geopackage.extension.related.UserMappingTable;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomCursor;

public class RelatedTilesUtils {

    /**
     * Test related tiles tables
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testTiles(GeoPackage geoPackage) throws Exception {

        // Create a related tables extension
        RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);

        if (rte.has()) {
            rte.removeExtension();
        }

        TestCase.assertFalse(rte.has());
        TestCase.assertTrue(rte.getRelationships().isEmpty());

        // Choose a random features table
        List<String> featuresTables = geoPackage.getFeatureTables();
        if (featuresTables.isEmpty()) {
            return; // pass with no testing
        }
        final String baseTableName = featuresTables
                .get((int) (Math.random() * featuresTables.size()));

        // Choose a random tiles table
        List<String> tilesTables = geoPackage.getTileTables();
        if (tilesTables.isEmpty()) {
            return; // pass with no testing
        }
        final String relatedTableName = tilesTables
                .get((int) (Math.random() * tilesTables.size()));

        // Create and validate a mapping table
        List<UserCustomColumn> additionalMappingColumns = RelatedTablesUtils
                .createAdditionalUserColumns();
        final String mappingTableName = "features_tiles";
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

        // Create the relationship between the features table and tiles
        // table
        ExtendedRelation extendedRelation = rte.addTilesRelationship(
                baseTableName, relatedTableName, userMappingTable);
        TestCase.assertTrue(rte.has());
        TestCase.assertTrue(rte.has(userMappingTable.getTableName()));
        TestCase.assertNotNull(extendedRelation);
        List<ExtendedRelation> extendedRelations = rte.getRelationships();
        TestCase.assertEquals(1, extendedRelations.size());
        TestCase.assertTrue(geoPackage.isTable(mappingTableName));

        // Build the Features ids
        FeatureDao featureDao = geoPackage.getFeatureDao(baseTableName);
        FeatureCursor featureCursor = featureDao.queryForAll();
        int featuresCount = featureCursor.getCount();
        List<Long> featureIds = new ArrayList<>();
        while (featureCursor.moveToNext()) {
            featureIds.add(featureCursor.getRow().getId());
        }
        featureCursor.close();

        // Build the Tile related ids
        TileDao tileDao = geoPackage.getTileDao(relatedTableName);
        TileCursor tileCursor = tileDao.queryForAll();
        int tilesCount = tileCursor.getCount();
        List<Long> tileIds = new ArrayList<>();
        while (tileCursor.moveToNext()) {
            tileIds.add(tileCursor.getRow().getId());
        }
        tileCursor.close();

        // Insert user mapping rows between feature ids and tile ids
        UserMappingDao dao = rte.getMappingDao(mappingTableName);
        UserMappingRow userMappingRow = null;
        for (int i = 0; i < 10; i++) {
            userMappingRow = dao.newRow();
            userMappingRow
                    .setBaseId(featureIds.get((int) (Math.random() * featuresCount)));
            userMappingRow
                    .setRelatedId(tileIds.get((int) (Math.random() * tilesCount)));
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
            TestCase.assertTrue(featureIds.contains(resultRow.getBaseId()));
            TestCase.assertTrue(tileIds.contains(resultRow.getRelatedId()));
            RelatedTablesUtils.validateUserRow(mappingColumns, resultRow);
            RelatedTablesUtils.validateDublinCoreColumns(resultRow);

            manualCount++;
        }
        TestCase.assertEquals(count, manualCount);
        cursor.close();

        ExtendedRelationsDao extendedRelationsDao = rte
                .getExtendedRelationsDao();

        // Get the relations starting from the features table
        List<ExtendedRelation> featuresExtendedRelations = extendedRelationsDao
                .getBaseTableRelations(featureDao.getTableName());
        List<ExtendedRelation> featuresExtendedRelations2 = extendedRelationsDao
                .getTableRelations(featureDao.getTableName());
        TestCase.assertEquals(1, featuresExtendedRelations.size());
        TestCase.assertEquals(1, featuresExtendedRelations2.size());
        TestCase.assertEquals(featuresExtendedRelations.get(0).getId(),
                featuresExtendedRelations2.get(0).getId());

        // Test the features table relations
        for (ExtendedRelation featuresRelation : featuresExtendedRelations) {

            // Test the relation
            TestCase.assertTrue(featuresRelation.getId() >= 0);
            TestCase.assertEquals(featureDao.getTableName(),
                    featuresRelation.getBaseTableName());
            TestCase.assertEquals(
                    featureDao.getTable().getPkColumn().getName(),
                    featuresRelation.getBasePrimaryColumn());
            TestCase.assertEquals(tileDao.getTableName(),
                    featuresRelation.getRelatedTableName());
            TestCase.assertEquals(tileDao.getTable().getPkColumn().getName(),
                    featuresRelation.getRelatedPrimaryColumn());
            TestCase.assertEquals(RelationType.TILES.getName(),
                    featuresRelation.getRelationName());
            TestCase.assertEquals(mappingTableName,
                    featuresRelation.getMappingTableName());

            // Test the user mappings from the relation
            UserMappingDao userMappingDao = rte.getMappingDao(featuresRelation);
            UserCustomCursor mappingCursor = userMappingDao.queryForAll();
            while (mappingCursor.moveToNext()) {
                userMappingRow = userMappingDao.getRow(mappingCursor);
                TestCase.assertTrue(featureIds.contains(userMappingRow
                        .getBaseId()));
                TestCase.assertTrue(tileIds.contains(userMappingRow
                        .getRelatedId()));
                RelatedTablesUtils.validateUserRow(mappingColumns,
                        userMappingRow);
                RelatedTablesUtils.validateDublinCoreColumns(userMappingRow);
            }
            mappingCursor.close();

        }

        // Get the relations starting from the tiles table
        List<ExtendedRelation> relatedExtendedRelations = extendedRelationsDao
                .getRelatedTableRelations(relatedTableName);
        List<ExtendedRelation> extendedRelations2 = extendedRelationsDao
                .getTableRelations(relatedTableName);
        TestCase.assertEquals(1, relatedExtendedRelations.size());
        TestCase.assertEquals(1, extendedRelations2.size());
        TestCase.assertEquals(relatedExtendedRelations.get(0).getId(),
                extendedRelations2.get(0).getId());

        // Test the tiles table relations
        for (ExtendedRelation relation : relatedExtendedRelations) {

            // Test the relation
            TestCase.assertTrue(relation.getId() >= 0);
            TestCase.assertEquals(featureDao.getTableName(),
                    relation.getBaseTableName());
            TestCase.assertEquals(
                    featureDao.getTable().getPkColumn().getName(),
                    relation.getBasePrimaryColumn());
            TestCase.assertEquals(tileDao.getTableName(),
                    relation.getRelatedTableName());
            TestCase.assertEquals(tileDao.getTable().getPkColumn().getName(),
                    relation.getRelatedPrimaryColumn());
            TestCase.assertEquals(RelationType.TILES.getName(),
                    relation.getRelationName());
            TestCase.assertEquals(mappingTableName,
                    relation.getMappingTableName());

            // Test the user mappings from the relation
            UserMappingDao userMappingDao = rte.getMappingDao(relation);
            int totalMappedCount = userMappingDao.count();
            UserCustomCursor mappingCursor = userMappingDao.queryForAll();
            while (mappingCursor.moveToNext()) {
                userMappingRow = userMappingDao.getRow(mappingCursor);
                TestCase.assertTrue(featureIds.contains(userMappingRow
                        .getBaseId()));
                TestCase.assertTrue(tileIds.contains(userMappingRow
                        .getRelatedId()));
                RelatedTablesUtils.validateUserRow(mappingColumns,
                        userMappingRow);
                RelatedTablesUtils.validateDublinCoreColumns(userMappingRow);
            }
            mappingCursor.close();

            // Get and test the features DAO
            featureDao = geoPackage.getFeatureDao(featureDao.getTableName());
            TestCase.assertNotNull(featureDao);
            FeatureTable featureTable = featureDao.getTable();
            TestCase.assertNotNull(featureTable);
            Contents featuresContents = featureTable.getContents();
            TestCase.assertNotNull(featuresContents);
            TestCase.assertEquals(ContentsDataType.FEATURES,
                    featuresContents.getDataType());
            TestCase.assertEquals(ContentsDataType.FEATURES.getName(),
                    featuresContents.getDataTypeString());
            TestCase.assertEquals(featureTable.getTableName(),
                    featuresContents.getTableName());
            TestCase.assertNotNull(featuresContents.getLastChange());

            // Get and test the Tile Rows mapped to each Feature Row
            tileCursor = tileDao.queryForAll();
            int totalMapped = 0;
            while (tileCursor.moveToNext()) {
                TileRow tileRow = tileCursor.getRow();
                List<Long> mappedIds = rte.getMappingsForRelated(relation,
                        tileRow.getId());
                for (long mappedId : mappedIds) {
                    FeatureRow featureRow = featureDao.queryForIdRow(mappedId);
                    TestCase.assertNotNull(featureRow);

                    TestCase.assertTrue(featureRow.hasId());
                    TestCase.assertTrue(featureRow.getId() >= 0);
                    TestCase.assertTrue(featureIds.contains(featureRow.getId()));
                    TestCase.assertTrue(mappedIds.contains(featureRow.getId()));
                }

                totalMapped += mappedIds.size();
            }
            tileCursor.close();
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
        TestCase.assertFalse(geoPackage.isTable(mappingTableName));

        // Delete the related tables extension
        rte.removeExtension();
        TestCase.assertFalse(rte.has());

    }

}
