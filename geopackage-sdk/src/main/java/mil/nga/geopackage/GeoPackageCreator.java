package mil.nga.geopackage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageCursorFactory;
import mil.nga.geopackage.db.GeoPackageDatabase;
import mil.nga.geopackage.db.SQLiteDatabaseUtils;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.validate.GeoPackageValidate;

/**
 * GeoPackage Creator
 *
 * @author osbornb
 * @since 5.1.0
 */
public class GeoPackageCreator {

    /**
     * Context
     */
    private Context context;

    /**
     * Validate the database header when opening a database
     */
    private boolean openHeaderValidation = false;

    /**
     * Validate the database integrity when opening a database
     */
    private boolean openIntegrityValidation = false;

    /**
     * Write ahead logging state for SQLite connections
     */
    private boolean sqliteWriteAheadLogging = false;

    /**
     * Constructor
     */
    public GeoPackageCreator() {

    }

    /**
     * Constructor
     *
     * @param context context
     */
    public GeoPackageCreator(Context context) {
        this.context = context;
    }

    /**
     * Constructor
     *
     * @param openHeaderValidation    validate the database header when opening a database
     * @param openIntegrityValidation validate the database integrity when opening a database
     * @param sqliteWriteAheadLogging write ahead logging state for SQLite connections
     */
    public GeoPackageCreator(boolean openHeaderValidation, boolean openIntegrityValidation, boolean sqliteWriteAheadLogging) {
        this.openHeaderValidation = openHeaderValidation;
        this.openIntegrityValidation = openIntegrityValidation;
        this.sqliteWriteAheadLogging = sqliteWriteAheadLogging;
    }

    /**
     * Constructor
     *
     * @param context                 context
     * @param openHeaderValidation    validate the database header when opening a database
     * @param openIntegrityValidation validate the database integrity when opening a database
     * @param sqliteWriteAheadLogging write ahead logging state for SQLite connections
     */
    public GeoPackageCreator(Context context, boolean openHeaderValidation, boolean openIntegrityValidation, boolean sqliteWriteAheadLogging) {
        this.context = context;
        this.openHeaderValidation = openHeaderValidation;
        this.openIntegrityValidation = openIntegrityValidation;
        this.sqliteWriteAheadLogging = sqliteWriteAheadLogging;
    }

    /**
     * Get the context
     *
     * @return context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Set the context
     *
     * @param context context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Is open database header validation enabled.
     * This causes a small time increase when opening a database to check the header bytes.
     * Off by default.
     *
     * @return true if enabled
     */
    public boolean isOpenHeaderValidation() {
        return openHeaderValidation;
    }

    /**
     * Set the open database header validation setting.
     * This causes a small time increase when opening a database to check the header bytes.
     * Off by default.
     *
     * @param enabled true to enable, false to disable
     */
    public void setOpenHeaderValidation(boolean enabled) {
        this.openHeaderValidation = enabled;
    }

    /**
     * Is open database integrity validation enabled.
     * This causes a noticeable time increase when opening a database to check the database integrity.
     * Off by default.
     *
     * @return true if enabled
     */
    public boolean isOpenIntegrityValidation() {
        return openIntegrityValidation;
    }

    /**
     * Set the open database integrity validation setting.
     * This causes a noticeable time increase when opening a database to check the database integrity.
     * Off by default.
     *
     * @param enabled true to enable, false to disable
     */
    public void setOpenIntegrityValidation(boolean enabled) {
        this.openIntegrityValidation = enabled;
    }

    /**
     * Is the SQLite write ahead logging setting enabled for connections.
     * Off by default.
     *
     * @return write ahead logging state
     */
    public boolean isSqliteWriteAheadLogging() {
        return sqliteWriteAheadLogging;
    }

    /**
     * Set the SQLite write ahead logging setting for connections.
     * Off by default.
     *
     * @param enabled true to enable, false to disable
     */
    public void setSqliteWriteAheadLogging(boolean enabled) {
        this.sqliteWriteAheadLogging = enabled;
    }

    /**
     * Open an external GeoPackage
     *
     * @param path     full file path
     * @param database database name
     * @param writable true to open as writable, false as read only
     * @return GeoPackage
     */
    public GeoPackage openExternal(String path, String database, boolean writable) {

        if (database == null) {
            database = Uri.parse(path).getLastPathSegment();
        }

        database = GeoPackageIOUtils.getFileNameWithoutExtension(database);

        GeoPackageCursorFactory cursorFactory = new GeoPackageCursorFactory();

        SQLiteDatabase sqlite = null;
        if (writable) {
            sqlite = SQLiteDatabaseUtils.openReadWriteDatabaseAttempt(path, cursorFactory);
        }
        if (sqlite == null) {
            sqlite = SQLiteDatabaseUtils.openReadOnlyDatabase(path, cursorFactory);
            writable = false;
        }

        return createGeoPackage(database, path, writable, cursorFactory, sqlite);
    }

    /**
     * Create a GeoPackage for the connection
     *
     * @param database      database name
     * @param path          full file path
     * @param writable      true to open as writable, false as read only
     * @param cursorFactory GeoPackage cursor factory
     * @param sqlite        SQLite database
     * @return GeoPackage
     */
    public GeoPackage createGeoPackage(String database, String path, boolean writable, GeoPackageCursorFactory cursorFactory, SQLiteDatabase sqlite) {

        GeoPackage db = null;

        if (sqliteWriteAheadLogging) {
            sqlite.enableWriteAheadLogging();
        } else {
            sqlite.disableWriteAheadLogging();
        }

        // Validate the database if validation is enabled
        SQLiteDatabaseUtils.validateDatabaseAndCloseOnError(sqlite, openHeaderValidation, openIntegrityValidation);

        GeoPackageConnection connection = new GeoPackageConnection(new GeoPackageDatabase(sqlite, writable, cursorFactory));
        connection.enableForeignKeys();

        db = new GeoPackageImpl(context, database, path, connection, cursorFactory, writable);

        // Validate the GeoPackage has the minimum required tables
        try {
            GeoPackageValidate.validateMinimumTables(db);
        } catch (RuntimeException e) {
            db.close();
            throw e;
        }

        return db;
    }

}
