package mil.nga.geopackage.test.extension.related;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.extension.related.ExtendedRelation;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.extension.related.UserMappingDao;
import mil.nga.geopackage.extension.related.UserMappingRow;
import mil.nga.geopackage.extension.related.UserMappingTable;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.test.LoadGeoPackageTestCase;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomCursor;


/**
 * Test Related Tables Extension writing
 *
 * @author osbornb
 */
public class RelatedTablesWriteTest extends LoadGeoPackageTestCase {

    /**
     * Constructor
     */
    public RelatedTablesWriteTest() {
        super(TestConstants.IMPORT_DB_NAME, TestConstants.IMPORT_DB_FILE_NAME);
    }

    /**
     * Test get tile
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testWriteRelationships() throws SQLException, IOException {

        RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);

        if (rte.has()) {
            rte.removeExtension();
        }

        // 1. Has extension
        TestCase.assertFalse(rte.has());

        // 4. Get relationships
        List<ExtendedRelation> extendedRelations = rte.getRelationships();
        TestCase.assertTrue(extendedRelations.isEmpty());

        // 2. Add extension
        // 5. Add relationship between "geometry2d" and "geometry3d"
        final String baseTableName = "geometry1";
        final String relatedTableName = "geometry2";
        final String mappingTableName = "g1_g2";

        List<UserCustomColumn> additionalColumns = RelatedTablesUtils
                .createAdditionalUserColumns();

        UserMappingTable userMappingTable = UserMappingTable.create(
                mappingTableName, additionalColumns);
        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        TestCase.assertEquals(UserMappingTable.numRequiredColumns()
                + additionalColumns.size(), userMappingTable.getColumns()
                .size());
        UserCustomColumn baseIdColumn = userMappingTable.getBaseIdColumn();
        TestCase.assertNotNull(baseIdColumn);
        TestCase.assertTrue(baseIdColumn
                .isNamed(UserMappingTable.COLUMN_BASE_ID));
        TestCase.assertTrue(baseIdColumn.isNotNull());
        TestCase.assertFalse(baseIdColumn.isPrimaryKey());
        UserCustomColumn relatedIdColumn = userMappingTable
                .getRelatedIdColumn();
        TestCase.assertNotNull(relatedIdColumn);
        TestCase.assertTrue(relatedIdColumn
                .isNamed(UserMappingTable.COLUMN_RELATED_ID));
        TestCase.assertTrue(relatedIdColumn.isNotNull());
        TestCase.assertFalse(relatedIdColumn.isPrimaryKey());

        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        ExtendedRelation extendedRelation = rte.addFeaturesRelationship(
                baseTableName, relatedTableName, userMappingTable);
        TestCase.assertTrue(rte.has());
        TestCase.assertTrue(rte.has(userMappingTable.getTableName()));
        TestCase.assertNotNull(extendedRelation);
        extendedRelations = rte.getRelationships();
        TestCase.assertEquals(1, extendedRelations.size());
        TestCase.assertTrue(geoPackage.isTable(mappingTableName));

        // 7. Add mappings
        FeatureDao baseDao = geoPackage.getFeatureDao(baseTableName);
        FeatureDao relatedDao = geoPackage.getFeatureDao(relatedTableName);
        FeatureCursor baseCursor = baseDao.queryForAll();
        int baseCount = baseCursor.getCount();
        long[] baseIds = new long[baseCount];
        int inx = 0;
        while (baseCursor.moveToNext()) {
            baseIds[inx++] = baseCursor.getRow().getId();
        }
        baseCursor.close();
        FeatureCursor relatedCursor = relatedDao.queryForAll();
        int relatedCount = relatedCursor.getCount();
        long[] relatedIds = new long[relatedCount];
        inx = 0;
        while (relatedCursor.moveToNext()) {
            relatedIds[inx++] = relatedCursor.getRow().getId();
        }
        relatedCursor.close();
        UserMappingDao dao = rte.getMappingDao(mappingTableName);
        UserMappingRow userMappingRow = null;
        for (inx = 0; inx < 10; inx++) {
            userMappingRow = dao.newRow();
            userMappingRow.setBaseId(baseIds[(int) (Math.random() * baseCount)]);
            userMappingRow.setRelatedId(relatedIds[(int) (Math.random() * relatedCount)]);
            RelatedTablesUtils.populateUserRow(userMappingTable,
                    userMappingRow, UserMappingTable.requiredColumns());
            TestCase.assertTrue(dao.create(userMappingRow) > 0);
        }

        TestCase.assertEquals(10, dao.count());

        userMappingTable = dao.getTable();
        String[] columns = userMappingTable.getColumnNames();
        UserCustomCursor cursor = dao.queryForAll();
        int count = cursor.getCount();
        TestCase.assertEquals(10, count);
        int manualCount = 0;
        while (cursor.moveToNext()) {

            UserMappingRow resultRow = dao.getRow(cursor);
            TestCase.assertFalse(resultRow.hasId());
            RelatedTablesUtils.validateUserRow(columns, resultRow);
            RelatedTablesUtils.validateDublinCoreColumns(resultRow);

            manualCount++;
        }
        TestCase.assertEquals(count, manualCount);
        cursor.close();

        // 8. Remove mappings (note: it is plausible and allowed
        // to have duplicate entries)
        TestCase.assertTrue(dao.deleteByIds(userMappingRow) > 0);

        // 6. Remove relationship
        rte.removeRelationship(extendedRelation);
        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        extendedRelations = rte.getRelationships();
        TestCase.assertEquals(0, extendedRelations.size());
        TestCase.assertFalse(geoPackage.isTable(mappingTableName));

        // 3. Remove extension
        rte.removeExtension();
        TestCase.assertFalse(rte.has());
    }

    /**
     * Test write relationships to attributes
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testWriteRelationshipsToAttributes() throws SQLException,
            IOException {

        RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);

        if (rte.has()) {
            rte.removeExtension();
        }

        // 1. Has extension
        TestCase.assertFalse(rte.has());

        // 4. Get relationships
        List<ExtendedRelation> extendedRelations = rte.getRelationships();
        TestCase.assertTrue(extendedRelations.isEmpty());

        // 2. Add extension
        // 5. Add relationship between "geometry2d" and "attributes"
        final String baseTableName = "geometry1";
        final String relatedTableName = "attributes";
        final String mappingTableName = "g1_a";

        List<UserCustomColumn> additionalColumns = RelatedTablesUtils
                .createAdditionalUserColumns();

        UserMappingTable userMappingTable = UserMappingTable.create(
                mappingTableName, additionalColumns);
        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        TestCase.assertEquals(UserMappingTable.numRequiredColumns()
                + additionalColumns.size(), userMappingTable.getColumns()
                .size());
        UserCustomColumn baseIdColumn = userMappingTable.getBaseIdColumn();
        TestCase.assertNotNull(baseIdColumn);
        TestCase.assertTrue(baseIdColumn
                .isNamed(UserMappingTable.COLUMN_BASE_ID));
        TestCase.assertTrue(baseIdColumn.isNotNull());
        TestCase.assertFalse(baseIdColumn.isPrimaryKey());
        UserCustomColumn relatedIdColumn = userMappingTable
                .getRelatedIdColumn();
        TestCase.assertNotNull(relatedIdColumn);
        TestCase.assertTrue(relatedIdColumn
                .isNamed(UserMappingTable.COLUMN_RELATED_ID));
        TestCase.assertTrue(relatedIdColumn.isNotNull());
        TestCase.assertFalse(relatedIdColumn.isPrimaryKey());

        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        ExtendedRelation extendedRelation = rte.addAttributesRelationship(
                baseTableName, relatedTableName, userMappingTable);
        TestCase.assertTrue(rte.has());
        TestCase.assertTrue(rte.has(userMappingTable.getTableName()));
        TestCase.assertNotNull(extendedRelation);
        extendedRelations = rte.getRelationships();
        TestCase.assertEquals(1, extendedRelations.size());
        TestCase.assertTrue(geoPackage.isTable(mappingTableName));

        // 7. Add mappings
        FeatureDao baseDao = geoPackage.getFeatureDao(baseTableName);
        AttributesDao relatedDao = geoPackage
                .getAttributesDao(relatedTableName);
        FeatureCursor baseFrs = baseDao.queryForAll();
        int baseCount = baseFrs.getCount();
        long[] baseIds = new long[baseCount];
        int inx = 0;
        while (baseFrs.moveToNext()) {
            baseIds[inx++] = baseFrs.getRow().getId();
        }
        baseFrs.close();
        AttributesCursor relatedArs = relatedDao.queryForAll();
        int relatedCount = relatedArs.getCount();
        long[] relatedIds = new long[relatedCount];
        inx = 0;
        while (relatedArs.moveToNext()) {
            relatedIds[inx++] = relatedArs.getRow().getId();
        }
        relatedArs.close();
        UserMappingDao dao = rte.getMappingDao(mappingTableName);
        UserMappingRow userMappingRow = null;
        for (inx = 0; inx < 10; inx++) {
            userMappingRow = dao.newRow();
            userMappingRow.setBaseId(baseIds[(int) (Math.random() * baseCount)]);
            userMappingRow.setRelatedId(relatedIds[(int) (Math.random() * relatedCount)]);
            RelatedTablesUtils.populateUserRow(userMappingTable,
                    userMappingRow, UserMappingTable.requiredColumns());
            TestCase.assertTrue(dao.create(userMappingRow) > 0);
        }

        TestCase.assertEquals(10, dao.count());

        userMappingTable = dao.getTable();
        String[] columns = userMappingTable.getColumnNames();
        UserCustomCursor cursor = dao.queryForAll();
        int count = cursor.getCount();
        TestCase.assertEquals(10, count);
        int manualCount = 0;
        while (cursor.moveToNext()) {

            UserMappingRow resultRow = dao.getRow(cursor);
            TestCase.assertFalse(resultRow.hasId());
            RelatedTablesUtils.validateUserRow(columns, resultRow);
            RelatedTablesUtils.validateDublinCoreColumns(resultRow);

            manualCount++;
        }
        TestCase.assertEquals(count, manualCount);
        cursor.close();

        // 8. Remove mappings (note: it is plausible and allowed
        // to have duplicate entries)
        TestCase.assertTrue(dao.deleteByIds(userMappingRow) > 0);

        // 6. Remove relationship
        rte.removeRelationship(extendedRelation);
        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        extendedRelations = rte.getRelationships();
        TestCase.assertEquals(0, extendedRelations.size());
        TestCase.assertFalse(geoPackage.isTable(mappingTableName));

        // 3. Remove extension
        rte.removeExtension();
        TestCase.assertFalse(rte.has());
    }

    /**
     * Test write relationships to tiles
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testWriteRelationshipsToTiles() throws SQLException,
            IOException {

        RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);

        if (rte.has()) {
            rte.removeExtension();
        }

        // 1. Has extension
        TestCase.assertFalse(rte.has());

        // 4. Get relationships
        List<ExtendedRelation> extendedRelations = rte.getRelationships();
        TestCase.assertTrue(extendedRelations.isEmpty());

        // 2. Add extension
        // 5. Add relationship between "geometry2d" and "geometry1_tiles"
        final String baseTableName = "geometry1";
        final String relatedTableName = "geometry1_tiles";
        final String mappingTableName = "g1_g1t";

        List<UserCustomColumn> additionalColumns = RelatedTablesUtils
                .createAdditionalUserColumns();

        UserMappingTable userMappingTable = UserMappingTable.create(
                mappingTableName, additionalColumns);
        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        TestCase.assertEquals(UserMappingTable.numRequiredColumns()
                + additionalColumns.size(), userMappingTable.getColumns()
                .size());
        UserCustomColumn baseIdColumn = userMappingTable.getBaseIdColumn();
        TestCase.assertNotNull(baseIdColumn);
        TestCase.assertTrue(baseIdColumn
                .isNamed(UserMappingTable.COLUMN_BASE_ID));
        TestCase.assertTrue(baseIdColumn.isNotNull());
        TestCase.assertFalse(baseIdColumn.isPrimaryKey());
        UserCustomColumn relatedIdColumn = userMappingTable
                .getRelatedIdColumn();
        TestCase.assertNotNull(relatedIdColumn);
        TestCase.assertTrue(relatedIdColumn
                .isNamed(UserMappingTable.COLUMN_RELATED_ID));
        TestCase.assertTrue(relatedIdColumn.isNotNull());
        TestCase.assertFalse(relatedIdColumn.isPrimaryKey());

        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        ExtendedRelation extendedRelation = rte.addTilesRelationship(
                baseTableName, relatedTableName, userMappingTable);
        TestCase.assertTrue(rte.has());
        TestCase.assertTrue(rte.has(userMappingTable.getTableName()));
        TestCase.assertNotNull(extendedRelation);
        extendedRelations = rte.getRelationships();
        TestCase.assertEquals(1, extendedRelations.size());
        TestCase.assertTrue(geoPackage.isTable(mappingTableName));

        // 7. Add mappings
        FeatureDao baseDao = geoPackage.getFeatureDao(baseTableName);
        TileDao relatedDao = geoPackage.getTileDao(relatedTableName);
        FeatureCursor baseFrs = baseDao.queryForAll();
        int baseCount = baseFrs.getCount();
        long[] baseIds = new long[baseCount];
        int inx = 0;
        while (baseFrs.moveToNext()) {
            baseIds[inx++] = baseFrs.getRow().getId();
        }
        baseFrs.close();
        TileCursor relatedTrs = relatedDao.queryForAll();
        int relatedCount = relatedTrs.getCount();
        long[] relatedIds = new long[relatedCount];
        inx = 0;
        while (relatedTrs.moveToNext()) {
            relatedIds[inx++] = relatedTrs.getRow().getId();
        }
        relatedTrs.close();
        UserMappingDao dao = rte.getMappingDao(mappingTableName);
        UserMappingRow userMappingRow = null;
        for (inx = 0; inx < 10; inx++) {
            userMappingRow = dao.newRow();
            userMappingRow.setBaseId(baseIds[(int) (Math.random() * baseCount)]);
            userMappingRow.setRelatedId(relatedIds[(int) (Math.random() * relatedCount)]);
            RelatedTablesUtils.populateUserRow(userMappingTable,
                    userMappingRow, UserMappingTable.requiredColumns());
            TestCase.assertTrue(dao.create(userMappingRow) > 0);
        }

        TestCase.assertEquals(10, dao.count());

        userMappingTable = dao.getTable();
        String[] columns = userMappingTable.getColumnNames();
        UserCustomCursor cursor = dao.queryForAll();
        int count = cursor.getCount();
        TestCase.assertEquals(10, count);
        int manualCount = 0;
        while (cursor.moveToNext()) {

            UserMappingRow resultRow = dao.getRow(cursor);
            TestCase.assertFalse(resultRow.hasId());
            RelatedTablesUtils.validateUserRow(columns, resultRow);
            RelatedTablesUtils.validateDublinCoreColumns(resultRow);

            manualCount++;
        }
        TestCase.assertEquals(count, manualCount);
        cursor.close();

        // 8. Remove mappings (note: it is plausible and allowed
        // to have duplicate entries)
        TestCase.assertTrue(dao.deleteByIds(userMappingRow) > 0);

        // 6. Remove relationship
        rte.removeRelationship(extendedRelation);
        TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
        extendedRelations = rte.getRelationships();
        TestCase.assertEquals(0, extendedRelations.size());
        TestCase.assertFalse(geoPackage.isTable(mappingTableName));

        // 3. Remove extension
        rte.removeExtension();
        TestCase.assertFalse(rte.has());
    }

}
