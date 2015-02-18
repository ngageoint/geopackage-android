package mil.nga.giat.geopackage.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.giat.geopackage.GeoPackageException;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Abstract User DAO for reading user tables
 * 
 * @param <TTable>
 * @param <TRow>
 * @param <TCursor>
 * 
 * @author osbornb
 */
public abstract class UserDao<TTable extends UserTable<?>, TRow extends UserRow<?, TTable>, TCursor extends UserCursor<?, TTable, TRow>> {

	/**
	 * Database connection
	 */
	private final SQLiteDatabase db;

	/**
	 * User table
	 */
	private final TTable table;

	/**
	 * Constructor
	 * 
	 * @param db
	 * @param table
	 */
	protected UserDao(SQLiteDatabase db, TTable table) {
		this.db = db;
		this.table = table;
	}

	/**
	 * Get a new empty row
	 * 
	 * @return
	 */
	public abstract TRow newRow();

	/**
	 * Get the database connection
	 * 
	 * @return
	 */
	public SQLiteDatabase getDb() {
		return db;
	}

	/**
	 * Get the table name
	 * 
	 * @return
	 */
	public String getTableName() {
		return table.getTableName();
	}

	/**
	 * Get the table
	 * 
	 * @return
	 */
	public TTable getTable() {
		return table;
	}

	/**
	 * Drop the user table
	 */
	public void dropTable() {
		db.execSQL("DROP TABLE IF EXISTS " + getTableName());
	}

	/**
	 * Query for all rows
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TCursor queryForAll() {
		return (TCursor) db.query(getTableName(), table.getColumnNames(), null,
				null, null, null, null);
	}

	/**
	 * Query for the row where the field equals the value
	 * 
	 * @param fieldName
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TCursor queryForEq(String fieldName, Object value) {
		String where = buildWhere(fieldName, value);
		String[] whereArgs = buildWhereArgs(value);
		return (TCursor) db.query(getTableName(), table.getColumnNames(),
				where, whereArgs, null, null, null);
	}

	/**
	 * Query for the row where the field equals the value
	 * 
	 * @param fieldName
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TCursor queryForEq(String fieldName, ColumnValue value) {
		String where = buildWhere(fieldName, value);
		String[] whereArgs = buildWhereArgs(value);
		return (TCursor) db.query(getTableName(), table.getColumnNames(),
				where, whereArgs, null, null, null);
	}

	/**
	 * Query for the row where all fields match their values
	 * 
	 * @param fieldValues
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TCursor queryForFieldValues(Map<String, Object> fieldValues) {
		String where = buildWhere(fieldValues.entrySet());
		String[] whereArgs = buildWhereArgs(fieldValues.values());
		return (TCursor) db.query(getTableName(), table.getColumnNames(),
				where, whereArgs, null, null, null);
	}

	/**
	 * Query for the row where all fields match their values
	 * 
	 * @param fieldValues
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TCursor queryForValueFieldValues(Map<String, ColumnValue> fieldValues) {
		String where = buildValueWhere(fieldValues.entrySet());
		String[] whereArgs = buildValueWhereArgs(fieldValues.values());
		return (TCursor) db.query(getTableName(), table.getColumnNames(),
				where, whereArgs, null, null, null);
	}

	/**
	 * Query for the row with the provided id
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TCursor queryForId(long id) {
		String where = getPkWhere(id);
		String[] whereArgs = getPkWhereArgs(id);
		return (TCursor) db.query(getTableName(), table.getColumnNames(),
				where, whereArgs, null, null, null);
	}

	/**
	 * Query for the row with the provided id
	 * 
	 * @param id
	 * @return
	 */
	public TRow queryForIdRow(long id) {
		TRow row = null;
		TCursor readCursor = queryForId(id);
		if (readCursor.moveToNext()) {
			row = readCursor.getRow();
		}
		readCursor.close();
		return row;
	}

