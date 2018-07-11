package mil.nga.geopackage.tiles.retriever;

/**
 * Interface defining the get tile retrieval method
 *
 * @author osbornb
 * @since 1.2.0
 */
public interface TileRetriever {

    /**
     * Check if there is a tile for the x, y, and zoom
     *
     * @param x    x coordinate
     * @param y    y coordinate
     * @param zoom zoom level
     * @return true if a tile exists
     * @since 1.2.6
     */
    public boolean hasTile(int x, int y, int zoom);

    /**
     * Get a tile from the x, y, and zoom
     *
     * @param x    x coordinate
     * @param y    y coordinate
     * @param zoom zoom level
     * @return tile with dimensions and bytes
     */
    public GeoPackageTile getTile(int x, int y, int zoom);

}
