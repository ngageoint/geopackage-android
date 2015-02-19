package mil.nga.giat.geopackage.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import android.content.Context;

/**
 * Input / Output utility methods
 * 
 * @author osbornb
 */
public class GeoPackageIOUtils {

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
	 * Copy a file to a file location
	 * 
	 * @param copyFrom
	 * @param copyTo
	 * @throws IOException
	 */
	public static void copyFile(File copyFrom, File copyTo) throws IOException {

		InputStream from = new FileInputStream(copyFrom);
		OutputStream to = new FileOutputStream(copyTo);

		copyStream(from, to);
	}

	/**
	 * Copy an input stream to a file location
	 * 
	 * @param copyFrom
	 * @param copyTo
	 * @throws IOException
	 */
	public static void copyFile(InputStream copyFrom, File copyTo)
			throws IOException {

		OutputStream to = new FileOutputStream(copyTo);

		copyStream(copyFrom, to);
	}

	/**
	 * Get the stream bytes
	 * 
	 * @param copyFrom
	 * @throws IOException
	 */
	public static byte[] streamBytes(InputStream stream) throws IOException {

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		copyStream(stream, bytes);

		return bytes.toByteArray();
	}

	/**
	 * Copy an input stream to an output stream
	 * 
	 * @param copyFrom
	 * @param copyTo
	 * @throws IOException
	 */
	public static void copyStream(InputStream copyFrom, OutputStream copyTo)
			throws IOException {

		byte[] buffer = new byte[1024];
		int length;
		while ((length = copyFrom.read(buffer)) > 0) {
			copyTo.write(buffer, 0, length);
		}

		copyTo.flush();
		copyTo.close();
		copyFrom.close();
	}

	/**
	 * Format the bytes into readable text
	 * 
	 * @param bytes
	 * @return
	 */
	public static String formatBytes(long bytes) {

		double value = bytes;
		String unit = "B";

		if (bytes >= 1024) {
			int exponent = (int) (Math.log(bytes) / Math.log(1024));
			exponent = Math.min(exponent, 4);
			switch (exponent) {
			case 1:
				unit = "KB";
				break;
			case 2:
				unit = "MB";
				break;
			case 3:
				unit = "GB";
				break;
			case 4:
				unit = "TB";
				break;
			}
			value = bytes / Math.pow(1024, exponent);
		}

		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(value) + " " + unit;
	}

}
