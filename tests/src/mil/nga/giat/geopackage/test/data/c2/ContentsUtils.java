package mil.nga.giat.geopackage.test.data.c2;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c2.Contents;
import mil.nga.giat.geopackage.data.c2.ContentsDao;
import mil.nga.giat.geopackage.util.GeoPackageException;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

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
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage, int expectedResults)
			throws GeoPackageException, SQLException {

		ContentsDao dao = geoPackage.getContentsDao();
		List<Contents> results = dao.queryForAll();
		TestCase.assertEquals("Unexpected number of contents rows",
				expectedResults, results.size());

		if (!results.isEmpty()) {

			int count = results.size();
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

			int random = (int) (Math.random() * results.size());
			Contents contents = results.get(random);

			Contents queryContents = dao.queryForId(contents.getTableName());
			TestCase.assertNotNull(queryContents);
			TestCase.assertEquals(contents.getTableName(),
					queryContents.getTableName());

			List<Contents> queryContentsList = dao.queryForEq(
					Contents.COLUMN_IDENTIFIER, contents.getIdentifier());
			TestCase.assertNotNull(queryContentsList);
			TestCase.assertEquals(1, queryContentsList.size());
			TestCase.assertEquals(contents.getIdentifier(), queryContentsList
					.get(0).getIdentifier());

			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(Contents.COLUMN_DATA_TYPE, contents.getDataType());
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

}
