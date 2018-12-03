package mil.nga.geopackage.test.extension.related;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Related Tiles Tables from an imported database
 * 
 * @author osbornb
 */
public class RelatedTilesImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedTilesImportTest() {

	}

	/**
	 * Test related tiles tables
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testTiles() throws Exception {

		RelatedTilesUtils.testTiles(geoPackage);

	}

}
