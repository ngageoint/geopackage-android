package mil.nga.giat.geopackage.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageFactory;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.util.GeoPackageException;
import mil.nga.giat.geopackage.util.GeoPackageFileUtils;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Test utility methods
 * 
 * @author osbornb
 */
public class TestUtils {

	/**
	 * Get test context
	 * 
	 * @param activity
	 * @return
	 * @throws NameNotFoundException
	 */
	public static Context getTestContext(Activity activity)
			throws NameNotFoundException {
		return activity.createPackageContext("mil.nga.giat.geopackage.test",
				Context.CONTEXT_IGNORE_SECURITY);
	}

	/**
	 * Set up the create database
	 * 
	 * @param activity
	 * @return
	 * @throws GeoPackageException
	 */
	public static GeoPackage setUpCreate(Activity activity)
			throws GeoPackageException {

		GeoPackageManager manager = GeoPackageFactory.getManager(activity);

		// Delete
		manager.delete(TestConstants.TEST_DB_NAME);

		// Create
		manager.create(TestConstants.TEST_DB_NAME);

		// Open
		GeoPackage geoPackage = manager.open(TestConstants.TEST_DB_NAME);
		if (geoPackage == null) {
			throw new GeoPackageException("Failed to open database");
		}

		return geoPackage;
	}

	/**
	 * Tear down the create database
	 * 
	 * @param activity
	 * @param geoPackage
	 */
	public static void tearDownCreate(Activity activity, GeoPackage geoPackage) {

		// Close
		if (geoPackage != null) {
			geoPackage.close();
		}

		// Delete
		GeoPackageManager manager = GeoPackageFactory.getManager(activity);
		manager.delete(TestConstants.TEST_DB_NAME);
	}

	/**
	 * Set up the import database
	 * 
	 * @param activity
	 * @param testContext
	 * @return
	 * @throws GeoPackageException
	 */
	public static GeoPackage setUpImport(Activity activity, Context testContext)
			throws GeoPackageException {

		GeoPackageManager manager = GeoPackageFactory.getManager(activity);

		// Delete
		manager.delete(TestConstants.IMPORT_DB_NAME);

		// Copy the test db file from assets to the internal storage
		TestUtils.copyAssetFileToInternalStorage(activity, testContext,
				TestConstants.IMPORT_DB_FILE_NAME);

		// Import
		String importLocation = TestUtils.getAssetFileInternalStorageLocation(
				activity, TestConstants.IMPORT_DB_FILE_NAME);
		manager.importGeoPackage(new File(importLocation));

		// Open
		GeoPackage geoPackage = manager.open(TestConstants.IMPORT_DB_NAME);
		if (geoPackage == null) {
			throw new GeoPackageException("Failed to open database");
		}

		return geoPackage;
	}

	/**
	 * Tear down the import database
	 * 
	 * @param activity
	 * @param geoPackage
	 */
	public static void tearDownImport(Activity activity, GeoPackage geoPackage) {

		// Close
		if (geoPackage != null) {
			geoPackage.close();
		}

		// Delete
		GeoPackageManager manager = GeoPackageFactory.getManager(activity);
		manager.delete(TestConstants.IMPORT_DB_NAME);
	}

	/**
	 * Copy the asset file to the internal memory storage
	 * 
	 * @param context
	 * @param assetPath
	 * @throws GeoPackageException
	 */
	public static void copyAssetFileToInternalStorage(Context context,
			Context testContext, String assetPath) throws GeoPackageException {

		String filePath = getAssetFileInternalStorageLocation(context,
				assetPath);
		try {
			copyAssetFile(testContext, assetPath, filePath);
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to copy asset file to internal storage: "
							+ assetPath, e);
		}
	}

	/**
	 * Get the internal storage location of the assest file
	 * 
	 * @param context
	 * @param assetPath
	 * @return
	 */
	public static String getAssetFileInternalStorageLocation(Context context,
			String assetPath) {
		return GeoPackageFileUtils.getInternalFilePath(context, assetPath);
	}

	/**
	 * Copy the asset file to the provided file path
	 * 
	 * @param testContext
	 * @param assetPath
	 * @param filePath
	 * @throws IOException
	 */
	private static void copyAssetFile(Context testContext, String assetPath,
			String filePath) throws IOException {

		InputStream assetFile = testContext.getAssets().open(assetPath);

		OutputStream newFile = new FileOutputStream(filePath);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = assetFile.read(buffer)) > 0) {
			newFile.write(buffer, 0, length);
		}

		// Close the streams
		newFile.flush();
		newFile.close();
		assetFile.close();
	}

}
