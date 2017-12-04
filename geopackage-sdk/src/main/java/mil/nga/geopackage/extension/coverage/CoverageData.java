package mil.nga.geopackage.extension.coverage;

import android.graphics.Rect;
import android.graphics.RectF;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxAndroidUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;

/**
 * Tiled Gridded Coverage Data, abstract Common Encoding, Extension
 *
 * @author osbornb
 * @since 2.0.1
 */
public abstract class CoverageData<TImage extends CoverageDataImage> extends CoverageDataCore<TImage> {

    /**
     * Get a Tiled Gridded Coverage Data
     *
     * @param geoPackage        GeoPackage
     * @param tileDao           tile dao
     * @param width             coverage data response width
     * @param height            coverage data response height
     * @param requestProjection request projection
     */
    public static CoverageData<?> getCoverageData(GeoPackage geoPackage,
                                                  TileDao tileDao, Integer width, Integer height,
                                                  Projection requestProjection) {

        TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();
        GriddedCoverageDao griddedCoverageDao = geoPackage
                .getGriddedCoverageDao();

        GriddedCoverage griddedCoverage = null;
        try {
            if (griddedCoverageDao.isTableExists()) {
                griddedCoverage = griddedCoverageDao.query(tileMatrixSet);
            }
        } catch (SQLException e) {
            throw new GeoPackageException(
                    "Failed to get Gridded Coverage for table name: "
                            + tileMatrixSet.getTableName(), e);
        }

        CoverageData<?> coverageData = null;

        GriddedCoverageDataType dataType = griddedCoverage.getDataType();
        switch (dataType) {
            case INTEGER:
                coverageData = new CoverageDataPng(geoPackage, tileDao, width,
                        height, requestProjection);
                break;
            case FLOAT:
                coverageData = new CoverageDataTiff(geoPackage, tileDao, width,
                        height, requestProjection);
                break;
            default:
                throw new GeoPackageException(
                        "Unsupported Gridded Coverage Data Type: " + dataType);
        }

        return coverageData;
    }

    /**
     * Get a Tiled Gridded Coverage Data, use the coverage data pixel tile size
     * as the request size width and height
     *
     * @param geoPackage GeoPackage
     * @param tileDao    tile dao
     */
    public static CoverageData<?> getCoverageData(GeoPackage geoPackage,
                                                  TileDao tileDao) {
        return getCoverageData(geoPackage, tileDao, null, null,
                tileDao.getProjection());
    }

