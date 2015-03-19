package mil.nga.giat.geopackage.sample;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Collection of active GeoPackage database tables
 * 
 * @author osbornb
 */
public class GeoPackageDatabases {

	/**
	 * Database preferences value
	 */
	private static final String DATABASES_PREFERENCE = "databases";
	
	/**
	 * Tile Tables Preference suffix
	 */
	private static final String TILE_TABLES_PREFERENCE_SUFFIX = "_tile_tables";
	
	/**
	 * Feature Tables Preference suffix
	 */
	private static final String FEATURE_TABLES_PREFERENCE_SUFFIX = "_feature_tables";

	/**
	 * Singleton instance
	 */
	private static GeoPackageDatabases instance;

	/**
	 * Initialization lock
	 */
	private static final Lock initializeLock = new ReentrantLock();

	/**
	 * Get the singleton instance
	 * 
	 * @param context
	 * @return
	 */
	public static GeoPackageDatabases getInstance(Context context) {
		if (instance == null) {
			try {
				initializeLock.lock();
				if (instance == null) {
					SharedPreferences preferences = PreferenceManager
							.getDefaultSharedPreferences(context);
					GeoPackageDatabases active = new GeoPackageDatabases();
					active.setPreferences(preferences);
					active.loadFromPreferences();
					instance = active;
				}
			} finally {
				initializeLock.unlock();
			}
		}
		return instance;
	}

	/**
	 * Map of databases
	 */
	private Map<String, GeoPackageDatabase> databases = new HashMap<String, GeoPackageDatabase>();

	/**
	 * Shared preference settings
	 */
	private SharedPreferences settings;

	/**
	 * Modified flag
	 */
	private boolean modified = false;

	/**
	 * Constructor
	 */
	private GeoPackageDatabases() {

	}

	/**
	 * Set the shared preferences
	 * 
	 * @param settings
	 */
	public void setPreferences(SharedPreferences settings) {
		this.settings = settings;
	}

	/**
	 * Check if the table exists in this collection of tables, is active
	 * 
	 * @param table
	 * @return
	 */
	public boolean exists(GeoPackageTable table) {
		boolean exists = false;
		GeoPackageDatabase database = databases.get(table.getDatabase());
		if (database != null) {
			exists = database.exists(table);
		}
		return exists;
	}

