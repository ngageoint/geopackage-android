package mil.nga.geopackage.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.attributes.AttributesColumn;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;
import mil.nga.geopackage.attributes.AttributesTable;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.db.DateConverter;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.ExtensionsDao;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.metadata.Metadata;
import mil.nga.geopackage.metadata.MetadataDao;
import mil.nga.geopackage.metadata.MetadataScopeType;
import mil.nga.geopackage.metadata.reference.MetadataReference;
import mil.nga.geopackage.metadata.reference.MetadataReferenceDao;
import mil.nga.geopackage.metadata.reference.ReferenceScopeType;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileTable;
import mil.nga.wkb.geom.GeometryType;

/**
 * Test setup and teardown methods for preparing and cleaning the database with
 * data
 * 
 * @author osbornb
 */
public class TestSetupTeardown {

	public static final int CREATE_SRS_COUNT = 3;

	public static final int CREATE_CONTENTS_COUNT = 6;

	public static final int CREATE_GEOMETRY_COLUMNS_COUNT = 4;

	public static final int CREATE_TILE_MATRIX_SET_COUNT = 1;

	public static final int CREATE_TILE_MATRIX_COUNT = 3;

	public static final int CREATE_DATA_COLUMNS_COUNT = CREATE_GEOMETRY_COLUMNS_COUNT;

	public static final int CREATE_DATA_COLUMN_CONSTRAINTS_COUNT = 7;

	public static final int CREATE_METADATA_COUNT = 4;

	public static final int CREATE_METADATA_REFERENCE_COUNT = 13;

	public static final int CREATE_EXTENSIONS_COUNT = 5;

