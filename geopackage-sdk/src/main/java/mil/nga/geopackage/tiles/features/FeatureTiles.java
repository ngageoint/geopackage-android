package mil.nga.geopackage.tiles.features;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint;
import android.util.Log;

import org.osgeo.proj4j.units.Units;

import java.io.IOException;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.R;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.util.GeometryUtils;

/**
 * Tiles drawn from or linked to features. Used to query features and optionally draw tiles
 * from those features.
 *
 * @author osbornb
 */
public abstract class FeatureTiles {

    /**
     * WGS84 Projection
     */
    protected static final Projection WGS_84_PROJECTION = ProjectionFactory
            .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

    /**
     * Web Mercator Projection
     */
    protected static final Projection WEB_MERCATOR_PROJECTION = ProjectionFactory
            .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

    /**
     * Context
     */
    protected final Context context;

    /**
     * Tile data access object
     */
    protected final FeatureDao featureDao;

    /**
     * Feature DAO Projection
     */
    protected Projection projection;

    /**
     * When not null, features are retrieved using a feature index
     */
    protected FeatureIndexManager indexManager;

    /**
     * Tile height
     */
    protected int tileWidth;

    /**
     * Tile height
     */
    protected int tileHeight;

    /**
     * Compress format
     */
    protected CompressFormat compressFormat;

    /**
     * Point radius
     */
    protected float pointRadius;

    /**
     * Point paint
     */
    protected Paint pointPaint = new Paint();

    /**
     * Optional point icon in place of a drawn circle
     */
    protected FeatureTilePointIcon pointIcon;

    /**
     * Line paint
     */
    protected Paint linePaint = new Paint();

    /**
     * Polygon paint
     */
    protected Paint polygonPaint = new Paint();

    /**
     * Fill polygon flag
     */
    protected boolean fillPolygon;

    /**
     * Polygon fill paint
     */
    protected Paint polygonFillPaint = new Paint();

    /**
     * Height overlapping pixels between tile images
     */
    protected float heightOverlap;

    /**
     * Width overlapping pixels between tile images
     */
    protected float widthOverlap;

    /**
     * Optional max features per tile. When more features than this value exist for creating a
     * single tile, the tile is not created
     */
    protected Integer maxFeaturesPerTile;

    /**
     * When not null and the number of features is greater than the max features per tile,
     * used to draw tiles for those tiles with more features than the max
     *
     * @see CustomFeaturesTile
     * @see mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile custom features tile implementation
     */
    protected CustomFeaturesTile maxFeaturesTileDraw;

    /**
     * When true, geometries are simplified before being drawn.  Default is true
     */
    protected boolean simplifyGeometries = true;

    /**
     * Constructor
     *
     * @param context
     * @param featureDao
     */
    public FeatureTiles(Context context, FeatureDao featureDao) {
        this.context = context;
        this.featureDao = featureDao;
        if (featureDao != null) {
            this.projection = featureDao.getProjection();
        }

        Resources resources = context.getResources();
        tileWidth = resources.getInteger(R.integer.feature_tiles_width);
        tileHeight = resources.getInteger(R.integer.feature_tiles_height);

        compressFormat = CompressFormat.valueOf(context.getString(R.string.feature_tiles_compress_format));

        pointPaint.setAntiAlias(true);
        pointRadius = Float.valueOf(context.getString(R.string.feature_tiles_point_radius));

        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(Float.valueOf(context.getString(R.string.feature_tiles_line_stroke_width)));
        linePaint.setStyle(Paint.Style.STROKE);

        polygonPaint.setAntiAlias(true);
        polygonPaint.setStrokeWidth(Float.valueOf(context.getString(R.string.feature_tiles_polygon_stroke_width)));
        polygonPaint.setStyle(Paint.Style.STROKE);

        fillPolygon = resources.getBoolean(R.bool.feature_tiles_polygon_fill);
        polygonFillPaint.setAntiAlias(true);
        polygonFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        polygonFillPaint.setAlpha(resources.getInteger(R.integer.feature_tiles_polygon_fill_alpha));

        calculateDrawOverlap();
    }

