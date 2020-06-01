package mil.nga.geopackage.test;

import android.app.Activity;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.contents.ContentsDataType;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.TableColumnKey;
import mil.nga.geopackage.extension.coverage.CoverageData;
import mil.nga.geopackage.GeoPackageFactory;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTableMetadata;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.sf.GeometryType;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;

/**
 * GeoPackage Utility test methods
 *
 * @author osbornb
 */
public class GeoPackageTestUtils {

    /**
     * Test create feature table with metadata
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testCreateFeatureTableWithMetadata(GeoPackage geoPackage)
            throws SQLException {

        SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao().getOrCreateCode(
                ProjectionConstants.AUTHORITY_EPSG, ProjectionConstants.EPSG_WEB_MERCATOR);

        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey("feature_metadata", "geom"));
        geometryColumns.setGeometryType(GeometryType.POINT);
        geometryColumns.setZ((byte) 1);
        geometryColumns.setM((byte) 0);
        geometryColumns.setSrs(srs);

        BoundingBox boundingBox = new BoundingBox(-90, -45, 90, 45);

        geoPackage.createFeatureTable(
                FeatureTableMetadata.create(geometryColumns, boundingBox));

        validateFeatureTableWithMetadata(geoPackage, geometryColumns, null,
                null);
    }

    /**
     * Test create feature table with metadata and id column
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testCreateFeatureTableWithMetadataIdColumn(
            GeoPackage geoPackage) throws SQLException {

        SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao().getOrCreateCode(
                ProjectionConstants.AUTHORITY_EPSG, ProjectionConstants.EPSG_WEB_MERCATOR);

        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey("feature_metadata2", "geom2"));
        geometryColumns.setGeometryType(GeometryType.POINT);
        geometryColumns.setZ((byte) 1);
        geometryColumns.setM((byte) 0);
        geometryColumns.setSrs(srs);

        BoundingBox boundingBox = new BoundingBox(-90, -45, 90, 45);

        String idColumn = "my_id";
        geoPackage.createFeatureTable(FeatureTableMetadata.create(geometryColumns,
                idColumn, boundingBox));

        validateFeatureTableWithMetadata(geoPackage, geometryColumns, idColumn,
                null);
    }

    /**
     * Test create feature table with metadata and additional columns
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testCreateFeatureTableWithMetadataAdditionalColumns(
            GeoPackage geoPackage) throws SQLException {

        SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao().getOrCreateCode(
                ProjectionConstants.AUTHORITY_EPSG, ProjectionConstants.EPSG_WEB_MERCATOR);

        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey("feature_metadata3", "geom3"));
        geometryColumns.setGeometryType(GeometryType.POINT);
        geometryColumns.setZ((byte) 1);
        geometryColumns.setM((byte) 0);
        geometryColumns.setSrs(srs);

        BoundingBox boundingBox = new BoundingBox(-90, -45, 90, 45);

        List<FeatureColumn> additionalColumns = getFeatureColumns();

        geoPackage.createFeatureTable(FeatureTableMetadata.create(geometryColumns,
                additionalColumns, boundingBox));

        validateFeatureTableWithMetadata(geoPackage, geometryColumns, null,
                additionalColumns);
    }

    /**
     * Test create feature table with metadata, id column, and additional
     * columns
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testCreateFeatureTableWithMetadataIdColumnAdditionalColumns(
            GeoPackage geoPackage) throws SQLException {

        SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao().getOrCreateCode(
                ProjectionConstants.AUTHORITY_EPSG, ProjectionConstants.EPSG_WEB_MERCATOR);

        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey("feature_metadata4", "geom4"));
        geometryColumns.setGeometryType(GeometryType.POINT);
        geometryColumns.setZ((byte) 1);
        geometryColumns.setM((byte) 0);
        geometryColumns.setSrs(srs);

        BoundingBox boundingBox = new BoundingBox(-90, -45, 90, 45);

        List<FeatureColumn> additionalColumns = getFeatureColumns();

        String idColumn = "my_other_id";
        geoPackage.createFeatureTable(FeatureTableMetadata.create(geometryColumns,
                idColumn, additionalColumns, boundingBox));

        validateFeatureTableWithMetadata(geoPackage, geometryColumns, idColumn,
                additionalColumns);
    }

    /**
     * Get additional feature columns to create
     *
     * @return
     */
    public static List<FeatureColumn> getFeatureColumns() {
        List<FeatureColumn> columns = new ArrayList<FeatureColumn>();

        columns.add(FeatureColumn.createColumn(7, "test_text_limited",
                GeoPackageDataType.TEXT, 5L, false, null));
        columns.add(FeatureColumn.createColumn(8, "test_blob_limited",
                GeoPackageDataType.BLOB, 7L, false, null));
        columns.add(FeatureColumn.createColumn(9, "test_date",
                GeoPackageDataType.DATE, false, null));
        columns.add(FeatureColumn.createColumn(10, "test_datetime",
                GeoPackageDataType.DATETIME, false, null));
        columns.add(FeatureColumn.createColumn(2, "test_text",
                GeoPackageDataType.TEXT, false, ""));
        columns.add(FeatureColumn.createColumn(3, "test_real",
                GeoPackageDataType.REAL, false, null));
        columns.add(FeatureColumn.createColumn(4, "test_boolean",
                GeoPackageDataType.BOOLEAN, false, null));
        columns.add(FeatureColumn.createColumn(5, "test_blob",
                GeoPackageDataType.BLOB, false, null));
        columns.add(FeatureColumn.createColumn(6, "test_integer",
                GeoPackageDataType.INTEGER, false, null));

        return columns;
    }

