package mil.nga.giat.geopackage.sample;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GeoPackageDatabases {

	private Map<String, GeoPackageDatabase> databases = new HashMap<String, GeoPackageDatabase>();

	private SharedPreferences settings;

	public GeoPackageDatabases() {

	}

	public void setPreferences(SharedPreferences settings) {
		this.settings = settings;
	}

	public boolean exists(GeoPackageTable table) {
		boolean exists = false;
		GeoPackageDatabase database = databases.get(table.getDatabase());
		if (database != null) {
			exists = database.exists(table);
		}
		return exists;
	}

	public void addTable(GeoPackageTable table) {
		GeoPackageDatabase database = databases.get(table.getDatabase());
		if (database == null) {
			database = new GeoPackageDatabase(table.getDatabase());
			databases.put(table.getDatabase(), database);
		}
		database.add(table);
		addTableToPreferences(table);
	}

	public void removeTable(GeoPackageTable table) {
		GeoPackageDatabase database = databases.get(table.getDatabase());
		if (database != null) {
			database.remove(table);
			removeTableFromPreferences(table);
			if (database.isEmpty()) {
				databases.remove(database.getDatabase());
				removeDatabaseFromPreferences(database.getDatabase());
			}
		}
	}

	public void removeDatabase(String database) {
		databases.remove(database);
		removeDatabaseFromPreferences(database);
	}

	public void fromPreferences() {
		Set<String> databases = settings.getStringSet("databases",
				new HashSet<String>());
		for (String database : databases) {
			Set<String> tiles = settings.getStringSet(
					database + "_tile_tables", new HashSet<String>());
			Set<String> features = settings.getStringSet(database
					+ "_feature_tables", new HashSet<String>());

			for (String tile : tiles) {
				addTable(GeoPackageTable.createTile(database, tile, 0));
			}
			for (String feature : features) {
				addTable(GeoPackageTable.createFeature(database, feature, 0));
			}
		}
	}

	private void removeDatabaseFromPreferences(String database) {
		Editor editor = settings.edit();

		Set<String> databases = settings.getStringSet("databases",
				new HashSet<String>());
		databases.remove(database);
		editor.putStringSet("databases", databases);
		editor.remove(database + "_tile_tables");
		editor.remove(database + "_feature_tables");

		editor.commit();
	}

	private void removeTableFromPreferences(GeoPackageTable table) {
		Editor editor = settings.edit();

		if (table.isTile()) {
			Set<String> tiles = settings.getStringSet(table.getDatabase()
					+ "_tile_tables", new HashSet<String>());
			tiles.remove(table.getName());
			editor.putStringSet(table.getDatabase() + "_tile_tables", tiles);
		} else {
			Set<String> features = settings.getStringSet(table.getDatabase()
					+ "_feature_tables", new HashSet<String>());
			features.remove(table.getName());
			editor.putStringSet(table.getDatabase() + "_tile_tables", features);
		}

		editor.commit();
	}

	private void addTableToPreferences(GeoPackageTable table) {
		Editor editor = settings.edit();

		Set<String> databases = settings.getStringSet("databases",
				new HashSet<String>());
		if (databases == null) {
			databases = new HashSet<String>();
		}

		databases.add(table.getDatabase());
		editor.putStringSet("databases", databases);

		if (table.isTile()) {
			Set<String> tiles = settings.getStringSet(table.getDatabase()
					+ "_tile_tables", new HashSet<String>());
			if (tiles == null) {
				tiles = new HashSet<String>();
			}
			tiles.add(table.getName());
			editor.putStringSet(table.getDatabase() + "_tile_tables", tiles);
		} else {
			Set<String> features = settings.getStringSet(table.getDatabase()
					+ "_feature_tables", new HashSet<String>());
			if (features == null) {
				features = new HashSet<String>();
			}
			features.add(table.getName());
			editor.putStringSet(table.getDatabase() + "_tile_tables", features);
		}

		editor.commit();
	}

}
