package mil.nga.geopackage.test.extension.nga.scale;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ExternalGeoPackageTestCase;

/**
 * Test Tile Table Scaling from an imported database
 *
 * @author osbornb
 */
public class TileTableScalingExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public TileTableScalingExternalTest() {

    }

    /**
     * Test index
     *
     * @throws SQLException
     */
    @Test
    public void testScaling() throws SQLException {

        TileTableScalingUtils.testScaling(geoPackage);

    }

}
