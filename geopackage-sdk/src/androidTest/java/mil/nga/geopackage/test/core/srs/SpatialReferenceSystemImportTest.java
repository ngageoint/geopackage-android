package mil.nga.geopackage.test.core.srs;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

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
     *
     * @throws SQLException
     */
    @Test
    public void testSqlMmRead() throws SQLException {

        SpatialReferenceSystemUtils.testSqlMmRead(geoPackage,null);

    }

    /**
     * Test reading using the SF/SQL view
     *
     * @throws SQLException
     */
    @Test
    public void testSfSqlRead() throws SQLException {

        SpatialReferenceSystemUtils.testSfSqlRead(geoPackage,null);

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
