package mil.nga.giat.geopackage.data.c4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c2.Contents;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.util.GeoPackageDatabaseUtils;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Feature DAO for reading feature user data tables
 * 
 * @author osbornb
 */
public class FeatureDao {

	/**
	 * Database connection
	 */
	private final SQLiteDatabase db;

	/**
	 * Geometry Columns
	 */
	private final GeometryColumns geometryColumns;

	/**
	 * Feature columns
	 */
	private final FeatureColumns columns;

	/**
	 * Constructor
	 * 
	 * @param db
	 * @param geometryColumns
	 */
	public FeatureDao(SQLiteDatabase db, GeometryColumns geometryColumns) {
		this.db = db;

		this.geometryColumns = geometryColumns;
		if (geometryColumns.getContents() == null) {
			throw new GeoPackageException(GeometryColumns.class.getSimpleName()
					+ " " + geometryColumns.getId() + " has null "
					+ Contents.class.getSimpleName());
		}
		if (geometryColumns.getSrs() == null) {
			throw new GeoPackageException(GeometryColumns.class.getSimpleName()
					+ " " + geometryColumns.getId() + " has null "
					+ SpatialReferenceSystem.class.getSimpleName());
		}

		// Build the table column metadata
		columns = buildColumns();

	}

	/**
	 * Set the table column values from the database schema
	 * 
	 * @return
	 */
	private FeatureColumns buildColumns() {

		List<FeatureColumn> columnList = new ArrayList<FeatureColumn>();

		Cursor cursor = db.rawQuery(
				"PRAGMA table_info(" + getTableName() + ")", null);
		while (cursor.moveToNext()) {
			int index = cursor.getInt(cursor.getColumnIndex("cid"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String type = cursor.getString(cursor.getColumnIndex("type"));
			boolean notNull = cursor.getInt(cursor.getColumnIndex("notnull")) == 1;
			int defaultValueIndex = cursor.getColumnIndex("dflt_value");
			Object defaultValue = GeoPackageDatabaseUtils.getValue(cursor,
					defaultValueIndex);
			boolean primaryKey = cursor.getInt(cursor.getColumnIndex("pk")) == 1;
			boolean geometry = name.equals(geometryColumns.getColumnName());

			FeatureColumn column = new FeatureColumn(index, name, type,
					notNull, defaultValue, primaryKey, geometry);
			columnList.add(column);
		}
		if (columnList.isEmpty()) {
			throw new GeoPackageException("Feature Table does not exist: "
					+ getTableName());
		}

		return new FeatureColumns(getTableName(), columnList);
	}

	/**
	 * Get the database connection
	 * 
	 * @return
	 */
	public SQLiteDatabase getDb() {
		return db;
	}

	/**
	 * Get the Geometry Columns
	 * 
	 * @return
	 */
	public GeometryColumns getGeometryColumns() {
		return geometryColumns;
	}

	/**
	 * Get the table name
	 * 
	 * @return
	 */
	public String getTableName() {
		return geometryColumns.getTableName();
	}

	/**
	 * The the Geometry Column name
	 * 
	 * @return
	 */
	public String getGeometryColumnName() {
		return geometryColumns.getColumnName();
	}

	/**
	 * Get the Geometry Type
	 * 
	 * @return
	 */
	public GeometryType getGeometryType() {
		return geometryColumns.getGeometryType();
	}

	/**
	 * Get the columns
	 * 
	 * @return
	 */
	public FeatureColumns getColumns() {
		return columns;
	}

	/**
	 * Query for all rows
	 * 
	 * @return
	 */
	public FeatureCursor queryForAll() {
		return (FeatureCursor) db.query(getTableName(),
				columns.getColumnNames(), null, null, null, null, null);
	}

	/**
	 * Query for the row where the field equals the value
	 * 
	 * @param id
	 * @return
	 */
	public FeatureCursor queryForEq(String fieldName, Object value) {
		return (FeatureCursor) db.query(getTableName(),
				columns.getColumnNames(), buildWhere(fieldName, value),
				buildWhereArgs(value), null, null, null);
	}

	/**
	 * Query for the row where all fields match their values
	 * 
	 * @param fieldValues
	 * @return
	 */
	public FeatureCursor queryForFieldValues(Map<String, Object> fieldValues) {
		return (FeatureCursor) db.query(getTableName(),
				columns.getColumnNames(), buildWhere(fieldValues.entrySet()),
				buildWhereArgs(fieldValues.values()), null, null, null);
	}

	/**
	 * Query for the row with the provided id
	 * 
	 * @param id
	 * @return
	 */
	public FeatureCursor queryForId(long id) {
		return (FeatureCursor) db.query(getTableName(),
				columns.getColumnNames(), getPkWhere(id), getPkWhereArgs(id),
				null, null, null);
	}

	/**
	 * Query for the feature row with the provided id
	 * 
	 * @param id
	 * @return
	 */
	public FeatureRow queryForIdRow(long id) {
		FeatureRow row = null;
		FeatureCursor readCursor = queryForId(id);
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
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return
	 */
	public FeatureCursor query(String where, String[] whereArgs,
			String groupBy, String having, String orderBy) {
		return (FeatureCursor) db.query(getTableName(),
				columns.getColumnNames(), where, whereArgs, groupBy, having,
				orderBy);
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
	public FeatureCursor query(String where, String[] whereArgs,
			String groupBy, String having, String orderBy, String limit) {
		return (FeatureCursor) db.query(getTableName(),
				columns.getColumnNames(), where, whereArgs, groupBy, having,
				orderBy, limit);
	}

	/**
	 * Update the feature row
	 * 
	 * @param row
	 * @return number of rows affected, should be 0 or 1
	 */
	public int update(FeatureRow row) {
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
	 * Delete the feature row
	 * 
	 * @param row
	 * @return number of rows affected, should be 0 or 1
	 */
	public int delete(FeatureRow row) {
		return deleteById(row.getId());
	}

	/**
	 * Delete a row by id
	 * 
	 * @param row
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
	 * Get a new empty feature row
	 * 
	 * @return
	 */
	public FeatureRow newRow() {
		return new FeatureRow(columns);
	}

	/**
	 * Creates a new feature, same as calling {@link #insert(FeatureRow)}
	 * 
	 * @param row
	 * @return row id
	 */
	public long create(FeatureRow row) {
		return insert(row);
	}

	/**
	 * Inserts a new feature row
	 * 
	 * @param row
	 * @return row id
	 */
	public long insert(FeatureRow row) {
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
		return buildWhere(columns.getPkColumn().getName(), id);
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
	 * Build where (or selection) statement for a single field
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	public String buildWhere(String field, Object value) {
		return field + " " + (value != null ? "= ?" : "IS NULL");
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

}