	/**
	 * Get the database
	 * 
	 * @return
	 */
	public Collection<GeoPackageDatabase> getDatabases() {
		return databases.values();
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * Add a table
	 * 
	 * @param table
	 */
	public void addTable(GeoPackageTable table) {
		addTable(table, true);
	}

	/**
	 * Add a table and update the saved preferences if flag is set
	 * 
	 * @param table
	 * @param updatePreferences
	 */
	public void addTable(GeoPackageTable table, boolean updatePreferences) {
		GeoPackageDatabase database = databases.get(table.getDatabase());
		if (database == null) {
			database = new GeoPackageDatabase(table.getDatabase());
			databases.put(table.getDatabase(), database);
		}
		database.add(table);
		if (updatePreferences) {
			addTableToPreferences(table);
		}
		setModified(true);
	}

	/**
	 * Remove a table
	 * 
	 * @param table
	 */
	public void removeTable(GeoPackageTable table) {
		GeoPackageDatabase database = databases.get(table.getDatabase());
		if (database != null) {
			database.remove(table);
			removeTableFromPreferences(table);
			if (database.isEmpty()) {
				databases.remove(database.getDatabase());
				removeDatabaseFromPreferences(database.getDatabase());
			}
			setModified(true);
		}
	}

	/**
	 * Remove all databases
	 */
	public void removeAll() {
		Set<String> allDatabases = new HashSet<String>();
		allDatabases.addAll(databases.keySet());
		for (String database : allDatabases) {
			removeDatabase(database);
		}
	}

	/**
	 * Remove a database
	 * 
	 * @param database
	 */
	public void removeDatabase(String database) {
		databases.remove(database);
		removeDatabaseFromPreferences(database);
		setModified(true);
	}

	/**
	 * Rename a database
	 * 
	 * @param database
	 * @param newDatabase
	 */
	public void renameDatabase(String database, String newDatabase) {
		GeoPackageDatabase geoPackageDatabase = databases.remove(database);
		if (geoPackageDatabase != null) {
			geoPackageDatabase.setDatabase(newDatabase);
			databases.put(newDatabase, geoPackageDatabase);
			removeDatabaseFromPreferences(database);
			for (GeoPackageTable featureTable : geoPackageDatabase
					.getFeatures()) {
				featureTable.setDatabase(newDatabase);
				addTableToPreferences(featureTable);
			}
			for (GeoPackageTable tileTable : geoPackageDatabase.getTiles()) {
				tileTable.setDatabase(newDatabase);
				addTableToPreferences(tileTable);
			}
		}
		setModified(true);
	}

	/**
	 * Load the GeoPackage databases from the saved preferences
	 */
	public void loadFromPreferences() {
		databases.clear();
		Set<String> databases = settings.getStringSet(DATABASES_PREFERENCE,
				new HashSet<String>());
		for (String database : databases) {
			Set<String> tiles = settings
					.getStringSet(getTileTablesPreferenceKey(database),
							new HashSet<String>());
			Set<String> features = settings.getStringSet(
					getFeatureTablesPreferenceKey(database),
					new HashSet<String>());

			for (String tile : tiles) {
				addTable(GeoPackageTable.createTile(database, tile, 0), false);
			}
			for (String feature : features) {
				addTable(GeoPackageTable.createFeature(database, feature, null,
						0), false);
			}
		}
	}

	/**
	 * Remove the database from the saved preferences
	 * 
	 * @param database
	 */
	private void removeDatabaseFromPreferences(String database) {
		Editor editor = settings.edit();

		Set<String> databases = settings.getStringSet(DATABASES_PREFERENCE,
				new HashSet<String>());
		if (databases != null && databases.contains(database)) {
			Set<String> newDatabases = new HashSet<String>();
			newDatabases.addAll(databases);
			newDatabases.remove(database);
			editor.putStringSet(DATABASES_PREFERENCE, newDatabases);
		}
		editor.remove(getTileTablesPreferenceKey(database));
		editor.remove(getFeatureTablesPreferenceKey(database));

		editor.commit();
	}

	/**
	 * Remove a table from the preferences
	 * 
	 * @param table
	 */
	private void removeTableFromPreferences(GeoPackageTable table) {
		Editor editor = settings.edit();

		if (table.isTile()) {
			Set<String> tiles = settings.getStringSet(
					getTileTablesPreferenceKey(table), new HashSet<String>());
			if (tiles != null && tiles.contains(table.getName())) {
				Set<String> newTiles = new HashSet<String>();
				newTiles.addAll(tiles);
				newTiles.remove(table.getName());
				editor.putStringSet(getTileTablesPreferenceKey(table), newTiles);
			}
		} else {
			Set<String> features = settings
					.getStringSet(getFeatureTablesPreferenceKey(table),
							new HashSet<String>());
			if (features != null && features.contains(table.getName())) {
				Set<String> newFeatures = new HashSet<String>();
				newFeatures.addAll(features);
				newFeatures.remove(table.getName());
				editor.putStringSet(getFeatureTablesPreferenceKey(table),
						newFeatures);
			}
		}

		editor.commit();
	}

	/**
	 * Add a table to the preferences, updating the saved databases and tables
	 * as needed
	 * 
	 * @param table
	 */
	private void addTableToPreferences(GeoPackageTable table) {
		Editor editor = settings.edit();

		Set<String> databases = settings.getStringSet(DATABASES_PREFERENCE,
				new HashSet<String>());
		if (databases == null || !databases.contains(table.getDatabase())) {
			Set<String> newDatabases = new HashSet<String>();
			if (databases != null) {
				newDatabases.addAll(databases);
			}
			newDatabases.add(table.getDatabase());
			editor.putStringSet(DATABASES_PREFERENCE, newDatabases);
		}

		if (table.isTile()) {
			Set<String> tiles = settings.getStringSet(
					getTileTablesPreferenceKey(table), new HashSet<String>());
			if (tiles == null || !tiles.contains(table.getName())) {
				Set<String> newTiles = new HashSet<String>();
				if (tiles != null) {
					newTiles.addAll(tiles);
				}
				newTiles.add(table.getName());
				editor.putStringSet(getTileTablesPreferenceKey(table), newTiles);
			}
		} else {
			Set<String> features = settings
					.getStringSet(getFeatureTablesPreferenceKey(table),
							new HashSet<String>());
			if (features == null || !features.contains(table.getName())) {
				Set<String> newFeatures = new HashSet<String>();
				if (features != null) {
					newFeatures.addAll(features);
				}
				newFeatures.add(table.getName());
				editor.putStringSet(getFeatureTablesPreferenceKey(table),
						newFeatures);
			}
		}

		editor.commit();
	}

	/**
	 * Get the Tiles Table Preference Key from table
	 * 
	 * @param table
	 * @return
	 */
	private String getTileTablesPreferenceKey(GeoPackageTable table) {
		return getTileTablesPreferenceKey(table.getDatabase());
	}

	/**
	 * Get the Tiles Table Preference Key from database name
	 * 
	 * @param database
	 * @return
	 */
	private String getTileTablesPreferenceKey(String database) {
		return database + TILE_TABLES_PREFERENCE_SUFFIX;
	}

	/**
	 * Get the Feature Table Preference Key from table
	 * 
	 * @param table
	 * @return
	 */
	private String getFeatureTablesPreferenceKey(GeoPackageTable table) {
		return getFeatureTablesPreferenceKey(table.getDatabase());
	}

	/**
	 * Get the Feature Table Preference Key from database name
	 * 
	 * @param database
	 * @return
	 */
	private String getFeatureTablesPreferenceKey(String database) {
		return database + FEATURE_TABLES_PREFERENCE_SUFFIX;
	}

}
