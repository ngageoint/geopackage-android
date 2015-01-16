package mil.nga.giat.geopackage.util;

import java.io.File;

import android.content.Context;

/**
 * File utility methods
 * 
 * @author osbornb
 */
public class GeoPackageFileUtils {

	/**
	 * Get the file extension
	 * 
	 * @param file
	 * @return
	 */
	public static String getFileExtension(File file) {

		String fileName = file.getName();
		String extension = null;

		int extensionIndex = fileName.lastIndexOf(".");
		if (extensionIndex > -1) {
			extension = fileName.substring(extensionIndex + 1);
		}

		return extension;
	}

	/**
	 * Get the file name with the extension removed
	 * 
	 * @param file
	 * @return
	 */
	public static String getFileNameWithoutExtension(File file) {

		String name = file.getName();

		int extensionIndex = name.lastIndexOf(".");
		if (extensionIndex > -1) {
			name = name.substring(0, extensionIndex);
		}

		return name;
	}

	/**
	 * Get the internal storage file for the file path
	 * 
	 * @param context
	 * @param filePath
	 * @return
	 */
	public static File getInternalFile(Context context, String filePath) {
		return new File(context.getFilesDir(), filePath);
	}

	/**
	 * Get the internal storage patch for the file path
	 * 
	 * @param context
	 * @param filePath
	 * @return
	 */
	public static String getInternalFilePath(Context context, String filePath) {
		return getInternalFile(context, filePath).getAbsolutePath();
	}

}
