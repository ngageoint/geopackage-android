package mil.nga.geopackage.tiles.retriever;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.io.IOException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.io.BitmapConverter;
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

/**
 * Tile Creator, creates a tile from a tile matrix to the desired projection
 *
 * @author osbornb
 * @since 1.2.10
 */
public class TileCreator {

    /**
     * Compress format
     */
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;

    /**
     * Tile DAO
     */
    public final TileDao tileDao;

    /**
     * Tile width
     */
    public final Integer width;

    /**
     * Tile height
     */
    public final Integer height;

    /**
     * Tile Matrix Set
     */
    public final TileMatrixSet tileMatrixSet;

    /**
     * Projection of the requests
     */
    public final Projection requestProjection;

    /**
     * Projection of the tiles
     */
    public final Projection tilesProjection;

    /**
     * Projection transform from the request projection to the tiles projection
     */
    public final ProjectionTransform transformRequestToTiles;

    /**
     * Projection transform from the tiles projection to the request projection
     */
    public final ProjectionTransform transformTilesToRequest;

    /**
     * Tile Set bounding box
     */
    public final BoundingBox tileSetBoundingBox;

    /**
     * Flag indicating
     */
    public final boolean sameProjection;

    /**
     * Constructor
     *
     * @param tileDao
     * @param width
     * @param height
     * @param tileMatrixSet
     * @param requestProjection
     * @param tilesProjection
     */
    public TileCreator(TileDao tileDao, Integer width, Integer height, TileMatrixSet tileMatrixSet, Projection requestProjection, Projection tilesProjection) {
        this.tileDao = tileDao;
        this.width = width;
        this.height = height;
        this.tileMatrixSet = tileMatrixSet;
        this.requestProjection = requestProjection;
        this.tilesProjection = tilesProjection;

        this.transformRequestToTiles = requestProjection.getTransformation(tilesProjection);
        this.transformTilesToRequest = tilesProjection.getTransformation(requestProjection);

        tileSetBoundingBox = tileMatrixSet.getBoundingBox();

        // Check if the projections have the same from meters value
        sameProjection = (requestProjection.getUnit().name.equals(tilesProjection.getUnit().name));
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
        BoundingBox tilesBoundingBox = transformRequestToTiles.transform(requestBoundingBox);

        TileMatrix tileMatrix = getTileMatrix(tilesBoundingBox);

        TileCursor tileResults = retrieveTileResults(tilesBoundingBox, tileMatrix);
        if (tileResults != null) {

            try {

                if (tileResults.getCount() > 0) {

                    BoundingBox requestProjectedBoundingBox = transformRequestToTiles.transform(requestBoundingBox);

                    // TODO Handle tiles in a different projection

                    // Get the requested tile dimensions
                    int tileWidth = width != null ? width : (int) tileMatrix
                            .getTileWidth();
                    int tileHeight = height != null ? height : (int) tileMatrix
                            .getTileHeight();

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
                                    .getFloatRectangle(tileWidth, tileHeight,
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

                    // Create the tile
                    if (tileBitmap != null) {
                        try {
                            byte[] tileData = BitmapConverter.toBytes(
                                    tileBitmap, COMPRESS_FORMAT);
                            tile = new GeoPackageTile(tileWidth, tileHeight, tileData);
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
            double distance = projectedRequestBoundingBox.getMaxLongitude()
                    - projectedRequestBoundingBox.getMinLongitude();

            // Get the zoom level to request based upon the tile size
            Long zoomLevel = tileDao.getZoomLevel(distance);

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
