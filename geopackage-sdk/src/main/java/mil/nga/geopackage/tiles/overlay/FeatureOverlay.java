package mil.nga.geopackage.tiles.overlay;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.features.FeatureTiles;

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
     * Min zoom
     */
    private Integer minZoom;

    /**
     * Max zoom
     */
    private Integer maxZoom;

    /**
     * Web mercator bounding box
     */
    private BoundingBox webMercatorBoundingBox;

    /**
     * Constructor
     *
     * @param featureTiles
     */
    public FeatureOverlay(FeatureTiles featureTiles) {
        this.featureTiles = featureTiles;
    }

    /**
     * Get the min zoom
     *
     * @return
     */
    public Integer getMinZoom() {
        return minZoom;
    }

    /**
     * Set the min zoom
     *
     * @param minZoom
     */
    public void setMinZoom(Integer minZoom) {
        this.minZoom = minZoom;
    }

    /**
     * Get the max zoom
     *
     * @return
     */
    public Integer getMaxZoom() {
        return maxZoom;
    }

    /**
     * Set the max zoom
     *
     * @param maxZoom
     */
    public void setMaxZoom(Integer maxZoom) {
        this.maxZoom = maxZoom;
    }

    /**
     * Set the bounding box, provided as the indicated projection
     *
     * @param boundingBox
     * @param projection
     */
    public void setBoundingBox(BoundingBox boundingBox, Projection projection) {
        ProjectionTransform projectionToWebMercator = projection
                .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        webMercatorBoundingBox = projectionToWebMercator
                .transform(boundingBox);
    }

    /**
     * Get the web mercator bounding box
     *
     * @return
     */
    public BoundingBox getWebMercatorBoundingBox() {
        return webMercatorBoundingBox;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {

        Tile tile = null;

        // Check if generating tiles for the zoom level
        if (isWithinZoom(zoom) && isWithinBounds(x, y, zoom)) {

            // Draw the tile
            byte[] tileData = featureTiles.drawTileBytes(x, y, zoom);

            if (tileData != null) {
                // Create the tile
                tile = new Tile(featureTiles.getTileWidth(), featureTiles.getTileHeight(), tileData);
            }
        }

        return tile;
    }

    /**
     * Check if the zoom is within the overlay zoom range
     *
     * @param zoom
     * @return
     */
    private boolean isWithinZoom(int zoom) {
        return (minZoom == null || zoom >= minZoom) && (maxZoom == null || zoom <= maxZoom);
    }

    /**
     * Check if the tile request is within the desired tile bounds
     *
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    private boolean isWithinBounds(int x, int y, int zoom) {
        boolean withinBounds = true;

        // If a bounding box is set, check if it overlaps with the request
        if (webMercatorBoundingBox != null) {

            // Get the bounding box of the requested tile
            BoundingBox requestWebMercatorBoundingBox = TileBoundingBoxUtils
                    .getWebMercatorBoundingBox(x, y, zoom);

            // Check if the request overlaps
            withinBounds = TileBoundingBoxUtils.overlap(webMercatorBoundingBox,
                    requestWebMercatorBoundingBox) != null;
        }

        return withinBounds;
    }

}
