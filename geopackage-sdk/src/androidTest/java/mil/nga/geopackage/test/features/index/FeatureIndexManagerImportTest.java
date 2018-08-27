package mil.nga.geopackage.test.features.index;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Feature Index Manager from an imported database
 *
 * @author osbornb
 */
public class FeatureIndexManagerImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureIndexManagerImportTest() {

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

        FeatureIndexManagerUtils.testLargeIndex(activity, geoPackage, 10000);

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
