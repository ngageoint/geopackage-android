package mil.nga.geopackage.test.extension.nga.link;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test Feature Tile Table Linker from a created database
 *
 * @author osbornb
 */
public class FeatureTileTablerLinkerCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTileTablerLinkerCreateTest() {

    }

    /**
     * Test link
     *
     * @throws SQLException
     */
    @Test
    public void testLink() throws SQLException {

        FeatureTileTablerLinkerUtils.testLink(geoPackage);

    }

}
