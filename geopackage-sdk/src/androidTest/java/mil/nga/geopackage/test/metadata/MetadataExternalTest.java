package mil.nga.geopackage.test.metadata;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ExternalGeoPackageTestCase;

/**
 * Test Metadata from an imported database
 *
 * @author osbornb
 */
public class MetadataExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public MetadataExternalTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    @Test
    public void testRead() throws SQLException {

        MetadataUtils.testRead(geoPackage, null);

    }

    /**
     * Test updating
     *
     * @throws SQLException
     */
    @Test
    public void testUpdate() throws SQLException {

        MetadataUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    @Test
    public void testCreate() throws SQLException {

        MetadataUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDelete() throws SQLException {

        MetadataUtils.testDelete(geoPackage);

    }

    /**
     * Test cascade deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDeleteCascade() throws SQLException {

        MetadataUtils.testDeleteCascade(geoPackage);

    }

}
