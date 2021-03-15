package mil.nga.geopackage.test.features.index;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ExternalGeoPackageTestCase;

/**
 * Test Feature Index Manager from an imported database
 *
 * @author osbornb
 */
public class FeatureIndexManagerExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureIndexManagerExternalTest() {

    }

    /**
     * Test index
     *
     * @throws SQLException
     */
    @Test
    public void testIndex() throws SQLException {

        FeatureIndexManagerUtils.testIndex(activity, geoPackage);

    }

    /**
     * Test large index
     *
     * @throws SQLException upon error
     */
    @Test
    public void testLargeIndex() throws SQLException {

        FeatureIndexManagerUtils.testLargeIndex(activity, geoPackage, 10000, false);

    }

    /**
     * Test timed index
     *
     * @throws SQLException upon error
     */
    @Test
    public void testTimedIndex() throws SQLException {

        FeatureIndexManagerUtils.testTimedIndex(activity, geoPackage, false, false);

    }

}
