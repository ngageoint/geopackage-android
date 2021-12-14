package mil.nga.geopackage.schema.constraints;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Data Column Constraints from an imported database
 *
 * @author osbornb
 */
public class DataColumnConstraintsExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public DataColumnConstraintsExternalTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    @Test
    public void testRead() throws SQLException {

        DataColumnConstraintsUtils.testRead(geoPackage, null);

    }

    /**
     * Test updating
     *
     * @throws SQLException
     */
    @Test
    public void testUpdate() throws SQLException {

        DataColumnConstraintsUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    @Test
    public void testCreate() throws SQLException {

        DataColumnConstraintsUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDelete() throws SQLException {

        DataColumnConstraintsUtils.testDelete(geoPackage);

    }

}
