package mil.nga.giat.geopackage.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mil.nga.giat.geopackage.util.GeoPackageFileUtils;
import android.content.Context;

/**
 * Test utility methods
 * 
 * @author osbornb
 */
public class TestUtils {

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
			e.printStackTrace();
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
