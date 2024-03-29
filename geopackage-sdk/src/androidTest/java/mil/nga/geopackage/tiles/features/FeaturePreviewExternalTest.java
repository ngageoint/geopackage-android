package mil.nga.geopackage.tiles.features;

import org.junit.Test;

import java.io.IOException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Feature Preview from an imported database
 *
 * @author osbornb
 */
public class FeaturePreviewExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeaturePreviewExternalTest() {

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
