package mil.nga.geopackage.extension.related.simple;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * User Simple Attributes DAO for reading user simple attributes data tables
 *
 * @author osbornb
 * @since 3.0.1
 */
public class SimpleAttributesDao extends UserCustomDao {

    /**
     * Constructor
     *
     * @param dao user custom data access object
     */
    public SimpleAttributesDao(UserCustomDao dao) {
        super(dao, new SimpleAttributesTable(dao.getTable()));
    }

    /**
     * Constructor
     *
     * @param dao                   user custom data access object
     * @param simpleAttributesTable simple attributes table
     */
    protected SimpleAttributesDao(UserCustomDao dao,
                                  SimpleAttributesTable simpleAttributesTable) {
        super(dao, simpleAttributesTable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleAttributesTable getTable() {
        return (SimpleAttributesTable) super.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleAttributesRow newRow() {
        return new SimpleAttributesRow(getTable());
    }

    /**
     * Get the simple attributes row from the current cursor location
     *
     * @param cursor cursor
     * @return simple attributes row
     */
    public SimpleAttributesRow getRow(UserCustomCursor cursor) {
        return getRow(cursor.getRow());
    }

    /**
     * Get a simple attributes row from the user custom row
     *
     * @param row custom row
     * @return simple attributes row
     */
    public SimpleAttributesRow getRow(UserCustomRow row) {
        return new SimpleAttributesRow(row);
    }

    /**
     * Get the simple attributes rows that exist with the provided ids
     *
     * @param ids list of ids
     * @return simple attributes rows
     */
    public List<SimpleAttributesRow> getRows(List<Long> ids) {
        List<SimpleAttributesRow> simpleAttributesRows = new ArrayList<>();
        for (long id : ids) {
            UserCustomRow userCustomRow = queryForIdRow(id);
            if (userCustomRow != null) {
                simpleAttributesRows.add(getRow(userCustomRow));
            }
        }
        return simpleAttributesRows;
    }

}
