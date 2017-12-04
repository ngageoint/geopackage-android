package mil.nga.geopackage.test.extension.coverage;

import mil.nga.geopackage.extension.coverage.CoverageDataAlgorithm;
import mil.nga.geopackage.test.CreateCoverageDataGeoPackageTestCase;

/**
 * Tiled Gridded Coverage Data Extension PNG Tests from a created GeoPackage
 * with no null extension values
 *
 * @author osbornb
 */
public class CoverageDataPngNoNullsCreateTest extends
        CreateCoverageDataGeoPackageTestCase {

    /**
     * Constructor
     */
    public CoverageDataPngNoNullsCreateTest() {
        super(false);
    }

    /**
     * Test the coverage data extension with a newly created GeoPackage using the
     * Nearest Neighbor Algorithm
     */
    public void testNearestNeighbor() throws Exception {

        CoverageDataPngTestUtils.testCoverageData(geoPackage, coverageDataValues,
                CoverageDataAlgorithm.NEAREST_NEIGHBOR, allowNulls);

    }

    /**
     * Test the coverage data extension with a newly created GeoPackage using the
     * Bilinear Algorithm
     */
    public void testBilinear() throws Exception {

        CoverageDataPngTestUtils.testCoverageData(geoPackage, coverageDataValues,
                CoverageDataAlgorithm.BILINEAR, allowNulls);

    }

    /**
     * Test the coverage data extension with a newly created GeoPackage using the
     * Bicubic Algorithm
     */
    public void testBicubic() throws Exception {

        CoverageDataPngTestUtils.testCoverageData(geoPackage, coverageDataValues,
                CoverageDataAlgorithm.BICUBIC, allowNulls);

    }

    /**
     * Test a random bounding box using the Nearest Neighbor Algorithm
     */
    public void testRandomBoundingBoxNearestNeighbor() throws Exception {

        CoverageDataTestUtils.testRandomBoundingBox(geoPackage,
                CoverageDataAlgorithm.NEAREST_NEIGHBOR,
                allowNulls);

    }

    /**
     * Test a random bounding box using the Bilinear Algorithm
     */
    public void testRandomBoundingBoxBilinear() throws Exception {

        CoverageDataTestUtils.testRandomBoundingBox(geoPackage,
                CoverageDataAlgorithm.BILINEAR,
                allowNulls);

    }

    /**
     * Test a random bounding box using the Bicubic Algorithm
     */
    public void testRandomBoundingBoxBicubic() throws Exception {

        CoverageDataTestUtils.testRandomBoundingBox(geoPackage,
                CoverageDataAlgorithm.BICUBIC,
                allowNulls);

    }

    /**
     * Test the pixel encoding
     */
    public void testPixelEncoding() throws Exception {

        CoverageDataTestUtils.testPixelEncoding(geoPackage, allowNulls);

    }

}
