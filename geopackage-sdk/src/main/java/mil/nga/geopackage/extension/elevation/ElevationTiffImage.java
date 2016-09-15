package mil.nga.geopackage.extension.elevation;

import java.util.List;

import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.tiff.io.FileDirectories;
import mil.nga.tiff.io.FileDirectory;
import mil.nga.tiff.io.Rasters;
import mil.nga.tiff.io.TiffReader;

/**
 * Elevation TIFF image
 *
 * @author osbornb
 * @since 1.3.1
 */
public class ElevationTiffImage implements ElevationImage {

    /**
     * Image width / number of columns
     */
    private final int width;

    /**
     * Image height / number of rows
     */
    private final int height;

    /**
     * Image bytes
     */
    private byte[] imageBytes;

    /**
     * File directories
     */
    private FileDirectories directories;

    /**
     * Constructor, used for reading a PNG
     *
     * @param tileRow tile row
     */
    public ElevationTiffImage(TileRow tileRow) {
        imageBytes = tileRow.getTileData();

        directories = TiffReader.readTiff(imageBytes);

        FileDirectory directory = directories.getFileDirectory();
        width = directory.getImageWidth().intValue();
        height = directory.getImageHeight().intValue();

        Rasters rasters = directory.readRasters(true, true);
        rasters.hasInterleaveValues();
        rasters.hasSampleValues();
    }

    public ElevationTiffImage(int width, int height) {
        // TODO
        this.width = width;
        this.height = height;
    }

    /**
     * Get the image bytes
     *
     * @return image bytes
     */
    public byte[] getImageBytes() {
        byte[] bytes = null;
        if (imageBytes != null) {
            bytes = imageBytes;
        } else {
            // TODO
        }
        return imageBytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return height;
    }


    public float getPixel(int x, int y) {

        return 0.0f; // TODO
    }

}
