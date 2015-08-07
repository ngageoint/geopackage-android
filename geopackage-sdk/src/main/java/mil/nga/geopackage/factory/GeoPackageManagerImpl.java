package mil.nga.geopackage.factory;

import android.content.Context;
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
import mil.nga.geopackage.db.GeoPackageTableCreator;
import mil.nga.geopackage.db.metadata.GeoPackageMetadata;
import mil.nga.geopackage.db.metadata.GeoPackageMetadataDataSource;
import mil.nga.geopackage.db.metadata.GeoPackageMetadataDb;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.validate.GeoPackageValidate;

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
     * Constructor
     *
     * @param context
     */
    GeoPackageManagerImpl(Context context) {
        this.context = context;
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
    public int count() {
        return context.databaseList().length;
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
    public boolean exists(String database) {
        return databaseSet().contains(database);
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
    public boolean create(String database) {

        boolean created = false;

        if (exists(database)) {
            throw new GeoPackageException("GeoPackage already exists: "
                    + database);
        } else {
            SQLiteDatabase db = context.openOrCreateDatabase(database,
                    Context.MODE_PRIVATE, null);
            GeoPackageConnection connection = new GeoPackageConnection(db);

            // Set the application id as a GeoPackage
            connection.setApplicationId();

            // Create the minimum required tables
            GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(connection);
            tableCreator.createRequired();

            connection.close();
            created = true;
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
                Log.w("Could not determine stream available size. Database: " + database, e);
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

        GeoPackage db = null;

        if (exists(database)) {
            GeoPackageCursorFactory cursorFactory = new GeoPackageCursorFactory();
            String path = null;
            boolean writable = true;
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
                    writable = false;
                }
            } else {
                sqlite = context.openOrCreateDatabase(database,
                        Context.MODE_PRIVATE, cursorFactory);
            }
            GeoPackageConnection connection = new GeoPackageConnection(sqlite);
            GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(connection);
            db = new GeoPackageImpl(database, path, connection, cursorFactory, tableCreator, writable);
        }

        return db;
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
        return importGeoPackageAsExternalLink(path.getAbsolutePath(), database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importGeoPackageAsExternalLink(String path, String database) {

        if (exists(database)) {
            throw new GeoPackageException(
                    "GeoPackage database already exists: " + database);
        }

        // Verify the file is a database and can be opened
        try {
            SQLiteDatabase sqlite = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READONLY
                            | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            sqlite.close();
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

            GeoPackage geoPackage = open(database);
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
     * Add all databases to the collection
     *
     * @param databases
     */
    private void addDatabases(Collection<String> databases) {
        String[] databaseArray = context.databaseList();
        for (String database : databaseArray) {
            if (!isTemporary(database)
                    && !database
                    .equalsIgnoreCase(GeoPackageMetadataDb.DATABASE_NAME)) {
                databases.add(database);
            }
        }

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

        if (!override && exists(database)) {
            throw new GeoPackageException(
                    "GeoPackage database already exists: " + database);
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
                sqlite.close();
            } catch (Exception e) {
                delete(database);
                throw new GeoPackageException(
                        "Invalid GeoPackage database file", e);
            }

            GeoPackage geoPackage = open(database);
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
