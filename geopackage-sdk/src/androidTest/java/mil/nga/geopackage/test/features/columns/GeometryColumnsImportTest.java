package mil.nga.geopackage.test.features.columns;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import static org.junit.Assert.fail;

/**
 * Test Geometry Columns from an imported database
 *
 * @author osbornb
 */
public class GeometryColumnsImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public GeometryColumnsImportTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    @Test
    public void testRead() throws SQLException {

        GeometryColumnsUtils.testRead(geoPackage, null);

    }

    /**
     * Test reading using the SQL/MM view
     */
    @Test
    public void testSqlMmRead() {

        try {
            geoPackage.getGeometryColumnsSqlMmDao();
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
            geoPackage.getGeometryColumnsSfSqlDao();
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

        GeometryColumnsUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    @Test
    public void testCreate() throws SQLException {

        GeometryColumnsUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDelete() throws SQLException {

        GeometryColumnsUtils.testDelete(geoPackage);

    }

}
