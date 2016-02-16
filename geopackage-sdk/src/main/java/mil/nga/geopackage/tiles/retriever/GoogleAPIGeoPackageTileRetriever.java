package mil.nga.geopackage.tiles.retriever;

import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * GeoPackage Tile Retriever, assumes the Google Maps API zoom level
 * and grid
 *
 * @author osbornb
 * @since 1.2.0
 */
public class GoogleAPIGeoPackageTileRetriever implements TileRetriever {

    /**
     * Tile data access object
     */
    private final TileDao tileDao;

    /**
     * Constructor
     *
     * @param tileDao
     */
    public GoogleAPIGeoPackageTileRetriever(TileDao tileDao) {
        this.tileDao = tileDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTile(int x, int y, int zoom) {
        return retrieveTileRow(x, y, zoom) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoPackageTile getTile(int x, int y, int zoom) {

        GeoPackageTile tile = null;

        TileRow tileRow = retrieveTileRow(x, y, zoom);
        if (tileRow != null) {
            TileMatrix tileMatrix = tileDao.getTileMatrix(zoom);
            int tileWidth = (int) tileMatrix.getTileWidth();
            int tileHeight = (int) tileMatrix.getTileHeight();
            tile = new GeoPackageTile(tileWidth, tileHeight, tileRow.getTileData());
        }

        return tile;
    }

    /**
     * Retrieve the tile row
     *
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    private TileRow retrieveTileRow(int x, int y, int zoom) {
        return tileDao.queryForTile(x, y, zoom);
    }

}
