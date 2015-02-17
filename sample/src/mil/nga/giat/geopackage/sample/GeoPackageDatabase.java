package mil.nga.giat.geopackage.sample;

import java.util.HashMap;
import java.util.Map;

public class GeoPackageDatabase {

	private final Map<String, GeoPackageTable> features = new HashMap<String, GeoPackageTable>();

	private final Map<String, GeoPackageTable> tiles = new HashMap<String, GeoPackageTable>();

	private final String database;

	public GeoPackageDatabase(String database) {
		this.database = database;
	}

	public Map<String, GeoPackageTable> getFeatures() {
		return features;
	}

	public Map<String, GeoPackageTable> getTiles() {
		return tiles;
	}

	public String getDatabase() {
		return database;
	}

	public boolean exists(GeoPackageTable table) {
		boolean exists = false;
		if (table.isFeature()) {
			exists = features.containsKey(table.getName());
		} else {
			exists = tiles.containsKey(table.getName());
		}
		return exists;
	}

	public void add(GeoPackageTable table) {
		if (table.isFeature()) {
			features.put(table.getName(), table);
		} else {
			tiles.put(table.getName(), table);
		}
	}

	public void remove(GeoPackageTable table) {
		if (table.isFeature()) {
			features.remove(table.getName());
		} else {
			tiles.remove(table.getName());
		}
	}

	public void remove(String tableName) {
		features.remove(tableName);
		tiles.remove(tableName);
	}

	public boolean isEmpty() {
		return features.isEmpty() && tiles.isEmpty();
	}
}
