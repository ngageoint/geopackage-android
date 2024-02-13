package mil.nga.geopackage;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.File;
import java.sql.SQLException;

/**
 * Test GeoPackage from an external database
 *
 * @author osbornb
 */
public class GeoPackageExternalTest extends ExternalGeoPackageTestCase {

    /**
     * Constructor
     */
    public GeoPackageExternalTest() {

    }

    /**
     * Test create feature table with metadata
     *
     * @throws SQLException
     */
    @Test
    public void testCreateFeatureTableWithMetadata() throws SQLException {

        GeoPackageTestUtils.testCreateFeatureTableWithMetadata(geoPackage);

    }

    /**
     * Test create feature table with metadata and id column
     *
     * @throws SQLException
     */
    @Test
    public void testCreateFeatureTableWithMetadataIdColumn()
            throws SQLException {

        GeoPackageTestUtils
                .testCreateFeatureTableWithMetadataIdColumn(geoPackage);

    }

    /**
     * Test create feature table with metadata and additional columns
     *
     * @throws SQLException
     */
    @Test
    public void testCreateFeatureTableWithMetadataAdditionalColumns()
            throws SQLException {

        GeoPackageTestUtils
                .testCreateFeatureTableWithMetadataAdditionalColumns(geoPackage);

    }

    /**
     * Test create feature table with metadata, id column, and additional
     * columns
     *
     * @throws SQLException
     */
    @Test
    public void testCreateFeatureTableWithMetadataIdColumnAdditionalColumns()
            throws SQLException {

        GeoPackageTestUtils
                .testCreateFeatureTableWithMetadataIdColumnAdditionalColumns(geoPackage);

    }

    /**
     * Test delete tables
     *
     * @throws SQLException
     */
    @Test
    public void testDeleteTables() throws SQLException {

        GeoPackageTestUtils.testDeleteTables(geoPackage);

    }

    /**
     * Test bounds
     *
     * @throws SQLException upon error
     */
    @Test
    public void testBounds() throws SQLException {

        GeoPackageTestUtils.testBounds(activity, geoPackage);

    }

    /**
     * Test vacuum
     */
    @Test
    public void testVacuum() {

        long size = new File(geoPackage.getPath()).length();

        for (String table : geoPackage.getTables()) {

            geoPackage.deleteTable(table);

            geoPackage.vacuum();

            long newSize = new File(geoPackage.getPath()).length();
            TestCase.assertTrue(size > newSize);
            size = newSize;

        }

    }

    /**
     * Test table types
     *
     * @throws SQLException upon error
     */
    @Test
    public void testTableTypes() throws SQLException {

        GeoPackageTestUtils.testTableTypes(geoPackage);

    }

    /**
     * Test user daos
     *
     * @throws SQLException
     *             upon error
     */
    @Test
    public void testUserDao() throws SQLException {

        GeoPackageTestUtils.testUserDao(geoPackage);

    }

}
