package mil.nga.geopackage.test.tiles.matrixset;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Tile Matrix Set from an imported database
 * 
 * @author osbornb
 */
public class TileMatrixSetImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public TileMatrixSetImportTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		TileMatrixSetUtils.testRead(geoPackage, null);

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
