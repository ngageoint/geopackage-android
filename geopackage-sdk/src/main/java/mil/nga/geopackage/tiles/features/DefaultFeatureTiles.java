package mil.nga.geopackage.tiles.features;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.wkb.geom.CompoundCurve;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryCollection;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.MultiLineString;
import mil.nga.wkb.geom.MultiPoint;
import mil.nga.wkb.geom.MultiPolygon;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;
import mil.nga.wkb.geom.PolyhedralSurface;

/**
 * Default Feature Tiles implementation using Android Graphics to draw tiles
 * from Well Known Binary Geometries
 *
 * @author osbornb
 * @since 1.3.1
 */
public class DefaultFeatureTiles extends FeatureTiles {

    /**
     * Constructor
     *
     * @param context
     * @param featureDao
     */
    public DefaultFeatureTiles(Context context, FeatureDao featureDao) {
        super(context, featureDao);
    }

    /**
     * Constructor, only for retrieving default feature attributes
     *
     * @param context
     */
    public DefaultFeatureTiles(Context context) {
        this(context, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(int zoom, BoundingBox webMercatorBoundingBox, FeatureIndexResults results) {

        // Create bitmap and canvas
        Bitmap bitmap = createNewBitmap();
        Canvas canvas = new Canvas(bitmap);

        ProjectionTransform transform = getProjectionToWebMercatorTransform(featureDao.getProjection());

        for (FeatureRow featureRow : results) {
            drawFeature(zoom, webMercatorBoundingBox, transform, canvas, featureRow);
        }

        return bitmap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(int zoom, BoundingBox boundingBox, FeatureCursor cursor) {

        Bitmap bitmap = createNewBitmap();
        Canvas canvas = new Canvas(bitmap);

        ProjectionTransform transform = getProjectionToWebMercatorTransform(featureDao.getProjection());

        while (cursor.moveToNext()) {
            FeatureRow row = cursor.getRow();
            drawFeature(zoom, boundingBox, transform, canvas, row);
        }

        cursor.close();

        return bitmap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(int zoom, BoundingBox boundingBox, List<FeatureRow> featureRow) {

        Bitmap bitmap = createNewBitmap();
        Canvas canvas = new Canvas(bitmap);

        ProjectionTransform transform = getProjectionToWebMercatorTransform(featureDao.getProjection());

        for (FeatureRow row : featureRow) {
            drawFeature(zoom, boundingBox, transform, canvas, row);
        }

        return bitmap;
    }

    /**
     * Draw the feature on the canvas
     *
     * @param zoom        zoom level
     * @param boundingBox
     * @param transform
     * @param canvas
     * @param row
     */
    private void drawFeature(int zoom, BoundingBox boundingBox, ProjectionTransform transform, Canvas canvas, FeatureRow row) {
        try {
            GeoPackageGeometryData geomData = row.getGeometry();
            if (geomData != null) {
                Geometry geometry = geomData.getGeometry();
                double simplifyTolerance = TileBoundingBoxUtils.toleranceDistance(zoom, tileWidth, tileHeight);
                drawShape(simplifyTolerance, boundingBox, transform, canvas, geometry);
            }
        } catch (Exception e) {
            Log.e(DefaultFeatureTiles.class.getSimpleName(), "Failed to draw feature in tile. Table: "
                    + featureDao.getTableName(), e);
        }
    }

    /**
     * Draw the geometry on the canvas
     *
     * @param simplifyTolerance simplify tolerance in meters
     * @param boundingBox
     * @param transform
     * @param canvas
     * @param geometry
     */
    private void drawShape(double simplifyTolerance, BoundingBox boundingBox, ProjectionTransform transform, Canvas canvas, Geometry geometry) {

        switch (geometry.getGeometryType()) {

            case POINT:
                Point point = (Point) geometry;
                drawPoint(boundingBox, transform, canvas, pointPaint, point);
                break;
            case LINESTRING:
            case CIRCULARSTRING:
                LineString lineString = (LineString) geometry;
                Path linePath = new Path();
                addLineString(simplifyTolerance, boundingBox, transform, linePath, lineString);
                drawLinePath(canvas, linePath);
                break;
            case POLYGON:
            case TRIANGLE:
                Polygon polygon = (Polygon) geometry;
                Path polygonPath = new Path();
                addPolygon(simplifyTolerance, boundingBox, transform, polygonPath, polygon);
                drawPolygonPath(canvas, polygonPath);
                break;
            case MULTIPOINT:
                MultiPoint multiPoint = (MultiPoint) geometry;
                for (Point pointFromMulti : multiPoint.getPoints()) {
                    drawPoint(boundingBox, transform, canvas, pointPaint, pointFromMulti);
                }
                break;
            case MULTILINESTRING:
                MultiLineString multiLineString = (MultiLineString) geometry;
                Path multiLinePath = new Path();
                for (LineString lineStringFromMulti : multiLineString.getLineStrings()) {
                    addLineString(simplifyTolerance, boundingBox, transform, multiLinePath, lineStringFromMulti);
                }
                drawLinePath(canvas, multiLinePath);
                break;
            case MULTIPOLYGON:
                MultiPolygon multiPolygon = (MultiPolygon) geometry;
                Path multiPolygonPath = new Path();
                for (Polygon polygonFromMulti : multiPolygon.getPolygons()) {
                    addPolygon(simplifyTolerance, boundingBox, transform, multiPolygonPath, polygonFromMulti);
                }
                drawPolygonPath(canvas, multiPolygonPath);
                break;
            case COMPOUNDCURVE:
                CompoundCurve compoundCurve = (CompoundCurve) geometry;
                Path compoundCurvePath = new Path();
                for (LineString lineStringFromCompoundCurve : compoundCurve.getLineStrings()) {
                    addLineString(simplifyTolerance, boundingBox, transform, compoundCurvePath, lineStringFromCompoundCurve);
                }
                drawLinePath(canvas, compoundCurvePath);
                break;
            case POLYHEDRALSURFACE:
            case TIN:
                PolyhedralSurface polyhedralSurface = (PolyhedralSurface) geometry;
                Path polyhedralSurfacePath = new Path();
                for (Polygon polygonFromPolyhedralSurface : polyhedralSurface.getPolygons()) {
                    addPolygon(simplifyTolerance, boundingBox, transform, polyhedralSurfacePath, polygonFromPolyhedralSurface);
                }
                drawPolygonPath(canvas, polyhedralSurfacePath);
                break;
            case GEOMETRYCOLLECTION:
                GeometryCollection<Geometry> geometryCollection = (GeometryCollection) geometry;
                List<Geometry> geometries = geometryCollection.getGeometries();
                for (Geometry geometryFromCollection : geometries) {
                    drawShape(simplifyTolerance, boundingBox, transform, canvas, geometryFromCollection);
                }
                break;
            default:
                throw new GeoPackageException("Unsupported Geometry Type: "
                        + geometry.getGeometryType().getName());
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
     * Add the linestring to the path
     *
     * @param simplifyTolerance simplify tolerance in meters
     * @param boundingBox
     * @param transform
     * @param path
     * @param lineString
     */
    private void addLineString(double simplifyTolerance, BoundingBox boundingBox, ProjectionTransform transform, Path path, LineString lineString) {

        List<Point> points = lineString.getPoints();

        if (points.size() >= 2) {

            // Try to simplify the number of points in the LineString
            points = simplifyPoints(simplifyTolerance, points);

            for (int i = 0; i < points.size(); i++) {
                Point point = points.get(i);
                Point webMercatorPoint = getPoint(transform, point);
                float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                        webMercatorPoint.getX());
                float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                        webMercatorPoint.getY());
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
     * @param simplifyTolerance simplify tolerance in meters
     * @param boundingBox
     * @param transform
     * @param path
     * @param polygon
     */
    private void addPolygon(double simplifyTolerance, BoundingBox boundingBox, ProjectionTransform transform, Path path, Polygon polygon) {
        List<LineString> rings = polygon.getRings();
        if (!rings.isEmpty()) {

            // Add the polygon points
            LineString polygonLineString = rings.get(0);
            List<Point> polygonPoints = polygonLineString.getPoints();
            if (polygonPoints.size() >= 2) {
                addRing(simplifyTolerance, boundingBox, transform, path, polygonPoints);

                // Add the holes
                for (int i = 1; i < rings.size(); i++) {
                    LineString holeLineString = rings.get(i);
                    List<Point> holePoints = holeLineString.getPoints();
                    if (holePoints.size() >= 2) {
                        addRing(simplifyTolerance, boundingBox, transform, path, holePoints);
                    }
                }
            }
        }
    }

    /**
     * Add a ring
     *
     * @param simplifyTolerance simplify tolerance in meters
     * @param boundingBox
     * @param transform
     * @param path
     * @param points
     */
    private void addRing(double simplifyTolerance, BoundingBox boundingBox, ProjectionTransform transform, Path path, List<Point> points) {

        // Try to simplify the number of points in the LineString
        points = simplifyPoints(simplifyTolerance, points);

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            Point webMercatorPoint = getPoint(transform, point);
            float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                    webMercatorPoint.getX());
            float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                    webMercatorPoint.getY());
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
    }

    /**
     * Draw the point on the canvas
     *
     * @param boundingBox
     * @param transform
     * @param canvas
     * @param paint
     * @param point
     */
    private void drawPoint(BoundingBox boundingBox, ProjectionTransform transform, Canvas canvas, Paint paint, Point point) {

        Point webMercatorPoint = getPoint(transform, point);
        float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                webMercatorPoint.getX());
        float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                webMercatorPoint.getY());

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
     * @return web mercator point
     */
    private Point getPoint(ProjectionTransform transform, Point point) {
        return transform.transform(point);
    }

}
