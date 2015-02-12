package mil.nga.giat.geopackage.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.R;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.db.GeoPackageTableCreator;
import mil.nga.giat.geopackage.io.GeoPackageFileUtils;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

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
	public List<String> databases() {

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
			GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(
					context, db);

			// Create the Spatial Reference System table (spec Requirement 10)
			tableCreator.createSpatialReferenceSystem();

			// Create the Contents table (spec Requirement 13)
			tableCreator.createContents();

			// Create the required Spatial Reference Systems (spec Requirement
			// 11)
			ConnectionSource connectionSource = new AndroidConnectionSource(db);
			try {
				SpatialReferenceSystemDao dao = DaoManager.createDao(
						connectionSource, SpatialReferenceSystem.class);
				dao.createEpsg(context);
				dao.createUndefinedCartesian(context);
				dao.createUndefinedGeographic(context);
			} catch (SQLException e) {
				throw new GeoPackageException(
						"Error creating default required Spatial Reference Systems",
						e);
			}

			db.close();
			created = true;
		}

		return created;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(File file) {
		return importGeoPackage(null, file, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(File file, boolean override) {
		return importGeoPackage(null, file, override);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(String name, File file) {
		return importGeoPackage(name, file, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(String name, File file, boolean override) {

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

		boolean success = false;
		try {
			FileInputStream geoPackageStream = new FileInputStream(file);
			success = importGeoPackage(database, override, geoPackageStream);
		} catch (FileNotFoundException e) {
			throw new GeoPackageException(
					"Failed read or write GeoPackage file '" + file
							+ "' to database: '" + database + "'", e);
		}

		return success;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(String name, URL url) {
		return importGeoPackage(name, url, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importGeoPackage(String name, URL url, boolean override) {

		boolean success = false;

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new GeoPackageException("Failed to import GeoPackage "
						+ name + " from URL: '" + url.toString() + "'. HTTP "
						+ connection.getResponseCode() + " "
						+ connection.getResponseMessage());
			}

			InputStream geoPackageStream = connection.getInputStream();
			success = importGeoPackage(name, override, geoPackageStream);
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to import GeoPackage " + name, e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return success;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportGeoPackage(String database, File directory) {
		exportGeoPackage(database, database, directory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportGeoPackage(String database, String name, File directory) {

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
			GeoPackageCursorFactory cursorFactory = new GeoPackageCursorFactory();
			SQLiteDatabase sqlite = context.openOrCreateDatabase(database,
					Context.MODE_PRIVATE, cursorFactory);
			GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(
					context, sqlite);
			db = new GeoPackageImpl(sqlite, cursorFactory, tableCreator);
		}

		return db;
	}

	/**
	 * Import the GeoPackage stream
	 * 
	 * @param database
	 * @param override
	 * @param geoPackageStream
	 * @return true if imported successfully
	 */
	private boolean importGeoPackage(String database, boolean override,
			InputStream geoPackageStream) {

		if (!override && exists(database)) {
			throw new GeoPackageException(
					"GeoPackage database already exists: " + database);
		}

		// Copy the geopackage over as a database
		File newDbFile = context.getDatabasePath(database);
		try {
			GeoPackageFileUtils.copyFile(geoPackageStream, newDbFile);
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to import GeoPackage database: " + database, e);
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
