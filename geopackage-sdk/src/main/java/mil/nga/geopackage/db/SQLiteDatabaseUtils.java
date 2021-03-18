package mil.nga.geopackage.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.sf.util.ByteReader;

/**
 * SQLite Database utilities
 *
 * @author osbornb
 * @since 5.1.0
 */
public class SQLiteDatabaseUtils {

    /**
     * Attempt to open a writable database, logging any errors
     *
     * @param path full file path
     * @return database or null if unable to open as writable
     */
    public static SQLiteDatabase openReadWriteDatabaseAttempt(String path) {
        return openReadWriteDatabaseAttempt(path, null);
    }

    /**
     * Attempt to open a writable database, logging any errors
     *
     * @param path          full file path
     * @param cursorFactory cursor factory
     * @return database or null if unable to open as writable
     */
    public static SQLiteDatabase openReadWriteDatabaseAttempt(String path, CursorFactory cursorFactory) {
        SQLiteDatabase sqlite = null;
        try {
            sqlite = openReadWriteDatabase(path, cursorFactory);
        } catch (Exception e) {
            Log.e(SQLiteDatabaseUtils.class.getSimpleName(), "Failed to open database as writable: " + path, e);
        }
        return sqlite;
    }

    /**
     * Open a writable database
     *
     * @param path full file path
     * @return database
     */
    public static SQLiteDatabase openReadWriteDatabase(String path) {
        return openReadWriteDatabase(path, null);
    }

    /**
     * Open a writable database
     *
     * @param path          full file path
     * @param cursorFactory cursor factory
     * @return database
     */
    public static SQLiteDatabase openReadWriteDatabase(String path, CursorFactory cursorFactory) {
        return openDatabase(path, true, cursorFactory);
    }

    /**
     * Open a readable database
     *
     * @param path full file path
     * @return database
     */
    public static SQLiteDatabase openReadOnlyDatabase(String path) {
        return openReadOnlyDatabase(path, null);
    }

    /**
     * Open a readable database
     *
     * @param path          full file path
     * @param cursorFactory cursor factory
     * @return database
     */
    public static SQLiteDatabase openReadOnlyDatabase(String path, CursorFactory cursorFactory) {
        return openDatabase(path, false, cursorFactory);
    }

    /**
     * Open a database
     *
     * @param path     full file path
     * @param writable open as writable or read only
     * @return database
     */
    public static SQLiteDatabase openDatabase(String path, boolean writable) {
        return openDatabase(path, writable, null);
    }

    /**
     * Open a database
     *
     * @param path          full file path
     * @param writable      open as writable or read only
     * @param cursorFactory cursor factory
     * @return database
     */
    public static SQLiteDatabase openDatabase(String path, boolean writable, CursorFactory cursorFactory) {
        return SQLiteDatabase.openDatabase(path,
                cursorFactory, (writable ? SQLiteDatabase.OPEN_READWRITE : SQLiteDatabase.OPEN_READONLY)
                        | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    /**
     * Validate the database and close when validation fails. Throw an error when not valid.
     *
     * @param sqliteDatabase    database
     * @param validateHeader    validate the header
     * @param validateIntegrity validate the integrity
     */
    public static void validateDatabaseAndCloseOnError(SQLiteDatabase sqliteDatabase, boolean validateHeader, boolean validateIntegrity) {
        validateDatabase(sqliteDatabase, validateHeader, validateIntegrity, false, true);
    }

    /**
     * Validate the database and close it. Throw an error when not valid.
     *
     * @param sqliteDatabase    database
     * @param validateHeader    validate the header
     * @param validateIntegrity validate the integrity
     */
    public static void validateDatabaseAndClose(SQLiteDatabase sqliteDatabase, boolean validateHeader, boolean validateIntegrity) {
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
    public static void validateDatabase(SQLiteDatabase sqliteDatabase, boolean validateHeader, boolean validateIntegrity, boolean close, boolean closeOnError) {
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
    public static void validateDatabaseHeader(SQLiteDatabase sqliteDatabase) {

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
    public static boolean isDatabaseHeaderValid(SQLiteDatabase sqliteDatabase) {

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
            Log.e(SQLiteDatabaseUtils.class.getSimpleName(), "Failed to retrieve database header", e);
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
    public static void validateDatabaseIntegrity(SQLiteDatabase sqliteDatabase) {

        if (!sqliteDatabase.isDatabaseIntegrityOk()) {
            throw new GeoPackageException(
                    "GeoPackage SQLite file integrity failed: " + sqliteDatabase.getPath());
        }
    }

}
