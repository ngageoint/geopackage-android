package mil.nga.geopackage.factory;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.CoreSQLUtils;

/**
 * GeoPackage Cursor Factory. Used when connecting to a GeoPackage database.
 * Registers cursor wrappers for GeoPackage data tables to wrap the cursors
 * returned from queries.
 *
 * @author osbornb
 */
class GeoPackageCursorFactory implements CursorFactory {

    /**
     * Mapping between table names and their cursor wrapper
     */
    private final Map<String, GeoPackageCursorWrapper> tableCursors = Collections
            .synchronizedMap(new HashMap<String, GeoPackageCursorWrapper>());

    /**
     * Constructor
     */
    GeoPackageCursorFactory() {
    }

    /**
     * Register a cursor wrapper for the provided table name. Database queries
     * will wrap the returned cursor
     *
     * @param tableName
     * @param cursorWrapper
     */
    public void registerTable(String tableName,
                              GeoPackageCursorWrapper cursorWrapper) {

        // Check for an existing wrapper
        GeoPackageCursorWrapper existing = tableCursors.get(tableName);

        // Add the wrapper
        if (existing == null) {
            tableCursors.put(tableName, cursorWrapper);
            tableCursors.put(CoreSQLUtils.quoteWrap(tableName), cursorWrapper);
        }
        // Verify that the wrapper is not different from the existing
        else if (!existing.getClass().equals(cursorWrapper.getClass())) {
            throw new GeoPackageException("Table '" + tableName
                    + "' was already registered for cursor wrapper '"
                    + existing.getClass().getSimpleName()
                    + "' and can not be registered for '"
                    + cursorWrapper.getClass().getSimpleName() + "'");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
                            String editTable, SQLiteQuery query) {

        // Create a standard cursor
        Cursor cursor = new SQLiteCursor(driver, editTable, query);

        // Check if there is an edit table
        if (editTable != null) {
            // Check if the table has a cursor wrapper
            GeoPackageCursorWrapper cursorWrapper = tableCursors.get(editTable);
            if (cursorWrapper != null) {
                cursor = cursorWrapper.wrapCursor(cursor);
            }
        }

        return cursor;
    }

}
