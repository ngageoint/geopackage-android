package mil.nga.giat.geopackage.data.c4;

import java.util.ArrayList;
import java.util.List;

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
	 * Query for the row with the provided id
	 * 
	 * @param id
	 * @return
	 */
	public FeatureCursor queryForId(int id) {
		return (FeatureCursor) db.query(getTableName(),
				columns.getColumnNames(), getPkWhere(), getPkWhereArgs(id),
				null, null, null);
	}

	/**
	 * Query for the feature row with the provided id
	 * 
	 * @param id
	 * @return
	 */
	public FeatureRow queryForIdRow(int id) {
		FeatureRow row = null;
		FeatureCursor readCursor = queryForId(id);
		if (readCursor.moveToNext()) {
			row = readCursor.getRow();
		}
		readCursor.close();
		return row;
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
			updated = db.update(getTableName(), contentValues, getPkWhere(),
					getPkWhereArgs(row.getId()));
		}
		return updated;
	}

	/**
	 * Delete the feature row
	 * 
	 * @param row
	 * @return number of rows affected, should be 0 or 1
	 */
	public int delete(FeatureRow row) {
		return db.delete(getTableName(), getPkWhere(),
				getPkWhereArgs(row.getId()));
	}

	/**
	 * Get the primary key where clause
	 * 
	 * @return
	 */
	private String getPkWhere() {
		return columns.getPkColumn().getName() + " = ?";
	}

	/**
	 * Get the primary key where args
	 * 
	 * @return
	 */
	private String[] getPkWhereArgs(int id) {
		return new String[] { String.valueOf(id) };
	}
}
