package mil.nga.geopackage.tiles.features;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.R;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.geom.map.GoogleMapShape;
import mil.nga.geopackage.geom.map.GoogleMapShapeConverter;
import mil.nga.geopackage.geom.map.MultiLatLng;
import mil.nga.geopackage.geom.map.MultiPolygonOptions;
import mil.nga.geopackage.geom.map.MultiPolylineOptions;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.Point;

/**
 * Tiles generated from features
 *
 * @author osbornb
 */
public class FeatureTiles {

    /**
     * WGS84 Projection
     */
    private static final Projection WGS_84_PROJECTION = ProjectionFactory
            .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

    /**
     * Web Mercator Projection
     */
    private static final Projection WEB_MERCATOR_PROJECTION = ProjectionFactory
            .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

    /**
     * Context
     */
    private final Context context;

    /**
     * Tile data access object
     */
    private final FeatureDao featureDao;

    /**
     * When not null, features are retrieved using a feature index
     */
    private FeatureIndexManager indexManager;

    /**
     * Tile height
     */
    private int tileWidth;

    /**
     * Tile height
     */
    private int tileHeight;

    /**
     * Compress format
     */
    private CompressFormat compressFormat;

    /**
     * Point radius
     */
    private float pointRadius;

    /**
     * Point paint
     */
    private Paint pointPaint = new Paint();

    /**
     * Optional point icon in place of a drawn circle
     */
    private FeatureTilePointIcon pointIcon;

    /**
     * Line paint
     */
    private Paint linePaint = new Paint();

    /**
     * Polygon paint
     */
    private Paint polygonPaint = new Paint();

    /**
     * Fill polygon flag
     */
    private boolean fillPolygon;

    /**
     * Polygon fill paint
     */
    private Paint polygonFillPaint = new Paint();

    /**
     * Height overlapping pixels between tile images
     */
    private float heightOverlap;

    /**
     * Width overlapping pixels between tile images
     */
    private float widthOverlap;

    /**
     * Optional max features per tile. When more features than this value exist for creating a
     * single tile, the tile is not created
     */
    private Integer maxFeaturesPerTile;

    /**
     * When not null and the number of features is greater than the max features per tile,
     * used to draw tiles for those tiles with more features than the max
     *
     * @see CustomFeaturesTile
     * @see mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile custom features tile implementation
     */
    private CustomFeaturesTile maxFeaturesTileDraw;

