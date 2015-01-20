package mil.nga.giat.geopackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.giat.geopackage.script.GeoPackageScriptExecutor;
import mil.nga.giat.geopackage.util.GeoPackageException;
import mil.nga.giat.geopackage.util.GeoPackageFileUtils;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * GeoPackage Database management implementation
 * 
 * @author osbornb
 */
class GeoPackageManagerImpl implements GeoPackageManager {

	/**
	 * Logger tag
	 */
	private static final String TAG = GeoPackageManagerImpl.class
			.getSimpleName();

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	GeoPackageManagerImpl(Context context) {
		this.context = context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(String database) {
		return databaseSet().contains(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(String database) {
		return context.deleteDatabase(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean create(String database) {

		boolean created = false;

		if (exists(database)) {
			Log.w(TAG, "Database already exists and could not be created: "
					+ database);
		} else {
			SQLiteDatabase db = context.openOrCreateDatabase(database,
					Context.MODE_PRIVATE, null);

			// Create the minimum required tables
			GeoPackageScriptExecutor scriptExecutor = new GeoPackageScriptExecutor(
					context, db);
			scriptExecutor.createSpatialReferenceSystem();
			scriptExecutor.createContents();

			db.close();
			created = true;
		}

		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(File file) throws GeoPackageException {
		return importGeoPackage(null, file, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(File file, boolean override)
			throws GeoPackageException {
		return importGeoPackage(null, file, override);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(String name, File file)
			throws GeoPackageException {
		return importGeoPackage(name, file, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(String name, File file, boolean override)
			throws GeoPackageException {

		// Verify the file has the right extension
		if (!hasGeoPackageExtension(file)) {
			throw new GeoPackageException(
					"GeoPackage database file '"
							+ file
							+ "' does not have a valid extension of '"
							+ context
									.getString(R.string.geopackage_file_suffix)
							+ "' or '"
							+ context
									.getString(R.string.geopackage_extended_file_suffix)
							+ "'");
		}

		// Use the provided name or the base file name as the database name
		String database;
		if (name != null) {
			database = name;
		} else {
			database = GeoPackageFileUtils.getFileNameWithoutExtension(file);
		}

		if (!override && exists(database)) {
			throw new GeoPackageException(
					"GeoPackage database already exists: " + database);
		}

		// Copy the geopackage file over as a database
		File newDbFile = context.getDatabasePath(database);
		try {
			GeoPackageFileUtils.copyFile(file, newDbFile);
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed read or write GeoPackage file '" + file
							+ "' to database: '" + database, e);
		}

		// Verify that the database is valid
		try {
			SQLiteDatabase sqlite = context.openOrCreateDatabase(database,
					Context.MODE_PRIVATE, null, new DatabaseErrorHandler() {
						@Override
						public void onCorruption(SQLiteDatabase dbObj) {
						}
					});
			sqlite.close();
		} catch (Exception e) {
			delete(database);
			throw new GeoPackageException("Invalid GeoPackage database file", e);
		}

		return exists(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportGeoPackage(String database, File directory)
			throws GeoPackageException {
		exportGeoPackage(database, database, directory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportGeoPackage(String database, String name, File directory)
			throws GeoPackageException {

		File file = new File(directory, name);

		// Add the extension if not on the name
		if (!hasGeoPackageExtension(file)) {
			name += "." + context.getString(R.string.geopackage_file_suffix);
			file = new File(directory, name);
		}

		// Copy the geopackage database to the new file location
		File dbFile = context.getDatabasePath(database);
		try {
			GeoPackageFileUtils.copyFile(dbFile, file);
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed read or write GeoPackage database '" + database
							+ "' to file: '" + file, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeoPackage open(String database) {

		GeoPackage db = null;

		if (exists(database)) {
			SQLiteDatabase sqlite = context.openOrCreateDatabase(database,
					Context.MODE_PRIVATE, null);
			db = new GeoPackageImpl(sqlite);
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
		return database.endsWith(context
				.getString(R.string.geopackage_db_rollback_suffix));
	}

	/**
	 * Check the file extension to see if it is a GeoPackage
	 * 
	 * @param file
	 * @return true if GeoPackage extension
	 */
	private boolean hasGeoPackageExtension(File file) {
		String extension = GeoPackageFileUtils.getFileExtension(file);
		boolean isGeoPackage = extension != null
				&& (extension.equalsIgnoreCase(context
						.getString(R.string.geopackage_file_suffix)) || extension
						.equalsIgnoreCase(context
								.getString(R.string.geopackage_extended_file_suffix)));
		return isGeoPackage;
	}

}
