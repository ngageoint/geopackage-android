package mil.nga.geopackage.test.features.user;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Feature Row Cache from an imported database
 *
 * @author osbornb
 */
public class FeatureCacheImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureCacheImportTest() {

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
