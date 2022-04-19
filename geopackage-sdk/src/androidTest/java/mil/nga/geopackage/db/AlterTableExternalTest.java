package mil.nga.geopackage.db;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.ExternalGeoPackageTestCase;

/**
 * Alter Table Import Test
 *
 * @author osbornb
 */
public class AlterTableExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Test column alters
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testColumns() throws SQLException, IOException {
        AlterTableUtils.testColumns(activity, geoPackage);
    }

    /**
     * Test copy feature table
     *
     * @throws SQLException upon error
     * @throws IOException  upon error
     */
    @Test
    public void testCopyFeatureTable() throws SQLException, IOException {
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