    /**
     * Validate feature table with metadata
     *
     * @param geoPackage
     * @throws SQLException
     */
    private static void validateFeatureTableWithMetadata(GeoPackage geoPackage,
                                                         GeometryColumns geometryColumns, String idColumn,
                                                         List<FeatureColumn> additionalColumns) throws SQLException {

        GeometryColumnsDao dao = geoPackage.getGeometryColumnsDao();

        GeometryColumns queryGeometryColumns = dao.queryForId(geometryColumns
                .getId());
        TestCase.assertNotNull(queryGeometryColumns);

        TestCase.assertEquals(geometryColumns.getTableName(),
                queryGeometryColumns.getTableName());
        TestCase.assertEquals(geometryColumns.getColumnName(),
                queryGeometryColumns.getColumnName());
        TestCase.assertEquals(GeometryType.POINT,
                queryGeometryColumns.getGeometryType());
        TestCase.assertEquals(geometryColumns.getZ(),
                queryGeometryColumns.getZ());
        TestCase.assertEquals(geometryColumns.getM(),
                queryGeometryColumns.getM());

        FeatureDao featureDao = geoPackage.getFeatureDao(geometryColumns
                .getTableName());
        FeatureRow featureRow = featureDao.newRow();

        TestCase.assertEquals(
                2 + (additionalColumns != null ? additionalColumns.size() : 0),
                featureRow.columnCount());
        if (idColumn == null) {
            idColumn = "id";
        }
        TestCase.assertEquals(idColumn, featureRow.getColumnName(0));
        TestCase.assertEquals(geometryColumns.getColumnName(),
                featureRow.getColumnName(1));

        if (additionalColumns != null) {
            TestCase.assertEquals("test_text", featureRow.getColumnName(2));
            TestCase.assertEquals("test_real", featureRow.getColumnName(3));
            TestCase.assertEquals("test_boolean", featureRow.getColumnName(4));
            TestCase.assertEquals("test_blob", featureRow.getColumnName(5));
            TestCase.assertEquals("test_integer", featureRow.getColumnName(6));
            TestCase.assertEquals("test_text_limited",
                    featureRow.getColumnName(7));
            TestCase.assertEquals("test_blob_limited",
                    featureRow.getColumnName(8));
        }
    }

