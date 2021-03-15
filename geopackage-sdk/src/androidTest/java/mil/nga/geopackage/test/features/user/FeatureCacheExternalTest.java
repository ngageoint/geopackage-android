package mil.nga.geopackage.test.features.user;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ExternalGeoPackageTestCase;

/**
 * Test Feature Row Cache from an imported database
 *
 * @author osbornb
 */
public class FeatureCacheExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureCacheExternalTest() {

    }

    /**
     * Test cache
     *
     * @throws SQLException
     */
    @Test
    public void testCache() throws SQLException {

        FeatureCacheUtils.testCache(activity, geoPackage);

    }

}
