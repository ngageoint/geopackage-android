package mil.nga.geopackage.db;

import android.content.ContentValues;

import java.util.Map;

import mil.nga.geopackage.GeoPackageException;

/**
 * Core SQL Utility methods
 *
 * @author osbornb
 * @since 1.3.1
 */
public class SQLUtils {

    /**
     * Wrap the content values names in quotes
     *
     * @param values content values
     * @return quoted content values
     */
    public static ContentValues quoteWrap(ContentValues values) {
        ContentValues quoteValues = null;
        if (values != null) {

            quoteValues = new ContentValues();

            for (Map.Entry<String, Object> value : values.valueSet()) {
                setContentValue(quoteValues, CoreSQLUtils.quoteWrap(value.getKey()), value.getValue());
            }

        }

        return quoteValues;
    }

    /**
     * Set the content value into the content values
     *
     * @param contentValues content values
     * @param key           key
     * @param value         object value
     * @since 3.2.1
     */
    public static void setContentValue(ContentValues contentValues, String key, Object value) {

        if (value == null) {
            contentValues.putNull(key);
        } else if (value instanceof String) {
            contentValues.put(key, (String) value);
        } else if (value instanceof Long) {
            contentValues.put(key, (Long) value);
        } else if (value instanceof Integer) {
            contentValues.put(key, (Integer) value);
        } else if (value instanceof Short) {
            contentValues.put(key, (Short) value);
        } else if (value instanceof Byte) {
            contentValues.put(key, (Byte) value);
        } else if (value instanceof Double) {
            contentValues.put(key, (Double) value);
        } else if (value instanceof Float) {
            contentValues.put(key, (Float) value);
        } else if (value instanceof Boolean) {
            contentValues.put(key, (Boolean) value);
        } else if (value instanceof byte[]) {
            contentValues.put(key, (byte[]) value);
        } else {
            throw new GeoPackageException("Unsupported Content Values value: " + value + ", key: " + key);
        }

    }

}
