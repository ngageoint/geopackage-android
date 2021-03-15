package mil.nga.geopackage.test;

import org.junit.After;

import mil.nga.geopackage.GeoPackage;

/**
 * Abstract Test Case for External GeoPackages
 *
 * @author osbornb
 */
public abstract class ExternalGeoPackageTestCase extends GeoPackageTestCase {

    /**
     * Constructor
     */
    public ExternalGeoPackageTestCase() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GeoPackage getGeoPackage() throws Exception {
        return TestSetupTeardown.setUpExternal(activity, testContext);
    }

    @After
    public void tearDown() throws Exception {

        // Tear down the import database
        TestSetupTeardown.tearDownExternal(activity, geoPackage);

    }

}
