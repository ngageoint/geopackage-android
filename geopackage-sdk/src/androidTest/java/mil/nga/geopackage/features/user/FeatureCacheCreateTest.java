package mil.nga.geopackage.features.user;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.CreateGeoPackageTestCase;

/**
 * Test Feature Row Cache from a created database
 *
 * @author osbornb
 */
public class FeatureCacheCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureCacheCreateTest() {

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
