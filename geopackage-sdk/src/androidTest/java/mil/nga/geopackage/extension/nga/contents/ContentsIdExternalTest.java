package mil.nga.geopackage.extension.nga.contents;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Test Contents Id from an imported database
 *
 * @author osbornb
 */
public class ContentsIdExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public ContentsIdExternalTest() {

    }

    /**
     * Test contents id
     *
     * @throws SQLException
     */
    @Test
    public void testContentsId() throws SQLException {

        ContentsIdUtils.testContentsId(geoPackage);

    }

}
