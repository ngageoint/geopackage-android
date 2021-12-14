package mil.nga.geopackage.extension.related;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Related Tiles Tables from an imported database
 *
 * @author osbornb
 */
public class RelatedTilesExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public RelatedTilesExternalTest() {

    }

    /**
     * Test related tiles tables
     *
     * @throws SQLException
     */
    @Test
    public void testTiles() throws Exception {

        RelatedTilesUtils.testTiles(geoPackage);

    }

}
