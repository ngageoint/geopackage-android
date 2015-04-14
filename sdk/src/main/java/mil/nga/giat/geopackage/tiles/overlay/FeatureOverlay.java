package mil.nga.giat.geopackage.tiles.overlay;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import mil.nga.giat.geopackage.tiles.features.FeatureTiles;

/**
 * Feature overlay which draws tiles from a feature table
 *
 * @author osbornb
 */
public class FeatureOverlay implements TileProvider {

    /**
     * Feature tiles
     */
    private final FeatureTiles featureTiles;

    /**
     * Constructor
     *
     * @param featureTiles
     */
    public FeatureOverlay(FeatureTiles featureTiles) {
        this.featureTiles = featureTiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {

        byte[] tileData = featureTiles.drawTileBytes(x, y, zoom);

        // Create the tile
        Tile tile = new Tile(featureTiles.getTileWidth(), featureTiles.getTileHeight(), tileData);

        return tile;
    }

}
