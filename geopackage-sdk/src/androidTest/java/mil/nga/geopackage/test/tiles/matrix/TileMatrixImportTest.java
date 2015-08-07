package mil.nga.geopackage.test.tiles.matrix;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Tile Matrix from an imported database
 * 
 * @author osbornb
 */
public class TileMatrixImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public TileMatrixImportTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		TileMatrixUtils.testRead(geoPackage, null);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	public void testUpdate() throws SQLException {

		TileMatrixUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	public void testCreate() throws SQLException {

		TileMatrixUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	public void testDelete() throws SQLException {

		TileMatrixUtils.testDelete(geoPackage);

	}

}
