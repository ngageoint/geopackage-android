package mil.nga.geopackage.tiles.features;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

import org.osgeo.proj4j.units.Units;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.R;
import mil.nga.geopackage.extension.style.FeatureStyle;
import mil.nga.geopackage.extension.style.FeatureTableStyles;
import mil.nga.geopackage.extension.style.IconCache;
import mil.nga.geopackage.extension.style.IconDao;
import mil.nga.geopackage.extension.style.IconRow;
import mil.nga.geopackage.extension.style.StyleDao;
import mil.nga.geopackage.extension.style.StyleRow;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.style.Color;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.sf.GeometryType;
import mil.nga.sf.Point;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;
import mil.nga.sf.util.GeometryUtils;

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
     * Feature Style extension
     */
    protected FeatureTableStyles featureTableStyles;

    /**
     * Tile width
     */
    protected int tileWidth;

    /**
     * Tile height
     */
    protected int tileHeight;

    /**
     * Empty transparent image for testing
     */
    private Bitmap emptyImage;

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
     * Feature paint cache
     */
    private FeaturePaintCache featurePaintCache = new FeaturePaintCache();

    /**
     * Icon Cache
     */
    private IconCache iconCache = new IconCache();

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
     * @param context    context
     * @param featureDao feature dao
     */
    public FeatureTiles(Context context, FeatureDao featureDao) {
        this(context, null, featureDao);
    }

    /**
     * Constructor, auto creates the index manager for indexed tables and feature styles for styled tables
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @param featureDao feature dao
     * @since 3.1.1
     */
    public FeatureTiles(Context context, GeoPackage geoPackage, FeatureDao featureDao) {

        this.context = context;
        this.featureDao = featureDao;
        if (featureDao != null) {
            this.projection = featureDao.getProjection();
        }

        Resources resources = context.getResources();
        tileWidth = resources.getInteger(R.integer.feature_tiles_width);
        tileHeight = resources.getInteger(R.integer.feature_tiles_height);
        createEmptyImage();

        compressFormat = CompressFormat.valueOf(context.getString(R.string.feature_tiles_compress_format));

        pointPaint.setAntiAlias(true);
        pointRadius = Float.valueOf(context.getString(R.string.feature_tiles_point_radius));

        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(Float.valueOf(context.getString(R.string.feature_tiles_line_stroke_width)));
        linePaint.setStyle(Style.STROKE);

        polygonPaint.setAntiAlias(true);
        polygonPaint.setStrokeWidth(Float.valueOf(context.getString(R.string.feature_tiles_polygon_stroke_width)));
        polygonPaint.setStyle(Style.STROKE);

        fillPolygon = resources.getBoolean(R.bool.feature_tiles_polygon_fill);
        polygonFillPaint.setAntiAlias(true);
        polygonFillPaint.setStyle(Style.FILL);
        polygonFillPaint.setAlpha(resources.getInteger(R.integer.feature_tiles_polygon_fill_alpha));

        if (geoPackage != null) {

            indexManager = new FeatureIndexManager(context, geoPackage, featureDao);
            if (!indexManager.isIndexed()) {
                indexManager.close();
                indexManager = null;
            }

            featureTableStyles = new FeatureTableStyles(geoPackage, featureDao.getTable());
            if (!featureTableStyles.has()) {
                featureTableStyles = null;
            }

        }

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
        emptyImage.recycle();
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

        if (featureTableStyles != null && featureTableStyles.has()) {

            // Style Rows
            Set<Long> styleRowIds = new HashSet<>();
            List<Long> tableStyleIds = featureTableStyles.getAllTableStyleIds();
            if (tableStyleIds != null) {
                styleRowIds.addAll(tableStyleIds);
            }
            List<Long> styleIds = featureTableStyles.getAllStyleIds();
            if (styleIds != null) {
                styleRowIds.addAll(styleIds);
            }

            StyleDao styleDao = featureTableStyles.getStyleDao();
            for (long styleRowId : styleRowIds) {
                StyleRow styleRow = styleDao.getRow(styleDao.queryForIdRow(styleRowId));
                float styleHalfWidth = (float) (styleRow.getWidthOrDefault() / 2.0f);
                widthOverlap = Math.max(widthOverlap, styleHalfWidth);
                heightOverlap = Math.max(heightOverlap, styleHalfWidth);
            }

            // Icon Rows
            Set<Long> iconRowIds = new HashSet<>();
            List<Long> tableIconIds = featureTableStyles.getAllTableIconIds();
            if (tableIconIds != null) {
                iconRowIds.addAll(tableIconIds);
            }
            List<Long> iconIds = featureTableStyles.getAllIconIds();
            if (iconIds != null) {
                iconRowIds.addAll(iconIds);
            }

            IconDao iconDao = featureTableStyles.getIconDao();
            for (long iconRowId : iconRowIds) {
                IconRow iconRow = iconDao.getRow(iconDao.queryForIdRow(iconRowId));
                double[] iconDimensions = iconRow.getDerivedDimensions();
                float iconWidth = (float) Math.ceil(iconDimensions[0]);
                float iconHeight = (float) Math.ceil(iconDimensions[1]);
                widthOverlap = Math.max(widthOverlap, iconWidth);
                heightOverlap = Math.max(heightOverlap, iconHeight);
            }

        }

    }

    /**
     * Manually set the width and height draw overlap
     *
     * @param pixels overlap pixels
     */
    public void setDrawOverlap(float pixels) {
        setWidthDrawOverlap(pixels);
        setHeightDrawOverlap(pixels);
    }

    /**
     * Get the width draw overlap
     *
     * @return width pixels
     */
    public float getWidthDrawOverlap() {
        return widthOverlap;
    }

    /**
     * Manually set the width draw overlap
     *
     * @param pixels overlap pixels
     */
    public void setWidthDrawOverlap(float pixels) {
        widthOverlap = pixels;
    }

    /**
     * Get the height draw overlap
     *
     * @return height pixels
     */
    public float getHeightDrawOverlap() {
        return heightOverlap;
    }

    /**
     * Manually set the height draw overlap
     *
     * @param pixels overlap pixels
     */
    public void setHeightDrawOverlap(float pixels) {
        heightOverlap = pixels;
    }

    /**
     * Get the feature DAO
     *
     * @return feature dao
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
     * @param indexManager index manager
     * @since 1.1.0
     */
    public void setIndexManager(FeatureIndexManager indexManager) {
        this.indexManager = indexManager;
    }

    /**
     * Get the feature table styles
     *
     * @return feature table styles
     * @since 3.1.1
     */
    public FeatureTableStyles getFeatureTableStyles() {
        return featureTableStyles;
    }

    /**
     * Set the feature table styles
     *
     * @param featureTableStyles feature table styles
     * @since 3.1.1
     */
    public void setFeatureTableStyles(FeatureTableStyles featureTableStyles) {
        this.featureTableStyles = featureTableStyles;
    }

    /**
     * Ignore the feature table styles within the GeoPackage
     *
     * @since 3.1.1
     */
    public void ignoreFeatureTableStyles() {
        setFeatureTableStyles(null);
        calculateDrawOverlap();
    }

    /**
     * Clear the style paint cache
     *
     * @since 3.1.1
     */
    public void clearStylePaintCache() {
        featurePaintCache.clear();
    }

    /**
     * Set / resize the style paint cache size
     *
     * @param size new size
     * @since 3.1.1
     */
    @TargetApi(21)
    public void setStylePaintCacheSize(int size) {
        featurePaintCache.resize(size);
    }

    /**
     * Clear the icon cache
     *
     * @since 3.1.1
     */
    public void clearIconCache() {
        iconCache.clear();
    }

    /**
     * Set / resize the icon cache size
     *
     * @param size new size
     * @since 3.1.1
     */
    @TargetApi(21)
    public void setIconCacheSize(int size) {
        iconCache.resize(size);
    }

    /**
     * Get the tile width
     *
     * @return tile width
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Set the tile width
     *
     * @param tileWidth tile width
     */
    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
        createEmptyImage();
    }

    /**
     * Get the tile height
     *
     * @return tile height
     */
    public int getTileHeight() {
        return tileHeight;
    }

    /**
     * Set the tile height
     *
     * @param tileHeight tile height
     */
    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
        createEmptyImage();
    }

    /**
     * Get the compress format
     *
     * @return compress format
     */
    public CompressFormat getCompressFormat() {
        return compressFormat;
    }

    /**
     * Set the compress format
     *
     * @param compressFormat compress format
     */
    public void setCompressFormat(CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    /**
     * Get the point radius
     *
     * @return point radius
     */
    public float getPointRadius() {
        return pointRadius;
    }

    /**
     * Set the point radius
     *
     * @param pointRadius point radius
     */
    public void setPointRadius(float pointRadius) {
        this.pointRadius = pointRadius;
    }

    /**
     * Get point paint
     *
     * @return point paint
     */
    public Paint getPointPaint() {
        return pointPaint;
    }

    /**
     * Set the point paint
     *
     * @param pointPaint point paint
     */
    public void setPointPaint(Paint pointPaint) {
        this.pointPaint = pointPaint;
    }

    /**
     * Get the point icon
     *
     * @return point icon
     */
    public FeatureTilePointIcon getPointIcon() {
        return pointIcon;
    }

    /**
     * Set the point icon
     *
     * @param pointIcon point icon
     */
    public void setPointIcon(FeatureTilePointIcon pointIcon) {
        this.pointIcon = pointIcon;
    }

    /**
     * Get the line paint
     *
     * @return line paint
     */
    public Paint getLinePaint() {
        return linePaint;
    }

    /**
     * Set the line paint
     *
     * @param linePaint line paint
     */
    public void setLinePaint(Paint linePaint) {
        this.linePaint = linePaint;
    }

    /**
     * Get the polygon paint
     *
     * @return polygon paint
     */
    public Paint getPolygonPaint() {
        return polygonPaint;
    }

    /**
     * Set the polygon paint
     *
     * @param polygonPaint polygon paint
     */
    public void setPolygonPaint(Paint polygonPaint) {
        this.polygonPaint = polygonPaint;
    }

    /**
     * Is fill polygon
     *
     * @return fill polygon
     */
    public boolean isFillPolygon() {
        return fillPolygon;
    }

    /**
     * Set the fill polygon
     *
     * @param fillPolygon fill polygon
     */
    public void setFillPolygon(boolean fillPolygon) {
        this.fillPolygon = fillPolygon;
    }

    /**
     * Get the polygon fill paint
     *
     * @return polygon fill paint
     */
    public Paint getPolygonFillPaint() {
        return polygonFillPaint;
    }

    /**
     * Set the polygon fill paint
     *
     * @param polygonFillPaint polygon fill paint
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
     * @param maxFeaturesPerTile max features per tile
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
     * @param maxFeaturesTileDraw max features tile draw
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
     * @param x    x coordinate
     * @param y    y coordinate
     * @param zoom zoom level
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
     * @param x    x coordinate
     * @param y    y coordinate
     * @param zoom zoom level
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
     * @param x    x coordinate
     * @param y    y coordinate
     * @param zoom zoom level
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

            long tileCount = results.count();

            // Draw if at least one geometry exists
            if (tileCount > 0) {

                if (maxFeaturesPerTile == null || tileCount <= maxFeaturesPerTile.longValue()) {

                    // Draw the tile bitmap
                    bitmap = drawTile(zoom, webMercatorBoundingBox, results);

                } else if (maxFeaturesTileDraw != null) {

                    // Draw the max features tile
                    bitmap = maxFeaturesTileDraw.drawTile(tileWidth, tileHeight, tileCount, results);
                }

            }
        } finally {
            results.close();
        }

        return bitmap;
    }

    /**
     * Query for feature result count in the x, y, and zoom
     *
     * @param x    x coordinate
     * @param y    y coordinate
     * @param zoom zoom level
     * @return feature count
     * @since 1.1.0
     */
    public long queryIndexedFeaturesCount(int x, int y, int zoom) {

        // Get the web mercator bounding box
        BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        // Query for the count of geometries matching the bounds in the index
        long count = queryIndexedFeaturesCount(webMercatorBoundingBox);

        return count;
    }

    /**
     * Query for feature result count in the bounding box
     *
     * @param webMercatorBoundingBox web mercator bounding box
     * @return feature count
     * @since 3.1.1
     */
    public long queryIndexedFeaturesCount(BoundingBox webMercatorBoundingBox) {

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
     * @param x    x coordinate
     * @param y    y coordinate
     * @param zoom zoom level
     * @return feature index results
     * @since 3.1.1
     */
    public FeatureIndexResults queryIndexedFeatures(int x, int y, int zoom) {

        // Get the web mercator bounding box
        BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        // Query for the geometries matching the bounds in the index
        return queryIndexedFeatures(webMercatorBoundingBox);
    }

    /**
     * Query for feature results in the bounding box
     *
     * @param webMercatorBoundingBox web mercator bounding box
     * @return feature index results
     * @since 1.1.0
     */
    public FeatureIndexResults queryIndexedFeatures(BoundingBox webMercatorBoundingBox) {

        // Create an expanded bounding box to handle features outside the tile
        // that overlap
        BoundingBox expandedQueryBoundingBox = expandBoundingBox(webMercatorBoundingBox);

        // Query for geometries matching the bounds in the index
        FeatureIndexResults results = indexManager.query(expandedQueryBoundingBox, WEB_MERCATOR_PROJECTION);

        return results;
    }

    /**
     * Create an expanded bounding box to handle features outside the tile that
     * overlap
     *
     * @param webMercatorBoundingBox web mercator bounding box
     * @return bounding box
     * @since 3.1.1
     */
    public BoundingBox expandBoundingBox(BoundingBox webMercatorBoundingBox) {

        // Create an expanded bounding box to handle features outside the tile
        // that overlap
        double minLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(
                tileWidth, webMercatorBoundingBox, 0 - widthOverlap);
        double maxLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(
                tileWidth, webMercatorBoundingBox, tileWidth + widthOverlap);
        double maxLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(
                tileHeight, webMercatorBoundingBox, 0 - heightOverlap);
        double minLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(
                tileHeight, webMercatorBoundingBox, tileHeight + heightOverlap);
        BoundingBox expandedQueryBoundingBox = new BoundingBox(minLongitude,
                minLatitude, maxLongitude, maxLatitude);

        return expandedQueryBoundingBox;
    }

    /**
     * Draw a tile bitmap from the x, y, and zoom level by querying all features. This could
     * be very slow if there are a lot of features
     *
     * @param x    x coordinate
     * @param y    y coordinate
     * @param zoom zoom level
     * @return drawn bitmap, or null
     */
    public Bitmap drawTileQueryAll(int x, int y, int zoom) {

        BoundingBox boundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        Bitmap bitmap = null;

        // Query for all features
        FeatureCursor cursor = featureDao.queryForAll();

        try {

            int totalCount = cursor.getCount();

            // Draw if at least one geometry exists
            if (totalCount > 0) {

                if (maxFeaturesPerTile == null || totalCount <= maxFeaturesPerTile) {

                    // Draw the tile bitmap
                    bitmap = drawTile(zoom, boundingBox, cursor);

                } else if (maxFeaturesTileDraw != null) {

                    // Draw the unindexed max features tile
                    bitmap = maxFeaturesTileDraw.drawUnindexedTile(tileWidth, tileHeight, totalCount, cursor);
                }

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
     * Get the feature style for the feature row and geometry type
     *
     * @param featureRow feature row
     * @return feature style
     */
    protected FeatureStyle getFeatureStyle(FeatureRow featureRow) {
        FeatureStyle featureStyle = null;
        if (featureTableStyles != null) {
            featureStyle = featureTableStyles.getFeatureStyle(featureRow);
        }
        return featureStyle;
    }

    /**
     * Get the feature style for the feature row and geometry type
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     * @return feature style
     */
    protected FeatureStyle getFeatureStyle(FeatureRow featureRow, GeometryType geometryType) {
        FeatureStyle featureStyle = null;
        if (featureTableStyles != null) {
            featureStyle = featureTableStyles.getFeatureStyle(featureRow, geometryType);
        }
        return featureStyle;
    }

    /**
     * Get the icon bitmap from the icon row
     *
     * @param iconRow icon row
     * @return icon bitmap
     */
    protected Bitmap getIcon(IconRow iconRow) {
        return iconCache.createIcon(iconRow, 1.0f);
    }

    /**
     * Get the point paint for the feature style, or return the default paint
     *
     * @param featureStyle feature style
     * @return paint
     */
    protected Paint getPointPaint(FeatureStyle featureStyle) {

        Paint paint = getFeatureStylePaint(featureStyle, FeatureDrawType.CIRCLE);

        if (paint == null) {
            paint = pointPaint;
        }

        return paint;
    }

    /**
     * Get the line paint for the feature style, or return the default paint
     *
     * @param featureStyle feature style
     * @return paint
     */
    protected Paint getLinePaint(FeatureStyle featureStyle) {

        Paint paint = getFeatureStylePaint(featureStyle, FeatureDrawType.STROKE);

        if (paint == null) {
            paint = linePaint;
        }

        return paint;
    }

    /**
     * Get the polygon paint for the feature style, or return the default paint
     *
     * @param featureStyle feature style
     * @return paint
     */
    protected Paint getPolygonPaint(FeatureStyle featureStyle) {

        Paint paint = getFeatureStylePaint(featureStyle, FeatureDrawType.STROKE);

        if (paint == null) {
            paint = polygonPaint;
        }

        return paint;
    }

    /**
     * Get the polygon fill paint for the feature style, or return the default paint
     *
     * @param featureStyle feature style
     * @return paint
     */
    protected Paint getPolygonFillPaint(FeatureStyle featureStyle) {

        Paint paint = null;

        boolean hasStyleColor = false;

        if (featureStyle != null) {

            StyleRow style = featureStyle.getStyle();

            if (style != null) {

                if (style.hasFillColor()) {
                    paint = getStylePaint(style, FeatureDrawType.FILL);
                } else {
                    hasStyleColor = style.hasColor();
                }

            }

        }

        if (paint == null && !hasStyleColor && fillPolygon) {
            paint = polygonFillPaint;
        }

        return paint;
    }

    /**
     * Get the feature style paint from cache, or create and cache it
     *
     * @param featureStyle feature style
     * @param drawType     draw type
     * @return feature style paint
     */
    private Paint getFeatureStylePaint(FeatureStyle featureStyle, FeatureDrawType drawType) {

        Paint paint = null;

        if (featureStyle != null) {

            StyleRow style = featureStyle.getStyle();

            if (style != null && style.hasColor()) {

                paint = getStylePaint(style, drawType);

            }
        }

        return paint;
    }

    /**
     * Get the style paint from cache, or create and cache it
     *
     * @param style    style row
     * @param drawType draw type
     * @return paint
     */
    private Paint getStylePaint(StyleRow style, FeatureDrawType drawType) {

        Paint paint = featurePaintCache.getPaint(style, drawType);

        if (paint == null) {

            Color color = null;
            Style paintStyle = null;
            Float strokeWidth = null;

            switch (drawType) {
                case CIRCLE:
                    color = style.getColorOrDefault();
                    paintStyle = Style.FILL;
                    break;
                case STROKE:
                    color = style.getColorOrDefault();
                    paintStyle = Style.STROKE;
                    strokeWidth = (float) style.getWidthOrDefault();
                    break;
                case FILL:
                    color = style.getFillColor();
                    paintStyle = Style.FILL;
                    strokeWidth = (float) style.getWidthOrDefault();
                    break;
                default:
                    throw new GeoPackageException("Unsupported Draw Type: " + drawType);
            }

            Paint stylePaint = new Paint();
            stylePaint.setAntiAlias(true);
            stylePaint.setStyle(paintStyle);
            stylePaint.setColor(color.getColorWithAlpha());
            if (strokeWidth != null) {
                stylePaint.setStrokeWidth(strokeWidth);
            }

            synchronized (featurePaintCache) {

                paint = featurePaintCache.getPaint(style, drawType);

                if (paint == null) {
                    featurePaintCache.setPaint(style, drawType, stylePaint);
                    paint = stylePaint;
                }

            }
        }

        return paint;
    }

    /**
     * Determine if the bitmap is a transparent image (must be in expected tile dimensions)
     *
     * @param bitmap bitmap
     * @return true if transparent
     */
    protected boolean isTransparent(Bitmap bitmap) {
        return bitmap != null && emptyImage.sameAs(bitmap);
    }

    /**
     * Check if the bitmap was drawn upon (non null and not transparent). Return the same bitmap if drawn, else recycle non null bitmaps and return null
     *
     * @param bitmap bitmap
     * @return drawn bitmap or null
     */
    protected Bitmap checkIfDrawn(Bitmap bitmap) {
        if (isTransparent(bitmap)) {
            bitmap.recycle();
            bitmap = null;
        }
        return bitmap;
    }

    /**
     * Create an empty image for transparent comparison
     */
    private void createEmptyImage() {
        if (emptyImage != null) {
            emptyImage.recycle();
        }
        emptyImage = createNewBitmap();
    }

    /**
     * Draw a tile bitmap from feature index results
     *
     * @param zoom                   zoom level
     * @param webMercatorBoundingBox web mercator bounding box
     * @param results                feature index results
     * @return tile
     * @since 2.0.0
     */
    public abstract Bitmap drawTile(int zoom, BoundingBox webMercatorBoundingBox, FeatureIndexResults results);

    /**
     * Draw a tile bitmap from feature geometries in the provided cursor
     *
     * @param zoom                   zoom level
     * @param webMercatorBoundingBox web mercator bounding box
     * @param cursor                 feature cursor
     * @return tile
     * @since 2.0.0
     */
    public abstract Bitmap drawTile(int zoom, BoundingBox webMercatorBoundingBox, FeatureCursor cursor);

    /**
     * Draw a tile bitmap from the feature rows
     *
     * @param zoom                   zoom level
     * @param webMercatorBoundingBox web mercator bounding box
     * @param featureRow             feature row
     * @return tile
     * @since 2.0.0
     */
    public abstract Bitmap drawTile(int zoom, BoundingBox webMercatorBoundingBox, List<FeatureRow> featureRow);

}
