package mil.nga.geopackage.test.db;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

/**
 * Alter Table Import Test
 *
 * @author osbornb
 */
public class AlterTableImportTest extends ImportGeoPackageTestCase {

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
