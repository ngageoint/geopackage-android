package mil.nga.geopackage.extension.elevation;

import android.graphics.Bitmap;

import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Elevation image, stores the tile row image
 *
 * @author osbornb
 * @since 1.3.1
 */
public class ElevationImage {

    // TODO may not be needed since there is no raster as in java

    /**
     * Bitmap Image
     */
    private final Bitmap image;

    /**
     * Constructor
     *
     * @param tileRow tile row
     */
    public ElevationImage(TileRow tileRow) {
        image = tileRow.getTileDataBitmap();
    }

    /**
     * Get the bitmap image
     *
     * @return bitmap image
     */
    public Bitmap getImage() {
        return image;
    }

    /**
     * Get the width
     *
     * @return width
     */
    public int getWidth() {
        return image.getWidth();
    }

    /**
     * Get the height
     *
     * @return height
     */
    public int getHeight() {
        return image.getHeight();
    }

}
