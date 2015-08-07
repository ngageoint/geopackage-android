package mil.nga.geopackage.test.tiles.matrix;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrix.TileMatrixKey;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

/**
 * Tile Matrix Set Utility test methods
 * 
 * @author osbornb
 */
public class TileMatrixUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		TileMatrixDao dao = geoPackage.getTileMatrixDao();
		if (dao.isTableExists()) {

			List<TileMatrix> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals("Unexpected number of tile matrix rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				// Verify non nulls
				for (TileMatrix result : results) {
					TestCase.assertNotNull(result.getTableName());
					TestCase.assertTrue(result.getZoomLevel() >= 0);
					TestCase.assertNotNull(result.getId());
					TestCase.assertTrue(result.getMatrixWidth() > 0);
					TestCase.assertTrue(result.getMatrixHeight() > 0);
					TestCase.assertTrue(result.getTileWidth() > 0);
					TestCase.assertTrue(result.getTileHeight() > 0);
					TestCase.assertTrue(result.getPixelXSize() > 0);
					TestCase.assertTrue(result.getPixelYSize() > 0);

					Contents contents = result.getContents();
					TestCase.assertNotNull(contents);
					TestCase.assertNotNull(contents.getTableName());
					TestCase.assertNotNull(contents.getDataType());
					TestCase.assertNotNull(contents.getLastChange());
				}

				// Choose random tile matrix
				int random = (int) (Math.random() * results.size());
				TileMatrix tileMatrix = results.get(random);

				// Query by id
				TileMatrix queryTileMatrix = dao.queryForId(tileMatrix.getId());
				TestCase.assertNotNull(queryTileMatrix);
				TestCase.assertEquals(tileMatrix.getId(),
						queryTileMatrix.getId());

				// Query for equal
				List<TileMatrix> queryTileMatrixList = dao
						.queryForEq(TileMatrix.COLUMN_ZOOM_LEVEL,
								tileMatrix.getZoomLevel());
				TestCase.assertNotNull(queryTileMatrixList);
				TestCase.assertTrue(queryTileMatrixList.size() >= 1);
				boolean found = false;
				for (TileMatrix queryTileMatrixValue : queryTileMatrixList) {
					TestCase.assertEquals(tileMatrix.getZoomLevel(),
							queryTileMatrixValue.getZoomLevel());
					if (!found) {
						found = tileMatrix.getId().equals(
								queryTileMatrixValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Query for field values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(TileMatrix.COLUMN_MATRIX_HEIGHT,
						tileMatrix.getMatrixHeight());
				fieldValues.put(TileMatrix.COLUMN_MATRIX_WIDTH,
						tileMatrix.getMatrixWidth());

				queryTileMatrixList = dao.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryTileMatrixList);
				TestCase.assertTrue(queryTileMatrixList.size() >= 1);
				found = false;
				for (TileMatrix queryTileMatrixValue : queryTileMatrixList) {
					TestCase.assertEquals(tileMatrix.getMatrixHeight(),
							queryTileMatrixValue.getMatrixHeight());
					TestCase.assertEquals(tileMatrix.getMatrixWidth(),
							queryTileMatrixValue.getMatrixWidth());
					if (!found) {
						found = tileMatrix.getId().equals(
								queryTileMatrixValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Prepared query
				QueryBuilder<TileMatrix, TileMatrixKey> qb = dao.queryBuilder();
				qb.where().eq(TileMatrix.COLUMN_ZOOM_LEVEL,
						tileMatrix.getZoomLevel());
				PreparedQuery<TileMatrix> query = qb.prepare();
				queryTileMatrixList = dao.query(query);

				found = false;
				for (TileMatrix queryTileMatrixValue : queryTileMatrixList) {
					if (tileMatrix.getId().equals(queryTileMatrixValue.getId())) {
						found = true;
						break;
					}
				}
				TestCase.assertTrue(found);

			}
		}
	}

	/**
	 * Test update
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testUpdate(GeoPackage geoPackage) throws SQLException {

		TileMatrixDao dao = geoPackage.getTileMatrixDao();
		if (dao.isTableExists()) {
			List<TileMatrix> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random tile matrix
				int random = (int) (Math.random() * results.size());
				TileMatrix tileMatrix = results.get(random);

				// Update
				double updatedPixelXSize = 4222.22;
				tileMatrix.setPixelXSize(updatedPixelXSize);
				dao.update(tileMatrix);

				// Verify update
				dao = geoPackage.getTileMatrixDao();
				TileMatrix updatedTileMatrix = dao.queryForId(tileMatrix
						.getId());
				TestCase.assertEquals(updatedPixelXSize,
						updatedTileMatrix.getPixelXSize());

				// Find expected results for prepared update
				int updatedTileWidthHeight = 256;
				QueryBuilder<TileMatrix, TileMatrixKey> qb = dao.queryBuilder();
				qb.where().gt(TileMatrix.COLUMN_TILE_WIDTH, 256).and()
						.gt(TileMatrix.COLUMN_TILE_HEIGHT, 256);
				PreparedQuery<TileMatrix> preparedQuery = qb.prepare();
				List<TileMatrix> queryTileMatrix = dao.query(preparedQuery);

				// Prepared update
				UpdateBuilder<TileMatrix, TileMatrixKey> ub = dao
						.updateBuilder();
				ub.updateColumnValue(TileMatrix.COLUMN_TILE_WIDTH,
						updatedTileWidthHeight);
				ub.updateColumnValue(TileMatrix.COLUMN_TILE_HEIGHT,
						updatedTileWidthHeight);
				ub.where().gt(TileMatrix.COLUMN_TILE_WIDTH, 256).and()
						.gt(TileMatrix.COLUMN_TILE_HEIGHT, 256);
				PreparedUpdate<TileMatrix> update = ub.prepare();
				int updated = dao.update(update);
				TestCase.assertEquals(queryTileMatrix.size(), updated);

				for (TileMatrix updatedQueryTileMatrix : queryTileMatrix) {
					TileMatrix reloadedTileMatrix = dao
							.queryForId(updatedQueryTileMatrix.getId());
					TestCase.assertNotNull(reloadedTileMatrix);
					TestCase.assertEquals(updatedTileWidthHeight,
							reloadedTileMatrix.getTileWidth());
					TestCase.assertEquals(updatedTileWidthHeight,
							reloadedTileMatrix.getTileHeight());
				}

			}
		}
	}

	/**
	 * Test create
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage) throws SQLException {

		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();
		ContentsDao contentsDao = geoPackage.getContentsDao();
		TileMatrixDao dao = geoPackage.getTileMatrixDao();

		if (dao.isTableExists()) {

			// Get current count
			long count = dao.countOf();

			// Retrieve a random srs
			List<SpatialReferenceSystem> results = srsDao.queryForAll();
			SpatialReferenceSystem srs = null;
			if (!results.isEmpty()) {
				int random = (int) (Math.random() * results.size());
				srs = results.get(random);
			}

			// Create a new contents
			Contents contents = new Contents();
			contents.setTableName("test_contents");
			contents.setDataType(ContentsDataType.TILES);
			contents.setIdentifier("test_contents");
			contents.setDescription("");
			contents.setLastChange(new Date());
			contents.setMinX(-180.0);
			contents.setMinY(-90.0);
			contents.setMaxX(180.0);
			contents.setMaxY(90.0);
			contents.setSrs(srs);

			// Create the user tile table
			geoPackage.createTileTable(TestUtils.buildTileTable(contents
					.getTableName()));

			contentsDao.create(contents);

			// Create new matrix tile
			int zoom = 3;
			int matrixWidth = 4;
			int matrixHeight = 5;
			int tileWidth = 128;
			int tileHeight = 256;
			double pixelXSize = 889.5;
			double pixelYSize = 900.1;

			TileMatrix tileMatrix = new TileMatrix();
			tileMatrix.setContents(contents);
			tileMatrix.setZoomLevel(zoom);
			tileMatrix.setMatrixWidth(matrixWidth);
			tileMatrix.setMatrixHeight(matrixHeight);
			tileMatrix.setTileWidth(tileWidth);
			tileMatrix.setTileHeight(tileHeight);
			tileMatrix.setPixelXSize(pixelXSize);
			tileMatrix.setPixelYSize(pixelYSize);
			dao.create(tileMatrix);

			// Verify count
			long newCount = dao.countOf();
			TestCase.assertEquals(count + 1, newCount);

			// Verify saved matrix tile
			TileMatrix queryTileMatrix = dao.queryForId(tileMatrix.getId());
			TestCase.assertEquals(contents.getId(),
					queryTileMatrix.getTableName());
			TestCase.assertEquals(zoom, queryTileMatrix.getZoomLevel());
			TestCase.assertEquals(matrixWidth, queryTileMatrix.getMatrixWidth());
			TestCase.assertEquals(matrixHeight,
					queryTileMatrix.getMatrixHeight());
			TestCase.assertEquals(tileWidth, queryTileMatrix.getTileWidth());
			TestCase.assertEquals(tileHeight, queryTileMatrix.getTileHeight());
			TestCase.assertEquals(pixelXSize, queryTileMatrix.getPixelXSize());
			TestCase.assertEquals(pixelYSize, queryTileMatrix.getPixelYSize());
			TestCase.assertEquals(contents.getId(), queryTileMatrix
					.getContents().getId());
		}
	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		TileMatrixDao dao = geoPackage.getTileMatrixDao();
		if (dao.isTableExists()) {
			List<TileMatrix> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random tile matrix
				int random = (int) (Math.random() * results.size());
				TileMatrix tileMatrix = results.get(random);

				// Delete the tile matrix
				dao.delete(tileMatrix);

				// Verify deleted
				TileMatrix queryTileMatrix = dao.queryForId(tileMatrix.getId());
				TestCase.assertNull(queryTileMatrix);

				// Prepared deleted
				results = dao.queryForAll();
				if (!results.isEmpty()) {

					// Choose random tile matrix
					random = (int) (Math.random() * results.size());
					tileMatrix = results.get(random);

					// Find which tile matrix to delete
					QueryBuilder<TileMatrix, TileMatrixKey> qb = dao
							.queryBuilder();
					qb.where().eq(TileMatrix.COLUMN_ZOOM_LEVEL,
							tileMatrix.getZoomLevel());
					PreparedQuery<TileMatrix> query = qb.prepare();
					List<TileMatrix> queryResults = dao.query(query);
					int count = queryResults.size();

					// Delete
					DeleteBuilder<TileMatrix, TileMatrixKey> db = dao
							.deleteBuilder();
					db.where().eq(TileMatrix.COLUMN_ZOOM_LEVEL,
							tileMatrix.getZoomLevel());
					PreparedDelete<TileMatrix> deleteQuery = db.prepare();
					int deleted = dao.delete(deleteQuery);

					TestCase.assertEquals(count, deleted);

				}
			}
		}
	}

}
