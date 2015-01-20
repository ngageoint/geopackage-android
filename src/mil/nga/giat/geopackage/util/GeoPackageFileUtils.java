package mil.nga.giat.geopackage.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
		File internalFile = null;
		if (filePath != null) {
			internalFile = new File(context.getFilesDir(), filePath);
		} else {
			internalFile = context.getFilesDir();
		}
		return internalFile;
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

	/**
	 * Copy a file to another location
	 * 
	 * @param copyFrom
	 * @param copyTo
	 * @throws IOException
	 */
	public static void copyFile(File copyFrom, File copyTo) throws IOException {

		InputStream from = new FileInputStream(copyFrom);
		OutputStream to = new FileOutputStream(copyTo);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = from.read(buffer)) > 0) {
			to.write(buffer, 0, length);
		}

		to.flush();
		to.close();
		from.close();
	}

}
