package mil.nga.geopackage.extension.nga.index;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Feature Table Index from an imported database
 *
 * @author osbornb
 */
public class FeatureTableIndexExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTableIndexExternalTest() {

    }

    /**
     * Test index
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testIndex() throws SQLException, IOException {

        FeatureTableIndexUtils.testIndex(geoPackage);

    }

    /**
     * Test delete all table indices
     *
     * @throws SQLException upon error
     */
    @Test
    public void testDeleteAll() throws SQLException {

        FeatureTableIndexUtils.testDeleteAll(geoPackage);

    }

}
