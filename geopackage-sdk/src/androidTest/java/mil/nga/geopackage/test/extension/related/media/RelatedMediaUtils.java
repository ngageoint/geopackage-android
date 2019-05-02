package mil.nga.geopackage.test.extension.related.media;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.related.ExtendedRelation;
import mil.nga.geopackage.extension.related.ExtendedRelationsDao;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.extension.related.UserMappingDao;
import mil.nga.geopackage.extension.related.UserMappingRow;
import mil.nga.geopackage.extension.related.UserMappingTable;
import mil.nga.geopackage.extension.related.dublin.DublinCoreType;
import mil.nga.geopackage.extension.related.media.MediaDao;
import mil.nga.geopackage.extension.related.media.MediaRow;
import mil.nga.geopackage.extension.related.media.MediaTable;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.extension.related.RelatedTablesUtils;
import mil.nga.geopackage.test.geom.GeoPackageGeometryDataUtils;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomCursor;

public class RelatedMediaUtils {

    /**
     * Test related media tables
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testMedia(Activity activity, Context testContext, GeoPackage geoPackage) throws Exception {

        // Create a related tables extension
        RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);

        if(rte.has()){
            rte.removeExtension();
        }

        TestCase.assertFalse(rte.has());
        TestCase.assertTrue(rte.getRelationships().isEmpty());

        // Choose a random feature table
        List<String> featureTables = geoPackage.getFeatureTables();
        if (featureTables.isEmpty()) {
            return; // pass with no testing
        }
        final String baseTableName = featureTables
                .get((int) (Math.random() * featureTables.size()));

        // Populate and validate a media table
        List<UserCustomColumn> additionalMediaColumns = RelatedTablesUtils
                .createAdditionalUserColumns();
        MediaTable mediaTable = MediaTable.create("media_table",
                additionalMediaColumns);
        String[] mediaColumns = mediaTable.getColumnNames();
        TestCase.assertEquals(MediaTable.numRequiredColumns()
                + additionalMediaColumns.size(), mediaColumns.length);
        UserCustomColumn idColumn = mediaTable.getIdColumn();
        TestCase.assertNotNull(idColumn);
        TestCase.assertTrue(idColumn.isNamed(MediaTable.COLUMN_ID));
        TestCase.assertEquals(GeoPackageDataType.INTEGER,
                idColumn.getDataType());
        TestCase.assertTrue(idColumn.isNotNull());
        TestCase.assertTrue(idColumn.isPrimaryKey());
        UserCustomColumn dataColumn = mediaTable.getDataColumn();
        TestCase.assertNotNull(dataColumn);
        TestCase.assertTrue(dataColumn.isNamed(MediaTable.COLUMN_DATA));
        TestCase.assertEquals(GeoPackageDataType.BLOB, dataColumn.getDataType());
        TestCase.assertTrue(dataColumn.isNotNull());
        TestCase.assertFalse(dataColumn.isPrimaryKey());
        UserCustomColumn contentTypeColumn = mediaTable.getContentTypeColumn();
        TestCase.assertNotNull(contentTypeColumn);
        TestCase.assertTrue(contentTypeColumn
                .isNamed(MediaTable.COLUMN_CONTENT_TYPE));
        TestCase.assertEquals(GeoPackageDataType.TEXT,
                contentTypeColumn.getDataType());
        TestCase.assertTrue(contentTypeColumn.isNotNull());
        TestCase.assertFalse(contentTypeColumn.isPrimaryKey());

        // Create and validate a mapping table
        List<UserCustomColumn> additionalMappingColumns = RelatedTablesUtils
                .createAdditionalUserColumns();
        final String mappingTableName = "features_media";
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

        // Create the media table, content row, and relationship between the
        // feature table and media table
        ContentsDao contentsDao = geoPackage.getContentsDao();
        TestCase.assertFalse(contentsDao.getTables().contains(
                mediaTable.getTableName()));
        ExtendedRelation extendedRelation = rte.addMediaRelationship(
                baseTableName, mediaTable, userMappingTable);
        validateContents(mediaTable, mediaTable.getContents());
        TestCase.assertTrue(rte.has());
        TestCase.assertTrue(rte.has(userMappingTable.getTableName()));
        TestCase.assertNotNull(extendedRelation);
        List<ExtendedRelation> extendedRelations = rte.getRelationships();
        TestCase.assertEquals(1, extendedRelations.size());
        TestCase.assertTrue(geoPackage.isTable(mappingTableName));
        TestCase.assertTrue(geoPackage.isTable(mediaTable.getTableName()));
        TestCase.assertTrue(contentsDao.getTables().contains(
                mediaTable.getTableName()));
        validateContents(mediaTable,
                contentsDao.queryForId(mediaTable.getTableName()));
        TestCase.assertEquals(MediaTable.RELATION_TYPE.getDataType(),
                geoPackage.getTableType(mediaTable.getTableName()));
        TestCase.assertTrue(geoPackage.isTableType(
                MediaTable.RELATION_TYPE.getDataType(), mediaTable.getTableName()));

        // Validate the media DAO
        MediaDao mediaDao = rte.getMediaDao(mediaTable);
        TestCase.assertNotNull(mediaDao);
        mediaTable = mediaDao.getTable();
        TestCase.assertNotNull(mediaTable);
        validateContents(mediaTable, mediaTable.getContents());

        // Insert media table rows
        TestUtils.copyAssetFileToInternalStorage(activity, testContext, TestConstants.TILE_FILE_NAME);
        String mediaImageName = TestUtils.getAssetFileInternalStorageLocation(activity, TestConstants.TILE_FILE_NAME);
        Bitmap mediaImage = BitmapFactory.decodeFile(mediaImageName);
        byte[] mediaData = BitmapConverter.toBytes(mediaImage, Bitmap.CompressFormat.PNG);
        String contentType = "image/png";
        int imageWidth = mediaImage.getWidth();
        int imageHeight = mediaImage.getHeight();
        int mediaCount = 2 + (int) (Math.random() * 9);
        long mediaRowId = 0;
        // Create and insert the first mediaCount - 1 rows
        for (int i = 0; i < mediaCount - 1; i++) {
            MediaRow mediaRow = mediaDao.newRow();
            mediaRow.setData(mediaData);
            mediaRow.setContentType(contentType);
            RelatedTablesUtils.populateUserRow(mediaTable, mediaRow,
                    MediaTable.requiredColumns());
            mediaRowId = mediaDao.create(mediaRow);
            TestCase.assertTrue(mediaRowId > 0);
        }
        // Copy the last row insert and insert the final media row
        MediaRow mediaRowToCopy = new MediaRow(
                mediaDao.queryForIdRow(mediaRowId));
        MediaRow mediaRowCopy = mediaRowToCopy.copy();
        long copyMediaRowId = mediaDao.create(mediaRowCopy);
        TestCase.assertTrue(copyMediaRowId > 0);
        TestCase.assertEquals(mediaRowId + 1, copyMediaRowId);
        TestCase.assertEquals(mediaCount, mediaDao.count());

        // Build the Feature ids
        FeatureDao featureDao = geoPackage.getFeatureDao(baseTableName);
        FeatureCursor featureCursor = featureDao.queryForAll();
        int featureCount = featureCursor.getCount();
        List<Long> featureIds = new ArrayList<>();
        while (featureCursor.moveToNext()) {
            featureIds.add(featureCursor.getRow().getId());
        }
        featureCursor.close();

        // Build the Media ids
        UserCustomCursor mediaCursor = mediaDao.queryForAll();
        mediaCount = mediaCursor.getCount();
        List<Long> mediaIds = new ArrayList<>();
        while (mediaCursor.moveToNext()) {
            mediaIds.add(mediaCursor.getRow().getId());
        }
        mediaCursor.close();

        // Insert user mapping rows between feature ids and media ids
        UserMappingDao dao = rte.getMappingDao(mappingTableName);
        UserMappingRow userMappingRow = null;
        for (int i = 0; i < 10; i++) {
            userMappingRow = dao.newRow();
            userMappingRow
                    .setBaseId(featureIds.get((int) (Math.random() * featureCount)));
            userMappingRow
                    .setRelatedId(mediaIds.get((int) (Math.random() * mediaCount)));
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
            TestCase.assertTrue(mediaIds.contains(resultRow.getRelatedId()));
            RelatedTablesUtils.validateUserRow(mappingColumns, resultRow);
            RelatedTablesUtils.validateDublinCoreColumns(resultRow);

            manualCount++;
        }
        TestCase.assertEquals(count, manualCount);
        cursor.close();

        ExtendedRelationsDao extendedRelationsDao = rte
                .getExtendedRelationsDao();

        // Get the relations starting from the feature table
        List<ExtendedRelation> featureExtendedRelations = extendedRelationsDao
                .getBaseTableRelations(featureDao.getTableName());
        List<ExtendedRelation> featureExtendedRelations2 = extendedRelationsDao
                .getTableRelations(featureDao.getTableName());
        TestCase.assertEquals(1, featureExtendedRelations.size());
        TestCase.assertEquals(1, featureExtendedRelations2.size());
        TestCase.assertEquals(featureExtendedRelations.get(0).getId(),
                featureExtendedRelations2.get(0).getId());
        TestCase.assertTrue(extendedRelationsDao.getRelatedTableRelations(
                featureDao.getTableName()).isEmpty());

        // Test the feature table relations
        for (ExtendedRelation featureRelation : featureExtendedRelations) {

            // Test the relation
            TestCase.assertTrue(featureRelation.getId() >= 0);
            TestCase.assertEquals(featureDao.getTableName(),
                    featureRelation.getBaseTableName());
            TestCase.assertEquals(
                    featureDao.getTable().getPkColumn().getName(),
                    featureRelation.getBasePrimaryColumn());
            TestCase.assertEquals(mediaDao.getTableName(),
                    featureRelation.getRelatedTableName());
            TestCase.assertEquals(mediaDao.getTable().getPkColumn().getName(),
                    featureRelation.getRelatedPrimaryColumn());
            TestCase.assertEquals(MediaTable.RELATION_TYPE.getName(),
                    featureRelation.getRelationName());
            TestCase.assertEquals(mappingTableName,
                    featureRelation.getMappingTableName());

            // Test the user mappings from the relation
            UserMappingDao userMappingDao = rte.getMappingDao(featureRelation);
            int totalMappedCount = userMappingDao.count();
            UserCustomCursor mappingCursor = userMappingDao.queryForAll();
            while (mappingCursor.moveToNext()) {
                userMappingRow = userMappingDao.getRow(mappingCursor);
                TestCase.assertTrue(featureIds.contains(userMappingRow
                        .getBaseId()));
                TestCase.assertTrue(mediaIds.contains(userMappingRow
                        .getRelatedId()));
                RelatedTablesUtils.validateUserRow(mappingColumns,
                        userMappingRow);
                RelatedTablesUtils.validateDublinCoreColumns(userMappingRow);
            }
            mappingCursor.close();

            // Get and test the media DAO
            mediaDao = rte.getMediaDao(featureRelation);
            TestCase.assertNotNull(mediaDao);
            mediaTable = mediaDao.getTable();
            TestCase.assertNotNull(mediaTable);
            validateContents(mediaTable, mediaTable.getContents());

            // Get and test the Media Rows mapped to each Feature Row
            featureCursor = featureDao.queryForAll();
            int totalMapped = 0;
            while (featureCursor.moveToNext()) {
                FeatureRow featureRow = featureCursor.getRow();
                List<Long> mappedIds = rte.getMappingsForBase(featureRelation,
                        featureRow.getId());
                List<MediaRow> mediaRows = mediaDao.getRows(mappedIds);
                TestCase.assertEquals(mappedIds.size(), mediaRows.size());

                for (MediaRow mediaRow : mediaRows) {
                    TestCase.assertTrue(mediaRow.hasId());
                    TestCase.assertTrue(mediaRow.getId() >= 0);
                    TestCase.assertTrue(mediaIds.contains(mediaRow.getId()));
                    TestCase.assertTrue(mappedIds.contains(mediaRow.getId()));
                    GeoPackageGeometryDataUtils.compareByteArrays(mediaData,
                            mediaRow.getData());
                    TestCase.assertEquals(contentType,
                            mediaRow.getContentType());
                    RelatedTablesUtils.validateUserRow(mediaColumns, mediaRow);
                    RelatedTablesUtils.validateDublinCoreColumns(mediaRow);
                    validateDublinCoreColumns(mediaRow);
                    Bitmap image = mediaRow.getDataBitmap();
                    TestCase.assertNotNull(image);
                    TestCase.assertEquals(imageWidth, image.getWidth());
                    TestCase.assertEquals(imageHeight, image.getHeight());
                }

                totalMapped += mappedIds.size();
            }
            featureCursor.close();
            TestCase.assertEquals(totalMappedCount, totalMapped);
        }

        // Get the relations starting from the media table
        List<ExtendedRelation> mediaExtendedRelations = extendedRelationsDao
                .getRelatedTableRelations(mediaTable.getTableName());
        List<ExtendedRelation> mediaExtendedRelations2 = extendedRelationsDao
                .getTableRelations(mediaTable.getTableName());
        TestCase.assertEquals(1, mediaExtendedRelations.size());
        TestCase.assertEquals(1, mediaExtendedRelations2.size());
        TestCase.assertEquals(mediaExtendedRelations.get(0).getId(),
                mediaExtendedRelations2.get(0).getId());
        TestCase.assertTrue(extendedRelationsDao.getBaseTableRelations(
                mediaTable.getTableName()).isEmpty());

        // Test the media table relations
        for (ExtendedRelation mediaRelation : mediaExtendedRelations) {

            // Test the relation
            TestCase.assertTrue(mediaRelation.getId() >= 0);
            TestCase.assertEquals(featureDao.getTableName(),
                    mediaRelation.getBaseTableName());
            TestCase.assertEquals(
                    featureDao.getTable().getPkColumn().getName(),
                    mediaRelation.getBasePrimaryColumn());
            TestCase.assertEquals(mediaDao.getTableName(),
                    mediaRelation.getRelatedTableName());
            TestCase.assertEquals(mediaDao.getTable().getPkColumn().getName(),
                    mediaRelation.getRelatedPrimaryColumn());
            TestCase.assertEquals(MediaTable.RELATION_TYPE.getName(),
                    mediaRelation.getRelationName());
            TestCase.assertEquals(mappingTableName,
                    mediaRelation.getMappingTableName());

            // Test the user mappings from the relation
            UserMappingDao userMappingDao = rte.getMappingDao(mediaRelation);
            int totalMappedCount = userMappingDao.count();
            UserCustomCursor mappingCursor = userMappingDao.queryForAll();
            while (mappingCursor.moveToNext()) {
                userMappingRow = userMappingDao.getRow(mappingCursor);
                TestCase.assertTrue(featureIds.contains(userMappingRow
                        .getBaseId()));
                TestCase.assertTrue(mediaIds.contains(userMappingRow
                        .getRelatedId()));
                RelatedTablesUtils.validateUserRow(mappingColumns,
                        userMappingRow);
                RelatedTablesUtils.validateDublinCoreColumns(userMappingRow);
            }
            mappingCursor.close();

            // Get and test the feature DAO
            featureDao = geoPackage.getFeatureDao(featureDao.getTableName());
            TestCase.assertNotNull(featureDao);
            FeatureTable featureTable = featureDao.getTable();
            TestCase.assertNotNull(featureTable);
            Contents featureContents = featureDao.getGeometryColumns()
                    .getContents();
            TestCase.assertNotNull(featureContents);
            TestCase.assertEquals(ContentsDataType.FEATURES,
                    featureContents.getDataType());
            TestCase.assertEquals(ContentsDataType.FEATURES.getName(),
                    featureContents.getDataTypeString());
            TestCase.assertEquals(featureTable.getTableName(),
                    featureContents.getTableName());
            TestCase.assertNotNull(featureContents.getLastChange());

            // Get and test the Feature Rows mapped to each Media Row
            mediaCursor = mediaDao.queryForAll();
            int totalMapped = 0;
            while (mediaCursor.moveToNext()) {
                MediaRow mediaRow = mediaDao.getRow(mediaCursor);
                List<Long> mappedIds = rte.getMappingsForRelated(mediaRelation,
                        mediaRow.getId());
                for (long mappedId : mappedIds) {
                    FeatureRow featureRow = featureDao.queryForIdRow(mappedId);
                    TestCase.assertNotNull(featureRow);

                    TestCase.assertTrue(featureRow.hasId());
                    TestCase.assertTrue(featureRow.getId() >= 0);
                    TestCase.assertTrue(featureIds.contains(featureRow.getId()));
                    TestCase.assertTrue(mappedIds.contains(featureRow.getId()));
                    if (featureRow
                            .getValue(featureRow.getGeometryColumnIndex()) != null) {
                        GeoPackageGeometryData geometryData = featureRow
                                .getGeometry();
                        TestCase.assertNotNull(geometryData);
                        if (!geometryData.isEmpty()) {
                            TestCase.assertNotNull(geometryData.getGeometry());
                        }
                    }
                }

                totalMapped += mappedIds.size();
            }
            mediaCursor.close();
            TestCase.assertEquals(totalMappedCount, totalMapped);
        }

        // Add more columns to the media table
        int existingColumns = mediaTable.getColumns().size();
        UserCustomColumn mediaIdColumn = mediaTable.getIdColumn();
        UserCustomColumn mediaDataColumn = mediaTable.getDataColumn();
        UserCustomColumn mediaContentTypeColumn = mediaTable
                .getContentTypeColumn();
        int newColumns = 0;
        String newColumnName = "new_column";
        mediaDao.addColumn(UserCustomColumn.createColumn(newColumnName
                + ++newColumns, GeoPackageDataType.TEXT));
        mediaDao.addColumn(UserCustomColumn.createColumn(newColumnName
                + ++newColumns, GeoPackageDataType.BLOB));
        TestCase.assertEquals(existingColumns + 2, mediaTable.getColumns()
                .size());
        for (int index = existingColumns; index < mediaTable.getColumns()
                .size(); index++) {
            String name = newColumnName + (index - existingColumns + 1);
            TestCase.assertEquals(name, mediaTable.getColumnName(index));
            TestCase.assertEquals(index, mediaTable.getColumnIndex(name));
            TestCase.assertEquals(name, mediaTable.getColumn(index).getName());
            TestCase.assertEquals(index, mediaTable.getColumn(index).getIndex());
            TestCase.assertEquals(name, mediaTable.getColumnNames()[index]);
            TestCase.assertEquals(name, mediaTable.getColumns().get(index)
                    .getName());
            try {
                mediaTable.getColumn(index).setIndex(index - 1);
                TestCase.fail("Changed index on a created table column");
            } catch (Exception e) {
            }
            mediaTable.getColumn(index).setIndex(index);
        }
        TestCase.assertEquals(mediaIdColumn, mediaTable.getIdColumn());
        TestCase.assertEquals(mediaDataColumn, mediaTable.getDataColumn());
        TestCase.assertEquals(mediaContentTypeColumn,
                mediaTable.getContentTypeColumn());

        // Add another row with the new columns and read it
        MediaRow mediaRow = mediaDao.newRow();
        mediaRow.setData(mediaData);
        mediaRow.setContentType(contentType);
        RelatedTablesUtils.populateUserRow(mediaTable, mediaRow,
                MediaTable.requiredColumns());
        String newValue = UUID.randomUUID().toString();
        mediaRow.setValue(existingColumns, newValue);
        mediaRow.setValue(existingColumns + 1, mediaRow.getData());
        mediaRowId = mediaDao.create(mediaRow);
        TestCase.assertTrue(mediaRowId > 0);
        MediaRow newMediaRow = mediaDao.getRow(mediaDao
                .queryForIdRow(mediaRowId));
        TestCase.assertNotNull(newMediaRow);
        TestCase.assertEquals(newValue, newMediaRow.getValue(existingColumns));
        GeoPackageGeometryDataUtils.compareByteArrays(mediaRow.getData(),
                (byte[]) newMediaRow.getValue(existingColumns + 1));

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

        // Delete the media table and contents row
        TestCase.assertTrue(geoPackage.isTable(mediaTable.getTableName()));
        TestCase.assertNotNull(contentsDao.queryForId(mediaTable.getTableName()));
        geoPackage.deleteTable(mediaTable.getTableName());
        TestCase.assertFalse(geoPackage.isTable(mediaTable.getTableName()));
        TestCase.assertNull(contentsDao.queryForId(mediaTable.getTableName()));

        // Delete the related tables extension
        rte.removeExtension();
        TestCase.assertFalse(rte.has());

    }

    /**
     * Validate contents
     *
     * @param mediaTable media table
     * @param contents   contents
     */
    private static void validateContents(MediaTable mediaTable,
                                         Contents contents) {
        TestCase.assertNotNull(contents);
        TestCase.assertNotNull(contents.getDataType());
        TestCase.assertEquals(MediaTable.RELATION_TYPE.getDataType(), contents
                .getDataType().getName());
        TestCase.assertEquals(MediaTable.RELATION_TYPE.getDataType(),
                contents.getDataTypeString());
        TestCase.assertEquals(mediaTable.getTableName(),
                contents.getTableName());
        TestCase.assertNotNull(contents.getLastChange());
    }

    /**
     * Validate a media row for expected Dublin Core Columns
     *
     * @param mediaRow media row
     */
    public static void validateDublinCoreColumns(MediaRow mediaRow) {

        RelatedTablesUtils.validateDublinCoreColumn(mediaRow,
                DublinCoreType.IDENTIFIER);
        RelatedTablesUtils.validateDublinCoreColumn(mediaRow,
                DublinCoreType.FORMAT);

    }

}
