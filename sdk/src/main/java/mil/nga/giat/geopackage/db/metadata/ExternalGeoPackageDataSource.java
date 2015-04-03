package mil.nga.giat.geopackage.db.metadata;

import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackageException;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * External GeoPackage Data Source
 * 
 * @author osbornb
 */
public class ExternalGeoPackageDataSource {

	/**
	 * Database
	 */
	private SQLiteDatabase db;

	/**
	 * Helper
	 */
	private GeoPackageMetadataOpenHelper helper;

	/**
	 * Columns
	 */
	private String[] columns = { GeoPackageMetadataOpenHelper.COLUMN_NAME,
			GeoPackageMetadataOpenHelper.COLUMN_PATH };

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public ExternalGeoPackageDataSource(Context context) {
		helper = new GeoPackageMetadataOpenHelper(context);
	}

	/**
	 * Open the connection
	 */
	public void open() {
		db = helper.getWritableDatabase();
	}

	/**
	 * Close the connection
	 */
	public void close() {
		helper.close();
	}

	/**
	 * Create a new External GeoPackage
	 * 
	 * @param external
	 */
	public void create(ExternalGeoPackage external) {
		ContentValues values = new ContentValues();
		values.put(GeoPackageMetadataOpenHelper.COLUMN_NAME, external.getName());
		values.put(GeoPackageMetadataOpenHelper.COLUMN_PATH, external.getPath());
		long insertId = db.insert(
				GeoPackageMetadataOpenHelper.TABLE_EXTERNAL_GEOPACKAGE, null,
				values);
		if (insertId == -1) {
			throw new GeoPackageException(
					"Failed to insert external GeoPackage link. Name: "
							+ external.getName() + ", Path: "
							+ external.getPath());
		}
	}

	/**
	 * Delete the External GeoPackage
	 * 
	 * @param external
	 * @return
	 */
	public boolean delete(ExternalGeoPackage external) {
		return delete(external.getName());
	}

	/**
	 * Delete the database
	 * 
	 * @param database
	 * @return
	 */
	public boolean delete(String database) {
		String whereClause = GeoPackageMetadataOpenHelper.COLUMN_NAME + " = ?";
		String[] whereArgs = new String[] { database };
		int deleteCount = db.delete(
				GeoPackageMetadataOpenHelper.TABLE_EXTERNAL_GEOPACKAGE,
				whereClause, whereArgs);
		return deleteCount > 0;
	}

	/**
	 * Rename the external GeoPackage to the new name
	 * 
	 * @param external
	 * @param newName
	 * @return
	 */
	public boolean rename(ExternalGeoPackage external, String newName) {
		boolean renamed = rename(external.getName(), newName);
		if (renamed) {
			external.setName(newName);
		}
		return renamed;
	}

	/**
	 * Rename the external GeoPackage name to the new name
	 * 
	 * @param name
	 * @param newName
	 * @return
	 */
	public boolean rename(String name, String newName) {
		String whereClause = GeoPackageMetadataOpenHelper.COLUMN_NAME + " = ?";
		String[] whereArgs = new String[] { name };
		ContentValues values = new ContentValues();
		values.put(GeoPackageMetadataOpenHelper.COLUMN_NAME, newName);
		int updateCount = db.update(
				GeoPackageMetadataOpenHelper.TABLE_EXTERNAL_GEOPACKAGE, values,
				whereClause, whereArgs);
		return updateCount > 0;
	}

	/**
	 * Get all External GeoPackages
	 * 
	 * @return
	 */
	public List<ExternalGeoPackage> getAll() {
		List<ExternalGeoPackage> externals = new ArrayList<ExternalGeoPackage>();

		Cursor cursor = db.query(
				GeoPackageMetadataOpenHelper.TABLE_EXTERNAL_GEOPACKAGE,
				columns, null, null, null, null, null);
		try {
			while (cursor.moveToNext()) {
				ExternalGeoPackage external = new ExternalGeoPackage();
				external.setName(cursor.getString(0));
				external.setPath(cursor.getString(1));
				externals.add(external);
			}
		} finally {
			cursor.close();
		}
		return externals;
	}

	/**
	 * Get all External GeoPackage name
	 * 
	 * @return
	 */
	public List<String> getAllNames() {
		List<String> externals = new ArrayList<String>();

		Cursor cursor = db.query(
				GeoPackageMetadataOpenHelper.TABLE_EXTERNAL_GEOPACKAGE,
				columns, null, null, null, null, null);
		try {
			while (cursor.moveToNext()) {
				externals.add(cursor.getString(0));
			}
		} finally {
			cursor.close();
		}
		return externals;
	}

	/**
	 * Get External GeoPackage by name
	 * 
	 * @return
	 */
	public ExternalGeoPackage get(String database) {
		ExternalGeoPackage external = null;
		String selection = GeoPackageMetadataOpenHelper.COLUMN_NAME + " = ?";
		String[] selectionArgs = new String[] { database };
		Cursor cursor = db.query(
				GeoPackageMetadataOpenHelper.TABLE_EXTERNAL_GEOPACKAGE,
				columns, selection, selectionArgs, null, null, null);
		try {

			if (cursor.moveToNext()) {
				external = new ExternalGeoPackage();
				external.setName(cursor.getString(0));
				external.setPath(cursor.getString(1));
			}
		} finally {
			cursor.close();
		}
		return external;
	}

	/**
	 * Determine if the database exists as an External GeoPackage
	 * 
	 * @param database
	 * @return
	 */
	public boolean exists(String database) {
		return get(database) != null;
	}

}
