package mil.nga.geopackage.extension.nga.style;

import mil.nga.geopackage.extension.related.media.MediaDao;
import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * Icon DAO for reading icon tables
 *
 * @author osbornb
 * @since 3.2.0
 */
public class IconDao extends MediaDao {

    /**
     * Constructor
     *
     * @param dao user custom data access object
     */
    public IconDao(UserCustomDao dao) {
        super(dao, new IconTable(dao.getTable()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IconTable getTable() {
        return (IconTable) super.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IconRow newRow() {
        return new IconRow(getTable());
    }

    /**
     * Get the icon row from the current result set location
     *
     * @param cursor cursor
     * @return icon row
     */
    public IconRow getRow(UserCustomCursor cursor) {
        return getRow(cursor.getRow());
    }

    /**
     * Get a icon row from the user custom row
     *
     * @param row custom row
     * @return icon row
     */
    public IconRow getRow(UserCustomRow row) {
        return new IconRow(row);
    }

    /**
     * Query for the icon row from a style mapping row
     *
     * @param styleMappingRow style mapping row
     * @return icon row
     */
    public IconRow queryForRow(StyleMappingRow styleMappingRow) {
        IconRow iconRow = null;

        UserCustomRow userCustomRow = queryForIdRow(styleMappingRow
                .getRelatedId());
        if (userCustomRow != null) {
            iconRow = getRow(userCustomRow);
        }

        return iconRow;
    }

}
