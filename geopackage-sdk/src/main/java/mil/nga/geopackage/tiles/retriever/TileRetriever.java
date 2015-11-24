package mil.nga.geopackage.tiles.retriever;

/**
 * Interface defining the get tile retrieval method
 *
 * @author osbornb
 * @since 1.2.0
 */
public interface TileRetriever {

    /**
     * Get a tile from the x, y, and zoom
     *
     * @param x
     * @param y
     * @param zoom
     * @return tile with dimensions and bytes
     */
    public GeoPackageTile getTile(int x, int y, int zoom);

}
