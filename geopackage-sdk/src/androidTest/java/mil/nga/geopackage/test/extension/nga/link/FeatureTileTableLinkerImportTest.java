package mil.nga.geopackage.test.extension.nga.link;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Feature Table Index from an imported database
 *
 * @author osbornb
 */
public class FeatureTileTableLinkerImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTileTableLinkerImportTest() {

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
