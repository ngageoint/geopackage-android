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
import mil.nga.geopackage.projection.ProjectionConstants;
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
 * GeoPackage Tile Retriever, retrieves a tile from a GeoPackage from XYZ coordinates
 *
 * @author osbornb
 * @since 1.2.0
 */
public class GeoPackageTileRetriever implements TileRetriever {

    /**
     * Compress format
     */
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;

    /**
     * Tile data access object
     */
    private final TileDao tileDao;

    /**
     * Tile width
     */
    private Integer width;

    /**
     * Tile height
     */
    private Integer height;

    /**
     * Tile Matrix set web mercator bounding box
     */
    private final BoundingBox setWebMercatorBoundingBox;

    /**
     * Constructor using GeoPackage tile sizes
     *
     * @param tileDao tile dao
     */
    public GeoPackageTileRetriever(TileDao tileDao) {
        this.tileDao = tileDao;
        tileDao.adjustTileMatrixLengths();

        Projection webMercator = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

        long epsg = tileDao.getTileMatrixSet().getSrs()
                .getOrganizationCoordsysId();
        Projection projection = ProjectionFactory.getProjection(epsg);

        ProjectionTransform projectionToWebMercator = projection
                .getTransformation(webMercator);

        TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();
        BoundingBox setProjectionBoundingBox = tileMatrixSet.getBoundingBox();
        setWebMercatorBoundingBox = projectionToWebMercator
                .transform(setProjectionBoundingBox);
    }

    /**
     * Constructor with specified tile size
     *
     * @param tileDao tile dao
     * @param width   width
     * @param height  height
     */
    public GeoPackageTileRetriever(TileDao tileDao, int width, int height) {
        this(tileDao);
        this.width = width;
        this.height = height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoPackageTile getTile(int x, int y, int zoom) {

        GeoPackageTile tile = null;

        // Get the bounding box of the requested tile
        BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        // Check if the request overlaps the tile matrix set
        if (TileBoundingBoxUtils.overlap(webMercatorBoundingBox,
                setWebMercatorBoundingBox) != null) {

            // Get the tile distance
            double distance = webMercatorBoundingBox.getMaxLongitude()
                    - webMercatorBoundingBox.getMinLongitude();

            // Get the zoom level to request based upon the tile size
            Long zoomLevel = tileDao.getZoomLevel(distance);

            // If there is a matching zoom level
            if (zoomLevel != null) {

                TileMatrix tileMatrix = tileDao.getTileMatrix(zoomLevel);

                // Get the tile grid
                TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
                        setWebMercatorBoundingBox, tileMatrix.getMatrixWidth(),
                        tileMatrix.getMatrixHeight(), webMercatorBoundingBox);

                // Query for matching tiles in the tile grid
                TileCursor tileCursor = tileDao.queryByTileGrid(tileGrid,
                        zoomLevel);
                if (tileCursor != null) {

                    // Get the requested tile dimensions
                    int tileWidth = width != null ? width : (int) tileMatrix
                            .getTileWidth();
                    int tileHeight = height != null ? height : (int) tileMatrix
                            .getTileHeight();

                    // Draw the resulting bitmap with the matching tiles
                    Bitmap tileBitmap = null;
                    Canvas canvas = null;
                    Paint paint = null;
                    while (tileCursor.moveToNext()) {

                        // Get the next tile
                        TileRow tileRow = tileCursor.getRow();
                        Bitmap tileDataBitmap = tileRow.getTileDataBitmap();

                        // Get the bounding box of the tile
                        BoundingBox tileWebMercatorBoundingBox = TileBoundingBoxUtils
                                .getWebMercatorBoundingBox(
                                        setWebMercatorBoundingBox, tileMatrix,
                                        tileRow.getTileColumn(), tileRow.getTileRow());

                        // Get the bounding box where the requested image and
                        // tile overlap
                        BoundingBox overlap = TileBoundingBoxUtils.overlap(
                                webMercatorBoundingBox,
                                tileWebMercatorBoundingBox);

                        // If the tile overlaps with the requested box
                        if (overlap != null) {

                            // Get the rectangle of the tile image to draw
                            Rect src = TileBoundingBoxAndroidUtils
                                    .getRectangle(tileMatrix.getTileWidth(),
                                            tileMatrix.getTileHeight(),
                                            tileWebMercatorBoundingBox, overlap);

                            // Get the rectangle of where to draw the tile in
                            // the resulting image
                            RectF dest = TileBoundingBoxAndroidUtils
                                    .getFloatRectangle(tileWidth, tileHeight,
                                            webMercatorBoundingBox, overlap);

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
                    tileCursor.close();

                    // Create the tile
                    if (tileBitmap != null) {
                        try {
                            byte[] tileData = BitmapConverter.toBytes(
                                    tileBitmap, COMPRESS_FORMAT);
                            tile = new GeoPackageTile(tileWidth, tileHeight, tileData);
                        } catch (IOException e) {
                            Log.e(GeoPackageTileRetriever.class.getSimpleName(), "Failed to create tile. x: " + x + ", y: "
                                    + y + ", zoom: " + zoom, e);
                        }
                    }

                }
            }
        }

        return tile;
    }

}
