package mil.nga.giat.geopackage.test;

import java.io.File;
import java.sql.SQLException;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageActivity;
import mil.nga.giat.geopackage.GeoPackageFactory;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.util.GeoPackageException;
import mil.nga.giat.geopackage.util.GeoPackageFileUtils;
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
	 * Import database name
	 */
	private static final String IMPORT_DB_NAME = "import_db";

	/**
	 * Import database file name, located in the test assets
	 */
	private static final String IMPORT_DB_FILE_NAME = IMPORT_DB_NAME + "."
			+ TestConstants.GEO_PACKAGE_EXTENSION;

	/**
	 * Import corrupt database name
	 */
	private static final String IMPORT_CORRUPT_DB_NAME = "import_db_corrupt";

	/**
	 * Import corrupt database file name, located in the test assets
	 */
	private static final String IMPORT_CORRUPT_DB_FILE_NAME = IMPORT_CORRUPT_DB_NAME
			+ "." + TestConstants.GEO_PACKAGE_EXTENSION;

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
		GeoPackageManager manager = GeoPackageFactory.getManager(activity);
		manager.delete(TEST_DB_NAME);
		manager.delete(IMPORT_DB_NAME);
		manager.delete(IMPORT_CORRUPT_DB_NAME);
	}

	/**
	 * Test creating and deleting a database
	 */
	public void testCreateOpenDelete() {

		GeoPackageManager manager = GeoPackageFactory.getManager(activity);

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
	 * Test importing a database from a GeoPackage file
	 * 
	 * @throws GeoPackageException
	 */
	public void testImport() throws GeoPackageException {

		GeoPackageManager manager = GeoPackageFactory.getManager(activity);

		// Verify does not exist
		assertFalse("Database already exists", manager.exists(IMPORT_DB_NAME));
		assertFalse("Database already returned in the set", manager
				.databaseSet().contains(IMPORT_DB_NAME));

		// Copy the test db file from assets to the internal storage
		TestUtils.copyAssetFileToInternalStorage(activity, testContext,
				IMPORT_DB_FILE_NAME);

		// Import
		String importLocation = TestUtils.getAssetFileInternalStorageLocation(
				activity, IMPORT_DB_FILE_NAME);
		File importFile = new File(importLocation);
		assertTrue("Database not imported",
				manager.importGeoPackage(importFile));
		assertTrue("Database does not exist", manager.exists(IMPORT_DB_NAME));
		assertTrue("Database not returned in the set", manager.databaseSet()
				.contains(IMPORT_DB_NAME));

		// Open
		GeoPackage geoPackage = manager.open(IMPORT_DB_NAME);
		assertNotNull("Failed to open database", geoPackage);
		geoPackage.close();

		// Attempt to import again
		try {
			manager.importGeoPackage(importFile);
			fail("Importing database again did not cause exception");
		} catch (GeoPackageException e) {
			// expected
		}

		// Import with override
		assertTrue("Database not imported",
				manager.importGeoPackage(importFile, true));

		// Open
		geoPackage = manager.open(IMPORT_DB_NAME);
		assertNotNull("Failed to open database", geoPackage);
		geoPackage.close();

		// Delete
		assertTrue("Database not deleted", manager.delete(IMPORT_DB_NAME));
		assertFalse("Database exists after delete",
				manager.exists(IMPORT_DB_NAME));
		assertFalse("Database returned in the set after delete", manager
				.databaseSet().contains(IMPORT_DB_NAME));

		// Try to import file with no extension
		String noFileExtension = TestUtils.getAssetFileInternalStorageLocation(
				activity, IMPORT_DB_NAME);
		try {
			manager.importGeoPackage(new File(noFileExtension));
			fail("GeoPackage without extension did not throw an exception");
		} catch (GeoPackageException e) {
			// Expected
		}

		// Try to import file with invalid extension
		String invalidFileExtension = TestUtils
				.getAssetFileInternalStorageLocation(activity, IMPORT_DB_NAME
						+ ".invalid");
		try {
			manager.importGeoPackage(new File(invalidFileExtension));
			fail("GeoPackage with invalid extension did not throw an exception");
		} catch (GeoPackageException e) {
			// Expected
		}

		// Try to import corrupt database
		TestUtils.copyAssetFileToInternalStorage(activity, testContext,
				IMPORT_CORRUPT_DB_FILE_NAME);
		String loadCorruptFileLocation = TestUtils
				.getAssetFileInternalStorageLocation(activity,
						IMPORT_CORRUPT_DB_FILE_NAME);
		File loadCorruptFile = new File(loadCorruptFileLocation);
		try {
			manager.importGeoPackage(loadCorruptFile);
			fail("Corrupted GeoPackage did not throw an exception");
		} catch (GeoPackageException e) {
			// Expected
		}

		// Delete the files
		assertTrue("Import file could not be deleted", importFile.delete());
		assertTrue("Corrupt Import file could not be deleted",
				loadCorruptFile.delete());
	}

	/**
	 * Test exporting a GeoPackage database to a file
	 * 
	 * @throws GeoPackageException
	 * @throws SQLException
	 */
	public void testExport() throws GeoPackageException, SQLException {

		GeoPackageManager manager = GeoPackageFactory.getManager(activity);

		// Verify does not exist
		assertFalse("Database already exists", manager.exists(TEST_DB_NAME));
		assertFalse("Database already returned in the set", manager
				.databaseSet().contains(TEST_DB_NAME));

		// Create
		assertTrue("Database failed to create", manager.create(TEST_DB_NAME));
		assertTrue("Database does not exist", manager.exists(TEST_DB_NAME));
		assertTrue("Database not returned in the set", manager.databaseSet()
				.contains(TEST_DB_NAME));

		File exportDirectory = GeoPackageFileUtils.getInternalFile(activity,
				null);

		// Delete previous exported file
		File exportedFile = new File(exportDirectory, TEST_DB_NAME + "."
				+ TestConstants.GEO_PACKAGE_EXTENSION);
		if (exportedFile.exists()) {
			assertTrue("Previous exported file could not be deleted",
					exportedFile.delete());
		}
		assertFalse("Previous exported file still exists",
				exportedFile.exists());

		// Export the GeoPackage
		manager.exportGeoPackage(TEST_DB_NAME, exportDirectory);
		assertTrue("Exported file does not exist", exportedFile.exists());
		assertTrue("Exported file was empty", exportedFile.length() > 0);

		// Import
		TestUtils.copyAssetFileToInternalStorage(activity, testContext,
				IMPORT_DB_FILE_NAME);
		String importLocation = TestUtils.getAssetFileInternalStorageLocation(
				activity, IMPORT_DB_FILE_NAME);
		File importFile = new File(importLocation);
		assertTrue("Database not imported",
				manager.importGeoPackage(importFile));
		assertTrue("Database does not exist", manager.exists(IMPORT_DB_NAME));
		assertTrue("Database not returned in the set", manager.databaseSet()
				.contains(IMPORT_DB_NAME));

		// Export the imported file
		String exportedImportName = "exportedImport";
		File exportedImport = new File(exportDirectory, exportedImportName
				+ "." + TestConstants.GEO_PACKAGE_EXTENSION);
		manager.exportGeoPackage(IMPORT_DB_NAME, exportedImportName,
				exportDirectory);
		assertTrue("Exported import file does not exist",
				exportedImport.exists());
		assertTrue("Exported import file was empty",
				exportedImport.length() > 0);
		assertTrue(
				"Exported import file length is smaller than the original import file size",
				importFile.length() <= exportedImport.length());

		// Import the exported again, using override
		assertTrue("Database not imported",
				manager.importGeoPackage(IMPORT_DB_NAME, exportedImport, true));

		// Open and query
		GeoPackage geoPackage = manager.open(IMPORT_DB_NAME);
		assertNotNull("Failed to open database", geoPackage);
		assertTrue("Failed to query from imported exported database",
				geoPackage.getSpatialReferenceSystemDao().queryForAll().size() > 0);
		geoPackage.close();

		// Delete the test database
		assertTrue("Database not deleted", manager.delete(TEST_DB_NAME));
		assertFalse("Database exists after delete",
				manager.exists(TEST_DB_NAME));
		assertFalse("Database returned in the set after delete", manager
				.databaseSet().contains(TEST_DB_NAME));

		// Delete the import database
		assertTrue("Database not deleted", manager.delete(IMPORT_DB_NAME));
		assertFalse("Database exists after delete",
				manager.exists(IMPORT_DB_NAME));
		assertFalse("Database returned in the set after delete", manager
				.databaseSet().contains(IMPORT_DB_NAME));

		// Delete the files
		assertTrue("Exported file could not be deleted", exportedFile.delete());
		assertTrue("Exported import file could not be deleted",
				exportedImport.delete());
		assertTrue("Import file could not be deleted", importFile.delete());
	}
}
