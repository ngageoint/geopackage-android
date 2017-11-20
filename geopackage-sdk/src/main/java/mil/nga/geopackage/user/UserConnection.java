package mil.nga.geopackage.user;

import android.database.Cursor;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageDatabase;

/**
 * GeoPackage Connection used to define common functionality within different
 * connection types
 *
 * @param <TColumn>
 * @param <TTable>
 * @param <TRow>
 * @param <TResult>
 * @author osbornb
 */
public abstract class UserConnection<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserCursor<TColumn, TTable, TRow>>
        extends UserCoreConnection<TColumn, TTable, TRow, TResult> {

    /**
     * Database connection
     */
    protected final GeoPackageDatabase database;

    /**
     * Constructor
     *
     * @param database
     */
    protected UserConnection(GeoPackageConnection database) {
        this.database = database.getDb();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult rawQuery(String sql, String[] selectionArgs) {
        UserQuery query = new UserQuery(sql, selectionArgs);
        TResult result = query(query);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy) {
        UserQuery query = new UserQuery(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        TResult result = query(query);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String[] columnsAs, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy) {
        UserQuery query = new UserQuery(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy);
        TResult result = query(query);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy, String limit) {
        UserQuery query = new UserQuery(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        TResult result = query(query);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String[] columnsAs, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy, String limit) {
        UserQuery query = new UserQuery(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy, limit);
        TResult result = query(query);
        return result;
    }

    /**
     * Query using the query from a previous query result
     *
     * @param previousResult previous result
     * @return result
     * @since 2.0.0
     */
    public TResult query(TResult previousResult) {
        UserQuery query = previousResult.getQuery();
        TResult result = query(query);
        return result;
    }

    /**
     * Query using the user query arguments
     *
     * @param query user query
     * @return result
     * @since 2.0.0
     */
    public TResult query(UserQuery query) {
        Cursor cursor = null;

        String[] selectionArgs = query.getSelectionArgs();

        String sql = query.getSql();
        if (sql != null) {
            cursor = database.rawQuery(sql, selectionArgs);
        } else {

            String table = query.getTable();
            String[] columns = query.getColumns();
            String selection = query.getSelection();
            String groupBy = query.getGroupBy();
            String having = query.getHaving();
            String orderBy = query.getOrderBy();

            String[] columnsAs = query.getColumnsAs();
            String limit = query.getLimit();

            if (columnsAs != null && limit != null) {
                cursor = database.query(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy, limit);
            } else if (columnsAs != null) {
                cursor = database.query(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy);
            } else if (limit != null) {
                cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            } else {
                cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            }

        }

        TResult result = handleCursor(cursor, query);

        return result;
    }

    /**
     * Convert the cursor to the result type cursor
     *
     * @param cursor cursor
     * @param query  user query
     * @return result cursor
     */
    private TResult handleCursor(Cursor cursor, UserQuery query) {
        TResult result = convertCursor(cursor);
        result.setQuery(query);
        return result;
    }

    /**
     * Convert the cursor to the result type cursor
     *
     * @param cursor cursor
     * @return result cursor
     * @since 2.0.0
     */
    protected TResult convertCursor(Cursor cursor) {
        return (TResult) cursor;
    }

}
