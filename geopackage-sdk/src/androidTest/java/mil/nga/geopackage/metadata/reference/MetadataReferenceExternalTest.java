package mil.nga.geopackage.metadata.reference;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Metadata Reference from an imported database
 *
 * @author osbornb
 */
public class MetadataReferenceExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public MetadataReferenceExternalTest() {

    }

    /**
     * Test reading
     *
     * @throws SQLException
     */
    @Test
    public void testRead() throws SQLException {

        MetadataReferenceUtils.testRead(geoPackage, null);

    }

    /**
     * Test updating
     *
     * @throws SQLException
     */
    @Test
    public void testUpdate() throws SQLException {

        MetadataReferenceUtils.testUpdate(geoPackage);

    }

    /**
     * Test creating
     *
     * @throws SQLException
     */
    @Test
    public void testCreate() throws SQLException {

        MetadataReferenceUtils.testCreate(geoPackage);

    }

    /**
     * Test deleting
     *
     * @throws SQLException
     */
    @Test
    public void testDelete() throws SQLException {

        MetadataReferenceUtils.testDelete(geoPackage);

    }

}