	/**
	 * Set up the create database
	 *
	 * @param activity
	 * @param testContext
	 * @param features
	 * @param tiles
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static GeoPackage setUpCreate(Activity activity,
										 Context testContext, boolean features, boolean tiles)
			throws SQLException, IOException {
		return setUpCreate(activity, testContext, features, true, tiles);
	}

	/**
	 * Set up the create database
	 * 
	 * @param activity
	 * @param testContext
	 * @param features
	 * @param allowEmptyFeatures
	 * @param tiles
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static GeoPackage setUpCreate(Activity activity,
			Context testContext, boolean features, boolean allowEmptyFeatures, boolean tiles)
			throws SQLException, IOException {

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

		TestCase.assertEquals("Application Id", geoPackage.getApplicationId(), GeoPackageConstants.APPLICATION_ID);
		TestCase.assertEquals("User Version", geoPackage.getUserVersion(), GeoPackageConstants.USER_VERSION);
		String userVersionString = String.valueOf(geoPackage.getUserVersion());
		String majorVersion = userVersionString.substring(0, userVersionString.length() - 4);
		String minorVersion = userVersionString.substring(userVersionString.length() - 4, userVersionString.length() - 2);
		String patchVersion = userVersionString.substring(userVersionString.length() - 2);
		TestCase.assertEquals("Major User Version", geoPackage.getUserVersionMajor(), Integer.valueOf(majorVersion).intValue());
		TestCase.assertEquals("Minor User Version", geoPackage.getUserVersionMinor(), Integer.valueOf(minorVersion).intValue());
		TestCase.assertEquals("Patch User Version", geoPackage.getUserVersionPatch(), Integer.valueOf(patchVersion).intValue());

		if (features) {
			setUpCreateFeatures(geoPackage, allowEmptyFeatures);
		}

		if (tiles) {
			setUpCreateTiles(testContext, geoPackage);
		}

		setUpCreateCommon(geoPackage);

		return geoPackage;
	}

	/**
	 * Set up create common
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	private static void setUpCreateCommon(GeoPackage geoPackage)
			throws SQLException {

		// Metadata
		geoPackage.createMetadataTable();
		MetadataDao metadataDao = geoPackage.getMetadataDao();

		Metadata metadata1 = new Metadata();
		metadata1.setId(1);
		metadata1.setMetadataScope(MetadataScopeType.DATASET);
		metadata1.setStandardUri("TEST_URI_1");
		metadata1.setMimeType("text/xml");
		metadata1.setMetadata("TEST METADATA 1");
		metadataDao.create(metadata1);

		Metadata metadata2 = new Metadata();
		metadata2.setId(2);
		metadata2.setMetadataScope(MetadataScopeType.FEATURE_TYPE);
		metadata2.setStandardUri("TEST_URI_2");
		metadata2.setMimeType("text/xml");
		metadata2.setMetadata("TEST METADATA 2");
		metadataDao.create(metadata2);

		Metadata metadata3 = new Metadata();
		metadata3.setId(3);
		metadata3.setMetadataScope(MetadataScopeType.TILE);
		metadata3.setStandardUri("TEST_URI_3");
		metadata3.setMimeType("text/xml");
		metadata3.setMetadata("TEST METADATA 3");
		metadataDao.create(metadata3);

		// Metadata Reference
		geoPackage.createMetadataReferenceTable();
		MetadataReferenceDao metadataReferenceDao = geoPackage
				.getMetadataReferenceDao();

		MetadataReference reference1 = new MetadataReference();
		reference1.setReferenceScope(ReferenceScopeType.GEOPACKAGE);
		reference1.setTimestamp(new Date());
		reference1.setMetadata(metadata1);
		metadataReferenceDao.create(reference1);

		MetadataReference reference2 = new MetadataReference();
		reference2.setReferenceScope(ReferenceScopeType.TABLE);
		reference2.setTableName("TEST_TABLE_NAME_2");
		reference2.setTimestamp(new Date());
		reference2.setMetadata(metadata2);
		reference2.setParentMetadata(metadata1);
		metadataReferenceDao.create(reference2);

		MetadataReference reference3 = new MetadataReference();
		reference3.setReferenceScope(ReferenceScopeType.ROW_COL);
		reference3.setTableName("TEST_TABLE_NAME_3");
		reference3.setColumnName("TEST_COLUMN_NAME_3");
		reference3.setRowIdValue(5L);
		reference3.setTimestamp(new Date());
		reference3.setMetadata(metadata3);
		metadataReferenceDao.create(reference3);

		// Extensions
		geoPackage.createExtensionsTable();
		ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();

		Extensions extensions1 = new Extensions();
		extensions1.setTableName("TEST_TABLE_NAME_1");
		extensions1.setColumnName("TEST_COLUMN_NAME_1");
		extensions1.setExtensionName("nga", "test_extension_1");
		extensions1.setDefinition("TEST DEFINITION 1");
		extensions1.setScope(ExtensionScopeType.READ_WRITE);
		extensionsDao.create(extensions1);

		Extensions extensions2 = new Extensions();
		extensions2.setTableName("TEST_TABLE_NAME_2");
		extensions2.setExtensionName("nga", "test_extension_2");
		extensions2.setDefinition("TEST DEFINITION 2");
		extensions2.setScope(ExtensionScopeType.WRITE_ONLY);
		extensionsDao.create(extensions2);

		Extensions extensions3 = new Extensions();
		extensions3.setExtensionName("nga", "test_extension_3");
		extensions3.setDefinition("TEST DEFINITION 3");
		extensions3.setScope(ExtensionScopeType.READ_WRITE);
		extensionsDao.create(extensions3);

		// Attributes

		List<AttributesColumn> columns = new ArrayList<AttributesColumn>();

		columns.add(AttributesColumn.createColumn(6, "test_text_limited",
				GeoPackageDataType.TEXT, 5L, false, null));
		columns.add(AttributesColumn.createColumn(7, "test_blob_limited",
				GeoPackageDataType.BLOB, 7L, false, null));
		columns.add(AttributesColumn.createColumn(8, "test_date",
				GeoPackageDataType.DATE, false, null));
		columns.add(AttributesColumn.createColumn(9, "test_datetime",
				GeoPackageDataType.DATETIME, false, null));
		columns.add(AttributesColumn.createColumn(1, "test_text",
				GeoPackageDataType.TEXT, false, ""));
		columns.add(AttributesColumn.createColumn(2, "test_real",
				GeoPackageDataType.REAL, false, null));
		columns.add(AttributesColumn.createColumn(3, "test_boolean",
				GeoPackageDataType.BOOLEAN, false, null));
		columns.add(AttributesColumn.createColumn(4, "test_blob",
				GeoPackageDataType.BLOB, false, null));
		columns.add(AttributesColumn.createColumn(5, "test_integer",
				GeoPackageDataType.INTEGER, false, null));

		AttributesTable attributesTable = geoPackage
				.createAttributesTableWithId("test_attributes", columns);
		TestCase.assertNotNull(attributesTable);
		Contents attributesContents = attributesTable.getContents();
		TestCase.assertNotNull(attributesContents);
		TestCase.assertEquals(ContentsDataType.ATTRIBUTES,
				attributesContents.getDataType());
		TestCase.assertEquals("test_attributes",
				attributesContents.getTableName());
		TestCase.assertEquals(attributesContents.getTableName(),
				attributesTable.getTableName());

		Metadata attributesMetadata = new Metadata();
		attributesMetadata.setId(4);
		attributesMetadata.setMetadataScope(MetadataScopeType.ATTRIBUTE_TYPE);
		attributesMetadata.setStandardUri("ATTRIBUTES_TEST_URI");
		attributesMetadata.setMimeType("text/plain");
		attributesMetadata.setMetadata("ATTRIBUTES METADATA");
		metadataDao.create(attributesMetadata);

		AttributesDao attributesDao = geoPackage
				.getAttributesDao(attributesTable.getTableName());

		for (int i = 0; i < 10; i++) {

			AttributesRow newRow = attributesDao.newRow();

			for (AttributesColumn column : attributesTable.getColumns()) {
				if (!column.isPrimaryKey()) {

					// Leave nullable columns null 20% of the time
					if (!column.isNotNull()) {
						if (Math.random() < 0.2) {
							continue;
						}
					}

					Object value = null;

					switch (column.getDataType()) {

						case TEXT:
							String text = UUID.randomUUID().toString();
							if (column.getMax() != null
									&& text.length() > column.getMax()) {
								text = text
										.substring(0, column.getMax().intValue());
							}
							value = text;
							break;
						case REAL:
						case DOUBLE:
							value = Math.random() * 5000.0;
							break;
						case BOOLEAN:
							value = Math.random() < .5 ? false : true;
							break;
						case INTEGER:
						case INT:
							value = (int) (Math.random() * 500);
							break;
						case BLOB:
							byte[] blob = UUID.randomUUID().toString().getBytes();
							if (column.getMax() != null
									&& blob.length > column.getMax()) {
								byte[] blobLimited = new byte[column.getMax()
										.intValue()];
								ByteBuffer
										.wrap(blob, 0, column.getMax().intValue())
										.get(blobLimited);
								blob = blobLimited;
							}
							value = blob;
							break;
						case DATE:
						case DATETIME:
							DateConverter converter = DateConverter.converter(column.getDataType());
							Date date = new Date();
							if(Math.random() < .5){
								value = date;
							}else{
								value = converter.stringValue(date);
							}
							break;
						default:
							throw new UnsupportedOperationException(
									"Not implemented for data type: "
											+ column.getDataType());
					}

					newRow.setValue(column.getName(), value);

				}
			}
			long rowId = attributesDao.create(newRow);

			MetadataReference attributesReference = new MetadataReference();
			attributesReference.setReferenceScope(ReferenceScopeType.ROW);
			attributesReference.setTableName(attributesTable.getTableName());
			attributesReference.setRowIdValue(rowId);
			attributesReference.setTimestamp(new Date());
			attributesReference.setMetadata(attributesMetadata);
			metadataReferenceDao.create(attributesReference);
		}
	}

	/**
	 * Set up create for features test
	 * 
	 * @param geoPackage
	 * @param allowEmptyFeatures
	 * @throws SQLException
	 */
	private static void setUpCreateFeatures(GeoPackage geoPackage, boolean allowEmptyFeatures)
			throws SQLException {

		// Get existing SRS objects
		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();

		SpatialReferenceSystem epsgSrs = srsDao.queryForId(4326l);
		SpatialReferenceSystem undefinedCartesianSrs = srsDao.queryForId(-1l);
		SpatialReferenceSystem undefinedGeographicSrs = srsDao.queryForId(0l);

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
		// point2dContents.setLastChange(new Date());
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
		// polygon2dContents.setLastChange(new Date());
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
		// point3dContents.setLastChange(new Date());
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
		// lineString3dMContents.setLastChange(new Date());
		lineString3dMContents.setMinX(-180.0);
		lineString3dMContents.setMinY(-90.0);
		lineString3dMContents.setMaxX(180.0);
		lineString3dMContents.setMaxY(90.0);
		lineString3dMContents.setSrs(undefinedCartesianSrs);

		// Create Data Column Constraints table and rows
		TestUtils.createConstraints(geoPackage);

		// Create data columns table
		geoPackage.createDataColumnsTable();

		String geometryColumn = "geometry";

		// Create the feature tables
		FeatureTable point2dTable = TestUtils.createFeatureTable(geoPackage,
				point2dContents, geometryColumn, GeometryType.POINT);
		FeatureTable polygon2dTable = TestUtils.createFeatureTable(geoPackage,
				polygon2dContents, geometryColumn, GeometryType.POLYGON);
		FeatureTable point3dTable = TestUtils.createFeatureTable(geoPackage,
				point3dContents, geometryColumn, GeometryType.POINT);
		FeatureTable lineString3dMTable = TestUtils.createFeatureTable(
				geoPackage, lineString3dMContents, geometryColumn,
				GeometryType.LINESTRING);

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
		point2dGeometryColumns.setZ((byte) 0);
		point2dGeometryColumns.setM((byte) 0);
		geometryColumnsDao.create(point2dGeometryColumns);

		GeometryColumns polygon2dGeometryColumns = new GeometryColumns();
		polygon2dGeometryColumns.setContents(polygon2dContents);
		polygon2dGeometryColumns.setColumnName(geometryColumn);
		polygon2dGeometryColumns.setGeometryType(GeometryType.POLYGON);
		polygon2dGeometryColumns.setSrs(polygon2dContents.getSrs());
		polygon2dGeometryColumns.setZ((byte) 0);
		polygon2dGeometryColumns.setM((byte) 0);
		geometryColumnsDao.create(polygon2dGeometryColumns);

		GeometryColumns point3dGeometryColumns = new GeometryColumns();
		point3dGeometryColumns.setContents(point3dContents);
		point3dGeometryColumns.setColumnName(geometryColumn);
		point3dGeometryColumns.setGeometryType(GeometryType.POINT);
		point3dGeometryColumns.setSrs(point3dContents.getSrs());
		point3dGeometryColumns.setZ((byte) 1);
		point3dGeometryColumns.setM((byte) 0);
		geometryColumnsDao.create(point3dGeometryColumns);

		GeometryColumns lineString3dMGeometryColumns = new GeometryColumns();
		lineString3dMGeometryColumns.setContents(lineString3dMContents);
		lineString3dMGeometryColumns.setColumnName(geometryColumn);
		lineString3dMGeometryColumns.setGeometryType(GeometryType.LINESTRING);
		lineString3dMGeometryColumns.setSrs(lineString3dMContents.getSrs());
		lineString3dMGeometryColumns.setZ((byte) 1);
		lineString3dMGeometryColumns.setM((byte) 1);
		geometryColumnsDao.create(lineString3dMGeometryColumns);

		// Populate the feature tables with rows
		TestUtils.addRowsToFeatureTable(geoPackage, point2dGeometryColumns,
				point2dTable, 3, false, false, allowEmptyFeatures);
		TestUtils.addRowsToFeatureTable(geoPackage, polygon2dGeometryColumns,
				polygon2dTable, 3, false, false, allowEmptyFeatures);
		TestUtils.addRowsToFeatureTable(geoPackage, point3dGeometryColumns,
				point3dTable, 3, true, false, allowEmptyFeatures);
		TestUtils
				.addRowsToFeatureTable(geoPackage,
						lineString3dMGeometryColumns, lineString3dMTable, 3,
						true, true, allowEmptyFeatures);

	}

