package mil.nga.geopackage.schema.columns;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Data Columns from an imported database
 *
 * @author osbornb
 */
public class DataColumnsExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public DataColumnsExternalTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    @Test
    public void testRead() throws SQLException {

        DataColumnsUtils.testRead(geoPackage, null);

    }

    /**
     * Test updating
     *
     * @throws SQLException
     */
    @Test
    public void testUpdate() throws SQLException {

        DataColumnsUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    @Test
    public void testCreate() throws SQLException {

        DataColumnsUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDelete() throws SQLException {

        DataColumnsUtils.testDelete(geoPackage);

    }

}
