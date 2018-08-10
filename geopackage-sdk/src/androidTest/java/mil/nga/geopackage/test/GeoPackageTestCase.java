package mil.nga.geopackage.test;

import org.junit.Before;

import mil.nga.geopackage.GeoPackage;

/**
 * Abstract Test Case on a single GeoPackage
 * 
 * @author osbornb
 */
public abstract class GeoPackageTestCase extends BaseTestCase {

	/**
	 * GeoPackage
	 */
	protected GeoPackage geoPackage = null;

	/**
	 * Constructor
	 */
	public GeoPackageTestCase() {

	}

	/**
	 * Get the geo package to test
	 * 
	 * @return
	 */
	protected abstract GeoPackage getGeoPackage() throws Exception;

	@Before
	public void geoPackageSetUp() throws Exception {
		// Get the geo package
		geoPackage = getGeoPackage();
	}

}
