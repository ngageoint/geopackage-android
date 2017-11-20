package mil.nga.geopackage.user;

import java.util.HashMap;
import java.util.Map;

/**
 * User Query parameter types
 *
 * @author osbornb
 * @since 2.0.0
 */
public class UserQuery {

    /**
     * Query parameters
     */
    private final Map<UserQueryParamType, Object> params = new HashMap<>();

    /**
     * Constructor
     */
    public UserQuery() {

    }

    /**
     * Constructor for raw query
     *
     * @param sql           sql statement
     * @param selectionArgs selection arguments
     */
    public UserQuery(String sql, String[] selectionArgs) {
        if (sql != null) {
            set(UserQueryParamType.SQL, sql);
        }
        if (selectionArgs != null) {
            set(UserQueryParamType.SELECTION_ARGS, selectionArgs);
        }
    }

    /**
     * Constructor
     *
     * @param table         table name
     * @param columns       column names
     * @param selection     selection
     * @param selectionArgs selection args
     * @param groupBy       group by
     * @param having        having
     * @param orderBy       order by
     */
    public UserQuery(String table, String[] columns, String selection,
                     String[] selectionArgs, String groupBy, String having,
                     String orderBy) {
        this(table, columns, null, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    /**
     * Constructor
     *
     * @param table         table name
     * @param columns       column names
     * @param columnsAs     columns as values
     * @param selection     selection
     * @param selectionArgs selection args
     * @param groupBy       group by
     * @param having        having
     * @param orderBy       order by
     */
    public UserQuery(String table, String[] columns, String[] columnsAs, String selection,
                     String[] selectionArgs, String groupBy, String having,
                     String orderBy) {
        this(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    /**
     * Constructor
     *
     * @param table         table name
     * @param columns       column names
     * @param selection     selection
     * @param selectionArgs selection args
     * @param groupBy       group by
     * @param having        having
     * @param orderBy       order by
     * @param limit         limit
     */
    public UserQuery(String table, String[] columns, String selection,
                     String[] selectionArgs, String groupBy, String having,
                     String orderBy, String limit) {
        this(table, columns, null, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * Constructor
     *
     * @param table         table name
     * @param columns       column names
     * @param columnsAs     columns as values
     * @param selection     selection
     * @param selectionArgs selection args
     * @param groupBy       group by
     * @param having        having
     * @param orderBy       order by
     * @param limit         limit
     */
    public UserQuery(String table, String[] columns, String[] columnsAs, String selection,
                     String[] selectionArgs, String groupBy, String having,
                     String orderBy, String limit) {

        if (table != null) {
            set(UserQueryParamType.TABLE, table);
        }
        if (columns != null) {
            set(UserQueryParamType.COLUMNS, columns);
        }
        if (columnsAs != null) {
            set(UserQueryParamType.COLUMNS_AS, columnsAs);
        }
        if (selection != null) {
            set(UserQueryParamType.SELECTION, selection);
        }
        if (selectionArgs != null) {
            set(UserQueryParamType.SELECTION_ARGS, selectionArgs);
        }
        if (groupBy != null) {
            set(UserQueryParamType.GROUP_BY, groupBy);
        }
        if (having != null) {
            set(UserQueryParamType.HAVING, having);
        }
        if (orderBy != null) {
            set(UserQueryParamType.ORDER_BY, orderBy);
        }
        if (limit != null) {
            set(UserQueryParamType.LIMIT, limit);
        }

    }

    /**
     * Set the value for the param type
     *
     * @param type  param type
     * @param value param value
     */
    public void set(UserQueryParamType type, Object value) {
        params.put(type, value);
    }

    /**
     * Get the param value
     *
     * @param type param type
     * @return param value
     */
    public Object get(UserQueryParamType type) {
        return params.get(type);
    }

    /**
     * Check if there is a param value
     *
     * @param type param type
     * @return true if param value exists
     */
    public boolean has(UserQueryParamType type) {
        return get(type) != null;
    }

    /**
     * Get the raw SQL value
     *
     * @return SQL value
     */
    public String getSql() {
        return (String) get(UserQueryParamType.SQL);
    }

    /**
     * Get the selection args
     *
     * @return selection args
     */
    public String[] getSelectionArgs() {
        return (String[]) get(UserQueryParamType.SELECTION_ARGS);
    }

    /**
     * Get the table name
     *
     * @return table name
     */
    public String getTable() {
        return (String) get(UserQueryParamType.TABLE);
    }

    /**
     * Get the column names
     *
     * @return column names
     */
    public String[] getColumns() {
        return (String[]) get(UserQueryParamType.COLUMNS);
    }

    /**
     * Get the columns as values
     *
     * @return columns as values
     */
    public String[] getColumnsAs() {
        return (String[]) get(UserQueryParamType.COLUMNS_AS);
    }

    /**
     * Get the selection value
     *
     * @return selection value
     */
    public String getSelection() {
        return (String) get(UserQueryParamType.SELECTION);
    }

    /**
     * Get the group by value
     *
     * @return group by value
     */
    public String getGroupBy() {
        return (String) get(UserQueryParamType.GROUP_BY);
    }

    /**
     * Get the having value
     *
     * @return having value
     */
    public String getHaving() {
        return (String) get(UserQueryParamType.HAVING);
    }

    /**
     * Get the order by value
     *
     * @return order by value
     */
    public String getOrderBy() {
        return (String) get(UserQueryParamType.ORDER_BY);
    }

    /**
     * Get the limit value
     *
     * @return limit value
     */
    public String getLimit() {
        return (String) get(UserQueryParamType.LIMIT);
    }

}
