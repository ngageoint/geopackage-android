package mil.nga.geopackage.user;

/**
 * User Query parameter types
 *
 * @author osbornb
 * @since 2.0.0
 */
public enum UserQueryParamType {

    /**
     * Raw SQL string
     */
    SQL,

    /**
     * Table name
     */
    TABLE,

    /**
     * Column names
     */
    COLUMNS,

    /**
     * Columns as values
     */
    COLUMNS_AS,

    /**
     * Selection
     */
    SELECTION,

    /**
     * Selection arguments
     */
    SELECTION_ARGS,

    /**
     * Group by
     */
    GROUP_BY,

    /**
     * Having
     */
    HAVING,

    /**
     * Order by
     */
    ORDER_BY,

    /**
     * Limit
     */
    LIMIT

}
