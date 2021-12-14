package mil.nga.geopackage.extension.nga.contents;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.CreateGeoPackageTestCase;

/**
 * Test Contents Id from a created database
 *
 * @author osbornb
 */
public class ContentsIdCreateTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public ContentsIdCreateTest() {

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
