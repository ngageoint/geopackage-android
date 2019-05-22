package mil.nga.geopackage.test.db;

import org.junit.Test;

import java.io.IOException;
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

    /**
     * Test copy feature table
     *
     * @throws SQLException upon error
     */
    @Test
    public void testCopyFeatureTable() throws SQLException {
        AlterTableUtils.testCopyFeatureTable(activity, geoPackage);
    }

    /**
     * Test copy tile table
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testCopyTileTable() throws SQLException, IOException {
        AlterTableUtils.testCopyTileTable(activity, geoPackage);
    }

    /**
     * Test copy attributes table
     *
     * @throws SQLException upon error
     */
    @Test
    public void testCopyAttributesTable() throws SQLException {
        AlterTableUtils.testCopyAttributesTable(activity, geoPackage);
    }

    /**
     * Test copy user table
     *
     * @throws SQLException upon error
     */
    @Test
    public void testCopyUserTable() throws SQLException {
        AlterTableUtils.testCopyUserTable(activity, geoPackage);
    }

}
