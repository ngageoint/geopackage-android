package mil.nga.geopackage.test.tiles.matrixset;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.test.TestSetupTeardown;

/**
 * Test Tile Matrix Set from a created database
 * 
 * @author osbornb
 */
public class TileMatrixSetCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public TileMatrixSetCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		TileMatrixSetUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_TILE_MATRIX_SET_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	public void testUpdate() throws SQLException {

		TileMatrixSetUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	public void testCreate() throws SQLException {

		TileMatrixSetUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	public void testDelete() throws SQLException {

		TileMatrixSetUtils.testDelete(geoPackage);

	}

}
