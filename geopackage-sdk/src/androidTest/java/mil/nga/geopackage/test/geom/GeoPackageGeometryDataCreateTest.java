package mil.nga.geopackage.test.geom;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test GeoPackage Geometry Data from a created database
 * 
 * @author osbornb
 */
public class GeoPackageGeometryDataCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public GeoPackageGeometryDataCreateTest() {

	}

	/**
	 * Test reading and writing (and comparing) geometry bytes
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void testReadWriteBytes() throws SQLException, IOException {

		GeoPackageGeometryDataUtils.testReadWriteBytes(geoPackage);

	}

	/**
	 * Test geometry projection transform
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	public void testGeometryProjectionTransform() throws SQLException,
			IOException {

		GeoPackageGeometryDataUtils.testGeometryProjectionTransform(geoPackage);

	}

}
