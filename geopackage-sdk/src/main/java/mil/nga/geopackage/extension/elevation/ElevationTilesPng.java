package mil.nga.geopackage.extension.elevation;

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
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Tiled Gridded Elevation Data, PNG Encoding, Extension
 *
 * @author osbornb
 * @since 1.3.1
 */
public class ElevationTilesPng extends ElevationTilesCommon<ElevationPngImage> {

    /**
     * Constructor
     *
     * @param geoPackage        GeoPackage
     * @param tileDao           tile dao
     * @param width             elevation response width
     * @param height            elevation response height
     * @param requestProjection request projection
     */
    public ElevationTilesPng(GeoPackage geoPackage, TileDao tileDao,
                             Integer width, Integer height, Projection requestProjection) {
        super(geoPackage, tileDao, width,
                height, requestProjection);
    }

    /**
     * Constructor, use the elevation tables pixel tile size as the request size
     * width and height
     *
     * @param geoPackage GeoPackage
     * @param tileDao    tile dao
     */
    public ElevationTilesPng(GeoPackage geoPackage, TileDao tileDao) {
        this(geoPackage, tileDao, null, null, tileDao.getProjection());
    }

    /**
     * Constructor, use the elevation tables pixel tile size as the request size
     * width and height, request as the specified projection
     *
     * @param geoPackage        GeoPackage
     * @param tileDao           tile dao
     * @param requestProjection request projection
     */
    public ElevationTilesPng(GeoPackage geoPackage, TileDao tileDao,
                             Projection requestProjection) {
        this(geoPackage, tileDao, null, null, requestProjection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElevationPngImage createElevationImage(TileRow tileRow) {
        return new ElevationPngImage(tileRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getElevationValue(GriddedTile griddedTile, TileRow tileRow,
                                    int x, int y) {
        byte[] imageBytes = tileRow.getTileData();
        double elevation = getElevationValue(griddedTile, imageBytes, x, y);
        return elevation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getElevationValue(GriddedTile griddedTile,
                                    ElevationPngImage image, int x, int y) {
        Double elevation = null;
        if (image.getReader() != null) {
            int pixelValue = image.getPixel(x, y);
            elevation = getElevationValue(griddedTile, pixelValue);
        } else {
            elevation = getElevationValue(griddedTile, image.getImageBytes(), x, y);
        }
        return elevation;
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
                    "The elevation tile is expected to be a single channel 16 bit unsigned short, channels: "
                            + reader.imgInfo.channels + ", bits: " + reader.imgInfo.bitDepth);
        }
    }

    /**
     * Get the elevation value
     *
     * @param griddedTile gridded tile
     * @param imageBytes  image bytes
     * @param x           x coordinate
     * @param y           y coordinate
     * @return elevation value
     */
    public Double getElevationValue(GriddedTile griddedTile,
                                    byte[] imageBytes, int x, int y) {
        int pixelValue = getPixelValue(imageBytes, x, y);
        Double elevation = getElevationValue(griddedTile, pixelValue);
        return elevation;
    }

    /**
     * Get the elevation values
     *
     * @param griddedTile gridded tile
     * @param imageBytes  image bytes
     * @return elevation values
     */
    public Double[] getElevationValues(GriddedTile griddedTile,
                                       byte[] imageBytes) {
        int[] pixelValues = getPixelValues(imageBytes);
        Double[] elevations = getElevationValues(griddedTile, pixelValues);
        return elevations;
    }

    /**
     * Draw an elevation image tile from the flat array of "unsigned short"
     * pixel values of length tileWidth * tileHeight where each pixel is at: (y
     * * tileWidth) + x
     *
     * @param pixelValues "unsigned short" pixel values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return elevation image tile
     */
    public ElevationPngImage drawTile(short[] pixelValues, int tileWidth,
                                      int tileHeight) {

        ElevationPngImage image = createImage(tileWidth, tileHeight);
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
     * Draw an elevation image tile and format as PNG bytes from the flat array
     * of "unsigned short" pixel values of length tileWidth * tileHeight where
     * each pixel is at: (y * tileWidth) + x
     *
     * @param pixelValues "unsigned short" pixel values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return elevation image tile bytes
     */
    public byte[] drawTileData(short[] pixelValues, int tileWidth,
                               int tileHeight) {
        ElevationPngImage image = drawTile(pixelValues, tileWidth, tileHeight);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw an elevation tile from the double array of "unsigned short" pixel
     * values formatted as short[row][width]
     *
     * @param pixelValues "unsigned short" pixel values as [row][width]
     * @return elevation image tile
     */
    public ElevationPngImage drawTile(short[][] pixelValues) {

        int tileWidth = pixelValues[0].length;
        int tileHeight = pixelValues.length;

        ElevationPngImage image = createImage(tileWidth, tileHeight);
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
     * Draw an elevation tile and format as PNG bytes from the double array of
     * "unsigned short" pixel values formatted as short[row][width]
     *
     * @param pixelValues "unsigned short" pixel values as [row][width]
     * @return elevation image tile bytes
     */
    public byte[] drawTileData(short[][] pixelValues) {
        ElevationPngImage image = drawTile(pixelValues);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw an elevation image tile from the flat array of unsigned 16 bit
     * integer pixel values of length tileWidth * tileHeight where each pixel is
     * at: (y * tileWidth) + x
     *
     * @param unsignedPixelValues unsigned 16 bit integer pixel values of length tileWidth *
     *                            tileHeight
     * @param tileWidth           tile width
     * @param tileHeight          tile height
     * @return elevation image tile
     */
    public ElevationPngImage drawTile(int[] unsignedPixelValues, int tileWidth,
                                      int tileHeight) {

        ElevationPngImage image = createImage(tileWidth, tileHeight);
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
     * Draw an elevation image tile and format as PNG bytes from the flat array
     * of unsigned 16 bit integer pixel values of length tileWidth * tileHeight
     * where each pixel is at: (y * tileWidth) + x
     *
     * @param unsignedPixelValues unsigned 16 bit integer pixel values of length tileWidth *
     *                            tileHeight
     * @param tileWidth           tile width
     * @param tileHeight          tile height
     * @return elevation image tile bytes
     */
    public byte[] drawTileData(int[] unsignedPixelValues, int tileWidth,
                               int tileHeight) {
        ElevationPngImage image = drawTile(unsignedPixelValues, tileWidth,
                tileHeight);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw an elevation image tile from the double array of unsigned 16 bit
     * integer pixel values formatted as int[row][width]
     *
     * @param unsignedPixelValues unsigned 16 bit integer pixel values as [row][width]
     * @return elevation image tile
     */
    public ElevationPngImage drawTile(int[][] unsignedPixelValues) {

        int tileWidth = unsignedPixelValues[0].length;
        int tileHeight = unsignedPixelValues.length;

        ElevationPngImage image = createImage(tileWidth, tileHeight);
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
     * Draw an elevation image tile and format as PNG bytes from the double
     * array of unsigned 16 bit integer pixel values formatted as
     * int[row][width]
     *
     * @param unsignedPixelValues unsigned 16 bit integer pixel values as [row][width]
     * @return elevation image tile bytes
     */
    public byte[] drawTileData(int[][] unsignedPixelValues) {
        ElevationPngImage image = drawTile(unsignedPixelValues);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw an elevation image tile from the flat array of elevations of length
     * tileWidth * tileHeight where each elevation is at: (y * tileWidth) + x
     *
     * @param griddedTile gridded tile
     * @param elevations  elevations of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return elevation image tile
     */
    public ElevationPngImage drawTile(GriddedTile griddedTile, Double[] elevations,
                                      int tileWidth, int tileHeight) {

        ElevationPngImage image = createImage(tileWidth, tileHeight);
        PngWriter writer = image.getWriter();
        for (int y = 0; y < tileHeight; y++) {
            ImageLineInt row = new ImageLineInt(writer.imgInfo, new int[tileWidth]);
            int[] rowLine = row.getScanline();
            for (int x = 0; x < tileWidth; x++) {
                Double elevation = elevations[(y * tileWidth) + x];
                short pixelValue = getPixelValue(griddedTile, elevation);
                setPixelValue(rowLine, x, pixelValue);
            }
            writer.writeRow(row);
        }
        writer.end();
        image.flushStream();

        return image;
    }

    /**
     * Draw an elevation image tile and format as PNG bytes from the flat array
     * of elevations of length tileWidth * tileHeight where each elevation is
     * at: (y * tileWidth) + x
     *
     * @param griddedTile gridded tile
     * @param elevations  elevations of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return elevation image tile bytes
     */
    public byte[] drawTileData(GriddedTile griddedTile, Double[] elevations,
                               int tileWidth, int tileHeight) {
        ElevationPngImage image = drawTile(griddedTile, elevations, tileWidth,
                tileHeight);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw an elevation image tile from the double array of unsigned elevations
     * formatted as Double[row][width]
     *
     * @param griddedTile gridded tile
     * @param elevations  elevations as [row][width]
     * @return elevation image tile
     */
    public ElevationPngImage drawTile(GriddedTile griddedTile, Double[][] elevations) {

        int tileWidth = elevations[0].length;
        int tileHeight = elevations.length;

        ElevationPngImage image = createImage(tileWidth, tileHeight);
        PngWriter writer = image.getWriter();
        for (int y = 0; y < tileHeight; y++) {
            ImageLineInt row = new ImageLineInt(writer.imgInfo, new int[tileWidth]);
            int[] rowLine = row.getScanline();
            for (int x = 0; x < tileWidth; x++) {
                Double elevation = elevations[y][x];
                short pixelValue = getPixelValue(griddedTile, elevation);
                setPixelValue(rowLine, x, pixelValue);
            }
            writer.writeRow(row);
        }
        writer.end();
        image.flushStream();

        return image;
    }

    /**
     * Draw an elevation image tile and format as PNG bytes from the double
     * array of unsigned elevations formatted as Double[row][width]
     *
     * @param griddedTile gridded tile
     * @param elevations  elevations as [row][width]
     * @return elevation image tile bytes
     */
    public byte[] drawTileData(GriddedTile griddedTile, Double[][] elevations) {
        ElevationPngImage image = drawTile(griddedTile, elevations);
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
    public ElevationPngImage createImage(int tileWidth, int tileHeight) {
        ImageInfo imageInfo = new ImageInfo(tileWidth, tileHeight, 16, false, true, false);
        ElevationPngImage image = new ElevationPngImage(imageInfo);
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
     * Create the elevation tile table with metadata and extension
     *
     * @param geoPackage
     * @param tableName
     * @param contentsBoundingBox
     * @param contentsSrsId
     * @param tileMatrixSetBoundingBox
     * @param tileMatrixSetSrsId
     * @return elevation tiles
     */
    public static ElevationTilesPng createTileTableWithMetadata(
            GeoPackage geoPackage, String tableName,
            BoundingBox contentsBoundingBox, long contentsSrsId,
            BoundingBox tileMatrixSetBoundingBox, long tileMatrixSetSrsId) {

        TileMatrixSet tileMatrixSet = ElevationTilesCore
                .createTileTableWithMetadata(geoPackage, tableName,
                        contentsBoundingBox, contentsSrsId,
                        tileMatrixSetBoundingBox, tileMatrixSetSrsId);
        TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
        ElevationTilesPng elevationTiles = new ElevationTilesPng(geoPackage, tileDao);
        elevationTiles.getOrCreate();

        return elevationTiles;
    }

}
