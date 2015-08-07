package mil.nga.geopackage.tiles.user;

import android.database.Cursor;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserWrapperConnection;

/**
 * GeoPackage Tile Cursor Wrapper Connection
 *
 * @author osbornb
 */
public class TileWrapperConnection extends
        UserWrapperConnection<TileColumn, TileTable, TileRow, TileCursor> {

    /**
     * Constructor
     *
     * @param database
     */
    public TileWrapperConnection(GeoPackageConnection database) {
        super(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TileCursor wrapCursor(Cursor cursor) {
        return new TileCursor(null, cursor);
    }
}
