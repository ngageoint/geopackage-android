package mil.nga.giat.geopackage.db;

import android.database.Cursor;

import mil.nga.giat.geopackage.GeoPackageException;

/**
 * Database utility methods
 * 
 * @author osbornb
 */
public class CursorDatabaseUtils {

	/**
	 * Get the value from the cursor from the provided column
	 * 
	 * @param cursor
	 * @param index
	 * @param dataType
	 * @return
	 */
	public static Object getValue(Cursor cursor, int index,
			GeoPackageDataType dataType) {

		Object value = null;

		int type = cursor.getType(index);

		switch (type) {

		case Cursor.FIELD_TYPE_INTEGER:
			value = getIntegerValue(cursor, index, dataType);
			break;

		case Cursor.FIELD_TYPE_FLOAT:
			value = getFloatValue(cursor, index, dataType);
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
	 * Get the integer value from the cursor of the column
	 * 
	 * @param cursor
	 * @param index
	 * @param dataType
	 * @return
	 */
	public static Object getIntegerValue(Cursor cursor, int index,
			GeoPackageDataType dataType) {

		Object value = null;

		switch (dataType) {

		case BOOLEAN:
			short booleanValue = cursor.getShort(index);
			value = booleanValue == 0 ? Boolean.FALSE : Boolean.TRUE;
			break;
		case TINYINT:
			value = (byte) cursor.getShort(index);
			break;
		case SMALLINT:
			value = cursor.getShort(index);
			break;
		case MEDIUMINT:
			value = cursor.getInt(index);
			break;
		case INT:
		case INTEGER:
			value = cursor.getLong(index);
			break;
		default:
			throw new GeoPackageException("Data Type " + dataType
					+ " is not an integer type");
		}

		return value;
	}

	/**
	 * Get the float value from the cursor of the column
	 * 
	 * @param cursor
	 * @param index
	 * @param dataType
	 * @return
	 */
	public static Object getFloatValue(Cursor cursor, int index,
			GeoPackageDataType dataType) {

		Object value = null;

		switch (dataType) {

		case FLOAT:
			value = cursor.getFloat(index);
			break;
		case DOUBLE:
		case REAL:
		case INTEGER:
		case INT:
			value = cursor.getDouble(index);
			break;
		default:
			throw new GeoPackageException("Data Type " + dataType
					+ " is not a float type");
		}

		return value;
	}

}
