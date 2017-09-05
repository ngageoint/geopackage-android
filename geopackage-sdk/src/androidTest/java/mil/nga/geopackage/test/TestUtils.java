package mil.nga.geopackage.test;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteException;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.db.DateConverter;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.ContextIOUtils;
import mil.nga.geopackage.schema.columns.DataColumns;
import mil.nga.geopackage.schema.columns.DataColumnsDao;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintType;
import mil.nga.geopackage.schema.constraints.DataColumnConstraints;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintsDao;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileColumn;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;

/**
 * Test utility methods
 *
 * @author osbornb
 */
public class TestUtils {

	/**
	 * Sample range data column constraint
	 */
	public static final String SAMPLE_RANGE_CONSTRAINT = "sampleRange";

	/**
	 * Sample enum data column constraint
	 */
	public static final String SAMPLE_ENUM_CONSTRAINT = "sampleEnum";

	/**
	 * Sample glob data column constraint
	 */
	public static final String SAMPLE_GLOB_CONSTRAINT = "sampleGlob";

	/**
	 * Test integer column name
	 */
	public static final String TEST_INTEGER_COLUMN = "test_integer";

	/**
	 * Get test context
	 *
	 * @param activity
	 * @return
	 * @throws NameNotFoundException
	 */
	public static Context getTestContext(Activity activity)
			throws NameNotFoundException {
		return activity.createPackageContext("mil.nga.geopackage.test",
				Context.CONTEXT_IGNORE_SECURITY);
	}

