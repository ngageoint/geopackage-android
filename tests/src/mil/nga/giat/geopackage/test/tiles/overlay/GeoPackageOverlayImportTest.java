package mil.nga.giat.geopackage.test.tiles.overlay;

import java.sql.SQLException;

import mil.nga.giat.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test GeoPackage Overlay from an imported database
 * 
 * @author osbornb
 */
public class GeoPackageOverlayImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public GeoPackageOverlayImportTest() {

	}

	/**
	 * Test overlay
	 * 
	 * @throws SQLException
	 */
	public void testOverlay() throws SQLException {

		GeoPackageOverlayUtils.testOverlay(geoPackage);

	}

}
