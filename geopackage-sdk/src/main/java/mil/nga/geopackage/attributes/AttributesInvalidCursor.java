package mil.nga.geopackage.attributes;

import java.util.List;

import mil.nga.geopackage.user.UserInvalidCursor;

/**
 * Attributes Invalid Cursor wrapper for attributes requery to handle failed rows due to large blobs
 *
 * @author osbornb
 * @since 2.0.0
 */
public class AttributesInvalidCursor extends UserInvalidCursor<AttributesColumn, AttributesTable, AttributesRow, AttributesCursor, AttributesDao> {

    /**
     * Constructor
     *
     * @param dao              attributes dao
     * @param cursor           attributes cursor
     * @param invalidPositions invalid positions from a previous cursor
     * @param blobColumns      blob columns
     */
    public AttributesInvalidCursor(AttributesDao dao, AttributesCursor cursor, List<Integer> invalidPositions, List<AttributesColumn> blobColumns) {
        super(dao, cursor, invalidPositions, blobColumns);
    }

}
