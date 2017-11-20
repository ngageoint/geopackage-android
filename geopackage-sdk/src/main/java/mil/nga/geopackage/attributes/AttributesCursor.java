package mil.nga.geopackage.attributes;

import android.database.Cursor;

import java.util.List;

import mil.nga.geopackage.user.UserCursor;
import mil.nga.geopackage.user.UserDao;
import mil.nga.geopackage.user.UserInvalidCursor;

/**
 * Attributes Cursor to wrap a database cursor for attributes queries
 *
 * @author osbornb
 * @since 1.3.1
 */
public class AttributesCursor extends
        UserCursor<AttributesColumn, AttributesTable, AttributesRow> {

    /**
     * Constructor
     *
     * @param table  attributes table
     * @param cursor cursor
     */
    public AttributesCursor(AttributesTable table, Cursor cursor) {
        super(table, cursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesRow getRow(int[] columnTypes, Object[] values) {
        return new AttributesRow(getTable(), columnTypes, values);
    }

    /**
     * Enable requery attempt of invalid rows after iterating through original query rows.
     * Only supported for {@link #moveToNext()} and {@link #getRow()} usage.
     *
     * @param dao data access object used to perform requery
     * @since 2.0.0
     */
    public void enableInvalidRequery(AttributesDao dao) {
        super.enableInvalidRequery(dao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserInvalidCursor<AttributesColumn, AttributesTable, AttributesRow, ? extends UserCursor<AttributesColumn, AttributesTable, AttributesRow>, ? extends UserDao<AttributesColumn, AttributesTable, AttributesRow, ? extends UserCursor<AttributesColumn, AttributesTable, AttributesRow>>> createInvalidCursor(UserDao dao, UserCursor cursor, List<Integer> invalidPositions, List<AttributesColumn> blobColumns) {
        return new AttributesInvalidCursor((AttributesDao) dao, (AttributesCursor) cursor, invalidPositions, blobColumns);
    }

}
