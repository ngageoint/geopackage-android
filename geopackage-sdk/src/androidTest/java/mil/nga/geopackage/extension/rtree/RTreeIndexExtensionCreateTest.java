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

        RTreeIndexExtensionUtils.testRTree(geoPackage, false);

    }

    /**
     * Test RTree with geodesic
     *
     * @throws SQLException
     *             upon error
     */
    @Test
    public void testRTreeGeodesic() throws SQLException {

        RTreeIndexExtensionUtils.testRTree(geoPackage, true);

    }

    @Override
    public boolean allowEmptyFeatures() {
        return false;
    }

}
