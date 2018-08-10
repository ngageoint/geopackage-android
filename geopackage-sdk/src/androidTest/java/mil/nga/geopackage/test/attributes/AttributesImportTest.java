package mil.nga.geopackage.test.attributes;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Attributes from an imported database
 *
 * @author osbornb
 */
public class AttributesImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public AttributesImportTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    @Test
    public void testRead() throws SQLException {

        AttributesUtils.testRead(geoPackage);

    }

    /**
     * Test updating
     *
     * @throws SQLException
     */
    @Test
    public void testUpdate() throws SQLException {

        AttributesUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    @Test
    public void testCreate() throws SQLException {

        AttributesUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDelete() throws SQLException {

        AttributesUtils.testDelete(geoPackage);

    }

}
