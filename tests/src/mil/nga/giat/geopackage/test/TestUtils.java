package mil.nga.giat.geopackage.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.db.GeoPackageDataType;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.features.user.FeatureColumn;
import mil.nga.giat.geopackage.features.user.FeatureDao;
import mil.nga.giat.geopackage.features.user.FeatureRow;
import mil.nga.giat.geopackage.features.user.FeatureTable;
import mil.nga.giat.geopackage.geom.Geometry;
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.geom.LineString;
import mil.nga.giat.geopackage.geom.Point;
import mil.nga.giat.geopackage.geom.Polygon;
import mil.nga.giat.geopackage.geom.data.GeoPackageGeometryData;
import mil.nga.giat.geopackage.io.GeoPackageFileUtils;
import mil.nga.giat.geopackage.tiles.user.TileColumn;
import mil.nga.giat.geopackage.tiles.user.TileTable;
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
		columns.add(FeatureColumn.createColumn(6, "test_integer",
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
	 * @throws SQLException
	 */
	public static void addRowsToFeatureTable(GeoPackage geoPackage,
			GeometryColumns geometryColumns, FeatureTable table, int numRows,
			boolean hasZ, boolean hasM) throws SQLException {

		FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

		for (int i = 0; i < numRows; i++) {

			FeatureRow newRow = dao.newRow();

			for (FeatureColumn column : table.getColumns()) {
				if (!column.isPrimaryKey()) {

					// Leave nullable columns null 20% of the time
					if (!column.isNotNull()) {
						if (Math.random() < .2) {
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

}
