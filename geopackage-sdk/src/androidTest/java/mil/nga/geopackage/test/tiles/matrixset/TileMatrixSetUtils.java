package mil.nga.geopackage.test.tiles.matrixset;

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
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;

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
public class TileMatrixSetUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();
		if (dao.isTableExists()) {
			List<TileMatrixSet> results = dao.queryForAll();
			if (expectedResults != null) {
				TestCase.assertEquals(
						"Unexpected number of tile matrix set rows",
						expectedResults.intValue(), results.size());
			}

			if (!results.isEmpty()) {

				// Verify non nulls
				for (TileMatrixSet result : results) {
					TestCase.assertNotNull(result.getTableName());
					TestCase.assertNotNull(result.getId());
					TestCase.assertNotNull(result.getSrsId());
					TestCase.assertNotNull(result.getMinX());
					TestCase.assertNotNull(result.getMinY());
					TestCase.assertNotNull(result.getMaxX());
					TestCase.assertNotNull(result.getMaxY());

					SpatialReferenceSystem srs = result.getSrs();
					TestCase.assertNotNull(srs);
					TestCase.assertNotNull(srs.getSrsName());
					TestCase.assertNotNull(srs.getSrsId());
					TestCase.assertNotNull(srs.getOrganization());
					TestCase.assertNotNull(srs.getOrganizationCoordsysId());
					TestCase.assertNotNull(srs.getDefinition());

					Contents contents = result.getContents();
					TestCase.assertNotNull(contents);
					TestCase.assertNotNull(contents.getTableName());
					TestCase.assertNotNull(contents.getDataType());
					TestCase.assertNotNull(contents.getLastChange());
				}

				// Choose random contents
				int random = (int) (Math.random() * results.size());
				TileMatrixSet tileMatrixSet = results.get(random);

				// Query by id
				TileMatrixSet queryTileMatrixSet = dao.queryForId(tileMatrixSet
						.getId());
				TestCase.assertNotNull(queryTileMatrixSet);
				TestCase.assertEquals(tileMatrixSet.getId(),
						queryTileMatrixSet.getId());

				// Query for equal
				List<TileMatrixSet> queryTileMatrixSetList = dao.queryForEq(
						TileMatrixSet.COLUMN_SRS_ID, tileMatrixSet.getSrsId());
				TestCase.assertNotNull(queryTileMatrixSetList);
				TestCase.assertTrue(queryTileMatrixSetList.size() >= 1);
				boolean found = false;
				for (TileMatrixSet queryTileMatrixSetValue : queryTileMatrixSetList) {
					TestCase.assertEquals(tileMatrixSet.getSrsId(),
							queryTileMatrixSetValue.getSrsId());
					if (!found) {
						found = tileMatrixSet.getId().equals(
								queryTileMatrixSetValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Query for field values
				Map<String, Object> fieldValues = new HashMap<String, Object>();
				fieldValues.put(TileMatrixSet.COLUMN_MIN_X,
						tileMatrixSet.getMinX());
				fieldValues.put(TileMatrixSet.COLUMN_MAX_X,
						tileMatrixSet.getMaxX());

				queryTileMatrixSetList = dao.queryForFieldValues(fieldValues);
				TestCase.assertNotNull(queryTileMatrixSetList);
				TestCase.assertTrue(queryTileMatrixSetList.size() >= 1);
				found = false;
				for (TileMatrixSet queryTileMatrixSetValue : queryTileMatrixSetList) {
					TestCase.assertEquals(tileMatrixSet.getMinX(),
							queryTileMatrixSetValue.getMinX());
					TestCase.assertEquals(tileMatrixSet.getMaxX(),
							queryTileMatrixSetValue.getMaxX());
					if (!found) {
						found = tileMatrixSet.getId().equals(
								queryTileMatrixSetValue.getId());
					}
				}
				TestCase.assertTrue(found);

				// Prepared query
				QueryBuilder<TileMatrixSet, String> qb = dao.queryBuilder();
				qb.where().eq(TileMatrixSet.COLUMN_SRS_ID,
						tileMatrixSet.getSrsId());
				PreparedQuery<TileMatrixSet> query = qb.prepare();
				queryTileMatrixSetList = dao.query(query);

				found = false;
				for (TileMatrixSet queryTileMatrixSetValue : queryTileMatrixSetList) {
					if (tileMatrixSet.getId().equals(
							queryTileMatrixSetValue.getId())) {
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

		TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();
		if (dao.isTableExists()) {
			List<TileMatrixSet> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random tile matrix set
				int random = (int) (Math.random() * results.size());
				TileMatrixSet tileMatrixSet = results.get(random);

				// Update
				double updatedMaxY = 30.3333;
				tileMatrixSet.setMaxY(updatedMaxY);
				dao.update(tileMatrixSet);

				// Verify update
				dao = geoPackage.getTileMatrixSetDao();
				TileMatrixSet updatedTileMatrixSet = dao
						.queryForId(tileMatrixSet.getId());
				TestCase.assertEquals(updatedMaxY,
						updatedTileMatrixSet.getMaxY());

				// Find expected results for prepared update
				double updatedMin = 1.11;
				QueryBuilder<TileMatrixSet, String> qb = dao.queryBuilder();
				qb.where().lt(TileMatrixSet.COLUMN_MIN_X, 0.0).and()
						.lt(TileMatrixSet.COLUMN_MIN_Y, 0.0);
				PreparedQuery<TileMatrixSet> preparedQuery = qb.prepare();
				List<TileMatrixSet> queryTileMatrixSet = dao
						.query(preparedQuery);

				// Prepared update
				UpdateBuilder<TileMatrixSet, String> ub = dao.updateBuilder();
				ub.updateColumnValue(TileMatrixSet.COLUMN_MIN_X, updatedMin);
				ub.updateColumnValue(TileMatrixSet.COLUMN_MIN_Y, updatedMin);
				ub.where().lt(TileMatrixSet.COLUMN_MIN_X, 0.0).and()
						.lt(TileMatrixSet.COLUMN_MIN_Y, 0.0);
				PreparedUpdate<TileMatrixSet> update = ub.prepare();
				int updated = dao.update(update);
				TestCase.assertEquals(queryTileMatrixSet.size(), updated);

				for (TileMatrixSet updatedQueryTileMatrixSet : queryTileMatrixSet) {
					TileMatrixSet reloadedTileMatrixSet = dao
							.queryForId(updatedQueryTileMatrixSet.getId());
					TestCase.assertNotNull(reloadedTileMatrixSet);
					TestCase.assertEquals(updatedMin,
							reloadedTileMatrixSet.getMinX());
					TestCase.assertEquals(updatedMin,
							reloadedTileMatrixSet.getMinY());
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
		TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();

		if (dao.isTableExists()) {

			// Get current count
			long count = dao.countOf();
			TestCase.assertEquals(count, dao.getTileTables().size());

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

			// Create new matrix tile set
			TileMatrixSet tileMatrixSet = new TileMatrixSet();
			tileMatrixSet.setContents(contents);
			tileMatrixSet.setSrs(contents.getSrs());
			tileMatrixSet.setMinX(contents.getMinX());
			tileMatrixSet.setMinY(contents.getMinY());
			tileMatrixSet.setMaxX(contents.getMaxX());
			tileMatrixSet.setMaxY(contents.getMaxY());
			dao.create(tileMatrixSet);

			// Verify count
			long newCount = dao.countOf();
			TestCase.assertEquals(count + 1, newCount);
			TestCase.assertEquals(newCount, dao.getTileTables().size());
			TestCase.assertTrue(dao.getTileTables().contains(
					contents.getTableName()));

			// Verify saved matrix tile set
			TileMatrixSet queryTileMatrixSet = dao.queryForId(tileMatrixSet
					.getId());
			TestCase.assertEquals(contents.getId(),
					queryTileMatrixSet.getTableName());
			TestCase.assertEquals(contents.getSrsId().longValue(),
					queryTileMatrixSet.getSrsId());
			TestCase.assertEquals(contents.getMinX(),
					queryTileMatrixSet.getMinX());
			TestCase.assertEquals(contents.getMinY(),
					queryTileMatrixSet.getMinY());
			TestCase.assertEquals(contents.getMaxX(),
					queryTileMatrixSet.getMaxX());
			TestCase.assertEquals(contents.getMaxY(),
					queryTileMatrixSet.getMaxY());
			TestCase.assertEquals(contents.getId(), queryTileMatrixSet
					.getContents().getId());
			TestCase.assertEquals(contents.getSrsId().longValue(),
					queryTileMatrixSet.getSrs().getId());
		}
	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();
		if (dao.isTableExists()) {
			List<TileMatrixSet> results = dao.queryForAll();

			if (!results.isEmpty()) {

				// Choose random tile matrix set
				int random = (int) (Math.random() * results.size());
				TileMatrixSet tileMatrixSet = results.get(random);

				// Delete the tile matrix set
				dao.delete(tileMatrixSet);

				// Verify deleted
				TileMatrixSet queryTileMatrixSet = dao.queryForId(tileMatrixSet
						.getId());
				TestCase.assertNull(queryTileMatrixSet);

				// Prepared deleted
				results = dao.queryForAll();
				if (!results.isEmpty()) {

					// Choose random tile matrix set
					random = (int) (Math.random() * results.size());
					tileMatrixSet = results.get(random);

					// Find which tile matrix set to delete
					QueryBuilder<TileMatrixSet, String> qb = dao.queryBuilder();
					qb.where().eq(TileMatrixSet.COLUMN_SRS_ID,
							tileMatrixSet.getSrsId());
					PreparedQuery<TileMatrixSet> query = qb.prepare();
					List<TileMatrixSet> queryResults = dao.query(query);
					int count = queryResults.size();

					// Delete
					DeleteBuilder<TileMatrixSet, String> db = dao
							.deleteBuilder();
					db.where().eq(TileMatrixSet.COLUMN_SRS_ID,
							tileMatrixSet.getSrsId());
					PreparedDelete<TileMatrixSet> deleteQuery = db.prepare();
					int deleted = dao.delete(deleteQuery);

					TestCase.assertEquals(count, deleted);

				}
			}
		}
	}

}