	/**
	 * Set up create for tiles test
	 * 
	 * @param testContext
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	private static void setUpCreateTiles(Context testContext,
			GeoPackage geoPackage) throws SQLException, IOException {

		// Get existing SRS objects
		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();

		SpatialReferenceSystem epsgSrs = srsDao.queryForId(4326l);

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
		// contents.setLastChange(new Date());
		contents.setMinX(-180.0);
		contents.setMinY(-90.0);
		contents.setMaxX(180.0);
		contents.setMaxY(90.0);
		contents.setSrs(epsgSrs);

		// Create the user tile table
		TileTable tileTable = TestUtils.buildTileTable(contents.getTableName());
		geoPackage.createTileTable(tileTable);

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

		// Read the asset tile to bytes and convert to bitmap
		byte[] assetTileData = TestUtils.getAssetFileBytes(testContext,
				TestConstants.TILE_FILE_NAME);
		Bitmap bitmap = BitmapConverter.toBitmap(assetTileData);

		// Get the width and height of the bitmap
		final int tileWidth = bitmap.getWidth();
		final int tileHeight = bitmap.getHeight();

		int matrixWidthAndHeight = 2;
		double pixelXSize = (tileMatrixSet.getMaxX() - tileMatrixSet.getMinX()) / (matrixWidthAndHeight * tileWidth);
		double pixelYSize = (tileMatrixSet.getMaxY() - tileMatrixSet.getMinY()) / (matrixWidthAndHeight * tileHeight);

		// Compress the bitmap back to bytes and use those for the test
		byte[] tileData = BitmapConverter.toBytes(bitmap, CompressFormat
				.valueOf(TestConstants.TILE_FILE_NAME_EXTENSION.toUpperCase()));

		for (int zoom = 0; zoom < CREATE_TILE_MATRIX_COUNT; zoom++) {

			TileMatrix tileMatrix = new TileMatrix();
			tileMatrix.setContents(contents);
			tileMatrix.setZoomLevel(zoom);
			tileMatrix.setMatrixWidth(matrixWidthAndHeight);
			tileMatrix.setMatrixHeight(matrixWidthAndHeight);
			tileMatrix.setTileWidth(tileWidth);
			tileMatrix.setTileHeight(tileHeight);
			tileMatrix.setPixelXSize(pixelXSize);
			tileMatrix.setPixelYSize(pixelYSize);
			tileMatrixDao.create(tileMatrix);

			matrixWidthAndHeight *= 2;
			pixelXSize /= 2.0;
			pixelYSize /= 2.0;

			// Populate the tile table with rows
			TestUtils.addRowsToTileTable(geoPackage, tileMatrix, tileData);
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
