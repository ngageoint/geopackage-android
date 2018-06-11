package mil.nga.geopackage.test.extension.related.media;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test Related Media Tables from a created database
 *
 * @author osbornb
 */
public class RelatedMediaCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public RelatedMediaCreateTest() {

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
