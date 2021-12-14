package mil.nga.geopackage.tiles.matrix;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Tile Matrix from an imported database
 *
 * @author osbornb
 */
public class TileMatrixExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public TileMatrixExternalTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    @Test
    public void testRead() throws SQLException {

        TileMatrixUtils.testRead(geoPackage, null);

    }

    /**
     * Test updating
     *
     * @throws SQLException
     */
    @Test
    public void testUpdate() throws SQLException {

        TileMatrixUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    @Test
    public void testCreate() throws SQLException {

        TileMatrixUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDelete() throws SQLException {

        TileMatrixUtils.testDelete(geoPackage);

    }

}
