package mil.nga.geopackage.test;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.GeoPackage;

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
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	@Override
	protected GeoPackage getGeoPackage() throws Exception {
		return TestSetupTeardown.setUpImport(activity, testContext);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void tearDown() throws Exception {

		// Tear down the import database
		TestSetupTeardown.tearDownImport(activity, geoPackage);

		super.tearDown();
	}

}