	/**
	 * Copy the asset file to the internal memory storage
	 *
	 * @param context
	 * @param assetPath
	 */
	public static void copyAssetFileToInternalStorage(Context context,
			Context testContext, String assetPath) {

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
	 * Get the internal storage location of the asset file
	 *
	 * @param context
	 * @param assetPath
	 * @return
	 */
	public static String getAssetFileInternalStorageLocation(Context context,
			String assetPath) {
		return ContextIOUtils.getInternalFilePath(context, assetPath);
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

		InputStream assetFile = getAssetFileStream(testContext, assetPath);

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

	/**
	 * Get asset file bytes
	 *
	 * @param testContext
	 * @param assetPath
	 * @return
	 * @throws IOException
	 */
	public static byte[] getAssetFileBytes(Context testContext, String assetPath)
			throws IOException {

		InputStream assetFile = getAssetFileStream(testContext, assetPath);

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int length;
		while ((length = assetFile.read(buffer)) > 0) {
			byteStream.write(buffer, 0, length);
		}

		byte[] bytes = byteStream.toByteArray();

		byteStream.close();
		assetFile.close();

		return bytes;
	}

	/**
	 * Get the asset file input stream
	 *
	 * @param testContext
	 * @param assetPath
	 * @return
	 * @throws IOException
	 */
	public static InputStream getAssetFileStream(Context testContext,
			String assetPath) throws IOException {
		return testContext.getAssets().open(assetPath);
	}

	/**
	 * Create the feature table with data columns entry
	 *
	 * @param geoPackage
	 * @param contents
	 * @param geometryColumn
	 * @param geometryType
	 * @return
	 * @throws SQLException
	 */
	public static FeatureTable createFeatureTable(GeoPackage geoPackage,
			Contents contents, String geometryColumn, GeometryType geometryType)
			throws SQLException {

		FeatureTable table = buildFeatureTable(contents.getTableName(),
				geometryColumn, geometryType);
		geoPackage.createFeatureTable(table);

		double random = Math.random();

		DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();
		DataColumns dataColumns = new DataColumns();
		dataColumns.setContents(contents);
		dataColumns.setColumnName(TEST_INTEGER_COLUMN);
		dataColumns.setName(contents.getTableName());
		dataColumns.setTitle("TEST_TITLE");
		dataColumns.setDescription("TEST_DESCRIPTION");
		dataColumns.setMimeType("TEST_MIME_TYPE");

		if (random < (1.0 / 3.0)) {
			dataColumns.setConstraintName(SAMPLE_RANGE_CONSTRAINT);
		} else if (random < (2.0 / 3.0)) {
			dataColumns.setConstraintName(SAMPLE_ENUM_CONSTRAINT);
		} else {
			dataColumns.setConstraintName(SAMPLE_GLOB_CONSTRAINT);
		}

		dataColumnsDao.create(dataColumns);

		return table;
	}

	/**
	 * Build an example feature table
	 *
	 * @param tableName
	 * @param geometryColumn
	 * @param geometryType
	 * @return
	 */
	public static FeatureTable buildFeatureTable(String tableName,
			String geometryColumn, GeometryType geometryType) {

		List<FeatureColumn> columns = new ArrayList<FeatureColumn>();

		columns.add(FeatureColumn.createPrimaryKeyColumn(0, "id"));
		columns.add(FeatureColumn.createColumn(7, "test_text_limited",
				GeoPackageDataType.TEXT, 5L, false, null));
		columns.add(FeatureColumn.createColumn(8, "test_blob_limited",
				GeoPackageDataType.BLOB, 7L, false, null));
		columns.add(FeatureColumn.createColumn(9, "test_date",
				GeoPackageDataType.DATE, false, null));
		columns.add(FeatureColumn.createColumn(10, "test_datetime",
				GeoPackageDataType.DATETIME, false, null));
		columns.add(FeatureColumn.createGeometryColumn(1, geometryColumn,
				geometryType, false, null));
		columns.add(FeatureColumn.createColumn(2, "test_text",
				GeoPackageDataType.TEXT, false, ""));
		columns.add(FeatureColumn.createColumn(3, "test_real",
				GeoPackageDataType.REAL, false, null));
		columns.add(FeatureColumn.createColumn(4, "test_boolean",
				GeoPackageDataType.BOOLEAN, false, null));
		columns.add(FeatureColumn.createColumn(5, "test_blob",
				GeoPackageDataType.BLOB, false, null));
		columns.add(FeatureColumn.createColumn(6, TEST_INTEGER_COLUMN,
				GeoPackageDataType.INTEGER, false, null));

		FeatureTable table = new FeatureTable(tableName, columns);

		return table;
	}

	/**
	 * Build an example tile table
	 *
	 * @param tableName
	 * @return
	 */
	public static TileTable buildTileTable(String tableName) {

		List<TileColumn> columns = TileTable.createRequiredColumns();

		TileTable table = new TileTable(tableName, columns);

		return table;
	}

	/**
	 * Add rows to the feature table
	 *
	 * @param geoPackage
	 * @param geometryColumns
	 * @param table
	 * @param numRows
	 * @param hasZ
	 * @param hasM
	 * @param allowEmptyFeatures
	 * @throws SQLException
	 */
	public static void addRowsToFeatureTable(GeoPackage geoPackage,
			GeometryColumns geometryColumns, FeatureTable table, int numRows,
			boolean hasZ, boolean hasM, boolean allowEmptyFeatures) throws SQLException {

		FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

		for (int i = 0; i < numRows; i++) {

			FeatureRow newRow = dao.newRow();

			for (FeatureColumn column : table.getColumns()) {
				if (!column.isPrimaryKey()) {

					// Leave nullable columns null 20% of the time
					if (!column.isNotNull()) {
						if (allowEmptyFeatures && Math.random() < .2) {
							continue;
						}
					}

					if (column.isGeometry()) {

						Geometry geometry = null;

						switch (column.getGeometryType()) {
						case POINT:
							geometry = createPoint(hasZ, hasM);
							break;
						case LINESTRING:
							geometry = createLineString(hasZ, hasM, false);
							break;
						case POLYGON:
							geometry = createPolygon(hasZ, hasM);
							break;
						default:
							throw new UnsupportedOperationException(
									"Not implemented for geometry type: "
											+ column.getGeometryType());
						}

						GeoPackageGeometryData geometryData = new GeoPackageGeometryData(
								geometryColumns.getSrsId());
						geometryData.setGeometry(geometry);

						newRow.setGeometry(geometryData);

					} else {

						Object value = null;

						switch (column.getDataType()) {

						case TEXT:
							String text = UUID.randomUUID().toString();
							if (column.getMax() != null
									&& text.length() > column.getMax()) {
								text = text.substring(0, column.getMax()
										.intValue());
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
							byte[] blob = UUID.randomUUID().toString()
									.getBytes();
							if (column.getMax() != null
									&& blob.length > column.getMax()) {
								byte[] blobLimited = new byte[column.getMax()
										.intValue()];
								ByteBuffer.wrap(blob, 0,
										column.getMax().intValue()).get(
										blobLimited);
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
			}
			dao.create(newRow);
		}
	}

	/**
	 * Add rows to the tile table
	 *
	 * @param geoPackage
	 * @param tileMatrix
	 * @param tileData
	 */
	public static void addRowsToTileTable(GeoPackage geoPackage,
			TileMatrix tileMatrix, byte[] tileData) {

		TileDao dao = geoPackage.getTileDao(tileMatrix.getTableName());

		for (int column = 0; column < tileMatrix.getMatrixWidth(); column++) {

			for (int row = 0; row < tileMatrix.getMatrixHeight(); row++) {

				TileRow newRow = dao.newRow();

				newRow.setZoomLevel(tileMatrix.getZoomLevel());
				newRow.setTileColumn(column);
				newRow.setTileRow(row);
				newRow.setTileData(tileData);

				dao.create(newRow);
			}

		}

	}

	/**
	 * Create a random point
	 *
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static Point createPoint(boolean hasZ, boolean hasM) {

		double x = Math.random() * 180.0 * (Math.random() < .5 ? 1 : -1);
		double y = Math.random() * 90.0 * (Math.random() < .5 ? 1 : -1);

		Point point = new Point(hasZ, hasM, x, y);

		if (hasZ) {
			double z = Math.random() * 1000.0;
			point.setZ(z);
		}

		if (hasM) {
			double m = Math.random() * 1000.0;
			point.setM(m);
		}

		return point;
	}

	/**
	 * Create a random line string
	 *
	 * @param hasZ
	 * @param hasM
	 * @param ring
	 * @return
	 */
	public static LineString createLineString(boolean hasZ, boolean hasM,
			boolean ring) {

		LineString lineString = new LineString(hasZ, hasM);

		int numPoints = 2 + ((int) (Math.random() * 9));

		for (int i = 0; i < numPoints; i++) {
			lineString.addPoint(createPoint(hasZ, hasM));
		}

		if (ring) {
			lineString.addPoint(lineString.getPoints().get(0));
		}

		return lineString;
	}

	/**
	 * Create a random polygon
	 *
	 * @param hasZ
	 * @param hasM
	 * @return
	 */
	public static Polygon createPolygon(boolean hasZ, boolean hasM) {

		Polygon polygon = new Polygon(hasZ, hasM);

		int numLineStrings = 1 + ((int) (Math.random() * 5));

		for (int i = 0; i < numLineStrings; i++) {
			polygon.addRing(createLineString(hasZ, hasM, true));
		}

		return polygon;
	}

	/**
	 * Validate the integer value with the data type
	 *
	 * @param value
	 * @param dataType
	 * @return
	 */
	public static void validateIntegerValue(Object value,
			GeoPackageDataType dataType) {

		switch (dataType) {

		case BOOLEAN:
			TestCase.assertTrue(value instanceof Boolean);
			break;
		case TINYINT:
			TestCase.assertTrue(value instanceof Byte);
			break;
		case SMALLINT:
			TestCase.assertTrue(value instanceof Short);
			break;
		case MEDIUMINT:
			TestCase.assertTrue(value instanceof Integer);
			break;
		case INT:
		case INTEGER:
			TestCase.assertTrue(value instanceof Long);
			break;
		default:
			throw new GeoPackageException("Data Type " + dataType
					+ " is not an integer type");
		}
	}

	/**
	 * Validate the float value with the data type
	 *
	 * @param value
	 * @param dataType
	 * @return
	 */
	public static void validateFloatValue(Object value,
			GeoPackageDataType dataType) {

		switch (dataType) {

		case FLOAT:
			TestCase.assertTrue(value instanceof Float);
			break;
		case DOUBLE:
		case REAL:
			TestCase.assertTrue(value instanceof Double);
			break;
		default:
			throw new GeoPackageException("Data Type " + dataType
					+ " is not a float type");
		}
	}

	/**
	 * Determine if the exception is caused from a missing function or module in
	 * SQLite versions 4.2.0 and later. Lollipop uses version 3.8.4.3 so these
	 * are not supported in Android.
	 *
	 * @param e
	 * @return
	 */
	public static boolean isFutureSQLiteException(SQLiteException e) {
		String message = e.getMessage();
		return message.contains("no such function: ST_IsEmpty")
				|| message.contains("no such module: rtree");
	}

	/**
	 * Create Data Column Constraints
	 *
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void createConstraints(GeoPackage geoPackage)
			throws SQLException {

		geoPackage.createDataColumnConstraintsTable();

		DataColumnConstraintsDao dao = geoPackage.getDataColumnConstraintsDao();

		DataColumnConstraints sampleRange = new DataColumnConstraints();
		sampleRange.setConstraintName(SAMPLE_RANGE_CONSTRAINT);
		sampleRange.setConstraintType(DataColumnConstraintType.RANGE);
		sampleRange.setMin(BigDecimal.ONE);
		sampleRange.setMinIsInclusive(true);
		sampleRange.setMax(BigDecimal.TEN);
		sampleRange.setMaxIsInclusive(true);
		dao.create(sampleRange);

		DataColumnConstraints sampleEnum1 = new DataColumnConstraints();
		sampleEnum1.setConstraintName(SAMPLE_ENUM_CONSTRAINT);
		sampleEnum1.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum1.setValue("1");
		dao.create(sampleEnum1);

		DataColumnConstraints sampleEnum3 = new DataColumnConstraints();
		sampleEnum3.setConstraintName(SAMPLE_ENUM_CONSTRAINT);
		sampleEnum3.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum3.setValue("3");
		dao.create(sampleEnum3);

		DataColumnConstraints sampleEnum5 = new DataColumnConstraints();
		sampleEnum5.setConstraintName(SAMPLE_ENUM_CONSTRAINT);
		sampleEnum5.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum5.setValue("5");
		dao.create(sampleEnum5);

		DataColumnConstraints sampleEnum7 = new DataColumnConstraints();
		sampleEnum7.setConstraintName(SAMPLE_ENUM_CONSTRAINT);
		sampleEnum7.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum7.setValue("7");
		dao.create(sampleEnum7);

		DataColumnConstraints sampleEnum9 = new DataColumnConstraints();
		sampleEnum9.setConstraintName(SAMPLE_ENUM_CONSTRAINT);
		sampleEnum9.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum9.setValue("9");
		dao.create(sampleEnum9);

		DataColumnConstraints sampleGlob = new DataColumnConstraints();
		sampleGlob.setConstraintName(SAMPLE_GLOB_CONSTRAINT);
		sampleGlob.setConstraintType(DataColumnConstraintType.GLOB);
		sampleGlob.setValue("[1-2][0-9][0-9][0-9]");
		dao.create(sampleGlob);
	}

	/**
	 * Validate the integrity and keys of the GeoPackage
	 *
	 * @param geoPackage
	 */
	public static void validateGeoPackage(GeoPackage geoPackage) {
		TestCase.assertNull(geoPackage.foreignKeyCheck());
		TestCase.assertNull(geoPackage.integrityCheck());
		TestCase.assertNull(geoPackage.quickCheck());
	}

}
