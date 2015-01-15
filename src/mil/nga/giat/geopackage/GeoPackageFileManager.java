package mil.nga.giat.geopackage;

import java.io.File;

import android.content.Context;

public class GeoPackageFileManager {

	/**
	 * Get the database name from the file
	 * 
	 * @param file
	 * @return
	 */
	public static String getDatabaseName(File file) {

		String dbName = file.getName();

		int extensionIndex = dbName.lastIndexOf(".");
		if (extensionIndex > -1) {
			String extension = dbName.substring(extensionIndex + 1);
			if (extension
					.equalsIgnoreCase(GeoPackageConstants.GEO_PACKAGE_SUFFIX)
					|| extension
							.equalsIgnoreCase(GeoPackageConstants.EXTENDED_GEO_PACKAGE_SUFFIX)) {
				dbName = dbName.substring(0, extensionIndex);
			}
		}
		return dbName;
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
