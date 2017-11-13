package mil.nga.geopackage.tiles.user;

import java.util.List;

import mil.nga.geopackage.user.UserInvalidCursor;

/**
 * Tile Invalid Cursor wrapper for tile requery to handle failed rows due to large blobs
 *
 * @author osbornb
 * @since 2.0.0
 */
public class TileInvalidCursor extends UserInvalidCursor<TileColumn, TileTable, TileRow, TileCursor, TileDao> {

    /**
     * Constructor
     *
     * @param dao              tile dao
     * @param cursor           tile cursor
     * @param invalidPositions invalid positions from a previous cursor
     * @param blobColumns      blob columns
     */
    public TileInvalidCursor(TileDao dao, TileCursor cursor, List<Integer> invalidPositions, List<TileColumn> blobColumns) {
        super(dao, cursor, invalidPositions, blobColumns);
    }

}
