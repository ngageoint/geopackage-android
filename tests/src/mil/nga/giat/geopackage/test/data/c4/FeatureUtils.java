package mil.nga.giat.geopackage.test.data.c4;

import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.data.c4.FeatureDao;

/**
 * Features Utility test methods
 * 
 * @author osbornb
 */
public class FeatureUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();
		List<GeometryColumns> results = geometryColumnsDao.queryForAll();

		for (GeometryColumns geometryColumns : results) {

			// Test the get feature DAO methods
			FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
			TestCase.assertNotNull(dao);
			dao = geoPackage.getFeatureDao(geometryColumns.getContents());
			TestCase.assertNotNull(dao);
			dao = geoPackage.getFeatureDao(geometryColumns.getTableName());
			TestCase.assertNotNull(dao);

			TestCase.assertNotNull(dao.getDb());
			TestCase.assertEquals(geometryColumns.getId(), dao
					.getGeometryColumns().getId());
			TestCase.assertEquals(geometryColumns.getTableName(),
					dao.getTableName());
			TestCase.assertEquals(geometryColumns.getColumnName(),
					dao.getGeometryColumnName());

			String[] columns = dao.getColumns();
			int geomIndex = dao.getGeometryColumnIndex();
			TestCase.assertTrue(geomIndex >= 0 && geomIndex < columns.length);
			TestCase.assertEquals(geometryColumns.getColumnName(),
					columns[geomIndex]);

			// TODO
		}

	}

	/**
	 * Test update
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testUpdate(GeoPackage geoPackage) throws SQLException {

		// TODO

	}

	/**
	 * Test create
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage) throws SQLException {

		// TODO

	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		// TODO

	}

}
