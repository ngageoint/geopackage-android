package mil.nga.geopackage.test.tiles.matrix;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.test.TestSetupTeardown;

/**
 * Test Tile Matrix from a created database
 * 
 * @author osbornb
 */
public class TileMatrixCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public TileMatrixCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testRead() throws SQLException {

		TileMatrixUtils.testRead(geoPackage,
				TestSetupTeardown.CREATE_TILE_MATRIX_COUNT);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testUpdate() throws SQLException {

		TileMatrixUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testCreate() throws SQLException {

		TileMatrixUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testDelete() throws SQLException {

		TileMatrixUtils.testDelete(geoPackage);

	}

}
