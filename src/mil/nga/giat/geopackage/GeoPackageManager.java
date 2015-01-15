package mil.nga.giat.geopackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.giat.geopackage.script.GeoPackageScriptExecutor;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * GeoPackage Database management and CRUD functionality
 * 
 * @author osbornb
 */
public class GeoPackageManager {

	/**
	 * Logger tag
	 */
	private static final String TAG = GeoPackageManager.class.getSimpleName();

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public GeoPackageManager(Context context) {
		this.context = context;
	}

	/**
	 * List all GeoPackage databases
	 * 
	 * @return database list
	 */
	public List<String> databaseList() {

		List<String> databases = new ArrayList<String>();

		String[] databaseArray = context.databaseList();
		for (String database : databaseArray) {
			if (!isTemporary(database)) {
				databases.add(database);
			}
		}

		return databases;
	}

	/**
	 * Set of all GeoPackage databases
	 * 
	 * @return database set
	 */
	public Set<String> databaseSet() {

		Set<String> databases = new HashSet<String>();

		String[] databaseArray = context.databaseList();
		for (String database : databaseArray) {
			if (!isTemporary(database)) {
				databases.add(database);
			}
		}

		return databases;
	}

	/**
	 * Determine if the database exists
	 * 
	 * @param database
	 * @return true if exists
	 */
	public boolean exists(String database) {
		return databaseSet().contains(database);
	}

	/**
	 * Delete a database
	 * 
	 * @param database
	 * @return true if deleted
	 */
	public boolean delete(String database) {
		return context.deleteDatabase(database);
	}

	/**
	 * Create a new GeoPackage database
	 * 
	 * @param database
	 * @return
	 */
	public boolean create(String database) {

		boolean created = false;

		if (exists(database)) {
			Log.w(TAG, "Database already exists and could not be created: "
					+ database);
		} else {
			SQLiteDatabase db = context.openOrCreateDatabase(database,
					Context.MODE_PRIVATE, null);
			GeoPackageScriptExecutor scriptExecutor = new GeoPackageScriptExecutor(
					context, db);
			scriptExecutor.createSpatialReferenceSystem();
			// TODO create a new GeoPackage database with at least minimal
			// required tables gpkg_spatial_ref_sys and gpkg_contents

			db.close();
			created = true;
		}

		return created;
	}

	/**
	 * Load and import a GeoPackage file
	 * 
	 * @param path
	 * @return true if loaded
	 * @throws GeoPackageException
	 *             on failure to load file or if database already exists
	 */
	public boolean load(String path) throws GeoPackageException {
		return load(path, false);
	}

	/**
	 * Load and import a GeoPackage file
	 * 
	 * @param path
	 * @param override
	 *            true to override existing
	 * @return true if created successfully
	 * @throws GeoPackageException
	 *             on failure to load file or if no overriding & database
	 *             already exists
	 */
	public boolean load(String path, boolean override)
			throws GeoPackageException {

		File file = new File(path);

		String database = GeoPackageFileManager.getDatabaseName(file);

		if (!override && exists(database)) {
			throw new GeoPackageException(
					"GeoPackage database already exists: '" + database);
		}

		try {
			InputStream importFile = new FileInputStream(file);

			File newDbFile = context.getDatabasePath(database);
			OutputStream newDb = new FileOutputStream(newDbFile);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = importFile.read(buffer)) > 0) {
				newDb.write(buffer, 0, length);
			}

			newDb.flush();
			newDb.close();
			importFile.close();
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed read or write GeoPackage file '" + file
							+ "' to database: '" + database, e);
		}

		return exists(database);
	}

	/**
	 * Open the database
	 * 
	 * @param database
	 * @return
	 */
	public GeoPackage open(String database) {

		GeoPackage db = null;

		if (exists(database)) {
			SQLiteDatabase sqlite = context.openOrCreateDatabase(database,
					Context.MODE_PRIVATE, null);
			db = new GeoPackage(sqlite);
		}

		return db;
	}

	/**
	 * Check if the database is temporary (rollback journal)
	 * 
	 * @param database
	 * @return
	 */
	private boolean isTemporary(String database) {
		return database.endsWith(GeoPackageConstants.ROLLBACK_JOURNAL_SUFFIX);
	}

}
