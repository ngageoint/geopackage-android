package mil.nga.giat.geopackage.test.data.c1;

import java.io.File;
import java.sql.SQLException;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageActivity;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.test.TestConstants;
import mil.nga.giat.geopackage.test.TestUtils;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Test Spatial Reference System from a loaded database (C.1.
 * gpkg_spatial_ref_sys)
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemLoadTest extends
		ActivityInstrumentationTestCase2<GeoPackageActivity> {

	/**
	 * Import database name
	 */
	private static final String IMPORT_DB_NAME = "import_db";

	/**
	 * Import database file name, located in the test assets
	 */
	private static final String IMPORT_DB_FILE_NAME = IMPORT_DB_NAME + "."
			+ TestConstants.GEO_PACKAGE_EXTENSION;

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
	public SpatialReferenceSystemLoadTest() {
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
		testContext = activity
				.createPackageContext("mil.nga.giat.geopackage.test",
						Context.CONTEXT_IGNORE_SECURITY);

		GeoPackageManager manager = new GeoPackageManager(activity);

		// Delete
		manager.delete(IMPORT_DB_NAME);

		// Copy the test db file from assets to the internal storage
		TestUtils.copyAssetFileToInternalStorage(activity, testContext,
				IMPORT_DB_FILE_NAME);

		// Import
		String importLocation = TestUtils.getAssetFileInternalStorageLocation(
				activity, IMPORT_DB_FILE_NAME);
		manager.importGeoPackage(new File(importLocation));

		// Open
		geoPackage = manager.open(IMPORT_DB_NAME);
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
		manager.delete(IMPORT_DB_NAME);

		super.tearDown();
	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 */
	public void testRead() throws GeoPackageException, SQLException {

		SpatialReferenceSystemUtils.testRead(geoPackage, 4);

	}

}
