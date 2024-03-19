package mil.nga.geopackage.extension.nga.index;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Feature Table Index from an imported database
 *
 * @author osbornb
 */
public class FeatureTableIndexExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTableIndexExternalTest() {

    }

    /**
     * Test index
     *
     * @throws Exception upon error
     */
    @Test
    public void testIndex() throws Exception {

        FeatureTableIndexUtils.testIndex(geoPackage, false);

    }

    /**
     * Test index
     *
     * @throws Exception
     *             upon error
     */
    @Test
    public void testIndexGeodesic() throws Exception {

        FeatureTableIndexUtils.testIndex(geoPackage, true);

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

}
