package mil.nga.geopackage.test.features.index;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test Feature Index Manager from a created database
 *
 * @author osbornb
 */
public class FeatureIndexManagerCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureIndexManagerCreateTest() {

    }

    /**
     * Test index
     *
     * @throws SQLException
     */
    public void testIndex() throws SQLException {

        FeatureIndexManagerUtils.testIndex(activity, geoPackage);

    }

    @Override
    public boolean allowEmptyFeatures() {
        return false;
    }

}
