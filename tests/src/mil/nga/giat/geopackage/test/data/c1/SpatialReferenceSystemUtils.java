package mil.nga.giat.geopackage.test.data.c1;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemSfSql;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemSfSqlDao;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemSqlMm;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemSqlMmDao;
import mil.nga.giat.geopackage.util.GeoPackageException;

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
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, int expectedResults)
			throws GeoPackageException, SQLException {

		SpatialReferenceSystemDao dao = geoPackage
				.getSpatialReferenceSystemDao();
		List<SpatialReferenceSystem> results = dao.queryForAll();
		TestCase.assertEquals(
				"Unexpected number of spatial reference system rows",
				expectedResults, results.size());

		if (!results.isEmpty()) {

			for (SpatialReferenceSystem result : results) {
				TestCase.assertNotNull(result.getSrsName());
				TestCase.assertNotNull(result.getSrsId());
				TestCase.assertNotNull(result.getOrganization());
				TestCase.assertNotNull(result.getOrganizationCoordsysId());
				TestCase.assertNotNull(result.getDefinition());
			}

			int random = (int) (Math.random() * results.size());
			SpatialReferenceSystem srs = results.get(random);

			SpatialReferenceSystem querySrs = dao.queryForId(srs.getSrsId());
			TestCase.assertNotNull(querySrs);
			TestCase.assertEquals(srs.getSrsId(), querySrs.getSrsId());

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
	 * @throws GeoPackageException
	 */
	public static void testSqlMmRead(GeoPackage geoPackage, int expectedResults)
			throws GeoPackageException, SQLException {

		SpatialReferenceSystemSqlMmDao dao = geoPackage
				.getSpatialReferenceSystemSqlMmDao();
		List<SpatialReferenceSystemSqlMm> results = dao.queryForAll();
		TestCase.assertEquals(
				"Unexpected number of spatial reference system rows",
				expectedResults, results.size());

		if (!results.isEmpty()) {

			for (SpatialReferenceSystemSqlMm result : results) {
				TestCase.assertNotNull(result.getSrsName());
				TestCase.assertNotNull(result.getSrsId());
				TestCase.assertNotNull(result.getOrganization());
				TestCase.assertNotNull(result.getOrganizationCoordsysId());
				TestCase.assertNotNull(result.getDefinition());
			}

			int random = (int) (Math.random() * results.size());
			SpatialReferenceSystemSqlMm srs = results.get(random);

			SpatialReferenceSystemSqlMm querySrs = dao.queryForId(srs
					.getSrsId());
			TestCase.assertNotNull(querySrs);
			TestCase.assertEquals(srs.getSrsId(), querySrs.getSrsId());

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
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public static void testSfSqlRead(GeoPackage geoPackage, int expectedResults)
			throws GeoPackageException, SQLException {

		SpatialReferenceSystemSfSqlDao dao = geoPackage
				.getSpatialReferenceSystemSfSqlDao();
		List<SpatialReferenceSystemSfSql> results = dao.queryForAll();
		TestCase.assertEquals(
				"Unexpected number of spatial reference system rows",
				expectedResults, results.size());

		if (!results.isEmpty()) {

			for (SpatialReferenceSystemSfSql result : results) {
				TestCase.assertNotNull(result.getSrid());
				TestCase.assertNotNull(result.getAuthName());
				TestCase.assertNotNull(result.getAuthSrid());
			}

			int random = (int) (Math.random() * results.size());
			SpatialReferenceSystemSfSql srs = results.get(random);

			SpatialReferenceSystemSfSql querySrs = dao
					.queryForId(srs.getSrid());
			TestCase.assertNotNull(querySrs);
			TestCase.assertEquals(srs.getSrid(), querySrs.getSrid());

			List<SpatialReferenceSystemSfSql> querySrsList = dao.queryForEq(
					SpatialReferenceSystemSfSql.COLUMN_AUTH_NAME, srs.getAuthName());
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

}
