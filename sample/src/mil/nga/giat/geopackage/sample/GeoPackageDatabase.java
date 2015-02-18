package mil.nga.giat.geopackage.sample;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Active feature and tile tables within a GeoPackage database
 * 
 * @author osbornb
 */
public class GeoPackageDatabase {

	/**
	 * Map of table names to feature tables
	 */
	private final Map<String, GeoPackageTable> features = new HashMap<String, GeoPackageTable>();

	/**
	 * Map of tables names to tile tables
	 */
	private final Map<String, GeoPackageTable> tiles = new HashMap<String, GeoPackageTable>();

	/**
	 * Database name
	 */
	private String database;

	/**
	 * Constructor
	 * 
	 * @param database
	 */
	public GeoPackageDatabase(String database) {
		this.database = database;
	}

	/**
	 * Get the feature tables
	 * 
	 * @return
	 */
	public Collection<GeoPackageTable> getFeatures() {
		return features.values();
	}

	/**
	 * Get the tile tables
	 * 
	 * @return
	 */
	public Collection<GeoPackageTable> getTiles() {
		return tiles.values();
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getDatabase() {
		return database;
	}

	/**
	 * Check if the table exists in this table, is active
	 * 
	 * @param table
	 * @return
	 */
	public boolean exists(GeoPackageTable table) {
		boolean exists = false;
		if (table.isFeature()) {
			exists = features.containsKey(table.getName());
		} else {
			exists = tiles.containsKey(table.getName());
		}
		return exists;
	}

	/**
	 * Add a table
	 * 
	 * @param table
	 */
	public void add(GeoPackageTable table) {
		if (table.isFeature()) {
			features.put(table.getName(), table);
		} else {
			tiles.put(table.getName(), table);
		}
	}

	/**
	 * Remove a table
	 * 
	 * @param table
	 */
	public void remove(GeoPackageTable table) {
		if (table.isFeature()) {
			features.remove(table.getName());
		} else {
			tiles.remove(table.getName());
		}
	}

	/**
	 * Remove a table name
	 * 
	 * @param tableName
	 */
	public void remove(String tableName) {
		features.remove(tableName);
		tiles.remove(tableName);
	}

	/**
	 * Empty if no active tile or feature tables
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return features.isEmpty() && tiles.isEmpty();
	}

}
