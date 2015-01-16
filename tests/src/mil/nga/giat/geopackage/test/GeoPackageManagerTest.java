package mil.nga.giat.geopackage.test;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageActivity;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Test GeoPackage Manager methods
 * 
 * @author osbornb
 */
public class GeoPackageManagerTest extends
		ActivityInstrumentationTestCase2<GeoPackageActivity> {

	/**
	 * Test database name
	 */
	private static final String TEST_DB_NAME = "test_db";

	/**
	 * Load database name
	 */
	private static final String LOAD_DB_NAME = "load_db";

	/**
	 * Load database file name, located in the test assets
	 */
	private static final String LOAD_DB_FILE_NAME = LOAD_DB_NAME + ".gpkg";

	/**
	 * Load corrupt database name
	 */
	private static final String LOAD_CORRUPT_DB_NAME = "load_db_corrupt";

	/**
	 * Load corrupt database file name, located in the test assets
	 */
	private static final String LOAD_CORRUPT_DB_FILE_NAME = LOAD_CORRUPT_DB_NAME
			+ ".gpkg";

	/**
	 * GeoPackage activity
	 */
	private Activity activity = null;

	/**
	 * GeoPackage test context
	 */
	private Context testContext = null;

	/**
	 * Constructor
	 */
	public GeoPackageManagerTest() {
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

		// Delete existing test databases
		GeoPackageManager manager = new GeoPackageManager(activity);
		manager.delete(TEST_DB_NAME);
		manager.delete(LOAD_DB_NAME);
		manager.delete(LOAD_CORRUPT_DB_NAME);
	}

	/**
	 * Test creating and deleting a database
	 */
	public void testCreateOpenDelete() {

		GeoPackageManager manager = new GeoPackageManager(activity);

		// Verify does not exist
		assertFalse("Database already exists", manager.exists(TEST_DB_NAME));
		assertFalse("Database already returned in the set", manager
				.databaseSet().contains(TEST_DB_NAME));

		// Create
		assertTrue("Database failed to create", manager.create(TEST_DB_NAME));
		assertTrue("Database does not exist", manager.exists(TEST_DB_NAME));
		assertTrue("Database not returned in the set", manager.databaseSet()
				.contains(TEST_DB_NAME));

		// Open
		GeoPackage geoPackage = manager.open(TEST_DB_NAME);
		assertNotNull("Failed to open database", geoPackage);
		geoPackage.close();

		// Delete
		assertTrue("Database not deleted", manager.delete(TEST_DB_NAME));
		assertFalse("Database exists after delete",
				manager.exists(TEST_DB_NAME));
		assertFalse("Database returned in the set after delete", manager
				.databaseSet().contains(TEST_DB_NAME));
	}

	/**
	 * Test loading a database from a GeoPackage file
	 * 
	 * @throws GeoPackageException
	 */
	public void testLoad() throws GeoPackageException {

		GeoPackageManager manager = new GeoPackageManager(activity);

		// Verify does not exist
		assertFalse("Database already exists", manager.exists(LOAD_DB_NAME));
		assertFalse("Database already returned in the set", manager
				.databaseSet().contains(LOAD_DB_NAME));

		// Copy the test db file from assets to the internal storage
		TestUtils.copyAssetFileToInternalStorage(activity, testContext,
				LOAD_DB_FILE_NAME);

		// Load
		String loadFile = TestUtils.getAssetFileInternalStorageLocation(
				activity, LOAD_DB_FILE_NAME);
		assertTrue("Database not loaded", manager.load(loadFile));
		assertTrue("Database does not exist", manager.exists(LOAD_DB_NAME));
		assertTrue("Database not returned in the set", manager.databaseSet()
				.contains(LOAD_DB_NAME));

		// Open
		GeoPackage geoPackage = manager.open(LOAD_DB_NAME);
		assertNotNull("Failed to open database", geoPackage);
		geoPackage.close();

		// Attempt to load again
		try {
			manager.load(loadFile);
			fail("Loading database again did not cause exception");
		} catch (GeoPackageException e) {
			// expected
		}

		// Load with override
		assertTrue("Database not loaded", manager.load(loadFile, true));

		// Open
		geoPackage = manager.open(LOAD_DB_NAME);
		assertNotNull("Failed to open database", geoPackage);
		geoPackage.close();

		// Delete
		assertTrue("Database not deleted", manager.delete(LOAD_DB_NAME));
		assertFalse("Database exists after delete",
				manager.exists(LOAD_DB_NAME));
		assertFalse("Database returned in the set after delete", manager
				.databaseSet().contains(LOAD_DB_NAME));

		// Try to load file with no extension
		String noFileExtension = TestUtils.getAssetFileInternalStorageLocation(
				activity, LOAD_DB_NAME);
		try {
			manager.load(noFileExtension);
			fail("GeoPackage without extension did not throw an exception");
		} catch (GeoPackageException e) {
			// Expected
		}

		// Try to load file with invalid extension
		String invalidFileExtension = TestUtils
				.getAssetFileInternalStorageLocation(activity, LOAD_DB_NAME
						+ ".invalid");
		try {
			manager.load(invalidFileExtension);
			fail("GeoPackage with invalid extension did not throw an exception");
		} catch (GeoPackageException e) {
			// Expected
		}

		// Try to load corrupt database
		TestUtils.copyAssetFileToInternalStorage(activity, testContext,
				LOAD_CORRUPT_DB_FILE_NAME);
		String loadCorruptFile = TestUtils.getAssetFileInternalStorageLocation(
				activity, LOAD_CORRUPT_DB_FILE_NAME);
		try {
			manager.load(loadCorruptFile);
			fail("Corrupted GeoPackage did not throw an exception");
		} catch (GeoPackageException e) {
			// Expected
		}

	}

}
