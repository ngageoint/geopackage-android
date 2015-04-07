package mil.nga.giat.geopackage.tiles.user;

import mil.nga.giat.geopackage.db.GeoPackageConnection;
import mil.nga.giat.geopackage.user.UserConnection;

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
