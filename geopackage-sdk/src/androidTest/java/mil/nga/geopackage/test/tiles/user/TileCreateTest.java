package mil.nga.geopackage.test.tiles.user;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test Tiles from a created database
 * 
 * @author osbornb
 */
public class TileCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public TileCreateTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		TileUtils.testRead(geoPackage);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void testUpdate() throws SQLException, IOException {

		TileUtils.testUpdate(testContext, geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	public void testCreate() throws SQLException {

		TileUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	public void testDelete() throws SQLException {

		TileUtils.testDelete(geoPackage);

	}

	/**
	 * Test getZoomLevel
	 * 
	 * @throws SQLException
	 */
	public void testGetZoomLevel() throws SQLException {

		TileUtils.testGetZoomLevel(geoPackage);

	}

	/**
	 * Test queryByRange
	 * 
	 * @throws SQLException
	 */
	public void testQueryByRange() throws SQLException {

		TileUtils.testQueryByRange(geoPackage);

	}

	/**
	 * Test tileMatrixBoundingBox
	 *
	 * @throws SQLException
	 */
	public void testTileMatrixBoundingBox() throws SQLException {

		TileUtils.testTileMatrixBoundingBox(geoPackage);

	}

	/**
	 * Test testThreadedTileDao
	 *
	 * @throws SQLException
	 */
	public void testThreadedTileDao() throws SQLException {

		TileUtils.testThreadedTileDao(geoPackage);

	}

}
