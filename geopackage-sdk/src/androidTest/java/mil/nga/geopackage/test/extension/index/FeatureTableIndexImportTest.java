package mil.nga.geopackage.test.extension.index;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Feature Table Index from an imported database
 *
 * @author osbornb
 */
public class FeatureTableIndexImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTableIndexImportTest() {

    }

    /**
     * Test index
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    public void testIndex() throws SQLException, IOException {

        FeatureTableIndexUtils.testIndex(geoPackage);

    }

    /**
     * Test delete all table indices
     *
     * @throws SQLException upon error
     */
    public void testDeleteAll() throws SQLException {

        FeatureTableIndexUtils.testDeleteAll(geoPackage);

    }

}