	/**
	 * Query for rows
	 * 
	 * @param where
	 * @param whereArgs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TCursor query(String where, String[] whereArgs) {
		return (TCursor) db.query(getTableName(), table.getColumnNames(),
				where, whereArgs, null, null, null);
	}

	/**
	 * Query for rows
	 * 
	 * @param where
	 * @param whereArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TCursor query(String where, String[] whereArgs, String groupBy,
			String having, String orderBy) {
		return (TCursor) db.query(getTableName(), table.getColumnNames(),
				where, whereArgs, groupBy, having, orderBy);
	}

	/**
	 * Query for rows
	 * 
	 * @param where
	 * @param whereArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @param limit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TCursor query(String where, String[] whereArgs, String groupBy,
			String having, String orderBy, String limit) {
		return (TCursor) db.query(getTableName(), table.getColumnNames(),
				where, whereArgs, groupBy, having, orderBy, limit);
	}

	/**
	 * Update the row
	 * 
	 * @param row
	 * @return number of rows affected, should be 0 or 1
	 */
	public int update(TRow row) {
		ContentValues contentValues = row.toContentValues();
		int updated = 0;
		if (contentValues.size() > 0) {
			updated = db.update(getTableName(), contentValues,
					getPkWhere(row.getId()), getPkWhereArgs(row.getId()));
		}
		return updated;
	}

	/**
	 * Update all rows matching the where clause with the provided values
	 * 
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public int update(ContentValues values, String whereClause,
			String[] whereArgs) {
		return db.update(getTableName(), values, whereClause, whereArgs);
	}

	/**
	 * Delete the row
	 * 
	 * @param row
	 * @return number of rows affected, should be 0 or 1
	 */
	public int delete(TRow row) {
		return deleteById(row.getId());
	}

	/**
	 * Delete a row by id
	 * 
	 * @param id
	 * @return number of rows affected, should be 0 or 1
	 */
	public int deleteById(long id) {
		return db.delete(getTableName(), getPkWhere(id), getPkWhereArgs(id));
	}

	/**
	 * Delete rows matching the where clause
	 * 
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public int delete(String whereClause, String[] whereArgs) {
		return db.delete(getTableName(), whereClause, whereArgs);
	}

	/**
	 * Creates a new row, same as calling {@link #insert(UserRow)}
	 * 
	 * @param row
	 * @return row id
	 */
	public long create(TRow row) {
		return insert(row);
	}

	/**
	 * Inserts a new row
	 * 
	 * @param row
	 * @return row id
	 */
	public long insert(TRow row) {
		long id = db.insertOrThrow(getTableName(), null, row.toContentValues());
		row.setId(id);
		return id;
	}

	/**
	 * Inserts a new row
	 * 
	 * @param values
	 * @return row id, -1 on error
	 */
	public long insert(ContentValues values) {
		return db.insert(getTableName(), null, values);
	}

	/**
	 * Inserts a new row
	 * 
	 * @param values
	 * @return row id
	 */
	public long insertOrThrow(ContentValues values) {
		return db.insertOrThrow(getTableName(), null, values);
	}

	/**
	 * Get the primary key where clause
	 * 
	 * @param id
	 * @return
	 */
	private String getPkWhere(long id) {
		return buildWhere(table.getPkColumn().getName(), id);
	}

	/**
	 * Get the primary key where args
	 * 
	 * @return
	 */
	private String[] getPkWhereArgs(long id) {
		return buildWhereArgs(id);
	}

	/**
	 * Build where (or selection) statement from the fields
	 * 
	 * @param fields
	 * @return
	 */
	public String buildWhere(Set<Map.Entry<String, Object>> fields) {
		StringBuilder selection = new StringBuilder();
		for (Map.Entry<String, Object> field : fields) {
			if (selection.length() > 0) {
				selection.append(" AND ");
			}
			selection.append(buildWhere(field.getKey(), field.getValue()));
		}
		return selection.toString();
	}

	/**
	 * Build where (or selection) statement from the fields
	 * 
	 * @param fields
	 * @return
	 */
	public String buildValueWhere(Set<Map.Entry<String, ColumnValue>> fields) {
		StringBuilder selection = new StringBuilder();
		for (Map.Entry<String, ColumnValue> field : fields) {
			if (selection.length() > 0) {
				selection.append(" AND ");
			}
			selection.append(buildWhere(field.getKey(), field.getValue()));
		}
		return selection.toString();
	}

