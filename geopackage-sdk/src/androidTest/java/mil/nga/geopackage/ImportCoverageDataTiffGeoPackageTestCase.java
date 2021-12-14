package mil.nga.geopackage;

import org.junit.After;

import java.io.File;

/**
 * Abstract Test Case for Imported TIFF Tiled Gridded Coverage Data GeoPackages
 *
 * @author osbornb
 */
public abstract class ImportCoverageDataTiffGeoPackageTestCase extends
        GeoPackageTestCase {

    /**
     * Constructor
     */
    public ImportCoverageDataTiffGeoPackageTestCase() {

    }

    @Override
    protected GeoPackage getGeoPackage() throws Exception {

        GeoPackageManager manager = GeoPackageFactory.getManager(activity);

        // Delete
        manager.delete(TestConstants.IMPORT_COVERAGE_DATA_TIFF_DB_NAME);

        // Copy the test db file from assets to the internal storage
        TestUtils.copyAssetFileToInternalStorage(activity, testContext,
                TestConstants.IMPORT_COVERAGE_DATA_TIFF_DB_FILE_NAME);

        // Import
        String importLocation = TestUtils.getAssetFileInternalStorageLocation(
                activity, TestConstants.IMPORT_COVERAGE_DATA_TIFF_DB_FILE_NAME);
        manager.importGeoPackage(new File(importLocation));

        // Open
        GeoPackage geoPackage = manager.open(TestConstants.IMPORT_COVERAGE_DATA_TIFF_DB_NAME);
        if (geoPackage == null) {
            throw new GeoPackageException("Failed to open database");
        }

        return geoPackage;
    }

    @After
    public void tearDown() throws Exception {

        // Close
        if (geoPackage != null) {
            geoPackage.close();
        }

    }

}
