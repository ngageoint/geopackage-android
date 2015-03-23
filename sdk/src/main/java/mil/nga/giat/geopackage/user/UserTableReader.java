package mil.nga.giat.geopackage.user;

import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Reads the metadata from an existing user table
 * 
 * @author osbornb
 */
public abstract class UserTableReader<TColumn extends UserColumn, TTable extends UserTable<TColumn>> {

	/**
	 * Index column
	 */
	private static final String CID = "cid";

	/**
	 * Name column
	 */
	private static final String NAME = "name";

	/**
	 * Type column
	 */
	private static final String TYPE = "type";

	/**
	 * Not null column
	 */
	private static final String NOT_NULL = "notnull";

	/**
	 * Primary key column
	 */
	private static final String PK = "pk";

	/**
	 * Default value column
	 */
	private static final String DFLT_VALUE = "dflt_value";

	/**
	 * Table name
	 */
	private final String tableName;

	/**
	 * Constructor
	 * 
	 * @param tableName
	 */
	protected UserTableReader(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Create the table
	 * 
	 * @param tableName
	 * @param columnList
	 * @return
	 */
	protected abstract TTable createTable(String tableName,
			List<TColumn> columnList);

	/**
	 * Create the column
	 * 
	 * @param cursor
	 * @param index
	 * @param name
	 * @param type
	 * @param max
	 * @param notNull
	 * @param defaultValueIndex
	 * @param primaryKey
	 * @return
	 */
	protected abstract TColumn createColumn(Cursor cursor, int index,
			String name, String type, Long max, boolean notNull,
			int defaultValueIndex, boolean primaryKey);

	/**
	 * Read the table
	 * 
	 * @param db
	 * @return
	 */
	public TTable readTable(SQLiteDatabase db) {

		List<TColumn> columnList = new ArrayList<TColumn>();

		Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")",
				null);
		try {
			while (cursor.moveToNext()) {
				int index = cursor.getInt(cursor.getColumnIndex(CID));
				String name = cursor.getString(cursor.getColumnIndex(NAME));
				String type = cursor.getString(cursor.getColumnIndex(TYPE));
				boolean notNull = cursor
						.getInt(cursor.getColumnIndex(NOT_NULL)) == 1;
				boolean primaryKey = cursor.getInt(cursor.getColumnIndex(PK)) == 1;

				// If the type has a max limit on it, pull it off
				Long max = null;
				if (type != null && type.endsWith(")")) {
					int maxStart = type.indexOf("(");
					if (maxStart > -1) {
						String maxString = type.substring(maxStart + 1,
								type.length() - 1);
						if (!maxString.isEmpty()) {
							try {
								max = Long.valueOf(maxString);
								type = type.substring(0, maxStart);
							} catch (NumberFormatException e) {
								Log.w(UserTableReader.class.getSimpleName(),
										"Failed to parse type max from type: "
												+ type, e);
							}
						}
					}
				}

				// Get the geometry or data type and default value
				int defaultValueIndex = cursor.getColumnIndex(DFLT_VALUE);

				TColumn column = createColumn(cursor, index, name, type, max,
						notNull, defaultValueIndex, primaryKey);
				columnList.add(column);
			}
		} finally {
			cursor.close();
		}
		if (columnList.isEmpty()) {
			throw new GeoPackageException("Table does not exist: " + tableName);
		}

		return createTable(tableName, columnList);
	}

}
