package mil.nga.giat.geopackage.sample;

import mil.nga.giat.geopackage.geom.GeometryType;

/**
 * GeoPackage table information
 * 
 * @author osbornb
 */
public class GeoPackageTable {

	/**
	 * Table name
	 */
	public final String name;

	/**
	 * Database name
	 */
	public String database;

	/**
	 * Count of features or tiles
	 */
	public final int count;

	/**
	 * True if a tile, false if a feature
	 */
	public final boolean tile;

	/**
	 * True when currently active or checked
	 */
	public boolean active = false;

	/**
	 * Geometry Type
	 */
	public GeometryType geometryType;

	/**
	 * Create a new feature table
	 * 
	 * @param database
	 * @param name
	 * @param geometryType
	 * @param count
	 * @return
	 */
	public static GeoPackageTable createFeature(String database, String name,
			GeometryType geometryType, int count) {
		GeoPackageTable table = new GeoPackageTable(database, name, count,
				false);
		table.setGeometryType(geometryType);
		return table;
	}

	/**
	 * Create a new tile table
	 * 
	 * @param database
	 * @param name
	 * @param count
	 * @return
	 */
	public static GeoPackageTable createTile(String database, String name,
			int count) {
		return new GeoPackageTable(database, name, count, true);
	}

	/**
	 * Constructor
	 * 
	 * @param database
	 * @param name
	 * @param count
	 * @param tile
	 */
	private GeoPackageTable(String database, String name, int count,
			boolean tile) {
		this.database = database;
		this.name = name;
		this.count = count;
		this.tile = tile;
	}

	public String getName() {
		return name;
	}

	public String getDatabase() {
		return database;
	}

	public int getCount() {
		return count;
	}

	public boolean isTile() {
		return tile;
	}

	public boolean isFeature() {
		return !tile;
	}

	public boolean isActive() {
		return active;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public GeometryType getGeometryType() {
		return geometryType;
	}

	public void setGeometryType(GeometryType geometryType) {
		this.geometryType = geometryType;
	}

}
