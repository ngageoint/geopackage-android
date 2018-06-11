package mil.nga.geopackage.test.extension.related.simple;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Test Related Simple Attributes Tables from an imported database
 *
 * @author osbornb
 */
public class RelatedSimpleAttributesImportTest extends ImportGeoPackageTestCase {

    /**
     * Constructor
     */
    public RelatedSimpleAttributesImportTest() {

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
