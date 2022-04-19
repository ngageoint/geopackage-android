package mil.nga.geopackage.extension.related;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.extension.related.media.MediaDao;
import mil.nga.geopackage.extension.related.media.MediaTable;
import mil.nga.geopackage.extension.related.simple.SimpleAttributesDao;
import mil.nga.geopackage.extension.related.simple.SimpleAttributesTable;
import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.geopackage.user.custom.UserCustomDao;

/**
 * Related Tables extension
 * <p>
 * <a href="http://docs.opengeospatial.org/is/18-000/18-000.html">http://docs.opengeospatial.org/is/18-000/18-000.html</a>
 *
 * @author osbornb
 * @since 3.0.1
 */
public class RelatedTablesExtension extends RelatedTablesCoreExtension {

    /**
     * GeoPackage connection
     */
    private GeoPackageConnection connection;

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     */
    public RelatedTablesExtension(GeoPackage geoPackage) {
        super(geoPackage);
        connection = geoPackage.getConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoPackage getGeoPackage() {
        return (GeoPackage) super.getGeoPackage();
    }

    /**
     * Get a User Custom DAO from a table name
     *
     * @param tableName table name
     * @return user custom dao
     */
    public UserCustomDao getUserDao(String tableName) {
        return UserCustomDao.readTable(getGeoPackage(),
                tableName);
    }

    /**
     * Get a User Mapping DAO from an extended relation
     *
     * @param extendedRelation extended relation
     * @return user mapping dao
     */
    public UserMappingDao getMappingDao(ExtendedRelation extendedRelation) {
        return getMappingDao(extendedRelation.getMappingTableName());
    }

    /**
     * Get a User Mapping DAO from a table name
     *
     * @param tableName mapping table name
     * @return user mapping dao
     */
    public UserMappingDao getMappingDao(String tableName) {
        UserMappingDao userMappingDao = new UserMappingDao(getUserDao(tableName));
        userMappingDao.registerCursorWrapper(getGeoPackage());
        return userMappingDao;
    }

    /**
     * Get a related media table DAO
     *
     * @param mediaTable media table
     * @return media DAO
     */
    public MediaDao getMediaDao(MediaTable mediaTable) {
        return getMediaDao(mediaTable.getTableName());
    }

    /**
     * Get a related media table DAO
     *
     * @param extendedRelation extended relation
     * @return media DAO
     */
    public MediaDao getMediaDao(ExtendedRelation extendedRelation) {
        return getMediaDao(extendedRelation.getRelatedTableName());
    }

    /**
     * Get a related media table DAO
     *
     * @param tableName media table name
     * @return media DAO
     */
    public MediaDao getMediaDao(String tableName) {
        MediaDao mediaDao = new MediaDao(getUserDao(tableName));
        mediaDao.registerCursorWrapper(getGeoPackage());
        setContents(mediaDao.getTable());
        return mediaDao;
    }

    /**
     * Get a related simple attributes table DAO
     *
     * @param simpleAttributesTable simple attributes table
     * @return simple attributes DAO
     */
    public SimpleAttributesDao getSimpleAttributesDao(
            SimpleAttributesTable simpleAttributesTable) {
        return getSimpleAttributesDao(simpleAttributesTable.getTableName());
    }

    /**
     * Get a related simple attributes table DAO
     *
     * @param extendedRelation extended relation
     * @return simple attributes DAO
     */
    public SimpleAttributesDao getSimpleAttributesDao(
            ExtendedRelation extendedRelation) {
        return getSimpleAttributesDao(extendedRelation.getRelatedTableName());
    }

    /**
     * Get a related simple attributes table DAO
     *
     * @param tableName simple attributes table name
     * @return simple attributes DAO
     */
    public SimpleAttributesDao getSimpleAttributesDao(String tableName) {
        SimpleAttributesDao simpleAttributesDao = new SimpleAttributesDao(
                getUserDao(tableName));
        simpleAttributesDao.registerCursorWrapper(getGeoPackage());
        setContents(simpleAttributesDao.getTable());
        return simpleAttributesDao;
    }

    /**
     * Get the related id mappings for the base id
     *
     * @param extendedRelation extended relation
     * @param baseId           base id
     * @return IDs representing the matching related IDs
     */
    public List<Long> getMappingsForBase(ExtendedRelation extendedRelation,
                                         long baseId) {
        return getMappingsForBase(extendedRelation.getMappingTableName(),
                baseId);
    }

    /**
     * Get the related id mappings for the base id
     *
     * @param tableName mapping table name
     * @param baseId    base id
     * @return IDs representing the matching related IDs
     */
    public List<Long> getMappingsForBase(String tableName, long baseId) {

        List<Long> relatedIds = new ArrayList<>();

        UserMappingDao userMappingDao = getMappingDao(tableName);
        UserCustomCursor cursor = userMappingDao.queryByBaseId(baseId);
        try {
            while (cursor.moveToNext()) {
                UserMappingRow row = userMappingDao.getRow(cursor);
                relatedIds.add(row.getRelatedId());
            }
        } finally {
            cursor.close();
        }

        return relatedIds;
    }

    /**
     * Get the base id mappings for the related id
     *
     * @param extendedRelation extended relation
     * @param relatedId        related id
     * @return IDs representing the matching base IDs
     */
    public List<Long> getMappingsForRelated(ExtendedRelation extendedRelation,
                                            long relatedId) {
        return getMappingsForRelated(extendedRelation.getMappingTableName(),
                relatedId);
    }

    /**
     * Get the base id mappings for the related id
     *
     * @param tableName mapping table name
     * @param relatedId related id
     * @return IDs representing the matching base IDs
     */
    public List<Long> getMappingsForRelated(String tableName, long relatedId) {

        List<Long> baseIds = new ArrayList<>();

        UserMappingDao userMappingDao = getMappingDao(tableName);
        UserCustomCursor cursor = userMappingDao
                .queryByRelatedId(relatedId);
        try {
            while (cursor.moveToNext()) {
                UserMappingRow row = userMappingDao.getRow(cursor);
                baseIds.add(row.getBaseId());
            }
        } finally {
            cursor.close();
        }

        return baseIds;
    }

    /**
     * Determine if the base id and related id mapping exists
     *
     * @param tableName mapping table name
     * @param baseId    base id
     * @param relatedId related id
     * @return true if mapping exists
     * @since 3.2.0
     */
    public boolean hasMapping(String tableName, long baseId, long relatedId) {
        boolean has = false;
        UserMappingDao userMappingDao = getMappingDao(tableName);
        UserCustomCursor cursor = userMappingDao.queryByIds(baseId,
                relatedId);
        try {
            has = cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
        return has;
    }

    /**
     * Count the number of mappings to the base table and id
     *
     * @param baseTable base table name
     * @param baseId    base id
     * @return mappings count
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int countMappingsToBase(String baseTable, long baseId)
            throws SQLException {
        return countMappingsToBase(getBaseTableRelations(baseTable), baseId);
    }

    /**
     * Determine if a mapping to the base table and id exists
     *
     * @param baseTable base table name
     * @param baseId    base id
     * @return true if mapping exists
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public boolean hasMappingToBase(String baseTable, long baseId)
            throws SQLException {
        return countMappingsToBase(baseTable, baseId) > 0;
    }

    /**
     * Count the number of mappings in the extended relations to the base id
     *
     * @param extendedRelations extended relations
     * @param baseId            base id
     * @return mappings count
     * @since 6.3.0
     */
    public int countMappingsToBase(
            Collection<ExtendedRelation> extendedRelations, long baseId) {
        int count = 0;
        if (extendedRelations != null) {
            for (ExtendedRelation extendedRelation : extendedRelations) {
                count += countMappingsToBase(extendedRelation, baseId);
            }
        }
        return count;
    }

    /**
     * Determine if a mapping in the extended relations to the base id exists
     *
     * @param extendedRelations extended relations
     * @param baseId            base id
     * @return true if mapping exists
     * @since 6.3.0
     */
    public boolean hasMappingToBase(
            Collection<ExtendedRelation> extendedRelations, long baseId) {
        return countMappingsToBase(extendedRelations, baseId) > 0;
    }

    /**
     * Count the number of mappings in the extended relation to the base id
     *
     * @param extendedRelation extended relation
     * @param baseId           base id
     * @return mappings count
     * @since 6.3.0
     */
    public int countMappingsToBase(ExtendedRelation extendedRelation,
                                   long baseId) {
        return getMappingDao(extendedRelation).countByBaseId(baseId);
    }

    /**
     * Determine if a mapping in the extended relation to the base id exists
     *
     * @param extendedRelation extended relation
     * @param baseId           base id
     * @return true if mapping exists
     * @since 6.3.0
     */
    public boolean hasMappingToBase(ExtendedRelation extendedRelation,
                                    long baseId) {
        return countMappingsToBase(extendedRelation, baseId) > 0;
    }

    /**
     * Delete mappings to the base table and id
     *
     * @param baseTable base table name
     * @param baseId    base id
     * @return rows deleted
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int deleteMappingsToBase(String baseTable, long baseId)
            throws SQLException {
        return deleteMappingsToBase(getBaseTableRelations(baseTable), baseId);
    }

    /**
     * Delete mappings in the extended relations to the base id
     *
     * @param extendedRelations extended relations
     * @param baseId            base id
     * @return rows deleted
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int deleteMappingsToBase(
            Collection<ExtendedRelation> extendedRelations, long baseId)
            throws SQLException {
        int count = 0;
        if (extendedRelations != null) {
            for (ExtendedRelation extendedRelation : extendedRelations) {
                count += deleteMappingsToBase(extendedRelation, baseId);
            }
        }
        return count;
    }

    /**
     * Delete mappings in the extended relation to the base id
     *
     * @param extendedRelation extended relation
     * @param baseId           base id
     * @return rows deleted
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int deleteMappingsToBase(ExtendedRelation extendedRelation,
                                    long baseId) throws SQLException {
        return getMappingDao(extendedRelation).deleteByBaseId(baseId);
    }

    /**
     * Count the number of mappings to the related table and id
     *
     * @param relatedTable related table name
     * @param relatedId    related id
     * @return mappings count
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int countMappingsToRelated(String relatedTable, long relatedId)
            throws SQLException {
        return countMappingsToRelated(getRelatedTableRelations(relatedTable),
                relatedId);
    }

    /**
     * Determine if a mapping to the related table and id exists
     *
     * @param relatedTable related table name
     * @param relatedId    related id
     * @return true if mapping exists
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public boolean hasMappingToRelated(String relatedTable, long relatedId)
            throws SQLException {
        return countMappingsToRelated(relatedTable, relatedId) > 0;
    }

    /**
     * Count the number of mappings in the extended relations to the related id
     *
     * @param extendedRelations extended relations
     * @param relatedId         related id
     * @return mappings count
     * @since 6.3.0
     */
    public int countMappingsToRelated(
            Collection<ExtendedRelation> extendedRelations, long relatedId) {
        int count = 0;
        if (extendedRelations != null) {
            for (ExtendedRelation extendedRelation : extendedRelations) {
                count += countMappingsToRelated(extendedRelation, relatedId);
            }
        }
        return count;
    }

    /**
     * Determine if a mapping in the extended relations to the related id exists
     *
     * @param extendedRelations extended relations
     * @param relatedId         related id
     * @return true if mapping exists
     * @since 6.3.0
     */
    public boolean hasMappingToRelated(
            Collection<ExtendedRelation> extendedRelations, long relatedId) {
        return countMappingsToRelated(extendedRelations, relatedId) > 0;
    }

    /**
     * Count the number of mappings in the extended relation to the related id
     *
     * @param extendedRelation extended relation
     * @param relatedId        related id
     * @return mappings count
     * @since 6.3.0
     */
    public int countMappingsToRelated(ExtendedRelation extendedRelation,
                                      long relatedId) {
        return getMappingDao(extendedRelation).countByRelatedId(relatedId);
    }

    /**
     * Determine if a mapping in the extended relation to the related id exists
     *
     * @param extendedRelation extended relation
     * @param relatedId        related id
     * @return true if mapping exists
     * @since 6.3.0
     */
    public boolean hasMappingToRelated(ExtendedRelation extendedRelation,
                                       long relatedId) {
        return countMappingsToRelated(extendedRelation, relatedId) > 0;
    }

    /**
     * Delete mappings to the related table and id
     *
     * @param relatedTable related table name
     * @param relatedId    related id
     * @return rows deleted
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int deleteMappingsToRelated(String relatedTable, long relatedId)
            throws SQLException {
        return deleteMappingsToRelated(getRelatedTableRelations(relatedTable),
                relatedId);
    }

    /**
     * Delete mappings in the extended relations to the related id
     *
     * @param extendedRelations extended relations
     * @param relatedId         related id
     * @return rows deleted
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int deleteMappingsToRelated(
            Collection<ExtendedRelation> extendedRelations, long relatedId)
            throws SQLException {
        int count = 0;
        if (extendedRelations != null) {
            for (ExtendedRelation extendedRelation : extendedRelations) {
                count += deleteMappingsToRelated(extendedRelation, relatedId);
            }
        }
        return count;
    }

    /**
     * Delete mappings in the extended relation to the related id
     *
     * @param extendedRelation extended relation
     * @param relatedId        related id
     * @return rows deleted
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int deleteMappingsToRelated(ExtendedRelation extendedRelation,
                                       long relatedId) throws SQLException {
        return getMappingDao(extendedRelation).deleteByRelatedId(relatedId);
    }

    /**
     * Count the number of mappings to the table and id
     *
     * @param table table name
     * @param id    table id
     * @return mappings count
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int countMappings(String table, long id) throws SQLException {
        return countMappingsToBase(getBaseTableRelations(table), id)
                + countMappingsToRelated(getRelatedTableRelations(table), id);
    }

    /**
     * Determine if a mapping to the table and id exists
     *
     * @param table table name
     * @param id    table id
     * @return true if mapping exists
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public boolean hasMapping(String table, long id) throws SQLException {
        return hasMappingToBase(getBaseTableRelations(table), id)
                || hasMappingToRelated(getRelatedTableRelations(table), id);
    }

    /**
     * Delete mappings to the table and id
     *
     * @param table table name
     * @param id    table id
     * @return rows deleted
     * @throws SQLException upon failure
     * @since 6.3.0
     */
    public int deleteMappings(String table, long id) throws SQLException {
        int count = deleteMappingsToBase(getBaseTableRelations(table), id);
        count += deleteMappingsToRelated(getRelatedTableRelations(table), id);
        return count;
    }

}
