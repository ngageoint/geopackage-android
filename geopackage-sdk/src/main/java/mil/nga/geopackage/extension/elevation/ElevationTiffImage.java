package mil.nga.geopackage.extension.elevation;

import java.io.IOException;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.TiffReader;
import mil.nga.tiff.TiffWriter;

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
     * File directory
     */
    private FileDirectory directory;

    /**
     * Rasters
     */
    private Rasters rasters;

    /**
     * Constructor, used for reading a TIFF
     *
     * @param tileRow tile row
     */
    public ElevationTiffImage(TileRow tileRow) {
        imageBytes = tileRow.getTileData();
        TIFFImage tiffImage = TiffReader.readTiff(imageBytes);
        directory = tiffImage.getFileDirectory();
        ElevationTilesTiff.validateImageType(directory);
        width = directory.getImageWidth().intValue();
        height = directory.getImageHeight().intValue();
    }

    /**
     * Constructor, used for writing a TIFF
     *
     * @param directory file directory
     */
    public ElevationTiffImage(FileDirectory directory) {
        this.directory = directory;
        this.rasters = directory.getWriteRasters();
        width = directory.getImageWidth().intValue();
        height = directory.getImageHeight().intValue();
    }

    /**
     * Get the image bytes
     *
     * @return image bytes
     */
    public byte[] getImageBytes() {
        if (imageBytes == null) {
            writeTiff();
        }
        return imageBytes;
    }

    /**
     * Get the file directory
     *
     * @return file directory
     */
    public FileDirectory getDirectory() {
        return directory;
    }

    /**
     * Get the rasters, read if needed
     *
     * @return rasters
     */
    public Rasters getRasters() {
        if (rasters == null) {
            readPixels();
        }
        return rasters;
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

    /**
     * Write the TIFF file to the image bytes
     */
    public void writeTiff() {
        if (directory.getWriteRasters() != null) {
            TIFFImage tiffImage = new TIFFImage();
            tiffImage.add(directory);
            try {
                imageBytes = TiffWriter.writeTiffToBytes(tiffImage);
            } catch (IOException e) {
                throw new GeoPackageException("Failed to write TIFF image", e);
            }
        }
    }

    /**
     * Get the pixel at the coordinate
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return pixel value
     */
    public float getPixel(int x, int y) {
        float pixel = -1;
        if (rasters == null) {
            readPixels();
        }
        if (rasters != null) {
            pixel = rasters.getFirstPixelSample(x, y).floatValue();
        } else {
            throw new GeoPackageException("Could not retrieve pixel value");
        }
        return pixel;
    }

    /**
     * Read all the pixels from the image
     */
    private void readPixels() {
        if (directory != null) {
            rasters = directory.readRasters();
        }
    }

}
