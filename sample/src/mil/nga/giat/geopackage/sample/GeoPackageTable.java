package mil.nga.giat.geopackage.sample;

public class GeoPackageTable {

	public final String name;

	public final String database;

	public final int count;

	public final boolean tile;

	public static GeoPackageTable createFeature(String database, String name,
			int count) {
		return new GeoPackageTable(database, name, count, false);
	}

	public static GeoPackageTable createTile(String database, String name,
			int count) {
		return new GeoPackageTable(database, name, count, true);
	}

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

}
