package mil.nga.geopackage.extension.nga.style;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.extension.related.UserMappingDao;
import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomRow;
import mil.nga.sf.GeometryType;

/**
 * Style Mapping DAO for reading style mapping data tables
 *
 * @author osbornb
 * @since 3.2.0
 */
public class StyleMappingDao extends UserMappingDao {

    /**
     * Constructor
     *
     * @param dao user custom data access object
     */
    public StyleMappingDao(UserCustomDao dao) {
        super(dao, new StyleMappingTable(dao.getTable()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleMappingTable getTable() {
        return (StyleMappingTable) super.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleMappingRow newRow() {
        return new StyleMappingRow(getTable());
    }

    /**
     * Get the style mapping row from the current cursor location
     *
     * @param cursor cursor
     * @return style mapping row
     */
    public StyleMappingRow getRow(UserCustomCursor cursor) {
        return getRow(cursor.getRow());
    }

    /**
     * Get a style mapping row from the user custom row
     *
     * @param row custom row
     * @return style mapping row
     */
    public StyleMappingRow getRow(UserCustomRow row) {
        return new StyleMappingRow(row);
    }

    /**
     * Query for style mappings by base id
     *
     * @param id base id, feature contents id or feature geometry id
     * @return style mappings rows
     */
    public List<StyleMappingRow> queryByBaseFeatureId(long id) {
        List<StyleMappingRow> rows = new ArrayList<>();
        UserCustomCursor cursor = queryByBaseId(id);
        try {
            while (cursor.moveToNext()) {
                rows.add(getRow(cursor));
            }
        } finally {
            cursor.close();
        }
        return rows;
    }

    /**
     * Delete by base is and geometry type
     *
     * @param id           base id
     * @param geometryType geometry type
     * @return rows deleted
     */
    public int deleteByBaseId(long id, GeometryType geometryType) {

        String geometryTypeName = null;
        if (geometryType != null) {
            geometryTypeName = geometryType.getName();
        }

        StringBuilder where = new StringBuilder();
        where.append(buildWhere(StyleMappingTable.COLUMN_BASE_ID, id));
        where.append(" AND ");
        where.append(buildWhere(StyleMappingTable.COLUMN_GEOMETRY_TYPE_NAME,
                geometryTypeName));

        List<Object> whereArguments = new ArrayList<>();
        whereArguments.add(id);
        if (geometryTypeName != null) {
            whereArguments.add(geometryTypeName);
        }

        String[] whereArgs = buildWhereArgs(whereArguments);

        int deleted = delete(where.toString(), whereArgs);

        return deleted;
    }

}
