package mil.nga.giat.geopackage.data.c4;

import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * Feature Row containing the values from a single cursor row
 * 
 * @author osbornb
 */
public class FeatureRow {

	/**
	 * Feature columns of the table
	 */
	private final FeatureColumns columns;

	/**
	 * Cursor column types of this row, based upon the data values
	 */
	private final int[] columnTypes;

	/**
	 * Array of row values
	 */
	private final Object[] values;

	/**
	 * Constructor
	 * 
	 * @param columns
	 * @param columnTypes
	 * @param values
	 */
	FeatureRow(FeatureColumns columns, int[] columnTypes, Object[] values) {
		this.columns = columns;
		this.columnTypes = columnTypes;
		this.values = values;
	}

	/**
	 * Get the column count
	 * 
	 * @return
	 */
	public int count() {
		return columns.count();
	}

	/**
	 * Get the column names
	 * 
	 * @return
	 */
	public String[] getNames() {
		return columns.getColumnNames();
	}

	/**
	 * Get the column name at the index
	 * 
	 * @param index
	 * @return
	 */
	public String getName(int index) {
		return columns.getColumnName(index);
	}

	/**
	 * Get the index of the column name
	 * 
	 * @param columnName
	 * @return
	 */
	public int getIndex(String columnName) {
		return columns.getColumnIndex(columnName);
	}

	/**
	 * Get the value at the index
	 * 
	 * @param index
	 * @return
	 */
	public Object getValue(int index) {
		return values[index];
	}

	/**
	 * Get the value of the column name
	 * 
	 * @param columnName
	 * @return
	 */
	public Object getValue(String columnName) {
		return values[columns.getColumnIndex(columnName)];
	}

	/**
	 * Get the row values
	 * 
	 * @return
	 */
	public Object[] getValues() {
		return values;
	}

	/**
	 * Get the Cursor column data types
	 * 
	 * @return
	 * @see Cursor#FIELD_TYPE_STRING
	 * @see Cursor#FIELD_TYPE_INTEGER
	 * @see Cursor#FIELD_TYPE_FLOAT
	 * @see Cursor#FIELD_TYPE_BLOB
	 * @see Cursor#FIELD_TYPE_NULL
	 */
	public int[] getRowColumnTypes() {
		return columnTypes;
	}

	/**
	 * Get the Cursor column data type at the index
	 * 
	 * @param index
	 * @return
	 * @see Cursor#FIELD_TYPE_STRING
	 * @see Cursor#FIELD_TYPE_INTEGER
	 * @see Cursor#FIELD_TYPE_FLOAT
	 * @see Cursor#FIELD_TYPE_BLOB
	 * @see Cursor#FIELD_TYPE_NULL
	 */
	public int getRowColumnType(int index) {
		return columnTypes[index];
	}

	/**
	 * Get the Cursor column data type of the column name
	 * 
	 * @param columnName
	 * @return
	 * @see Cursor#FIELD_TYPE_STRING
	 * @see Cursor#FIELD_TYPE_INTEGER
	 * @see Cursor#FIELD_TYPE_FLOAT
	 * @see Cursor#FIELD_TYPE_BLOB
	 * @see Cursor#FIELD_TYPE_NULL
	 */
	public int getRowColumnType(String columnName) {
		return columnTypes[columns.getColumnIndex(columnName)];
	}

	/**
	 * Get the feature columns
	 * 
	 * @return
	 */
	public FeatureColumns getColumns() {
		return columns;
	}

	/**
	 * Get the feature column at the index
	 * 
	 * @param index
	 * @return
	 */
	public FeatureColumn getColumn(int index) {
		return columns.getColumn(index);
	}

	/**
	 * Get the feature column of the column name
	 * 
	 * @param columnName
	 * @return
	 */
	public FeatureColumn getColumn(String columnName) {
		return columns.getColumn(columnName);
	}

