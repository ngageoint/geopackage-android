package mil.nga.geopackage.dgiwg;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManagerImpl;
import mil.nga.geopackage.extension.CrsWktExtension;
import mil.nga.geopackage.extension.CrsWktExtensionVersion;
import mil.nga.geopackage.io.GeoPackageIOUtils;

/**
 * DGIWG (Defence Geospatial Information Working Group) GeoPackage Manager used
 * to create and open GeoPackages
 *
 * @author osbornb
 * @since 6.5.1
 */
public class DGIWGGeoPackageManager extends GeoPackageManagerImpl {

    /**
     * Constructor
     *
     * @param context context
     */
    protected DGIWGGeoPackageManager(Context context) {
        super(context);
    }

    /**
     * Create a GeoPackage
     *
     * @param database database name
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile create(String database, String metadata) {
        return create(database, DGIWGConstants.DMF_DEFAULT_URI, metadata);
    }

    /**
     * Create a GeoPackage
     *
     * @param database database name
     * @param uri      URI
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile create(String database, String uri, String metadata) {
        GeoPackageFile geoPackageFile = null;
        if (super.create(database)) {
            geoPackageFile = createDGIWG(database, null, uri, metadata);
        }
        return geoPackageFile;
    }

    /**
     * Create a new GeoPackage database at the provided directory path
     *
     * @param database database name
     * @param path     directory path
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createAtPath(String database, File path, String metadata) {
        return createAtPath(database, path, DGIWGConstants.DMF_DEFAULT_URI, metadata);
    }

    /**
     * Create a new GeoPackage database at the provided directory path
     *
     * @param database database name
     * @param path     directory path
     * @param uri      URI
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createAtPath(String database, File path, String uri, String metadata) {
        GeoPackageFile geoPackageFile = null;
        if (super.createAtPath(database, path)) {
            geoPackageFile = createDGIWG(database, path, uri, metadata);
        }
        return geoPackageFile;
    }

    /**
     * Create a new GeoPackage database at the specified file location
     *
     * @param file     GeoPackage file path
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createFile(File file, String metadata) {
        return createFile(file, DGIWGConstants.DMF_DEFAULT_URI, metadata);
    }

    /**
     * Create a new GeoPackage database at the specified file location
     *
     * @param file     GeoPackage file path
     * @param uri      URI
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createFile(File file, String uri, String metadata) {
        return createFile(null, file, uri, metadata);
    }

    /**
     * Create a new GeoPackage database at the specified file location with the provided name
     *
     * @param database database name
     * @param file     GeoPackage file path
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createFile(String database, File file, String metadata) {
        return createFile(database, file, DGIWGConstants.DMF_DEFAULT_URI, metadata);
    }

    /**
     * Create a new GeoPackage database at the specified file location with the provided name
     *
     * @param database database name
     * @param file     GeoPackage file path
     * @param uri      URI
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createFile(String database, File file, String uri, String metadata) {
        GeoPackageFile geoPackageFile = null;
        if (super.createFile(database, file)) {
            geoPackageFile = createDGIWG(database, file, uri, metadata);
        }
        return geoPackageFile;
    }

    /**
     * Create a new GeoPackage database at the specified file location
     *
     * @param file     GeoPackage document file
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createFile(DocumentFile file, String metadata) {
        return createFile(file, DGIWGConstants.DMF_DEFAULT_URI, metadata);
    }

    /**
     * Create a new GeoPackage database at the specified file location
     *
     * @param file     GeoPackage document file
     * @param uri      URI
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createFile(DocumentFile file, String uri, String metadata) {
        return createFile(file.getName(), file, uri, metadata);
    }

    /**
     * Create a new GeoPackage database at the specified file location with the provided name
     *
     * @param database database name
     * @param file     GeoPackage document file
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createFile(String database, DocumentFile file, String metadata) {
        return createFile(database, file, DGIWGConstants.DMF_DEFAULT_URI, metadata);
    }

    /**
     * Create a new GeoPackage database at the specified file location with the provided name
     *
     * @param database database name
     * @param file     GeoPackage document file
     * @param uri      URI
     * @param metadata metadata
     * @return GeoPackage file if created
     */
    public GeoPackageFile createFile(String database, DocumentFile file, String uri, String metadata) {
        return createFile(database, getFile(file), uri, metadata);
    }

