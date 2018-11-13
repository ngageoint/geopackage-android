package mil.nga.geopackage.extension.related.media;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * User Media DAO for reading user media data tables
 *
 * @author osbornb
 * @since 3.0.1
 */
public class MediaDao extends UserCustomDao {

    /**
     * Constructor
     *
     * @param dao user custom data access object
     */
    public MediaDao(UserCustomDao dao) {
        super(dao, new MediaTable(dao.getTable()));
    }

    /**
     * Constructor
     *
     * @param dao        user custom data access object
     * @param mediaTable media table
     */
    protected MediaDao(UserCustomDao dao, MediaTable mediaTable) {
        super(dao, mediaTable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaTable getTable() {
        return (MediaTable) super.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaRow newRow() {
        return new MediaRow(getTable());
    }

    /**
     * Get the media row from the current cursor location
     *
     * @param cursor cursor
     * @return media row
     */
    public MediaRow getRow(UserCustomCursor cursor) {
        return getRow(cursor.getRow());
    }

    /**
     * Get a media row from the user custom row
     *
     * @param row custom row
     * @return media row
     */
    public MediaRow getRow(UserCustomRow row) {
        return new MediaRow(row);
    }

    /**
     * Get the media rows that exist with the provided ids
     *
     * @param ids list of ids
     * @return media rows
     */
    public List<MediaRow> getRows(List<Long> ids) {
        List<MediaRow> mediaRows = new ArrayList<>();
        for (long id : ids) {
            UserCustomRow userCustomRow = queryForIdRow(id);
            if (userCustomRow != null) {
                mediaRows.add(getRow(userCustomRow));
            }
        }
        return mediaRows;
    }

}
