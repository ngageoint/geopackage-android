package mil.nga.geopackage.test.tiles.features;

import org.junit.Test;

import java.io.IOException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test Feature Preview from a created database
 *
 * @author osbornb
 */
public class FeaturePreviewCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeaturePreviewCreateTest() {

    }

    /**
     * Test draw
     *
     * @throws IOException upon error
     */
    @Test
    public void testDraw() throws IOException {

        FeaturePreviewUtils.testDraw(activity, geoPackage);

    }

}
