package mil.nga.geopackage.test;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.schema.TableColumnKey;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.sf.GeometryType;

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

		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setId(new TableColumnKey("feature_metadata", "geom"));
		geometryColumns.setGeometryType(GeometryType.POINT);
		geometryColumns.setZ((byte) 1);
		geometryColumns.setM((byte) 0);

		BoundingBox boundingBox = new BoundingBox(-90, -45, 90, 45);

		SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
				.createWebMercator();
		geometryColumns = geoPackage.createFeatureTableWithMetadata(
				geometryColumns, boundingBox, srs.getId());

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

		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setId(new TableColumnKey("feature_metadata2", "geom2"));
		geometryColumns.setGeometryType(GeometryType.POINT);
		geometryColumns.setZ((byte) 1);
		geometryColumns.setM((byte) 0);

		BoundingBox boundingBox = new BoundingBox(-90, -45, 90, 45);

		SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
				.createWebMercator();
		String idColumn = "my_id";
		geometryColumns = geoPackage.createFeatureTableWithMetadata(
				geometryColumns, idColumn, boundingBox, srs.getId());

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

		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setId(new TableColumnKey("feature_metadata3", "geom3"));
		geometryColumns.setGeometryType(GeometryType.POINT);
		geometryColumns.setZ((byte) 1);
		geometryColumns.setM((byte) 0);

		BoundingBox boundingBox = new BoundingBox(-90, -45, 90, 45);

		List<FeatureColumn> additionalColumns = getFeatureColumns();

		SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
				.createWebMercator();
		geometryColumns = geoPackage.createFeatureTableWithMetadata(
				geometryColumns, additionalColumns, boundingBox, srs.getId());

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

		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setId(new TableColumnKey("feature_metadata4", "geom4"));
		geometryColumns.setGeometryType(GeometryType.POINT);
		geometryColumns.setZ((byte) 1);
		geometryColumns.setM((byte) 0);

		BoundingBox boundingBox = new BoundingBox(-90, -45, 90, 45);

		List<FeatureColumn> additionalColumns = getFeatureColumns();

		SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
				.createWebMercator();
		String idColumn = "my_other_id";
		geometryColumns = geoPackage.createFeatureTableWithMetadata(
				geometryColumns, idColumn, additionalColumns, boundingBox,
				srs.getId());

		validateFeatureTableWithMetadata(geoPackage, geometryColumns, idColumn,
				additionalColumns);
	}

	/**
	 * Get additional feature columns to create
	 * 
	 * @return
	 */
	private static List<FeatureColumn> getFeatureColumns() {
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

			TestCase.assertEquals(geoPackage.getTileTables().size(),
					tileMatrixSetDao.countOf());
			for (String tileTable : geoPackage.getTileTables()) {
				TestCase.assertTrue(geoPackage.isTable(tileTable));
				TestCase.assertNotNull(contentsDao.queryForId(tileTable));
				geoPackage.deleteTable(tileTable);
				TestCase.assertFalse(geoPackage.isTable(tileTable));
				TestCase.assertNull(contentsDao.queryForId(tileTable));
			}
			TestCase.assertEquals(0, tileMatrixSetDao.countOf());

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

}
