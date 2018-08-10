package mil.nga.geopackage.test.core.srs;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import static org.junit.Assert.fail;

/**
 * Test Spatial Reference System from an imported database
 *
 * @author osbornb
 */
public class SpatialReferenceSystemImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public SpatialReferenceSystemImportTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    @Test
    public void testRead() throws SQLException {

        SpatialReferenceSystemUtils.testRead(geoPackage, null);

    }

    /**
     * Test reading using the SQL/MM view
     */
    @Test
    public void testSqlMmRead() {

        try {
            geoPackage.getSpatialReferenceSystemSqlMmDao();
            fail("No exception was thrown when the SQL/MM view was not expected to exist");
        } catch (GeoPackageException e) {
            // Expected
        }

    }

    /**
     * Test reading using the SF/SQL view
     */
    @Test
    public void testSfSqlRead() {

        try {
            geoPackage.getSpatialReferenceSystemSfSqlDao();
            fail("No exception was thrown when the SF/SQL view was not expected to exist");
        } catch (GeoPackageException e) {
            // Expected
        }

    }

    /**
     * Test updating
     *
     * @throws SQLException
     */
    @Test
    public void testUpdate() throws SQLException {

        SpatialReferenceSystemUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    @Test
    public void testCreate() throws SQLException {

        SpatialReferenceSystemUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDelete() throws SQLException {

        SpatialReferenceSystemUtils.testDelete(geoPackage);

    }

    /**
     * Test cascade deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDeleteCascade() throws SQLException {

        SpatialReferenceSystemUtils.testDeleteCascade(geoPackage);

    }

}
