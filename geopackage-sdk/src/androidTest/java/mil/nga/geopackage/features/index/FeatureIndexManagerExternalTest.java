package mil.nga.geopackage.features.index;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

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
     * Test index chunk
     *
     * @throws SQLException upon error
     */
    @Test
    public void testIndexChunk() throws SQLException {

        FeatureIndexManagerUtils.testIndexChunk(activity, geoPackage);

    }

    /**
     * Test index pagination
     *
     * @throws SQLException upon error
     */
    @Test
    public void testIndexPagination() throws SQLException {

        FeatureIndexManagerUtils.testIndexPagination(activity, geoPackage);

    }

    /**
     * Test large index
     *
     * @throws SQLException upon error
     */
    @Test
    public void testLargeIndex() throws SQLException {

        FeatureIndexManagerUtils.testLargeIndex(activity, geoPackage, 1000, false);

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