    /**
     * Constructor
     *
     * @param context
     * @param featureDao
     */
    public FeatureTiles(Context context, FeatureDao featureDao) {
        this.context = context;
        this.featureDao = featureDao;

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
     * Draw a tile bitmap from the x, y, and zoom level by querying all features. This could
     * be very slow if there are a lot of features
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

        // Create an expanded bounding box to handle features outside the tile that overlap
        double minLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(tileWidth, webMercatorBoundingBox, 0 - widthOverlap);
        double maxLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(tileWidth, webMercatorBoundingBox, tileWidth + widthOverlap);
        double maxLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(tileHeight, webMercatorBoundingBox, 0 - heightOverlap);
        double minLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(tileHeight, webMercatorBoundingBox, tileHeight + heightOverlap);
        BoundingBox expandedQueryBoundingBox = new BoundingBox(
                minLongitude,
                maxLongitude,
                minLatitude,
                maxLatitude);

        Bitmap bitmap = null;

        // Query for geometries matching the bounds in the index
        FeatureIndexResults results = indexManager.query(expandedQueryBoundingBox, WEB_MERCATOR_PROJECTION);

        try {

            Long tileCount = null;
            if (maxFeaturesPerTile != null) {
                tileCount = results.count();
            }

            if (maxFeaturesPerTile == null || tileCount <= maxFeaturesPerTile.longValue()) {

                // Create bitmap and canvas
                bitmap = Bitmap.createBitmap(tileWidth,
                        tileHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);

                // WGS84 to web mercator projection and google shape converter
                ProjectionTransform wgs84ToWebMercatorTransform = WGS_84_PROJECTION.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
                GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                        featureDao.getProjection());

                for (FeatureRow featureRow : results) {
                    drawFeature(webMercatorBoundingBox, wgs84ToWebMercatorTransform, canvas, featureRow, converter);
                }

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
                bitmap = drawTile(boundingBox, cursor);

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
     * Draw a tile bitmap from feature geometries in the provided cursor
     *
     * @param boundingBox
     * @param cursor
     * @return
     */
    public Bitmap drawTile(BoundingBox boundingBox, FeatureCursor cursor) {

        Bitmap bitmap = Bitmap.createBitmap(tileWidth,
                tileHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        ProjectionTransform wgs84ToWebMercatorTransform = WGS_84_PROJECTION.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                featureDao.getProjection());

        while (cursor.moveToNext()) {
            FeatureRow row = cursor.getRow();
            drawFeature(boundingBox, wgs84ToWebMercatorTransform, canvas, row, converter);
        }

        cursor.close();

        return bitmap;
    }

    /**
     * Draw a tile bitmap from the feature rows
     *
     * @param boundingBox
     * @param featureRow
     * @return
     */
    public Bitmap drawTile(BoundingBox boundingBox, List<FeatureRow> featureRow) {

        Bitmap bitmap = Bitmap.createBitmap(tileWidth,
                tileHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        ProjectionTransform wgs84ToWebMercatorTransform = WGS_84_PROJECTION.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                featureDao.getProjection());

        for (FeatureRow row : featureRow) {
            drawFeature(boundingBox, wgs84ToWebMercatorTransform, canvas, row, converter);
        }

        return bitmap;
    }

    /**
     * Draw the feature on the canvas
     *
     * @param boundingBox
     * @param transform
     * @param canvas
     * @param row
     * @param converter
     */
    private void drawFeature(BoundingBox boundingBox, ProjectionTransform transform, Canvas canvas, FeatureRow row, GoogleMapShapeConverter converter) {
        GeoPackageGeometryData geomData = row.getGeometry();
        if (geomData != null) {
            Geometry geometry = geomData.getGeometry();
            GoogleMapShape shape = converter.toShape(geometry);
            drawShape(boundingBox, transform, canvas, shape);
        }
    }

    /**
     * Draw the shape on the canvas
     *
     * @param boundingBox
     * @param transform
     * @param canvas
     * @param shape
     */
    private void drawShape(BoundingBox boundingBox, ProjectionTransform transform, Canvas canvas, GoogleMapShape shape) {

        Object shapeObject = shape.getShape();

        switch (shape.getShapeType()) {

            case LAT_LNG:
                LatLng latLng = (LatLng) shapeObject;
                drawLatLng(boundingBox, transform, canvas, pointPaint, latLng);
                break;
            case POLYLINE_OPTIONS:
                PolylineOptions polylineOptions = (PolylineOptions) shapeObject;
                Path linePath = new Path();
                addPolyline(boundingBox, transform, linePath, polylineOptions);
                drawLinePath(canvas, linePath);
                break;
            case POLYGON_OPTIONS:
                PolygonOptions polygonOptions = (PolygonOptions) shapeObject;
                Path polygonPath = new Path();
                addPolygon(boundingBox, transform, polygonPath, polygonOptions);
                drawPolygonPath(canvas, polygonPath);
                break;
            case MULTI_LAT_LNG:
                MultiLatLng multiLatLng = (MultiLatLng) shapeObject;
                for (LatLng latLngFromMulti : multiLatLng.getLatLngs()) {
                    drawLatLng(boundingBox, transform, canvas, pointPaint, latLngFromMulti);
                }
                break;
            case MULTI_POLYLINE_OPTIONS:
                MultiPolylineOptions multiPolylineOptions = (MultiPolylineOptions) shapeObject;
                Path multiLinePath = new Path();
                for (PolylineOptions polyline : multiPolylineOptions.getPolylineOptions()) {
                    addPolyline(boundingBox, transform, multiLinePath, polyline);
                }
                drawLinePath(canvas, multiLinePath);
                break;
            case MULTI_POLYGON_OPTIONS:
                MultiPolygonOptions multiPolygonOptions = (MultiPolygonOptions) shapeObject;
                Path multiPolygonPath = new Path();
                for (PolygonOptions polygon : multiPolygonOptions.getPolygonOptions()) {
                    addPolygon(boundingBox, transform, multiPolygonPath, polygon);
                }
                drawPolygonPath(canvas, multiPolygonPath);
                break;
            case COLLECTION:
                List<GoogleMapShape> shapes = (List<GoogleMapShape>) shapeObject;
                for (GoogleMapShape listShape : shapes) {
                    drawShape(boundingBox, transform, canvas, listShape);
                }
                break;
        }

    }

    /**
     * Draw the line path on the canvas
     *
     * @param canvas
     * @param path
     */
    private void drawLinePath(Canvas canvas, Path path) {
        canvas.drawPath(path, linePaint);
    }

    /**
     * Draw the path on the canvas
     *
     * @param canvas
     * @param path
     */
    private void drawPolygonPath(Canvas canvas, Path path) {
        canvas.drawPath(path, polygonPaint);
        if (fillPolygon) {
            path.setFillType(Path.FillType.EVEN_ODD);
            canvas.drawPath(path, polygonFillPaint);
        }
    }

    /**
     * Add the polyline to the path
     *
     * @param boundingBox
     * @param path
     * @param polyline
     */
    private void addPolyline(BoundingBox boundingBox, ProjectionTransform transform, Path path, PolylineOptions polyline) {
        List<LatLng> points = polyline.getPoints();
        if (points.size() >= 2) {

            for (int i = 0; i < points.size(); i++) {
                LatLng latLng = points.get(i);
                Point point = getPoint(transform, latLng);
                float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                        point.getX());
                float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                        point.getY());
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
        }
    }

    /**
     * Add the polygon on the canvas
     *
     * @param boundingBox
     * @param transform
     * @param path
     * @param polygon
     */
    private void addPolygon(BoundingBox boundingBox, ProjectionTransform transform, Path path, PolygonOptions polygon) {
        List<LatLng> points = polygon.getPoints();
        if (points.size() >= 2) {
            addRing(boundingBox, transform, path, points);

            // Add the holes
            for (List<LatLng> hole : polygon.getHoles()) {
                if (hole.size() >= 2) {
                    addRing(boundingBox, transform, path, hole);
                }
            }
        }
    }

    /**
     * Add a ring
     *
     * @param boundingBox
     * @param transform
     * @param path
     * @param points
     */
    private void addRing(BoundingBox boundingBox, ProjectionTransform transform, Path path, List<LatLng> points) {

        for (int i = 0; i < points.size(); i++) {
            LatLng latLng = points.get(i);
            Point point = getPoint(transform, latLng);
            float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                    point.getX());
            float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                    point.getY());
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
    }

