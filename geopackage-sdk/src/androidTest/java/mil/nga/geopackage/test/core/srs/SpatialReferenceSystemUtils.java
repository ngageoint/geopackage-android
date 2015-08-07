package mil.nga.geopackage.test.core.srs;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemSfSql;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemSfSqlDao;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemSqlMm;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemSqlMmDao;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.schema.TableColumnKey;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

/**
 * Spatial Reference System Utility test methods
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, Integer expectedResults)
			throws SQLException {

		SpatialReferenceSystemDao dao = geoPackage
				.getSpatialReferenceSystemDao();
		List<SpatialReferenceSystem> results = dao.queryForAll();
		if (expectedResults != null) {
			TestCase.assertEquals(
					"Unexpected number of spatial reference system rows",
					expectedResults.intValue(), results.size());
		}

		if (!results.isEmpty()) {

			// Verify non nulls
			for (SpatialReferenceSystem result : results) {
				TestCase.assertNotNull(result.getSrsName());
				TestCase.assertNotNull(result.getSrsId());
				TestCase.assertNotNull(result.getOrganization());
				TestCase.assertNotNull(result.getOrganizationCoordsysId());
				TestCase.assertNotNull(result.getDefinition());
			}

			// Choose random srs
			int random = (int) (Math.random() * results.size());
			SpatialReferenceSystem srs = results.get(random);

			// Query by id
			SpatialReferenceSystem querySrs = dao.queryForId(srs.getSrsId());
			TestCase.assertNotNull(querySrs);
			TestCase.assertEquals(srs.getSrsId(), querySrs.getSrsId());

			// Query for equal
			List<SpatialReferenceSystem> querySrsList = dao.queryForEq(
					SpatialReferenceSystem.COLUMN_ORGANIZATION_COORDSYS_ID,
					srs.getOrganizationCoordsysId());
			TestCase.assertNotNull(querySrsList);
			TestCase.assertTrue(querySrsList.size() >= 1);
			boolean found = false;
			for (SpatialReferenceSystem querySrsValue : querySrsList) {
				TestCase.assertEquals(srs.getOrganizationCoordsysId(),
						querySrsValue.getOrganizationCoordsysId());
				if (!found) {
					found = srs.getSrsId() == querySrsValue.getSrsId();
				}
			}
			TestCase.assertTrue(found);

			// Query for fields values
			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(SpatialReferenceSystem.COLUMN_DEFINITION,
					srs.getDefinition());
			if (srs.getDescription() != null) {
				fieldValues.put(SpatialReferenceSystem.COLUMN_DESCRIPTION,
						srs.getDescription());
			}
			querySrsList = dao.queryForFieldValues(fieldValues);
			TestCase.assertNotNull(querySrsList);
			TestCase.assertTrue(querySrsList.size() >= 1);
			found = false;
			for (SpatialReferenceSystem querySrsValue : querySrsList) {
				TestCase.assertEquals(srs.getDefinition(),
						querySrsValue.getDefinition());
				if (srs.getDescription() != null) {
					TestCase.assertEquals(srs.getDescription(),
							querySrsValue.getDescription());
				}
				if (!found) {
					found = srs.getSrsId() == querySrsValue.getSrsId();
				}
			}
			TestCase.assertTrue(found);
		}
	}

	/**
	 * Test SQL/MM read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testSqlMmRead(GeoPackage geoPackage, int expectedResults)
			throws SQLException {

		SpatialReferenceSystemSqlMmDao dao = geoPackage
				.getSpatialReferenceSystemSqlMmDao();
		List<SpatialReferenceSystemSqlMm> results = dao.queryForAll();
		TestCase.assertEquals(
				"Unexpected number of spatial reference system rows",
				expectedResults, results.size());

		if (!results.isEmpty()) {

			// Verify non nulls
			for (SpatialReferenceSystemSqlMm result : results) {
				TestCase.assertNotNull(result.getSrsName());
				TestCase.assertNotNull(result.getSrsId());
				TestCase.assertNotNull(result.getOrganization());
				TestCase.assertNotNull(result.getOrganizationCoordsysId());
				TestCase.assertNotNull(result.getDefinition());
			}

			// Choose random srs
			int random = (int) (Math.random() * results.size());
			SpatialReferenceSystemSqlMm srs = results.get(random);

			// Query by id
			SpatialReferenceSystemSqlMm querySrs = dao.queryForId(srs
					.getSrsId());
			TestCase.assertNotNull(querySrs);
			TestCase.assertEquals(srs.getSrsId(), querySrs.getSrsId());

			// Query for equal
			List<SpatialReferenceSystemSqlMm> querySrsList = dao.queryForEq(
					SpatialReferenceSystem.COLUMN_ORGANIZATION_COORDSYS_ID,
					srs.getOrganizationCoordsysId());
			TestCase.assertNotNull(querySrsList);
			TestCase.assertTrue(querySrsList.size() >= 1);
			boolean found = false;
			for (SpatialReferenceSystemSqlMm querySrsValue : querySrsList) {
				TestCase.assertEquals(srs.getOrganizationCoordsysId(),
						querySrsValue.getOrganizationCoordsysId());
				if (!found) {
					found = srs.getSrsId() == querySrsValue.getSrsId();
				}
			}
			TestCase.assertTrue(found);

			// Query for fields values
			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(SpatialReferenceSystem.COLUMN_DEFINITION,
					srs.getDefinition());
			if (srs.getDescription() != null) {
				fieldValues.put(SpatialReferenceSystem.COLUMN_DESCRIPTION,
						srs.getDescription());
			}
			querySrsList = dao.queryForFieldValues(fieldValues);
			TestCase.assertNotNull(querySrsList);
			TestCase.assertTrue(querySrsList.size() >= 1);
			found = false;
			for (SpatialReferenceSystemSqlMm querySrsValue : querySrsList) {
				TestCase.assertEquals(srs.getDefinition(),
						querySrsValue.getDefinition());
				if (srs.getDescription() != null) {
					TestCase.assertEquals(srs.getDescription(),
							querySrsValue.getDescription());
				}
				if (!found) {
					found = srs.getSrsId() == querySrsValue.getSrsId();
				}
			}
			TestCase.assertTrue(found);
		}
	}

	/**
	 * Test SF/SQL read
	 * 
	 * @param geoPackage
	 * @param expectedResults
	 * @throws SQLException
	 */
	public static void testSfSqlRead(GeoPackage geoPackage, int expectedResults)
			throws SQLException {

		SpatialReferenceSystemSfSqlDao dao = geoPackage
				.getSpatialReferenceSystemSfSqlDao();
		List<SpatialReferenceSystemSfSql> results = dao.queryForAll();
		TestCase.assertEquals(
				"Unexpected number of spatial reference system rows",
				expectedResults, results.size());

		if (!results.isEmpty()) {

			// Verify non nulls
			for (SpatialReferenceSystemSfSql result : results) {
				TestCase.assertNotNull(result.getSrid());
				TestCase.assertNotNull(result.getAuthName());
				TestCase.assertNotNull(result.getAuthSrid());
			}

			// Choose random srs
			int random = (int) (Math.random() * results.size());
			SpatialReferenceSystemSfSql srs = results.get(random);

			// Query by id
			SpatialReferenceSystemSfSql querySrs = dao
					.queryForId(srs.getSrid());
			TestCase.assertNotNull(querySrs);
			TestCase.assertEquals(srs.getSrid(), querySrs.getSrid());

			// Query for equal
			List<SpatialReferenceSystemSfSql> querySrsList = dao.queryForEq(
					SpatialReferenceSystemSfSql.COLUMN_AUTH_NAME,
					srs.getAuthName());
			TestCase.assertNotNull(querySrsList);
			TestCase.assertTrue(querySrsList.size() >= 1);
			boolean found = false;
			for (SpatialReferenceSystemSfSql querySrsValue : querySrsList) {
				TestCase.assertEquals(srs.getAuthName(),
						querySrsValue.getAuthName());
				if (!found) {
					found = srs.getSrid() == querySrsValue.getSrid();
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

		SpatialReferenceSystemDao dao = geoPackage
				.getSpatialReferenceSystemDao();
		List<SpatialReferenceSystem> results = dao.queryForAll();

		if (!results.isEmpty()) {

			// Choose random srs
			int random = (int) (Math.random() * results.size());
			SpatialReferenceSystem srs = results.get(random);

			// Update
			String updatedOrganization = "TEST_ORG";
			srs.setOrganization(updatedOrganization);
			dao.update(srs);

			// Verify update
			dao = geoPackage.getSpatialReferenceSystemDao();
			SpatialReferenceSystem updatedSrs = dao.queryForId(srs.getId());
			TestCase.assertEquals(updatedOrganization,
					updatedSrs.getOrganization());

			// Prepared update
			String updatedDescription = "TEST_DESCRIPTION";
			UpdateBuilder<SpatialReferenceSystem, Long> ub = dao
					.updateBuilder();
			ub.updateColumnValue(SpatialReferenceSystem.COLUMN_DESCRIPTION,
					updatedDescription);
			ub.where().ge(SpatialReferenceSystem.COLUMN_ID, 0);
			PreparedUpdate<SpatialReferenceSystem> update = ub.prepare();
			int updated = dao.update(update);

			// Verify prepared update
			results = dao.queryForAll();
			int count = 0;
			for (SpatialReferenceSystem preparedUpdateSrs : results) {
				if (preparedUpdateSrs.getId() >= 0) {
					TestCase.assertEquals(updatedDescription,
							preparedUpdateSrs.getDescription());
					count++;
				}
			}
			TestCase.assertEquals(updated, count);
		}

	}

	/**
	 * Test create
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage) throws SQLException {

		SpatialReferenceSystemDao dao = geoPackage
				.getSpatialReferenceSystemDao();

		// Get current count
		long count = dao.countOf();

		String srsName = "TEST_SRS_NAME";
		long srsId = 123456l;
		String organization = "TEST_ORG";
		int organizationCoordSysId = 123456;
		String definition = "TEST_DEFINITION";
		String description = "TEST_DESCRIPTION";

		// Create new srs
		SpatialReferenceSystem srs = new SpatialReferenceSystem();
		srs.setSrsName(srsName);
		srs.setSrsId(srsId);
		srs.setOrganization(organization);
		srs.setOrganizationCoordsysId(organizationCoordSysId);
		srs.setDefinition(definition);
		srs.setDescription(description);
		dao.create(srs);

		// Verify count
		long newCount = dao.countOf();
		TestCase.assertEquals(count + 1, newCount);

		// Verify saved srs
		SpatialReferenceSystem querySrs = dao.queryForId(srsId);
		TestCase.assertEquals(srsName, querySrs.getSrsName());
		TestCase.assertEquals(srsId, querySrs.getSrsId());
		TestCase.assertEquals(organization, querySrs.getOrganization());
		TestCase.assertEquals(organizationCoordSysId,
				querySrs.getOrganizationCoordsysId());
		TestCase.assertEquals(definition, querySrs.getDefinition());
		TestCase.assertEquals(description, querySrs.getDescription());

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

		SpatialReferenceSystemDao dao = geoPackage
				.getSpatialReferenceSystemDao();
		List<SpatialReferenceSystem> results = dao.queryForAll();

		if (!results.isEmpty()) {

			// Choose random srs
			int random = (int) (Math.random() * results.size());
			SpatialReferenceSystem srs = results.get(random);

			// Save the ids of contents
			List<String> contentsIds = new ArrayList<String>();
			for (Contents contents : srs.getContents()) {
				contentsIds.add(contents.getId());
			}

			// Save the ids of geometry columns
			List<TableColumnKey> geometryColumnsIds = new ArrayList<TableColumnKey>();
			GeometryColumnsDao geometryColumnsDao = geoPackage
					.getGeometryColumnsDao();
			if (geometryColumnsDao.isTableExists()) {
				for (GeometryColumns geometryColumns : srs.getGeometryColumns()) {
					geometryColumnsIds.add(geometryColumns.getId());
				}
			}

			// Delete the srs
			if (cascade) {
				dao.deleteCascade(srs);
			} else {
				dao.delete(srs);
			}

			// Verify deleted
			SpatialReferenceSystem querySrs = dao.queryForId(srs.getSrsId());
			TestCase.assertNull(querySrs);

			// Verify that contents or foreign keys were deleted
			ContentsDao contentsDao = geoPackage.getContentsDao();
			for (String contentsId : contentsIds) {
				Contents queryContents = contentsDao.queryForId(contentsId);
				if (cascade) {
					TestCase.assertNull(queryContents);
				} else {
					TestCase.assertNull(queryContents.getSrs());
				}
			}

			// Verify that geometry columns or foreign keys were deleted
			for (TableColumnKey geometryColumnsId : geometryColumnsIds) {
				GeometryColumns queryGeometryColumns = geometryColumnsDao
						.queryForId(geometryColumnsId);
				if (cascade) {
					TestCase.assertNull(queryGeometryColumns);
				} else {
					TestCase.assertNull(queryGeometryColumns.getSrs());
				}
			}

			// Choose prepared deleted
			results = dao.queryForAll();
			if (!results.isEmpty()) {

				// Choose random srs
				random = (int) (Math.random() * results.size());
				srs = results.get(random);

				// Find which srs to delete and the contents
				QueryBuilder<SpatialReferenceSystem, Long> qb = dao
						.queryBuilder();
				qb.where().eq(SpatialReferenceSystem.COLUMN_ORGANIZATION,
						srs.getOrganization());
				PreparedQuery<SpatialReferenceSystem> query = qb.prepare();
				List<SpatialReferenceSystem> queryResults = dao.query(query);
				int count = queryResults.size();
				contentsIds = new ArrayList<String>();
				geometryColumnsIds = new ArrayList<TableColumnKey>();
				for (SpatialReferenceSystem queryResultsSrs : queryResults) {
					for (Contents contents : queryResultsSrs.getContents()) {
						contentsIds.add(contents.getId());
					}
					if (geometryColumnsDao.isTableExists()) {
						for (GeometryColumns geometryColumns : queryResultsSrs
								.getGeometryColumns()) {
							geometryColumnsIds.add(geometryColumns.getId());
						}
					}
				}

				// Delete
				int deleted;
				if (cascade) {
					deleted = dao.deleteCascade(query);
				} else {
					DeleteBuilder<SpatialReferenceSystem, Long> db = dao
							.deleteBuilder();
					db.where().eq(SpatialReferenceSystem.COLUMN_ORGANIZATION,
							srs.getOrganization());
					PreparedDelete<SpatialReferenceSystem> deleteQuery = db
							.prepare();
					deleted = dao.delete(deleteQuery);
				}
				TestCase.assertEquals(count, deleted);

				// Verify that contents or foreign keys were deleted
				for (String contentsId : contentsIds) {
					Contents queryContents = contentsDao.queryForId(contentsId);
					if (cascade) {
						TestCase.assertNull(queryContents);
					} else {
						TestCase.assertNull(queryContents.getSrs());
					}
				}

				// Verify that geometry columns or foreign keys were deleted
				for (TableColumnKey geometryColumnsId : geometryColumnsIds) {
					GeometryColumns queryGeometryColumns = geometryColumnsDao
							.queryForId(geometryColumnsId);
					if (cascade) {
						TestCase.assertNull(queryGeometryColumns);
					} else {
						TestCase.assertNull(queryGeometryColumns.getSrs());
					}
				}
			}
		}
	}

}
