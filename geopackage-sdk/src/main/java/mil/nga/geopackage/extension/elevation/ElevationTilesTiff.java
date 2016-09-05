package mil.nga.geopackage.extension.elevation;

import android.graphics.Bitmap;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.property.GeoPackageProperties;
import mil.nga.geopackage.property.PropertyConstants;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Tiled Gridded Elevation Data, TIFF Encoding, Extension
 *
 * @author osbornb
 * @since 1.3.1
 */
public class ElevationTilesTiff extends ElevationTilesCommon {

    /**
     * Extension name without the author
     */
    public static final String EXTENSION_NAME_NO_AUTHOR = "elevation_tiles_tiff";

    /**
     * Extension, with author and name
     */
    public static final String EXTENSION_NAME = Extensions.buildExtensionName(
            EXTENSION_AUTHOR, EXTENSION_NAME_NO_AUTHOR);

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
    public ElevationTilesTiff(GeoPackage geoPackage, TileDao tileDao,
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
    public ElevationTilesTiff(GeoPackage geoPackage, TileDao tileDao) {
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
    public ElevationTilesTiff(GeoPackage geoPackage, TileDao tileDao,
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
     * Get the pixel value as a float from the image and the coordinate
     *
     * @param image image
     * @param x     x coordinate
     * @param y     y coordinate
     * @return float pixel value
     */
    public float getPixelValue(Bitmap image, int x, int y) {
        validateImageType(image);
        //Object pixelData = raster.getDataElements(x, y, null);
        //float sdata[] = (float[]) pixelData;
        //if (sdata.length != 1) {
        //    throw new UnsupportedOperationException(
        //            "This method is not supported by this color model");
        //}
        //float pixelValue = sdata[0];

        //return pixelValue;
        return 0; // TODO
    }

    /**
     * Get the pixel values of the image as floats
     *
     * @param image image
     * @return float pixel values
     */
    public float[] getPixelValues(Bitmap image) {
        validateImageType(image);
        //DataBufferFloat buffer = (DataBufferFloat) raster.getDataBuffer();
        //float[] pixelValues = buffer.getData();
        //return pixelValues;
        return null; // TODO
    }

    /**
     * Validate that the image type is float
     *
     * @param image tile image
     */
    public void validateImageType(Bitmap image) {
        if (image == null) {
            throw new GeoPackageException("The image is null");
        }
        //if (image.getColorModel().getTransferType() != DataBuffer.TYPE_FLOAT) {
        //    throw new GeoPackageException(
        //            "The elevation tile is expected to be a 32 bit float, actual: "
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
        float pixelValue = getPixelValue(image, x, y);
        Double elevation = getElevationValue(griddedTile, pixelValue);
        return elevation;
    }

    /**
     * Get the elevation values
     *
     * @param griddedTile gridded tile
     * @param image       image
     * @return elevation values
     */
    public Double[] getElevationValues(GriddedTile griddedTile,
                                       Bitmap image) {
        float[] pixelValues = getPixelValues(image);
        Double[] elevations = getElevationValues(griddedTile, pixelValues);
        return elevations;
    }

    /**
     * Draw an elevation image tile from the flat array of float pixel values of
     * length tileWidth * tileHeight where each pixel is at: (y * tileWidth) + x
     *
     * @param pixelValues float pixel values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return elevation image tile
     */
    public Bitmap drawTile(float[] pixelValues, int tileWidth,
                           int tileHeight) {

        Bitmap image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                float pixelValue = pixelValues[(y * tileWidth) + x];
                setPixelValue(image, x, y, pixelValue);
            }
        }

        return image;
    }

    /**
     * Draw an elevation image tile and format as TIFF bytes from the flat array
     * of float pixel values of length tileWidth * tileHeight where each pixel
     * is at: (y * tileWidth) + x
     *
     * @param pixelValues float pixel values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return elevation image tile bytes
     */
    public byte[] drawTileData(float[] pixelValues, int tileWidth,
                               int tileHeight) {
        Bitmap image = drawTile(pixelValues, tileWidth, tileHeight);
        byte[] bytes = getImageBytes(image);
        return bytes;
    }

    /**
     * Draw an elevation image tile from the double array of float pixel values
     * formatted as float[row][width]
     *
     * @param pixelValues float pixel values as [row][width]
     * @return elevation image tile
     */
    public Bitmap drawTile(float[][] pixelValues) {

        int tileWidth = pixelValues[0].length;
        int tileHeight = pixelValues.length;

        Bitmap image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                float pixelValue = pixelValues[y][x];
                setPixelValue(image, x, y, pixelValue);
            }
        }

        return image;
    }

    /**
     * Draw an elevation image tile and format as TIFF bytes from the double
     * array of float pixel values formatted as float[row][width]
     *
     * @param pixelValues float pixel values as [row][width]
     * @return elevation image tile bytes
     */
    public byte[] drawTileData(float[][] pixelValues) {
        Bitmap image = drawTile(pixelValues);
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
                float pixelValue = getPixelValue(griddedTile, elevation);
                setPixelValue(image, x, y, pixelValue);
            }
        }

        return image;
    }

    /**
     * Draw an elevation image tile and format as TIFF bytes from the flat array
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
     * Draw an elevation image tile from the double array of elevations
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
     * Draw an elevation image tile and format as TIFF bytes from the double
     * array of elevations formatted as Double[row][width]
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
     * Create a new image
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return image
     */
    public Bitmap createImage(int tileWidth, int tileHeight) {

        /*
        SampleModel sampleModel = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_FLOAT, tileWidth, tileHeight, 1, tileWidth,
                new int[] { 0 });
        DataBuffer buffer = new DataBufferFloat(tileWidth * tileHeight);
        WritableRaster raster = Raster.createWritableRaster(sampleModel,
                buffer, null);
        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel colorModel = new ComponentColorModel(colorSpace, false,
                false, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
        BufferedImage image = new BufferedImage(colorModel, raster,
                colorModel.isAlphaPremultiplied(), null);
                */

        return null; // TODO
    }

    /**
     * Get the image as TIFF bytes
     *
     * @param image image
     * @return image bytes
     */
    public byte[] getImageBytes(Bitmap image) {
        byte[] bytes = null;
        /*
        try {
            bytes = ImageUtils.writeImageToBytes(image,
                    ImageUtils.IMAGE_FORMAT_TIFF);
        } catch (IOException e) {
            throw new GeoPackageException("Failed to write image to "
                    + ImageUtils.IMAGE_FORMAT_TIFF + " bytes", e);
        }*/ // TODO
        return bytes;
    }

    /**
     * Set the pixel value into the image
     *
     * @param image      image
     * @param x          x coordinate
     * @param y          y coordinate
     * @param pixelValue pixel value
     */
    public void setPixelValue(Bitmap image, int x, int y,
                              float pixelValue) {
        //float data[] = new float[] { pixelValue };
        //raster.setDataElements(x, y, data);
        // TODO
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
    public static ElevationTilesTiff createTileTableWithMetadata(
            GeoPackage geoPackage, String tableName,
            BoundingBox contentsBoundingBox, long contentsSrsId,
            BoundingBox tileMatrixSetBoundingBox, long tileMatrixSetSrsId) {

        TileMatrixSet tileMatrixSet = ElevationTilesCore
                .createTileTableWithMetadata(geoPackage, tableName,
                        contentsBoundingBox, contentsSrsId,
                        tileMatrixSetBoundingBox, tileMatrixSetSrsId);
        TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
        ElevationTilesTiff elevationTiles = new ElevationTilesTiff(geoPackage,
                tileDao);
        elevationTiles.getOrCreate();

        return elevationTiles;
    }

}