    /**
     * Test deleting tables by name
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testDeleteTables(GeoPackage geoPackage)
            throws SQLException {

        GeometryColumnsDao geometryColumnsDao = geoPackage
                .getGeometryColumnsDao();
        TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
        ContentsDao contentsDao = geoPackage.getContentsDao();

        TestCase.assertTrue(geometryColumnsDao.isTableExists()
                || tileMatrixSetDao.isTableExists());

        geoPackage.foreignKeys(false);

        if (geometryColumnsDao.isTableExists()) {

            TestCase.assertEquals(geoPackage.getFeatureTables().size(),
                    geometryColumnsDao.countOf());
            for (String featureTable : geoPackage.getFeatureTables()) {
                TestCase.assertTrue(geoPackage.isTable(featureTable));
                TestCase.assertNotNull(contentsDao.queryForId(featureTable));
                geoPackage.deleteTable(featureTable);
                TestCase.assertFalse(geoPackage.isTable(featureTable));
                TestCase.assertNull(contentsDao.queryForId(featureTable));
            }
            TestCase.assertEquals(0, geometryColumnsDao.countOf());

            geoPackage.dropTable(GeometryColumns.TABLE_NAME);

            TestCase.assertFalse(geometryColumnsDao.isTableExists());
        }

        if (tileMatrixSetDao.isTableExists()) {
            TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

            TestCase.assertTrue(tileMatrixSetDao.isTableExists());
            TestCase.assertTrue(tileMatrixDao.isTableExists());

            TestCase.assertEquals(geoPackage.getTables(ContentsDataType.TILES).size() + geoPackage.getTables(CoverageData.GRIDDED_COVERAGE).size(),
                    tileMatrixSetDao.countOf());
            for (String tileTable : geoPackage.getTileTables()) {
                TestCase.assertTrue(geoPackage.isTable(tileTable));
                TestCase.assertNotNull(contentsDao.queryForId(tileTable));
                geoPackage.deleteTable(tileTable);
                TestCase.assertFalse(geoPackage.isTable(tileTable));
                TestCase.assertNull(contentsDao.queryForId(tileTable));
            }
            TestCase.assertEquals(geoPackage.getTables(CoverageData.GRIDDED_COVERAGE).size(), tileMatrixSetDao.countOf());

            geoPackage.dropTable(TileMatrix.TABLE_NAME);
            geoPackage.dropTable(TileMatrixSet.TABLE_NAME);

            TestCase.assertFalse(tileMatrixSetDao.isTableExists());
            TestCase.assertFalse(tileMatrixDao.isTableExists());
        }

        for (String attributeTable : geoPackage.getAttributesTables()) {

            TestCase.assertTrue(geoPackage.isTable(attributeTable));
            TestCase.assertNotNull(contentsDao.queryForId(attributeTable));
            geoPackage.deleteTable(attributeTable);
            TestCase.assertFalse(geoPackage.isTable(attributeTable));
            TestCase.assertNull(contentsDao.queryForId(attributeTable));

        }

    }

    /**
     * Test GeoPackage bounds
     *
     * @param activity   app activity
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testBounds(Activity activity, GeoPackage geoPackage) throws SQLException {

        Projection projection = ProjectionFactory.getProjection(
                ProjectionConstants.AUTHORITY_EPSG,
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
                .queryForOrganizationCoordsysId(
                        ProjectionConstants.AUTHORITY_EPSG,
                        ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        // Create a feature table with empty contents
        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey("feature_empty_contents",
                "geom"));
        geometryColumns.setGeometryType(GeometryType.POINT);
        geometryColumns.setZ((byte) 0);
        geometryColumns.setM((byte) 0);
        geometryColumns.setSrs(srs);
        geoPackage
                .createFeatureTable(FeatureTableMetadata.create(geometryColumns));

        BoundingBox geoPackageContentsBoundingBox = geoPackage
                .getContentsBoundingBox(projection);

        BoundingBox expectedContentsBoundingBox = null;

        ContentsDao contentsDao = geoPackage.getContentsDao();
        for (Contents contents : contentsDao.queryForAll()) {

            BoundingBox contentsBoundingBox = contents
                    .getBoundingBox(projection);
            if (contentsBoundingBox != null) {
                TestCase.assertTrue(geoPackageContentsBoundingBox
                        .contains(contentsBoundingBox));

                if (expectedContentsBoundingBox == null) {
                    expectedContentsBoundingBox = contentsBoundingBox;
                } else {
                    expectedContentsBoundingBox = expectedContentsBoundingBox
                            .union(contentsBoundingBox);
                }
            }

            TestCase.assertEquals(
                    contentsBoundingBox,
                    geoPackage.getContentsBoundingBox(projection,
                            contents.getTableName()));
            TestCase.assertEquals(contents.getBoundingBox(),
                    geoPackage.getContentsBoundingBox(contents.getTableName()));

        }

        TestCase.assertEquals(expectedContentsBoundingBox,
                geoPackageContentsBoundingBox);

        BoundingBox geoPackageBoundingBox = geoPackage
                .getBoundingBox(projection);
        BoundingBox geoPackageManualBoundingBox = geoPackage.getBoundingBox(
                projection, true);

        BoundingBox expectedBoundingBox = expectedContentsBoundingBox;
        BoundingBox expectedManualBoundingBox = expectedContentsBoundingBox;

        for (Contents contents : contentsDao.queryForAll()) {

            ContentsDataType dataType = contents.getDataType();
            if (dataType != null) {

                switch (dataType) {
                    case FEATURES:
                        FeatureIndexManager manager = new FeatureIndexManager(activity,
                                geoPackage, contents.getTableName());
                        BoundingBox featureBoundingBox = manager
                                .getBoundingBox(projection);
                        if (featureBoundingBox != null) {
                            if (manager.isIndexed()) {
                                expectedBoundingBox = expectedBoundingBox
                                        .union(featureBoundingBox);
                            }
                            expectedManualBoundingBox = expectedManualBoundingBox
                                    .union(featureBoundingBox);
                        }

                        BoundingBox expectedFeatureProjectionBoundingBox = contents
                                .getBoundingBox(projection);
                        if (featureBoundingBox != null && manager.isIndexed()) {
                            if (expectedFeatureProjectionBoundingBox == null) {
                                expectedFeatureProjectionBoundingBox = featureBoundingBox;
                            } else {
                                expectedFeatureProjectionBoundingBox = expectedFeatureProjectionBoundingBox
                                        .union(featureBoundingBox);
                            }
                        }
                        BoundingBox featureProjectionBoundingBox = geoPackage
                                .getBoundingBox(projection, contents.getTableName());
                        if (featureProjectionBoundingBox == null) {
                            TestCase.assertNull(expectedFeatureProjectionBoundingBox);
                        } else {
                            TestCase.assertTrue(expectedBoundingBox
                                    .contains(featureProjectionBoundingBox));
                            TestCase.assertEquals(
                                    expectedFeatureProjectionBoundingBox,
                                    featureProjectionBoundingBox);
                        }

                        BoundingBox expectedFeatureManualProjectionBoundingBox = contents
                                .getBoundingBox(projection);
                        if (featureBoundingBox != null) {
                            if (expectedFeatureManualProjectionBoundingBox == null) {
                                expectedFeatureManualProjectionBoundingBox = featureBoundingBox;
                            } else {
                                expectedFeatureManualProjectionBoundingBox = expectedFeatureManualProjectionBoundingBox
                                        .union(featureBoundingBox);
                            }
                        }
                        BoundingBox featureManualProjectionBoundingBox = geoPackage
                                .getBoundingBox(projection,
                                        contents.getTableName(), true);
                        if (featureManualProjectionBoundingBox == null) {
                            TestCase.assertNull(expectedFeatureManualProjectionBoundingBox);
                        } else {
                            TestCase.assertTrue(expectedManualBoundingBox
                                    .contains(featureManualProjectionBoundingBox));
                            TestCase.assertEquals(
                                    expectedFeatureManualProjectionBoundingBox,
                                    featureManualProjectionBoundingBox);
                        }

                        featureBoundingBox = manager.getBoundingBox();

                        BoundingBox expectedFeatureBoundingBox = contents
                                .getBoundingBox();
                        if (featureBoundingBox != null && manager.isIndexed()) {
                            if (expectedFeatureBoundingBox == null) {
                                expectedFeatureBoundingBox = featureBoundingBox;
                            } else {
                                expectedFeatureBoundingBox = expectedFeatureBoundingBox
                                        .union(featureBoundingBox);
                            }
                        }
                        BoundingBox featureBox = geoPackage.getBoundingBox(contents
                                .getTableName());
                        if (featureBox == null) {
                            TestCase.assertNull(expectedFeatureBoundingBox);
                        } else {
                            TestCase.assertEquals(expectedFeatureBoundingBox,
                                    featureBox);
                        }

                        BoundingBox expectedFeatureManualBoundingBox = contents
                                .getBoundingBox();
                        if (featureBoundingBox != null) {
                            if (expectedFeatureManualBoundingBox == null) {
                                expectedFeatureManualBoundingBox = featureBoundingBox;
                            } else {
                                expectedFeatureManualBoundingBox = expectedFeatureManualBoundingBox
                                        .union(featureBoundingBox);
                            }
                        }
                        BoundingBox featureManualBoundingBox = geoPackage
                                .getBoundingBox(contents.getTableName(), true);
                        if (featureManualBoundingBox == null) {
                            TestCase.assertNull(expectedFeatureManualBoundingBox);
                        } else {
                            TestCase.assertEquals(expectedFeatureManualBoundingBox,
                                    featureManualBoundingBox);
                        }

                        manager.close();

                        break;
                    case TILES:
                        TileDao tileDao = geoPackage.getTileDao(contents
                                .getTableName());
                        BoundingBox tileBoundingBox = tileDao
                                .getBoundingBox(projection);
                        expectedBoundingBox = expectedBoundingBox
                                .union(tileBoundingBox);
                        expectedManualBoundingBox = expectedManualBoundingBox
                                .union(tileBoundingBox);

                        BoundingBox expectedProjectionTileBoundingBox = tileBoundingBox
                                .union(contents.getBoundingBox(projection));
                        TestCase.assertEquals(
                                expectedProjectionTileBoundingBox,
                                geoPackage.getBoundingBox(projection,
                                        contents.getTableName()));
                        TestCase.assertEquals(
                                expectedProjectionTileBoundingBox,
                                geoPackage.getBoundingBox(projection,
                                        contents.getTableName(), true));

                        BoundingBox expectedTileBoundingBox = tileDao
                                .getBoundingBox().union(contents.getBoundingBox());
                        TestCase.assertEquals(expectedTileBoundingBox,
                                geoPackage.getBoundingBox(contents.getTableName()));
                        TestCase.assertEquals(expectedTileBoundingBox, geoPackage
                                .getBoundingBox(contents.getTableName(), true));
                        break;
                    default:
                        break;
                }

            }

        }

        TestCase.assertEquals(expectedBoundingBox, geoPackageBoundingBox);
        TestCase.assertEquals(expectedManualBoundingBox,
                geoPackageManualBoundingBox);

    }

    /**
     * Test the GeoPackage vacuum
     *
     * @param activity   activity
     * @param geoPackage GeoPackage
     */
    public static void testVacuum(Activity activity, GeoPackage geoPackage) {

        GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        long size = manager.size(geoPackage.getName());

        for (String table : geoPackage.getTables()) {

            geoPackage.deleteTable(table);

            geoPackage.vacuum();

            long newSize = manager.size(geoPackage.getName());
            TestCase.assertTrue(size > newSize);
            size = newSize;

        }

    }

