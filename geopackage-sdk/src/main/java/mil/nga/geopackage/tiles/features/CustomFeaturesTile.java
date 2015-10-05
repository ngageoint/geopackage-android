package mil.nga.geopackage.tiles.features;

import android.graphics.Bitmap;

/**
 * Interface defining custom feature tile drawing.
 * The tile drawn will be used instead of drawing all of the features.
 *
 * @author osbornb
 * @since 1.1.0
 */
public interface CustomFeaturesTile {

    /**
     * Draw a custom tile
     *
     * @param tileWidth
     * @param tileHeight
     * @param features   number of features in the tile
     * @return custom bitmap, or null
     */
    public Bitmap drawTile(int tileWidth, int tileHeight, long features);

}
