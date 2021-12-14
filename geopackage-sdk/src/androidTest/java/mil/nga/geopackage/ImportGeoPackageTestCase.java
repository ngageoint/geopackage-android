package mil.nga.geopackage;

import org.junit.After;

/**
 * Abstract Test Case for Imported GeoPackages
 * 
 * @author osbornb
 */
public abstract class ImportGeoPackageTestCase extends GeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ImportGeoPackageTestCase() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected GeoPackage getGeoPackage() throws Exception {
		return TestSetupTeardown.setUpImport(activity, testContext);
	}

	@After
	public void tearDown() throws Exception {

		// Tear down the import database
		TestSetupTeardown.tearDownImport(activity, geoPackage);

	}

}
