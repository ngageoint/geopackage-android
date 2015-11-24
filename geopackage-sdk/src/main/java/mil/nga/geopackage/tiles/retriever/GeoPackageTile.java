package mil.nga.geopackage.tiles.retriever;

/**
 * GeoPackage tile wrapper containing tile dimensions and raw image bytes
 *
 * @author osbornb
 * @since 1.2.0
 */
public class GeoPackageTile {

    /**
     * Tile width
     */
    public final int width;

    /**
     * Tile height
     */
    public final int height;

    /**
     * Image bytes
     */
    public final byte[] data;

    /**
     * Constructor
     *
     * @param width
     * @param height
     * @param data
     */
    public GeoPackageTile(int width, int height, byte[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    /**
     * Get width
     *
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get height
     *
     * @return height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get image data
     *
     * @return image data
     */
    public byte[] getData() {
        return data;
    }

}
