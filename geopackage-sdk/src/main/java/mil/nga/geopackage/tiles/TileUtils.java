package mil.nga.geopackage.tiles;

import android.util.DisplayMetrics;

/**
 * Tile utilities and constants
 *
 * @author osbornb
 * @since 3.2.0
 */
public class TileUtils {

    /**
     * Displayed device-independent pixels
     */
    public static final int TILE_DP = 256;

    /**
     * Tile pixels for default dpi tiles
     */
    public static final int TILE_PIXELS_DEFAULT = TILE_DP;

    /**
     * Tile pixels for high dpi tiles
     */
    public static final int TILE_PIXELS_HIGH = TILE_PIXELS_DEFAULT * 2;

    /**
     * High density scale
     */
    public static final float HIGH_DENSITY = ((float) DisplayMetrics.DENSITY_HIGH) / DisplayMetrics.DENSITY_DEFAULT;

    /**
     * Get the tile side (width and height) dimension based upon the display density scale
     *
     * @param density display density: {@link android.util.DisplayMetrics#density}
     * @return default tile length
     */
    public static int tileLength(float density) {
        int length;
        if (density < HIGH_DENSITY) {
            length = TILE_PIXELS_DEFAULT;
        } else {
            length = TILE_PIXELS_HIGH;
        }
        return length;
    }

    /**
     * Get the tile density based upon the display density scale and tile dimensions
     *
     * @param density    display density: {@link android.util.DisplayMetrics#density}
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return tile density
     */
    public static float tileDensity(float density, int tileWidth, int tileHeight) {
        return tileDensity(density, Math.min(tileWidth, tileHeight));
    }

    /**
     * Get the tile density based upon the display density scale and tile length (width or height)
     *
     * @param density    display density: {@link android.util.DisplayMetrics#density}
     * @param tileLength tile length (width or height)
     * @return tile density
     */
    public static float tileDensity(float density, int tileLength) {
        return density * TILE_DP / tileLength;
    }

    /**
     * Get the density based upon the tile dimensions
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return density
     */
    public static float density(int tileWidth, int tileHeight) {
        return density(Math.min(tileWidth, tileHeight));
    }

    /**
     * Get the density based upon the tile length (width or height)
     *
     * @param tileLength tile length (width or height)
     * @return density
     */
    public static float density(int tileLength) {
        return ((float) tileLength) / TILE_DP;
    }

}
