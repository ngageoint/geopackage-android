package mil.nga.geopackage.tiles.retriever;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import org.osgeo.proj4j.ProjCoordinate;

import java.io.IOException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxAndroidUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Tile Creator, creates a tile from a tile matrix to the desired projection
 *
 * @author osbornb
 * @since 1.3.0
 */
public class TileCreator {

    /**
     * Compress format
     */
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;

    /**
     * Tile DAO
     */
    private final TileDao tileDao;

    /**
     * Tile width
     */
    private final Integer width;

    /**
     * Tile height
     */
    private final Integer height;

    /**
     * Tile Matrix Set
     */
    private final TileMatrixSet tileMatrixSet;

    /**
     * Projection of the requests
     */
    private final Projection requestProjection;

    /**
     * Projection of the tiles
     */
    private final Projection tilesProjection;

    /**
     * Tile Set bounding box
     */
    private final BoundingBox tileSetBoundingBox;

    /**
     * Flag indicating the the tile and request projections are the same
     */
    private final boolean sameProjection;

    /**
     * Constructor, specified tile size and projection
     *
     * @param tileDao           tile dao
     * @param width             requested width
     * @param height            requested height
     * @param requestProjection requested projection
     */
    public TileCreator(TileDao tileDao, Integer width, Integer height, Projection requestProjection) {
        this.tileDao = tileDao;
        this.width = width;
        this.height = height;
        this.requestProjection = requestProjection;

        tileMatrixSet = tileDao.getTileMatrixSet();
        tilesProjection = ProjectionFactory.getProjection(tileDao.getTileMatrixSet().getSrs());
        tileSetBoundingBox = tileMatrixSet.getBoundingBox();

        // Check if the projections have the same units
        sameProjection = (requestProjection.getUnit().name.equals(tilesProjection.getUnit().name));
    }

    /**
     * Constructor, tile tables tile size and projection
     *
     * @param tileDao tile dao
     */
    public TileCreator(TileDao tileDao) {
        this(tileDao, null, null, tileDao.getProjection());
    }

    /**
     * Constructor, tile tables projection with specified tile size
     *
     * @param tileDao tile dao
     * @param width   requested width
     * @param height  requested height
     */
    public TileCreator(TileDao tileDao, Integer width, Integer height) {
        this(tileDao, width, height, tileDao.getProjection());
    }

    /**
     * Constructor, tile tables tile size and requested projection
     *
     * @param tileDao           tile dao
     * @param requestProjection requested projection
     */
    public TileCreator(TileDao tileDao, Projection requestProjection) {
        this(tileDao, null, null, requestProjection);
    }

    /**
     * Get the tile dao
     *
     * @return tile dao
     */
    public TileDao getTileDao() {
        return tileDao;
    }

    /**
     * Get the requested tile width
     *
     * @return width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Get the requested tile height
     *
     * @return height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Get the tile matrix set
     *
     * @return tile matrix set
     */
    public TileMatrixSet getTileMatrixSet() {
        return tileMatrixSet;
    }

    /**
     * Get the request projection
     *
     * @return request projection
     */
    public Projection getRequestProjection() {
        return requestProjection;
    }

    /**
     * Get the tiles projection
     *
     * @return tiles projection
     */
    public Projection getTilesProjection() {
        return tilesProjection;
    }

    /**
     * Get the tile set bounding box
     *
     * @return tile set bounding box
     */
    public BoundingBox getTileSetBoundingBox() {
        return tileSetBoundingBox;
    }

    /**
     * Is the request and tile projection the same
     *
     * @return true if the same
     */
    public boolean isSameProjection() {
        return sameProjection;
    }

    /**
     * Check if the tile table contains a tile for the request bounding box
     *
     * @param requestBoundingBox request bounding box in the request projection
     * @return true if a tile exists
     */
    public boolean hasTile(BoundingBox requestBoundingBox) {

        boolean hasTile = false;

        // Transform to the projection of the tiles
        ProjectionTransform transformRequestToTiles = requestProjection.getTransformation(tilesProjection);
        BoundingBox tilesBoundingBox = transformRequestToTiles.transform(requestBoundingBox);

        TileMatrix tileMatrix = getTileMatrix(tilesBoundingBox);

        TileCursor tileResults = retrieveTileResults(tilesBoundingBox, tileMatrix);
        if (tileResults != null) {

            try {
                hasTile = tileResults.getCount() > 0;
            } finally {
                tileResults.close();
            }
        }

        return hasTile;
    }

