package mil.nga.geopackage.test.extension.related;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Related Attributes Tables from a created database
 * 
 * @author osbornb
 */
public class RelatedAttributesCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedAttributesCreateTest() {

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
