package mil.nga.giat.geopackage.data.c4;

import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c2.Contents;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryType;
import mil.nga.giat.geopackage.util.GeoPackageException;
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
	 * List of columns
	 */
	private final String[] columns;

	/**
	 * Column index of the geometry column
	 */
	private final int geometryColumnIndex;

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

		// Query for the table columns
		Cursor cursor = db.query(geometryColumns.getTableName(), null, null,
				null, null, null, null);
		List<String> columnList = new ArrayList<String>();
		columns = cursor.getColumnNames();

		// Find the index of the geometry column
		int geomIndex = -1;
		for (int i = 0; i < columns.length; i++) {
			String columnName = columns[i];
			columnList.add(columnName);
			if (columnName.equals(geometryColumns.getColumnName())) {
				geomIndex = i;
				break;
			}
		}
		if (geomIndex == -1) {
			throw new GeoPackageException("Geometry column '"
					+ geometryColumns.getColumnName()
					+ "' was not found in table '"
					+ geometryColumns.getTableName() + "'. Columns: " + columns);
		}
		geometryColumnIndex = geomIndex;
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
	public String[] getColumns() {
		return columns;
	}

	/**
	 * The the Geometry Column index within the columns
	 * 
	 * @return
	 */
	public int getGeometryColumnIndex() {
		return geometryColumnIndex;
	}

	/**
	 * Query for all rows
	 * 
	 * @return
	 */
	public FeatureCursor queryForAll() {
		return (FeatureCursor) db.query(getTableName(), columns, null, null,
				null, null, null);
	}

}
