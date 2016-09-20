package mil.nga.geopackage.extension.elevation;

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
import mil.nga.tiff.FileDirectories;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import mil.nga.tiff.util.TiffConstants;

/**
 * Tiled Gridded Elevation Data, TIFF Encoding, Extension
 *
 * @author osbornb
 * @since 1.3.1
 */
public class ElevationTilesTiff extends ElevationTilesCommon<ElevationTiffImage> {

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
    public ElevationTiffImage createElevationImage(TileRow tileRow) {
        return new ElevationTiffImage(tileRow);
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
                                    ElevationTiffImage image, int x, int y) {
        Double elevation = null;
        if (image.getDirectory() != null) {
            float pixelValue = image.getPixel(x, y);
            elevation = getElevationValue(griddedTile, pixelValue);
        } else {
            elevation = getElevationValue(griddedTile, image.getImageBytes(), x, y);
        }
        return elevation;
    }

    /**
     * Get the pixel value as a float from the image and the coordinate
     *
     * @param imageBytes image bytes
     * @param x          x coordinate
     * @param y          y coordinate
     * @return float pixel value
     */
    public float getPixelValue(byte[] imageBytes, int x, int y) {

        FileDirectories directories = TiffReader.readTiff(imageBytes);
        FileDirectory directory = directories.getFileDirectory();
        validateImageType(directory);
        Rasters rasters = directory.readRasters();
        float pixelValue = rasters.getFirstPixelSample(x, y).floatValue();

        return pixelValue;
    }

    /**
     * Get the pixel values of the image as floats
     *
     * @param imageBytes image bytes
     * @return float pixel values
     */
    public float[] getPixelValues(byte[] imageBytes) {
        FileDirectories directories = TiffReader.readTiff(imageBytes);
        FileDirectory directory = directories.getFileDirectory();
        validateImageType(directory);
        Rasters rasters = directory.readRasters();
        Number[] values = rasters.getSampleValues()[0];
        float[] pixels = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            pixels[i] = values[i].floatValue();
        }
        return pixels;
    }

    /**
     * Validate that the image type
     *
     * @param directory file directory
     */
    public void validateImageType(FileDirectory directory) {
        if (directory == null) {
            throw new GeoPackageException("The image is null");
        }

        Integer samplesPerPixel = directory.getSamplesPerPixel();
        Integer bitsPerSample = null;
        if (directory.getBitsPerSample() != null && !directory.getBitsPerSample().isEmpty()) {
            bitsPerSample = directory.getBitsPerSample().get(0);
        }
        Integer sampleFormat = null;
        if (directory.getSampleFormat() != null && !directory.getSampleFormat().isEmpty()) {
            sampleFormat = directory.getSampleFormat().get(0);
        }

        if (samplesPerPixel == null || samplesPerPixel != 1
                || bitsPerSample == null || bitsPerSample != 32
                || sampleFormat == null || sampleFormat != TiffConstants.SAMPLE_FORMAT_FLOAT) {
            throw new GeoPackageException(
                    "The elevation tile is expected to be a single sample 32 bit float. Samples Per Pixel: "
                            + samplesPerPixel
                            + ", Bits Per Sample: " + bitsPerSample
                            + ", Sample Format: " + sampleFormat);
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
        float pixelValue = getPixelValue(imageBytes, x, y);
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
        float[] pixelValues = getPixelValues(imageBytes);
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
    public ElevationTiffImage drawTile(float[] pixelValues, int tileWidth,
                                       int tileHeight) {

        ElevationTiffImage image = createImage(tileWidth, tileHeight);
        for (int y = 0; y < tileHeight; y++) {
            for (int x = 0; x < tileWidth; x++) {
                float pixelValue = pixelValues[(y * tileWidth)
                        + x];
                setPixelValue(image, x, y, pixelValue);
            }
        }
        image.writeTiff();

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
        ElevationTiffImage image = drawTile(pixelValues, tileWidth, tileHeight);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw an elevation image tile from the double array of float pixel values
     * formatted as float[row][width]
     *
     * @param pixelValues float pixel values as [row][width]
     * @return elevation image tile
     */
    public ElevationTiffImage drawTile(float[][] pixelValues) {

        int tileWidth = pixelValues[0].length;
        int tileHeight = pixelValues.length;

        ElevationTiffImage image = createImage(tileWidth, tileHeight);
        for (int y = 0; y < tileHeight; y++) {
            for (int x = 0; x < tileWidth; x++) {
                float pixelValue = pixelValues[y][x];
                setPixelValue(image, x, y, pixelValue);
            }
        }
        image.writeTiff();

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
        ElevationTiffImage image = drawTile(pixelValues);
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
    public ElevationTiffImage drawTile(GriddedTile griddedTile, Double[] elevations,
                                       int tileWidth, int tileHeight) {

        ElevationTiffImage image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                Double elevation = elevations[(y * tileWidth) + x];
                float pixelValue = getPixelValue(griddedTile, elevation);
                setPixelValue(image, x, y, pixelValue);
            }
        }
        image.writeTiff();

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
        ElevationTiffImage image = drawTile(griddedTile, elevations, tileWidth,
                tileHeight);
        byte[] bytes = image.getImageBytes();
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
    public ElevationTiffImage drawTile(GriddedTile griddedTile, Double[][] elevations) {

        int tileWidth = elevations[0].length;
        int tileHeight = elevations.length;

        ElevationTiffImage image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                Double elevation = elevations[y][x];
                short pixelValue = getPixelValue(griddedTile, elevation);
                setPixelValue(image, x, y, pixelValue);
            }
        }
        image.writeTiff();

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
        ElevationTiffImage image = drawTile(griddedTile, elevations);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Create a new image
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     * @return image
     */
    public ElevationTiffImage createImage(int tileWidth, int tileHeight) {

        FileDirectory fileDirectory = new FileDirectory();
        // TODO populate entries
        fileDirectory.setImageHeight(tileHeight);
        fileDirectory.setImageWidth(tileWidth);
        Rasters rasters = new Rasters(tileWidth, tileHeight, 1);
        fileDirectory.setWriteRasters(rasters);

        ElevationTiffImage image = new ElevationTiffImage(fileDirectory);

        return image;
    }

    /**
     * Set the pixel value into the image
     *
     * @param image      image
     * @param x          x coordinate
     * @param y          y coordinate
     * @param pixelValue pixel value
     */
    public void setPixelValue(ElevationTiffImage image, int x, int y,
                              float pixelValue) {
        image.getRasters().setFirstPixelSample(x, y, pixelValue);
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
