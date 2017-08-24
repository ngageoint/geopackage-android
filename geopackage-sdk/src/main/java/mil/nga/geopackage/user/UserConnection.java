package mil.nga.geopackage.user;

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
    private final GeoPackageDatabase database;

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
        return (TResult) database.rawQuery(sql, selectionArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy) {
        return (TResult) database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String[] columnsAs, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy) {
        return (TResult) database.query(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy, String limit) {
        return (TResult) database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TResult query(String table, String[] columns, String[] columnsAs, String selection,
                         String[] selectionArgs, String groupBy, String having,
                         String orderBy, String limit) {
        return (TResult) database.query(table, columns, columnsAs, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

}
