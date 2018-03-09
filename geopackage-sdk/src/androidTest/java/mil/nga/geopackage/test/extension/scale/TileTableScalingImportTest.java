package mil.nga.geopackage.test.extension.scale;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Tile Table Scaling from an imported database
 *
 * @author osbornb
 */
public class TileTableScalingImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public TileTableScalingImportTest() {

    }

    /**
     * Test index
     *
     * @throws SQLException
     */
    public void testScaling() throws SQLException {

        TileTableScalingUtils.testScaling(geoPackage);

    }

}
