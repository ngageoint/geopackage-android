package mil.nga.giat.geopackage.test.data.c1;

import java.sql.SQLException;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageActivity;
import mil.nga.giat.geopackage.test.TestUtils;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Test Spatial Reference System from an imported database (C.1.
 * gpkg_spatial_ref_sys)
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemImportTest extends
		ActivityInstrumentationTestCase2<GeoPackageActivity> {

	/**
	 * GeoPackage activity
	 */
	private Activity activity = null;

	/**
	 * GeoPackage test context
	 */
	private Context testContext = null;

	/**
	 * GeoPackage
	 */
	private GeoPackage geoPackage = null;

	/**
	 * Constructor
	 */
	public SpatialReferenceSystemImportTest() {
		super(GeoPackageActivity.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Set the activity and test context
		activity = getActivity();
		testContext = TestUtils.getTestContext(activity);

		// Import the database
		geoPackage = TestUtils.setUpImport(activity, testContext);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void tearDown() throws Exception {

		// Tear down the import database
		TestUtils.tearDownImport(activity, geoPackage);

		super.tearDown();
	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		SpatialReferenceSystemUtils.testRead(geoPackage, 4);

	}

	/**
	 * Test reading using the SQL/MM view
	 */
	public void testSqlMmRead() {

		try {
			geoPackage.getSpatialReferenceSystemSqlMmDao();
			fail("No exception was thrown when the SQL/MM view was not expected to exist");
		} catch (GeoPackageException e) {
			// Expected
		}

	}

	/**
	 * Test reading using the SF/SQL view
	 */
	public void testSfSqlRead() {

		try {
			geoPackage.getSpatialReferenceSystemSfSqlDao();
			fail("No exception was thrown when the SF/SQL view was not expected to exist");
		} catch (GeoPackageException e) {
			// Expected
		}

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	public void testUpdate() throws SQLException {

		SpatialReferenceSystemUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	public void testCreate() throws SQLException {

		SpatialReferenceSystemUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	public void testDelete() throws SQLException {

		SpatialReferenceSystemUtils.testDelete(geoPackage);

	}

	/**
	 * Test cascade deleting
	 * 
	 * @throws SQLException
	 */
	public void testDeleteCascade() throws SQLException {

		SpatialReferenceSystemUtils.testDeleteCascade(geoPackage);

	}

}
