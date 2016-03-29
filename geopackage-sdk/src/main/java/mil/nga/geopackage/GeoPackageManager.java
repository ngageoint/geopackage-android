package mil.nga.geopackage;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.io.GeoPackageProgress;

/**
 * GeoPackage Database management
 *
 * @author osbornb
 */
public interface GeoPackageManager {

    /**
     * List all GeoPackage databases sorted alphabetically
     *
     * @return database list
     */
    public List<String> databases();

    /**
     * List GeoPackage databases that match the provided like argument
     *
     * @param like like argument, using % as a wild card
     * @return database names
     * @since 1.2.7
     */
    public List<String> databasesLike(String like);

    /**
     * List GeoPackage databases that do not match the provided like argument
     *
     * @param notLike not like argument, using % as a wild card
     * @return database names
     * @since 1.2.7
     */
    public List<String> databasesNotLike(String notLike);

    /**
     * List all internal GeoPackage databases sorted alphabetically
     *
     * @return internal database list
     * @since 1.2.4
     */
    public List<String> internalDatabases();

    /**
     * List all external GeoPackage databases sorted alphabetically
     *
     * @return external database list
     */
    public List<String> externalDatabases();

    /**
     * Get the count of GeoPackage databases
     *
     * @return
     */
    public int count();

    /**
     * Get the count of internal GeoPackage databases
     *
     * @return internal count
     * @since 1.2.4
     */
    public int internalCount();

    /**
     * Get the count of external GeoPackage databases
     *
     * @return external count
     * @since 1.2.4
     */
    public int externalCount();

    /**
     * Set of all GeoPackage databases
     *
     * @return database set
     */
    public Set<String> databaseSet();

    /**
     * Set of all internal GeoPackage databases
     *
     * @return internal database set
     * @since 1.2.4
     */
    public Set<String> internalDatabaseSet();

    /**
     * Set of all external GeoPackage databases
     *
     * @return external database set
     */
    public Set<String> externalDatabaseSet();

    /**
     * Determine if the database exists
     *
     * @param database
     * @return true if exists
     */
    public boolean exists(String database);

    /**
     * Size of the database in bytes
     *
     * @param database
     * @return
     */
    public long size(String database);

    /**
     * Determine if the database is a linked external file
     *
     * @param database
     * @return
     */
    public boolean isExternal(String database);

    /**
     * Determine if a database exists at the provided external file
     *
     * @param file database file
     * @return true if exists
     * @since 1.1.1
     */
    public boolean existsAtExternalFile(File file);

    /**
     * Determine if a database exists at the provided external file path
     *
     * @param path database file path
     * @return true if exists
     * @since 1.1.1
     */
    public boolean existsAtExternalPath(String path);

    /**
     * Get the path of the database
     *
     * @param database
     * @return
     */
    public String getPath(String database);

    /**
     * Get the file of the database
     *
     * @param database
     * @return
     */
    public File getFile(String database);

    /**
     * Get the database name at the external file
     *
     * @param file database file
     * @return database name or null if does not exist
     * @since 1.1.1
     */
    public String getDatabaseAtExternalFile(File file);

    /**
     * Get the database name at the external file path
     *
     * @param path database file path
     * @return database name or null if does not exist
     * @since 1.1.1
     */
    public String getDatabaseAtExternalPath(String path);

    /**
     * Get a readable version of the database size
     *
     * @param database
     * @return
     */
    public String readableSize(String database);

    /**
     * Delete a database
     *
     * @param database
     * @return true if deleted
     */
    public boolean delete(String database);

    /**
     * Delete all databases
     *
     * @return true if deleted
     */
    public boolean deleteAll();

    /**
     * Delete all external GeoPackages
     *
     * @return true if deleted
     */
    public boolean deleteAllExternal();

    /**
     * Delete all external GeoPackages where the external file can no longer be found
     *
     * @return true if any were deleted
     * @since 1.1.1
     */
    public boolean deleteAllMissingExternal();

    /**
     * Create a new GeoPackage database
     *
     * @param database database name
     * @return true if created
     */
    public boolean create(String database);

    /**
     * Create a new GeoPackage database at the provided directory path
     *
     * @param database database name
     * @param path     directory path
     * @return true if created
     * @since 1.2.6
     */
    public boolean createAtPath(String database, File path);

    /**
     * Create a new GeoPackage database at the specified file location
     *
     * @param file GeoPackage file path
     * @return true if created
     * @since 1.2.6
     */
    public boolean createFile(File file);

    /**
     * Create a new GeoPackage database at the specified file location with the provided name
     *
     * @param database database name
     * @param file     GeoPackage file path
     * @return true if created
     * @since 1.2.6
     */
    public boolean createFile(String database, File file);

    /**
     * Import a GeoPackage file
     *
     * @param file GeoPackage file to import
     * @return true if loaded
     */
    public boolean importGeoPackage(File file);

    /**
     * Import a GeoPackage file
     *
     * @param file     GeoPackage file to import
     * @param override true to override existing
     * @return true if created successfully
     */
    public boolean importGeoPackage(File file, boolean override);

    /**
     * Import a GeoPackage stream
     *
     * @param database database name to save as
     * @param stream   GeoPackage stream to import
     * @return true if loaded
     */
    public boolean importGeoPackage(String database, InputStream stream);

    /**
     * Import a GeoPackage stream
     *
     * @param database database name to save as
     * @param stream   GeoPackage stream to import
     * @param progress
     * @return true if loaded
     */
    public boolean importGeoPackage(String database, InputStream stream,
                                    GeoPackageProgress progress);

