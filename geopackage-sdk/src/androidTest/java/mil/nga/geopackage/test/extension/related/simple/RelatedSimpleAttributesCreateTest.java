package mil.nga.geopackage.test.extension.related.simple;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Test Related Simple Attributes Tables from a created database
 *
 * @author osbornb
 */
public class RelatedSimpleAttributesCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public RelatedSimpleAttributesCreateTest() {

    }

    /**
     * Test related simple attributes tables
     *
     * @throws SQLException
     */
    public void testSimpleAttributes() throws Exception {

        RelatedSimpleAttributesUtils.testSimpleAttributes(geoPackage);

    }

}
