package mil.nga.giat.geopackage.test.data.c4;

import java.sql.SQLException;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageActivity;
import mil.nga.giat.geopackage.test.TestSetupTeardown;
import mil.nga.giat.geopackage.test.TestUtils;
import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Test Features from an imported database
 * 
 * @author osbornb
 */
public class FeatureImportTest extends
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
	public FeatureImportTest() {
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
		geoPackage = TestSetupTeardown.setUpImport(activity, testContext);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void tearDown() throws Exception {

		// Tear down the import database
		TestSetupTeardown.tearDownImport(activity, geoPackage);

		super.tearDown();
	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws SQLException {

		FeatureUtils.testRead(geoPackage);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 */
	public void testUpdate() throws SQLException {

		FeatureUtils.testUpdate(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 */
	public void testCreate() throws SQLException {

		FeatureUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 */
	public void testDelete() throws SQLException {

		FeatureUtils.testDelete(geoPackage);

	}

}
