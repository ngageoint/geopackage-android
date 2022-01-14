package mil.nga.geopackage.extension.nga.index;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.CreateGeoPackageTestCase;

/**
 * Test Feature Table Index from a created database
 *
 * @author osbornb
 */
public class FeatureTableIndexCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTableIndexCreateTest() {

    }

    /**
     * Test index
     *
     * @throws Exception  upon error
     */
    @Test
    public void testIndex() throws Exception {

        FeatureTableIndexUtils.testIndex(geoPackage);

    }

    /**
     * Test delete all table indices
     *
     * @throws SQLException upon error
     */
    @Test
    public void testDeleteAll() throws SQLException {

        FeatureTableIndexUtils.testDeleteAll(geoPackage);

    }

    @Override
    public boolean allowEmptyFeatures() {
        return false;
    }

}