	/**
	 * Get the id value, which is the value of the primary key
	 * 
	 * @return
	 */
	public int getId() {
		int id;
		Object objectValue = getValue(getPkIndex());
		if (objectValue == null) {
			throw new GeoPackageException("Feature Row Id was null. Table: "
					+ columns.getTableName() + ", Column Index: "
					+ getPkIndex() + ", Column Name: "
					+ getPkColumn().getName());
		}
		if (objectValue instanceof Number) {
			id = ((Number) objectValue).intValue();
		} else {
			throw new GeoPackageException(
					"Feature Row Id was not a number. Table: "
							+ columns.getTableName() + ", Column Index: "
							+ getPkIndex() + ", Column Name: "
							+ getPkColumn().getName());
		}

		return id;
	}

	/**
	 * Get the primary key column index
	 * 
	 * @return
	 */
	public int getPkIndex() {
		return columns.getPkIndex();
	}

	/**
	 * Get the primary key feature column
	 * 
	 * @return
	 */
	public FeatureColumn getPkColumn() {
		return columns.getPkColumn();
	}

	/**
	 * Get the geometry column index
	 * 
	 * @return
	 */
	public int getGeometryIndex() {
		return columns.getGeometryIndex();
	}

	/**
	 * Get the geometry feature column
	 * 
	 * @return
	 */
	public FeatureColumn getGeometryColumn() {
		return columns.getGeometryColumn();
	}

	/**
	 * Get the geometry
	 * 
	 * @return
	 */
	public GeoPackageGeometryData getGeometry() {
		GeoPackageGeometryData geometryData = null;
		Object value = getValue(getGeometryIndex());
		if (value != null) {
			geometryData = (GeoPackageGeometryData) value;
		}
		return geometryData;
	}

	/**
	 * Set the column value at the index
	 * 
	 * @param index
	 * @param value
	 */
	public void setValue(int index, Object value) {
		if (index == columns.getPkIndex()) {
			throw new GeoPackageException(
					"Can not update the primary key of the feature row. Table Name: "
							+ columns.getTableName() + ", Index: " + index
							+ ", Name: " + columns.getPkColumn().getName());
		}
		values[index] = value;
	}

	/**
	 * Convert the feature row to content values
	 * 
	 * @return
	 */
	public ContentValues toContentValues() {

		ContentValues contentValues = new ContentValues();
		for (FeatureColumn column : columns.getColumns()) {

			if (!column.isPrimaryKey()) {

				Object value = values[column.getIndex()];
				String columnName = column.getName();

				if (value == null) {
					contentValues.putNull(columnName);
				} else if (column.isGeometry()) {
					if (value instanceof GeoPackageGeometryData) {
						GeoPackageGeometryData geometryData = (GeoPackageGeometryData) value;
						contentValues.put(columnName, geometryData.toBytes());
					} else if (value instanceof byte[]) {
						contentValues.put(columnName, (byte[]) value);
					} else {
						throw new GeoPackageException(
								"Unsupported update geometry column value type. column: "
										+ columnName + ", value type: "
										+ value.getClass().getName());
					}
				} else {

					if (value instanceof Number) {
						if (value instanceof Integer) {
							contentValues.put(columnName, (Integer) value);
						} else if (value instanceof Float) {
							contentValues.put(columnName, (Integer) value);
						} else if (value instanceof Long) {
							contentValues.put(columnName, (Long) value);
						} else if (value instanceof Double) {
							contentValues.put(columnName, (Double) value);
						} else if (value instanceof Short) {
							contentValues.put(columnName, (Short) value);
						} else if (value instanceof Byte) {
							contentValues.put(columnName, (Byte) value);
						}
					} else if (value instanceof String) {
						contentValues.put(columnName, (String) value);
					} else if (value instanceof byte[]) {
						contentValues.put(columnName, (byte[]) value);
					} else if (value instanceof Boolean) {
						contentValues.put(columnName, (Boolean) value);
					} else {
						throw new GeoPackageException(
								"Unsupported update column value. column: "
										+ columnName + ", value: " + value);
					}
				}

			}

		}

		return contentValues;
	}

}
