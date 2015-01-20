package mil.nga.giat.geopackage.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageFactory;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c2.Contents;
import mil.nga.giat.geopackage.data.c2.ContentsDao;
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
	 * @throws SQLException 
	 */
	public static GeoPackage setUpCreate(Activity activity)
			throws GeoPackageException, SQLException {

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

		SpatialReferenceSystemDao srsDao = geoPackage.getSpatialReferenceSystemDao();
		
		SpatialReferenceSystem srs = new SpatialReferenceSystem();
		srs.setSrsName("Undefined geographic SRS");
		srs.setSrsId(0);
		srs.setOrganization("NONE");
		srs.setOrganizationCoordsysId(0);
		srs.setDefinition("undefined");
		srs.setDescription("undefined geographic coordinate reference system");
		srsDao.create(srs);
		
		SpatialReferenceSystem srs2 = new SpatialReferenceSystem();
		srs2.setSrsName("WGS 84 geodetic");
		srs2.setSrsId(4326);
		srs2.setOrganization("EPSG");
		srs2.setOrganizationCoordsysId(4326);
		srs2.setDefinition("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326”]]");
		srs2.setDescription("longitude/latitude coordinates in decimal degrees on the WGS 84 spheroid");
		srsDao.create(srs2);
		
		ContentsDao contentsDao = geoPackage.getContentsDao();
		
		Contents contents = new Contents();
		contents.setTableName("linestring2d");
		contents.setDataType("features");
		contents.setIdentifier("linestring2d");
		//contents.setDescription("");
		contents.setLastChange(new Date());
		contents.setMinX(-180.0);
		contents.setMinY(-90.0);
		contents.setMaxX(180.0);
		contents.setMaxY(90.0);
		contents.setSrs(srs2);
		contentsDao.create(contents);
		
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
