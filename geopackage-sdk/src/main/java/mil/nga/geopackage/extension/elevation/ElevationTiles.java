package mil.nga.geopackage.extension.elevation;

import android.graphics.Bitmap;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.property.GeoPackageProperties;
import mil.nga.geopackage.property.PropertyConstants;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Tiled Gridded Elevation Data Extension
 *
 * @author osbornb
 * @since 1.3.1
 */
public class ElevationTiles extends ElevationTilesCommon {

    /**
     * Extension name without the author
     */
    public static final String EXTENSION_NAME_NO_AUTHOR = CORE_EXTENSION_NAME_NO_AUTHOR;

    /**
     * Extension, with author and name
     */
    public static final String EXTENSION_NAME = CORE_EXTENSION_NAME;

    /**
     * Extension definition URL
     */
    public static final String EXTENSION_DEFINITION = GeoPackageProperties
            .getProperty(PropertyConstants.EXTENSIONS, EXTENSION_NAME_NO_AUTHOR);

    /**
     * Constructor
     *
     * @param geoPackage        GeoPackage
     * @param tileDao           tile dao
     * @param width             elevation response width
     * @param height            elevation response height
     * @param requestProjection request projection
     */
    public ElevationTiles(GeoPackage geoPackage, TileDao tileDao,
                          Integer width, Integer height, Projection requestProjection) {
        super(geoPackage, EXTENSION_NAME, EXTENSION_DEFINITION, tileDao, width,
                height, requestProjection);
    }

    /**
     * Constructor, use the elevation tables pixel tile size as the request size
     * width and height
     *
     * @param geoPackage GeoPackage
     * @param tileDao    tile dao
     */
    public ElevationTiles(GeoPackage geoPackage, TileDao tileDao) {
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
    public ElevationTiles(GeoPackage geoPackage, TileDao tileDao,
                          Projection requestProjection) {
        this(geoPackage, tileDao, null, null, requestProjection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getElevationValue(GriddedTile griddedTile, TileRow tileRow,
                                    int x, int y) {
        Bitmap image = tileRow.getTileDataBitmap();
        double elevation = getElevationValue(griddedTile, image, x, y);
        return elevation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getElevationValue(GriddedTile griddedTile,
                                    ElevationImage image, int x, int y) {
        return getElevationValue(griddedTile, image.getImage(), x, y);
    }

    /**
     * Get the pixel value as an "unsigned short" from the bitmap and the
     * coordinate
     *
     * @param image image
     * @param x     x coordinate
     * @param y     y coordinate
     * @return "unsigned short" pixel value
     */
    public short getPixelValue(Bitmap image, int x, int y) {
        validateImageType(image);
        /*
        Object pixelData = raster.getDataElements(x, y, null);
        short sdata[] = (short[]) pixelData;
        if (sdata.length != 1) {
            throw new UnsupportedOperationException(
                    "This method is not supported by this color model");
        }
        short pixelValue = sdata[0];

        return pixelValue;
        */
        return 0; //TODO
    }

    /**
     * Get the pixel value as a 16 bit unsigned integer value
     *
     * @param image image
     * @param x     x coordinate
     * @param y     y coordinate
     * @return unsigned integer pixel value
     */
    public int getUnsignedPixelValue(Bitmap image, int x, int y) {
        short pixelValue = getPixelValue(image, x, y);
        int unsignedPixelValue = getUnsignedPixelValue(pixelValue);
        return unsignedPixelValue;
    }

    /**
     * Get the pixel values of the image as "unsigned shorts"
     *
     * @param image image
     * @return "unsigned short" pixel values
     */
    public short[] getPixelValues(Bitmap image) {
        validateImageType(image);
        //DataBufferUShort buffer = (DataBufferUShort) raster.getDataBuffer();
        //short[] pixelValues = buffer.getData();
        //return pixelValues;
        return null; // TODO
    }

    /**
     * Get the pixel values of the image as 16 bit unsigned integer values
     *
     * @param image image
     * @return unsigned integer pixel values
     */
    public int[] getUnsignedPixelValues(Bitmap image) {
        short[] pixelValues = getPixelValues(image);
        int[] unsignedPixelValues = getUnsignedPixelValues(pixelValues);
        return unsignedPixelValues;
    }

    /**
     * Validate that the image type is an unsigned short
     *
     * @param image tile image
     */
    public void validateImageType(Bitmap image) {
        if (image == null) {
            throw new GeoPackageException("The image is null");
        }
        //if (image.getColorModel().getTransferType() != DataBuffer.TYPE_USHORT) {
        //    throw new GeoPackageException(
        //            "The elevation tile is expected to be a 16 bit unsigned short, actual: "
        //                    + image.getColorModel().getTransferType());
        //}
        // TODO
    }

    /**
     * Get the elevation value
     *
     * @param griddedTile gridded tile
     * @param image       image
     * @param x           x coordinate
     * @param y           y coordinate
     * @return elevation value
     */
    public Double getElevationValue(GriddedTile griddedTile,
                                    Bitmap image, int x, int y) {
        short pixelValue = getPixelValue(image, x, y);
        Double elevation = getElevationValue(griddedTile, pixelValue);
        return elevation;
    }

    /**
     * Get the elevation values
     *
     * @param griddedTile gridded tile
     * @param image       tile image
     * @return elevation values
     */
    public Double[] getElevationValues(GriddedTile griddedTile,
                                       Bitmap image) {
        short[] pixelValues = getPixelValues(image);
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
    public Bitmap drawTile(short[] pixelValues, int tileWidth,
                           int tileHeight) {

        Bitmap image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                short pixelValue = pixelValues[(y * tileWidth) + x];
                setPixelValue(image, x, y, pixelValue);
            }
        }

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
        Bitmap image = drawTile(pixelValues, tileWidth, tileHeight);
        byte[] bytes = getImageBytes(image);
        return bytes;
    }

    /**
     * Draw an elevation tile from the double array of "unsigned short" pixel
     * values formatted as short[row][width]
     *
     * @param pixelValues "unsigned short" pixel values as [row][width]
     * @return elevation image tile
     */
    public Bitmap drawTile(short[][] pixelValues) {

        int tileWidth = pixelValues[0].length;
        int tileHeight = pixelValues.length;

        Bitmap image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                short pixelValue = pixelValues[y][x];
                setPixelValue(image, x, y, pixelValue);
            }
        }

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
        Bitmap image = drawTile(pixelValues);
        byte[] bytes = getImageBytes(image);
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
    public Bitmap drawTile(int[] unsignedPixelValues, int tileWidth,
                           int tileHeight) {

        Bitmap image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                int unsignedPixelValue = unsignedPixelValues[(y * tileWidth)
                        + x];
                setPixelValue(image, x, y, unsignedPixelValue);
            }
        }

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
        Bitmap image = drawTile(unsignedPixelValues, tileWidth,
                tileHeight);
        byte[] bytes = getImageBytes(image);
        return bytes;
    }

