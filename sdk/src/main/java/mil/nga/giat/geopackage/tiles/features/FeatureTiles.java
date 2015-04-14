package mil.nga.giat.geopackage.tiles.features;

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

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.R;
import mil.nga.giat.geopackage.features.user.FeatureCursor;
import mil.nga.giat.geopackage.features.user.FeatureDao;
import mil.nga.giat.geopackage.features.user.FeatureRow;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.geom.map.GoogleMapShape;
import mil.nga.giat.geopackage.geom.map.GoogleMapShapeConverter;
import mil.nga.giat.geopackage.geom.map.MultiLatLng;
import mil.nga.giat.geopackage.geom.map.MultiPolygonOptions;
import mil.nga.giat.geopackage.geom.map.MultiPolylineOptions;
import mil.nga.giat.geopackage.io.BitmapConverter;
import mil.nga.giat.geopackage.projection.Projection;
import mil.nga.giat.geopackage.projection.ProjectionConstants;
import mil.nga.giat.geopackage.projection.ProjectionFactory;
import mil.nga.giat.geopackage.projection.ProjectionTransform;
import mil.nga.giat.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.giat.wkb.geom.Geometry;

/**
 * Tiles generated from features
 *
 * @author osbornb
 */
public class FeatureTiles {

    /**
     * WGS84 Projection
     */
    private static final Projection wgs84Projection = ProjectionFactory
            .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

    /**
     * Tile data access object
     */
    private final FeatureDao featureDao;

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
     * Constructor
     *
     * @param context
     * @param featureDao
     */
    public FeatureTiles(Context context, FeatureDao featureDao) {
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
     * Draw the tile and get the bytes from the x, y, and zoom level
     *
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    public byte[] drawTileBytes(int x, int y, int zoom) {

        Bitmap bitmap = drawTile(x, y, zoom);

        // Convert the bitmap to bytes
        byte[] tileData = null;
        try {
            tileData = BitmapConverter.toBytes(
                    bitmap, compressFormat);
        } catch (IOException e) {
            Log.e("Failed to create tile. x: " + x + ", y: "
                    + y + ", zoom: " + zoom, e.getMessage());
        } finally {
            bitmap.recycle();
        }

        return tileData;
    }

    /**
     * Draw a tile bitmap from the x, y, and zoom level
     *
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    public Bitmap drawTile(int x, int y, int zoom) {

        BoundingBox boundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(x, y, zoom);

        // TODO query by bounding box
        FeatureCursor cursor = featureDao.queryForAll();

        // Draw the tile bitmap
        Bitmap bitmap = drawTile(boundingBox, cursor);

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

        ProjectionTransform wgs84ToWebMercatorTransform = wgs84Projection.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                featureDao.getProjection());

        while (cursor.moveToNext()) {
            FeatureRow row = cursor.getRow();
            drawFeature(boundingBox, wgs84ToWebMercatorTransform, bitmap, canvas, row, converter);
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

        ProjectionTransform wgs84ToWebMercatorTransform = wgs84Projection.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
        GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                featureDao.getProjection());

        for (FeatureRow row : featureRow) {
            drawFeature(boundingBox, wgs84ToWebMercatorTransform, bitmap, canvas, row, converter);
        }

        return bitmap;
    }

    /**
     * Draw the feature on the canvas
     *
     * @param boundingBox
     * @param transform
     * @param bitmap
     * @param canvas
     * @param row
     * @param converter
     */
    private void drawFeature(BoundingBox boundingBox, ProjectionTransform transform, Bitmap bitmap, Canvas canvas, FeatureRow row, GoogleMapShapeConverter converter) {
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
                LatLng point = points.get(i);
                float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                        getLongitude(transform, point));
                float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                        getLatitude(transform, point));
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
            LatLng point = point = points.get(i);
            float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                    getLongitude(transform, point));
            float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                    getLatitude(transform, point));
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
        float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                getLongitude(transform, latLng));
        float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                getLatitude(transform, latLng));
        if (x >= 0 && x <= tileWidth && y >= 0 && y <= tileHeight) {
            canvas.drawCircle(x, y, pointRadius, paint);
        }
    }

    /**
     * Get the web mercator longitude
     *
     * @param transform
     * @param point
     * @return
     */
    private double getLongitude(ProjectionTransform transform, LatLng point) {
        return transform.transformLongitude(point.longitude);
    }

    /**
     * Get the web mercator latitude
     *
     * @param transform
     * @param point
     * @return
     */
    private double getLatitude(ProjectionTransform transform, LatLng point) {
        return transform.transformLatitude(point.latitude);
    }

}
