package mil.nga.geopackage.db;

import android.database.Cursor;

/**
 * Interface for database cursor wrapping implementations. Used to wrap the
 * cursors of queries to GeoPackage data tables.
 *
 * @author osbornb
 */
public interface GeoPackageCursorWrapper {

    /**
     * Wrap the cursor
     *
     * @param cursor cursor
     * @return wrapped cursor
     */
    public Cursor wrapCursor(Cursor cursor);

}