    /**
     * Draw an elevation image tile from the double array of unsigned 16 bit
     * integer pixel values formatted as int[row][width]
     *
     * @param unsignedPixelValues unsigned 16 bit integer pixel values as [row][width]
     * @return elevation image tile
     */
    public Bitmap drawTile(int[][] unsignedPixelValues) {

        int tileWidth = unsignedPixelValues[0].length;
        int tileHeight = unsignedPixelValues.length;

        Bitmap image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                int unsignedPixelValue = unsignedPixelValues[y][x];
                setPixelValue(image, x, y, unsignedPixelValue);
            }
        }

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
        Bitmap image = drawTile(unsignedPixelValues);
        byte[] bytes = getImageBytes(image);
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
    public Bitmap drawTile(GriddedTile griddedTile, Double[] elevations,
                           int tileWidth, int tileHeight) {

        Bitmap image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                Double elevation = elevations[(y * tileWidth) + x];
                short pixelValue = getPixelValue(griddedTile, elevation);
                setPixelValue(image, x, y, pixelValue);
            }
        }

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
        Bitmap image = drawTile(griddedTile, elevations, tileWidth,
                tileHeight);
        byte[] bytes = getImageBytes(image);
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
    public Bitmap drawTile(GriddedTile griddedTile, Double[][] elevations) {

        int tileWidth = elevations[0].length;
        int tileHeight = elevations.length;

        Bitmap image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                Double elevation = elevations[y][x];
                short pixelValue = getPixelValue(griddedTile, elevation);
                setPixelValue(image, x, y, pixelValue);
            }
        }

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
        Bitmap image = drawTile(griddedTile, elevations);
        byte[] bytes = getImageBytes(image);
        return bytes;
    }

    /**
     * Create a new unsigned 16 bit short grayscale image
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return image
     */
    public Bitmap createImage(int tileWidth, int tileHeight) {
        //return new Bitmap(tileWidth, tileHeight,
        //        BufferedImage.TYPE_USHORT_GRAY);
        return null; // TODO
    }

    /**
     * Get the image as PNG bytes
     *
     * @param image image
     * @return image bytes
     */
    public byte[] getImageBytes(Bitmap image) {
        byte[] bytes = null;
        /*
        try {
            bytes = ImageUtils.writeImageToBytes(image,
                    ImageUtils.IMAGE_FORMAT_PNG);
        } catch (IOException e) {
            throw new GeoPackageException("Failed to write image to "
                    + ImageUtils.IMAGE_FORMAT_PNG + " bytes", e);
        }
        */ // TODO
        return bytes;
    }

    /**
     * Set the "unsigned short" pixel value into the image
     *
     * @param image      image
     * @param x          x coordinate
     * @param y          y coordinate
     * @param pixelValue "unsigned short" pixel value
     */
    public void setPixelValue(Bitmap image, int x, int y,
                              short pixelValue) {
        //short data[] = new short[] { pixelValue };
        //image.setDataElements(x, y, data);
        // TODO
    }

    /**
     * Set the unsigned 16 bit integer pixel value into the image
     *
     * @param image              image
     * @param x                  x coordinate
     * @param y                  y coordinate
     * @param unsignedPixelValue unsigned 16 bit integer pixel value
     */
    public void setPixelValue(Bitmap image, int x, int y,
                              int unsignedPixelValue) {
        short pixelValue = getPixelValue(unsignedPixelValue);
        setPixelValue(image, x, y, pixelValue);
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
    public static ElevationTiles createTileTableWithMetadata(
            GeoPackage geoPackage, String tableName,
            BoundingBox contentsBoundingBox, long contentsSrsId,
            BoundingBox tileMatrixSetBoundingBox, long tileMatrixSetSrsId) {

        TileMatrixSet tileMatrixSet = ElevationTilesCore
                .createTileTableWithMetadata(geoPackage, tableName,
                        contentsBoundingBox, contentsSrsId,
                        tileMatrixSetBoundingBox, tileMatrixSetSrsId);
        TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
        ElevationTiles elevationTiles = new ElevationTiles(geoPackage, tileDao);
        elevationTiles.getOrCreate();

        return elevationTiles;
    }

}
