package mil.nga.geopackage.extension.related.media;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Related Media Tables from an imported database
 *
 * @author osbornb
 */
public class RelatedMediaExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public RelatedMediaExternalTest() {

    }

    /**
     * Test related media tables
     *
     * @throws SQLException
     */
    @Test
    public void testMedia() throws Exception {

        RelatedMediaUtils.testMedia(activity, testContext, geoPackage);

    }

}
