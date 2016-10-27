package mil.nga.geopackage.extension.elevation;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReaderInt;
import ar.com.hjg.pngj.PngWriter;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Elevation image, stores the tile row image
 *
 * @author osbornb
 * @since 1.3.1
 */
public class ElevationPngImage implements ElevationImage {

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
     * PNG reader
     */
    private PngReaderInt reader;

    /**
     * Byte array output stream
     */
    private ByteArrayOutputStream outputStream;

    /**
     * PNG writer
     */
    private PngWriter writer;

    /**
     * Pixel values
     */
    private int[][] pixels;

    /**
     * Constructor, used for reading a PNG
     *
     * @param tileRow tile row
     */
    public ElevationPngImage(TileRow tileRow) {
        imageBytes = tileRow.getTileData();
        reader = new PngReaderInt(new ByteArrayInputStream(imageBytes));
        ElevationTilesPng.validateImageType(reader);
        width = reader.imgInfo.cols;
        height = reader.imgInfo.rows;
    }

    /**
     * Constructor, used for writing a PNG
     *
     * @param imageInfo
     */
    public ElevationPngImage(ImageInfo imageInfo) {
        outputStream = new ByteArrayOutputStream();
        writer = new PngWriter(outputStream, imageInfo);
        width = imageInfo.cols;
        height = imageInfo.rows;
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
        } else if (outputStream != null) {
            bytes = outputStream.toByteArray();
        }
        return bytes;
    }

    /**
     * Get the PNG reader
     *
     * @return reader
     */
    public PngReaderInt getReader() {
        return reader;
    }

    /**
     * Get the PNG writer
     *
     * @return writer
     */
    public PngWriter getWriter() {
        return writer;
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
     * Flush the output stream and set the image bytes, close the stream
     */
    public void flushStream() {
        if (outputStream != null) {
            if (imageBytes == null) {
                imageBytes = outputStream.toByteArray();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.w(ElevationPngImage.class.getSimpleName(), "Failed to close output stream", e);
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
    public int getPixel(int x, int y) {
        int pixel = -1;
        if (pixels == null) {
            readPixels();
        }
        if (pixels != null) {
            pixel = pixels[y][x];
        } else {
            throw new GeoPackageException("Could not retrieve pixel value");
        }
        return pixel;
    }

    /**
     * Read all the pixels from the image
     */
    private void readPixels() {
        if (reader != null) {
            pixels = new int[reader.imgInfo.rows][reader.imgInfo.cols];
            int rowCount = 0;
            while (reader.hasMoreRows()) {
                ImageLineInt row = reader.readRowInt();
                int[] columnValues = new int[reader.imgInfo.cols];
                System.arraycopy(row.getScanline(), 0, columnValues, 0, columnValues.length);
                pixels[rowCount++] = columnValues;
            }
            reader.close();
        }
    }

}
