package mil.nga.geopackage.test.db;

import android.content.Context;

import junit.framework.TestCase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesTable;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageCoreConnection;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.master.SQLiteMaster;
import mil.nga.geopackage.db.master.SQLiteMasterColumn;
import mil.nga.geopackage.db.master.SQLiteMasterQuery;
import mil.nga.geopackage.db.master.SQLiteMasterType;
import mil.nga.geopackage.db.table.Constraint;
import mil.nga.geopackage.db.table.ConstraintType;
import mil.nga.geopackage.db.table.RawConstraint;
import mil.nga.geopackage.db.table.UniqueConstraint;
import mil.nga.geopackage.extension.coverage.CoverageData;
import mil.nga.geopackage.extension.coverage.GriddedCoverage;
import mil.nga.geopackage.extension.coverage.GriddedTile;
import mil.nga.geopackage.extension.metadata.MetadataExtension;
import mil.nga.geopackage.extension.metadata.reference.MetadataReference;
import mil.nga.geopackage.extension.metadata.reference.MetadataReferenceDao;
import mil.nga.geopackage.extension.nga.contents.ContentsId;
import mil.nga.geopackage.extension.nga.contents.ContentsIdExtension;
import mil.nga.geopackage.extension.nga.link.FeatureTileTableLinker;
import mil.nga.geopackage.extension.nga.scale.TileScaling;
import mil.nga.geopackage.extension.nga.scale.TileTableScaling;
import mil.nga.geopackage.extension.nga.style.FeatureTableStyles;
import mil.nga.geopackage.extension.nga.style.StyleMappingDao;
import mil.nga.geopackage.extension.related.ExtendedRelation;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.extension.schema.SchemaExtension;
import mil.nga.geopackage.extension.schema.columns.DataColumns;
import mil.nga.geopackage.extension.schema.columns.DataColumnsDao;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.test.attributes.AttributesUtils;
import mil.nga.geopackage.test.features.user.FeatureUtils;
import mil.nga.geopackage.test.tiles.user.TileUtils;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileTable;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomRow;
import mil.nga.geopackage.user.custom.UserCustomTable;
import mil.nga.sf.proj.ProjectionConstants;

/**
 * Alter Table test utils
 *
 * @author osbornb
 */
public class AlterTableUtils {


