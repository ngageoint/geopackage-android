package mil.nga.geopackage.user.custom;

import java.util.List;

import mil.nga.geopackage.user.UserInvalidCursor;

/**
 * User Custom Invalid Cursor wrapper for user custom requery to handle failed rows due to large blobs
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomInvalidCursor extends UserInvalidCursor<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomCursor, UserCustomDao> {

    /**
     * Constructor
     *
     * @param dao              user custom dao
     * @param cursor           user custom cursor
     * @param invalidPositions invalid positions from a previous cursor
     * @param blobColumns      blob columns
     */
    public UserCustomInvalidCursor(UserCustomDao dao, UserCustomCursor cursor, List<Integer> invalidPositions, List<UserCustomColumn> blobColumns) {
        super(dao, cursor, invalidPositions, blobColumns);
    }

}