    /**
     * Get a Tiled Gridded Coverage Data, use the coverage data pixel tile size
     * as the request size width and height, request as the specified projection
     *
     * @param geoPackage        GeoPackage
     * @param tileDao           tile dao
     * @param requestProjection request projection
     */
    public static CoverageData<?> getCoverageData(GeoPackage geoPackage,
                                                  TileDao tileDao, Projection requestProjection) {
        return getCoverageData(geoPackage, tileDao, null, null,
                requestProjection);
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
     * @param dataType                 gridded coverage data type
     * @return coverage data
     */
    public static CoverageData<?> createTileTableWithMetadata(
            GeoPackage geoPackage, String tableName,
            BoundingBox contentsBoundingBox, long contentsSrsId,
            BoundingBox tileMatrixSetBoundingBox, long tileMatrixSetSrsId,
            GriddedCoverageDataType dataType) {

        TileMatrixSet tileMatrixSet = CoverageDataCore
                .createTileTableWithMetadata(geoPackage, tableName,
                        contentsBoundingBox, contentsSrsId,
                        tileMatrixSetBoundingBox, tileMatrixSetSrsId);
        TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

        CoverageData<?> coverageData = null;
        switch (dataType) {
            case INTEGER:
                coverageData = new CoverageDataPng(geoPackage, tileDao);
                break;
            case FLOAT:
                coverageData = new CoverageDataTiff(geoPackage, tileDao);
                break;
            default:
                throw new GeoPackageException(
                        "Unsupported Gridded Coverage Data Type: " + dataType);
        }

        coverageData.getOrCreate();

        return coverageData;
    }

    /**
     * Tile DAO
     */
    protected final TileDao tileDao;

    /**
     * Constructor
     *
     * @param geoPackage        GeoPackage
     * @param tileDao           tile dao
     * @param width             coverage data response width
     * @param height            coverage data response height
     * @param requestProjection request projection
     */
    public CoverageData(GeoPackage geoPackage, TileDao tileDao, Integer width,
                        Integer height, Projection requestProjection) {
        super(geoPackage, tileDao
                .getTileMatrixSet(), width, height, requestProjection);
        this.tileDao = tileDao;
    }

    /**
     * Create a coverage data image
     *
     * @param tileRow tile row
     * @return image
     */
    public abstract TImage createImage(TileRow tileRow);

    /**
     * Get the coverage data value of the pixel in the tile row image
     *
     * @param griddedTile gridded tile
     * @param tileRow     tile row
     * @param x           x coordinate
     * @param y           y coordinate
     * @return coverage data value
     */
    public abstract double getValue(GriddedTile griddedTile,
                                    TileRow tileRow, int x, int y);

    /**
     * Get the coverage data value
     *
     * @param griddedTile gridded tile
     * @param imageBytes  image bytes
     * @param x           x coordinate
     * @param y           y coordinate
     * @return coverage data value
     */
    public abstract Double getValue(GriddedTile griddedTile,
                                    byte[] imageBytes, int x, int y);

    /**
     * Get the coverage data values
     *
     * @param griddedTile gridded tile
     * @param imageBytes  image bytes
     * @return coverage data values
     */
    public abstract Double[] getValues(GriddedTile griddedTile,
                                       byte[] imageBytes);

    /**
     * Draw a coverage data image tile and format as TIFF bytes from the flat array
     * of coverage data values of length tileWidth * tileHeight where each coverage data value is
     * at: (y * tileWidth) + x
     *
     * @param griddedTile gridded tile
     * @param values      coverage data values of length tileWidth * tileHeight
     * @param tileWidth   tile width
     * @param tileHeight  tile height
     * @return coverage data image tile bytes
     */
    public abstract byte[] drawTileData(GriddedTile griddedTile, Double[] values,
                                        int tileWidth, int tileHeight);

    /**
     * Draw a coverage data image tile and format as TIFF bytes from the double
     * array of coverage data values formatted as Double[row][width]
     *
     * @param griddedTile gridded tile
     * @param values      coverage data values as [row][width]
     * @return coverage data image tile bytes
     */
    public abstract byte[] drawTileData(GriddedTile griddedTile, Double[][] values);

    /**
     * Get the tile dao
     *
     * @return tile dao
     */
    public TileDao getTileDao() {
        return tileDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoverageDataResults getValues(CoverageDataRequest request,
                                         Integer width, Integer height) {

        CoverageDataResults coverageDataResults = null;

        // Transform to the projection of the coverage data tiles
        ProjectionTransform transformRequestToCoverage = null;
        BoundingBox requestProjectedBoundingBox = request.getBoundingBox();
        if (!sameProjection) {
            transformRequestToCoverage = requestProjection
                    .getTransformation(coverageProjection);
            requestProjectedBoundingBox = transformRequestToCoverage
                    .transform(requestProjectedBoundingBox);
        }
        request.setProjectedBoundingBox(requestProjectedBoundingBox);

        // Determine how many overlapping pixels to store based upon the
        // algorithm
        int overlappingPixels;
        switch (algorithm) {
            case BICUBIC:
                overlappingPixels = 3;
                break;
            default:
                overlappingPixels = 1;
        }

        // Find the tile matrix and results
        CoverageDataTileMatrixResults results = getResults(request,
                requestProjectedBoundingBox, overlappingPixels);

        if (results != null) {

            TileMatrix tileMatrix = results.getTileMatrix();
            TileCursor tileResults = results.getTileResults();

            try {

                // Determine the requested coverage data dimensions, or use the
                // dimensions of a single tile matrix coverage data tile
                int requestedCoverageDataWidth = width != null ? width
                        : (int) tileMatrix.getTileWidth();
                int requestedCoverageDataHeight = height != null ? height
                        : (int) tileMatrix.getTileHeight();

                // Determine the size of the non projected coverage data results
                int tileWidth = requestedCoverageDataWidth;
                int tileHeight = requestedCoverageDataHeight;
                if (!sameProjection) {
                    int projectedWidth = (int) Math
                            .round((requestProjectedBoundingBox
                                    .getMaxLongitude() - requestProjectedBoundingBox
                                    .getMinLongitude())
                                    / tileMatrix.getPixelXSize());
                    if (projectedWidth > 0) {
                        tileWidth = projectedWidth;
                    }
                    int projectedHeight = (int) Math
                            .round((requestProjectedBoundingBox
                                    .getMaxLatitude() - requestProjectedBoundingBox
                                    .getMinLatitude())
                                    / tileMatrix.getPixelYSize());
                    if (projectedHeight > 0) {
                        tileHeight = projectedHeight;
                    }
                }

                // Retrieve the coverage data from the results
                Double[][] values = getValues(tileMatrix, tileResults,
                        request, tileWidth, tileHeight, overlappingPixels);

                // Project the coverage data if needed
                if (values != null && !sameProjection && !request.isPoint()) {
                    values = reprojectCoverageData(values,
                            requestedCoverageDataWidth,
                            requestedCoverageDataHeight,
                            request.getBoundingBox(),
                            transformRequestToCoverage,
                            requestProjectedBoundingBox);
                }

                // Create the results
                if (values != null) {
                    coverageDataResults = new CoverageDataResults(values,
                            tileMatrix);
                }
            } finally {
                tileResults.close();
            }
        }

        return coverageDataResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoverageDataResults getValuesUnbounded(CoverageDataRequest request) {

        CoverageDataResults coverageDataResults = null;

        // Transform to the projection of the coverage data tiles
        ProjectionTransform transformRequestToCoverage = null;
        BoundingBox requestProjectedBoundingBox = request.getBoundingBox();
        if (!sameProjection) {
            transformRequestToCoverage = requestProjection
                    .getTransformation(coverageProjection);
            requestProjectedBoundingBox = transformRequestToCoverage
                    .transform(requestProjectedBoundingBox);
        }
        request.setProjectedBoundingBox(requestProjectedBoundingBox);

        // Find the tile matrix and results
        CoverageDataTileMatrixResults results = getResults(request,
                requestProjectedBoundingBox);

        if (results != null) {

            TileMatrix tileMatrix = results.getTileMatrix();
            TileCursor tileResults = results.getTileResults();

            try {

                // Retrieve the coverage data values from the results
                Double[][] values = getValuesUnbounded(tileMatrix,
                        tileResults, request);

                // Project the coverage data if needed
                if (values != null && !sameProjection && !request.isPoint()) {
                    values = reprojectCoverageData(values,
                            values[0].length, values.length,
                            request.getBoundingBox(),
                            transformRequestToCoverage,
                            requestProjectedBoundingBox);
                }

                // Create the results
                if (values != null) {
                    coverageDataResults = new CoverageDataResults(values,
                            tileMatrix);
                }

            } finally {
                tileResults.close();
            }
        }

        return coverageDataResults;
    }

    /**
     * Get the coverage data tile results by finding the tile matrix with values
     *
     * @param request                     coverage data request
     * @param requestProjectedBoundingBox request projected bounding box
     * @return tile matrix results
     */
    private CoverageDataTileMatrixResults getResults(CoverageDataRequest request,
                                                     BoundingBox requestProjectedBoundingBox) {
        return getResults(request, requestProjectedBoundingBox, 0);
    }

    /**
     * Get the coverage data tile results by finding the tile matrix with values
     *
     * @param request                     coverage data request
     * @param requestProjectedBoundingBox request projected bounding box
     * @param overlappingPixels           overlapping request pixels
     * @return tile matrix results
     */
    private CoverageDataTileMatrixResults getResults(CoverageDataRequest request,
                                                     BoundingBox requestProjectedBoundingBox, int overlappingPixels) {
        // Try to get the coverage data from the current zoom level
        TileMatrix tileMatrix = getTileMatrix(request);
        CoverageDataTileMatrixResults results = null;
        if (tileMatrix != null) {
            results = getResults(requestProjectedBoundingBox, tileMatrix,
                    overlappingPixels);

            // Try to zoom in or out to find a matching coverage data
            if (results == null) {
                results = getResultsZoom(requestProjectedBoundingBox,
                        tileMatrix, overlappingPixels);
            }
        }
        return results;
    }

    /**
     * Get the coverage data tile results for a specified tile matrix
     *
     * @param requestProjectedBoundingBox request projected bounding box
     * @param tileMatrix                  tile matrix
     * @param overlappingPixels           number of overlapping pixels used by the algorithm
     * @return tile matrix results
     */
    private CoverageDataTileMatrixResults getResults(
            BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix,
            int overlappingPixels) {
        CoverageDataTileMatrixResults results = null;
        BoundingBox paddedBoundingBox = padBoundingBox(tileMatrix,
                requestProjectedBoundingBox, overlappingPixels);
        TileCursor tileResults = retrieveSortedTileResults(
                paddedBoundingBox, tileMatrix);
        if (tileResults != null) {
            if (tileResults.getCount() > 0) {
                results = new CoverageDataTileMatrixResults(tileMatrix,
                        tileResults);
            } else {
                tileResults.close();
            }
        }
        return results;
    }

    /**
     * Get the coverage data tile results by zooming in or out as needed from the
     * provided tile matrix to find values
     *
     * @param requestProjectedBoundingBox request projected bounding box
     * @param tileMatrix                  tile matrix
     * @param overlappingPixels           overlapping request pixels
     * @return tile matrix results
     */
    private CoverageDataTileMatrixResults getResultsZoom(
            BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix,
            int overlappingPixels) {

        CoverageDataTileMatrixResults results = null;

        if (zoomIn && zoomInBeforeOut) {
            results = getResultsZoomIn(requestProjectedBoundingBox, tileMatrix,
                    overlappingPixels);
        }
        if (results == null && zoomOut) {
            results = getResultsZoomOut(requestProjectedBoundingBox,
                    tileMatrix, overlappingPixels);
        }
        if (results == null && zoomIn && !zoomInBeforeOut) {
            results = getResultsZoomIn(requestProjectedBoundingBox, tileMatrix,
                    overlappingPixels);
        }

        return results;
    }

    /**
     * Get the coverage data tile results by zooming in from the provided tile
     * matrix
     *
     * @param requestProjectedBoundingBox request projected bounding box
     * @param tileMatrix                  tile matrix
     * @param overlappingPixels           overlapping request pixels
     * @return tile matrix results
     */
    private CoverageDataTileMatrixResults getResultsZoomIn(
            BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix,
            int overlappingPixels) {

        CoverageDataTileMatrixResults results = null;
        for (long zoomLevel = tileMatrix.getZoomLevel() + 1; zoomLevel <= tileDao
                .getMaxZoom(); zoomLevel++) {
            TileMatrix zoomTileMatrix = tileDao.getTileMatrix(zoomLevel);
            if (zoomTileMatrix != null) {
                results = getResults(requestProjectedBoundingBox,
                        zoomTileMatrix, overlappingPixels);
                if (results != null) {
                    break;
                }
            }
        }
        return results;
    }

    /**
     * Get the coverage data tile results by zooming out from the provided tile
     * matrix
     *
     * @param requestProjectedBoundingBox request projected bounding box
     * @param tileMatrix                  tile matrix
     * @param overlappingPixels           overlapping request pixels
     * @return tile matrix results
     */
    private CoverageDataTileMatrixResults getResultsZoomOut(
            BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix,
            int overlappingPixels) {

        CoverageDataTileMatrixResults results = null;
        for (long zoomLevel = tileMatrix.getZoomLevel() - 1; zoomLevel >= tileDao
                .getMinZoom(); zoomLevel--) {
            TileMatrix zoomTileMatrix = tileDao.getTileMatrix(zoomLevel);
            if (zoomTileMatrix != null) {
                results = getResults(requestProjectedBoundingBox,
                        zoomTileMatrix, overlappingPixels);
                if (results != null) {
                    break;
                }
            }
        }
        return results;
    }

    /**
     * Get the coverage data values from the tile results scaled to the provided
     * dimensions
     *
     * @param tileMatrix        tile matrix
     * @param tileResults       tile results
     * @param request           coverage data request
     * @param tileWidth         tile width
     * @param tileHeight        tile height
     * @param overlappingPixels overlapping request pixels
     * @return coverage data values
     */
    private Double[][] getValues(TileMatrix tileMatrix,
                                 TileCursor tileResults, CoverageDataRequest request, int tileWidth,
                                 int tileHeight, int overlappingPixels) {

        Double[][] values = null;

        // Tiles are ordered by rows and then columns. Track the last column
        // coverage data values of the tile to the left and the last rows of the tiles
        // in the row above
        Double[][] leftLastColumns = null;
        Map<Long, Double[][]> lastRowsByColumn = null;
        Map<Long, Double[][]> previousLastRowsByColumn = null;

        long previousRow = -1;
        long previousColumn = Long.MAX_VALUE;

        // Process each coverage data tile
        while (tileResults.moveToNext()) {

            // Get the next coverage data tile
            TileRow tileRow = tileResults.getRow();

            long currentRow = tileRow.getTileRow();
            long currentColumn = tileRow.getTileColumn();

            // If the row has changed, save off the previous last rows and begin
            // tracking this row. Clear the left last columns.
            if (currentRow > previousRow) {
                previousLastRowsByColumn = lastRowsByColumn;
                lastRowsByColumn = new HashMap<Long, Double[][]>();
                leftLastColumns = null;
            }

            // If there was a previous row, retrieve the top left and top
            // overlapping rows
            Double[][] topLeftRows = null;
            Double[][] topRows = null;
            if (previousLastRowsByColumn != null) {
                topLeftRows = previousLastRowsByColumn.get(currentColumn - 1);
                topRows = previousLastRowsByColumn.get(currentColumn);
            }

            // If the current column is not the column after the previous clear
            // the left values
            if (currentColumn < previousColumn
                    || currentColumn != previousColumn + 1) {
                leftLastColumns = null;
            }

            // Get the bounding box of the coverage data
            BoundingBox tileBoundingBox = TileBoundingBoxUtils
                    .getBoundingBox(coverageBoundingBox, tileMatrix,
                            currentColumn, currentRow);

            // Get the bounding box where the request and coverage data tile overlap
            BoundingBox overlap = request.overlap(tileBoundingBox);

            // Get the gridded tile value for the tile
            GriddedTile griddedTile = getGriddedTile(tileRow.getId());

            // Get the coverage data tile image
            TImage image = createImage(tileRow);

            // If the tile overlaps with the requested box
            if (overlap != null) {

                // Get the rectangle of the tile coverage data with matching values
                RectF src = TileBoundingBoxAndroidUtils
                        .getFloatRectangle(tileMatrix.getTileWidth(),
                                tileMatrix.getTileHeight(), tileBoundingBox,
                                overlap);

                // Get the rectangle of where to store the results
                RectF dest = null;
                if (request.getProjectedBoundingBox().equals(overlap)) {
                    if (request.isPoint()) {
                        // For single points request only a single destination
                        // pixel
                        dest = new RectF(0, 0, 0, 0);
                    } else {
                        // The overlap is equal to the request, set as the full
                        // destination size
                        dest = new RectF(0, 0, tileWidth, tileHeight);
                    }
                } else {
                    dest = TileBoundingBoxAndroidUtils.getFloatRectangle(
                            tileWidth, tileHeight,
                            request.getProjectedBoundingBox(), overlap);
                }

                if (TileBoundingBoxAndroidUtils.isValidAllowEmpty(src) && TileBoundingBoxAndroidUtils.isValidAllowEmpty(dest)) {

                    // Create the coverage data array first time through
                    if (values == null) {
                        values = new Double[tileHeight][tileWidth];
                    }

                    // Get the destination widths
                    float destWidth = dest.right - dest.left;
                    float destHeight = dest.bottom - dest.top;

                    // Get the destination heights
                    float srcWidth = src.right - src.left;
                    float srcHeight = src.bottom - src.top;

                    // Determine the source to destination ratio and how many
                    // destination pixels equal half a source pixel
                    float widthRatio;
                    float halfDestWidthPixel;
                    if (destWidth == 0) {
                        widthRatio = 0.0f;
                        halfDestWidthPixel = 0.0f;
                    } else {
                        widthRatio = srcWidth / destWidth;
                        halfDestWidthPixel = 0.5f / widthRatio;
                    }
                    float heightRatio;
                    float halfDestHeightPixel;
                    if (destHeight == 0) {
                        heightRatio = 0.0f;
                        halfDestHeightPixel = 0.0f;
                    } else {
                        heightRatio = srcHeight / destHeight;
                        halfDestHeightPixel = 0.5f / heightRatio;
                    }

                    float algorithmDestWidthPixelOverlap = halfDestWidthPixel
                            * overlappingPixels;
                    float algorithmDestHeightPixelOverlap = halfDestHeightPixel
                            * overlappingPixels;

                    // Determine the range of destination values to set
                    int minDestY = (int) Math.floor(dest.top
                            - algorithmDestHeightPixelOverlap);
                    int maxDestY = (int) Math.ceil(dest.bottom
                            + algorithmDestHeightPixelOverlap);
                    int minDestX = (int) Math.floor(dest.left
                            - algorithmDestWidthPixelOverlap);
                    int maxDestX = (int) Math.ceil(dest.right
                            + algorithmDestWidthPixelOverlap);
                    minDestY = Math.max(minDestY, 0);
                    minDestX = Math.max(minDestX, 0);
                    maxDestY = Math.min(maxDestY, tileHeight - 1);
                    maxDestX = Math.min(maxDestX, tileWidth - 1);

                    // Read and set the coverage data values
                    for (int y = minDestY; y <= maxDestY; y++) {
                        for (int x = minDestX; x <= maxDestX; x++) {

                            // Determine the coverage data based upon the
                            // selected algorithm
                            Double value = null;
                            switch (algorithm) {
                                case NEAREST_NEIGHBOR:
                                    value = getNearestNeighborValue(
                                            griddedTile, image, leftLastColumns,
                                            topLeftRows, topRows, y, x, widthRatio,
                                            heightRatio, dest.top,
                                            dest.left, src.top,
                                            src.left);
                                    break;
                                case BILINEAR:
                                    value = getBilinearInterpolationValue(
                                            griddedTile, image, leftLastColumns,
                                            topLeftRows, topRows, y, x, widthRatio,
                                            heightRatio, dest.top,
                                            dest.left, src.top,
                                            src.left);
                                    break;
                                case BICUBIC:
                                    value = getBicubicInterpolationValue(
                                            griddedTile, image, leftLastColumns,
                                            topLeftRows, topRows, y, x, widthRatio,
                                            heightRatio, dest.top,
                                            dest.left, src.top,
                                            src.left);
                                    break;
                                default:
                                    throw new UnsupportedOperationException(
                                            "Algorithm is not supported: "
                                                    + algorithm);
                            }

                            if (value != null) {
                                values[y][x] = value;
                            }

                        }
                    }

                }
            }

            // Determine and store the coverage data values of the last columns and rows
            leftLastColumns = new Double[overlappingPixels][(int) tileMatrix
                    .getTileHeight()];
            Double[][] lastRows = new Double[overlappingPixels][(int) tileMatrix
                    .getTileWidth()];
            lastRowsByColumn.put(currentColumn, lastRows);

            // For each overlapping pixel
            for (int lastIndex = 0; lastIndex < overlappingPixels; lastIndex++) {

                // Store the last column row coverage data values
                int lastColumnIndex = (int) tileMatrix.getTileWidth()
                        - lastIndex - 1;
                for (int row = 0; row < tileMatrix.getTileHeight(); row++) {
                    Double value = getValue(griddedTile, image,
                            lastColumnIndex, row);
                    leftLastColumns[lastIndex][row] = value;
                }

                // Store the last row column coverage data values
                int lastRowIndex = (int) tileMatrix.getTileHeight() - lastIndex
                        - 1;
                for (int column = 0; column < tileMatrix.getTileWidth(); column++) {
                    Double value = getValue(griddedTile, image,
                            column, lastRowIndex);
                    lastRows[lastIndex][column] = value;
                }

            }

            // Update the previous row and column
            previousRow = currentRow;
            previousColumn = currentColumn;
        }

        return values;
    }

    /**
     * Get the coverage data values from the tile results unbounded in result size
     *
     * @param tileMatrix  tile matrix
     * @param tileResults tile results
     * @param request     coverage data request
     * @return coverage data values
     */
    private Double[][] getValuesUnbounded(TileMatrix tileMatrix,
                                          TileCursor tileResults, CoverageDataRequest request) {

        // Build a map of rows to maps of columns and values
        Map<Long, Map<Long, Double[][]>> rowsMap = new TreeMap<>();

        // Track the min and max row and column
        Long minRow = null;
        Long maxRow = null;
        Long minColumn = null;
        Long maxColumn = null;

        // Track count of tiles involved in the results
        int tileCount = 0;

        // Process each coverage data tile row
        while (tileResults.moveToNext()) {

            // Get the next coverage data tile
            TileRow tileRow = tileResults.getRow();

            // Get the bounding box of the coverage data
            BoundingBox tileBoundingBox = TileBoundingBoxUtils.getBoundingBox(
                    coverageBoundingBox, tileMatrix, tileRow.getTileColumn(),
                    tileRow.getTileRow());

            // Get the bounding box where the request and coverage data tile overlap
            BoundingBox overlap = request.overlap(tileBoundingBox);

            // If the coverage data tile overlaps with the requested box
            if (overlap != null) {

                // Get the rectangle of the tile coverage data with matching values
                Rect src = TileBoundingBoxAndroidUtils.getRectangle(
                        tileMatrix.getTileWidth(), tileMatrix.getTileHeight(),
                        tileBoundingBox, overlap);

                if (TileBoundingBoxAndroidUtils.isValidAllowEmpty(src)) {

                    // Get the source dimensions
                    int srcTop = Math.min(src.top,
                            (int) tileMatrix.getTileHeight() - 1);
                    int srcBottom = Math.min(src.bottom,
                            (int) tileMatrix.getTileHeight() - 1);
                    int srcLeft = Math.min(src.left,
                            (int) tileMatrix.getTileWidth() - 1);
                    int srcRight = Math.min(src.right,
                            (int) tileMatrix.getTileWidth() - 1);

                    // Get the gridded tile value for the tile
                    GriddedTile griddedTile = getGriddedTile(tileRow.getId());

                    // Get the coverage data tile image
                    TImage image = createImage(tileRow);

                    // Create the coverage data results for this tile
                    Double[][] values = new Double[srcBottom - srcTop + 1][srcRight
                            - srcLeft + 1];

                    // Get or add the columns map to the rows map
                    Map<Long, Double[][]> columnsMap = rowsMap.get(tileRow
                            .getTileRow());
                    if (columnsMap == null) {
                        columnsMap = new TreeMap<Long, Double[][]>();
                        rowsMap.put(tileRow.getTileRow(), columnsMap);
                    }

                    // Read and set the coverage data values
                    for (int y = srcTop; y <= srcBottom; y++) {

                        for (int x = srcLeft; x <= srcRight; x++) {

                            // Get the coverage data value from the source pixel
                            Double value = getValue(griddedTile,
                                    image, x, y);

                            values[y - srcTop][x - srcLeft] = value;
                        }
                    }

                    // Set the coverage data values in the results map
                    columnsMap.put(tileRow.getTileColumn(), values);

                    // Increase the contributing tiles count
                    tileCount++;

                    // Track the min and max row and column
                    minRow = minRow == null ? tileRow.getTileRow() : Math.min(
                            minRow, tileRow.getTileRow());
                    maxRow = maxRow == null ? tileRow.getTileRow() : Math.max(
                            maxRow, tileRow.getTileRow());
                    minColumn = minColumn == null ? tileRow.getTileColumn()
                            : Math.min(minColumn, tileRow.getTileColumn());
                    maxColumn = maxColumn == null ? tileRow.getTileColumn()
                            : Math.max(maxColumn, tileRow.getTileColumn());
                }
            }
        }

        // Handle formatting the results
        Double[][] values = formatUnboundedResults(tileMatrix, rowsMap,
                tileCount, minRow, maxRow, minColumn, maxColumn);

        return values;
    }

    /**
     * Get the tile matrix for the zoom level as defined by the area of the
     * request
     *
     * @param request coverage data request
     * @return tile matrix or null
     */
    private TileMatrix getTileMatrix(CoverageDataRequest request) {

        TileMatrix tileMatrix = null;

        // Check if the request overlaps coverage data bounding box
        if (request.overlap(coverageBoundingBox) != null) {

            // Get the tile distance
            BoundingBox projectedBoundingBox = request
                    .getProjectedBoundingBox();
            double distanceWidth = projectedBoundingBox.getMaxLongitude()
                    - projectedBoundingBox.getMinLongitude();
            double distanceHeight = projectedBoundingBox.getMaxLatitude()
                    - projectedBoundingBox.getMinLatitude();

            // Get the zoom level to request based upon the tile size
            Long zoomLevel = tileDao.getClosestZoomLevel(distanceWidth,
                    distanceHeight);

            // If there is a matching zoom level
            if (zoomLevel != null) {
                tileMatrix = tileDao.getTileMatrix(zoomLevel);
            }
        }

        return tileMatrix;
    }

    /**
     * Get the tile row results of coverage data tiles needed to create the
     * requested bounding box coverage data, sorted by row and then column
     *
     * @param projectedRequestBoundingBox bounding box projected to the coverage data
     * @param tileMatrix                  tile matrix
     * @return tile results or null
     */
    private TileCursor retrieveSortedTileResults(
            BoundingBox projectedRequestBoundingBox, TileMatrix tileMatrix) {

        TileCursor tileResults = null;

        if (tileMatrix != null) {

            // Get the tile grid
            TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
                    coverageBoundingBox, tileMatrix.getMatrixWidth(),
                    tileMatrix.getMatrixHeight(), projectedRequestBoundingBox);

            // Query for matching tiles in the tile grid
            tileResults = tileDao.queryByTileGrid(tileGrid,
                    tileMatrix.getZoomLevel(), TileTable.COLUMN_TILE_ROW + ","
                            + TileTable.COLUMN_TILE_COLUMN);

        }

        return tileResults;
    }

    /**
     * Get the coverage data value of the pixel in the tile row image
     *
     * @param tileRow tile row
     * @param x       x coordinate
     * @param y       y coordinate
     * @return coverage data value
     */
    public double getValue(TileRow tileRow, int x, int y) {
        GriddedTile griddedTile = getGriddedTile(tileRow.getId());
        double value = getValue(griddedTile, tileRow, x, y);
        return value;
    }

}
