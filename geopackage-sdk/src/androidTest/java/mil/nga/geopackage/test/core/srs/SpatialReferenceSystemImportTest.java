package mil.nga.geopackage.test.core.srs;

import java.sql.SQLException;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Spatial Reference System from an imported database
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public SpatialReferenceSystemImportTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		SpatialReferenceSystemUtils.testRead(geoPackage, null);

	}

	/**
	 * Test reading using the SQL/MM view
	 */
	public void testSqlMmRead() {

		try {
			geoPackage.getSpatialReferenceSystemSqlMmDao();
			fail("No exception was thrown when the SQL/MM view was not expected to exist");
		} catch (GeoPackageException e) {
			// Expected
		}

	}

	/**
	 * Test reading using the SF/SQL view
	 */
	public void testSfSqlRead() {

		try {
			geoPackage.getSpatialReferenceSystemSfSqlDao();
			fail("No exception was thrown when the SF/SQL view was not expected to exist");
		} catch (GeoPackageException e) {
			// Expected
		}

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	public void testUpdate() throws SQLException {

		SpatialReferenceSystemUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	public void testCreate() throws SQLException {

		SpatialReferenceSystemUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	public void testDelete() throws SQLException {

		SpatialReferenceSystemUtils.testDelete(geoPackage);

	}

	/**
	 * Test cascade deleting
	 * 
	 * @throws SQLException
	 */
	public void testDeleteCascade() throws SQLException {

		SpatialReferenceSystemUtils.testDeleteCascade(geoPackage);

	}

}
