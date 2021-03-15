package mil.nga.geopackage.test.geom;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.test.ExternalGeoPackageTestCase;

/**
 * Test GeoPackage Geometry Data from an imported database
 *
 * @author osbornb
 */
public class GeoPackageGeometryDataExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public GeoPackageGeometryDataExternalTest() {

    }

    /**
     * Test reading and writing (and comparing) geometry bytes
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testReadWriteBytes() throws SQLException, IOException {

        GeoPackageGeometryDataUtils.testReadWriteBytes(geoPackage);

    }

    /**
     * Test geometry projection transform
     *
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void testGeometryProjectionTransform() throws SQLException,
            IOException {

        GeoPackageGeometryDataUtils.testGeometryProjectionTransform(geoPackage);

    }

}
