package mil.nga.geopackage.schema.columns;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.CreateGeoPackageTestCase;
import mil.nga.geopackage.TestSetupTeardown;

/**
 * Test Data Columns from a created database
 * 
 * @author osbornb
 */
public class DataColumnsCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public DataColumnsCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testRead() throws SQLException {

		DataColumnsUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_DATA_COLUMNS_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testUpdate() throws SQLException {

		DataColumnsUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testCreate() throws SQLException {

		DataColumnsUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testDelete() throws SQLException {

		DataColumnsUtils.testDelete(geoPackage);

	}

}