    /**
     * Create the DGIWG GeoPackage file
     *
     * @param database database name
     * @param file     GeoPackage file path
     * @param uri      URI
     * @param metadata metadata
     * @return GeoPackage file
     */
    private GeoPackageFile createDGIWG(String database, File file, String uri, String metadata) {

        GeoPackageFile geoPackageFile = null;

        if (database == null) {
            // Get the database name
            database = getDatabase(file);
        } else {
            database = GeoPackageIOUtils.getFileNameWithoutExtension(database);
        }

        try (DGIWGGeoPackage geoPackage = open(false, database)) {

            if (geoPackage != null) {

                CrsWktExtension wktExtension = new CrsWktExtension(geoPackage);
                wktExtension.getOrCreate(CrsWktExtensionVersion.V_1);

                geoPackage.createGeoPackageDatasetMetadata(uri, metadata);

                geoPackageFile = geoPackage.getFile();

            }
        }

        return geoPackageFile;
    }

    /**
     * Open the database
     *
     * @param database database name
     * @return open GeoPackage
     */
    public DGIWGGeoPackage open(String database) {
        return open(database, true, true);
    }

    /**
     * Open the database
     *
     * @param database database name
     * @param writable true to open as writable, false as read only
     * @return open GeoPackage
     */
    public DGIWGGeoPackage open(String database, boolean writable) {
        return open(database, writable, true);
    }

    /**
     * Open the database
     *
     * @param validate validate the GeoPackage
     * @param database database name
     * @return open GeoPackage
     */
    public DGIWGGeoPackage open(boolean validate, String database) {
        return open(database, true, validate);
    }

    /**
     * Open the database
     *
     * @param database database name
     * @param writable true to open as writable, false as read only
     * @param validate validate the GeoPackage
     * @return open GeoPackage
     */
    public DGIWGGeoPackage open(String database, boolean writable, boolean validate) {

        DGIWGGeoPackage geoPackage = null;

        GeoPackage gp = super.open(database, writable);
        if (gp != null) {

            geoPackage = new DGIWGGeoPackage(gp);

            if (validate) {
                validate(geoPackage);
            }

        }

        return geoPackage;
    }

    /**
     * Open the database
     *
     * @param file GeoPackage file
     * @return open GeoPackage
     */
    public DGIWGGeoPackage open(GeoPackageFile file) {
        return open(file, true, true);
    }

    /**
     * Open the database
     *
     * @param file     GeoPackage file
     * @param writable true to open as writable, false as read only
     * @return open GeoPackage
     */
    public DGIWGGeoPackage open(GeoPackageFile file, boolean writable) {
        return open(file, writable, true);
    }

    /**
     * Open the database
     *
     * @param validate validate the GeoPackage
     * @param file     GeoPackage file
     * @return open GeoPackage
     */
    public DGIWGGeoPackage open(boolean validate, GeoPackageFile file) {
        return open(file, true, validate);
    }

    /**
     * Open the database
     *
     * @param file     GeoPackage file
     * @param writable true to open as writable, false as read only
     * @param validate validate the GeoPackage
     * @return open GeoPackage
     */
    public DGIWGGeoPackage open(GeoPackageFile file, boolean writable, boolean validate) {
        return open(file.getName(), writable, validate);
    }

    /**
     * Delete the database
     *
     * @param file GeoPackage file
     * @return true if deleted
     */
    public boolean delete(GeoPackageFile file) {
        return super.delete(file.getName());
    }

    /**
     * Is the GeoPackage valid according to the DGIWG GeoPackage Profile
     *
     * @param geoPackage GeoPackage
     * @return true if valid
     */
    public static boolean isValid(DGIWGGeoPackage geoPackage) {
        return geoPackage.isValid();
    }

    /**
     * Validate the GeoPackage against the DGIWG GeoPackage Profile
     *
     * @param geoPackage GeoPackage
     * @return validation errors
     */
    public static DGIWGValidationErrors validate(DGIWGGeoPackage geoPackage) {
        return geoPackage.validate();
    }

}
