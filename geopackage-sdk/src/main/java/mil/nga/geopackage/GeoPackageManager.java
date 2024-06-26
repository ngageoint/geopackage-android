package mil.nga.geopackage;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.IOException;
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
     * Get the application context
     *
     * @return context
     * @since 5.1.0
     */
    public Context getContext();

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
     * @return count
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
     * @param database database name
     * @return true if exists
     */
    public boolean exists(String database);

    /**
     * Size of the database in bytes
     *
     * @param database database name
     * @return bytes
     */
    public long size(String database);

    /**
     * Determine if the database is a linked external file
     *
     * @param database database name
     * @return external flag
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
     * Determine if a database exists at the provided external document file
     *
     * @param file document file
     * @return true if exists
     * @since 5.0.0
     */
    public boolean existsAtExternalFile(DocumentFile file);

    /**
     * Get the path of the database
     *
     * @param database database name
     * @return path
     */
    public String getPath(String database);

    /**
     * Get the file of the database
     *
     * @param database database name
     * @return file
     */
    public File getFile(String database);

    /**
     * Get the document file of the database
     *
     * @param database database name
     * @return document file
     * @since 5.0.0
     */
    public DocumentFile getDocumentFile(String database);

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
     * Get the database name at the external file
     *
     * @param file database document file
     * @return database name or null if does not exist
     * @since 5.0.0
     */
    public String getDatabaseAtExternalFile(DocumentFile file);

    /**
     * Get a readable version of the database size
     *
     * @param database database name
     * @return size
     */
    public String readableSize(String database);

    /**
     * Delete a database
     *
     * @param database database name
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
     * Create a new GeoPackage database at the specified file location
     *
     * @param file GeoPackage document file
     * @return true if created
     * @since 5.0.0
     */
    public boolean createFile(DocumentFile file);

    /**
     * Create a new GeoPackage database at the specified file location with the provided name
     *
     * @param database database name
     * @param file     GeoPackage document file
     * @return true if created
     * @since 5.0.0
     */
    public boolean createFile(String database, DocumentFile file);

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
     * @param progress progress tracker
     * @return true if loaded
     * @since 5.0.0
     */
    public boolean importGeoPackage(File file, GeoPackageProgress progress);

    /**
     * Import a GeoPackage file
     *
     * @param file     GeoPackage file to import
     * @param override true to override existing
     * @return true if created successfully
     */
    public boolean importGeoPackage(File file, boolean override);

    /**
     * Import a GeoPackage file
     *
     * @param file     GeoPackage file to import
     * @param override true to override existing
     * @param progress progress tracker
     * @return true if created successfully
     * @since 5.0.0
     */
    public boolean importGeoPackage(File file, boolean override, GeoPackageProgress progress);

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
     * @param name     database name to save as
     * @param file     GeoPackage file to import
     * @param progress progress tracker
     * @return true if created successfully
     * @since 5.0.0
     */
    public boolean importGeoPackage(String name, File file, GeoPackageProgress progress);

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
     * Import a GeoPackage file
     *
     * @param name     database name to save the imported file as
     * @param file     GeoPackage file to import
     * @param override true to override existing
     * @param progress progress tracker
     * @return true if created successfully
     * @since 5.0.0
     */
    public boolean importGeoPackage(String name, File file, boolean override, GeoPackageProgress progress);

    /**
     * Import a GeoPackage file
     *
     * @param file GeoPackage document file to import
     * @return true if loaded
     * @since 5.0.0
     */
    public boolean importGeoPackage(DocumentFile file);

    /**
     * Import a GeoPackage file
     *
     * @param file     GeoPackage document file to import
     * @param progress progress tracker
     * @return true if loaded
     * @since 5.0.0
     */
    public boolean importGeoPackage(DocumentFile file, GeoPackageProgress progress);

    /**
     * Import a GeoPackage file
     *
     * @param file     GeoPackage document file to import
     * @param override true to override existing
     * @return true if created successfully
     * @since 5.0.0
     */
    public boolean importGeoPackage(DocumentFile file, boolean override);

    /**
     * Import a GeoPackage file
     *
     * @param file     GeoPackage document file to import
     * @param override true to override existing
     * @param progress progress tracker
     * @return true if created successfully
     * @since 5.0.0
     */
    public boolean importGeoPackage(DocumentFile file, boolean override,
                                    GeoPackageProgress progress);

    /**
     * Import a GeoPackage file
     *
     * @param name database name to save as
     * @param file GeoPackage document file to import
     * @return true if created successfully
     * @since 5.0.0
     */
    public boolean importGeoPackage(String name, DocumentFile file);

    /**
     * Import a GeoPackage file
     *
     * @param name     database name to save as
     * @param file     GeoPackage document file to import
     * @param progress progress tracker
     * @return true if created successfully
     * @since 5.0.0
     */
    public boolean importGeoPackage(String name, DocumentFile file, GeoPackageProgress progress);

    /**
     * Import a GeoPackage file
     *
     * @param name     database name to save the imported file as
     * @param file     GeoPackage document file to import
     * @param override true to override existing
     * @return true if created successfully
     * @since 5.0.0
     */
    public boolean importGeoPackage(String name, DocumentFile file, boolean override);

    /**
     * Import a GeoPackage file
     *
     * @param name     database name to save the imported file as
     * @param file     GeoPackage document file to import
     * @param override true to override existing
     * @param progress progress tracker
     * @return true if created successfully
     * @since 5.0.0
     */
    public boolean importGeoPackage(String name, DocumentFile file, boolean override,
                                    GeoPackageProgress progress);

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
     * @param progress progress tracker
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
     * @param progress progress tracker
     * @return true if created successfully
     */
    public boolean importGeoPackage(String database, InputStream stream,
                                    boolean override, GeoPackageProgress progress);

    /**
     * Import a GeoPackage file from a URL
     *
     * @param name GeoPackage name
     * @param url  url
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, URL url);

    /**
     * Import a GeoPackage file from a URL
     *
     * @param name     GeoPackage name
     * @param url      url
     * @param progress progress tracker
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, URL url,
                                    GeoPackageProgress progress);

    /**
     * Import a GeoPackage file from a URL
     *
     * @param name     GeoPackage name
     * @param url      url
     * @param override override flag
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, URL url, boolean override);

    /**
     * Import a GeoPackage file from a URL
     *
     * @param name     GeoPackage name
     * @param url      url
     * @param override override flag
     * @param progress progress tracker
     * @return true if created successfully
     */
    public boolean importGeoPackage(String name, URL url, boolean override,
                                    GeoPackageProgress progress);

    /**
     * Export a GeoPackage database to a file
     *
     * @param database  database name
     * @param directory export directory
     */
    public void exportGeoPackage(String database, File directory);

    /**
     * Export a GeoPackage database to a file
     *
     * @param database  database name
     * @param directory export directory
     * @param progress  progress tracker
     * @since 5.0.0
     */
    public void exportGeoPackage(String database, File directory, GeoPackageProgress progress);

    /**
     * Export a GeoPackage database to a file
     *
     * @param database  database name
     * @param name      name
     * @param directory export directory
     */
    public void exportGeoPackage(String database, String name, File directory);

    /**
     * Export a GeoPackage database to a file
     *
     * @param database  database name
     * @param name      name
     * @param directory export directory
     * @param progress  progress tracker
     * @since 5.0.0
     */
    public void exportGeoPackage(String database, String name, File directory, GeoPackageProgress progress);

    /**
     * Export a GeoPackage database to a document file
     *
     * @param database database name
     * @param file     export document file
     * @since 5.0.0
     */
    public void exportGeoPackage(String database, DocumentFile file);

    /**
     * Export a GeoPackage database to a document file
     *
     * @param database database name
     * @param file     export document file
     * @param progress progress tracker
     * @since 5.0.0
     */
    public void exportGeoPackage(String database, DocumentFile file, GeoPackageProgress progress);

    /**
     * Export a GeoPackage database to a media store
     *
     * @param database     database name
     * @param relativePath Relative path of this media item within the storage device where it is persisted
     * @param uri          The URL of the table to insert into
     * @throws IOException upon error
     * @see android.provider.MediaStore.MediaColumns#RELATIVE_PATH
     * @see android.content.ContentResolver#insert(Uri, ContentValues)
     * @since 3.5.0
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void exportGeoPackage(String database, String relativePath, Uri uri) throws IOException;

    /**
     * Export a GeoPackage database to a media store
     *
     * @param database     database name
     * @param relativePath Relative path of this media item within the storage device where it is persisted
     * @param uri          The URL of the table to insert into
     * @param progress     progress tracker
     * @throws IOException upon error
     * @see android.provider.MediaStore.MediaColumns#RELATIVE_PATH
     * @see android.content.ContentResolver#insert(Uri, ContentValues)
     * @since 5.0.0
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void exportGeoPackage(String database, String relativePath, Uri uri, GeoPackageProgress progress) throws IOException;

    /**
     * Export a GeoPackage database to a media store
     *
     * @param database     database name
     * @param name         name
     * @param relativePath Relative path of this media item within the storage device where it is persisted
     * @param uri          The URL of the table to insert into
     * @throws IOException upon error
     * @see android.provider.MediaStore.MediaColumns#RELATIVE_PATH
     * @see android.content.ContentResolver#insert(Uri, ContentValues)
     * @since 3.5.0
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void exportGeoPackage(String database, String name, String relativePath, Uri uri) throws IOException;

    /**
     * Export a GeoPackage database to a media store
     *
     * @param database     database name
     * @param name         name
     * @param relativePath Relative path of this media item within the storage device where it is persisted
     * @param uri          The URL of the table to insert into
     * @param progress     progress tracker
     * @throws IOException upon error
     * @see android.provider.MediaStore.MediaColumns#RELATIVE_PATH
     * @see android.content.ContentResolver#insert(Uri, ContentValues)
     * @since 5.0.0
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void exportGeoPackage(String database, String name, String relativePath, Uri uri, GeoPackageProgress progress) throws IOException;

    /**
     * Export a GeoPackage database to a Uri
     *
     * @param database      database name
     * @param uri           The URL of the table to insert into
     * @param contentValues The initial values for the newly inserted row
     * @throws IOException upon error
     * @see android.content.ContentResolver#insert(Uri, ContentValues)
     * @since 3.5.0
     */
    public void exportGeoPackage(String database, Uri uri, ContentValues contentValues) throws IOException;

    /**
     * Export a GeoPackage database to a Uri
     *
     * @param database      database name
     * @param uri           The URL of the table to insert into
     * @param contentValues The initial values for the newly inserted row
     * @param progress      progress tracker
     * @throws IOException upon error
     * @see android.content.ContentResolver#insert(Uri, ContentValues)
     * @since 5.0.0
     */
    public void exportGeoPackage(String database, Uri uri, ContentValues contentValues, GeoPackageProgress progress) throws IOException;

    /**
     * Open the database
     *
     * @param database database name
     * @return open GeoPackage
     */
    public GeoPackage open(String database);

    /**
     * Open the database
     *
     * @param database database name
     * @param writable true to open as writable, false as read only
     * @return open GeoPackage
     * @since 2.0.1
     */
    public GeoPackage open(String database, boolean writable);

    /**
     * Open an external GeoPackage
     *
     * @param path full file path
     * @return open GeoPackage
     * @since 5.1.0
     */
    public GeoPackage openExternal(File path);

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param writable true to open as writable, false as read only
     * @return 5.1.0
     */
    public GeoPackage openExternal(File path, boolean writable);

    /**
     * Open an external GeoPackage
     *
     * @param path full file path
     * @return open GeoPackage
     * @since 5.1.0
     */
    public GeoPackage openExternal(String path);

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param writable true to open as writable, false as read only
     * @return 5.1.0
     */
    public GeoPackage openExternal(String path, boolean writable);

    /**
     * Open an external GeoPackage
     *
     * @param file document file
     * @return open GeoPackage
     * @since 5.1.0
     */
    public GeoPackage openExternal(DocumentFile file);

    /**
     * Open an external GeoPackage
     *
     * @param file     document file
     * @param writable true to open as writable, false as read only
     * @return 5.1.0
     */
    public GeoPackage openExternal(DocumentFile file, boolean writable);

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
     * Is the SQLite write ahead logging setting enabled for connections.
     * Off by default.
     *
     * @return write ahead logging state
     * @since 3.1.0
     */
    public boolean isSqliteWriteAheadLogging();

    /**
     * Set the SQLite write ahead logging setting for connections.
     * Off by default.
     *
     * @param enabled true to enable, false to disable
     * @since 3.1.0
     */
    public void setSqliteWriteAheadLogging(boolean enabled);

    /**
     * Get the ignored internal databases
     *
     * @return databases
     * @since 6.7.4
     */
    public Set<String> getIgnoredInternals();

    /**
     * Is the database an ignored internal database by name
     *
     * @param database database name
     * @return true if ignored
     * @since 6.7.4
     */
    public boolean isIgnoredInternal(String database);

    /**
     * Ignore an internal database by name
     *
     * @param database database name
     * @since 6.7.4
     */
    public void ignoreInternal(String database);

    /**
     * Do not ignore an internal database by name
     *
     * @param database database name
     * @since 6.7.4
     */
    public void includeInternal(String database);

    /**
     * Validate the database header and integrity.
     *
     * @param database database name
     * @return true if valid, false if not
     * @since 1.1.1
     */
    public boolean validate(String database);

    /**
     * Validate the database header. Checks the beginning bytes for the SQLite header string.
     *
     * @param database database name
     * @return true if valid, false if not
     * @since 1.1.1
     */
    public boolean validateHeader(String database);

    /**
     * Validate the database integrity. Performs a database integrity ok check.
     *
     * @param database database name
     * @return true if valid, false if not
     * @since 1.1.1
     */
    public boolean validateIntegrity(String database);

    /**
     * Copy the database
     *
     * @param database     database name
     * @param databaseCopy database copy name
     * @return true if copied
     */
    public boolean copy(String database, String databaseCopy);

    /**
     * Copy the database
     *
     * @param database     database name
     * @param databaseCopy database copy name
     * @param progress     progress tracker
     * @return true if copied
     * @since 5.0.0
     */
    public boolean copy(String database, String databaseCopy, GeoPackageProgress progress);

    /**
     * Rename the database to the new name
     *
     * @param database    database name
     * @param newDatabase new database name
     * @return true if renamed
     */
    public boolean rename(String database, String newDatabase);

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param path full file path
     * @return true if imported successfully
     * @since 5.0.0
     */
    public boolean importGeoPackageAsExternalLink(File path);

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
     * @param override true to delete an existing database
     * @return true if imported successfully
     * @since 5.0.0
     */
    public boolean importGeoPackageAsExternalLink(File path, boolean override);

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
     * @param path full file path
     * @return true if imported successfully
     * @since 5.0.0
     */
    public boolean importGeoPackageAsExternalLink(String path);

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
     * @param override true to delete an existing database
     * @return true if imported successfully
     * @since 5.0.0
     */
    public boolean importGeoPackageAsExternalLink(String path, boolean override);

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

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param file document file
     * @return true if imported successfully
     * @since 5.0.0
     */
    public boolean importGeoPackageAsExternalLink(DocumentFile file);

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param file     document file
     * @param override true to delete an existing database
     * @return true if imported successfully
     * @since 5.0.0
     */
    public boolean importGeoPackageAsExternalLink(DocumentFile file, boolean override);

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param file     document file
     * @param database name to reference the database
     * @return true if imported successfully
     * @since 5.0.0
     */
    public boolean importGeoPackageAsExternalLink(DocumentFile file, String database);

    /**
     * Import an GeoPackage as an external file link without copying locally
     *
     * @param file     document file
     * @param database name to reference the database
     * @param override true to delete an existing database
     * @return true if imported successfully
     * @since 5.0.0
     */
    public boolean importGeoPackageAsExternalLink(DocumentFile file, String database, boolean override);

}
