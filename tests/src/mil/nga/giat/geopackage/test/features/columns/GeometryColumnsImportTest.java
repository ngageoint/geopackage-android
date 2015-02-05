package mil.nga.giat.geopackage.test.features.columns;

import java.sql.SQLException;

import mil.nga.giat.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Geometry Columns from an imported database
 * 
 * @author osbornb
 */
public class GeometryColumnsImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public GeometryColumnsImportTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		GeometryColumnsUtils.testRead(geoPackage, null);

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
