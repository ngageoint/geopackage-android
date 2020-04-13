package mil.nga.geopackage.factory;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mil.nga.geopackage.db.CoreSQLUtils;

/**
 * GeoPackage Cursor Factory. Used when connecting to a GeoPackage database.
 * Registers cursor wrappers for GeoPackage data tables to wrap the cursors
 * returned from queries.
 *
 * @author osbornb
 */
public class GeoPackageCursorFactory implements CursorFactory {

    /**
     * Log queries flag
     */
    private boolean debugLogQueries = false;

    /**
     * Mapping between table names and their cursor wrapper
     */
    private final Map<String, GeoPackageCursorWrapper> tableCursors = Collections
            .synchronizedMap(new HashMap<String, GeoPackageCursorWrapper>());

    /**
     * Bindings Cursor Factory
     */
    private org.sqlite.database.sqlite.SQLiteDatabase.CursorFactory bindingsCursorFactory;

    /**
     * Constructor
     */
    GeoPackageCursorFactory() {
    }

    /**
     * Is debug log queries enabled
     *
     * @return true if queries are logged at the debug level
     * @since 3.4.0
     */
    public boolean isDebugLogQueries() {
        return debugLogQueries;
    }

    /**
     * Set the debug log queries flag
     *
     * @param debugLogQueries true to debug log queries
     * @since 3.4.0
     */
    public void setDebugLogQueries(boolean debugLogQueries) {
        this.debugLogQueries = debugLogQueries;
    }

    /**
     * Register a cursor wrapper for the provided table name. Database queries
     * will wrap the returned cursor
     *
     * @param tableName     table name
     * @param cursorWrapper cursor wrapper
     */
    public void registerTable(String tableName,
                              GeoPackageCursorWrapper cursorWrapper) {

        // Add the wrapper
        tableCursors.put(tableName, cursorWrapper);
        String quotedTableName = CoreSQLUtils.quoteWrap(tableName);
        tableCursors.put(quotedTableName, cursorWrapper);

        // The Android android.database.sqlite.SQLiteDatabase findEditTable method
        // finds the new cursor edit table name based upon the first space or comma.
        // Fix (hopefully temporary) to wrap with the expected cursor type
        int spacePosition = tableName.indexOf(' ');
        if (spacePosition > 0) {
            tableCursors.put(tableName.substring(0, spacePosition), cursorWrapper);
            tableCursors.put(quotedTableName.substring(0, quotedTableName.indexOf(' ')), cursorWrapper);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
                            String editTable, SQLiteQuery query) {

        if (debugLogQueries) {
            Log.d(GeoPackageCursorFactory.class.getSimpleName(), query.toString());
        }

        // Create a standard cursor
        Cursor cursor = new SQLiteCursor(driver, editTable, query);

        // Wrap the cursor
        Cursor wrappedCursor = wrapCursor(cursor, editTable);

        return wrappedCursor;
    }

    /**
     * Wrap the cursor
     *
     * @param cursor    cursor
     * @param editTable edit table
     * @return cursor
     * @since 3.4.0
     */
    public Cursor wrapCursor(Cursor cursor, String editTable) {

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

    /**
     * Get the SQLite Android Bindings cursor factory
     *
     * @return bindings cursor factory
     * @since 3.4.0
     */
    public org.sqlite.database.sqlite.SQLiteDatabase.CursorFactory getBindingsCursorFactory() {
        if (bindingsCursorFactory == null) {
            bindingsCursorFactory = new org.sqlite.database.sqlite.SQLiteDatabase.CursorFactory() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Cursor newCursor(org.sqlite.database.sqlite.SQLiteDatabase db, org.sqlite.database.sqlite.SQLiteCursorDriver driver, String editTable, org.sqlite.database.sqlite.SQLiteQuery query) {

                    if (debugLogQueries) {
                        Log.d(GeoPackageCursorFactory.class.getSimpleName(), query.toString());
                    }

                    // Create a standard cursor
                    Cursor cursor = new org.sqlite.database.sqlite.SQLiteCursor(driver, editTable, query);

                    // Wrap the cursor
                    Cursor wrappedCursor = wrapCursor(cursor, editTable);

                    return wrappedCursor;
                }

            };
        }
        return bindingsCursorFactory;
    }

}
