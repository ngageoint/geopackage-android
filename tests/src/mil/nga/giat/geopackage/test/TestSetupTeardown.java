package mil.nga.giat.geopackage.test;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c2.Contents;
import mil.nga.giat.geopackage.data.c2.ContentsDao;
import mil.nga.giat.geopackage.data.c2.ContentsDataType;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.data.c3.GeometryType;
import mil.nga.giat.geopackage.factory.GeoPackageFactory;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.app.Activity;
import android.content.Context;

/**
 * Test setup and teardown methods for preparing and cleaning the database with
 * data
 * 
 * @author osbornb
 */
public class TestSetupTeardown {

	public static final int CREATE_SRS_COUNT = 3;

	public static final int CREATE_CONTENTS_COUNT = 3;

	public static final int CREATE_GEOMETRY_COLUMNS_COUNT = 3;

	/**
	 * Set up the create database
	 * 
	 * @param activity
	 * @param features
	 * @param tiles
	 * @return
	 * @throws SQLException
	 */
	public static GeoPackage setUpCreate(Activity activity, boolean features,
			boolean tiles) throws SQLException {

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

		if (features) {
			setUpCreateFeatures(geoPackage);
		}

		return geoPackage;
	}

	/**
	 * Set up create for features test
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void setUpCreateFeatures(GeoPackage geoPackage)
			throws SQLException {

		// Get existing SRS objects
		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();

		SpatialReferenceSystem epsgSrs = srsDao.queryForId(4326);
		SpatialReferenceSystem undefinedCartesianSrs = srsDao.queryForId(-1);
		SpatialReferenceSystem undefinedGeographicSrs = srsDao.queryForId(0);

		TestCase.assertNotNull(undefinedGeographicSrs);
		TestCase.assertNotNull(epsgSrs);

		// Create the Geometry Columns table
		geoPackage.createGeometryColumnsTable();

		// Create new Contents
		ContentsDao contentsDao = geoPackage.getContentsDao();

		Contents point2dContents = new Contents();
		point2dContents.setTableName("point2d");
		point2dContents.setDataType(ContentsDataType.FEATURES);
		point2dContents.setIdentifier("point2d");
		// point2dContents.setDescription("");
		point2dContents.setLastChange(new Date());
		point2dContents.setMinX(-180.0);
		point2dContents.setMinY(-90.0);
		point2dContents.setMaxX(180.0);
		point2dContents.setMaxY(90.0);
		point2dContents.setSrs(epsgSrs);
		contentsDao.create(point2dContents);

		Contents polygon2dContents = new Contents();
		polygon2dContents.setTableName("polygon2d");
		polygon2dContents.setDataType(ContentsDataType.FEATURES);
		polygon2dContents.setIdentifier("polygon2d");
		// polygon2dContents.setDescription("");
		polygon2dContents.setLastChange(new Date());
		polygon2dContents.setMinX(0.0);
		polygon2dContents.setMinY(0.0);
		polygon2dContents.setMaxX(10.0);
		polygon2dContents.setMaxY(10.0);
		polygon2dContents.setSrs(undefinedGeographicSrs);
		contentsDao.create(polygon2dContents);

		Contents point3dContents = new Contents();
		point3dContents.setTableName("point3d");
		point3dContents.setDataType(ContentsDataType.FEATURES);
		point3dContents.setIdentifier("point3d");
		// point3dContents.setDescription("");
		point3dContents.setLastChange(new Date());
		point3dContents.setMinX(-180.0);
		point3dContents.setMinY(-90.0);
		point3dContents.setMaxX(180.0);
		point3dContents.setMaxY(90.0);
		point3dContents.setSrs(undefinedCartesianSrs);
		contentsDao.create(point3dContents);

		// Create new Geometry Columns
		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();

		GeometryColumns point2dGeometryColumns = new GeometryColumns();
		point2dGeometryColumns.setContents(point2dContents);
		point2dGeometryColumns.setColumnName("geom");
		point2dGeometryColumns.setGeometryType(GeometryType.POINT);
		point2dGeometryColumns.setSrs(point2dContents.getSrs());
		point2dGeometryColumns.setZ(0);
		point2dGeometryColumns.setM(0);
		geometryColumnsDao.create(point2dGeometryColumns);

		GeometryColumns polygon2dGeometryColumns = new GeometryColumns();
		polygon2dGeometryColumns.setContents(polygon2dContents);
		polygon2dGeometryColumns.setColumnName("geom");
		polygon2dGeometryColumns.setGeometryType(GeometryType.POLYGON);
		polygon2dGeometryColumns.setSrs(polygon2dContents.getSrs());
		polygon2dGeometryColumns.setZ(0);
		polygon2dGeometryColumns.setM(0);
		geometryColumnsDao.create(polygon2dGeometryColumns);

		GeometryColumns point3dGeometryColumns = new GeometryColumns();
		point3dGeometryColumns.setContents(point3dContents);
		point3dGeometryColumns.setColumnName("geom");
		point3dGeometryColumns.setGeometryType(GeometryType.POINT);
		point3dGeometryColumns.setSrs(point3dContents.getSrs());
		point3dGeometryColumns.setZ(1);
		point3dGeometryColumns.setM(0);
		geometryColumnsDao.create(point3dGeometryColumns);
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
	 */
	public static GeoPackage setUpImport(Activity activity, Context testContext) {

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

}
