package mil.nga.giat.geopackage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class TempTests {

	private static final String TAG = TempTests.class.getSimpleName();

	public static final String exampleDb = "gdal_sample";

	public static final String exampleDbFile = exampleDb + "."
			+ GeoPackageConstants.GEO_PACKAGE_SUFFIX;

	public static void copySampleToInternalStorage(Context context) {

		String filePath = getExampleFilePath(context);
		try {
			copyAssetToInternalStorage(context, "db" + File.separatorChar
					+ exampleDbFile, filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getExampleFilePath(Context context) {
		return GeoPackageFileManager
				.getInternalFilePath(context, exampleDbFile);
	}

	private static void copyAssetToInternalStorage(Context context,
			String assetPath, String filePath) throws IOException {

		InputStream assetFile = context.getAssets().open(assetPath);

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
	 * Log the columns and row values
	 * 
	 * @param cursor
	 */
	public static void logRows(Cursor cursor) {

		StringBuilder logValue = new StringBuilder();

		for (String columnName : cursor.getColumnNames()) {

			if (logValue.length() > 0) {
				logValue.append(", ");
			}
			logValue.append(columnName);

		}

		while (cursor.moveToNext()) {

			for (int i = 0; i < cursor.getColumnCount(); i++) {

				if (i == 0) {
					logValue.append("\n");
				} else {
					logValue.append(", ");
				}

				int type = cursor.getType(i);

				switch (type) {

				case Cursor.FIELD_TYPE_STRING:
					logValue.append(cursor.getString(i));
					break;

				case Cursor.FIELD_TYPE_INTEGER:
					logValue.append(cursor.getLong(i));
					break;

				case Cursor.FIELD_TYPE_FLOAT:
					logValue.append(cursor.getDouble(i));
					break;

				case Cursor.FIELD_TYPE_BLOB:
					logValue.append(cursor.getBlob(i));
					break;

				}

			}

		}

		Log.i(TAG, logValue.toString());
	}

}