    /**
     * Close the feature tiles connection
     *
     * @since 1.2.7
     */
    public void close() {
        if (indexManager != null) {
            indexManager.close();
        }
    }

    /**
     * Call after making changes to the point icon, point radius, or paint stroke widths.
     * Determines the pixel overlap between tiles
     */
    public void calculateDrawOverlap() {
        if (pointIcon != null) {
            heightOverlap = pointIcon.getHeight();
            widthOverlap = pointIcon.getWidth();
        } else {
            heightOverlap = pointRadius;
            widthOverlap = pointRadius;
        }

        float linePaintHalfStroke = linePaint.getStrokeWidth() / 2.0f;
        heightOverlap = Math.max(heightOverlap, linePaintHalfStroke);
        widthOverlap = Math.max(widthOverlap, linePaintHalfStroke);

        float polygonPaintHalfStroke = polygonPaint.getStrokeWidth() / 2.0f;
        heightOverlap = Math.max(heightOverlap, polygonPaintHalfStroke);
        widthOverlap = Math.max(widthOverlap, polygonPaintHalfStroke);
    }

    /**
     * Manually set the width and height draw overlap
     *
     * @param pixels
     */
    public void setDrawOverlap(float pixels) {
        setWidthDrawOverlap(pixels);
        setHeightDrawOverlap(pixels);
    }

    /**
     * Get the width draw overlap
     *
     * @return
     */
    public float getWidthDrawOverlap() {
        return widthOverlap;
    }

    /**
     * Manually set the width draw overlap
     *
     * @param pixels
     */
    public void setWidthDrawOverlap(float pixels) {
        widthOverlap = pixels;
    }

    /**
     * Get the height draw overlap
     *
     * @return
     */
    public float getHeightDrawOverlap() {
        return heightOverlap;
    }

    /**
     * Manually set the height draw overlap
     *
     * @param pixels
     */
    public void setHeightDrawOverlap(float pixels) {
        heightOverlap = pixels;
    }

    /**
     * Get the feature DAO
     *
     * @return
     */
    public FeatureDao getFeatureDao() {
        return featureDao;
    }

    /**
     * Is index query
     *
     * @return true if an index query
     */
    public boolean isIndexQuery() {
        return indexManager != null && indexManager.isIndexed();
    }

    /**
     * Get the index manager
     *
     * @return index manager or null
     * @since 1.1.0
     */
    public FeatureIndexManager getIndexManager() {
        return indexManager;
    }

    /**
     * Set the index
     *
     * @param indexManager
     * @since 1.1.0
     */
    public void setIndexManager(FeatureIndexManager indexManager) {
        this.indexManager = indexManager;
    }

    /**
     * Get the tile width
     *
     * @return
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Set the tile width
     *
     * @param tileWidth
     */
    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    /**
     * Get the tile height
     *
     * @return
     */
    public int getTileHeight() {
        return tileHeight;
    }

