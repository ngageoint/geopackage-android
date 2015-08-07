package mil.nga.geopackage.test.core.contents;

import java.sql.SQLException;
import java.util.ArrayList;
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
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.schema.TableColumnKey;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.wkb.geom.GeometryType;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

/**
 * Contents Utility test methods
 * 
 * @author osbornb
 */
public class ContentsUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		ContentsDao dao = geoPackage.getContentsDao();
		List<Contents> results = dao.queryForAll();
		if (expectedResults != null) {
			TestCase.assertEquals("Unexpected number of contents rows",
					expectedResults.intValue(), results.size());
		}

		if (!results.isEmpty()) {

			int count = results.size();

			// Verify non nulls
			for (Contents result : results) {
				TestCase.assertNotNull(result.getTableName());
				TestCase.assertNotNull(result.getDataType());
				TestCase.assertNotNull(result.getLastChange());
				SpatialReferenceSystem srs = result.getSrs();
				if (srs != null) {
					TestCase.assertNotNull(srs.getSrsName());
					TestCase.assertNotNull(srs.getSrsId());
					TestCase.assertNotNull(srs.getOrganization());
					TestCase.assertNotNull(srs.getOrganizationCoordsysId());
					TestCase.assertNotNull(srs.getDefinition());
				}
			}

			// Choose random contents
			int random = (int) (Math.random() * results.size());
			Contents contents = results.get(random);

			// Query by id
			Contents queryContents = dao.queryForId(contents.getTableName());
			TestCase.assertNotNull(queryContents);
			TestCase.assertEquals(contents.getTableName(),
					queryContents.getTableName());

			// Query for equal
			List<Contents> queryContentsList = dao.queryForEq(
					Contents.COLUMN_IDENTIFIER, contents.getIdentifier());
			TestCase.assertNotNull(queryContentsList);
			TestCase.assertEquals(1, queryContentsList.size());
			TestCase.assertEquals(contents.getIdentifier(), queryContentsList
					.get(0).getIdentifier());

			// Query for field values
			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(Contents.COLUMN_DATA_TYPE, contents.getDataType()
					.getName());
			if (contents.getSrs() != null) {
				fieldValues.put(Contents.COLUMN_SRS_ID, contents.getSrs()
						.getSrsId());
			}
			queryContentsList = dao.queryForFieldValues(fieldValues);
			TestCase.assertNotNull(queryContentsList);
			TestCase.assertTrue(queryContentsList.size() >= 1);
			boolean found = false;
			for (Contents queryContentsValue : queryContentsList) {
				TestCase.assertEquals(contents.getDataType(),
						queryContentsValue.getDataType());
				if (contents.getSrs() != null) {
					TestCase.assertEquals(contents.getSrs().getSrsId(),
							queryContentsValue.getSrs().getSrsId());
				}
				if (!found) {
					found = contents.getTableName().equals(
							queryContentsValue.getTableName());
				}
			}
			TestCase.assertTrue(found);

			// Prepared query, less than equal date
			QueryBuilder<Contents, String> qb = dao.queryBuilder();
			qb.where()
					.le(Contents.COLUMN_LAST_CHANGE, contents.getLastChange());
			PreparedQuery<Contents> query = qb.prepare();
			queryContentsList = dao.query(query);

			int queryCount = queryContentsList.size();

			found = false;
			for (Contents queryContentsValue : queryContentsList) {
				if (contents.getTableName().equals(
						queryContentsValue.getTableName())) {
					found = true;
					break;
				}
			}
			TestCase.assertTrue(found);

			// Prepared query, greater than date
			qb = dao.queryBuilder();
			qb.where()
					.gt(Contents.COLUMN_LAST_CHANGE, contents.getLastChange());
			query = qb.prepare();
			queryContentsList = dao.query(query);

			found = false;
			for (Contents queryContentsValue : queryContentsList) {
				if (contents.getTableName().equals(
						queryContentsValue.getTableName())) {
					found = true;
					break;
				}
			}
			TestCase.assertFalse(found);

			queryCount += queryContentsList.size();
			TestCase.assertEquals(count, queryCount);

		}
	}

	/**
	 * Test update
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testUpdate(GeoPackage geoPackage) throws SQLException {

		ContentsDao dao = geoPackage.getContentsDao();
		List<Contents> results = dao.queryForAll();

		if (!results.isEmpty()) {

			// Choose random contents
			int random = (int) (Math.random() * results.size());
			Contents contents = results.get(random);

			// Update
			Date updatedLastChange = new Date();
			contents.setLastChange(updatedLastChange);
			dao.update(contents);

			// Verify update
			dao = geoPackage.getContentsDao();
			Contents updatedContents = dao.queryForId(contents.getId());
			TestCase.assertEquals(updatedLastChange,
					updatedContents.getLastChange());

			// Find expected results for prepared update
			double updatedMinimum = -90.0;
			QueryBuilder<Contents, String> qb = dao.queryBuilder();
			qb.where().ge(Contents.COLUMN_MIN_X, 0).or()
					.ge(Contents.COLUMN_MIN_Y, 0);
			PreparedQuery<Contents> preparedQuery = qb.prepare();
			List<Contents> queryContents = dao.query(preparedQuery);

			// Prepared update
			UpdateBuilder<Contents, String> ub = dao.updateBuilder();
			ub.updateColumnValue(Contents.COLUMN_MIN_X, updatedMinimum);
			ub.updateColumnValue(Contents.COLUMN_MIN_Y, updatedMinimum);
			ub.where().ge(Contents.COLUMN_MIN_X, 0).or()
					.ge(Contents.COLUMN_MIN_Y, 0);
			PreparedUpdate<Contents> update = ub.prepare();
			int updated = dao.update(update);
			TestCase.assertEquals(queryContents.size(), updated);

			for (Contents updatedContent : queryContents) {
				Contents reloadedContents = dao.queryForId(updatedContent
						.getId());
				TestCase.assertEquals(updatedMinimum,
						reloadedContents.getMinX(), 0.0);
				TestCase.assertEquals(updatedMinimum,
						reloadedContents.getMinY(), 0.0);
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
		ContentsDao dao = geoPackage.getContentsDao();

		// Get current count
		long count = dao.countOf();

		// Retrieve a random srs
		List<SpatialReferenceSystem> results = srsDao.queryForAll();
		SpatialReferenceSystem srs = null;
		if (!results.isEmpty()) {
			int random = (int) (Math.random() * results.size());
			srs = results.get(random);
		}

		String tableName = "TEST_TABLE_NAME";
		ContentsDataType dataType = ContentsDataType.FEATURES;
		String identifier = "TEST_IDENTIFIER";
		String description = "TEST_DESCRIPTION";
		Date lastChange = new Date();
		double minX = -180.0;
		double minY = -90.0;
		double maxX = 180.0;
		double maxY = 90.0;

		// Create new contents
		Contents contents = new Contents();
		contents.setTableName(tableName);
		contents.setDataType(dataType);
		contents.setIdentifier(identifier);
		contents.setDescription(description);
		contents.setLastChange(lastChange);
		contents.setMinX(minX);
		contents.setMinY(minY);
		contents.setMaxX(maxX);
		contents.setMaxY(maxY);
		contents.setSrs(srs);

		// Create the feature table
		geoPackage.createFeatureTable(TestUtils.buildFeatureTable(
				contents.getTableName(), "geom", GeometryType.GEOMETRY));

		geoPackage.createGeometryColumnsTable();

		dao.create(contents);

		// Verify count
		long newCount = dao.countOf();
		TestCase.assertEquals(count + 1, newCount);

		// Verify saved contents
		Contents queryContents = dao.queryForId(tableName);
		TestCase.assertEquals(tableName, queryContents.getTableName());
		TestCase.assertEquals(dataType, queryContents.getDataType());
		TestCase.assertEquals(identifier, queryContents.getIdentifier());
		TestCase.assertEquals(description, queryContents.getDescription());
		TestCase.assertEquals(lastChange, queryContents.getLastChange());
		TestCase.assertEquals(minX, queryContents.getMinX());
		TestCase.assertEquals(minY, queryContents.getMinY());
		TestCase.assertEquals(maxX, queryContents.getMaxX());
		TestCase.assertEquals(maxY, queryContents.getMaxY());
		if (srs != null) {
			TestCase.assertEquals(srs.getId(), queryContents.getSrs().getId());
		} else {
			TestCase.assertNull(queryContents.getSrs());
		}
	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

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

		ContentsDao dao = geoPackage.getContentsDao();
		List<Contents> results = dao.queryForAll();

		if (!results.isEmpty()) {

			// Choose random contents
			int random = (int) (Math.random() * results.size());
			Contents contents = results.get(random);

			// Save the ids of geometry columns
			List<TableColumnKey> geometryColumnsIds = new ArrayList<TableColumnKey>();
			GeometryColumnsDao geometryColumnsDao = geoPackage
					.getGeometryColumnsDao();
			if (geometryColumnsDao.isTableExists()) {
				GeometryColumns geometryColumns = contents.getGeometryColumns();
				if (geometryColumns != null) {
					geometryColumnsIds.add(geometryColumns.getId());
				}
			}

			// Delete the contents
			if (cascade) {
				dao.deleteCascade(contents);
			} else {
				dao.delete(contents);
			}

			// Verify deleted
			Contents queryContents = dao.queryForId(contents.getId());
			TestCase.assertNull(queryContents);

			// Verify that geometry columns or foreign keys were deleted
			for (TableColumnKey geometryColumnsId : geometryColumnsIds) {
				GeometryColumns queryGeometryColumns = geometryColumnsDao
						.queryForId(geometryColumnsId);
				if (cascade) {
					TestCase.assertNull(queryGeometryColumns);
				} else {
					TestCase.assertNull(queryGeometryColumns.getContents());
				}
			}

			// Choose prepared deleted
			results = dao.queryForAll();
			if (!results.isEmpty()) {

				// Choose random contents
				random = (int) (Math.random() * results.size());
				contents = results.get(random);

				// Find which contents to delete and the geometry columns
				QueryBuilder<Contents, String> qb = dao.queryBuilder();
				qb.where().eq(Contents.COLUMN_DATA_TYPE,
						contents.getDataType().getName());
				PreparedQuery<Contents> query = qb.prepare();
				List<Contents> queryResults = dao.query(query);
				int count = queryResults.size();
				geometryColumnsIds = new ArrayList<TableColumnKey>();
				for (Contents queryResultsContents : queryResults) {
					if (geometryColumnsDao.isTableExists()) {
						GeometryColumns geometryColumns = queryResultsContents
								.getGeometryColumns();
						if (geometryColumns != null) {
							geometryColumnsIds.add(geometryColumns.getId());
						}
					}
				}

				// Delete
				int deleted;
				if (cascade) {
					deleted = dao.deleteCascade(query);
				} else {
					DeleteBuilder<Contents, String> db = dao.deleteBuilder();
					db.where().eq(Contents.COLUMN_DATA_TYPE,
							contents.getDataType().getName());
					PreparedDelete<Contents> deleteQuery = db.prepare();
					deleted = dao.delete(deleteQuery);
				}
				TestCase.assertEquals(count, deleted);

				// Verify that geometry columns or foreign keys were deleted
				for (TableColumnKey geometryColumnsId : geometryColumnsIds) {
					GeometryColumns queryGeometryColumns = geometryColumnsDao
							.queryForId(geometryColumnsId);
					if (cascade) {
						TestCase.assertNull(queryGeometryColumns);
					} else {
						TestCase.assertNull(queryGeometryColumns.getContents());
					}
				}
			}
		}
	}

}
