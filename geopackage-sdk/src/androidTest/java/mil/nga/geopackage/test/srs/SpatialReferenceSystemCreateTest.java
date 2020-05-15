package mil.nga.geopackage.test.srs;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.test.TestSetupTeardown;

/**
 * Test Spatial Reference System from a created database
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public SpatialReferenceSystemCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testRead() throws SQLException {

		SpatialReferenceSystemUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_SRS_COUNT);

	}

	/**
	 * Test reading using the SQL/MM view
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testSqlMmRead() throws SQLException {

		SpatialReferenceSystemUtils.testSqlMmRead(geoPackage,
				TestSetupTeardown.CREATE_SRS_COUNT);

	}

	/**
	 * Test reading using the SF/SQL view
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testSfSqlRead() throws SQLException {

		SpatialReferenceSystemUtils.testSfSqlRead(geoPackage,
				TestSetupTeardown.CREATE_SRS_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testUpdate() throws SQLException {

		SpatialReferenceSystemUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testCreate() throws SQLException {

		SpatialReferenceSystemUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testDelete() throws SQLException {

		SpatialReferenceSystemUtils.testDelete(geoPackage);

	}

	/**
	 * Test cascade deleting
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testDeleteCascade() throws SQLException {

		SpatialReferenceSystemUtils.testDeleteCascade(geoPackage);

	}

}