    /**
     * Set the tile height
     *
     * @param tileHeight
     */
    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    /**
     * Get the compress format
     *
     * @return
     */
    public CompressFormat getCompressFormat() {
        return compressFormat;
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
     * Get the point radius
     *
     * @return
     */
    public float getPointRadius() {
        return pointRadius;
    }

    /**
     * Set the point radius
     *
     * @param pointRadius
     */
    public void setPointRadius(float pointRadius) {
        this.pointRadius = pointRadius;
    }

    /**
     * Get point paint
     *
     * @return
     */
    public Paint getPointPaint() {
        return pointPaint;
    }

    /**
     * Set the point paint
     *
     * @param pointPaint
     */
    public void setPointPaint(Paint pointPaint) {
        this.pointPaint = pointPaint;
    }

    /**
     * Get the point icon
     *
     * @return
     */
    public FeatureTilePointIcon getPointIcon() {
        return pointIcon;
    }

    /**
     * Set the point icon
     *
     * @param pointIcon
     */
    public void setPointIcon(FeatureTilePointIcon pointIcon) {
        this.pointIcon = pointIcon;
    }

    /**
     * Get the line paint
     *
     * @return
     */
    public Paint getLinePaint() {
        return linePaint;
    }

    /**
     * Set the line paint
     *
     * @param linePaint
     */
    public void setLinePaint(Paint linePaint) {
        this.linePaint = linePaint;
    }

    /**
     * Get the polygon paint
     *
     * @return
     */
    public Paint getPolygonPaint() {
        return polygonPaint;
    }

    /**
     * Set the polygon paint
     *
     * @param polygonPaint
     */
    public void setPolygonPaint(Paint polygonPaint) {
        this.polygonPaint = polygonPaint;
    }

    /**
     * Is fill polygon
     *
     * @return
     */
    public boolean isFillPolygon() {
        return fillPolygon;
    }

    /**
     * Set the fill polygon
     *
     * @param fillPolygon
     */
    public void setFillPolygon(boolean fillPolygon) {
        this.fillPolygon = fillPolygon;
    }

    /**
     * Get the polygon fill paint
     *
     * @return
     */
    public Paint getPolygonFillPaint() {
        return polygonFillPaint;
    }

    /**
     * Set the polygon fill paint
     *
     * @param polygonFillPaint
     */
    public void setPolygonFillPaint(Paint polygonFillPaint) {
        this.polygonFillPaint = polygonFillPaint;
    }

    /**
     * Get the max features per tile
     *
     * @return max features per tile or null
     * @since 1.1.0
     */
    public Integer getMaxFeaturesPerTile() {
        return maxFeaturesPerTile;
    }

    /**
     * Set the max features per tile. When more features are returned in a query to create a
     * single tile, the tile is not created.
     *
     * @param maxFeaturesPerTile
     * @since 1.1.0
     */
    public void setMaxFeaturesPerTile(Integer maxFeaturesPerTile) {
        this.maxFeaturesPerTile = maxFeaturesPerTile;
    }

    /**
     * Get the max features tile draw, the custom tile drawing implementation for tiles with more
     * features than the max at #getMaxFeaturesPerTile
     *
     * @return max features tile draw or null
     * @see CustomFeaturesTile
     * @see mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile custom features tile implementation
     * @since 1.1.0
     */
    public CustomFeaturesTile getMaxFeaturesTileDraw() {
        return maxFeaturesTileDraw;
    }

    /**
     * Set the max features tile draw, used to draw tiles when more features for a single tile
     * than the max at #getMaxFeaturesPerTile exist
     *
     * @param maxFeaturesTileDraw
     * @see CustomFeaturesTile
     * @see mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile custom features tile implementation
     * @since 1.1.0
     */
    public void setMaxFeaturesTileDraw(CustomFeaturesTile maxFeaturesTileDraw) {
        this.maxFeaturesTileDraw = maxFeaturesTileDraw;
    }

    /**
     * Is the simplify geometries flag set?  Default is true
     *
     * @return simplify geometries flag
     * @since 2.0.0
     */
    public boolean isSimplifyGeometries() {
        return simplifyGeometries;
    }

    /**
     * Set the simplify geometries flag
     *
     * @param simplifyGeometries simplify geometries flag
     * @since 2.0.0
     */
    public void setSimplifyGeometries(boolean simplifyGeometries) {
        this.simplifyGeometries = simplifyGeometries;
    }

    /**
     * Draw the tile and get the bytes from the x, y, and zoom level
     *
     * @param x
     * @param y
     * @param zoom
     * @return tile bytes, or null
     */
    public byte[] drawTileBytes(int x, int y, int zoom) {

        Bitmap bitmap = drawTile(x, y, zoom);

        byte[] tileData = null;

        // Convert the bitmap to bytes
        if (bitmap != null) {
            try {
                tileData = BitmapConverter.toBytes(
                        bitmap, compressFormat);
            } catch (IOException e) {
                Log.e(FeatureTiles.class.getSimpleName(), "Failed to create tile. x: " + x + ", y: "
                        + y + ", zoom: " + zoom, e);
            } finally {
                bitmap.recycle();
            }
        }

        return tileData;
    }

    /**
     * Draw a tile bitmap from the x, y, and zoom level
     *
     * @param x
     * @param y
     * @param zoom
     * @return tile bitmap, or null
     */
    public Bitmap drawTile(int x, int y, int zoom) {
        Bitmap bitmap;
        if (isIndexQuery()) {
            bitmap = drawTileQueryIndex(x, y, zoom);
        } else {
            bitmap = drawTileQueryAll(x, y, zoom);
        }
        return bitmap;
    }

    /**
     * Draw a tile bitmap from the x, y, and zoom level by querying features in the tile location
     *
     * @param x
     * @param y
     * @param zoom
     * @return drawn bitmap, or null
     */
    public Bitmap drawTileQueryIndex(int x, int y, int zoom) {

        // Get the web mercator bounding box
        BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        Bitmap bitmap = null;

        // Query for geometries matching the bounds in the index
        FeatureIndexResults results = queryIndexedFeatures(webMercatorBoundingBox);

        try {

            Long tileCount = null;
            if (maxFeaturesPerTile != null) {
                tileCount = results.count();
            }

            if (maxFeaturesPerTile == null || tileCount <= maxFeaturesPerTile.longValue()) {

                // Draw the tile bitmap
                bitmap = drawTile(zoom, webMercatorBoundingBox, results);

            } else if (maxFeaturesTileDraw != null) {

                // Draw the max features tile
                bitmap = maxFeaturesTileDraw.drawTile(tileWidth, tileHeight, tileCount, results);
            }
        } finally {
            results.close();
        }

        return bitmap;
    }

    /**
     * Draw a tile bitmap from the x, y, and zoom level by querying features in the tile location
     *
     * @param x
     * @param y
     * @param zoom
     * @return feature count
     * @since 1.1.0
     */
    public long queryIndexedFeaturesCount(int x, int y, int zoom) {

        // Get the web mercator bounding box
        BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        // Query for geometries matching the bounds in the index
        FeatureIndexResults results = queryIndexedFeatures(webMercatorBoundingBox);

        long count = 0;

        try {
            count = results.count();
        } finally {
            results.close();
        }

        return count;
    }

    /**
     * Query for feature results in the x, y, and zoom level by querying features in the tile location
     *
     * @param webMercatorBoundingBox
     * @return feature index results
     * @since 1.1.0
     */
    public FeatureIndexResults queryIndexedFeatures(BoundingBox webMercatorBoundingBox) {

        // Create an expanded bounding box to handle features outside the tile that overlap
        double minLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(tileWidth, webMercatorBoundingBox, 0 - widthOverlap);
        double maxLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(tileWidth, webMercatorBoundingBox, tileWidth + widthOverlap);
        double maxLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(tileHeight, webMercatorBoundingBox, 0 - heightOverlap);
        double minLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(tileHeight, webMercatorBoundingBox, tileHeight + heightOverlap);
        BoundingBox expandedQueryBoundingBox = new BoundingBox(
                minLongitude,
                minLatitude,
                maxLongitude,
                maxLatitude);

        // Query for geometries matching the bounds in the index
        FeatureIndexResults results = indexManager.query(expandedQueryBoundingBox, WEB_MERCATOR_PROJECTION);

        return results;
    }

    /**
     * Draw a tile bitmap from the x, y, and zoom level by querying all features. This could
     * be very slow if there are a lot of features
     *
     * @param x
     * @param y
     * @param zoom
     * @return drawn bitmap, or null
     */
    public Bitmap drawTileQueryAll(int x, int y, int zoom) {

        BoundingBox boundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        Bitmap bitmap = null;

        // Query for all features
        FeatureCursor cursor = featureDao.queryForAll();

        try {

            Integer totalCount = null;
            if (maxFeaturesPerTile != null) {
                totalCount = cursor.getCount();
            }

            if (maxFeaturesPerTile == null || totalCount <= maxFeaturesPerTile) {

                // Draw the tile bitmap
                bitmap = drawTile(zoom, boundingBox, cursor);

            } else if (maxFeaturesTileDraw != null) {

                // Draw the unindexed max features tile
                bitmap = maxFeaturesTileDraw.drawUnindexedTile(tileWidth, tileHeight, totalCount, cursor);
            }
        } finally {
            cursor.close();
        }

        return bitmap;
    }

    /**
     * Create a new empty Bitmap
     *
     * @return bitmap
     */
    protected Bitmap createNewBitmap() {
        return Bitmap.createBitmap(tileWidth,
                tileHeight, Bitmap.Config.ARGB_8888);
    }

    /**
     * Create a projection transformation from WGS84 to Web Mercator
     *
     * @return transform
     */
    protected ProjectionTransform getWgs84ToWebMercatorTransform() {
        return WGS_84_PROJECTION.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
    }

    /**
     * Create a projection transformation from provided projection to Web Mercator
     *
     * @param projection projection from
     * @return transform
     */
    protected ProjectionTransform getProjectionToWebMercatorTransform(Projection projection) {
        return projection.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
    }

    /**
     * When the simplify tolerance is set, simplify the points to a similar
     * curve with fewer points.
     *
     * @param simplifyTolerance simplify tolerance in meters
     * @param points            ordered points
     * @return simplified points
     * @since 2.0.0
     */
    protected List<Point> simplifyPoints(double simplifyTolerance,
                                         List<Point> points) {

        List<Point> simplifiedPoints = null;
        if (simplifyGeometries) {

            // Reproject to web mercator if not in meters
            if (projection != null && projection.getUnit() != Units.METRES) {
                ProjectionTransform toWebMercator = projection
                        .getTransformation(WEB_MERCATOR_PROJECTION);
                points = toWebMercator.transform(points);
            }

            // Simplify the points
            simplifiedPoints = GeometryUtils.simplifyPoints(points,
                    simplifyTolerance);

            // Reproject back to the original projection
            if (projection != null && projection.getUnit() != Units.METRES) {
                ProjectionTransform fromWebMercator = WEB_MERCATOR_PROJECTION
                        .getTransformation(projection);
                simplifiedPoints = fromWebMercator.transform(simplifiedPoints);
            }
        } else {
            simplifiedPoints = points;
        }

        return simplifiedPoints;
    }

    /**
     * Draw a tile bitmap from feature index results
     *
     * @param zoom                   zoom level
     * @param webMercatorBoundingBox
     * @param results
     * @return tile
     * @since 2.0.0
     */
    public abstract Bitmap drawTile(int zoom, BoundingBox webMercatorBoundingBox, FeatureIndexResults results);

    /**
     * Draw a tile bitmap from feature geometries in the provided cursor
     *
     * @param zoom                   zoom level
     * @param webMercatorBoundingBox
     * @param cursor
     * @return tile
     * @since 2.0.0
     */
    public abstract Bitmap drawTile(int zoom, BoundingBox webMercatorBoundingBox, FeatureCursor cursor);

    /**
     * Draw a tile bitmap from the feature rows
     *
     * @param zoom                   zoom level
     * @param webMercatorBoundingBox
     * @param featureRow
     * @return tile
     * @since 2.0.0
     */
    public abstract Bitmap drawTile(int zoom, BoundingBox webMercatorBoundingBox, List<FeatureRow> featureRow);

}
