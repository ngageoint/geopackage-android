package mil.nga.geopackage.tiles.overlay;

import com.google.android.gms.maps.model.Tile;

import mil.nga.geopackage.tiles.features.FeatureTiles;

/**
 * Feature overlay which draws tiles from a feature table
 *
 * @author osbornb
 */
public class FeatureOverlay extends BoundedOverlay {

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
     * Get the feature tiles
     *
     * @return feature tiles
     * @since 1.1.0
     */
    public FeatureTiles getFeatureTiles() {
        return featureTiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tile retrieveTile(int x, int y, int zoom) {

        // Draw the tile
        byte[] tileData = featureTiles.drawTileBytes(x, y, zoom);

        Tile tile = null;
        if (tileData != null) {
            // Create the tile
            tile = new Tile(featureTiles.getTileWidth(), featureTiles.getTileHeight(), tileData);
        }

        return tile;
    }

}
