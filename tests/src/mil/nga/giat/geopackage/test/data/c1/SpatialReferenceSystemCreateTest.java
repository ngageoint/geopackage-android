package mil.nga.giat.geopackage.test.data.c1;

import java.sql.SQLException;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageActivity;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Test Spatial Reference System from a created database (C.1.
 * gpkg_spatial_ref_sys)
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemCreateTest extends
		ActivityInstrumentationTestCase2<GeoPackageActivity> {

	/**
	 * Test database name
	 */
	private static final String TEST_DB_NAME = "test_db";

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
	public SpatialReferenceSystemCreateTest() {
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

		GeoPackageManager manager = new GeoPackageManager(activity);

		// Delete
		manager.delete(TEST_DB_NAME);

		// Create
		manager.create(TEST_DB_NAME);

		// Open
		geoPackage = manager.open(TEST_DB_NAME);
		assertNotNull("Failed to open database", geoPackage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void tearDown() throws Exception {

		// Close
		if (geoPackage != null) {
			geoPackage.close();
		}

		// Delete
		GeoPackageManager manager = new GeoPackageManager(activity);
		manager.delete(TEST_DB_NAME);

		super.tearDown();
	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws GeoPackageException, SQLException {

		SpatialReferenceSystemUtils.testRead(geoPackage, 0);

	}

	/**
	 * Test reading using the SF/SQL view
	 * 
	 * @throws SQLException
	 */
	public void testSfSqlRead() throws GeoPackageException, SQLException {

		SpatialReferenceSystemUtils.testSfSqlRead(geoPackage, 0);

	}

}
