package mil.nga.geopackage.extension.coverage;

import org.junit.Test;

import mil.nga.geopackage.CreateCoverageDataGeoPackageTestCase;

/**
 * Tiled Gridded Coverage Data Extension PNG Tests from a created GeoPackage
 *
 * @author osbornb
 */
public class CoverageDataPngCreateTest extends
        CreateCoverageDataGeoPackageTestCase {

    /**
     * Constructor
     */
    public CoverageDataPngCreateTest() {
        super(true);
    }

    /**
     * Test the coverage data extension with a newly created GeoPackage using the
     * Nearest Neighbor Algorithm
     */
    @Test
    public void testNearestNeighbor() throws Exception {

        CoverageDataPngTestUtils.testCoverageData(geoPackage, coverageDataValues,
                CoverageDataAlgorithm.NEAREST_NEIGHBOR, allowNulls);

    }

    /**
     * Test the coverage data extension with a newly created GeoPackage using the
     * Bilinear Algorithm
     */
    @Test
    public void testBilinear() throws Exception {

        CoverageDataPngTestUtils.testCoverageData(geoPackage, coverageDataValues,
                CoverageDataAlgorithm.BILINEAR, allowNulls);

    }

    /**
     * Test the coverage data extension with a newly created GeoPackage using the
     * Bicubic Algorithm
     */
    @Test
    public void testBicubic() throws Exception {

        CoverageDataPngTestUtils.testCoverageData(geoPackage, coverageDataValues,
                CoverageDataAlgorithm.BICUBIC, allowNulls);

    }

    /**
     * Test a random bounding box using the Nearest Neighbor Algorithm
     */
    @Test
    public void testRandomBoundingBoxNearestNeighbor() throws Exception {

        CoverageDataTestUtils.testRandomBoundingBox(geoPackage,
                CoverageDataAlgorithm.NEAREST_NEIGHBOR,
                allowNulls);

    }

    /**
     * Test a random bounding box using the Bilinear Algorithm
     */
    @Test
    public void testRandomBoundingBoxBilinear() throws Exception {

        CoverageDataTestUtils.testRandomBoundingBox(geoPackage,
                CoverageDataAlgorithm.BILINEAR,
                allowNulls);

    }

    /**
     * Test a random bounding box using the Bicubic Algorithm
     */
    @Test
    public void testRandomBoundingBoxBicubic() throws Exception {

        CoverageDataTestUtils.testRandomBoundingBox(geoPackage,
                CoverageDataAlgorithm.BICUBIC,
                allowNulls);

    }

    /**
     * Test the pixel encoding
     */
    @Test
    public void testPixelEncoding() throws Exception {

        CoverageDataTestUtils.testPixelEncoding(geoPackage, allowNulls);

    }

}
