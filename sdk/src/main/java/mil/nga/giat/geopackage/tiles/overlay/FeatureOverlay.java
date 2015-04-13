package mil.nga.giat.geopackage.tiles.overlay;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.IOException;
import java.util.List;

import mil.nga.giat.geopackage.BoundingBox;
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
import mil.nga.giat.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.giat.wkb.geom.Geometry;

/**
 * Feature overlay which draws tiles from a feature table
 *
 * @author osbornb
 */
public class FeatureOverlay implements TileProvider {

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
     * Line path
     */
    private Path linePath = new Path();

    /**
     * Polygon paint
     */
    private Paint polygonPaint = new Paint();

    /**
     * Polygon path
     */
    private Path polygonPath = new Path();

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
     * @param featureDao
     */
    public FeatureOverlay(FeatureDao featureDao) {
        this.featureDao = featureDao;

        // TODO configure default values

        tileWidth = 256;
        tileHeight = 256;

        compressFormat = CompressFormat.PNG;

        pointPaint.setAntiAlias(true);
        pointRadius = 3.0f;

        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(3.0f);
        linePaint.setStyle(Paint.Style.STROKE);

        polygonPaint.setAntiAlias(true);
        polygonPaint.setStrokeWidth(3.0f);
        polygonPaint.setStyle(Paint.Style.STROKE);

        fillPolygon = false;
        polygonFillPaint.setAntiAlias(true);
        polygonFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public CompressFormat getCompressFormat() {
        return compressFormat;
    }

    public void setCompressFormat(CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public float getPointRadius() {
        return pointRadius;
    }

    public void setPointRadius(float pointRadius) {
        this.pointRadius = pointRadius;
    }

    public Paint getPointPaint() {
        return pointPaint;
    }

    public void setPointPaint(Paint pointPaint) {
        this.pointPaint = pointPaint;
    }

    public Paint getLinePaint() {
        return linePaint;
    }

    public void setLinePaint(Paint linePaint) {
        this.linePaint = linePaint;
    }

    public Paint getPolygonPaint() {
        return polygonPaint;
    }

    public void setPolygonPaint(Paint polygonPaint) {
        this.polygonPaint = polygonPaint;
    }

    public Path getLinePath() {
        return linePath;
    }

    public void setLinePath(Path linePath) {
        this.linePath = linePath;
    }

    public Path getPolygonPath() {
        return polygonPath;
    }

    public void setPolygonPath(Path polygonPath) {
        this.polygonPath = polygonPath;
    }

    public boolean isFillPolygon() {
        return fillPolygon;
    }

    public void setFillPolygon(boolean fillPolygon) {
        this.fillPolygon = fillPolygon;
    }

    public Paint getPolygonFillPaint() {
        return polygonFillPaint;
    }

    public void setPolygonFillPaint(Paint polygonFillPaint) {
        this.polygonFillPaint = polygonFillPaint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {

        BoundingBox boundingBox = TileBoundingBoxUtils
                .getBoundingBox(x, y, zoom);

        // TODO query by bounding box
        FeatureCursor cursor = featureDao.queryForAll();

        // Draw the tile bitmap
        Bitmap bitmap = drawTile(boundingBox, cursor);

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

        // Create the tile
        Tile tile = new Tile(tileWidth, tileHeight, tileData);

        return tile;
    }

    /**
     * Draw a Bitmap from feature geometries in the provided cursor
     *
     * @param boundingBox
     * @param cursor
     * @return
     */
    private Bitmap drawTile(BoundingBox boundingBox, FeatureCursor cursor) {

        Bitmap bitmap = Bitmap.createBitmap(tileWidth,
                tileHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                featureDao.getProjection());

        while (cursor.moveToNext()) {
            FeatureRow row = cursor.getRow();
            drawFeature(boundingBox, bitmap, canvas, row, converter);
        }

        cursor.close();

        return bitmap;
    }

    /**
     * Draw the feature on the canvas
     *
     * @param boundingBox
     * @param bitmap
     * @param canvas
     * @param row
     * @param converter
     */
    private void drawFeature(BoundingBox boundingBox, Bitmap bitmap, Canvas canvas, FeatureRow row, GoogleMapShapeConverter converter) {
        GeoPackageGeometryData geomData = row.getGeometry();
        if (geomData != null) {
            Geometry geometry = geomData.getGeometry();
            GoogleMapShape shape = converter.toShape(geometry);
            drawShape(boundingBox, canvas, shape);
        }
    }

    /**
     * Draw the shape on the canvas
     *
     * @param boundingBox
     * @param canvas
     * @param shape
     */
    private void drawShape(BoundingBox boundingBox, Canvas canvas, GoogleMapShape shape) {

        Object shapeObject = shape.getShape();

        switch (shape.getShapeType()) {

            case LAT_LNG:
                LatLng latLng = (LatLng) shapeObject;
                drawLatLng(boundingBox, canvas, pointPaint, latLng);
                break;
            case POLYLINE_OPTIONS:
                PolylineOptions polylineOptions = (PolylineOptions) shapeObject;
                linePath.reset();
                addPolyline(boundingBox, polylineOptions);
                drawLinePath(canvas);
                break;
            case POLYGON_OPTIONS:
                PolygonOptions polygonOptions = (PolygonOptions) shapeObject;
                polygonPath.reset();
                addPolygon(boundingBox, polygonOptions);
                drawPolygonPath(canvas);
                break;
            case MULTI_LAT_LNG:
                MultiLatLng multiLatLng = (MultiLatLng) shapeObject;
                for (LatLng latLngFromMulti : multiLatLng.getLatLngs()) {
                    drawLatLng(boundingBox, canvas, pointPaint, latLngFromMulti);
                }
                break;
            case MULTI_POLYLINE_OPTIONS:
                MultiPolylineOptions multiPolylineOptions = (MultiPolylineOptions) shapeObject;
                linePath.reset();
                for (PolylineOptions polyline : multiPolylineOptions.getPolylineOptions()) {
                    addPolyline(boundingBox, polyline);
                }
                drawLinePath(canvas);
                break;
            case MULTI_POLYGON_OPTIONS:
                MultiPolygonOptions multiPolygonOptions = (MultiPolygonOptions) shapeObject;
                polygonPath.reset();
                for (PolygonOptions polygon : multiPolygonOptions.getPolygonOptions()) {
                    addPolygon(boundingBox, polygon);
                }
                drawPolygonPath(canvas);
                break;
            case COLLECTION:
                List<GoogleMapShape> shapes = (List<GoogleMapShape>) shapeObject;
                for (GoogleMapShape listShape : shapes) {
                    drawShape(boundingBox, canvas, listShape);
                }
                break;
        }

    }

    /**
     * Draw the line path on the canvas
     *
     * @param canvas
     */
    private void drawLinePath(Canvas canvas) {
        canvas.drawPath(linePath, linePaint);
    }

    /**
     * Draw the path on the canvas
     *
     * @param canvas
     */
    private void drawPolygonPath(Canvas canvas) {
        canvas.drawPath(polygonPath, polygonPaint);
        if (fillPolygon) {
            polygonPath.setFillType(Path.FillType.EVEN_ODD);
            canvas.drawPath(polygonPath, polygonFillPaint);
        }
    }

    /**
     * Add the polyline to the path
     *
     * @param boundingBox
     * @param polyline
     */
    private void addPolyline(BoundingBox boundingBox, PolylineOptions polyline) {
        List<LatLng> points = polyline.getPoints();
        if (points.size() >= 2) {

            for (int i = 0; i < points.size(); i++) {
                LatLng point = points.get(i);
                float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                        point.longitude);
                float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                        point.latitude);
                if (i == 0) {
                    linePath.moveTo(x, y);
                } else {
                    linePath.lineTo(x, y);
                }
            }
        }
    }

    /**
     * Add the polygon on the canvas
     *
     * @param boundingBox
     * @param polygon
     */
    private void addPolygon(BoundingBox boundingBox, PolygonOptions polygon) {
        List<LatLng> points = polygon.getPoints();
        if (points.size() >= 2) {
            addRing(boundingBox, points);

            // Add the holes
            for (List<LatLng> hole : polygon.getHoles()) {
                if (hole.size() >= 2) {
                    addRing(boundingBox, hole);
                }
            }
        }
    }

    /**
     * Add a ring
     *
     * @param boundingBox
     * @param points
     */
    private void addRing(BoundingBox boundingBox, List<LatLng> points) {

        for (int i = 0; i < points.size(); i++) {
            LatLng point = point = points.get(i);
            float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                    point.longitude);
            float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                    point.latitude);
            if (i == 0) {
                polygonPath.moveTo(x, y);
            } else {
                polygonPath.lineTo(x, y);
            }
        }
        polygonPath.close();
    }

    /**
     * Draw the lat lng on the canvas
     *
     * @param boundingBox
     * @param canvas
     * @param paint
     * @param latLng
     */
    private void drawLatLng(BoundingBox boundingBox, Canvas canvas, Paint paint, LatLng latLng) {
        float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                latLng.longitude);
        float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                latLng.latitude);
        if (x >= 0 && x <= tileWidth && y >= 0 && y <= tileHeight) {
            canvas.drawCircle(x, y, pointRadius, paint);
        }
    }

}
