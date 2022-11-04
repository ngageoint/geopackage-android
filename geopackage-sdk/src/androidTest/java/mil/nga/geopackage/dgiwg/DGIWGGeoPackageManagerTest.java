package mil.nga.geopackage.dgiwg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.documentfile.provider.DocumentFile;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import mil.nga.geopackage.BaseTestCase;
import mil.nga.geopackage.TestConstants;

/**
 * Test DGIWG GeoPackage Manager methods
 *
 * @author osbornb
 */
public class DGIWGGeoPackageManagerTest extends BaseTestCase {

    /**
     * Test file name
     */
    public static final String FILE_NAME = "AGC_BUCK_Ft-Bliss_14-20_v1-0_29AUG2016";

    /**
     * Non informative Test file name
     */
    public static final String FILE_NAME_NON_INFORMATIVE = "NonInformativeName";

    /**
     * Non informative Test file name
     */
    public static final String FILE_NAME_NON_INFORMATIVE2 = "Non-Informative_Name";

    /**
     * Constructor
     */
    public DGIWGGeoPackageManagerTest() {

    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateOpenInformative() throws IOException {
        testCreateOpen(FILE_NAME, true);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateOpenNonInformative() throws IOException {
        testCreateOpen(FILE_NAME_NON_INFORMATIVE, false);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateOpenNonInformative2() throws IOException {
        testCreateOpen(FILE_NAME_NON_INFORMATIVE2, false);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateFileOpenInformative() throws IOException {
        testCreateExternalOpen(FILE_NAME, true, 0);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateFileOpenInformative2() throws IOException {
        testCreateExternalOpen(FILE_NAME, true, 1);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateFileOpenNonInformative() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE, false, 0);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateFileOpenNonInformative2() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE2, false, 0);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateFileOpenNonInformative3() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE, false, 1);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateFileOpenNonInformative4() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE2, false, 1);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateAtPathOpenInformative() throws IOException {
        testCreateExternalOpen(FILE_NAME, true, 2);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateAtPathOpenNonInformative() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE, false, 2);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateAtPathOpenNonInformative2() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE2, false, 2);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateDocumentFileOpenInformative() throws IOException {
        testCreateExternalOpen(FILE_NAME, true, 3);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateDocumentFileOpenInformative2() throws IOException {
        testCreateExternalOpen(FILE_NAME, true, 4);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateDocumentFileOpenNonInformative() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE, false, 3);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateDocumentFileOpenNonInformative2() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE2, false, 3);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateDocumentFileOpenNonInformative3() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE, false, 4);
    }

    /**
     * Test creating and opening a database
     *
     * @throws IOException upon error
     */
    @Test
    public void testCreateDocumentFileOpenNonInformative4() throws IOException {
        testCreateExternalOpen(FILE_NAME_NON_INFORMATIVE2, false, 4);
    }

    /**
     * Test creating and opening a database
     *
     * @param name        file name
     * @param informative expected complete informative file name
     * @throws IOException upon error
     */
    private void testCreateOpen(String name, boolean informative)
            throws IOException {

        DGIWGGeoPackageManager manager = DGIWGGeoPackageFactory.getManager(activity);

        manager.delete(name);

        // Create
        GeoPackageFile file = manager.create(name, DGIWGGeoPackageTest.getMetadata(activity));
        assertNotNull("Database failed to create", file);
        assertEquals(name, file.getName());
        assertEquals(informative, file.getFileName().isInformative());
        assertTrue("Database does not exist",
                manager.exists(name));
        assertTrue("Database not returned in the set", manager.databaseSet()
                .contains(name));

        // Open
        DGIWGGeoPackage geoPackage = manager.open(file);
        assertNotNull("Failed to open database", geoPackage);
        assertEquals(name, geoPackage.getName());
        assertEquals(informative, geoPackage.getFileName().isInformative());
        assertEquals(name, geoPackage.getFileName().getName());
        geoPackage.close();

        assertTrue(manager.delete(file));
    }

    /**
     * Test creating and opening a database
     *
     * @param name        file name
     * @param informative expected complete informative file name
     * @param testCase    test case
     * @throws IOException upon error
     */
    private void testCreateExternalOpen(String name, boolean informative, int testCase)
            throws IOException {

        DGIWGGeoPackageManager manager = DGIWGGeoPackageFactory.getManager(activity);

        manager.delete(name);

        // Create
        GeoPackageFile file = createExternal(name, manager, testCase);
        assertNotNull("Database failed to create", file);
        assertEquals(name, file.getName());
        assertEquals(informative, file.getFileName().isInformative());
        assertTrue("Database does not exist",
                manager.exists(name));
        assertTrue("Database not returned in the set", manager.databaseSet()
                .contains(name));

        // Open
        DGIWGGeoPackage geoPackage = manager.open(file);
        assertNotNull("Failed to open database", geoPackage);
        assertEquals(name, geoPackage.getName());
        assertEquals(informative, geoPackage.getFileName().isInformative());
        assertEquals(name, geoPackage.getFileName().getName());
        geoPackage.close();

        assertTrue(manager.delete(file));

    }

    /**
     * Create the externally linked GeoPackage
     *
     * @param name     name
     * @param manager  manager
     * @param testCase test case
     * @return GeoPackage file
     * @throws IOException upon error
     */
    private GeoPackageFile createExternal(String name, DGIWGGeoPackageManager manager, int testCase) throws IOException {

        File externalDirectory = activity.getExternalCacheDir();
        File createFile = new File(externalDirectory, name + "."
                + TestConstants.GEO_PACKAGE_EXTENSION);
        createFile.delete();

        String metadata = DGIWGGeoPackageTest.getMetadata(activity);

        GeoPackageFile geoPackageFile = null;
        switch (testCase) {
            case 0:
                geoPackageFile = manager.createFile(createFile, metadata);
                break;
            case 1:
                geoPackageFile = manager.createFile(name, createFile, metadata);
                break;
            case 2:
                geoPackageFile = manager.createAtPath(name, externalDirectory, metadata);
                break;
            case 3:
                geoPackageFile = manager.createFile(DocumentFile.fromFile(createFile), metadata);
                break;
            case 4:
                geoPackageFile = manager.createFile(name, DocumentFile.fromFile(createFile), metadata);
                break;
            default:
                fail("Unsupported test case");
        }
        assertNotNull("Database not created", geoPackageFile);

        return geoPackageFile;
    }

}
