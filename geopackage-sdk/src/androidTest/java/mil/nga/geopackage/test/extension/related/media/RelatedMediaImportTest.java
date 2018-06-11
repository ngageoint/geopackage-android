package mil.nga.geopackage.test.extension.related.media;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Related Media Tables from an imported database
 *
 * @author osbornb
 */
public class RelatedMediaImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public RelatedMediaImportTest() {

    }

    /**
     * Test related media tables
     *
     * @throws SQLException
     */
    public void testMedia() throws Exception {

        RelatedMediaUtils.testMedia(activity, testContext, geoPackage);

    }

}
