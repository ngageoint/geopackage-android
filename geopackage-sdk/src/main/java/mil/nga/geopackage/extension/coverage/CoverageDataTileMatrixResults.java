package mil.nga.geopackage.extension.coverage;

import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileCursor;

/**
 * Coverage Data Tile Matrix results including the coverage data tile results
 * and the tile matrix where found
 *
 * @author osbornb
 * @since 2.0.1
 */
public class CoverageDataTileMatrixResults {

    /**
     * Tile matrix
     */
    private TileMatrix tileMatrix;

    /**
     * Coverage data tile results
     */
    private TileCursor tileResults;

    /**
     * Constructor
     *
     * @param tileMatrix  tile matrix
     * @param tileResults coverage data tile results
     */
    public CoverageDataTileMatrixResults(TileMatrix tileMatrix,
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
