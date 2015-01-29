package mil.nga.giat.geopackage.test.data.c3;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c2.Contents;
import mil.nga.giat.geopackage.data.c2.ContentsDao;
import mil.nga.giat.geopackage.data.c2.ContentsDataType;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsKey;
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.test.TestUtils;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

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
					GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME, geometryColumns
							.getGeometryType().getName());
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
	public static void testUpdate(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao dao = geoPackage.getGeometryColumnsDao();
		List<GeometryColumns> results = dao.queryForAll();

		if (!results.isEmpty()) {

			// Choose random geometry columns
			int random = (int) (Math.random() * results.size());
			GeometryColumns geometryColumns = results.get(random);

			// Update
			int updatedM = 2;
			geometryColumns.setM(updatedM);
			dao.update(geometryColumns);

			// Verify update
			dao = geoPackage.getGeometryColumnsDao();
			GeometryColumns updatedContents = dao.queryForId(geometryColumns
					.getId());
			TestCase.assertEquals(updatedM, updatedContents.getM().intValue());

			// Find expected results for prepared update
			String updatedColumnName = "new_geom";
			QueryBuilder<GeometryColumns, GeometryColumnsKey> qb = dao
					.queryBuilder();
			qb.where().eq(GeometryColumns.COLUMN_Z, 0).or()
					.eq(GeometryColumns.COLUMN_Z, 2);
			PreparedQuery<GeometryColumns> preparedQuery = qb.prepare();
			List<GeometryColumns> queryGeometryColumns = dao
					.query(preparedQuery);

			// Prepared update
			UpdateBuilder<GeometryColumns, GeometryColumnsKey> ub = dao
					.updateBuilder();
			ub.updateColumnValue(GeometryColumns.COLUMN_COLUMN_NAME,
					updatedColumnName);
			ub.where().eq(GeometryColumns.COLUMN_Z, 0).or()
					.eq(GeometryColumns.COLUMN_Z, 2);
			PreparedUpdate<GeometryColumns> update = ub.prepare();
			int updated = dao.update(update);
			TestCase.assertEquals(queryGeometryColumns.size(), updated);

			for (GeometryColumns updatedGeometryColumns : queryGeometryColumns) {
				updatedGeometryColumns.setColumnName(updatedColumnName);
				GeometryColumns reloadedGeometryColumns = dao
						.queryForId(updatedGeometryColumns.getId());
				TestCase.assertNotNull(reloadedGeometryColumns);
				TestCase.assertEquals(updatedColumnName,
						reloadedGeometryColumns.getColumnName());
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
		GeometryColumnsDao dao = geoPackage.getGeometryColumnsDao();

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
		contents.setDataType(ContentsDataType.FEATURES);
		contents.setIdentifier("test_contents");
		contents.setDescription("");
		contents.setLastChange(new Date());
		contents.setMinX(-180.0);
		contents.setMinY(-90.0);
		contents.setMaxX(180.0);
		contents.setMaxY(90.0);
		contents.setSrs(srs);

		// Create the feature table
		geoPackage.createTable(TestUtils.buildTable(contents.getTableName(),
				"geom", "GEOMETRY"));

		contentsDao.create(contents);

		String columnName = "TEST_COLUMN_NAME";
		GeometryType geometryType = GeometryType.POINT;
		int z = 2;
		int m = 2;

		// Create new geometry columns
		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setContents(contents);
		geometryColumns.setColumnName(columnName);
		geometryColumns.setGeometryType(geometryType);
		geometryColumns.setSrs(contents.getSrs());
		geometryColumns.setZ(z);
		geometryColumns.setM(m);
		dao.create(geometryColumns);

		// Verify count
		long newCount = dao.countOf();
		TestCase.assertEquals(count + 1, newCount);

		// Verify saved geometry columns
		GeometryColumns queryGeometryColumns = dao.queryForId(geometryColumns
				.getId());
		TestCase.assertEquals(contents.getId(),
				queryGeometryColumns.getTableName());
		TestCase.assertEquals(columnName, queryGeometryColumns.getColumnName());
		TestCase.assertEquals(geometryType,
				queryGeometryColumns.getGeometryType());
		TestCase.assertEquals(contents.getSrsId(),
				queryGeometryColumns.getSrsId());
		TestCase.assertEquals(z, queryGeometryColumns.getZ().intValue());
		TestCase.assertEquals(m, queryGeometryColumns.getM().intValue());
		TestCase.assertEquals(contents.getId(), queryGeometryColumns
				.getContents().getId());
		TestCase.assertEquals(contents.getSrsId(), queryGeometryColumns
				.getSrs().getId());

	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao dao = geoPackage.getGeometryColumnsDao();
		List<GeometryColumns> results = dao.queryForAll();

		if (!results.isEmpty()) {

			// Choose random geometry columns
			int random = (int) (Math.random() * results.size());
			GeometryColumns geometryColumns = results.get(random);

			// Delete the geometry columns
			dao.delete(geometryColumns);

			// Verify deleted
			GeometryColumns queryGeometryColumns = dao
					.queryForId(geometryColumns.getId());
			TestCase.assertNull(queryGeometryColumns);

			// Prepared deleted
			results = dao.queryForAll();
			if (!results.isEmpty()) {

				// Choose random geometry columns
				random = (int) (Math.random() * results.size());
				geometryColumns = results.get(random);

				// Find which geometry columns to delete
				QueryBuilder<GeometryColumns, GeometryColumnsKey> qb = dao
						.queryBuilder();
				qb.where().eq(GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME,
						geometryColumns.getGeometryType().getName());
				PreparedQuery<GeometryColumns> query = qb.prepare();
				List<GeometryColumns> queryResults = dao.query(query);
				int count = queryResults.size();

				// Delete
				DeleteBuilder<GeometryColumns, GeometryColumnsKey> db = dao
						.deleteBuilder();
				db.where().eq(GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME,
						geometryColumns.getGeometryType().getName());
				PreparedDelete<GeometryColumns> deleteQuery = db.prepare();
				int deleted = dao.delete(deleteQuery);

				TestCase.assertEquals(count, deleted);

			}
		}
	}

}
