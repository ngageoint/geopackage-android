package mil.nga.geopackage.test.extension.nga.link;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ExternalGeoPackageTestCase;

/**
 * Test Feature Table Index from an imported database
 *
 * @author osbornb
 */
public class FeatureTileTableLinkerExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTileTableLinkerExternalTest() {

    }

    /**
     * Test index
     *
     * @throws SQLException
     */
    @Test
    public void testLink() throws SQLException {

        FeatureTileTableLinkerUtils.testLink(geoPackage);

    }

}
