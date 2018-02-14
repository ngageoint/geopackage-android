package mil.nga.geopackage.factory;

import android.content.Context;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.R;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageDatabase;
import mil.nga.geopackage.db.GeoPackageTableCreator;
import mil.nga.geopackage.db.metadata.GeoPackageMetadata;
import mil.nga.geopackage.db.metadata.GeoPackageMetadataDataSource;
import mil.nga.geopackage.db.metadata.GeoPackageMetadataDb;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.validate.GeoPackageValidate;
import mil.nga.wkb.io.ByteReader;

/**
 * GeoPackage Database management implementation
 *
 * @author osbornb
 */
class GeoPackageManagerImpl implements GeoPackageManager {

    /**
     * Context
     */
    private final Context context;

    /**
     * Validate the database header of an imported database
     */
    private boolean importHeaderValidation;

    /**
     * Validate the database integrity of a imported database
     */
    private boolean importIntegrityValidation;

    /**
     * Validate the database header when opening a database
     */
    private boolean openHeaderValidation;

    /**
     * Validate the database integrity when opening a database
     */
    private boolean openIntegrityValidation;

    /**
     * Constructor
     *
     * @param context
     */
    GeoPackageManagerImpl(Context context) {
        this.context = context;

        Resources resources = context.getResources();
        importHeaderValidation = resources.getBoolean(R.bool.manager_validation_import_header);
        importIntegrityValidation = resources.getBoolean(R.bool.manager_validation_import_integrity);
        openHeaderValidation = resources.getBoolean(R.bool.manager_validation_open_header);
        openIntegrityValidation = resources.getBoolean(R.bool.manager_validation_open_integrity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> databases() {
        Set<String> sortedDatabases = new TreeSet<String>();
        addDatabases(sortedDatabases);
        List<String> databases = new ArrayList<String>();
        databases.addAll(sortedDatabases);
        return databases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> databasesLike(String like) {
        List<String> databases = null;
        GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                context);
        metadataDb.open();
        try {
            GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
            databases = dataSource.getMetadataWhereNameLike(like, GeoPackageMetadata.COLUMN_NAME);
        } finally {
            metadataDb.close();
        }

        databases = deleteMissingDatabases(databases);

        return databases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> databasesNotLike(String notLike) {
        List<String> databases = null;
        GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                context);
        metadataDb.open();
        try {
            GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
            databases = dataSource.getMetadataWhereNameNotLike(notLike, GeoPackageMetadata.COLUMN_NAME);
        } finally {
            metadataDb.close();
        }

        databases = deleteMissingDatabases(databases);

        return databases;
    }

    /**
     * Delete all databases that do not exist or the database file does not exist
     *
     * @param databases list of databases
     * @return databases that exist
     */
    private List<String> deleteMissingDatabases(List<String> databases) {
        List<String> filesExist = new ArrayList<>();
        for (String database : databases) {
            if (exists(database)) {
                filesExist.add(database);
            }
        }
        return filesExist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> internalDatabases() {
        Set<String> sortedDatabases = new TreeSet<String>();
        addInternalDatabases(sortedDatabases);
        List<String> databases = new ArrayList<String>();
        databases.addAll(sortedDatabases);
        return databases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> externalDatabases() {
        Set<String> sortedDatabases = new TreeSet<String>();
        addExternalDatabases(sortedDatabases);
        List<String> databases = new ArrayList<String>();
        databases.addAll(sortedDatabases);
        return databases;
    }

    /**
     * {@inheritDoc}
     */
    public int count() {
        return databaseSet().size();
    }

    /**
     * {@inheritDoc}
     */
    public int internalCount() {
        return internalDatabaseSet().size();
    }

    /**
     * {@inheritDoc}
     */
    public int externalCount() {
        return externalDatabaseSet().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> databaseSet() {
        Set<String> databases = new HashSet<String>();
        addDatabases(databases);
        return databases;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> internalDatabaseSet() {
        Set<String> databases = new HashSet<String>();
        addInternalDatabases(databases);
        return databases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> externalDatabaseSet() {
        Set<String> databases = new HashSet<String>();
        addExternalDatabases(databases);
        return databases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String database) {
        boolean exists = internalDatabaseSet().contains(database);

        if (!exists) {
            GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                    context);
            metadataDb.open();
            try {
                GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
                GeoPackageMetadata metadata = dataSource.get(database);
                if (metadata != null) {
                    if (metadata.getExternalPath() != null && !new File(metadata.getExternalPath()).exists()) {
                        delete(database);
                    } else {
                        exists = true;
                    }
                }
            } finally {
                metadataDb.close();
            }
        }
        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size(String database) {
        File dbFile = getFile(database);
        long size = dbFile.length();
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExternal(String database) {
        boolean external = false;
        GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                context);
        metadataDb.open();
        try {
            GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
            external = dataSource.isExternal(database);
        } finally {
            metadataDb.close();
        }
        return external;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsAtExternalFile(File file) {
        return existsAtExternalPath(file.getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsAtExternalPath(String path) {
        GeoPackageMetadata metadata = getGeoPackageMetadataAtExternalPath(path);
        return metadata != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath(String database) {
        File dbFile = getFile(database);
        String path = dbFile.getAbsolutePath();
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile(String database) {
        File dbFile = null;
        GeoPackageMetadata metadata = getGeoPackageMetadata(database);
        if (metadata != null && metadata.isExternal()) {
            dbFile = new File(metadata.getExternalPath());
        } else {
            dbFile = context.getDatabasePath(database);
        }

        if (dbFile == null || !dbFile.exists()) {
            throw new GeoPackageException("GeoPackage does not exist: "
                    + database);
        }

        return dbFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabaseAtExternalFile(File file) {
        return getDatabaseAtExternalPath(file.getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabaseAtExternalPath(String path) {
        String database = null;
        GeoPackageMetadata metadata = getGeoPackageMetadataAtExternalPath(path);
        if (metadata != null) {
            database = metadata.getName();
        }
        return database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readableSize(String database) {
        long size = size(database);
        return GeoPackageIOUtils.formatBytes(size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String database) {
        boolean deleted = false;
        boolean external = isExternal(database);

        GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                context);
        metadataDb.open();
        try {
            GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
            deleted = dataSource.delete(database);
        } finally {
            metadataDb.close();
        }

        if (!external) {
            deleted = context.deleteDatabase(database);
        }
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAll() {

        boolean deleted = true;

        for (String database : databaseSet()) {
            deleted = delete(database) && deleted;
        }

        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllExternal() {

        boolean deleted = true;

        for (String database : externalDatabaseSet()) {
            deleted = delete(database) && deleted;
        }

        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllMissingExternal() {

        boolean deleted = false;

        List<GeoPackageMetadata> externalGeoPackages = getExternalGeoPackages();
        for (GeoPackageMetadata external : externalGeoPackages) {
            if (!new File(external.getExternalPath()).exists()) {
                deleted = delete(external.getName()) || deleted;
            }
        }

        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean create(String database) {

        boolean created = false;

        if (exists(database)) {
            throw new GeoPackageException("GeoPackage already exists: "
                    + database);
        } else {
            GeoPackageDatabase db = new GeoPackageDatabase(context.openOrCreateDatabase(database,
                    Context.MODE_PRIVATE, null));
            createAndCloseGeoPackage(db);
            GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                    context);
            metadataDb.open();
            try {
                GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
                // Save in metadata
                GeoPackageMetadata metadata = new GeoPackageMetadata();
                metadata.setName(database);
                dataSource.create(metadata);
            } finally {
                metadataDb.close();
            }
            created = true;
        }

        return created;
    }

    /**
     * Create the required GeoPackage application id and tables in the newly created and open database connection.  Then close the connection.
     *
     * @param db
     */
    private void createAndCloseGeoPackage(GeoPackageDatabase db) {

        GeoPackageConnection connection = new GeoPackageConnection(db);

        // Set the GeoPackage application id and user version
        connection.setApplicationId();
        connection.setUserVersion();

        // Create the minimum required tables
        GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(connection);
        tableCreator.createRequired();

        connection.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createAtPath(String database, File path) {

        // Create the absolute file path
        File file = new File(path, database + "." + GeoPackageConstants.GEOPACKAGE_EXTENSION);

        // Create the GeoPackage
        boolean created = createFile(database, file);

        return created;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createFile(File file) {

        // Get the database name
        String database = GeoPackageIOUtils.getFileNameWithoutExtension(file);

        // Create the GeoPackage
        boolean created = createFile(database, file);

        return created;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createFile(String database, File file) {

        boolean created = false;

        if (exists(database)) {
            throw new GeoPackageException("GeoPackage already exists: "
                    + database);
        } else {

            // Check if the path is an absolute path to the GeoPackage file to create
            if (!GeoPackageValidate.hasGeoPackageExtension(file)) {

                // Make sure this isn't a path to another file extension
                if (GeoPackageIOUtils.getFileExtension(file) != null) {
                    throw new GeoPackageException("File can not have a non GeoPackage extension. Invalid File: "
                            + file.getAbsolutePath());
                }

                // Add the extension
                file = new File(file.getParentFile(), file.getName() + "." + GeoPackageConstants.GEOPACKAGE_EXTENSION);
            }

            // Make sure the file does not already exist
            if (file.exists()) {
                throw new GeoPackageException("GeoPackage file already exists: "
                        + file.getAbsolutePath());
            }

            // Create the new GeoPackage file
            GeoPackageDatabase db = new GeoPackageDatabase(SQLiteDatabase.openOrCreateDatabase(file, null));
            createAndCloseGeoPackage(db);

            // Import the GeoPackage
            created = importGeoPackageAsExternalLink(file, database);
        }
        return created;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(File file) {
        return importGeoPackage(null, file, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(File file, boolean override) {
        return importGeoPackage(null, file, override);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String database, InputStream stream) {
        return importGeoPackage(database, stream, false, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String database, InputStream stream, GeoPackageProgress progress) {
        return importGeoPackage(database, stream, false, progress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String database, InputStream stream,
                                    boolean override) {
        return importGeoPackage(database, stream, override, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String database, InputStream stream,
                                    boolean override, GeoPackageProgress progress) {

        if (progress != null) {
            try {
                int streamLength = stream.available();
                if (streamLength > 0) {
                    progress.setMax(streamLength);
                }
            } catch (IOException e) {
                Log.w(GeoPackageManagerImpl.class.getSimpleName(), "Could not determine stream available size. Database: " + database, e);
            }
        }

        boolean success = importGeoPackage(database, override, stream, progress);
        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String name, File file) {
        return importGeoPackage(name, file, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String name, File file, boolean override) {

        // Verify the file has the right extension
        GeoPackageValidate.validateGeoPackageExtension(file);

        // Use the provided name or the base file name as the database name
        String database;
        if (name != null) {
            database = name;
        } else {
            database = GeoPackageIOUtils.getFileNameWithoutExtension(file);
        }

        boolean success = false;
        try {
            FileInputStream geoPackageStream = new FileInputStream(file);
            success = importGeoPackage(database, override, geoPackageStream,
                    null);
        } catch (FileNotFoundException e) {
            throw new GeoPackageException(
                    "Failed read or write GeoPackage file '" + file
                            + "' to database: '" + database + "'", e);
        }

        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String name, URL url) {
        return importGeoPackage(name, url, false, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String name, URL url,
                                    GeoPackageProgress progress) {
        return importGeoPackage(name, url, false, progress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String name, URL url, boolean override) {
        return importGeoPackage(name, url, override, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackage(String name, URL url, boolean override,
                                    GeoPackageProgress progress) {

        boolean success = false;

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER){
                String redirect = connection.getHeaderField("Location");
                connection.disconnect();
                url = new URL(redirect);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
            }

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new GeoPackageException("Failed to import GeoPackage "
                        + name + " from URL: '" + url.toString() + "'. HTTP "
                        + connection.getResponseCode() + " "
                        + connection.getResponseMessage());
            }

            int fileLength = connection.getContentLength();
            if (fileLength != -1 && progress != null) {
                progress.setMax(fileLength);
            }

            InputStream geoPackageStream = connection.getInputStream();
            success = importGeoPackage(name, override, geoPackageStream,
                    progress);
        } catch (IOException e) {
            throw new GeoPackageException("Failed to import GeoPackage " + name
                    + " from URL: '" + url.toString() + "'", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportGeoPackage(String database, File directory) {
        exportGeoPackage(database, database, directory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportGeoPackage(String database, String name, File directory) {

        File file = new File(directory, name);

        // Add the extension if not on the name
        if (!GeoPackageValidate.hasGeoPackageExtension(file)) {
            name += "." + GeoPackageConstants.GEOPACKAGE_EXTENSION;
            file = new File(directory, name);
        }

        // Copy the geopackage database to the new file location
        File dbFile = getFile(database);
        try {
            GeoPackageIOUtils.copyFile(dbFile, file);
        } catch (IOException e) {
            throw new GeoPackageException(
                    "Failed read or write GeoPackage database '" + database
                            + "' to file: '" + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoPackage open(String database) {
        return open(database, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoPackage open(String database, boolean writable) {

        GeoPackage db = null;

        if (exists(database)) {
            GeoPackageCursorFactory cursorFactory = new GeoPackageCursorFactory();
            String path = null;
            SQLiteDatabase sqlite = null;
            GeoPackageMetadata metadata = getGeoPackageMetadata(database);
            if (metadata != null && metadata.isExternal()) {
                path = metadata.getExternalPath();
                if(writable){
                    try {
                        sqlite = SQLiteDatabase.openDatabase(path,
                                cursorFactory, SQLiteDatabase.OPEN_READWRITE
                                        | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                    } catch (Exception e) {
                        Log.e(GeoPackageManagerImpl.class.getSimpleName(), "Failed to open database as writable: " + database, e);
                    }
                }
                if(sqlite == null){
                    sqlite = SQLiteDatabase.openDatabase(path,
                            cursorFactory, SQLiteDatabase.OPEN_READONLY
                                    | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                    writable = false;
                }
            } else {
                sqlite = context.openOrCreateDatabase(database,
                        Context.MODE_PRIVATE, cursorFactory);
            }

            // Validate the database if validation is enabled
            validateDatabaseAndCloseOnError(sqlite, openHeaderValidation, openIntegrityValidation);

            GeoPackageConnection connection = new GeoPackageConnection(new GeoPackageDatabase(sqlite));
            GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(connection);
            db = new GeoPackageImpl(database, path, connection, cursorFactory, tableCreator, writable);
        }

        return db;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isImportHeaderValidation() {
        return importHeaderValidation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImportHeaderValidation(boolean enabled) {
        this.importHeaderValidation = enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isImportIntegrityValidation() {
        return importIntegrityValidation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setImportIntegrityValidation(boolean enabled) {
        this.importIntegrityValidation = enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpenHeaderValidation() {
        return openHeaderValidation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOpenHeaderValidation(boolean enabled) {
        this.openHeaderValidation = enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpenIntegrityValidation() {
        return openIntegrityValidation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOpenIntegrityValidation(boolean enabled) {
        this.openIntegrityValidation = enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(String database) {
        boolean valid = isValid(database, true, true);
        return valid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateHeader(String database) {
        boolean valid = isValid(database, true, false);
        return valid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateIntegrity(String database) {
        boolean valid = isValid(database, false, true);
        return valid;
    }

    /**
     * Validate the GeoPackage database
     *
     * @param database
     * @param validateHeader    true to validate the header of the database
     * @param validateIntegrity true to validate the integrity of the database
     * @return true if valid
     */
    private boolean isValid(String database, boolean validateHeader, boolean validateIntegrity) {

        boolean valid = false;

        if (exists(database)) {
            GeoPackageCursorFactory cursorFactory = new GeoPackageCursorFactory();
            String path = null;
            SQLiteDatabase sqlite;
            GeoPackageMetadata metadata = getGeoPackageMetadata(database);
            if (metadata != null && metadata.isExternal()) {
                path = metadata.getExternalPath();
                try {
                    sqlite = SQLiteDatabase.openDatabase(path,
                            cursorFactory, SQLiteDatabase.OPEN_READWRITE
                                    | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                } catch (Exception e) {
                    sqlite = SQLiteDatabase.openDatabase(path,
                            cursorFactory, SQLiteDatabase.OPEN_READONLY
                                    | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                }
            } else {
                path = context.getDatabasePath(database).getAbsolutePath();
                sqlite = context.openOrCreateDatabase(database,
                        Context.MODE_PRIVATE, cursorFactory);
            }

            try {
                valid = (!validateHeader || isDatabaseHeaderValid(sqlite))
                        && (!validateIntegrity || sqlite.isDatabaseIntegrityOk());
            } catch (Exception e) {
                Log.e(GeoPackageManagerImpl.class.getSimpleName(), "Failed to validate database", e);
            } finally {
                sqlite.close();
            }
        }

        return valid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(String database, String databaseCopy) {
        // Copy the database as a new file
        File dbFile = getFile(database);
        File dbCopyFile = context.getDatabasePath(databaseCopy);
        try {
            GeoPackageIOUtils.copyFile(dbFile, dbCopyFile);
        } catch (IOException e) {
            throw new GeoPackageException(
                    "Failed to copy GeoPackage database '" + database
                            + "' to '" + databaseCopy + "'", e);
        }

        return exists(databaseCopy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rename(String database, String newDatabase) {
        GeoPackageMetadata metadata = getGeoPackageMetadata(database);
        if (metadata != null) {

            GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                    context);
            metadataDb.open();
            try {
                GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
                dataSource.rename(metadata, newDatabase);
            } finally {
                metadataDb.close();
            }
        }

        if ((metadata == null || !metadata.isExternal()) && copy(database, newDatabase)) {
            delete(database);
        }
        return exists(newDatabase);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackageAsExternalLink(File path, String database) {
        return importGeoPackageAsExternalLink(path, database, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackageAsExternalLink(File path, String database, boolean override) {
        return importGeoPackageAsExternalLink(path.getAbsolutePath(), database, override);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackageAsExternalLink(String path, String database) {
        return importGeoPackageAsExternalLink(path, database, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackageAsExternalLink(String path, String database, boolean override) {

        if (exists(database)) {
            if (override) {
                if (!delete(database)) {
                    throw new GeoPackageException(
                            "Failed to delete existing database: " + database);
                }
            } else {
                throw new GeoPackageException(
                        "GeoPackage database already exists: " + database);
            }
        }

        // Verify the file is a database and can be opened
        try {
            SQLiteDatabase sqlite = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READONLY
                            | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            validateDatabaseAndClose(sqlite, importHeaderValidation, importIntegrityValidation);
        } catch (SQLiteException e) {
            throw new GeoPackageException(
                    "Failed to import GeoPackage database as external link: "
                            + database + ", Path: " + path, e);
        }

        GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                context);
        metadataDb.open();
        try {
            GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
            // Save the external link in metadata
            GeoPackageMetadata metadata = new GeoPackageMetadata();
            metadata.setName(database);
            metadata.setExternalPath(path);
            dataSource.create(metadata);

            GeoPackage geoPackage = open(database, false);
            if (geoPackage != null) {
                try {
                    GeoPackageValidate.validateMinimumTables(geoPackage);
                } catch (RuntimeException e) {
                    dataSource.delete(database);
                    throw e;
                } finally {
                    geoPackage.close();
                }
            } else {
                dataSource.delete(database);
                throw new GeoPackageException(
                        "Unable to open GeoPackage database. Database: "
                                + database);
            }
        } finally {
            metadataDb.close();
        }

        return exists(database);
    }

    /**
     * Validate the database and close when validation fails. Throw an error when not valid.
     *
     * @param sqliteDatabase    database
     * @param validateHeader    validate the header
     * @param validateIntegrity validate the integrity
     */
    private void validateDatabaseAndCloseOnError(SQLiteDatabase sqliteDatabase, boolean validateHeader, boolean validateIntegrity) {
        validateDatabase(sqliteDatabase, validateHeader, validateIntegrity, false, true);
    }

    /**
     * Validate the database and close it. Throw an error when not valid.
     *
     * @param sqliteDatabase    database
     * @param validateHeader    validate the header
     * @param validateIntegrity validate the integrity
     */
    private void validateDatabaseAndClose(SQLiteDatabase sqliteDatabase, boolean validateHeader, boolean validateIntegrity) {
        validateDatabase(sqliteDatabase, validateHeader, validateIntegrity, true, true);
    }

    /**
     * Validate the database header and integrity.  Throw an error when not valid.
     *
     * @param sqliteDatabase    database
     * @param validateHeader    validate the header
     * @param validateIntegrity validate the integrity
     * @param close             close the database after validation
     * @param closeOnError      close the database if validation fails
     */
    private void validateDatabase(SQLiteDatabase sqliteDatabase, boolean validateHeader, boolean validateIntegrity, boolean close, boolean closeOnError) {
        try {
            if (validateHeader) {
                validateDatabaseHeader(sqliteDatabase);
            }
            if (validateIntegrity) {
                validateDatabaseIntegrity(sqliteDatabase);
            }
        } catch (Exception e) {
            if (closeOnError) {
                sqliteDatabase.close();
            }
            throw e;
        }

        if (close) {
            sqliteDatabase.close();
        }
    }

    /**
     * Validate the header of the database file to verify it is a sqlite database
     *
     * @param sqliteDatabase database
     */
    private void validateDatabaseHeader(SQLiteDatabase sqliteDatabase) {

        boolean validHeader = isDatabaseHeaderValid(sqliteDatabase);
        if (!validHeader) {
            throw new GeoPackageException(
                    "GeoPackage SQLite header is not valid: " + sqliteDatabase.getPath());
        }
    }

    /**
     * Determine if the header of the database file is valid
     *
     * @param sqliteDatabase database
     * @return true if valid
     */
    private boolean isDatabaseHeaderValid(SQLiteDatabase sqliteDatabase) {

        boolean validHeader = false;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(sqliteDatabase.getPath());
            byte[] headerBytes = new byte[16];
            if (fis.read(headerBytes) == 16) {
                ByteReader byteReader = new ByteReader(headerBytes);
                String header = byteReader.readString(headerBytes.length);
                String headerPrefix = header.substring(0, GeoPackageConstants.SQLITE_HEADER_PREFIX.length());
                validHeader = headerPrefix.equalsIgnoreCase(GeoPackageConstants.SQLITE_HEADER_PREFIX);
            }
        } catch (Exception e) {
            Log.e(GeoPackageManagerImpl.class.getSimpleName(), "Failed to retrieve database header", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // eat
                }
            }
        }

        return validHeader;
    }

    /**
     * Validate the integrity of the database
     *
     * @param sqliteDatabase database
     */
    private void validateDatabaseIntegrity(SQLiteDatabase sqliteDatabase) {

        if (!sqliteDatabase.isDatabaseIntegrityOk()) {
            throw new GeoPackageException(
                    "GeoPackage SQLite file integrity failed: " + sqliteDatabase.getPath());
        }
    }

    /**
     * Add all databases to the collection
     *
     * @param databases
     */
    private void addDatabases(Collection<String> databases) {

        // Add the internal databases
        addInternalDatabases(databases);

        // Add the external databases
        addExternalDatabases(databases);
    }

    /**
     * Add all internal databases to the collection
     *
     * @param databases
     */
    private void addInternalDatabases(Collection<String> databases) {
        String[] databaseArray = context.databaseList();
        for (String database : databaseArray) {
            if (!isTemporary(database)
                    && !database
                    .equalsIgnoreCase(GeoPackageMetadataDb.DATABASE_NAME)) {
                databases.add(database);
            }
        }
    }

    /**
     * Add all external databases to the collection
     *
     * @param databases
     */
    private void addExternalDatabases(Collection<String> databases) {
        // Get the external GeoPackages, adding those where the file exists and
        // deleting those with missing files
        List<GeoPackageMetadata> externalGeoPackages = getExternalGeoPackages();
        for (GeoPackageMetadata external : externalGeoPackages) {
            if (new File(external.getExternalPath()).exists()) {
                databases.add(external.getName());
            } else {
                delete(external.getName());
            }
        }
    }

    /**
     * Import the GeoPackage stream
     *
     * @param database
     * @param override
     * @param geoPackageStream
     * @param progress
     * @return true if imported successfully
     */
    private boolean importGeoPackage(String database, boolean override,
                                     InputStream geoPackageStream, GeoPackageProgress progress) {

        if (exists(database)) {
            if (override) {
                if (!delete(database)) {
                    throw new GeoPackageException(
                            "Failed to delete existing database: " + database);
                }
            } else {
                throw new GeoPackageException(
                        "GeoPackage database already exists: " + database);
            }
        }

        // Copy the geopackage over as a database
        File newDbFile = context.getDatabasePath(database);
        try {
            SQLiteDatabase db = context.openOrCreateDatabase(database,
                    Context.MODE_PRIVATE, null);
            db.close();
            GeoPackageIOUtils.copyStream(geoPackageStream, newDbFile, progress);
        } catch (IOException e) {
            throw new GeoPackageException(
                    "Failed to import GeoPackage database: " + database, e);
        }

        if (progress == null || progress.isActive()) {

            // Verify that the database is valid
            try {
                SQLiteDatabase sqlite = context.openOrCreateDatabase(database,
                        Context.MODE_PRIVATE, null, new DatabaseErrorHandler() {
                            @Override
                            public void onCorruption(SQLiteDatabase dbObj) {
                            }
                        });
                validateDatabaseAndClose(sqlite, importHeaderValidation, importIntegrityValidation);

                GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                        context);
                metadataDb.open();
                try {
                    GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
                    // Save in metadata
                    GeoPackageMetadata metadata = new GeoPackageMetadata();
                    metadata.setName(database);
                    dataSource.create(metadata);
                } finally {
                    metadataDb.close();
                }
            } catch (Exception e) {
                delete(database);
                throw new GeoPackageException(
                        "Invalid GeoPackage database file", e);
            }

            GeoPackage geoPackage = open(database, false);
            if (geoPackage != null) {
                try {
                    if (!geoPackage.getSpatialReferenceSystemDao()
                            .isTableExists()
                            || !geoPackage.getContentsDao().isTableExists()) {
                        delete(database);
                        throw new GeoPackageException(
                                "Invalid GeoPackage database file. Does not contain required tables: "
                                        + SpatialReferenceSystem.TABLE_NAME
                                        + " & " + Contents.TABLE_NAME
                                        + ", Database: " + database);
                    }
                } catch (SQLException e) {
                    delete(database);
                    throw new GeoPackageException(
                            "Invalid GeoPackage database file. Could not verify existence of required tables: "
                                    + SpatialReferenceSystem.TABLE_NAME
                                    + " & "
                                    + Contents.TABLE_NAME
                                    + ", Database: "
                                    + database);
                } finally {
                    geoPackage.close();
                }
            } else {
                delete(database);
                throw new GeoPackageException(
                        "Unable to open GeoPackage database. Database: "
                                + database);
            }
        }

        return exists(database);
    }

    /**
     * Get all external GeoPackage metadata
     *
     * @return
     */
    private List<GeoPackageMetadata> getExternalGeoPackages() {
        List<GeoPackageMetadata> metadata = null;

        GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                context);
        metadataDb.open();
        try {
            GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
            metadata = dataSource.getAllExternal();
        } finally {
            metadataDb.close();
        }

        return metadata;
    }

    /**
     * Get the GeoPackage metadata
     *
     * @param database
     * @return
     */
    private GeoPackageMetadata getGeoPackageMetadata(String database) {
        GeoPackageMetadata metadata = null;

        GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                context);
        metadataDb.open();
        try {
            GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
            metadata = dataSource.get(database);
        } finally {
            metadataDb.close();
        }

        return metadata;
    }

    /**
     * Get the GeoPackage metadata of the database at the external path
     *
     * @param path external database path
     * @return metadata or null
     */
    private GeoPackageMetadata getGeoPackageMetadataAtExternalPath(String path) {
        GeoPackageMetadata metadata = null;

        GeoPackageMetadataDb metadataDb = new GeoPackageMetadataDb(
                context);
        metadataDb.open();
        try {
            GeoPackageMetadataDataSource dataSource = new GeoPackageMetadataDataSource(metadataDb);
            metadata = dataSource.getExternalAtPath(path);
        } finally {
            metadataDb.close();
        }

        return metadata;
    }

    /**
     * Check if the database is temporary (rollback journal)
     *
     * @param database
     * @return
     */
    private boolean isTemporary(String database) {
        return database.endsWith(context
                .getString(R.string.geopackage_db_rollback_suffix));
    }

}