    /**
     * Test column table alterations
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testColumns(Context context, GeoPackage geoPackage) throws SQLException {

        GeometryColumnsDao geometryColumnsDao = geoPackage
                .getGeometryColumnsDao();

        if (geometryColumnsDao.isTableExists()) {
            List<GeometryColumns> results = geometryColumnsDao.queryForAll();

            for (GeometryColumns geometryColumns : results) {

                GeoPackageConnection db = geoPackage.getConnection();
                FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

                FeatureIndexManager indexManager = new FeatureIndexManager(context,
                        geoPackage, dao);
                indexManager.setContinueOnError(false);
                int indexGeoPackageCount;
                if (indexManager.isIndexed(FeatureIndexType.GEOPACKAGE)) {
                    indexManager.prioritizeQueryLocation(
                            FeatureIndexType.GEOPACKAGE);
                    indexGeoPackageCount = (int) indexManager.count();
                } else {
                    indexGeoPackageCount = indexManager
                            .index(FeatureIndexType.GEOPACKAGE);
                }
                TestCase.assertTrue(
                        indexManager.isIndexed(FeatureIndexType.GEOPACKAGE));

                int indexRTreeCount;
                if (indexManager.isIndexed(FeatureIndexType.RTREE)) {
                    indexManager
                            .prioritizeQueryLocation(FeatureIndexType.RTREE);
                    indexRTreeCount = (int) indexManager.count();
                } else {
                    indexRTreeCount = 0;
                    //indexRTreeCount = indexManager
                    //        .index(FeatureIndexType.RTREE);
                }
                //TestCase.assertTrue(
                //        indexManager.isIndexed(FeatureIndexType.RTREE));

                FeatureTable featureTable = dao.getTable();
                String tableName = featureTable.getTableName();

                for (FeatureColumn column : featureTable.getColumns()) {
                    indexColumn(db, tableName, column);
                }

                createViewWithPrefix(db, featureTable, "v_", true);
                createViewWithPrefix(db, featureTable, "v2_", false);

                int rowCount = dao.count();
                int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
                        SQLiteMasterType.TABLE, tableName);
                int indexCount = indexCount(geoPackage.getDatabase(),
                        tableName);
                int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
                        SQLiteMasterType.TRIGGER, tableName);
                int viewCount = SQLiteMaster
                        .countViewsOnTable(geoPackage.getDatabase(), tableName);

                TestCase.assertEquals(1, tableCount);
                TestCase.assertTrue(
                        indexCount >= featureTable.columnCount() - 2);
                //TestCase.assertTrue(triggerCount >= 6);
                TestCase.assertTrue(viewCount >= 2);

                FeatureTable table = dao.getTable();
                int existingColumns = table.getColumns().size();
                FeatureColumn pk = table.getPkColumn();
                FeatureColumn geometry = table.getGeometryColumn();

                int newColumns = 0;
                String newColumnName = "new_column";

                dao.addColumn(
                        FeatureColumn.createColumn(newColumnName + ++newColumns,
                                GeoPackageDataType.TEXT, false, ""));
                dao.addColumn(FeatureColumn.createColumn(
                        newColumnName + ++newColumns, GeoPackageDataType.REAL));
                dao.addColumn(
                        FeatureColumn.createColumn(newColumnName + ++newColumns,
                                GeoPackageDataType.BOOLEAN));
                dao.addColumn(FeatureColumn.createColumn(
                        newColumnName + ++newColumns, GeoPackageDataType.BLOB));
                dao.addColumn(
                        FeatureColumn.createColumn(newColumnName + ++newColumns,
                                GeoPackageDataType.INTEGER));
                dao.addColumn(FeatureColumn.createColumn(
                        newColumnName + ++newColumns, GeoPackageDataType.TEXT,
                        (long) UUID.randomUUID().toString().length()));
                dao.addColumn(FeatureColumn.createColumn(
                        newColumnName + ++newColumns, GeoPackageDataType.BLOB,
                        (long) UUID.randomUUID().toString().getBytes().length));
                dao.addColumn(FeatureColumn.createColumn(
                        newColumnName + ++newColumns, GeoPackageDataType.DATE));
                dao.addColumn(
                        FeatureColumn.createColumn(newColumnName + ++newColumns,
                                GeoPackageDataType.DATETIME));

                TestCase.assertEquals(existingColumns + newColumns,
                        table.getColumns().size());
                TestCase.assertEquals(rowCount, dao.count());
                testTableCounts(db, tableName, tableCount, indexCount,
                        triggerCount, viewCount);

                for (int index = existingColumns; index < table.getColumns()
                        .size(); index++) {

                    indexColumn(db, tableName, table.getColumn(index));

                    String name = newColumnName + (index - existingColumns + 1);
                    TestCase.assertEquals(name, table.getColumnName(index));
                    TestCase.assertEquals(index, table.getColumnIndex(name));
                    TestCase.assertEquals(name,
                            table.getColumn(index).getName());
                    TestCase.assertEquals(index,
                            table.getColumn(index).getIndex());
                    TestCase.assertEquals(name, table.getColumnNames()[index]);
                    TestCase.assertEquals(name,
                            table.getColumns().get(index).getName());
                    try {
                        table.getColumn(index).setIndex(index - 1);
                        TestCase.fail(
                                "Changed index on a created table column");
                    } catch (Exception e) {
                    }
                    table.getColumn(index).setIndex(index);
                }

                testTableCounts(db, tableName, tableCount,
                        indexCount + newColumns, triggerCount, viewCount);

                TestCase.assertEquals(geometryColumns.getTableName(),
                        table.getTableName());
                TestCase.assertEquals(pk, table.getPkColumn());
                TestCase.assertEquals(geometry, table.getGeometryColumn());

                testIndex(indexManager, indexGeoPackageCount, indexRTreeCount);

                FeatureUtils.testUpdate(dao);

                String newerColumnName = "newer_column";
                for (int newColumn = 2; newColumn <= newColumns; newColumn++) {
                    dao.renameColumn(newColumnName + newColumn,
                            newerColumnName + newColumn);
                }

                dao.alterColumn(FeatureColumn.createColumn(newerColumnName + 3,
                        GeoPackageDataType.BOOLEAN, true, false));

                List<FeatureColumn> alterColumns = new ArrayList<>();
                alterColumns.add(FeatureColumn.createColumn(newerColumnName + 5,
                        GeoPackageDataType.FLOAT, true, 1.5f));
                alterColumns.add(FeatureColumn.createColumn(newerColumnName + 8,
                        GeoPackageDataType.TEXT, true, "date_to_text"));
                alterColumns.add(FeatureColumn.createColumn(newerColumnName + 9,
                        GeoPackageDataType.DATETIME, true,
                        "(strftime('%Y-%m-%dT%H:%M:%fZ','now'))"));
                dao.alterColumns(alterColumns);

                for (int index = existingColumns + 1; index < table.getColumns()
                        .size(); index++) {
                    String name = newerColumnName
                            + (index - existingColumns + 1);
                    TestCase.assertEquals(name, table.getColumnName(index));
                    TestCase.assertEquals(index, table.getColumnIndex(name));
                    TestCase.assertEquals(name,
                            table.getColumn(index).getName());
                    TestCase.assertEquals(index,
                            table.getColumn(index).getIndex());
                    TestCase.assertEquals(name, table.getColumnNames()[index]);
                    TestCase.assertEquals(name,
                            table.getColumns().get(index).getName());
                }

                TestCase.assertEquals(existingColumns + newColumns,
                        table.getColumns().size());
                TestCase.assertEquals(rowCount, dao.count());
                testTableCounts(db, tableName, tableCount,
                        indexCount + newColumns, triggerCount, viewCount);
                TestCase.assertEquals(geometryColumns.getTableName(),
                        table.getTableName());
                TestCase.assertEquals(pk, table.getPkColumn());
                TestCase.assertEquals(geometry, table.getGeometryColumn());

                testIndex(indexManager, indexGeoPackageCount, indexRTreeCount);

                FeatureUtils.testUpdate(dao);

                dao.dropColumn(newColumnName + 1);
                testTableCounts(db, tableName, tableCount,
                        indexCount + newColumns - 1, triggerCount, viewCount);
                dao.dropColumnNames(Arrays.asList(newerColumnName + 2,
                        newerColumnName + 3, newerColumnName + 4));
                for (int newColumn = 5; newColumn <= newColumns; newColumn++) {
                    dao.dropColumn(newerColumnName + newColumn);
                }

                TestCase.assertEquals(existingColumns,
                        table.getColumns().size());
                TestCase.assertEquals(rowCount, dao.count());
                testTableCounts(db, tableName, tableCount, indexCount,
                        triggerCount, viewCount);

                for (int index = 0; index < existingColumns; index++) {
                    TestCase.assertEquals(index,
                            table.getColumn(index).getIndex());
                }

                TestCase.assertEquals(geometryColumns.getTableName(),
                        table.getTableName());
                TestCase.assertEquals(pk, table.getPkColumn());
                TestCase.assertEquals(geometry, table.getGeometryColumn());

                testIndex(indexManager, indexGeoPackageCount, indexRTreeCount);

                FeatureUtils.testUpdate(dao);

                indexManager.close();
            }
        }
    }

    /**
     * Index the column
     *
     * @param db        connection
     * @param tableName table name
     * @param column    feature column
     */
    private static void indexColumn(GeoPackageConnection db, String tableName,
                                    FeatureColumn column) {
        if (!column.isPrimaryKey() && !column.isGeometry()) {
            StringBuilder index = new StringBuilder(
                    "CREATE INDEX IF NOT EXISTS ");
            index.append(CoreSQLUtils
                    .quoteWrap("idx_" + tableName + "_" + column.getName()));
            index.append(" ON ");
            index.append(CoreSQLUtils.quoteWrap(tableName));
            index.append(" ( ");
            String columnName = column.getName();
            if (columnName.contains(" ")) {
                columnName = CoreSQLUtils.quoteWrap(columnName);
            }
            index.append(columnName);
            index.append(" )");

            db.execSQL(index.toString());
        }
    }

    /**
     * Create a table view
     *
     * @param db           connection
     * @param featureTable feature column
     * @param namePrefix   view name prefix
     * @param quoteWrap
     */
    private static void createViewWithPrefix(GeoPackageConnection db,
                                             FeatureTable featureTable, String namePrefix, boolean quoteWrap) {
        String viewName = namePrefix + featureTable.getTableName();
        createViewWithName(db, featureTable, viewName, quoteWrap);
    }

    /**
     * Create a table view
     *
     * @param db           connection
     * @param featureTable feature column
     * @param viewName     view name
     * @param quoteWrap
     */
    private static void createViewWithName(GeoPackageConnection db,
                                           FeatureTable featureTable, String viewName, boolean quoteWrap) {

        StringBuilder view = new StringBuilder("CREATE VIEW ");
        if (quoteWrap) {
            viewName = CoreSQLUtils.quoteWrap(viewName);
        }
        view.append(viewName);
        view.append(" AS SELECT ");
        for (int i = 0; i < featureTable.columnCount(); i++) {
            if (i > 0) {
                view.append(", ");
            }
            view.append(CoreSQLUtils.quoteWrap(featureTable.getColumnName(i)));
            view.append(" AS ");
            String columnName = "column" + (i + 1);
            if (quoteWrap) {
                columnName = CoreSQLUtils.quoteWrap(columnName);
            }
            view.append(columnName);
        }
        view.append(" FROM ");
        String tableName = featureTable.getTableName();
        if (quoteWrap) {
            tableName = CoreSQLUtils.quoteWrap(tableName);
        }
        view.append(tableName);

        db.execSQL(view.toString());
    }

    /**
     * Get the expected index count
     *
     * @param db        connection
     * @param tableName table name
     * @return index count
     */
    private static int indexCount(GeoPackageCoreConnection db,
                                  String tableName) {
        SQLiteMasterQuery indexQuery = SQLiteMasterQuery.createAnd();
        indexQuery.add(SQLiteMasterColumn.TBL_NAME, tableName);
        indexQuery.addIsNotNull(SQLiteMasterColumn.SQL);
        int count = SQLiteMaster.count(db, SQLiteMasterType.INDEX, indexQuery);
        return count;
    }

