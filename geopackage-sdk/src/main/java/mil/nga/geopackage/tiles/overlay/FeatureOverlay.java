package mil.nga.geopackage.tiles.overlay;

import com.google.android.gms.maps.model.Tile;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.user.TileDao;

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
     * Linked GeoPackage overlays
     */
    private List<GeoPackageOverlay> linkedOverlays = new ArrayList<>();

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
    public boolean hasTileToRetrieve(int x, int y, int zoom) {

        // Determine if the tile should be drawn
        boolean drawTile = true;
        for (GeoPackageOverlay geoPackageOverlay : linkedOverlays) {
            if (geoPackageOverlay.hasTile(x, y, zoom)) {
                drawTile = false;
                break;
            }
        }

        return drawTile;
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

    /**
     * Ignore drawing tiles if they exist in the tile tables represented by the tile daos
     *
     * @param tileDaos tile data access objects
     * @since 1.2.6
     */
    public void ignoreTileDaos(List<TileDao> tileDaos) {

        for (TileDao tileDao : tileDaos) {
            ignoreTileDao(tileDao);
        }

    }

    /**
     * Ignore drawing tiles if they exist in the tile table represented by the tile dao
     *
     * @param tileDao tile data access object
     * @since 1.2.6
     */
    public void ignoreTileDao(TileDao tileDao) {

        GeoPackageOverlay tileOverlay = new GeoPackageOverlay(tileDao);
        linkedOverlays.add(tileOverlay);
    }

    /***
     * Clear all ignored tile tables
     *
     * @since 1.2.6
     */
    public void clearIgnored() {
        linkedOverlays.clear();
    }

}
