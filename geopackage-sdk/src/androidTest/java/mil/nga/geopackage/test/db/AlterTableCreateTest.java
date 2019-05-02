package mil.nga.geopackage.test.db;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Alter Table Create Test
 *
 * @author osbornb
 */
public class AlterTableCreateTest extends CreateGeoPackageTestCase {

    /**
     * Test column alters
     *
     * @throws SQLException upon error
     */
    @Test
    public void testColumns() throws SQLException {
        AlterTableUtils.testColumns(activity, geoPackage);
    }

}
