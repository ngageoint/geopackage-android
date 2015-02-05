package mil.nga.giat.geopackage.test.features.columns;

import java.sql.SQLException;

import mil.nga.giat.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.giat.geopackage.test.TestSetupTeardown;

/**
 * Test Geometry Columns from a created database
 * 
 * @author osbornb
 */
public class GeometryColumnsCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public GeometryColumnsCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		GeometryColumnsUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_GEOMETRY_COLUMNS_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	public void testUpdate() throws SQLException {

		GeometryColumnsUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	public void testCreate() throws SQLException {

		GeometryColumnsUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	public void testDelete() throws SQLException {

		GeometryColumnsUtils.testDelete(geoPackage);

	}

}
