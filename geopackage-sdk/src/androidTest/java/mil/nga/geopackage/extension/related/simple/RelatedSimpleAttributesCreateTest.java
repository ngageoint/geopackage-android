package mil.nga.geopackage.extension.related.simple;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.CreateGeoPackageTestCase;

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
    @Test
    public void testSimpleAttributes() throws Exception {

        RelatedSimpleAttributesUtils.testSimpleAttributes(geoPackage);

    }

}
