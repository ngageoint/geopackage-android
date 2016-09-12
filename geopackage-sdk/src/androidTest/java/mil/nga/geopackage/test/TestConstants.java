package mil.nga.geopackage.test;

/**
 * Test constants
 * 
 * @author osbornb
 */
public class TestConstants {

	/**
	 * GeoPackage file extension
	 */
	public static final String GEO_PACKAGE_EXTENSION = "gpkg";

	/**
	 * Test database name
	 */
	public static final String TEST_DB_NAME = "test_db";

	/**
	 * Import database name
	 */
	public static final String IMPORT_DB_NAME = "import_db";

	/**
	 * Tiles database name
	 */
	public static final String TILES_DB_NAME = "tiles";

	/**
	 * Tiles 2 database name
	 */
	public static final String TILES2_DB_NAME = "tiles2";

	/**
	 * Import url
	 */
	public static final String IMPORT_URL = "http://www.geopackage.org/data/gdal_sample.gpkg";

	/**
	 * Import database file name, located in the test assets
	 */
	public static final String IMPORT_DB_FILE_NAME = IMPORT_DB_NAME + "."
			+ GEO_PACKAGE_EXTENSION;

	/**
	 * Tiles database file name, located in the test assets
	 */
	public static final String TILES_DB_FILE_NAME = TILES_DB_NAME + "."
			+ GEO_PACKAGE_EXTENSION;

	/**
	 * Tiles 2 database file name, located in the test assets
	 */
	public static final String TILES2_DB_FILE_NAME = TILES2_DB_NAME + "."
			+ GEO_PACKAGE_EXTENSION;

	/**
	 * Create elevation tiles database name
	 */
	public static final String CREATE_ELEVATION_TILES_DB_NAME = "elevation_tiles";

	/**
	 * Create elevation tiles database file name
	 */
	public static final String CREATE_ELEVATION_TILES_DB_FILE_NAME = CREATE_ELEVATION_TILES_DB_NAME
			+ "." + TestConstants.GEO_PACKAGE_EXTENSION;

	/**
	 * Import elevation tiles database name
	 */
	public static final String IMPORT_ELEVATION_TILES_DB_NAME = "elevation_tiles";

	/**
	 * Import elevation tiles tiff database name
	 */
	public static final String IMPORT_ELEVATION_TILES_TIFF_DB_NAME = "elevation_tiles_tiff";

	/**
	 * Import elevation tiles database file name, located in the test assets
	 */
	public static final String IMPORT_ELEVATION_TILES_DB_FILE_NAME = IMPORT_ELEVATION_TILES_DB_NAME
			+ "." + TestConstants.GEO_PACKAGE_EXTENSION;

	/**
	 * Import elevation tiles tiff database file name, located in the test
	 * assets
	 */
	public static final String IMPORT_ELEVATION_TILES_TIFF_DB_FILE_NAME = IMPORT_ELEVATION_TILES_TIFF_DB_NAME
			+ "." + TestConstants.GEO_PACKAGE_EXTENSION;

	/**
	 * Tile file name extension
	 */
	public static final String TILE_FILE_NAME_EXTENSION = "png";

	/**
	 * Tile file name
	 */
	public static final String TILE_FILE_NAME = "tile."
			+ TILE_FILE_NAME_EXTENSION;

	/**
	 * Tiles database table name
	 */
	public static final String TILES_DB_TABLE_NAME = "tiles";

	/**
	 * Tiles 2 database table name
	 */
	public static final String TILES2_DB_TABLE_NAME = "imagery";

	/**
	 * Elevation Tiles table name
	 */
	public static final String CREATE_ELEVATION_TILES_DB_TABLE_NAME = "elevations";

	/**
	 * Tiles 2 database Web Mercator test image
	 */
	public static final String TILES2_WEB_MERCATOR_TEST_IMAGE = "webMercator.png";

	/**
	 * Tiles 2 database WGS84 test image
	 */
	public static final String TILES2_WGS84_TEST_IMAGE = "wgs84.png";

}
