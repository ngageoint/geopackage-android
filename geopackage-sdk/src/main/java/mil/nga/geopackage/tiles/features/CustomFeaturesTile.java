package mil.nga.geopackage.tiles.features;

import android.graphics.Bitmap;

import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureCursor;

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
     * @param tileWidth        tile width to draw
     * @param tileHeight       tile height to draw
     * @param tileFeatureCount count of features in the requested tile
     * @param featureIndexResults          results as feature index results
     * @return custom bitmap, or null
     */
    public Bitmap drawTile(int tileWidth, int tileHeight, long tileFeatureCount, FeatureIndexResults featureIndexResults);

    /**
     * Draw a custom tile when the number of features within the tile is unknown.
     * This is called when a feature table is not indexed and more total features exist than the
     * max per tile.
     *
     * @param tileWidth         tile width to draw
     * @param tileHeight        tile height to draw
     * @param totalFeatureCount count of total features in the feature table
     * @param allFeatureResults           results in a feature cursor
     * @return custom bitmap, or null
     */
    public Bitmap drawUnindexedTile(int tileWidth, int tileHeight, long totalFeatureCount, FeatureCursor allFeatureResults);

}
