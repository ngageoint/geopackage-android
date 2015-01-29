package mil.nga.giat.geopackage.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.geopackage.data.c4.FeatureColumn;
import mil.nga.giat.geopackage.data.c4.FeatureTable;
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
	public static FeatureTable buildTable(String tableName,
			String geometryColumn, String geometryType) {

		List<FeatureColumn> columns = new ArrayList<FeatureColumn>();

		columns.add(FeatureColumn.createPrimaryKeyColumn(0, "id"));
		columns.add(FeatureColumn.createGeometryColumn(1, geometryColumn,
				geometryType, false, null));
		columns.add(FeatureColumn.createColumn(2, "test_text", "TEXT", false,
				""));
		columns.add(FeatureColumn.createColumn(3, "test_real", "REAL", false,
				null));
		columns.add(FeatureColumn.createColumn(4, "test_boolean", "BOOLEAN",
				false, null));
		columns.add(FeatureColumn.createColumn(5, "test_blob", "BLOB", false,
				null));

		FeatureTable table = new FeatureTable(tableName, columns);

		return table;
	}

}
