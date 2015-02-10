package mil.nga.giat.geopackage.test.geom.conversion;

import java.sql.SQLException;

import mil.nga.giat.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test Google Map Shape Converter from a created database
 * 
 * @author osbornb
 */
public class GoogleMapShapeConverterCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public GoogleMapShapeConverterCreateTest() {

	}

	/**
	 * Test shapes
	 * 
	 * @throws SQLException
	 */
	public void testShapes() throws SQLException {

		GoogleMapShapeConverterUtils.testShapes(geoPackage);

	}

}
