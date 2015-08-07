package mil.nga.geopackage.test.extension;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.test.TestSetupTeardown;

/**
 * Test Extensions from a created database
 * 
 * @author osbornb
 */
public class ExtensionsCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ExtensionsCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		ExtensionsUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_EXTENSIONS_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	public void testUpdate() throws SQLException {

		ExtensionsUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	public void testCreate() throws SQLException {

		ExtensionsUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	public void testDelete() throws SQLException {

		ExtensionsUtils.testDelete(geoPackage);

	}

}
