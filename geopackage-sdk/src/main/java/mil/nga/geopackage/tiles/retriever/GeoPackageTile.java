package mil.nga.geopackage.tiles.retriever;

import android.graphics.Bitmap;

import mil.nga.geopackage.io.BitmapConverter;

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
     * @param width  tile width
     * @param height tile height
     * @param data   tile bytes
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

    /**
     * Get image bitmap
     *
     * @return image bitmap
     * @since 6.0.0
     */
    public Bitmap getBitmap() {
        Bitmap bitmap = null;
        if (data != null) {
            bitmap = BitmapConverter.toBitmap(data);
        }
        return bitmap;
    }

}
