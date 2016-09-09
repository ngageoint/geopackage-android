package mil.nga.geopackage.test;

import java.io.File;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;

/**
 * Abstract Test Case for Imported Elevation Tiles Tiff GeoPackages
 *
 * @author osbornb
 */
public abstract class ImportElevationTilesTiffGeoPackageTestCase extends
        GeoPackageTestCase {

    /**
     * Constructor
     */
    public ImportElevationTilesTiffGeoPackageTestCase() {

    }

    @Override
    protected GeoPackage getGeoPackage() throws Exception {

        GeoPackageManager manager = GeoPackageFactory.getManager(activity);

        // Delete
        manager.delete(TestConstants.IMPORT_ELEVATION_TILES_TIFF_DB_NAME);

        // Copy the test db file from assets to the internal storage
        TestUtils.copyAssetFileToInternalStorage(activity, testContext,
                TestConstants.IMPORT_ELEVATION_TILES_TIFF_DB_FILE_NAME);

        // Import
        String importLocation = TestUtils.getAssetFileInternalStorageLocation(
                activity, TestConstants.IMPORT_ELEVATION_TILES_TIFF_DB_FILE_NAME);
        manager.importGeoPackage(new File(importLocation));

        // Open
        GeoPackage geoPackage = manager.open(TestConstants.IMPORT_ELEVATION_TILES_TIFF_DB_NAME);
        if (geoPackage == null) {
            throw new GeoPackageException("Failed to open database");
        }

        return geoPackage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {

        // Close
        if (geoPackage != null) {
            geoPackage.close();
        }

        super.tearDown();
    }

}
