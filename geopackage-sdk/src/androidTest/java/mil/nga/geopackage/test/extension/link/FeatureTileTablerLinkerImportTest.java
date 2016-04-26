package mil.nga.geopackage.test.extension.link;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Feature Table Index from an imported database
 *
 * @author osbornb
 */
public class FeatureTileTablerLinkerImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTileTablerLinkerImportTest() {

    }

    /**
     * Test index
     *
     * @throws SQLException
     */
    public void testLink() throws SQLException {

        FeatureTileTablerLinkerUtils.testLink(geoPackage);

    }

}
