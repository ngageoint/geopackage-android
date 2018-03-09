package mil.nga.geopackage.test.extension.scale;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test Tile Table Scaling from a created database
 *
 * @author osbornb
 */
public class TileTableScalingCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public TileTableScalingCreateTest() {

    }

    /**
     * Test link
     *
     * @throws SQLException
     */
    public void testScaling() throws SQLException {

        TileTableScalingUtils.testScaling(geoPackage);

    }

}