    /**
     * Test the table schema counts
     *
     * @param db           connection
     * @param tableName    table name
     * @param tableCount   table count
     * @param indexCount   index count
     * @param triggerCount trigger count
     * @param viewCount    view count
     */
    private static void testTableCounts(GeoPackageConnection db,
                                        String tableName, int tableCount, int indexCount, int triggerCount,
                                        int viewCount) {
        TestCase.assertEquals(tableCount,
                SQLiteMaster.count(db, SQLiteMasterType.TABLE, tableName));
        TestCase.assertEquals(indexCount, indexCount(db, tableName));
        TestCase.assertEquals(triggerCount,
                SQLiteMaster.count(db, SQLiteMasterType.TRIGGER, tableName));
        TestCase.assertEquals(viewCount,
                SQLiteMaster.countViewsOnTable(db, tableName));
    }

    /**
     * Test the feature indexes
     *
     * @param indexManager    index manager
     * @param geoPackageCount GeoPackage index count
     * @param rTreeCount      RTree index count
     */
    private static void testIndex(FeatureIndexManager indexManager,
                                  int geoPackageCount, int rTreeCount) {

        TestCase.assertTrue(
                indexManager.isIndexed(FeatureIndexType.GEOPACKAGE));
        indexManager.prioritizeQueryLocation(FeatureIndexType.GEOPACKAGE);
        TestCase.assertEquals(geoPackageCount, indexManager.count());

        //TestCase.assertTrue(indexManager.isIndexed(FeatureIndexType.RTREE));
        if (indexManager.isIndexed(FeatureIndexType.RTREE)) {
            indexManager.prioritizeQueryLocation(FeatureIndexType.RTREE);
            TestCase.assertEquals(rTreeCount, indexManager.count());
        }

    }

