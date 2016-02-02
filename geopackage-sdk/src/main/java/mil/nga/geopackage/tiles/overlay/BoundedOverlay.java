package mil.nga.geopackage.tiles.overlay;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;

/**
 * Abstract overlay which provides bounding returned tiles by zoom levels and/or a bounding box
 *
 * @author osbornb
 * @since 1.2.5
 */
public abstract class BoundedOverlay implements TileProvider {

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
     */
    public BoundedOverlay() {

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
     * Get the bounding box as the provided projection
     *
     * @param projection
     */
    public BoundingBox getBoundingBox(Projection projection) {
        ProjectionTransform webMercatorToProjection = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR)
                .getTransformation(projection);
        return webMercatorToProjection
                .transform(webMercatorBoundingBox);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {

        Tile tile = null;

        // Check if generating tiles for the zoom level and is within the bounding box
        if (isWithinBounds(x, y, zoom)) {

            // Retrieve the tile
            tile = retrieveTile(x, y, zoom);
        }

        return tile;
    }

    /**
     * Retrieve the tile
     *
     * @param x
     * @param y
     * @param zoom
     * @return tile
     */
    protected abstract Tile retrieveTile(int x, int y, int zoom);

    /**
     * Is the tile within the zoom and bounding box bounds
     *
     * @param x
     * @param y
     * @param zoom
     * @return true if within bounds
     */
    public boolean isWithinBounds(int x, int y, int zoom) {
        return isWithinZoom(zoom) && isWithinBoundingBox(x, y, zoom);
    }

    /**
     * Check if the zoom is within the overlay zoom range
     *
     * @param zoom
     * @return
     */
    public boolean isWithinZoom(float zoom) {
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
    public boolean isWithinBoundingBox(int x, int y, int zoom) {
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
