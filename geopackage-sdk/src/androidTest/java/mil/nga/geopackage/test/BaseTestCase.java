package mil.nga.geopackage.test;

import mil.nga.geopackage.GeoPackageActivity;
import mil.nga.geopackage.db.metadata.GeoPackageMetadataDb;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Abstract Base Test Case
 * 
 * @author osbornb
 */
public abstract class BaseTestCase extends
		ActivityInstrumentationTestCase2<GeoPackageActivity> {

	/**
	 * Activity
	 */
	protected Activity activity = null;

	/**
	 * Test context
	 */
	protected Context testContext = null;

	/**
	 * Constructor
	 */
	public BaseTestCase() {
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

        activity.deleteDatabase(GeoPackageMetadataDb.DATABASE_NAME);
	}

}
