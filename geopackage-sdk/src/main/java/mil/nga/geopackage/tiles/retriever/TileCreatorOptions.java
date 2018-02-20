package mil.nga.geopackage.tiles.retriever;

/**
 * Tile Creator options for defining how to search for tiles in nearby zoom levels. When a tile
 * does not exist, a search for further zoomed in and/or out tiles can be performed.  The direction
 * and order of the search is defined in these options.
 *
 * @author osbornb
 * @since 2.0.2
 */
public class TileCreatorOptions {

    /**
     * Options search behavior type
     */
    private TileCreatorOptionsType type = null;

    /**
     * Max zoom levels in to search
     */
    private long zoomIn = 0;

    /**
     * Max zoom levels out to search
     */
    private long zoomOut = 0;

    /**
     * Default constructor
     */
    public TileCreatorOptions() {

    }

    /**
     * Constructor
     *
     * @param type    options search behavior type
     * @param zoomIn  max zoom in levels
     * @param zoomOut max zoom out levels
     */
    public TileCreatorOptions(TileCreatorOptionsType type, long zoomIn, long zoomOut) {
        this.type = type;
        this.zoomIn = zoomIn;
        this.zoomOut = zoomOut;
    }

    /**
     * Get the type
     *
     * @return type
     */
    public TileCreatorOptionsType getType() {
        return type;
    }

    /**
     * Set the type
     *
     * @param type type
     */
    public void setType(TileCreatorOptionsType type) {
        this.type = type;
    }

    /**
     * Get the max zoom in levels
     *
     * @return zoom in levels
     */
    public long getZoomIn() {
        return zoomIn;
    }

    /**
     * Set the max zoom in levels
     *
     * @param zoomIn zoom in levels
     */
    public void setZoomIn(long zoomIn) {
        this.zoomIn = zoomIn;
    }

    /**
     * Get the max zoom out levels
     *
     * @return zoom out levels
     */
    public long getZoomOut() {
        return zoomOut;
    }

    /**
     * Set the max zoom out levels
     *
     * @param zoomOut zoom out levels
     */
    public void setZoomOut(long zoomOut) {
        this.zoomOut = zoomOut;
    }

    /**
     * Is zoom in tile search enabled
     *
     * @return true if zoom in for tiles is allowed
     */
    public boolean isZoomIn() {
        return zoomIn > 0 && type != null && type != TileCreatorOptionsType.ZOOM_OUT;
    }

    /**
     * Is zoom out tile search enabled
     *
     * @return true if zoom out for tiles is allowed
     */
    public boolean isZoomOut() {
        return zoomOut > 0 && type != null && type != TileCreatorOptionsType.ZOOM_IN;
    }

}
