package mil.nga.giat.geopackage.test.tiles.matrixset;

import java.sql.SQLException;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageActivity;
import mil.nga.giat.geopackage.test.TestSetupTeardown;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Test Tile Matrix Set from a created database
 * 
 * @author osbornb
 */
public class TileMatrixSetCreateTest extends
		ActivityInstrumentationTestCase2<GeoPackageActivity> {

	/**
	 * GeoPackage activity
	 */
	private Activity activity = null;

	/**
	 * GeoPackage
	 */
	private GeoPackage geoPackage = null;

	/**
	 * Constructor
	 */
	public TileMatrixSetCreateTest() {
		super(GeoPackageActivity.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Set the activity
		activity = getActivity();

		// Create the database
		geoPackage = TestSetupTeardown.setUpCreate(activity, false, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void tearDown() throws Exception {

		// Tear down the create database
		TestSetupTeardown.tearDownCreate(activity, geoPackage);

		super.tearDown();
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
