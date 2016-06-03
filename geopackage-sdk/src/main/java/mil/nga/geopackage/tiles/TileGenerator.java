package mil.nga.geopackage.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.SparseArray;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.io.GeoPackageProgress;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrix.TileMatrixKey;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;

/**
 * Creates a set of tiles within a GeoPackage
 *
 * @author osbornb
 */
public abstract class TileGenerator {

    /**
     * Projection transformation from WGS84 to Web Mercator
     */
    private static final ProjectionTransform wgs84ToWebMercatorTransform = ProjectionFactory
            .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
            .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);

    /**
     * Projection transformation from Web Mercator to WGS84
     */
    private static final ProjectionTransform webMercatorToWgs84Transform = ProjectionFactory
            .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR)
            .getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

    /**
     * Context
     */
    protected final Context context;

    /**
     * GeoPackage
     */
    private final GeoPackage geoPackage;

    /**
     * Table Name
     */
    private final String tableName;

    /**
     * Min zoom level
     */
    private final int minZoom;

    /**
     * Max zoom level
     */
    private final int maxZoom;

    /**
     * Total tile count
     */
    private Integer tileCount;

    /**
     * Tile grids by zoom level
     */
    private final SparseArray<TileGrid> tileGrids = new SparseArray<TileGrid>();

    /**
     * Tile bounding box
     */
    protected BoundingBox boundingBox = new BoundingBox(-180.0, 180.0,
            ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE,
            ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE);

    /**
     * Tile matrix set bounding box
     */
    private BoundingBox tileMatrixSetBoundingBox;

    /**
     * Compress format
     */
    private CompressFormat compressFormat = null;

    /**
     * Compress quality
     */
    private int compressQuality = 100;

    /**
     * GeoPackage progress
     */
    private GeoPackageProgress progress;

    /**
     * Compression options
     */
    private Options options = null;

    /**
     * True when generating tiles in Google tile format, false when generating
     * GeoPackage format where rows and columns do not match the Google row &
     * column coordinates
     */
    private boolean googleTiles = false;

    /**
     * Web mercator bounding box
     */
    private BoundingBox webMercatorBoundingBox;

    /**
     * Matrix height when GeoPackage tile format
     */
    private long matrixHeight = 0;

    /**
     * Matrix width when GeoPackage tile format
     */
    private long matrixWidth = 0;

    /**
     * Constructor
     *
     * @param context
     * @param geoPackage
     * @param tableName
     * @param minZoom
     * @param maxZoom
     */
    public TileGenerator(Context context, GeoPackage geoPackage,
                         String tableName, int minZoom, int maxZoom) {
        this.context = context;
        geoPackage.verifyWritable();
        this.geoPackage = geoPackage;
        this.tableName = tableName;

        this.minZoom = minZoom;
        this.maxZoom = maxZoom;

    }

    /**
     * Get the GeoPackage
     *
     * @return GeoPackage
     * @since 1.2.5
     */
    public GeoPackage getGeoPackage() {
        return geoPackage;
    }

    /**
     * Get the table name
     *
     * @return table name
     * @since 1.2.5
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Get the min zoom
     *
     * @return min zoom
     * @since 1.2.5
     */
    public int getMinZoom() {
        return minZoom;
    }

    /**
     * Get the max zoom
     *
     * @return max zoom
     * @since 1.2.5
     */
    public int getMaxZoom() {
        return maxZoom;
    }

    /**
     * Set the tile bounding box
     *
     * @param boundingBox
     */
    public void setTileBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        this.boundingBox.setMinLatitude(Math.max(boundingBox.getMinLatitude(),
                ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE));
        this.boundingBox.setMaxLatitude(Math.min(boundingBox.getMaxLatitude(),
                ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE));
    }

    /**
     * Get the tile bounding box in WGS84 projection
     *
     * @return WGS84 bounding box
     * @since 1.2.5
     */
    public BoundingBox getTileBoundingBox() {
        return boundingBox;
    }

    /**
     * Set the tile bounding box specified in the provided projection
     *
     * @param boundingBox
     * @param projection
     * @since 1.2.5
     */
    public void setTileBoundingBox(BoundingBox boundingBox,
                                   Projection projection) {
        BoundingBox bbox = null;
        if (projection != null) {
            ProjectionTransform transform = projection
                    .getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            bbox = transform.transform(boundingBox);
        } else {
            bbox = boundingBox;
        }
        setTileBoundingBox(bbox);
    }

    /**
     * Get the tile bounding box in specified projection
     *
     * @param projection
     *            requested projection
     * @return bounding box
     * @since 1.2.5
     */
    public BoundingBox getTileBoundingBox(Projection projection) {
        ProjectionTransform transform = ProjectionFactory.getProjection(
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                .getTransformation(projection);
        return transform.transform(boundingBox);
    }

    /**
     * Set the compress format
     *
     * @param compressFormat
     */
    public void setCompressFormat(CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    /**
     * Get the compress format
     *
     * @return compress format
     * @since 1.2.5
     */
    public CompressFormat getCompressFormat() {
        return compressFormat;
    }

    /**
     * Set the compress quality. The Compress format must be set for this to be
     * used.
     *
     * @param compressQuality
     */
    public void setCompressQuality(Integer compressQuality) {
        if (compressQuality != null) {
            this.compressQuality = compressQuality;
        }
    }

    /**
     * Get the compress quality
     *
     * @return compress quality or null
     * @since 1.2.5
     */
    public Integer getCompressQuality() {
        return compressQuality;
    }

    /**
     * Set the progress tracker
     *
     * @param progress
     */
    public void setProgress(GeoPackageProgress progress) {
        this.progress = progress;
    }

    /**
     * Get the progress tracker
     *
     * @return progress
     * @since 1.2.5
     */
    public GeoPackageProgress getProgress() {
        return progress;
    }

    /**
     * Set the Bitmap Compress Config
     *
     * @param config
     */
    public void setBitmapCompressionConfig(Config config) {
        if (options == null) {
            options = new Options();
        }
        options.inPreferredConfig = config;
    }

    /**
     * Set the Google Tiles flag to true to generate Google tile format tiles.
     * Default is false
     *
     * @param googleTiles
     */
    public void setGoogleTiles(boolean googleTiles) {
        this.googleTiles = googleTiles;
    }

    /**
     * Is the Google Tiles flag set to generate Google tile format tiles.
     *
     * @return true if Google Tiles format, false if GeoPackage
     * @since 1.2.5
     */
    public boolean isGoogleTiles() {
        return googleTiles;
    }

    /**
     * Get the tile count of tiles to be generated
     *
     * @return
     */
    public int getTileCount() {
        if (tileCount == null) {
            long count = 0;
            BoundingBox requestWebMercatorBoundingBox = TileBoundingBoxUtils
                    .toWebMercator(boundingBox);
            for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
                // Get the tile grid that includes the entire bounding box
                TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
                        requestWebMercatorBoundingBox, zoom);
                count += tileGrid.count();
                tileGrids.put(zoom, tileGrid);
            }

            tileCount = (int) Math.min(count, Integer.MAX_VALUE);
        }
        return tileCount;
    }

    /**
     * Generate the tiles
     *
     * @return tiles created
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public int generateTiles() throws SQLException, IOException {

        int totalCount = getTileCount();

        // Set the max progress count
        if (progress != null) {
            progress.setMax(totalCount);
        }

        int count = 0;
        boolean update = false;

        // Get the web mercator projection of the requested bounding box
        BoundingBox requestWebMercatorBoundingBox = TileBoundingBoxUtils
                .toWebMercator(boundingBox);

        // Adjust the tile matrix set and web mercator bounds
        adjustBounds(requestWebMercatorBoundingBox, minZoom);

        // Create a new tile matrix or update an existing
        TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
        TileMatrixSet tileMatrixSet = null;
        if (!tileMatrixSetDao.isTableExists()
                || !tileMatrixSetDao.idExists(tableName)) {
            // Create the web mercator srs if needed
            SpatialReferenceSystemDao srsDao = geoPackage.getSpatialReferenceSystemDao();
            SpatialReferenceSystem webMercatorSrs = srsDao.getOrCreateFromEpsg(ProjectionConstants.EPSG_WEB_MERCATOR);
            SpatialReferenceSystem wgs84Srs = srsDao.getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            // Create the tile table
            tileMatrixSet = geoPackage.createTileTableWithMetadata(
                    tableName,
                    boundingBox,
                    wgs84Srs.getSrsId(),
                    webMercatorBoundingBox,
                    webMercatorSrs.getSrsId());
        } else {
            update = true;
            // Query to get the Tile Matrix Set
            tileMatrixSet = tileMatrixSetDao.queryForId(tableName);

            // Update the tile bounds between the existing and this request
            updateTileBounds(tileMatrixSet);
        }

        preTileGeneration();

        // Create the tiles
        try {
            Contents contents = tileMatrixSet.getContents();
            TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
            TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

            // Create the new matrix tiles
            for (int zoom = minZoom; zoom <= maxZoom
                    && (progress == null || progress.isActive()); zoom++) {

                TileGrid localTileGrid = null;

                // Determine the matrix width and height for Google format
                if (googleTiles) {
                    matrixWidth = TileBoundingBoxUtils.tilesPerSide(zoom);
                    matrixHeight = matrixWidth;
                }
                // Get the local tile grid for GeoPackage format of where the
                // tiles belong
                else {
                    localTileGrid = TileBoundingBoxUtils.getTileGrid(
                            webMercatorBoundingBox, matrixWidth, matrixHeight,
                            requestWebMercatorBoundingBox);
                }

                // Generate the tiles for the zoom level
                TileGrid tileGrid = tileGrids.get(zoom);
                count += generateTiles(tileMatrixDao, tileDao, contents, zoom,
                        tileGrid, localTileGrid, matrixWidth, matrixHeight,
                        update);

                if (!googleTiles) {
                    // Double the matrix width and height for the next level
                    matrixWidth *= 2;
                    matrixHeight *= 2;
                }
            }

            // Delete the table if cancelled
            if (progress != null && !progress.isActive()
                    && progress.cleanupOnCancel()) {
                geoPackage.deleteTableQuietly(tableName);
                count = 0;
            } else {
                // Update the contents last modified date
                contents.setLastChange(new Date());
                ContentsDao contentsDao = geoPackage.getContentsDao();
                contentsDao.update(contents);
            }
        } catch (RuntimeException e) {
            geoPackage.deleteTableQuietly(tableName);
            throw e;
        } catch (SQLException e) {
            geoPackage.deleteTableQuietly(tableName);
            throw e;
        } catch (IOException e) {
            geoPackage.deleteTableQuietly(tableName);
            throw e;
        }

        return count;
    }

    /**
     * Adjust the tile matrix set and web mercator bounds
     *
     * @param requestWebMercatorBoundingBox
     * @param zoom
     */
    private void adjustBounds(BoundingBox requestWebMercatorBoundingBox,
                              int zoom) {
        // Google Tile Format
        if (googleTiles) {
            adjustGoogleBounds();
        }
        // GeoPackage Tile Format
        else {
            adjustGeoPackageBounds(requestWebMercatorBoundingBox, zoom);
        }
    }

    /**
     * Adjust the tile matrix set and web mercator bounds for Google tile format
     */
    private void adjustGoogleBounds() {
        // Set the tile matrix set bounding box to be the world
        tileMatrixSetBoundingBox = new BoundingBox(-180.0, 180.0,
                ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE,
                ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE);
        webMercatorBoundingBox = wgs84ToWebMercatorTransform
                .transform(tileMatrixSetBoundingBox);
    }

    /**
     * Adjust the tile matrix set and web mercator bounds for GeoPackage format.
     * Determine the tile grid width and height
     *
     * @param requestWebMercatorBoundingBox
     * @param zoom
     */
    private void adjustGeoPackageBounds(
            BoundingBox requestWebMercatorBoundingBox, int zoom) {
        // Get the fitting tile grid and determine the bounding box that
        // fits it
        TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
                requestWebMercatorBoundingBox, zoom);
        webMercatorBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(tileGrid, zoom);
        tileMatrixSetBoundingBox = webMercatorToWgs84Transform
                .transform(webMercatorBoundingBox);
        matrixWidth = tileGrid.getMaxX() + 1 - tileGrid.getMinX();
        matrixHeight = tileGrid.getMaxY() + 1 - tileGrid.getMinY();
    }

    /**
     * Update the Content and Tile Matrix Set bounds
     *
     * @param tileMatrixSet
     * @throws java.sql.SQLException
     */
    private void updateTileBounds(TileMatrixSet tileMatrixSet)
            throws SQLException {

        TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

        if (tileDao.isGoogleTiles()) {
            if (!googleTiles) {
                // If adding GeoPackage tiles to a Google Tile format, add them
                // as Google tiles
                googleTiles = true;
                adjustGoogleBounds();
            }
        } else if (googleTiles) {
            // Can't add Google formatted tiles to GeoPackage tiles
            throw new GeoPackageException(
                    "Can not add Google formatted tiles to "
                            + tableName
                            + " which already contains GeoPackage formatted tiles");
        }

        Contents contents = tileMatrixSet.getContents();

        ProjectionTransform transformContentsToWgs84 = ProjectionFactory.getProjection(
                contents.getSrs())
                .getTransformation(
                        ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        // Combine the existing content and request bounding boxes
        BoundingBox contentsBoundingBox = transformContentsToWgs84.transform(contents
                .getBoundingBox());
        boundingBox = TileBoundingBoxUtils.union(contentsBoundingBox,
                boundingBox);

        // Update the contents if modified
        if (!contentsBoundingBox.equals(boundingBox)) {
            ProjectionTransform transformContentsToProjection = ProjectionFactory
                    .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                    .getTransformation(
                            contents.getSrs());
            contents.setBoundingBox(transformContentsToProjection
                    .transform(boundingBox));
            ContentsDao contentsDao = geoPackage.getContentsDao();
            contentsDao.update(contents);
        }

        // If updating GeoPackage format tiles, all existing metadata and tile
        // rows needs to be adjusted
        if (!googleTiles) {

            ProjectionTransform transformTileMatrixSetToWgs84 = ProjectionFactory.getProjection(
                    tileMatrixSet.getSrs())
                    .getTransformation(
                            ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            BoundingBox previousTileMatrixSetBoundingBox = transformTileMatrixSetToWgs84
                    .transform(tileMatrixSet.getBoundingBox());

            // Adjust the bounds to include the request and existing bounds
            BoundingBox totalBoundingBox = TileBoundingBoxUtils
                    .toWebMercator(boundingBox);
            int minNewOrUpdateZoom = Math.min(minZoom,
                    (int) tileDao.getMinZoom());
            adjustGeoPackageBounds(totalBoundingBox, minNewOrUpdateZoom);

            // Update the tile matrix set if modified
            if (!previousTileMatrixSetBoundingBox
                    .equals(tileMatrixSetBoundingBox)) {
                ProjectionTransform transformTileMatrixSetToProjection = ProjectionFactory
                        .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                        .getTransformation(
                                tileMatrixSet.getSrs());
                tileMatrixSet.setBoundingBox(transformTileMatrixSetToProjection
                        .transform(tileMatrixSetBoundingBox));
                TileMatrixSetDao tileMatrixSetDao = geoPackage
                        .getTileMatrixSetDao();
                tileMatrixSetDao.update(tileMatrixSet);
            }

            // Get the previous bounding box and new bounding box in web
            // mercator
            ProjectionTransform transformWgs84ToWebMercator = ProjectionFactory
                    .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                    .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
            BoundingBox previousTileMatrixSetWebMercatorBoundingBox = transformWgs84ToWebMercator
                    .transform(previousTileMatrixSetBoundingBox);
            BoundingBox tileMatrixSetWebMercatorBoundingBox = transformWgs84ToWebMercator
                    .transform(tileMatrixSetBoundingBox);

            TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

            // Adjust the tile matrix metadata and tile rows at each existing
            // zoom level
            for (long zoom = tileDao.getMinZoom(); zoom <= tileDao.getMaxZoom(); zoom++) {
                TileMatrix tileMatrix = tileDao.getTileMatrix(zoom);
                if (tileMatrix != null) {

                    // Determine the new width and height at this level
                    long adjustment = (long) Math.pow(2, zoom
                            - minNewOrUpdateZoom);
                    long zoomMatrixWidth = matrixWidth * adjustment;
                    long zoomMatrixHeight = matrixHeight * adjustment;

                    // Get the zoom level tile rows, starting with highest rows
                    // and columns so when updating we avoid constraint
                    // violations
                    TileCursor tileCursor = tileDao
                            .queryForTileDescending(zoom);
                    try {
                        // Update each tile row at this zoom level
                        while (tileCursor.moveToNext()) {
                            TileRow tileRow = tileCursor.getRow();

                            // Get the bounding box of the existing tile
                            BoundingBox tileBoundingBox = TileBoundingBoxUtils
                                    .getBoundingBox(
                                            previousTileMatrixSetWebMercatorBoundingBox,
                                            tileMatrix, tileRow.getTileColumn(), tileRow.getTileRow());

                            // Get the mid lat and lon to find the new tile row
                            // and column
                            double midLatitude = tileBoundingBox
                                    .getMinLatitude()
                                    + ((tileBoundingBox.getMaxLatitude() - tileBoundingBox
                                    .getMinLatitude()) / 2.0);
                            double midLongitude = tileBoundingBox
                                    .getMinLongitude()
                                    + ((tileBoundingBox.getMaxLongitude() - tileBoundingBox
                                    .getMinLongitude()) / 2.0);

                            // Get the new tile row and column with regards to
                            // the new bounding box
                            long newTileRow = TileBoundingBoxUtils.getTileRow(
                                    tileMatrixSetWebMercatorBoundingBox,
                                    zoomMatrixHeight, midLatitude);
                            long newTileColumn = TileBoundingBoxUtils
                                    .getTileColumn(
                                            tileMatrixSetWebMercatorBoundingBox,
                                            zoomMatrixWidth, midLongitude);

                            // Update the tile row
                            tileRow.setTileRow(newTileRow);
                            tileRow.setTileColumn(newTileColumn);
                            tileDao.update(tileRow);
                        }
                    } finally {
                        tileCursor.close();
                    }

                    // Calculate the pixel size
                    double pixelXSize = (webMercatorBoundingBox
                            .getMaxLongitude() - webMercatorBoundingBox
                            .getMinLongitude())
                            / zoomMatrixWidth / tileMatrix.getTileWidth();
                    double pixelYSize = (webMercatorBoundingBox
                            .getMaxLatitude() - webMercatorBoundingBox
                            .getMinLatitude())
                            / zoomMatrixHeight / tileMatrix.getTileHeight();

                    // Update the tile matrix
                    tileMatrix.setMatrixWidth(zoomMatrixWidth);
                    tileMatrix.setMatrixHeight(zoomMatrixHeight);
                    tileMatrix.setPixelXSize(pixelXSize);
                    tileMatrix.setPixelYSize(pixelYSize);

                    tileMatrixDao.update(tileMatrix);
                }
            }

            // Adjust the width and height to the min zoom level of the
            // request
            if (minNewOrUpdateZoom < minZoom) {
                long adjustment = (long) Math.pow(2, minZoom
                        - minNewOrUpdateZoom);
                matrixWidth *= adjustment;
                matrixHeight *= adjustment;
            }

        }
    }

    /**
     * Close the GeoPackage
     */
    public void close() {
        if (geoPackage != null) {
            geoPackage.close();
        }
    }

    /**
     * Generate the tiles for the zoom level
     *
     * @param tileMatrixDao
     * @param tileDao
     * @param contents
     * @param zoomLevel
     * @param tileGrid
     * @param localTileGrid
     * @param matrixWidth
     * @param matrixHeight
     * @param update
     * @return tile count
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    private int generateTiles(TileMatrixDao tileMatrixDao, TileDao tileDao,
                              Contents contents, int zoomLevel, TileGrid tileGrid,
                              TileGrid localTileGrid, long matrixWidth, long matrixHeight,
                              boolean update) throws SQLException, IOException {

        int count = 0;

        Integer tileWidth = null;
        Integer tileHeight = null;

        // Download and create the tile and each coordinate
        for (long x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {

            // Check if the progress has been cancelled
            if (progress != null && !progress.isActive()) {
                break;
            }

            for (long y = tileGrid.getMinY(); y <= tileGrid.getMaxY(); y++) {

                // Check if the progress has been cancelled
                if (progress != null && !progress.isActive()) {
                    break;
                }

                try {

                    // Create the tile
                    byte[] tileBytes = createTile(zoomLevel, x, y);

                    if (tileBytes != null) {

                        Bitmap bitmap = null;

                        // Compress the image
                        if (compressFormat != null) {
                            bitmap = BitmapConverter.toBitmap(tileBytes, options);
                            if (bitmap != null) {
                                tileBytes = BitmapConverter.toBytes(bitmap,
                                        compressFormat, compressQuality);
                            }
                        }

                        // Create a new tile row
                        TileRow newRow = tileDao.newRow();
                        newRow.setZoomLevel(zoomLevel);

                        long tileColumn = x;
                        long tileRow = y;

                        // Update the column and row to the local tile grid location
                        if (localTileGrid != null) {
                            tileColumn = (x - tileGrid.getMinX())
                                    + localTileGrid.getMinX();
                            tileRow = (y - tileGrid.getMinY())
                                    + localTileGrid.getMinY();
                        }

                        // If an update, delete an existing row
                        if (update) {
                            tileDao.deleteTile(tileColumn, tileRow, zoomLevel);
                        }

                        newRow.setTileColumn(tileColumn);
                        newRow.setTileRow(tileRow);
                        newRow.setTileData(tileBytes);
                        tileDao.create(newRow);

                        count++;

                        // Determine the tile width and height
                        if (tileWidth == null) {
                            if (bitmap == null) {
                                bitmap = BitmapConverter.toBitmap(tileBytes,
                                        options);
                            }
                            if (bitmap != null) {
                                tileWidth = bitmap.getWidth();
                                tileHeight = bitmap.getHeight();
                            }
                        }
                    }
                } catch (Exception e) {
                    // Skip this tile, don't increase count
                }

                // Update the progress count, even on failures
                if (progress != null) {
                    progress.addProgress(1);
                }

            }

        }

        // If none of the tiles were translated into a bitmap with dimensions,
        // delete them
        if (tileWidth == null || tileHeight == null) {
            count = 0;

            StringBuilder where = new StringBuilder();

            where.append(tileDao.buildWhere(TileTable.COLUMN_ZOOM_LEVEL,
                    zoomLevel));

            where.append(" AND ");
            where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_COLUMN,
                    tileGrid.getMinX(), ">="));

            where.append(" AND ");
            where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_COLUMN,
                    tileGrid.getMaxX(), "<="));

            where.append(" AND ");
            where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_ROW,
                    tileGrid.getMinY(), ">="));

            where.append(" AND ");
            where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_ROW,
                    tileGrid.getMaxY(), "<="));

            String[] whereArgs = tileDao.buildWhereArgs(new Object[]{
                    zoomLevel, tileGrid.getMinX(), tileGrid.getMaxX(),
                    tileGrid.getMinY(), tileGrid.getMaxY()});

            tileDao.delete(where.toString(), whereArgs);

        } else {

            // Check if the tile matrix already exists
            boolean create = true;
            if (update) {
                create = !tileMatrixDao.idExists(new TileMatrixKey(tableName,
                        zoomLevel));
            }

            // Create the tile matrix
            if (create) {

                // Calculate meters per pixel
                double pixelXSize = (webMercatorBoundingBox.getMaxLongitude() - webMercatorBoundingBox
                        .getMinLongitude()) / matrixWidth / tileWidth;
                double pixelYSize = (webMercatorBoundingBox.getMaxLatitude() - webMercatorBoundingBox
                        .getMinLatitude()) / matrixHeight / tileHeight;

                // Create the tile matrix for this zoom level
                TileMatrix tileMatrix = new TileMatrix();
                tileMatrix.setContents(contents);
                tileMatrix.setZoomLevel(zoomLevel);
                tileMatrix.setMatrixWidth(matrixWidth);
                tileMatrix.setMatrixHeight(matrixHeight);
                tileMatrix.setTileWidth(tileWidth);
                tileMatrix.setTileHeight(tileHeight);
                tileMatrix.setPixelXSize(pixelXSize);
                tileMatrix.setPixelYSize(pixelYSize);
                tileMatrixDao.create(tileMatrix);
            }
        }

        return count;
    }

    /**
     * Called after set up and right before tile generation starts for the first
     * zoom level
     */
    protected abstract void preTileGeneration();

    /**
     * Create the tile
     *
     * @param z
     * @param x
     * @param y
     * @return
     */
    protected abstract byte[] createTile(int z, long x, long y);

}
