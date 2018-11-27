package mil.nga.geopackage.extension.related;

import java.util.List;

import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * User Mapping DAO for reading user mapping data tables
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserMappingDao extends UserCustomDao {

    /**
     * Constructor
     *
     * @param dao user custom data access object
     */
    public UserMappingDao(UserCustomDao dao) {
        super(dao, new UserMappingTable(dao.getTable()));
    }

    /**
     * Constructor
     *
     * @param dao              user custom data access object
     * @param userMappingTable user mapping table
     */
    protected UserMappingDao(UserCustomDao dao,
                             UserMappingTable userMappingTable) {
        super(dao, userMappingTable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserMappingTable getTable() {
        return (UserMappingTable) super.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserMappingRow newRow() {
        return new UserMappingRow(getTable());
    }

    /**
     * Get the user mapping row from the current cursor location
     *
     * @param cursor cursor
     * @return user mapping row
     */
    public UserMappingRow getRow(UserCustomCursor cursor) {
        return getRow(cursor.getRow());
    }

    /**
     * Get a user mapping row from the user custom row
     *
     * @param row custom row
     * @return user mapping row
     */
    public UserMappingRow getRow(UserCustomRow row) {
        return new UserMappingRow(row);
    }

    /**
     * Query by base id
     *
     * @param userMappingRow user mapping row
     * @return cursor
     */
    public UserCustomCursor queryByBaseId(UserMappingRow userMappingRow) {
        return queryByBaseId(userMappingRow.getBaseId());
    }

    /**
     * Query by base id
     *
     * @param baseId base id
     * @return cursor
     */
    public UserCustomCursor queryByBaseId(long baseId) {
        return queryForEq(UserMappingTable.COLUMN_BASE_ID, baseId);
    }

    /**
     * Count by base id
     *
     * @param userMappingRow user mapping row
     * @return count
     */
    public int countByBaseId(UserMappingRow userMappingRow) {
        return countByBaseId(userMappingRow.getBaseId());
    }

    /**
     * Count by base id
     *
     * @param baseId base id
     * @return count
     */
    public int countByBaseId(long baseId) {
        return count(queryByBaseId(baseId));
    }

    /**
     * Query by related id
     *
     * @param userMappingRow user mapping row
     * @return cursor
     */
    public UserCustomCursor queryByRelatedId(UserMappingRow userMappingRow) {
        return queryByRelatedId(userMappingRow.getRelatedId());
    }

    /**
     * Query by related id
     *
     * @param relatedId related id
     * @return cursor
     */
    public UserCustomCursor queryByRelatedId(long relatedId) {
        return queryForEq(UserMappingTable.COLUMN_RELATED_ID, relatedId);
    }

    /**
     * Count by related id
     *
     * @param userMappingRow user mapping row
     * @return count
     */
    public int countByRelatedId(UserMappingRow userMappingRow) {
        return countByRelatedId(userMappingRow.getRelatedId());
    }

    /**
     * Count by related id
     *
     * @param relatedId related id
     * @return count
     */
    public int countByRelatedId(long relatedId) {
        return count(queryByRelatedId(relatedId));
    }

    /**
     * Query by both base id and related id
     *
     * @param userMappingRow user mapping row
     * @return cursor
     */
    public UserCustomCursor queryByIds(UserMappingRow userMappingRow) {
        return queryByIds(userMappingRow.getBaseId(),
                userMappingRow.getRelatedId());
    }

    /**
     * Query by both base id and related id
     *
     * @param baseId    base id
     * @param relatedId related id
     * @return cursor
     */
    public UserCustomCursor queryByIds(long baseId, long relatedId) {
        return query(buildWhereIds(baseId, relatedId),
                buildWhereIdsArgs(baseId, relatedId));
    }

    /**
     * Get the unique base ids
     *
     * @return list of unique base ids
     * @since 3.1.1
     */
    public List<Long> uniqueBaseIds() {
        return querySingleColumnTypedResults(
                "SELECT DISTINCT " + CoreSQLUtils.quoteWrap(UserMappingTable.COLUMN_BASE_ID) + " FROM "
                        + CoreSQLUtils.quoteWrap(getTableName()), null);
    }

    /**
     * Get the unique related ids
     *
     * @return list of unique related ids
     * @since 3.1.1
     */
    public List<Long> uniqueRelatedIds() {
        return querySingleColumnTypedResults(
                "SELECT DISTINCT " + CoreSQLUtils.quoteWrap(UserMappingTable.COLUMN_RELATED_ID) + " FROM "
                        + CoreSQLUtils.quoteWrap(getTableName()), null);
    }

    /**
     * Count by both base id and related id
     *
     * @param userMappingRow user mapping row
     * @return count
     */
    public int countByIds(UserMappingRow userMappingRow) {
        return countByIds(userMappingRow.getBaseId(),
                userMappingRow.getRelatedId());
    }

    /**
     * Count by both base id and related id
     *
     * @param baseId    base id
     * @param relatedId related id
     * @return count
     */
    public int countByIds(long baseId, long relatedId) {
        return count(queryByIds(baseId, relatedId));
    }

    /**
     * Delete user mappings by base id
     *
     * @param userMappingRow user mapping row
     * @return rows deleted
     */
    public int deleteByBaseId(UserMappingRow userMappingRow) {
        return deleteByBaseId(userMappingRow.getBaseId());
    }

    /**
     * Delete user mappings by base id
     *
     * @param baseId base id
     * @return rows deleted
     */
    public int deleteByBaseId(long baseId) {

        StringBuilder where = new StringBuilder();
        where.append(buildWhere(UserMappingTable.COLUMN_BASE_ID, baseId));

        String[] whereArgs = buildWhereArgs(new Object[]{baseId});

        int deleted = delete(where.toString(), whereArgs);

        return deleted;
    }

    /**
     * Delete user mappings by related id
     *
     * @param userMappingRow user mapping row
     * @return rows deleted
     */
    public int deleteByRelatedId(UserMappingRow userMappingRow) {
        return deleteByRelatedId(userMappingRow.getRelatedId());
    }

    /**
     * Delete user mappings by related id
     *
     * @param relatedId related id
     * @return rows deleted
     */
    public int deleteByRelatedId(long relatedId) {

        StringBuilder where = new StringBuilder();
        where.append(buildWhere(UserMappingTable.COLUMN_RELATED_ID, relatedId));

        String[] whereArgs = buildWhereArgs(new Object[]{relatedId});

        int deleted = delete(where.toString(), whereArgs);

        return deleted;
    }

    /**
     * Delete user mappings by both base id and related id
     *
     * @param userMappingRow user mapping row
     * @return rows deleted
     */
    public int deleteByIds(UserMappingRow userMappingRow) {
        return deleteByIds(userMappingRow.getBaseId(),
                userMappingRow.getRelatedId());
    }

    /**
     * Delete user mappings by both base id and related id
     *
     * @param baseId    base id
     * @param relatedId related id
     * @return rows deleted
     */
    public int deleteByIds(long baseId, long relatedId) {
        return delete(buildWhereIds(baseId, relatedId),
                buildWhereIdsArgs(baseId, relatedId));
    }

    /**
     * Build the where ids clause
     *
     * @param baseId    base id
     * @param relatedId related id
     * @return where clause
     */
    private String buildWhereIds(long baseId, long relatedId) {

        StringBuilder where = new StringBuilder();
        where.append(buildWhere(UserMappingTable.COLUMN_BASE_ID, baseId));
        where.append(" AND ");
        where.append(buildWhere(UserMappingTable.COLUMN_RELATED_ID, relatedId));

        return where.toString();
    }

    /**
     * Build the where ids clause arguments
     *
     * @param baseId    base id
     * @param relatedId related id
     * @return where args
     */
    private String[] buildWhereIdsArgs(long baseId, long relatedId) {
        return buildWhereArgs(new Object[]{baseId, relatedId});
    }

}
