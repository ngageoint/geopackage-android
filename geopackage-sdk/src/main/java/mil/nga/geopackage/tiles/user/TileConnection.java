package mil.nga.geopackage.tiles.user;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage Tile Connection
 *
 * @author osbornb
 */
public class TileConnection extends
        UserConnection<TileColumn, TileTable, TileRow, TileCursor> {

    /**
     * Constructor
     *
     * @param database
     */
    public TileConnection(GeoPackageConnection database) {
        super(database);
    }

}