    /**
     * Test copy feature table
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testCopyFeatureTable(Context context, GeoPackage geoPackage)
            throws SQLException {

        GeometryColumnsDao geometryColumnsDao = geoPackage
                .getGeometryColumnsDao();

        if (geometryColumnsDao.isTableExists()) {
            List<GeometryColumns> results = geometryColumnsDao.queryForAll();

            int viewNameCount = 0;

            for (GeometryColumns geometryColumns : results) {

                GeoPackageConnection db = geoPackage.getConnection();
                FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
                FeatureTable table = dao.getTable();
                String tableName = table.getTableName();
                String newTableName = tableName + "_copy";

                int existingColumns = table.columnCount();
                FeatureColumn pk = table.getPkColumn();
                FeatureColumn geometry = table.getGeometryColumn();

                FeatureIndexManager indexManager = new FeatureIndexManager(context,
                        geoPackage, dao);
                indexManager.setContinueOnError(false);

                int indexGeoPackageCount = 0;
                if (indexManager.isIndexed(FeatureIndexType.GEOPACKAGE)) {
                    indexManager.prioritizeQueryLocation(
                            FeatureIndexType.GEOPACKAGE);
                    indexGeoPackageCount = (int) indexManager.count();
                }

                int indexRTreeCount = 0;
                if (indexManager.isIndexed(FeatureIndexType.RTREE)) {
                    indexManager
                            .prioritizeQueryLocation(FeatureIndexType.RTREE);
                    indexRTreeCount = (int) indexManager.count();
                }

                FeatureTileTableLinker linker = new FeatureTileTableLinker(
                        geoPackage);
                List<String> tileTables = linker
                        .getTileTablesForFeatureTable(tableName);

                ContentsIdExtension contentsIdExtension = new ContentsIdExtension(
                        geoPackage);
                ContentsId contentsId = contentsIdExtension.get(tableName);

                List<MetadataReference> metadataReference = null;
                MetadataReferenceDao metadataReferenceDao = MetadataExtension
                        .getMetadataReferenceDao(geoPackage);
                if (metadataReferenceDao.isTableExists()) {
                    metadataReference = metadataReferenceDao
                            .queryByTable(tableName);
                }

                List<DataColumns> dataColumns = null;
                DataColumnsDao dataColumnsDao = (new SchemaExtension(
                        geoPackage)).getDataColumnsDao();
                if (dataColumnsDao.isTableExists()) {
                    dataColumns = dataColumnsDao.queryByTable(tableName);
                }

                List<ExtendedRelation> extendedRelations = null;
                RelatedTablesExtension relatedTablesExtension = new RelatedTablesExtension(
                        geoPackage);
                if (relatedTablesExtension.has()) {
                    extendedRelations = relatedTablesExtension
                            .getExtendedRelationsDao()
                            .getBaseTableRelations(tableName);
                }

                FeatureTableStyles featureTableStyles = new FeatureTableStyles(
                        geoPackage, table);
                boolean featureStyle = featureTableStyles.has();
                List<Long> styleIds = null;
                List<Long> iconIds = null;
                List<Long> tableStyleIds = null;
                List<Long> tableIconIds = null;
                if (featureStyle) {
                    styleIds = featureTableStyles.getAllStyleIds();
                    iconIds = featureTableStyles.getAllIconIds();
                    tableStyleIds = featureTableStyles.getAllTableStyleIds();
                    tableIconIds = featureTableStyles.getAllTableIconIds();
                }

                String viewName = "v_my_" + (++viewNameCount) + "_view";
                createViewWithName(db, table, viewName, true);
                createViewWithName(db, table, viewName + "_2", false);

                int rowCount = dao.count();
                int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
                        SQLiteMasterType.TABLE, tableName);
                int indexCount = indexCount(geoPackage.getDatabase(),
                        tableName);
                int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
                        SQLiteMasterType.TRIGGER, tableName);
                int viewCount = SQLiteMaster
                        .countViewsOnTable(geoPackage.getDatabase(), tableName);

                geoPackage.copyTable(tableName, newTableName);

                FeatureUtils.testUpdate(dao);

                FeatureDao copyDao = geoPackage.getFeatureDao(newTableName);
                GeometryColumns copyGeometryColumns = copyDao
                        .getGeometryColumns();

                FeatureUtils.testUpdate(copyDao);

                FeatureTable copyTable = copyDao.getTable();

                TestCase.assertEquals(existingColumns, table.columnCount());
                TestCase.assertEquals(existingColumns, copyTable.columnCount());
                TestCase.assertEquals(rowCount, dao.count());
                TestCase.assertEquals(rowCount, copyDao.count());
                testTableCounts(db, tableName, tableCount, indexCount,
                        triggerCount, viewCount);
                testTableCounts(db, newTableName, tableCount, indexCount,
                        triggerCount, viewCount);

                TestCase.assertEquals(geometryColumns.getTableName(),
                        table.getTableName());
                TestCase.assertEquals(newTableName,
                        copyGeometryColumns.getTableName());
                TestCase.assertEquals(newTableName, copyTable.getTableName());
                TestCase.assertEquals(pk, table.getPkColumn());
                TestCase.assertEquals(pk.getName(),
                        copyTable.getPkColumn().getName());
                TestCase.assertEquals(pk.getIndex(),
                        copyTable.getPkColumn().getIndex());
                TestCase.assertEquals(geometry, table.getGeometryColumn());
                TestCase.assertEquals(geometry.getName(),
                        copyTable.getGeometryColumnName());
                TestCase.assertEquals(geometry.getIndex(),
                        copyTable.getGeometryColumn().getIndex());

                FeatureIndexManager copyIndexManager = new FeatureIndexManager(context,
                        geoPackage, copyDao);
                copyIndexManager.setContinueOnError(false);

                if (indexManager.isIndexed(FeatureIndexType.GEOPACKAGE)) {
                    indexManager.prioritizeQueryLocation(
                            FeatureIndexType.GEOPACKAGE);
                    TestCase.assertEquals(indexGeoPackageCount,
                            indexManager.count());
                    TestCase.assertTrue(copyIndexManager
                            .isIndexed(FeatureIndexType.GEOPACKAGE));
                    copyIndexManager.prioritizeQueryLocation(
                            FeatureIndexType.GEOPACKAGE);
                    TestCase.assertEquals(indexGeoPackageCount,
                            copyIndexManager.count());
                } else {
                    TestCase.assertFalse(copyIndexManager
                            .isIndexed(FeatureIndexType.GEOPACKAGE));
                }

                if (indexManager.isIndexed(FeatureIndexType.RTREE)) {
                    indexManager
                            .prioritizeQueryLocation(FeatureIndexType.RTREE);
                    TestCase.assertEquals(indexRTreeCount,
                            indexManager.count());
                    //TestCase.assertTrue(
                    //        copyIndexManager.isIndexed(FeatureIndexType.RTREE));
                    //copyIndexManager
                    //        .prioritizeQueryLocation(FeatureIndexType.RTREE);
                    //TestCase.assertEquals(indexRTreeCount,
                    //        copyIndexManager.count());
                } else {
                    TestCase.assertFalse(
                            copyIndexManager.isIndexed(FeatureIndexType.RTREE));
                }

                List<String> copyTileTables = linker
                        .getTileTablesForFeatureTable(newTableName);
                TestCase.assertEquals(tileTables.size(), copyTileTables.size());
                for (String tileTable : tileTables) {
                    TestCase.assertTrue(copyTileTables.contains(tileTable));
                }

                ContentsId copyContentsId = contentsIdExtension
                        .get(newTableName);
                if (contentsId != null) {
                    TestCase.assertNotNull(copyContentsId);
                    TestCase.assertEquals(newTableName,
                            copyContentsId.getTableName());
                    TestCase.assertTrue(copyContentsId.getId() >= 0);
                    TestCase.assertTrue(
                            copyContentsId.getId() > contentsId.getId());
                } else {
                    TestCase.assertNull(copyContentsId);
                }

                if (metadataReference != null) {
                    List<MetadataReference> copyMetadataReference = metadataReferenceDao
                            .queryByTable(newTableName);
                    TestCase.assertEquals(metadataReference.size(),
                            copyMetadataReference.size());
                    for (int i = 0; i < metadataReference.size(); i++) {
                        TestCase.assertEquals(tableName,
                                metadataReference.get(i).getTableName());
                        TestCase.assertEquals(newTableName,
                                copyMetadataReference.get(i).getTableName());
                    }
                }

                if (dataColumns != null) {
                    List<DataColumns> copyDataColumns = dataColumnsDao
                            .queryByTable(newTableName);
                    TestCase.assertEquals(dataColumns.size(),
                            copyDataColumns.size());
                    for (int i = 0; i < dataColumns.size(); i++) {
                        TestCase.assertEquals(tableName,
                                dataColumns.get(i).getTableName());
                        TestCase.assertEquals(newTableName,
                                copyDataColumns.get(i).getTableName());
                    }
                }

                if (extendedRelations != null) {
                    List<ExtendedRelation> copyExtendedRelations = relatedTablesExtension
                            .getExtendedRelationsDao()
                            .getBaseTableRelations(newTableName);
                    TestCase.assertEquals(extendedRelations.size(),
                            copyExtendedRelations.size());
                    Map<String, ExtendedRelation> mappingTableToRelations = new HashMap<>();
                    for (ExtendedRelation copyExtendedRelation : copyExtendedRelations) {
                        mappingTableToRelations.put(
                                copyExtendedRelation.getMappingTableName(),
                                copyExtendedRelation);
                    }
                    for (ExtendedRelation extendedRelation : extendedRelations) {
                        String mappingTableName = extendedRelation
                                .getMappingTableName();
                        String copyMappingTableName = CoreSQLUtils.createName(
                                geoPackage.getDatabase(), mappingTableName, tableName, newTableName);
                        ExtendedRelation copyExtendedRelation = mappingTableToRelations
                                .get(copyMappingTableName);
                        TestCase.assertNotNull(copyExtendedRelation);
                        TestCase.assertTrue(extendedRelation
                                .getId() < copyExtendedRelation.getId());
                        TestCase.assertEquals(tableName,
                                extendedRelation.getBaseTableName());
                        TestCase.assertEquals(newTableName,
                                copyExtendedRelation.getBaseTableName());
                        TestCase.assertEquals(
                                extendedRelation.getBasePrimaryColumn(),
                                copyExtendedRelation.getBasePrimaryColumn());
                        TestCase.assertEquals(
                                extendedRelation.getRelatedTableName(),
                                copyExtendedRelation.getRelatedTableName());
                        TestCase.assertEquals(
                                extendedRelation.getRelatedPrimaryColumn(),
                                copyExtendedRelation.getRelatedPrimaryColumn());
                        TestCase.assertEquals(
                                extendedRelation.getRelationName(),
                                copyExtendedRelation.getRelationName());
                        TestCase.assertTrue(
                                geoPackage.isTable(mappingTableName));
                        TestCase.assertTrue(
                                geoPackage.isTable(copyMappingTableName));
                        int mappingTableCount = geoPackage.getConnection()
                                .count(mappingTableName);
                        int copyMappingTableCount = geoPackage.getConnection()
                                .count(copyMappingTableName);
                        TestCase.assertEquals(mappingTableCount,
                                copyMappingTableCount);
                    }
                }

                FeatureTableStyles copyFeatureTableStyles = new FeatureTableStyles(
                        geoPackage, copyTable);
                TestCase.assertEquals(featureStyle,
                        copyFeatureTableStyles.has());
                if (featureStyle) {
                    compareIds(styleIds,
                            copyFeatureTableStyles.getAllStyleIds());
                    compareIds(iconIds, copyFeatureTableStyles.getAllIconIds());
                    compareIds(tableStyleIds,
                            copyFeatureTableStyles.getAllTableStyleIds());
                    compareIds(tableIconIds,
                            copyFeatureTableStyles.getAllTableIconIds());
                    if (featureTableStyles.hasStyleRelationship()) {
                        StyleMappingDao styleMappingDao = featureTableStyles
                                .getStyleMappingDao();
                        StyleMappingDao copyStyleMappingDao = copyFeatureTableStyles
                                .getStyleMappingDao();
                        TestCase.assertEquals(styleMappingDao.count(),
                                copyStyleMappingDao.count());
                    }
                    if (featureTableStyles.hasIconRelationship()) {
                        StyleMappingDao iconMappingDao = featureTableStyles
                                .getIconMappingDao();
                        StyleMappingDao copyIconMappingDao = copyFeatureTableStyles
                                .getIconMappingDao();
                        TestCase.assertEquals(iconMappingDao.count(),
                                copyIconMappingDao.count());
                    }
                    if (featureTableStyles.hasTableStyleRelationship()) {
                        StyleMappingDao tableStyleMappingDao = featureTableStyles
                                .getTableStyleMappingDao();
                        StyleMappingDao copyTableStyleMappingDao = copyFeatureTableStyles
                                .getTableStyleMappingDao();
                        TestCase.assertEquals(tableStyleMappingDao.count(),
                                copyTableStyleMappingDao.count());
                    }
                    if (featureTableStyles.hasTableIconRelationship()) {
                        StyleMappingDao tableIconMappingDao = featureTableStyles
                                .getTableIconMappingDao();
                        StyleMappingDao copyTableIconMappingDao = copyFeatureTableStyles
                                .getTableIconMappingDao();
                        TestCase.assertEquals(tableIconMappingDao.count(),
                                copyTableIconMappingDao.count());
                    }
                }

                indexManager.close();
                copyIndexManager.close();

                String newTableName2 = tableName + "_copy2";
                geoPackage.copyTableAsEmpty(tableName, newTableName2);
                FeatureDao copyDao2 = geoPackage.getFeatureDao(newTableName2);
                TestCase.assertEquals(0, copyDao2.count());

            }
        }
    }

    /**
     * Compare two lists of ids
     *
     * @param ids  ids
     * @param ids2 ids 2
     */
    private static void compareIds(List<Long> ids, List<Long> ids2) {
        if (ids == null) {
            TestCase.assertNull(ids2);
        } else {
            TestCase.assertNotNull(ids2);
            TestCase.assertEquals(ids.size(), ids2.size());
            for (long id : ids) {
                TestCase.assertTrue(ids2.contains(id));
            }
        }
    }

