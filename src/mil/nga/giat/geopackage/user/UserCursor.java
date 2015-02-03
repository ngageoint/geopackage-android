package mil.nga.giat.geopackage.user;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * Abstract User Cursor
 * 
 * @param <TRow>
 * 
 * @author osbornb
 */
public abstract class UserCursor<TRow extends UserRow<?, ?>> extends
		CursorWrapper {

	/**
	 * Constructor
	 * 
	 * @param dao
	 */
	protected UserCursor(Cursor cursor) {
		super(cursor);
	}

	/**
	 * Get the row at the current cursor position
	 * 
	 * @return
	 */
	public abstract TRow getRow();

}
