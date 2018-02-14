package mil.nga.geopackage.extension.coverage;

import java.io.ByteArrayInputStream;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngReaderInt;
import ar.com.hjg.pngj.PngWriter;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Tiled Gridded Coverage Data, PNG Encoding, Extension
 *
 * @author osbornb
 * @since 2.0.1
 */
public class CoverageDataPng extends CoverageData<CoverageDataPngImage> {

    /**
     * Constructor
     *
     * @param geoPackage        GeoPackage
     * @param tileDao           tile dao
     * @param width             coverage data response width
     * @param height            coverage data response height
     * @param requestProjection request projection
     */
    public CoverageDataPng(GeoPackage geoPackage, TileDao tileDao,
                           Integer width, Integer height, Projection requestProjection) {
        super(geoPackage, tileDao, width,
                height, requestProjection);
    }

    /**
     * Constructor, use the coverage data tables pixel tile size as the request size
     * width and height
     *
     * @param geoPackage GeoPackage
     * @param tileDao    tile dao
     */
    public CoverageDataPng(GeoPackage geoPackage, TileDao tileDao) {
        this(geoPackage, tileDao, null, null, tileDao.getProjection());
    }

    /**
     * Constructor, use the coverage data tables pixel tile size as the request size
     * width and height, request as the specified projection
     *
     * @param geoPackage        GeoPackage
     * @param tileDao           tile dao
     * @param requestProjection request projection
     */
    public CoverageDataPng(GeoPackage geoPackage, TileDao tileDao,
                           Projection requestProjection) {
        this(geoPackage, tileDao, null, null, requestProjection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoverageDataPngImage createImage(TileRow tileRow) {
        return new CoverageDataPngImage(tileRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getValue(GriddedTile griddedTile, TileRow tileRow,
                           int x, int y) {
        byte[] imageBytes = tileRow.getTileData();
        double value = getValue(griddedTile, imageBytes, x, y);
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getValue(GriddedTile griddedTile,
                           CoverageDataPngImage image, int x, int y) {
        Double value = null;
        if (image.getReader() != null) {
            int pixelValue = image.getPixel(x, y);
            value = getValue(griddedTile, pixelValue);
        } else {
            value = getValue(griddedTile, image.getImageBytes(), x, y);
        }
        return value;
    }

    /**
     * Get the pixel value as a 16 bit unsigned integer value
     *
     * @param imageBytes image bytes
     * @param x          x coordinate
     * @param y          y coordinate
     * @return pixel value
     */
    public int getPixelValue(byte[] imageBytes, int x, int y) {

        PngReaderInt reader = new PngReaderInt(new ByteArrayInputStream(imageBytes));
        validateImageType(reader);
        ImageLineInt row = (ImageLineInt) reader.readRow(y);
        int pixelValue = row.getScanline()[x];
        reader.close();

        return pixelValue;
    }

    /**
     * Get the pixel values of the image as 16 bit unsigned integer values
     *
     * @param imageBytes image bytes
     * @return 16 bit unsigned integer pixel values
     */
    public int[] getPixelValues(byte[] imageBytes) {

        PngReaderInt reader = new PngReaderInt(new ByteArrayInputStream(imageBytes));
        validateImageType(reader);
        int[] pixels = new int[reader.imgInfo.cols * reader.imgInfo.rows];
        int rowNumber = 0;
        while (reader.hasMoreRows()) {
            ImageLineInt row = reader.readRowInt();
            int[] rowValues = row.getScanline();
            System.arraycopy(rowValues, 0, pixels, rowNumber * reader.imgInfo.cols, rowValues.length);
            rowNumber++;
        }
        reader.close();

        return pixels;
    }

    /**
     * Validate that the image type is single channel 16 bit
     *
     * @param reader png reader
     */
    public static void validateImageType(PngReader reader) {
        if (reader == null) {
            throw new GeoPackageException("The image is null");
        }
        if (reader.imgInfo.channels != 1 || reader.imgInfo.bitDepth != 16) {
            throw new GeoPackageException(
                    "The coverage data tile is expected to be a single channel 16 bit unsigned short, channels: "
                            + reader.imgInfo.channels + ", bits: " + reader.imgInfo.bitDepth);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getValue(GriddedTile griddedTile,
                           byte[] imageBytes, int x, int y) {
        int pixelValue = getPixelValue(imageBytes, x, y);
        Double value = getValue(griddedTile, pixelValue);
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double[] getValues(GriddedTile griddedTile,
                              byte[] imageBytes) {
        int[] pixelValues = getPixelValues(imageBytes);
        Double[] values = getValues(griddedTile, pixelValues);
        return values;
    }

    /**
     * Draw a coverage data image tile from the flat array of "unsigned short"
     * pixel values of length tileWidth * tileHeight where each pixel is at: (y
     * * tileWidth) + x
     *
     * @param pixelValues "unsigned short" pixel values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return coverage data image tile
     */
    public CoverageDataPngImage drawTile(short[] pixelValues, int tileWidth,
                                         int tileHeight) {

        CoverageDataPngImage image = createImage(tileWidth, tileHeight);
        PngWriter writer = image.getWriter();
        for (int y = 0; y < tileHeight; y++) {
            ImageLineInt row = new ImageLineInt(writer.imgInfo, new int[tileWidth]);
            int[] rowLine = row.getScanline();
            for (int x = 0; x < tileWidth; x++) {
                short pixelValue = pixelValues[(y * tileWidth) + x];
                setPixelValue(rowLine, x, pixelValue);
            }
            writer.writeRow(row);
        }
        writer.end();
        image.flushStream();

        return image;
    }

    /**
     * Draw a coverage data image tile and format as PNG bytes from the flat array
     * of "unsigned short" pixel values of length tileWidth * tileHeight where
     * each pixel is at: (y * tileWidth) + x
     *
     * @param pixelValues "unsigned short" pixel values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return coverage data image tile bytes
     */
    public byte[] drawTileData(short[] pixelValues, int tileWidth,
                               int tileHeight) {
        CoverageDataPngImage image = drawTile(pixelValues, tileWidth, tileHeight);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw a coverage data tile from the double array of "unsigned short" pixel
     * values formatted as short[row][width]
     *
     * @param pixelValues "unsigned short" pixel values as [row][width]
     * @return coverage data image tile
     */
    public CoverageDataPngImage drawTile(short[][] pixelValues) {

        int tileWidth = pixelValues[0].length;
        int tileHeight = pixelValues.length;

        CoverageDataPngImage image = createImage(tileWidth, tileHeight);
        PngWriter writer = image.getWriter();
        for (int y = 0; y < tileHeight; y++) {
            ImageLineInt row = new ImageLineInt(writer.imgInfo, new int[tileWidth]);
            int[] rowLine = row.getScanline();
            for (int x = 0; x < tileWidth; x++) {
                short pixelValue = pixelValues[y][x];
                setPixelValue(rowLine, x, pixelValue);
            }
            writer.writeRow(row);
        }
        writer.end();
        image.flushStream();

        return image;
    }

    /**
     * Draw a coverage data tile and format as PNG bytes from the double array of
     * "unsigned short" pixel values formatted as short[row][width]
     *
     * @param pixelValues "unsigned short" pixel values as [row][width]
     * @return coverage data image tile bytes
     */
    public byte[] drawTileData(short[][] pixelValues) {
        CoverageDataPngImage image = drawTile(pixelValues);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw a coverage data image tile from the flat array of unsigned 16 bit
     * integer pixel values of length tileWidth * tileHeight where each pixel is
     * at: (y * tileWidth) + x
     *
     * @param unsignedPixelValues unsigned 16 bit integer pixel values of length tileWidth *
     *                            tileHeight
     * @param tileWidth           tile width
     * @param tileHeight          tile height
     * @return coverage data image tile
     */
    public CoverageDataPngImage drawTile(int[] unsignedPixelValues, int tileWidth,
                                         int tileHeight) {

        CoverageDataPngImage image = createImage(tileWidth, tileHeight);
        PngWriter writer = image.getWriter();
        for (int y = 0; y < tileHeight; y++) {
            ImageLineInt row = new ImageLineInt(writer.imgInfo, new int[tileWidth]);
            int[] rowLine = row.getScanline();
            for (int x = 0; x < tileWidth; x++) {
                int unsignedPixelValue = unsignedPixelValues[(y * tileWidth)
                        + x];
                setPixelValue(rowLine, x, unsignedPixelValue);
            }
            writer.writeRow(row);
        }
        writer.end();
        image.flushStream();

        return image;
    }

    /**
     * Draw a coverage data image tile and format as PNG bytes from the flat array
     * of unsigned 16 bit integer pixel values of length tileWidth * tileHeight
     * where each pixel is at: (y * tileWidth) + x
     *
     * @param unsignedPixelValues unsigned 16 bit integer pixel values of length tileWidth *
     *                            tileHeight
     * @param tileWidth           tile width
     * @param tileHeight          tile height
     * @return coverage data image tile bytes
     */
    public byte[] drawTileData(int[] unsignedPixelValues, int tileWidth,
                               int tileHeight) {
        CoverageDataPngImage image = drawTile(unsignedPixelValues, tileWidth,
                tileHeight);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw a coverage data image tile from the double array of unsigned 16 bit
     * integer pixel values formatted as int[row][width]
     *
     * @param unsignedPixelValues unsigned 16 bit integer pixel values as [row][width]
     * @return coverage data image tile
     */
    public CoverageDataPngImage drawTile(int[][] unsignedPixelValues) {

        int tileWidth = unsignedPixelValues[0].length;
        int tileHeight = unsignedPixelValues.length;

        CoverageDataPngImage image = createImage(tileWidth, tileHeight);
        PngWriter writer = image.getWriter();
        for (int y = 0; y < tileHeight; y++) {
            ImageLineInt row = new ImageLineInt(writer.imgInfo, new int[tileWidth]);
            int[] rowLine = row.getScanline();
            for (int x = 0; x < tileWidth; x++) {
                int unsignedPixelValue = unsignedPixelValues[y][x];
                setPixelValue(rowLine, x, unsignedPixelValue);
            }
            writer.writeRow(row);
        }
        writer.end();
        image.flushStream();

        return image;
    }

    /**
     * Draw a coverage data image tile and format as PNG bytes from the double
     * array of unsigned 16 bit integer pixel values formatted as
     * int[row][width]
     *
     * @param unsignedPixelValues unsigned 16 bit integer pixel values as [row][width]
     * @return coverage data image tile bytes
     */
    public byte[] drawTileData(int[][] unsignedPixelValues) {
        CoverageDataPngImage image = drawTile(unsignedPixelValues);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw a coverage data image tile from the flat array of coverage data values of length
     * tileWidth * tileHeight where each coverage data value is at: (y * tileWidth) + x
     *
     * @param griddedTile gridded tile
     * @param values      coverage data values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return coverage data image tile
     */
    public CoverageDataPngImage drawTile(GriddedTile griddedTile, Double[] values,
                                         int tileWidth, int tileHeight) {

        CoverageDataPngImage image = createImage(tileWidth, tileHeight);
        PngWriter writer = image.getWriter();
        for (int y = 0; y < tileHeight; y++) {
            ImageLineInt row = new ImageLineInt(writer.imgInfo, new int[tileWidth]);
            int[] rowLine = row.getScanline();
            for (int x = 0; x < tileWidth; x++) {
                Double value = values[(y * tileWidth) + x];
                short pixelValue = getPixelValue(griddedTile, value);
                setPixelValue(rowLine, x, pixelValue);
            }
            writer.writeRow(row);
        }
        writer.end();
        image.flushStream();

        return image;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] drawTileData(GriddedTile griddedTile, Double[] values,
                               int tileWidth, int tileHeight) {
        CoverageDataPngImage image = drawTile(griddedTile, values, tileWidth,
                tileHeight);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw a coverage data image tile from the double array of unsigned coverage data values
     * formatted as Double[row][width]
     *
     * @param griddedTile gridded tile
     * @param values      coverage data values as [row][width]
     * @return coverage data image tile
     */
    public CoverageDataPngImage drawTile(GriddedTile griddedTile, Double[][] values) {

        int tileWidth = values[0].length;
        int tileHeight = values.length;

        CoverageDataPngImage image = createImage(tileWidth, tileHeight);
        PngWriter writer = image.getWriter();
        for (int y = 0; y < tileHeight; y++) {
            ImageLineInt row = new ImageLineInt(writer.imgInfo, new int[tileWidth]);
            int[] rowLine = row.getScanline();
            for (int x = 0; x < tileWidth; x++) {
                Double value = values[y][x];
                short pixelValue = getPixelValue(griddedTile, value);
                setPixelValue(rowLine, x, pixelValue);
            }
            writer.writeRow(row);
        }
        writer.end();
        image.flushStream();

        return image;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] drawTileData(GriddedTile griddedTile, Double[][] values) {
        CoverageDataPngImage image = drawTile(griddedTile, values);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Create a new 16 bit single channel image
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return image
     */
    public CoverageDataPngImage createImage(int tileWidth, int tileHeight) {
        ImageInfo imageInfo = new ImageInfo(tileWidth, tileHeight, 16, false, true, false);
        CoverageDataPngImage image = new CoverageDataPngImage(imageInfo);
        return image;
    }

    /**
     * Set the pixel value
     *
     * @param row        image line row
     * @param x          x coordinate
     * @param pixelValue pixel value
     */
    public void setPixelValue(ImageLineInt row, int x,
                              short pixelValue) {
        setPixelValue(row.getScanline(), x, pixelValue);
    }

    /**
     * Set the pixel value
     *
     * @param row        row array
     * @param x          x coordinate
     * @param pixelValue pixel value
     */
    public void setPixelValue(int[] row, int x,
                              short pixelValue) {
        row[x] = pixelValue;
    }

    /**
     * Set the pixel value
     *
     * @param row                image line row
     * @param x                  x coordinate
     * @param unsignedPixelValue unsigned pixel value
     */
    public void setPixelValue(ImageLineInt row, int x,
                              int unsignedPixelValue) {
        short pixelValue = getPixelValue(unsignedPixelValue);
        setPixelValue(row, x, pixelValue);
    }

    /**
     * Set the pixel value
     *
     * @param row                row array
     * @param x                  x coordinate
     * @param unsignedPixelValue unsigned pixel value
     */
    public void setPixelValue(int[] row, int x,
                              int unsignedPixelValue) {
        short pixelValue = getPixelValue(unsignedPixelValue);
        setPixelValue(row, x, pixelValue);
    }

    /**
     * Create the coverage data tile table with metadata and extension
     *
     * @param geoPackage
     * @param tableName
     * @param contentsBoundingBox
     * @param contentsSrsId
     * @param tileMatrixSetBoundingBox
     * @param tileMatrixSetSrsId
     * @return coverage data
     */
    public static CoverageDataPng createTileTableWithMetadata(
            GeoPackage geoPackage, String tableName,
            BoundingBox contentsBoundingBox, long contentsSrsId,
            BoundingBox tileMatrixSetBoundingBox, long tileMatrixSetSrsId) {

        CoverageDataPng coverageData = (CoverageDataPng) CoverageData
                .createTileTableWithMetadata(geoPackage, tableName,
                        contentsBoundingBox, contentsSrsId,
                        tileMatrixSetBoundingBox, tileMatrixSetSrsId,
                        GriddedCoverageDataType.INTEGER);
        return coverageData;
    }

}