    /**
     * Test copy tile table
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    public static void testCopyTileTable(Context context, GeoPackage geoPackage)
            throws SQLException, IOException {

        TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

        if (tileMatrixSetDao.isTableExists()) {
            List<TileMatrixSet> results = tileMatrixSetDao.queryForAll();

            for (TileMatrixSet tileMatrixSet : results) {

                GeoPackageConnection db = geoPackage.getConnection();
                TileDao dao = geoPackage.getTileDao(tileMatrixSet);
                TileTable table = dao.getTable();
                String tableName = table.getTableName();
                String newTableName = tableName + "_copy";

                int existingColumns = table.columnCount();

                FeatureTileTableLinker linker = new FeatureTileTableLinker(
                        geoPackage);
                List<String> featureTables = linker
                        .getFeatureTablesForTileTable(tableName);

                ContentsIdExtension contentsIdExtension = new ContentsIdExtension(
                        geoPackage);
                ContentsId contentsId = contentsIdExtension.get(tableName);

                List<MetadataReference> metadataReference = null;
                MetadataReferenceDao metadataReferenceDao = MetadataExtension
                        .getMetadataReferenceDao(geoPackage);
                if (metadataReferenceDao.isTableExists()) {
                    metadataReference = metadataReferenceDao
                            .queryByTable(tableName);
                }

                List<DataColumns> dataColumns = null;
                DataColumnsDao dataColumnsDao = (new SchemaExtension(
                        geoPackage)).getDataColumnsDao();
                if (dataColumnsDao.isTableExists()) {
                    dataColumns = dataColumnsDao.queryByTable(tableName);
                }

                List<ExtendedRelation> extendedRelations = null;
                RelatedTablesExtension relatedTablesExtension = new RelatedTablesExtension(
                        geoPackage);
                if (relatedTablesExtension.has()) {
                    extendedRelations = relatedTablesExtension
                            .getExtendedRelationsDao()
                            .getBaseTableRelations(tableName);
                }

                TileScaling tileScaling = null;
                TileTableScaling tileTableScaling = new TileTableScaling(
                        geoPackage, tileMatrixSet);
                if (tileTableScaling.has()) {
                    tileScaling = tileTableScaling.get();
                }

                GriddedCoverage griddedCoverage = null;
                List<GriddedTile> griddedTiles = null;
                if (geoPackage.isTableType(tableName, CoverageData.GRIDDED_COVERAGE)) {
                    CoverageData<?> coverageData = CoverageData
                            .getCoverageData(geoPackage, dao);
                    griddedCoverage = coverageData.queryGriddedCoverage();
                    griddedTiles = coverageData.getGriddedTile();
                }

                int rowCount = dao.count();
                int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
                        SQLiteMasterType.TABLE, tableName);
                int indexCount = indexCount(geoPackage.getDatabase(),
                        tableName);
                int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
                        SQLiteMasterType.TRIGGER, tableName);
                int viewCount = SQLiteMaster
                        .countViewsOnTable(geoPackage.getDatabase(), tableName);

                geoPackage.copyTable(tableName, newTableName);

                TileUtils.testUpdate(context, dao);

                TileDao copyDao = geoPackage.getTileDao(newTableName);
                TileMatrixSet copyTileMatrixSet = copyDao.getTileMatrixSet();

                TileUtils.testUpdate(context, copyDao);

                TileTable copyTable = copyDao.getTable();

                TestCase.assertEquals(existingColumns, table.columnCount());
                TestCase.assertEquals(existingColumns, copyTable.columnCount());
                TestCase.assertEquals(rowCount, dao.count());
                TestCase.assertEquals(rowCount, copyDao.count());
                testTableCounts(db, tableName, tableCount, indexCount,
                        triggerCount, viewCount);
                testTableCounts(db, newTableName, tableCount, indexCount,
                        triggerCount, viewCount);

                TestCase.assertEquals(tileMatrixSet.getTableName(),
                        table.getTableName());
                TestCase.assertEquals(newTableName,
                        copyTileMatrixSet.getTableName());
                TestCase.assertEquals(newTableName, copyTable.getTableName());

                List<String> copyFeatureTables = linker
                        .getFeatureTablesForTileTable(newTableName);
                TestCase.assertEquals(featureTables.size(),
                        copyFeatureTables.size());
                for (String featureTable : featureTables) {
                    TestCase.assertTrue(
                            copyFeatureTables.contains(featureTable));
                }

                ContentsId copyContentsId = contentsIdExtension
                        .get(newTableName);
                if (contentsId != null) {
                    TestCase.assertNotNull(copyContentsId);
                    TestCase.assertEquals(newTableName,
                            copyContentsId.getTableName());
                    TestCase.assertTrue(copyContentsId.getId() >= 0);
                    TestCase.assertTrue(
                            copyContentsId.getId() > contentsId.getId());
                } else {
                    TestCase.assertNull(copyContentsId);
                }

                if (metadataReference != null) {
                    List<MetadataReference> copyMetadataReference = metadataReferenceDao
                            .queryByTable(newTableName);
                    TestCase.assertEquals(metadataReference.size(),
                            copyMetadataReference.size());
                    for (int i = 0; i < metadataReference.size(); i++) {
                        TestCase.assertEquals(tableName,
                                metadataReference.get(i).getTableName());
                        TestCase.assertEquals(newTableName,
                                copyMetadataReference.get(i).getTableName());
                    }
                }

                if (dataColumns != null) {
                    List<DataColumns> copyDataColumns = dataColumnsDao
                            .queryByTable(newTableName);
                    TestCase.assertEquals(dataColumns.size(),
                            copyDataColumns.size());
                    for (int i = 0; i < dataColumns.size(); i++) {
                        TestCase.assertEquals(tableName,
                                dataColumns.get(i).getTableName());
                        TestCase.assertEquals(newTableName,
                                copyDataColumns.get(i).getTableName());
                    }
                }

                if (extendedRelations != null) {
                    List<ExtendedRelation> copyExtendedRelations = relatedTablesExtension
                            .getExtendedRelationsDao()
                            .getBaseTableRelations(newTableName);
                    TestCase.assertEquals(extendedRelations.size(),
                            copyExtendedRelations.size());
                    Map<String, ExtendedRelation> mappingTableToRelations = new HashMap<>();
                    for (ExtendedRelation copyExtendedRelation : copyExtendedRelations) {
                        mappingTableToRelations.put(
                                copyExtendedRelation.getMappingTableName(),
                                copyExtendedRelation);
                    }
                    for (ExtendedRelation extendedRelation : extendedRelations) {
                        String mappingTableName = extendedRelation
                                .getMappingTableName();
                        String copyMappingTableName = CoreSQLUtils.createName(
                                geoPackage.getDatabase(), mappingTableName, tableName, newTableName);
                        ExtendedRelation copyExtendedRelation = mappingTableToRelations
                                .get(copyMappingTableName);
                        TestCase.assertNotNull(copyExtendedRelation);
                        TestCase.assertTrue(extendedRelation
                                .getId() < copyExtendedRelation.getId());
                        TestCase.assertEquals(tableName,
                                extendedRelation.getBaseTableName());
                        TestCase.assertEquals(newTableName,
                                copyExtendedRelation.getBaseTableName());
                        TestCase.assertEquals(
                                extendedRelation.getBasePrimaryColumn(),
                                copyExtendedRelation.getBasePrimaryColumn());
                        TestCase.assertEquals(
                                extendedRelation.getRelatedTableName(),
                                copyExtendedRelation.getRelatedTableName());
                        TestCase.assertEquals(
                                extendedRelation.getRelatedPrimaryColumn(),
                                copyExtendedRelation.getRelatedPrimaryColumn());
                        TestCase.assertEquals(
                                extendedRelation.getRelationName(),
                                copyExtendedRelation.getRelationName());
                        TestCase.assertTrue(
                                geoPackage.isTable(mappingTableName));
                        TestCase.assertTrue(
                                geoPackage.isTable(copyMappingTableName));
                        int mappingTableCount = geoPackage.getConnection()
                                .count(mappingTableName);
                        int copyMappingTableCount = geoPackage.getConnection()
                                .count(copyMappingTableName);
                        TestCase.assertEquals(mappingTableCount,
                                copyMappingTableCount);
                    }
                }

                if (tileScaling != null) {
                    TileTableScaling copyTileTableScaling = new TileTableScaling(
                            geoPackage, copyTileMatrixSet);
                    TestCase.assertTrue(copyTileTableScaling.has());
                    TileScaling copyTileScaling = copyTileTableScaling.get();
                    TestCase.assertEquals(newTableName,
                            copyTileScaling.getTableName());
                    TestCase.assertEquals(tileScaling.getScalingTypeString(),
                            copyTileScaling.getScalingTypeString());
                    TestCase.assertEquals(tileScaling.getZoomIn(),
                            copyTileScaling.getZoomIn());
                    TestCase.assertEquals(tileScaling.getZoomOut(),
                            copyTileScaling.getZoomOut());
                }

                if (griddedCoverage != null) {
                    CoverageData<?> copyCoverageData = CoverageData
                            .getCoverageData(geoPackage, copyDao);
                    GriddedCoverage copyGriddedCoverage = copyCoverageData
                            .queryGriddedCoverage();
                    List<GriddedTile> copyGriddedTiles = copyCoverageData
                            .getGriddedTile();
                    TestCase.assertEquals(tableName,
                            griddedCoverage.getTileMatrixSetName());
                    TestCase.assertEquals(newTableName,
                            copyGriddedCoverage.getTileMatrixSetName());
                    TestCase.assertEquals(griddedTiles.size(),
                            copyGriddedTiles.size());
                    for (int i = 0; i < griddedTiles.size(); i++) {
                        TestCase.assertEquals(tableName,
                                griddedTiles.get(i).getTableName());
                        TestCase.assertEquals(newTableName,
                                copyGriddedTiles.get(i).getTableName());
                    }
                }

                String newTableName2 = tableName + "_copy2";
                geoPackage.copyTableAsEmpty(tableName, newTableName2);
                TileDao copyDao2 = geoPackage.getTileDao(newTableName2);
                TestCase.assertEquals(0, copyDao2.count());

            }
        }
    }

    /**
     * Test copy attributes table
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testCopyAttributesTable(Context context, GeoPackage geoPackage)
            throws SQLException {

        List<String> attributesTables = geoPackage.getAttributesTables();

        for (String attributesTable : attributesTables) {

            GeoPackageConnection db = geoPackage.getConnection();
            AttributesDao dao = geoPackage.getAttributesDao(attributesTable);
            AttributesTable table = dao.getTable();
            String tableName = table.getTableName();
            String newTableName = tableName + "_copy";

            int existingColumns = table.columnCount();

            ContentsIdExtension contentsIdExtension = new ContentsIdExtension(
                    geoPackage);
            ContentsId contentsId = contentsIdExtension.get(tableName);

            List<MetadataReference> metadataReference = null;
            MetadataReferenceDao metadataReferenceDao = MetadataExtension
                    .getMetadataReferenceDao(geoPackage);
            if (metadataReferenceDao.isTableExists()) {
                metadataReference = metadataReferenceDao
                        .queryByTable(tableName);
            }

            List<DataColumns> dataColumns = null;
            DataColumnsDao dataColumnsDao = (new SchemaExtension(geoPackage))
                    .getDataColumnsDao();
            if (dataColumnsDao.isTableExists()) {
                dataColumns = dataColumnsDao.queryByTable(tableName);
            }

            List<ExtendedRelation> extendedRelations = null;
            RelatedTablesExtension relatedTablesExtension = new RelatedTablesExtension(
                    geoPackage);
            if (relatedTablesExtension.has()) {
                extendedRelations = relatedTablesExtension
                        .getExtendedRelationsDao()
                        .getBaseTableRelations(tableName);
            }

            int rowCount = dao.count();
            int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
                    SQLiteMasterType.TABLE, tableName);
            int indexCount = indexCount(geoPackage.getDatabase(), tableName);
            int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
                    SQLiteMasterType.TRIGGER, tableName);
            int viewCount = SQLiteMaster
                    .countViewsOnTable(geoPackage.getDatabase(), tableName);

            geoPackage.copyTable(tableName, newTableName);

            AttributesUtils.testUpdate(dao);

            AttributesDao copyDao = geoPackage.getAttributesDao(newTableName);

            AttributesUtils.testUpdate(copyDao);

            AttributesTable copyTable = copyDao.getTable();

            TestCase.assertEquals(existingColumns, table.columnCount());
            TestCase.assertEquals(existingColumns, copyTable.columnCount());
            TestCase.assertEquals(rowCount, dao.count());
            TestCase.assertEquals(rowCount, copyDao.count());
            testTableCounts(db, tableName, tableCount, indexCount, triggerCount,
                    viewCount);
            testTableCounts(db, newTableName, tableCount, indexCount,
                    triggerCount, viewCount);

            TestCase.assertEquals(newTableName, copyTable.getTableName());

            ContentsId copyContentsId = contentsIdExtension.get(newTableName);
            if (contentsId != null) {
                TestCase.assertNotNull(copyContentsId);
                TestCase.assertEquals(newTableName,
                        copyContentsId.getTableName());
                TestCase.assertTrue(copyContentsId.getId() >= 0);
                TestCase.assertTrue(
                        copyContentsId.getId() > contentsId.getId());
            } else {
                TestCase.assertNull(copyContentsId);
            }

            if (metadataReference != null) {
                List<MetadataReference> copyMetadataReference = metadataReferenceDao
                        .queryByTable(newTableName);
                TestCase.assertEquals(metadataReference.size(),
                        copyMetadataReference.size());
                for (int i = 0; i < metadataReference.size(); i++) {
                    TestCase.assertEquals(tableName,
                            metadataReference.get(i).getTableName());
                    TestCase.assertEquals(newTableName,
                            copyMetadataReference.get(i).getTableName());
                }
            }

            if (dataColumns != null) {
                List<DataColumns> copyDataColumns = dataColumnsDao
                        .queryByTable(newTableName);
                TestCase.assertEquals(dataColumns.size(),
                        copyDataColumns.size());
                for (int i = 0; i < dataColumns.size(); i++) {
                    TestCase.assertEquals(tableName,
                            dataColumns.get(i).getTableName());
                    TestCase.assertEquals(newTableName,
                            copyDataColumns.get(i).getTableName());
                }
            }

            if (extendedRelations != null) {
                List<ExtendedRelation> copyExtendedRelations = relatedTablesExtension
                        .getExtendedRelationsDao()
                        .getBaseTableRelations(newTableName);
                TestCase.assertEquals(extendedRelations.size(),
                        copyExtendedRelations.size());
                Map<String, ExtendedRelation> mappingTableToRelations = new HashMap<>();
                for (ExtendedRelation copyExtendedRelation : copyExtendedRelations) {
                    mappingTableToRelations.put(
                            copyExtendedRelation.getMappingTableName(),
                            copyExtendedRelation);
                }
                for (ExtendedRelation extendedRelation : extendedRelations) {
                    String mappingTableName = extendedRelation
                            .getMappingTableName();
                    String copyMappingTableName = CoreSQLUtils.createName(
                            geoPackage.getDatabase(), mappingTableName, tableName, newTableName);
                    ExtendedRelation copyExtendedRelation = mappingTableToRelations
                            .get(copyMappingTableName);
                    TestCase.assertNotNull(copyExtendedRelation);
                    TestCase.assertTrue(extendedRelation
                            .getId() < copyExtendedRelation.getId());
                    TestCase.assertEquals(tableName,
                            extendedRelation.getBaseTableName());
                    TestCase.assertEquals(newTableName,
                            copyExtendedRelation.getBaseTableName());
                    TestCase.assertEquals(
                            extendedRelation.getBasePrimaryColumn(),
                            copyExtendedRelation.getBasePrimaryColumn());
                    TestCase.assertEquals(
                            extendedRelation.getRelatedTableName(),
                            copyExtendedRelation.getRelatedTableName());
                    TestCase.assertEquals(
                            extendedRelation.getRelatedPrimaryColumn(),
                            copyExtendedRelation.getRelatedPrimaryColumn());
                    TestCase.assertEquals(extendedRelation.getRelationName(),
                            copyExtendedRelation.getRelationName());
                    TestCase.assertTrue(geoPackage.isTable(mappingTableName));
                    TestCase.assertTrue(
                            geoPackage.isTable(copyMappingTableName));
                    int mappingTableCount = geoPackage.getConnection()
                            .count(mappingTableName);
                    int copyMappingTableCount = geoPackage.getConnection()
                            .count(copyMappingTableName);
                    TestCase.assertEquals(mappingTableCount,
                            copyMappingTableCount);
                }
            }

            String newTableName2 = tableName + "_copy2";
            geoPackage.copyTableAsEmpty(tableName, newTableName2);
            AttributesDao copyDao2 = geoPackage.getAttributesDao(newTableName2);
            TestCase.assertEquals(0, copyDao2.count());

        }
    }

    /**
     * Test copy user table
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testCopyUserTable(Context context, GeoPackage geoPackage)
            throws SQLException {

        String tableName = "user_test_table";
        String columnName = "column";
        int countCount = 0;
        int rowCount = 100;
        String copyTableName = "user_test_copy";
        String copyTableName2 = "user_test_another_copy";

        List<UserCustomColumn> columns = new ArrayList<>();
        columns.add(UserCustomColumn
                .createPrimaryKeyColumn(columnName + ++countCount));
        UserCustomColumn column2 = UserCustomColumn.createColumn(
                columnName + ++countCount, GeoPackageDataType.TEXT, true);
        column2.addUniqueConstraint();
        columns.add(column2);
        columns.add(UserCustomColumn.createColumn(columnName + ++countCount,
                GeoPackageDataType.TEXT, true, "default_value"));
        columns.add(UserCustomColumn.createColumn(columnName + ++countCount,
                GeoPackageDataType.BOOLEAN));
        columns.add(UserCustomColumn.createColumn(columnName + ++countCount,
                GeoPackageDataType.DOUBLE));
        UserCustomColumn column6 = UserCustomColumn.createColumn(
                columnName + ++countCount, GeoPackageDataType.INTEGER, true);
        column6.addConstraint("CONSTRAINT check_constraint CHECK ("
                + (columnName + countCount) + " >= 0)");
        columns.add(column6);
        UserCustomColumn column7 = UserCustomColumn.createColumn(
                columnName + ++countCount, GeoPackageDataType.INTEGER);
        column7.addConstraint("CONSTRAINT another_check_constraint_13 CHECK ("
                + (columnName + countCount) + " >= 0)");
        columns.add(column7);

        UserCustomTable table = new UserCustomTable(tableName, columns);

        table.addConstraint(new UniqueConstraint(tableName + "_unique",
                columns.get(1), columns.get(2)));
        table.addConstraint(
                new UniqueConstraint(columns.get(1), columns.get(2)));
        table.addConstraint(new RawConstraint("CHECK (column5 < 1.0)"));
        table.addConstraint(new RawConstraint("CONSTRAINT fk_" + tableName
                + " FOREIGN KEY (column6) REFERENCES gpkg_spatial_ref_sys(srs_id)"));

        geoPackage.createUserTable(table);

        long srsId = geoPackage.getSpatialReferenceSystemDao()
                .getOrCreateFromEpsg(
                        ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getId();

        UserCustomDao dao = geoPackage.getUserCustomDao(tableName);
        for (int i = 0; i < rowCount; i++) {
            UserCustomRow row = dao.newRow();
            row.setValue(columnName + 2, UUID.randomUUID().toString());
            row.setValue(columnName + 3, UUID.randomUUID().toString());
            row.setValue(columnName + 4, Math.random() < .5);
            row.setValue(columnName + 5, Math.random());
            row.setValue(columnName + 6, srsId);
            dao.create(row);
        }

        TestCase.assertEquals(rowCount, dao.count());

        int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
                SQLiteMasterType.TABLE, tableName);
        int indexCount = indexCount(geoPackage.getDatabase(), tableName);
        int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
                SQLiteMasterType.TRIGGER, tableName);
        int viewCount = SQLiteMaster.countViewsOnTable(geoPackage.getDatabase(),
                tableName);

        geoPackage.copyTable(tableName, copyTableName);

        UserCustomDao copyDao = geoPackage.getUserCustomDao(copyTableName);
        UserCustomTable copyTable = copyDao.getTable();

        TestCase.assertEquals(columns.size(), table.columnCount());
        TestCase.assertEquals(columns.size(), copyTable.columnCount());
        TestCase.assertEquals(rowCount, dao.count());
        TestCase.assertEquals(rowCount, copyDao.count());
        testTableCounts(geoPackage.getConnection(), tableName, tableCount,
                indexCount, triggerCount, viewCount);
        testTableCounts(geoPackage.getConnection(), copyTableName, tableCount,
                indexCount, triggerCount, viewCount);
        TestCase.assertEquals(copyTableName, copyTable.getTableName());

        List<Constraint> copyConstraints = copyTable.getConstraints();
        TestCase.assertEquals(4, copyConstraints.size());
        TestCase.assertEquals(copyTableName + "_unique",
                copyConstraints.get(0).getName());
        TestCase.assertEquals(ConstraintType.UNIQUE,
                copyConstraints.get(0).getType());
        TestCase.assertEquals(
                "CONSTRAINT \"" + copyTableName
                        + "_unique\" UNIQUE (column2, column3)",
                copyConstraints.get(0).buildSql());
        TestCase.assertNull(copyConstraints.get(1).getName());
        TestCase.assertEquals(ConstraintType.UNIQUE,
                copyConstraints.get(1).getType());
        TestCase.assertEquals(table.getConstraints().get(1).buildSql(),
                copyConstraints.get(1).buildSql());
        TestCase.assertNull(copyConstraints.get(2).getName());
        TestCase.assertEquals(ConstraintType.CHECK,
                copyConstraints.get(2).getType());
        TestCase.assertEquals(table.getConstraints().get(2).buildSql(),
                copyConstraints.get(2).buildSql());
        TestCase.assertEquals("fk_" + copyTableName,
                copyConstraints.get(3).getName());
        TestCase.assertEquals(ConstraintType.FOREIGN_KEY,
                copyConstraints.get(3).getType());
        TestCase.assertEquals("CONSTRAINT fk_" + copyTableName
                        + " FOREIGN KEY (column6) REFERENCES gpkg_spatial_ref_sys(srs_id)",
                copyConstraints.get(3).buildSql());

        TestCase.assertEquals("NOT NULL",
                copyTable.getColumn(0).getConstraints().get(0).buildSql());
        TestCase.assertEquals("PRIMARY KEY AUTOINCREMENT",
                copyTable.getColumn(0).getConstraints().get(1).buildSql());
        TestCase.assertEquals("NOT NULL",
                copyTable.getColumn(1).getConstraints().get(0).buildSql());
        TestCase.assertEquals("UNIQUE",
                copyTable.getColumn(1).getConstraints().get(1).buildSql());
        TestCase.assertEquals("NOT NULL",
                copyTable.getColumn(2).getConstraints().get(0).buildSql());
        TestCase.assertEquals("DEFAULT 'default_value'",
                copyTable.getColumn(2).getConstraints().get(1).buildSql());
        TestCase.assertEquals("NOT NULL",
                copyTable.getColumn(5).getConstraints().get(0).buildSql());
        TestCase.assertEquals(
                "CONSTRAINT check_constraint_2 CHECK (" + column6.getName()
                        + " >= 0)",
                copyTable.getColumn(5).getConstraints().get(1).buildSql());
        TestCase.assertEquals("check_constraint_2",
                copyTable.getColumn(5).getConstraints().get(1).getName());
        TestCase.assertEquals(
                "CONSTRAINT another_check_constraint_14 CHECK ("
                        + column7.getName() + " >= 0)",
                copyTable.getColumn(6).getConstraints().get(0).buildSql());
        TestCase.assertEquals("another_check_constraint_14",
                copyTable.getColumn(6).getConstraints().get(0).getName());

        geoPackage.copyTableAsEmpty(tableName, copyTableName2);

        UserCustomDao copyDao2 = geoPackage.getUserCustomDao(copyTableName2);
        UserCustomTable copyTable2 = copyDao2.getTable();

        TestCase.assertEquals(columns.size(), table.columnCount());
        TestCase.assertEquals(columns.size(), copyTable2.columnCount());
        TestCase.assertEquals(rowCount, dao.count());
        TestCase.assertEquals(0, copyDao2.count());
        testTableCounts(geoPackage.getConnection(), tableName, tableCount,
                indexCount, triggerCount, viewCount);
        testTableCounts(geoPackage.getConnection(), copyTableName2, tableCount,
                indexCount, triggerCount, viewCount);
        TestCase.assertEquals(copyTableName2, copyTable2.getTableName());

        List<Constraint> copyConstraints2 = copyTable2.getConstraints();
        TestCase.assertEquals(copyConstraints.size(), copyConstraints2.size());
        TestCase.assertEquals(copyTableName2 + "_unique",
                copyConstraints2.get(0).getName());
        TestCase.assertEquals(ConstraintType.UNIQUE,
                copyConstraints2.get(0).getType());
        TestCase.assertEquals(
                "CONSTRAINT \"" + copyTableName2
                        + "_unique\" UNIQUE (column2, column3)",
                copyConstraints2.get(0).buildSql());
        TestCase.assertNull(copyConstraints2.get(1).getName());
        TestCase.assertEquals(ConstraintType.UNIQUE,
                copyConstraints2.get(1).getType());
        TestCase.assertEquals(table.getConstraints().get(1).buildSql(),
                copyConstraints2.get(1).buildSql());
        TestCase.assertNull(copyConstraints2.get(2).getName());
        TestCase.assertEquals(ConstraintType.CHECK,
                copyConstraints2.get(2).getType());
        TestCase.assertEquals(table.getConstraints().get(2).buildSql(),
                copyConstraints2.get(2).buildSql());
        TestCase.assertEquals("fk_" + copyTableName2,
                copyConstraints2.get(3).getName());
        TestCase.assertEquals(ConstraintType.FOREIGN_KEY,
                copyConstraints2.get(3).getType());
        TestCase.assertEquals("CONSTRAINT fk_" + copyTableName2
                        + " FOREIGN KEY (column6) REFERENCES gpkg_spatial_ref_sys(srs_id)",
                copyConstraints2.get(3).buildSql());

    }

}
