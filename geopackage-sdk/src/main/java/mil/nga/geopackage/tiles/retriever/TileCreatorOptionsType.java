package mil.nga.geopackage.tiles.retriever;

/**
 * Tile Creator options types for defining nearby zoom level tile search behavior
 *
 * @author osbornb
 * @since 2.0.2
 */
public enum TileCreatorOptionsType {

    /**
     * Search for tiles by zooming in
     */
    ZOOM_IN,

    /**
     * Search for tiles by zooming out
     */
    ZOOM_OUT,

    /**
     * Search for tiles by zooming in first, and then zooming out
     */
    ZOOM_IN_BEFORE_OUT,

    /**
     * Search for tiles by zooming out first, and then zooming in
     */
    ZOOM_OUT_BEFORE_IN,

    /**
     * Search for tiles in closest zoom level order, zoom in levels before zoom out
     */
    ZOOM_CLOSEST_IN_BEFORE_OUT,

    /**
     * Search for tiles in closest zoom level order, zoom out levels before zoom in
     */
    ZOOM_CLOSEST_OUT_BEFORE_IN;

}
