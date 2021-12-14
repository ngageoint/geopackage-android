package mil.nga.geopackage.extension.related;

import java.sql.SQLException;

import mil.nga.geopackage.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Related Tiles Tables from a created database
 * 
 * @author osbornb
 */
public class RelatedTilesCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedTilesCreateTest() {

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
