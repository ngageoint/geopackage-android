package mil.nga.geopackage.db;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * Cursor result implementation
 *
 * @author osbornb
 * @since 3.0.3
 */
public class CursorResult extends CursorWrapper implements Result {

    /**
     * Constructor
     *
     * @param cursor cursor
     */
    public CursorResult(Cursor cursor) {
        super(cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(int index) {
        return ResultUtils.getValue(this, index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(int index, GeoPackageDataType dataType) {
        return ResultUtils.getValue(this, index, dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wasNull() {
        return false;
    }

}
