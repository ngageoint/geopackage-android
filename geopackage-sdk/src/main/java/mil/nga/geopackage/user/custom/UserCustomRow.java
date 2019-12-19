package mil.nga.geopackage.user.custom;

import mil.nga.geopackage.user.UserRow;

/**
 * User Custom Row containing the values from a single cursor row
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomRow extends UserRow<UserCustomColumn, UserCustomTable> {

    /**
     * Constructor
     *
     * @param table       user custom table
     * @param columns     columns
     * @param columnTypes column types
     * @param values      values
     * @since 3.5.0
     */
    public UserCustomRow(UserCustomTable table, UserCustomColumns columns, int[] columnTypes,
                         Object[] values) {
        super(table, columns, columnTypes, values);
    }

    /**
     * Constructor to create an empty row
     *
     * @param table user custom table
     */
    public UserCustomRow(UserCustomTable table) {
        super(table);
    }

    /**
     * Copy Constructor
     *
     * @param userCustomRow user custom row to copy
     */
    public UserCustomRow(UserCustomRow userCustomRow) {
        super(userCustomRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomColumns getColumns() {
        return (UserCustomColumns) super.getColumns();
    }

    /**
     * Copy the row
     *
     * @return row copy
     */
    public UserCustomRow copy() {
        return new UserCustomRow(this);
    }

}
