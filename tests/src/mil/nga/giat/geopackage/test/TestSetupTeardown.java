package mil.nga.giat.geopackage.test;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.contents.ContentsDao;
import mil.nga.giat.geopackage.core.contents.ContentsDataType;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.factory.GeoPackageFactory;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.giat.geopackage.features.user.FeatureTable;
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSetDao;
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

	public static final int CREATE_CONTENTS_COUNT = 4;

	public static final int CREATE_GEOMETRY_COLUMNS_COUNT = 4;

	public static final int CREATE_TILE_MATRIX_SET_COUNT = 1;
	
	public static final int CREATE_TILE_MATRIX_COUNT = 9;

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

		if (tiles) {
			setUpCreateTiles(geoPackage);
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

		TestCase.assertNotNull(epsgSrs);
		TestCase.assertNotNull(undefinedCartesianSrs);
		TestCase.assertNotNull(undefinedGeographicSrs);

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

		Contents lineString3dMContents = new Contents();
		lineString3dMContents.setTableName("lineString3dM");
		lineString3dMContents.setDataType(ContentsDataType.FEATURES);
		lineString3dMContents.setIdentifier("lineString3dM");
		// lineString3dMContents.setDescription("");
		lineString3dMContents.setLastChange(new Date());
		lineString3dMContents.setMinX(-180.0);
		lineString3dMContents.setMinY(-90.0);
		lineString3dMContents.setMaxX(180.0);
		lineString3dMContents.setMaxY(90.0);
		lineString3dMContents.setSrs(undefinedCartesianSrs);

		String geometryColumn = "geometry";

		// Create the feature tables
		FeatureTable point2dTable = TestUtils.buildTable(
				point2dContents.getTableName(), geometryColumn,
				GeometryType.POINT);
		geoPackage.createTable(point2dTable);
		FeatureTable polygon2dTable = TestUtils.buildTable(
				polygon2dContents.getTableName(), geometryColumn,
				GeometryType.POLYGON);
		geoPackage.createTable(polygon2dTable);
		FeatureTable point3dTable = TestUtils.buildTable(
				point3dContents.getTableName(), geometryColumn,
				GeometryType.POINT);
		geoPackage.createTable(point3dTable);
		FeatureTable lineString3dMTable = TestUtils.buildTable(
				lineString3dMContents.getTableName(), geometryColumn,
				GeometryType.LINESTRING);
		geoPackage.createTable(lineString3dMTable);

		// Create the contents
		contentsDao.create(point2dContents);
		contentsDao.create(polygon2dContents);
		contentsDao.create(point3dContents);
		contentsDao.create(lineString3dMContents);

		// Create new Geometry Columns
		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();

		GeometryColumns point2dGeometryColumns = new GeometryColumns();
		point2dGeometryColumns.setContents(point2dContents);
		point2dGeometryColumns.setColumnName(geometryColumn);
		point2dGeometryColumns.setGeometryType(GeometryType.POINT);
		point2dGeometryColumns.setSrs(point2dContents.getSrs());
		point2dGeometryColumns.setZ(0);
		point2dGeometryColumns.setM(0);
		geometryColumnsDao.create(point2dGeometryColumns);

		GeometryColumns polygon2dGeometryColumns = new GeometryColumns();
		polygon2dGeometryColumns.setContents(polygon2dContents);
		polygon2dGeometryColumns.setColumnName(geometryColumn);
		polygon2dGeometryColumns.setGeometryType(GeometryType.POLYGON);
		polygon2dGeometryColumns.setSrs(polygon2dContents.getSrs());
		polygon2dGeometryColumns.setZ(0);
		polygon2dGeometryColumns.setM(0);
		geometryColumnsDao.create(polygon2dGeometryColumns);

		GeometryColumns point3dGeometryColumns = new GeometryColumns();
		point3dGeometryColumns.setContents(point3dContents);
		point3dGeometryColumns.setColumnName(geometryColumn);
		point3dGeometryColumns.setGeometryType(GeometryType.POINT);
		point3dGeometryColumns.setSrs(point3dContents.getSrs());
		point3dGeometryColumns.setZ(1);
		point3dGeometryColumns.setM(0);
		geometryColumnsDao.create(point3dGeometryColumns);

		GeometryColumns lineString3dMGeometryColumns = new GeometryColumns();
		lineString3dMGeometryColumns.setContents(lineString3dMContents);
		lineString3dMGeometryColumns.setColumnName(geometryColumn);
		lineString3dMGeometryColumns.setGeometryType(GeometryType.LINESTRING);
		lineString3dMGeometryColumns.setSrs(lineString3dMContents.getSrs());
		lineString3dMGeometryColumns.setZ(1);
		lineString3dMGeometryColumns.setM(1);
		geometryColumnsDao.create(lineString3dMGeometryColumns);

		// Populated the feature tables with rows
		TestUtils.addRowsToTable(geoPackage, point2dGeometryColumns,
				point2dTable, 3, false, false);
		TestUtils.addRowsToTable(geoPackage, polygon2dGeometryColumns,
				polygon2dTable, 3, false, false);
		TestUtils.addRowsToTable(geoPackage, point3dGeometryColumns,
				point3dTable, 3, true, false);
		TestUtils.addRowsToTable(geoPackage, lineString3dMGeometryColumns,
				lineString3dMTable, 3, true, true);
	}

	/**
	 * Set up create for tiles test
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void setUpCreateTiles(GeoPackage geoPackage)
			throws SQLException {

		// Get existing SRS objects
		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();

		SpatialReferenceSystem epsgSrs = srsDao.queryForId(4326);

		TestCase.assertNotNull(epsgSrs);

		// Create the Tile Matrix Set and Tile Matrix tables
		geoPackage.createTileMatrixSetTable();
		geoPackage.createTileMatrixTable();

		// Create new Contents
		ContentsDao contentsDao = geoPackage.getContentsDao();

		Contents contents = new Contents();
		contents.setTableName("test_tiles");
		contents.setDataType(ContentsDataType.TILES);
		contents.setIdentifier("test_tiles");
		// contents.setDescription("");
		contents.setLastChange(new Date());
		contents.setMinX(-180.0);
		contents.setMinY(-90.0);
		contents.setMaxX(180.0);
		contents.setMaxY(90.0);
		contents.setSrs(epsgSrs);

		// Create the user tile table
		// TODO

		// Create the contents
		contentsDao.create(contents);

		// Create new Tile Matrix Set
		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

		TileMatrixSet tileMatrixSet = new TileMatrixSet();
		tileMatrixSet.setContents(contents);
		tileMatrixSet.setSrs(contents.getSrs());
		tileMatrixSet.setMinX(contents.getMinX());
		tileMatrixSet.setMinY(contents.getMinY());
		tileMatrixSet.setMaxX(contents.getMaxX());
		tileMatrixSet.setMaxY(contents.getMaxY());
		tileMatrixSetDao.create(tileMatrixSet);

		// Create new Tile Matrix rows
		TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

		int matrixWidthAndHeight = 8;
		final int tileWidthAndHeight = 512;
		double pixelXSize = 69237.2;
		double pixelYSize = 68412.1;

		for (int zoom = 0; zoom <= 8; zoom++) {

			TileMatrix tileMatrix = new TileMatrix();
			tileMatrix.setContents(contents);
			tileMatrix.setZoomLevel(zoom);
			tileMatrix.setMatrixWidth(matrixWidthAndHeight);
			tileMatrix.setMatrixHeight(matrixWidthAndHeight);
			tileMatrix.setTileWidth(tileWidthAndHeight);
			tileMatrix.setTileHeight(tileWidthAndHeight);
			tileMatrix.setPixelXSize(pixelXSize);
			tileMatrix.setPixelYSize(pixelYSize);
			tileMatrixDao.create(tileMatrix);

			matrixWidthAndHeight *= 2;
			pixelXSize /= 2.0;
			pixelYSize /= 2.0;
		}

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