    /**
     * Get the tile from the request bounding box in the request projection
     *
     * @param requestBoundingBox request bounding box in the request projection
     * @return tile
     */
    public GeoPackageTile getTile(BoundingBox requestBoundingBox) {

        GeoPackageTile tile = null;

        // Transform to the projection of the tiles
        ProjectionTransform transformRequestToTiles = requestProjection.getTransformation(tilesProjection);
        BoundingBox tilesBoundingBox = transformRequestToTiles.transform(requestBoundingBox);

        TileMatrix tileMatrix = getTileMatrix(tilesBoundingBox);

        TileCursor tileResults = retrieveTileResults(tilesBoundingBox, tileMatrix);
        if (tileResults != null) {

            try {

                if (tileResults.getCount() > 0) {

                    BoundingBox requestProjectedBoundingBox = transformRequestToTiles.transform(requestBoundingBox);

                    // Determine the requested tile dimensions, or use the dimensions of a single tile matrix tile
                    int requestedTileWidth = width != null ? width : (int) tileMatrix
                            .getTileWidth();
                    int requestedTileHeight = height != null ? height : (int) tileMatrix
                            .getTileHeight();

                    // Determine the size of the tile to initially draw
                    int tileWidth = requestedTileWidth;
                    int tileHeight = requestedTileHeight;
                    if (!sameProjection) {
                        tileWidth = (int) Math.round(
                                (requestProjectedBoundingBox.getMaxLongitude() - requestProjectedBoundingBox.getMinLongitude())
                                        / tileMatrix.getPixelXSize());
                        tileHeight = (int) Math.round(
                                (requestProjectedBoundingBox.getMaxLatitude() - requestProjectedBoundingBox.getMinLatitude())
                                        / tileMatrix.getPixelYSize());
                    }

                    // Draw the resulting bitmap with the matching tiles
                    Bitmap tileBitmap = drawTile(tileMatrix, tileResults, requestProjectedBoundingBox, tileWidth, tileHeight);

                    // Create the tile
                    if (tileBitmap != null) {

                        // Project the tile if needed
                        if (!sameProjection) {
                            Bitmap reprojectTile = reprojectTile(tileBitmap, requestedTileWidth, requestedTileHeight, requestBoundingBox, transformRequestToTiles, tilesBoundingBox);
                            tileBitmap.recycle();
                            tileBitmap = reprojectTile;
                        }

                        try {
                            byte[] tileData = BitmapConverter.toBytes(
                                    tileBitmap, COMPRESS_FORMAT);
                            tileBitmap.recycle();
                            tile = new GeoPackageTile(requestedTileWidth, requestedTileHeight, tileData);
                        } catch (IOException e) {
                            Log.e(TileCreator.class.getSimpleName(), "Failed to create tile. min lat: "
                                    + requestBoundingBox.getMinLatitude()
                                    + ", max lat: " + requestBoundingBox.getMaxLatitude()
                                    + ", min lon: " + requestBoundingBox.getMinLongitude() +
                                    ", max lon: " + requestBoundingBox.getMaxLongitude(), e);
                        }
                    }

                }
            } finally {
                tileResults.close();
            }
        }

        return tile;
    }

