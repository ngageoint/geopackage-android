package mil.nga.geopackage.factory;

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
	 * @param cursor
	 * @return wrapped cursor
	 */
	public Cursor wrapCursor(Cursor cursor);

}