    /**
     * Import a GeoPackage stream
     *
     * @param database database name to save as
     * @param stream   GeoPackage stream to import
     * @param override true to override existing
     * @return true if created successfully
     */
    public boolean importGeoPackage(String database, InputStream stream,
                                    boolean override);

    /**
     * Import a GeoPackage stream
     *
     * @param database database name to save as
     * @param stream   GeoPackage stream to import
     * @param override true to override existing
     * @param progress
     * @return true if created successfully
     */
    public boolean importGeoPackage(String database, InputStream stream,
                                    boolean override, GeoPackageProgress progress);

    /**
     * Import a GeoPackage file
     *
     * @param name database name to save as
     * @param file GeoPackage file to import
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, File file);

    /**
     * Import a GeoPackage file
     *
     * @param name     database name to save the imported file as
     * @param file     GeoPackage file to import
     * @param override true to override existing
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, File file, boolean override);

    /**
     * Import a GeoPackage file from a URL
     *
     * @param name
     * @param url
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, URL url);

    /**
     * Import a GeoPackage file from a URL
     *
     * @param name
     * @param url
     * @param progress
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, URL url,
                                    GeoPackageProgress progress);

    /**
     * Import a GeoPackage file from a URL
     *
     * @param name
     * @param url
     * @param override
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, URL url, boolean override);

    /**
     * Import a GeoPackage file from a URL
     *
     * @param name
     * @param url
     * @param override
     * @param progress
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, URL url, boolean override,
                                    GeoPackageProgress progress);

    /**
     * Export a GeoPackage database to a file
     *
     * @param database
     * @param directory
     */
    public void exportGeoPackage(String database, File directory);

    /**
     * Export a GeoPackage database to a file
     *
     * @param database
     * @param name
     * @param directory
     */
    public void exportGeoPackage(String database, String name, File directory);

    /**
     * Open the database
     *
     * @param database
     * @return
     */
    public GeoPackage open(String database);

    /**
     * Is import database header validation enabled.
     * This causes a small time increase when importing a database to check the header bytes.
     * On by default.
     *
     * @return true if enabled
     * @since 1.1.1
     */
    public boolean isImportHeaderValidation();

    /**
     * Set the import database header validation setting.
     * This causes a small time increase when importing a database to check the header bytes.
     * On by default.
     *
     * @param enabled true to enable, false to disable
     * @since 1.1.1
     */
    public void setImportHeaderValidation(boolean enabled);

    /**
     * Is import database integrity validation enabled.
     * This causes a noticeable time increase when importing a database to check the database integrity.
     * Off by default.
     *
     * @return true if enabled
     * @since 1.1.1
     */
    public boolean isImportIntegrityValidation();

    /**
     * Set the import database integrity validation setting.
     * This causes a noticeable time increase when importing a database to check the database integrity.
     * Off by default.
     *
     * @param enabled true to enable, false to disable
     * @since 1.1.1
     */
    public void setImportIntegrityValidation(boolean enabled);

    /**
     * Is open database header validation enabled.
     * This causes a small time increase when opening a database to check the header bytes.
     * Off by default.
     *
     * @return true if enabled
     * @since 1.1.1
     */
    public boolean isOpenHeaderValidation();

    /**
     * Set the open database header validation setting.
     * This causes a small time increase when opening a database to check the header bytes.
     * Off by default.
     *
     * @param enabled true to enable, false to disable
     * @since 1.1.1
     */
    public void setOpenHeaderValidation(boolean enabled);

    /**
     * Is open database integrity validation enabled.
     * This causes a noticeable time increase when opening a database to check the database integrity.
     * Off by default.
     *
     * @return true if enabled
     * @since 1.1.1
     */
    public boolean isOpenIntegrityValidation();

    /**
     * Set the open database integrity validation setting.
     * This causes a noticeable time increase when opening a database to check the database integrity.
     * Off by default.
     *
     * @param enabled true to enable, false to disable
     * @since 1.1.1
     */
    public void setOpenIntegrityValidation(boolean enabled);

    /**
     * Validate the database header and integrity.
     *
     * @param database
     * @return true if valid, false if not
     * @since 1.1.1
     */
    public boolean validate(String database);

    /**
     * Validate the database header. Checks the beginning bytes for the SQLite header string.
     *
     * @param database
     * @return true if valid, false if not
     * @since 1.1.1
     */
    public boolean validateHeader(String database);

    /**
     * Validate the database integrity. Performs a database integrity ok check.
     *
     * @param database
     * @return true if valid, false if not
     * @since 1.1.1
     */
    public boolean validateIntegrity(String database);

    /**
     * Copy the database
     *
     * @param database
     * @param databaseCopy
     * @return
     */
    public boolean copy(String database, String databaseCopy);

    /**
     * Rename the database to the new name
     *
     * @param database
     * @param newDatabase
     * @return
     */
    public boolean rename(String database, String newDatabase);

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param path     full file path
     * @param database name to reference the database
     * @return true if imported successfully
     */
    public boolean importGeoPackageAsExternalLink(File path, String database);

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param path     full file path
     * @param database name to reference the database
     * @param override true to delete an existing database
     * @return true if imported successfully
     * @since 1.2.7
     */
    public boolean importGeoPackageAsExternalLink(File path, String database, boolean override);

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param path     full file path
     * @param database name to reference the database
     * @return true if imported successfully
     */
    public boolean importGeoPackageAsExternalLink(String path, String database);

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param path     full file path
     * @param database name to reference the database
     * @param override true to delete an existing database
     * @return true if imported successfully
     * @since 1.2.7
     */
    public boolean importGeoPackageAsExternalLink(String path, String database, boolean override);

}