    /**
     * Draw the tile from the tile results
     *
     * @param tileMatrix
     * @param tileResults
     * @param requestProjectedBoundingBox
     * @param tileWidth
     * @param tileHeight
     * @return tile bitmap
     */
    private Bitmap drawTile(TileMatrix tileMatrix, TileCursor tileResults, BoundingBox requestProjectedBoundingBox, int tileWidth, int tileHeight) {

        // Draw the resulting bitmap with the matching tiles
        Bitmap tileBitmap = null;
        Canvas canvas = null;
        Paint paint = null;
        while (tileResults.moveToNext()) {

            // Get the next tile
            TileRow tileRow = tileResults.getRow();
            Bitmap tileDataBitmap = tileRow.getTileDataBitmap();

            // Get the bounding box of the tile
            BoundingBox tileBoundingBox = TileBoundingBoxUtils
                    .getBoundingBox(
                            tileSetBoundingBox, tileMatrix,
                            tileRow.getTileColumn(), tileRow.getTileRow());

            // Get the bounding box where the requested image and
            // tile overlap
            BoundingBox overlap = TileBoundingBoxUtils.overlap(
                    requestProjectedBoundingBox,
                    tileBoundingBox);

            // If the tile overlaps with the requested box
            if (overlap != null) {

                // Get the rectangle of the tile image to draw
                Rect src = TileBoundingBoxAndroidUtils
                        .getRectangle(tileMatrix.getTileWidth(),
                                tileMatrix.getTileHeight(),
                                tileBoundingBox, overlap);

                // Get the rectangle of where to draw the tile in
                // the resulting image
                RectF dest = TileBoundingBoxAndroidUtils
                        .getRoundedFloatRectangle(tileWidth, tileHeight,
                                requestProjectedBoundingBox, overlap);

                // Create the bitmap first time through
                if (tileBitmap == null) {
                    tileBitmap = Bitmap.createBitmap(tileWidth,
                            tileHeight, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(tileBitmap);
                    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                }

                // Draw the tile to the bitmap
                canvas.drawBitmap(tileDataBitmap, src, dest, paint);
            }
        }

        return tileBitmap;
    }

    /**
     * Reproject the tile to the requested projection
     *
     * @param tile                    tile in the tile matrix projection
     * @param requestedTileWidth      requested tile width
     * @param requestedTileHeight     requested tile height
     * @param requestBoundingBox      request bounding box in the request projection
     * @param transformRequestToTiles transformation from request to tiles
     * @param tilesBoundingBox        request bounding box in the tile matrix projection
     * @return projected tile
     */
    private Bitmap reprojectTile(Bitmap tile, int requestedTileWidth, int requestedTileHeight, BoundingBox requestBoundingBox, ProjectionTransform transformRequestToTiles, BoundingBox tilesBoundingBox) {

        final double requestedWidthUnitsPerPixel = (requestBoundingBox.getMaxLongitude() - requestBoundingBox.getMinLongitude()) / requestedTileWidth;
        final double requestedHeightUnitsPerPixel = (requestBoundingBox.getMaxLatitude() - requestBoundingBox.getMinLatitude()) / requestedTileHeight;

        final double tilesDistanceWidth = tilesBoundingBox.getMaxLongitude() - tilesBoundingBox.getMinLongitude();
        final double tilesDistanceHeight = tilesBoundingBox.getMaxLatitude() - tilesBoundingBox.getMinLatitude();

        final int width = tile.getWidth();
        final int height = tile.getHeight();

        // Tile pixels of the tile matrix tiles
        int[] pixels = new int[width * height];
        tile.getPixels(pixels, 0, width, 0, 0, width, height);

        // Projected tile pixels to draw the reprojected tile
        int[] projectedPixels = new int[requestedTileWidth * requestedTileHeight];

        // Retrieve each pixel in the new tile from the unprojected tile
        for (int y = 0; y < requestedTileHeight; y++) {
            for (int x = 0; x < requestedTileWidth; x++) {

                double longitude = requestBoundingBox.getMinLongitude() + (x * requestedWidthUnitsPerPixel);
                double latitude = requestBoundingBox.getMaxLatitude() - (y * requestedHeightUnitsPerPixel);
                ProjCoordinate fromCoord = new ProjCoordinate(longitude, latitude);
                ProjCoordinate toCoord = transformRequestToTiles.transform(fromCoord);
                double projectedLongitude = toCoord.x;
                double projectedLatitude = toCoord.y;

                int xPixel = (int) Math.round(((projectedLongitude - tilesBoundingBox.getMinLongitude()) / tilesDistanceWidth) * width);
                int yPixel = (int) Math.round(((tilesBoundingBox.getMaxLatitude() - projectedLatitude) / tilesDistanceHeight) * height);

                xPixel = Math.max(0, xPixel);
                xPixel = Math.min(width - 1, xPixel);

                yPixel = Math.max(0, yPixel);
                yPixel = Math.min(height - 1, yPixel);

                int color = pixels[(yPixel * width) + xPixel];
                projectedPixels[(y * requestedTileWidth) + x] = color;
            }
        }

        // Draw the new tile bitmap
        Bitmap projectedTileBitmap = Bitmap.createBitmap(requestedTileWidth,
                requestedTileHeight, tile.getConfig());
        projectedTileBitmap.setPixels(projectedPixels, 0, requestedTileWidth, 0, 0, requestedTileWidth, requestedTileHeight);

        return projectedTileBitmap;
    }

    /**
     * Get the tile matrix that contains the tiles for the bounding box, matches against the bounding box and zoom level
     *
     * @param projectedRequestBoundingBox bounding box projected to the tiles
     * @return tile matrix or null
     */
    private TileMatrix getTileMatrix(BoundingBox projectedRequestBoundingBox) {

        TileMatrix tileMatrix = null;

        // Check if the request overlaps the tile matrix set
        if (TileBoundingBoxUtils.overlap(projectedRequestBoundingBox,
                tileSetBoundingBox) != null) {

            // Get the tile distance
            double distanceWidth = projectedRequestBoundingBox
                    .getMaxLongitude()
                    - projectedRequestBoundingBox.getMinLongitude();
            double distanceHeight = projectedRequestBoundingBox
                    .getMaxLatitude()
                    - projectedRequestBoundingBox.getMinLatitude();

            // Get the zoom level to request based upon the tile size
            Long zoomLevel = tileDao
                    .getZoomLevel(distanceWidth, distanceHeight);

            // If there is a matching zoom level
            if (zoomLevel != null) {
                tileMatrix = tileDao.getTileMatrix(zoomLevel);
            }
        }

        return tileMatrix;
    }

    /**
     * Get the tile row results of tiles needed to draw the requested bounding box tile
     *
     * @param projectedRequestBoundingBox bounding box projected to the tiles
     * @param tileMatrix
     * @return tile cursor results or null
     */
    private TileCursor retrieveTileResults(BoundingBox projectedRequestBoundingBox, TileMatrix tileMatrix) {

        TileCursor tileResults = null;

        if (tileMatrix != null) {

            // Get the tile grid
            TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
                    tileSetBoundingBox, tileMatrix.getMatrixWidth(),
                    tileMatrix.getMatrixHeight(), projectedRequestBoundingBox);

            // Query for matching tiles in the tile grid
            tileResults = tileDao.queryByTileGrid(tileGrid,
                    tileMatrix.getZoomLevel());

        }

        return tileResults;
    }

}
