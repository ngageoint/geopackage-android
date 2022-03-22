package mil.nga.geopackage.geom;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.ImportGeoPackageTestCase;

/**
 * Test GeoPackage Geometry Data from an imported database
 *
 * @author osbornb
 */
public class GeoPackageGeometryDataImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public GeoPackageGeometryDataImportTest() {

    }

    /**
     * Test reading and writing (and comparing) geometry bytes
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testReadWriteBytes() throws SQLException, IOException {

        GeoPackageGeometryDataUtils.testReadWriteBytes(geoPackage);

    }

    /**
     * Test geometry projection transform
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testGeometryProjectionTransform() throws SQLException,
            IOException {

        GeoPackageGeometryDataUtils.testGeometryProjectionTransform(geoPackage);

    }

    /**
     * Test insert geometry bytes
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testInsertGeometryBytes() throws SQLException, IOException {

        GeoPackageGeometryDataUtils.testInsertGeometryBytes(geoPackage);

    }

    /**
     * Test insert header bytes
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testInsertHeaderBytes() throws SQLException, IOException {

        GeoPackageGeometryDataUtils.testInsertHeaderBytes(geoPackage);

    }

    /**
     * Test insert header and geometry bytes
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testInsertHeaderAndGeometryBytes()
            throws SQLException, IOException {

        GeoPackageGeometryDataUtils
                .testInsertHeaderAndGeometryBytes(geoPackage);

    }

    /**
     * Test insert bytes
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testInsertBytes() throws SQLException, IOException {

        GeoPackageGeometryDataUtils.testInsertBytes(geoPackage);

    }

}