    /**
     * Draw the lat lng on the canvas
     *
     * @param boundingBox
     * @param transform
     * @param canvas
     * @param paint
     * @param latLng
     */
    private void drawLatLng(BoundingBox boundingBox, ProjectionTransform transform, Canvas canvas, Paint paint, LatLng latLng) {

        Point point = getPoint(transform, latLng);
        float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                point.getX());
        float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                point.getY());

        if (pointIcon != null) {
            if (x >= 0 - pointIcon.getWidth() && x <= tileWidth + pointIcon.getWidth() && y >= 0 - pointIcon.getHeight() && y <= tileHeight + pointIcon.getHeight()) {
                canvas.drawBitmap(pointIcon.getIcon(), x - pointIcon.getXOffset(), y - pointIcon.getYOffset(), paint);
            }
        } else {
            if (x >= 0 - pointRadius && x <= tileWidth + pointRadius && y >= 0 - pointRadius && y <= tileHeight + pointRadius) {
                canvas.drawCircle(x, y, pointRadius, paint);
            }
        }

    }

    /**
     * Get the web mercator point
     *
     * @param transform
     * @param point
     * @return
     */
    private Point getPoint(ProjectionTransform transform, LatLng point) {
        double[] lonLat = transform.transform(point.longitude, point.latitude);
        return new Point(lonLat[0], lonLat[1]);
    }

}
