package mil.nga.geopackage.test.extension.rtree;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ExternalGeoPackageTestCase;

/**
 * Test RTree Extension from an imported database
 *
 * @author osbornb
 */
public class RTreeIndexExtensionExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public RTreeIndexExtensionExternalTest() {

    }

    /**
     * Test RTree
     *
     * @throws SQLException upon error
     */
    @Test
    public void testRTree() throws SQLException {

        RTreeIndexExtensionUtils.testRTree(geoPackage);

    }

}