    /**
     * Test the GeoPackage table types
     *
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testTableTypes(GeoPackage geoPackage)
            throws SQLException {

        Map<String, Set<String>> types = new HashMap<>();
        Map<String, String> tables = new HashMap<>();

        for (ContentsDataType type : ContentsDataType.values()) {
            types.put(type.getName(), new HashSet<>());
        }

        ContentsDao dao = geoPackage.getContentsDao();
        for (Contents contents : dao.queryForAll()) {

            String tableName = contents.getTableName();
            String dataTypeNameString = contents.getDataTypeName();

            Set<String> typeTables = types.get(dataTypeNameString);
            if (typeTables == null) {
                typeTables = new HashSet<>();
                types.put(dataTypeNameString, typeTables);
            }
            typeTables.add(tableName);

            tables.put(tableName, dataTypeNameString);
        }

        String previousType = null;

        for (String type : types.keySet()) {

            Set<String> typeTables = types.get(type);

            List<String> tablesByType = geoPackage.getTables(type);
            TestCase.assertEquals(typeTables.size(), tablesByType.size());
            for (String tableByType : tablesByType) {
                TestCase.assertTrue(typeTables.contains(tableByType));
            }

            ContentsDataType coreDataType = ContentsDataType.fromCoreName(type);
            if (coreDataType != null) {
                List<String> coreTablesByType = geoPackage
                        .getTables(coreDataType);
                TestCase.assertEquals(typeTables.size(),
                        coreTablesByType.size());
                for (String coreTableByType : coreTablesByType) {
                    TestCase.assertTrue(typeTables.contains(coreTableByType));
                }
            }

            if (previousType != null) {

                Set<String> previousTypeTables = types.get(previousType);

                List<String> tablesByMultiType = geoPackage.getTables(type,
                        previousType);
                TestCase.assertEquals(
                        typeTables.size() + previousTypeTables.size(),
                        tablesByMultiType.size());
                for (String tableByMultiType : tablesByMultiType) {
                    TestCase.assertTrue(typeTables.contains(tableByMultiType)
                            || previousTypeTables.contains(tableByMultiType));
                }

                if (coreDataType != null) {
                    ContentsDataType previousCoreDataType = ContentsDataType
                            .fromCoreName(previousType);
                    if (previousCoreDataType != null) {
                        List<String> coreTablesByMultiType = geoPackage
                                .getTables(coreDataType, previousCoreDataType);
                        TestCase.assertEquals(
                                typeTables.size() + previousTypeTables.size(),
                                coreTablesByMultiType.size());
                        for (String coreTableByMultiType : coreTablesByMultiType) {
                            TestCase.assertTrue(
                                    typeTables.contains(coreTableByMultiType)
                                            || previousTypeTables.contains(
                                            coreTableByMultiType));
                        }

                    }
                }

            }

            previousType = type;
        }

        previousType = null;

        for (String table : tables.keySet()) {
            String type = tables.get(table);

            TestCase.assertTrue(geoPackage.isTableOrView(table));
            TestCase.assertTrue(geoPackage.isContentsTable(table));
            TestCase.assertTrue(geoPackage.isTableType(table, type));

            ContentsDataType dataType = ContentsDataType.fromName(type);
            if (dataType != null) {
                TestCase.assertTrue(ContentsDataType.isType(type));
                TestCase.assertTrue(geoPackage.isTableType(table, dataType));
                boolean attributes = dataType == ContentsDataType.ATTRIBUTES;
                boolean features = dataType == ContentsDataType.FEATURES;
                boolean tiles = dataType == ContentsDataType.TILES;

                TestCase.assertTrue(attributes || features || tiles);
                TestCase.assertEquals(attributes,
                        geoPackage.isAttributeTable(table));
                TestCase.assertEquals(features,
                        geoPackage.isFeatureTable(table));
                TestCase.assertEquals(tiles, geoPackage.isTileTable(table));
            }

            if (previousType != null && !type.equals(previousType)) {
                TestCase.assertTrue(
                        geoPackage.isTableType(table, type, previousType));

                if (dataType != null) {
                    ContentsDataType previousDataType = ContentsDataType
                            .fromName(previousType);
                    if (previousDataType != null) {
                        boolean sameCoreType = dataType == previousDataType;
                        TestCase.assertEquals(sameCoreType, geoPackage
                                .isTableType(table, previousDataType));
                        TestCase.assertTrue(geoPackage.isTableType(table,
                                dataType, previousDataType));
                    }
                }
            }

            previousType = type;
        }

    }

}
