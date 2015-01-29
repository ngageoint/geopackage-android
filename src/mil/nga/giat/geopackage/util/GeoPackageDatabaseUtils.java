package mil.nga.giat.geopackage.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Database utility methods
 * 
 * @author osbornb
 */
public class GeoPackageDatabaseUtils {

	/**
	 * Get the value from the cursor at the provided index based upon its type
	 * 
	 * @param cursor
	 * @param index
	 * @return
	 */
	public static Object getValue(Cursor cursor, int index) {

		Object value = null;

		int type = cursor.getType(index);

		switch (type) {

		case Cursor.FIELD_TYPE_INTEGER:
			value = cursor.getLong(index);
			break;

		case Cursor.FIELD_TYPE_FLOAT:
			value = cursor.getDouble(index);
			break;

		case Cursor.FIELD_TYPE_STRING:
			value = cursor.getString(index);
			break;

		case Cursor.FIELD_TYPE_BLOB:
			value = cursor.getBlob(index);
			break;

		case Cursor.FIELD_TYPE_NULL:
			// leave value as null
		}

		return value;
	}

	/**
	 * Does the table exist?
	 * 
	 * @param db
	 * @param tableName
	 * @return
	 */
	public static boolean tableExists(SQLiteDatabase db, String tableName) {

		boolean exists = false;

		Cursor cursor = db
				.rawQuery(
						"select DISTINCT tbl_name from sqlite_master where tbl_name = ?",
						new String[] { tableName });
		if (cursor != null) {
			exists = cursor.getCount() > 0;
			cursor.close();
		}

		return exists;
	}

}
