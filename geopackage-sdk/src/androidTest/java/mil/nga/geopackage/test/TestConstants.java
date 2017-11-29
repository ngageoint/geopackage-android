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
	 * Create coverage data database name
	 */
	public static final String CREATE_COVERAGE_DATA_DB_NAME = "coverage_data";

	/**
	 * Create coverage data database file name
	 */
	public static final String CREATE_COVERAGE_DATA_DB_FILE_NAME = CREATE_COVERAGE_DATA_DB_NAME
			+ "." + TestConstants.GEO_PACKAGE_EXTENSION;

	/**
	 * Import coverage data database name
	 */
	public static final String IMPORT_COVERAGE_DATA_DB_NAME = "coverage_data";

	/**
	 * Import coverage data tiff database name
	 */
	public static final String IMPORT_COVERAGE_DATA_TIFF_DB_NAME = "coverage_data_tiff";

	/**
	 * Import coverage data database file name, located in the test assets
	 */
	public static final String IMPORT_COVERAGE_DATA_DB_FILE_NAME = IMPORT_COVERAGE_DATA_DB_NAME
			+ "." + TestConstants.GEO_PACKAGE_EXTENSION;

	/**
	 * Import coverage data tiff database file name, located in the test
	 * assets
	 */
	public static final String IMPORT_COVERAGE_DATA_TIFF_DB_FILE_NAME = IMPORT_COVERAGE_DATA_TIFF_DB_NAME
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
	 * Coverage Data table name
	 */
	public static final String CREATE_COVERAGE_DATA_DB_TABLE_NAME = "coverages";

	/**
	 * Tiles 2 database Web Mercator test image
	 */
	public static final String TILES2_WEB_MERCATOR_TEST_IMAGE = "webMercator.png";

	/**
	 * Tiles 2 database WGS84 test image
	 */
	public static final String TILES2_WGS84_TEST_IMAGE = "wgs84.png";

}
