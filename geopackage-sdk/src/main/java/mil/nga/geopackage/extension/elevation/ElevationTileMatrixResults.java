package mil.nga.geopackage.extension.elevation;

import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileCursor;

/**
 * Elevation Tile Matrix results including the elevation tile results and the
 * tile matrix where found
 *
 * @author osbornb
 * @since 1.3.1
 */
public class ElevationTileMatrixResults {

    /**
     * Tile matrix
     */
    private TileMatrix tileMatrix;

    /**
     * Elevation tile results
     */
    private TileCursor tileResults;

    /**
     * Constructor
     *
     * @param tileMatrix  tile matrix
     * @param tileResults elevation tile results
     */
    public ElevationTileMatrixResults(TileMatrix tileMatrix,
                                      TileCursor tileResults) {
        this.tileMatrix = tileMatrix;
        this.tileResults = tileResults;
    }

    /**
     * Get the tile matrix
     *
     * @return tile matrix
     */
    public TileMatrix getTileMatrix() {
        return tileMatrix;
    }

    /**
     * Get the tile results
     *
     * @return tile results
     */
    public TileCursor getTileResults() {
        return tileResults;
    }

}
