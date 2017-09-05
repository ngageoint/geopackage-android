package mil.nga.geopackage.tiles.user;

import mil.nga.geopackage.user.UserRowSync;

/**
 * Tile Row Sync to support reading a single tile row copy when multiple
 * near simultaneous asynchronous requests are made
 *
 * @author osbornb
 * @since 1.4.2
 */
public class TileRowSync extends UserRowSync<TileColumn, TileTable, TileRow> {

    /**
     * Constructor
     */
    public TileRowSync() {

    }

}
