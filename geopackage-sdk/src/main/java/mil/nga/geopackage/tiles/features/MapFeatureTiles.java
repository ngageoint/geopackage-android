package mil.nga.geopackage.tiles.features;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import mil.nga.geopackage.BoundingBox;
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
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.Point;

/**
 * Google maps Feature Tiles implementation
 *
 * @author osbornb
 * @since 1.2.0
 */
public class MapFeatureTiles extends FeatureTiles {

    /**
     * Constructor
     *
     * @param context
     * @param featureDao
     */
    public MapFeatureTiles(Context context, FeatureDao featureDao) {
        super(context, featureDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(BoundingBox webMercatorBoundingBox, FeatureIndexResults results) {

        // Create bitmap and canvas
        Bitmap bitmap = createNewBitmap();
        Canvas canvas = new Canvas(bitmap);

        // WGS84 to web mercator projection and google shape converter
        ProjectionTransform wgs84ToWebMercatorTransform = getWgs84ToWebMercatorTransform();
        GoogleMapShapeConverter converter = new GoogleMapShapeConverter(
                featureDao.getProjection());

        for (FeatureRow featureRow : results) {
            drawFeature(webMercatorBoundingBox, wgs84ToWebMercatorTransform, canvas, featureRow, converter);
        }

        return bitmap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(BoundingBox boundingBox, FeatureCursor cursor) {

        Bitmap bitmap = createNewBitmap();
        Canvas canvas = new Canvas(bitmap);

        ProjectionTransform wgs84ToWebMercatorTransform = getWgs84ToWebMercatorTransform();
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
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(BoundingBox boundingBox, List<FeatureRow> featureRow) {

        Bitmap bitmap = createNewBitmap();
        Canvas canvas = new Canvas(bitmap);

        ProjectionTransform wgs84ToWebMercatorTransform = getWgs84ToWebMercatorTransform();
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
