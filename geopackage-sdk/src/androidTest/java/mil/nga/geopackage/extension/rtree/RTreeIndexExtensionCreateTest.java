package mil.nga.geopackage.extension.rtree;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.CreateGeoPackageTestCase;

/**
 * Test RTree Extension from a created database
 *
 * @author osbornb
 */
public class RTreeIndexExtensionCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public RTreeIndexExtensionCreateTest() {

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

    @Override
    public boolean allowEmptyFeatures() {
        return false;
    }

}
