package mil.nga.giat.geopackage.test.data.c3;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c2.Contents;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsKey;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * Geometry Columns Utility test methods
 * 
 * @author osbornb
 */
public class GeometryColumnsUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, int expectedResults)
			throws SQLException {

		GeometryColumnsDao dao = geoPackage.getGeometryColumnsDao();
		List<GeometryColumns> results = dao.queryForAll();
		TestCase.assertEquals("Unexpected number of geometry columns rows",
				expectedResults, results.size());

		if (!results.isEmpty()) {

			// Verify non nulls
			for (GeometryColumns result : results) {
				TestCase.assertNotNull(result.getTableName());
				TestCase.assertNotNull(result.getColumnName());
				TestCase.assertNotNull(result.getGeometryType());
				TestCase.assertNotNull(result.getSrsId());
				TestCase.assertNotNull(result.getZ());
				TestCase.assertNotNull(result.getM());
				SpatialReferenceSystem srs = result.getSrs();
				if (srs != null) {
					TestCase.assertNotNull(srs.getSrsName());
					TestCase.assertNotNull(srs.getSrsId());
					TestCase.assertNotNull(srs.getOrganization());
					TestCase.assertNotNull(srs.getOrganizationCoordsysId());
					TestCase.assertNotNull(srs.getDefinition());
				}
				Contents contents = result.getContents();
				if (contents != null) {
					TestCase.assertNotNull(contents.getTableName());
					TestCase.assertNotNull(contents.getDataType());
					TestCase.assertNotNull(contents.getLastChange());
				}
			}

			// Choose random contents
			int random = (int) (Math.random() * results.size());
			GeometryColumns geometryColumns = results.get(random);

			// Query by id
			GeometryColumns queryGeometryColumns = dao
					.queryForId(geometryColumns.getId());
			TestCase.assertNotNull(queryGeometryColumns);
			TestCase.assertEquals(geometryColumns.getId(),
					queryGeometryColumns.getId());

			// Query for equal
			List<GeometryColumns> queryGeometryColumnsList = dao.queryForEq(
					GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME,
					geometryColumns.getGeometryType().name());
			TestCase.assertNotNull(queryGeometryColumnsList);
			TestCase.assertTrue(queryGeometryColumnsList.size() >= 1);
			boolean found = false;
			for (GeometryColumns queryGeometryColumnsValue : queryGeometryColumnsList) {
				TestCase.assertEquals(geometryColumns.getGeometryType(),
						queryGeometryColumnsValue.getGeometryType());
				if (!found) {
					found = geometryColumns.getId().equals(
							queryGeometryColumnsValue.getId());
				}
			}
			TestCase.assertTrue(found);

			// Query for field values
			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(GeometryColumns.COLUMN_Z, geometryColumns.getZ());
			fieldValues.put(GeometryColumns.COLUMN_M, geometryColumns.getM());

			queryGeometryColumnsList = dao.queryForFieldValues(fieldValues);
			TestCase.assertNotNull(queryGeometryColumnsList);
			TestCase.assertTrue(queryGeometryColumnsList.size() >= 1);
			found = false;
			for (GeometryColumns queryGeometryColumnsValue : queryGeometryColumnsList) {
				TestCase.assertEquals(geometryColumns.getZ(),
						queryGeometryColumnsValue.getZ());
				TestCase.assertEquals(geometryColumns.getM(),
						queryGeometryColumnsValue.getM());
				if (!found) {
					found = geometryColumns.getId().equals(
							queryGeometryColumnsValue.getId());
				}
			}
			TestCase.assertTrue(found);

			// Prepared query
			QueryBuilder<GeometryColumns, GeometryColumnsKey> qb = dao
					.queryBuilder();
			qb.where().le(GeometryColumns.COLUMN_COLUMN_NAME,
					geometryColumns.getColumnName());
			PreparedQuery<GeometryColumns> query = qb.prepare();
			queryGeometryColumnsList = dao.query(query);

			found = false;
			for (GeometryColumns queryGeometryColumnsValue : queryGeometryColumnsList) {
				if (geometryColumns.getId().equals(
						queryGeometryColumnsValue.getId())) {
					found = true;
					break;
				}
			}
			TestCase.assertTrue(found);

		}
	}

	/**
	 * Test update
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testUpdate(GeoPackage geoPackage)
			throws SQLException {
//TODO
//		ContentsDao dao = geoPackage.getContentsDao();
//		List<Contents> results = dao.queryForAll();
//
//		if (!results.isEmpty()) {
//
//			// Choose random contents
//			int random = (int) (Math.random() * results.size());
//			Contents contents = results.get(random);
//
//			// Update
//			Date updatedLastChange = new Date();
//			contents.setLastChange(updatedLastChange);
//			dao.update(contents);
//
//			// Verify update
//			dao = geoPackage.getContentsDao();
//			Contents updatedContents = dao.queryForId(contents.getId());
//			TestCase.assertEquals(updatedLastChange,
//					updatedContents.getLastChange());
//
//			// Find expected results for prepared update
//			double updatedMinimum = -90.0;
//			QueryBuilder<Contents, String> qb = dao.queryBuilder();
//			qb.where().ge(Contents.COLUMN_MIN_X, 0).or()
//					.ge(Contents.COLUMN_MIN_Y, 0);
//			PreparedQuery<Contents> preparedQuery = qb.prepare();
//			List<Contents> queryContents = dao.query(preparedQuery);
//
//			// Prepared update
//			UpdateBuilder<Contents, String> ub = dao.updateBuilder();
//			ub.updateColumnValue(Contents.COLUMN_MIN_X, updatedMinimum);
//			ub.updateColumnValue(Contents.COLUMN_MIN_Y, updatedMinimum);
//			ub.where().ge(Contents.COLUMN_MIN_X, 0).or()
//					.ge(Contents.COLUMN_MIN_Y, 0);
//			PreparedUpdate<Contents> update = ub.prepare();
//			int updated = dao.update(update);
//			TestCase.assertEquals(queryContents.size(), updated);
//
//			for (Contents updatedContent : queryContents) {
//				Contents reloadedContents = dao.queryForId(updatedContent
//						.getId());
//				TestCase.assertEquals(updatedMinimum,
//						reloadedContents.getMinX(), 0.0);
//				TestCase.assertEquals(updatedMinimum,
//						reloadedContents.getMinY(), 0.0);
//			}
//
//		}

	}

	/**
	 * Test create
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage)
			throws SQLException {
//TODO
//		SpatialReferenceSystemDao srsDao = geoPackage
//				.getSpatialReferenceSystemDao();
//		ContentsDao dao = geoPackage.getContentsDao();
//
//		// Get current count
//		long count = dao.countOf();
//
//		// Retrieve a random srs
//		List<SpatialReferenceSystem> results = srsDao.queryForAll();
//		SpatialReferenceSystem srs = null;
//		if (!results.isEmpty()) {
//			int random = (int) (Math.random() * results.size());
//			srs = results.get(random);
//		}
//
//		String tableName = "TEST_TABLE_NAME";
//		String dataType = "features";
//		String identifier = "TEST_IDENTIFIER";
//		String description = "TEST_DESCRIPTION";
//		Date lastChange = new Date();
//		double minX = -180.0;
//		double minY = -90.0;
//		double maxX = 180.0;
//		double maxY = 90.0;
//
//		// Create new contents
//		Contents contents = new Contents();
//		contents.setTableName(tableName);
//		contents.setDataType(dataType);
//		contents.setIdentifier(identifier);
//		contents.setDescription(description);
//		contents.setLastChange(lastChange);
//		contents.setMinX(minX);
//		contents.setMinY(minY);
//		contents.setMaxX(maxX);
//		contents.setMaxY(maxY);
//		contents.setSrs(srs);
//		dao.create(contents);
//
//		// Verify count
//		long newCount = dao.countOf();
//		TestCase.assertEquals(count + 1, newCount);
//
//		// Verify saved contents
//		Contents queryContents = dao.queryForId(tableName);
//		TestCase.assertEquals(tableName, queryContents.getTableName());
//		TestCase.assertEquals(dataType, queryContents.getDataType());
//		TestCase.assertEquals(identifier, queryContents.getIdentifier());
//		TestCase.assertEquals(description, queryContents.getDescription());
//		TestCase.assertEquals(lastChange, queryContents.getLastChange());
//		TestCase.assertEquals(minX, queryContents.getMinX());
//		TestCase.assertEquals(minY, queryContents.getMinY());
//		TestCase.assertEquals(maxX, queryContents.getMaxX());
//		TestCase.assertEquals(maxY, queryContents.getMaxY());
//		if (srs != null) {
//			TestCase.assertEquals(srs.getId(), queryContents.getSrs().getId());
//		} else {
//			TestCase.assertNull(queryContents.getSrs());
//		}
	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage)
			throws SQLException {

		testDeleteHelper(geoPackage, false);

	}

	/**
	 * Test delete cascade
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDeleteCascade(GeoPackage geoPackage)
			throws SQLException {

		testDeleteHelper(geoPackage, true);

	}

	/**
	 * Test delete helper
	 * 
	 * @param geoPackage
	 * @param cascade
	 * @throws SQLException
	 */
	private static void testDeleteHelper(GeoPackage geoPackage, boolean cascade)
			throws SQLException {
//TODO
//		ContentsDao dao = geoPackage.getContentsDao();
//		List<Contents> results = dao.queryForAll();
//
//		if (!results.isEmpty()) {
//
//			// Choose random contents
//			int random = (int) (Math.random() * results.size());
//			Contents contents = results.get(random);
//
//			// Save the ids of geometry columns
//			List<GeometryColumnsKey> geometryColumnsIds = new ArrayList<GeometryColumnsKey>();
//			GeometryColumnsDao geometryColumnsDao = geoPackage
//					.getGeometryColumnsDao();
//			if (geometryColumnsDao.isTableExists()) {
//				for (GeometryColumns geometryColumns : contents
//						.getGeometryColumns()) {
//					geometryColumnsIds.add(geometryColumns.getId());
//				}
//			}
//
//			// Delete the contents
//			if (cascade) {
//				dao.deleteCascade(contents);
//			} else {
//				dao.delete(contents);
//			}
//
//			// Verify deleted
//			Contents queryContents = dao.queryForId(contents.getId());
//			TestCase.assertNull(queryContents);
//
//			// Verify that geometry columns or foreign keys were deleted
//			for (GeometryColumnsKey geometryColumnsId : geometryColumnsIds) {
//				GeometryColumns queryGeometryColumns = geometryColumnsDao
//						.queryForId(geometryColumnsId);
//				if (cascade) {
//					TestCase.assertNull(queryGeometryColumns);
//				} else {
//					TestCase.assertNull(queryGeometryColumns.getContents());
//				}
//			}
//
//			// Choose prepared deleted
//			results = dao.queryForAll();
//			if (!results.isEmpty()) {
//
//				// Choose random contents
//				random = (int) (Math.random() * results.size());
//				contents = results.get(random);
//
//				// Find which contents to delete and the geometry columns
//				QueryBuilder<Contents, String> qb = dao.queryBuilder();
//				qb.where()
//						.eq(Contents.COLUMN_DATA_TYPE, contents.getDataType());
//				PreparedQuery<Contents> query = qb.prepare();
//				List<Contents> queryResults = dao.query(query);
//				int count = queryResults.size();
//				geometryColumnsIds = new ArrayList<GeometryColumnsKey>();
//				for (Contents queryResultsContents : queryResults) {
//					if (geometryColumnsDao.isTableExists()) {
//						for (GeometryColumns geometryColumns : queryResultsContents
//								.getGeometryColumns()) {
//							geometryColumnsIds.add(geometryColumns.getId());
//						}
//					}
//				}
//
//				// Delete
//				int deleted;
//				if (cascade) {
//					deleted = dao.deleteCascade(query);
//				} else {
//					DeleteBuilder<Contents, String> db = dao.deleteBuilder();
//					db.where().eq(Contents.COLUMN_DATA_TYPE,
//							contents.getDataType());
//					PreparedDelete<Contents> deleteQuery = db.prepare();
//					deleted = dao.delete(deleteQuery);
//				}
//				TestCase.assertEquals(count, deleted);
//
//				// Verify that geometry columns or foreign keys were deleted
//				for (GeometryColumnsKey geometryColumnsId : geometryColumnsIds) {
//					GeometryColumns queryGeometryColumns = geometryColumnsDao
//							.queryForId(geometryColumnsId);
//					if (cascade) {
//						TestCase.assertNull(queryGeometryColumns);
//					} else {
//						TestCase.assertNull(queryGeometryColumns.getContents());
//					}
//				}
//			}
//		}
	}

}
