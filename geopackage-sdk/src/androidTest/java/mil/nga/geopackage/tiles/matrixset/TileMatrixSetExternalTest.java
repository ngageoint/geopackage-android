package mil.nga.geopackage.tiles.matrixset;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Tile Matrix Set from an imported database
 *
 * @author osbornb
 */
public class TileMatrixSetExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public TileMatrixSetExternalTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    @Test
    public void testRead() throws SQLException {

        TileMatrixSetUtils.testRead(geoPackage, null);

    }

    /**
     * Test updating
     *
     * @throws SQLException
     */
    @Test
    public void testUpdate() throws SQLException {

        TileMatrixSetUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    @Test
    public void testCreate() throws SQLException {

        TileMatrixSetUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDelete() throws SQLException {

        TileMatrixSetUtils.testDelete(geoPackage);

    }

}
