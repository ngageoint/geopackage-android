package mil.nga.giat.geopackage.tiles.features;

import android.content.Context;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.tiles.TileGenerator;

/**
 * Creates a set of tiles within a GeoPackage by generating tiles from features
 *
 * @author osbornb
 */
public class FeatureTileGenerator extends TileGenerator {

    /**
     * Feature tiles
     */
    private final FeatureTiles featureTiles;

    /**
     * Constructor
     *
     * @param context
     * @param geoPackage
     * @param tableName
     * @param featureTiles
     * @param minZoom
     * @param maxZoom
     */
    public FeatureTileGenerator(Context context, GeoPackage geoPackage,
                                String tableName, FeatureTiles featureTiles, int minZoom, int maxZoom) {
        super(context, geoPackage, tableName, minZoom, maxZoom);
        this.featureTiles = featureTiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte[] createTile(int z, long x, long y) {

        byte[] tileData = featureTiles.drawTileBytes((int) x, (int) y, z);

        return tileData;
    }

}