	/**
	 * Build where (or selection) statement for a single field
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	public String buildWhere(String field, Object value) {
		return buildWhere(field, value, "=");
	}

	/**
	 * Build where (or selection) statement for a single field using the
	 * provided operation
	 * 
	 * @param field
	 * @param value
	 * @param operation
	 * @return
	 */
	public String buildWhere(String field, Object value, String operation) {
		return field + " " + (value != null ? operation + " ?" : "IS NULL");
	}

	/**
	 * Build where (or selection) statement for a single field
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	public String buildWhere(String field, ColumnValue value) {
		String where;
		if (value != null) {
			if (value.getValue() != null && value.getTolerance() != null) {
				if (!(value.getValue() instanceof Number)) {
					throw new GeoPackageException(
							"Field value is not a number and can not use a tolerance, Field: "
									+ field + ", Value: " + value);
				}
				where = field + " >= ? AND " + field + " <= ?";
			} else {
				where = buildWhere(field, value.getValue());
			}
		} else {
			where = buildWhere(field, null);
		}
		return where;
	}

	/**
	 * Build where (or selection) args for the values
	 * 
	 * @param values
	 * @return
	 */
	public String[] buildWhereArgs(Collection<Object> values) {
		List<String> selectionArgs = new ArrayList<String>();
		for (Object value : values) {
			if (value != null) {
				selectionArgs.add(value.toString());
			}
		}
		return selectionArgs.isEmpty() ? null : selectionArgs
				.toArray(new String[] {});
	}

	/**
	 * Build where (or selection) args for the values
	 * 
	 * @param values
	 * @return
	 */
	public String[] buildWhereArgs(Object[] values) {
		List<String> selectionArgs = new ArrayList<String>();
		for (Object value : values) {
			if (value != null) {
				selectionArgs.add(value.toString());
			}
		}
		return selectionArgs.isEmpty() ? null : selectionArgs
				.toArray(new String[] {});
	}

	/**
	 * Build where (or selection) args for the values
	 * 
	 * @param values
	 * @return
	 */
	public String[] buildValueWhereArgs(Collection<ColumnValue> values) {
		List<String> selectionArgs = new ArrayList<String>();
		for (ColumnValue value : values) {
			if (value != null && value.getValue() != null) {
				if (value.getTolerance() != null) {
					String[] toleranceArgs = getValueToleranceRange(value);
					selectionArgs.add(toleranceArgs[0]);
					selectionArgs.add(toleranceArgs[1]);
				} else {
					selectionArgs.add(value.getValue().toString());
				}
			}
		}
		return selectionArgs.isEmpty() ? null : selectionArgs
				.toArray(new String[] {});
	}

	/**
	 * Build where (or selection) args for the value
	 * 
	 * @param value
	 * @return
	 */
	public String[] buildWhereArgs(Object value) {
		String[] args = null;
		if (value != null) {
			args = new String[] { value.toString() };
		}
		return args;
	}

	/**
	 * Build where (or selection) args for the value
	 * 
	 * @param value
	 * @return
	 */
	public String[] buildWhereArgs(ColumnValue value) {
		String[] args = null;
		if (value != null) {
			if (value.getValue() != null && value.getTolerance() != null) {
				args = getValueToleranceRange(value);
			} else {
				args = buildWhereArgs(value.getValue());
			}
		}
		return args;
	}

	/**
	 * Get the total count
	 * 
	 * @return
	 */
	public int count() {
		return count(null, null);
	}

	/**
	 * Get the count
	 * 
	 * @return
	 */
	public int count(String where, String[] args) {

		StringBuilder countQuery = new StringBuilder();
		countQuery.append("select count(*) from " + getTableName());
		if (where != null) {
			countQuery.append(" where ").append(where);
		}

		Cursor countCursor = db.rawQuery(countQuery.toString(), args);
		countCursor.moveToFirst();
		int count = countCursor.getInt(0);
		countCursor.close();
		return count;
	}

	/**
	 * Get the value tolerance range min and max values
	 * 
	 * @param value
	 * @return
	 */
	private String[] getValueToleranceRange(ColumnValue value) {
		double doubleValue = ((Number) value.getValue()).doubleValue();
		double tolerance = value.getTolerance();
		return new String[] { Double.toString(doubleValue - tolerance),
				Double.toString(doubleValue + tolerance) };
	}

}
