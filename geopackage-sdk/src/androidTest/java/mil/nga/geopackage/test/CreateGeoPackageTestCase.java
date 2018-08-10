package mil.nga.geopackage.test;

import org.junit.After;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.GeoPackage;

/**
 * Abstract Test Case for Created GeoPackages
 * 
 * @author osbornb
 */
public abstract class CreateGeoPackageTestCase extends GeoPackageTestCase {

	/**
	 * Constructor
	 */
	public CreateGeoPackageTestCase() {

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	@Override
	protected GeoPackage getGeoPackage() throws Exception {
		return TestSetupTeardown.setUpCreate(activity, testContext, true, true);
	}

	@After
	public void tearDown() throws Exception {

		// Tear down the create database
		TestSetupTeardown.tearDownCreate(activity, geoPackage);

	}

	/**
	 * Return true to allow a chance that a feature will be created with an
	 * empty geometry
	 *
	 * @return
	 */
	public boolean allowEmptyFeatures() {
		return true;
	}

}
