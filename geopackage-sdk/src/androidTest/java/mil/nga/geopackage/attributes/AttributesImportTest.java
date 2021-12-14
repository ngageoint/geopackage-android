package mil.nga.geopackage.attributes;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ImportGeoPackageTestCase;

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
     * @throws SQLException upon error
     */
    @Test
    public void testRead() throws SQLException {

        AttributesUtils.testRead(geoPackage);

    }

    /**
     * Test updating
     *
     * @throws SQLException upon error
     */
    @Test
    public void testUpdate() throws SQLException {

        AttributesUtils.testUpdate(geoPackage);

    }

    /**
     * Test updating with added columns
     *
     * @throws SQLException upon error
     */
    @Test
    public void testUpdateAddColumns() throws SQLException {

        AttributesUtils.testUpdateAddColumns(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException upon error
     */
    @Test
    public void testCreate() throws SQLException {

        AttributesUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException upon error
     */
    @Test
    public void testDelete() throws SQLException {

        AttributesUtils.testDelete(geoPackage);

    }

}
