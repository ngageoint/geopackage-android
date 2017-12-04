package mil.nga.geopackage.extension.coverage;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.TiffReader;
import mil.nga.tiff.util.TiffConstants;

/**
 * Tiled Gridded Coverage Data, TIFF Encoding, Extension
 *
 * @author osbornb
 * @since 2.0.1
 */
public class CoverageDataTiff extends CoverageData<CoverageDataTiffImage> {

    /**
     * Single sample coverage data
     */
    public static final int SAMPLES_PER_PIXEL = 1;

    /**
     * Bits per value for floating point coverage data
     */
    public static final int BITS_PER_SAMPLE = 32;

    /**
     * Constructor
     *
     * @param geoPackage        GeoPackage
     * @param tileDao           tile dao
     * @param width             coverage data response width
     * @param height            coverage data response height
     * @param requestProjection request projection
     */
    public CoverageDataTiff(GeoPackage geoPackage, TileDao tileDao,
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
    public CoverageDataTiff(GeoPackage geoPackage, TileDao tileDao) {
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
    public CoverageDataTiff(GeoPackage geoPackage, TileDao tileDao,
                            Projection requestProjection) {
        this(geoPackage, tileDao, null, null, requestProjection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoverageDataTiffImage createImage(TileRow tileRow) {
        return new CoverageDataTiffImage(tileRow);
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
                           CoverageDataTiffImage image, int x, int y) {
        Double value = null;
        if (image.getDirectory() != null) {
            float pixelValue = image.getPixel(x, y);
            value = getValue(griddedTile, pixelValue);
        } else {
            value = getValue(griddedTile, image.getImageBytes(), x, y);
        }
        return value;
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

        TIFFImage tiffImage = TiffReader.readTiff(imageBytes);
        FileDirectory directory = tiffImage.getFileDirectory();
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
        TIFFImage tiffImage = TiffReader.readTiff(imageBytes);
        FileDirectory directory = tiffImage.getFileDirectory();
        validateImageType(directory);
        Rasters rasters = directory.readRasters();
        float[] pixels = new float[rasters.getWidth() * rasters.getHeight()];
        for (int y = 0; y < rasters.getHeight(); y++) {
            for (int x = 0; x < rasters.getWidth(); x++) {
                int index = rasters.getSampleIndex(x, y);
                pixels[index] = rasters.getPixelSample(0, x, y).floatValue();
            }
        }
        return pixels;
    }

    /**
     * Validate that the image type
     *
     * @param directory file directory
     */
    public static void validateImageType(FileDirectory directory) {
        if (directory == null) {
            throw new GeoPackageException("The image is null");
        }

        int samplesPerPixel = directory.getSamplesPerPixel();
        Integer bitsPerSample = null;
        if (directory.getBitsPerSample() != null && !directory.getBitsPerSample().isEmpty()) {
            bitsPerSample = directory.getBitsPerSample().get(0);
        }
        Integer sampleFormat = null;
        if (directory.getSampleFormat() != null && !directory.getSampleFormat().isEmpty()) {
            sampleFormat = directory.getSampleFormat().get(0);
        }

        if (samplesPerPixel != SAMPLES_PER_PIXEL
                || bitsPerSample == null || bitsPerSample != BITS_PER_SAMPLE
                || sampleFormat == null || sampleFormat != TiffConstants.SAMPLE_FORMAT_FLOAT) {
            throw new GeoPackageException(
                    "The coverage data tile is expected to be a single sample 32 bit float. Samples Per Pixel: "
                            + samplesPerPixel
                            + ", Bits Per Sample: " + bitsPerSample
                            + ", Sample Format: " + sampleFormat);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getValue(GriddedTile griddedTile,
                           byte[] imageBytes, int x, int y) {
        float pixelValue = getPixelValue(imageBytes, x, y);
        Double value = getValue(griddedTile, pixelValue);
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double[] getValues(GriddedTile griddedTile,
                              byte[] imageBytes) {
        float[] pixelValues = getPixelValues(imageBytes);
        Double[] values = getValues(griddedTile, pixelValues);
        return values;
    }

    /**
     * Draw a coverage data image tile from the flat array of float pixel values of
     * length tileWidth * tileHeight where each pixel is at: (y * tileWidth) + x
     *
     * @param pixelValues float pixel values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return coverage data image tile
     */
    public CoverageDataTiffImage drawTile(float[] pixelValues, int tileWidth,
                                          int tileHeight) {

        CoverageDataTiffImage image = createImage(tileWidth, tileHeight);
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
     * Draw a coverage data image tile and format as TIFF bytes from the flat array
     * of float pixel values of length tileWidth * tileHeight where each pixel
     * is at: (y * tileWidth) + x
     *
     * @param pixelValues float pixel values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return coverage data image tile bytes
     */
    public byte[] drawTileData(float[] pixelValues, int tileWidth,
                               int tileHeight) {
        CoverageDataTiffImage image = drawTile(pixelValues, tileWidth, tileHeight);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw a coverage data image tile from the double array of float pixel values
     * formatted as float[row][width]
     *
     * @param pixelValues float pixel values as [row][width]
     * @return coverage data image tile
     */
    public CoverageDataTiffImage drawTile(float[][] pixelValues) {

        int tileWidth = pixelValues[0].length;
        int tileHeight = pixelValues.length;

        CoverageDataTiffImage image = createImage(tileWidth, tileHeight);
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
     * Draw a coverage data image tile and format as TIFF bytes from the double
     * array of float pixel values formatted as float[row][width]
     *
     * @param pixelValues float pixel values as [row][width]
     * @return coverage data image tile bytes
     */
    public byte[] drawTileData(float[][] pixelValues) {
        CoverageDataTiffImage image = drawTile(pixelValues);
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
    public CoverageDataTiffImage drawTile(GriddedTile griddedTile, Double[] values,
                                          int tileWidth, int tileHeight) {

        CoverageDataTiffImage image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                Double value = values[(y * tileWidth) + x];
                float pixelValue = getPixelValue(griddedTile, value);
                setPixelValue(image, x, y, pixelValue);
            }
        }
        image.writeTiff();

        return image;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] drawTileData(GriddedTile griddedTile, Double[] values,
                               int tileWidth, int tileHeight) {
        CoverageDataTiffImage image = drawTile(griddedTile, values, tileWidth,
                tileHeight);
        byte[] bytes = image.getImageBytes();
        return bytes;
    }

    /**
     * Draw a coverage data image tile from the double array of coverage data values
     * formatted as Double[row][width]
     *
     * @param griddedTile gridded tile
     * @param values      coverage data values as [row][width]
     * @return coverage data image tile
     */
    public CoverageDataTiffImage drawTile(GriddedTile griddedTile, Double[][] values) {

        int tileWidth = values[0].length;
        int tileHeight = values.length;

        CoverageDataTiffImage image = createImage(tileWidth, tileHeight);
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                Double value = values[y][x];
                short pixelValue = getPixelValue(griddedTile, value);
                setPixelValue(image, x, y, pixelValue);
            }
        }
        image.writeTiff();

        return image;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] drawTileData(GriddedTile griddedTile, Double[][] values) {
        CoverageDataTiffImage image = drawTile(griddedTile, values);
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
    public CoverageDataTiffImage createImage(int tileWidth, int tileHeight) {

        Rasters rasters = new Rasters(tileWidth, tileHeight, 1,
                BITS_PER_SAMPLE, TiffConstants.SAMPLE_FORMAT_FLOAT);

        int rowsPerStrip = rasters.calculateRowsPerStrip(TiffConstants.PLANAR_CONFIGURATION_CHUNKY);

        FileDirectory fileDirectory = new FileDirectory();
        fileDirectory.setImageWidth(tileWidth);
        fileDirectory.setImageHeight(tileHeight);
        fileDirectory.setBitsPerSample(BITS_PER_SAMPLE);
        fileDirectory.setCompression(TiffConstants.COMPRESSION_NO);
        fileDirectory.setPhotometricInterpretation(TiffConstants.PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO);
        fileDirectory.setSamplesPerPixel(SAMPLES_PER_PIXEL);
        fileDirectory.setRowsPerStrip(rowsPerStrip);
        fileDirectory.setPlanarConfiguration(TiffConstants.PLANAR_CONFIGURATION_CHUNKY);
        fileDirectory.setSampleFormat(TiffConstants.SAMPLE_FORMAT_FLOAT);
        fileDirectory.setWriteRasters(rasters);

        CoverageDataTiffImage image = new CoverageDataTiffImage(fileDirectory);

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
    public void setPixelValue(CoverageDataTiffImage image, int x, int y,
                              float pixelValue) {
        image.getRasters().setFirstPixelSample(x, y, pixelValue);
    }

    /**
     * Create the coverage data tile table with metadata and extension
     *
     * @param geoPackage               GeoPackage
     * @param tableName                table name
     * @param contentsBoundingBox      contents bounding box
     * @param contentsSrsId            contents srs id
     * @param tileMatrixSetBoundingBox tile matrix set bounding box
     * @param tileMatrixSetSrsId       tile matrix set srs id
     * @return coverage data
     */
    public static CoverageDataTiff createTileTableWithMetadata(
            GeoPackage geoPackage, String tableName,
            BoundingBox contentsBoundingBox, long contentsSrsId,
            BoundingBox tileMatrixSetBoundingBox, long tileMatrixSetSrsId) {

        CoverageDataTiff coverageData = (CoverageDataTiff) CoverageData
                .createTileTableWithMetadata(geoPackage, tableName,
                        contentsBoundingBox, contentsSrsId,
                        tileMatrixSetBoundingBox, tileMatrixSetSrsId,
                        GriddedCoverageDataType.FLOAT);
        return coverageData;
    }

}
