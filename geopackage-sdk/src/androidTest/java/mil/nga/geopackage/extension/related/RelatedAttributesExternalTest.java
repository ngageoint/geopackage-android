package mil.nga.geopackage.extension.related;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Related Attributes Tables from an imported database
 *
 * @author osbornb
 */
public class RelatedAttributesExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public RelatedAttributesExternalTest() {

    }

    /**
     * Test related attributes tables
     *
     * @throws SQLException
     */
    @Test
    public void testAttributes() throws Exception {

        RelatedAttributesUtils.testAttributes(geoPackage);

    }

}
