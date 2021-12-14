package mil.nga.geopackage.extension.related;

import java.sql.SQLException;

import mil.nga.geopackage.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Related Attributes Tables from an imported database
 * 
 * @author osbornb
 */
public class RelatedAttributesImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedAttributesImportTest() {

	}

	/**
	 * Test related attributes tables
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testAttributes() throws Exception {

		RelatedAttributesUtils.testAttributes(geoPackage);

	}

}
