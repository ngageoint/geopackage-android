package mil.nga.geopackage.tiles.features;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.style.FeatureStyle;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.sf.CompoundCurve;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryCollection;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;
import mil.nga.sf.LineString;
import mil.nga.sf.MultiLineString;
import mil.nga.sf.MultiPoint;
import mil.nga.sf.MultiPolygon;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;
import mil.nga.sf.PolyhedralSurface;
import mil.nga.sf.proj.ProjectionTransform;

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
     * @param context    context
     * @param featureDao feature dao
     */
    public DefaultFeatureTiles(Context context, FeatureDao featureDao) {
        super(context, featureDao);
    }

    /**
     * Constructor, auto creates the index manager for indexed tables and feature styles for styled tables
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @param featureDao feature dao
     */
    public DefaultFeatureTiles(Context context, GeoPackage geoPackage, FeatureDao featureDao) {
        super(context, geoPackage, featureDao);
    }

    /**
     * Constructor, only for retrieving default feature attributes
     *
     * @param context context
     */
    public DefaultFeatureTiles(Context context) {
        this(context, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(int zoom, BoundingBox boundingBox, FeatureIndexResults results) {

        // Create bitmap and canvas
        Bitmap bitmap = createNewBitmap();
        Canvas canvas = new Canvas(bitmap);

        ProjectionTransform transform = getProjectionToWebMercatorTransform(featureDao.getProjection());
        BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

        boolean drawn = false;
        for (FeatureRow featureRow : results) {
            if (drawFeature(zoom, boundingBox, expandedBoundingBox, transform, canvas, featureRow)) {
                drawn = true;
            }
        }

        if (!drawn) {
            bitmap.recycle();
            bitmap = null;
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
        BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

        boolean drawn = false;
        while (cursor.moveToNext()) {
            FeatureRow row = cursor.getRow();
            if (drawFeature(zoom, boundingBox, expandedBoundingBox, transform, canvas, row)) {
                drawn = true;
            }
        }

        cursor.close();
        if (!drawn) {
            bitmap.recycle();
            bitmap = null;
        }

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
        BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

        boolean drawn = false;
        for (FeatureRow row : featureRow) {
            if (drawFeature(zoom, boundingBox, expandedBoundingBox, transform, canvas, row)) {
                drawn = true;
            }
        }

        if (!drawn) {
            bitmap.recycle();
            bitmap = null;
        }

        return bitmap;
    }

    /**
     * Draw the feature on the canvas
     *
     * @param zoom                zoom level
     * @param boundingBox         bounding box
     * @param expandedBoundingBox expanded bounding box
     * @param transform           projection transform
     * @param canvas              canvas to draw on
     * @param row                 feature row
     * @return true if at least one feature was drawn
     */
    private boolean drawFeature(int zoom, BoundingBox boundingBox, BoundingBox expandedBoundingBox, ProjectionTransform transform, Canvas canvas, FeatureRow row) {

        boolean drawn = false;

        try {
            GeoPackageGeometryData geomData = row.getGeometry();
            if (geomData != null) {
                Geometry geometry = geomData.getGeometry();
                if (geometry != null) {

                    GeometryEnvelope envelope = geomData.getOrBuildEnvelope();
                    BoundingBox geometryBoundingBox = new BoundingBox(envelope);
                    BoundingBox transformedBoundingBox = geometryBoundingBox.transform(transform);

                    if (expandedBoundingBox.intersects(transformedBoundingBox, true)) {

                        double simplifyTolerance = TileBoundingBoxUtils.toleranceDistance(zoom, tileWidth, tileHeight);
                        drawShape(simplifyTolerance, boundingBox, transform, canvas, row, geometry);

                        drawn = true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(DefaultFeatureTiles.class.getSimpleName(), "Failed to draw feature in tile. Table: "
                    + featureDao.getTableName(), e);
        }

        return drawn;
    }

    /**
     * Draw the geometry on the canvas
     *
     * @param simplifyTolerance simplify tolerance in meters
     * @param boundingBox       bounding box
     * @param transform         projection transform
     * @param canvas            draw canvas
     * @param featureRow        feature row
     * @param geometry          feature geometry
     */
    private void drawShape(double simplifyTolerance, BoundingBox boundingBox, ProjectionTransform transform, Canvas canvas, FeatureRow featureRow, Geometry geometry) {

        GeometryType geometryType = geometry.getGeometryType();
        FeatureStyle featureStyle = getFeatureStyle(featureRow, geometryType);

        switch (geometryType) {

            case POINT:
                Point point = (Point) geometry;
                drawPoint(boundingBox, transform, canvas, point);
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
                drawPolygonPath(canvas, polygonPath, featureStyle);
                break;
            case MULTIPOINT:
                MultiPoint multiPoint = (MultiPoint) geometry;
                for (Point pointFromMulti : multiPoint.getPoints()) {
                    drawPoint(boundingBox, transform, canvas, pointFromMulti);
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
                drawPolygonPath(canvas, multiPolygonPath, featureStyle);
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
                drawPolygonPath(canvas, polyhedralSurfacePath, featureStyle);
                break;
            case GEOMETRYCOLLECTION:
                GeometryCollection<Geometry> geometryCollection = (GeometryCollection) geometry;
                List<Geometry> geometries = geometryCollection.getGeometries();
                for (Geometry geometryFromCollection : geometries) {
                    drawShape(simplifyTolerance, boundingBox, transform, canvas, featureRow, geometryFromCollection);
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
    private void drawPolygonPath(Canvas canvas, Path path, FeatureStyle featureStyle) {

        Paint pathPaint = getPolygonPaint(featureStyle);
        canvas.drawPath(path, pathPaint);

        Paint fillPaint = getPolygonFillPaint(featureStyle);
        if (fillPaint != null) {
            path.setFillType(Path.FillType.EVEN_ODD);
            canvas.drawPath(path, fillPaint);
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
     * @param boundingBox bounding box
     * @param transform   projection transform
     * @param canvas      draw canvas
     * @param point       point
     */
    private void drawPoint(BoundingBox boundingBox, ProjectionTransform transform, Canvas canvas, Point point) {

        Point webMercatorPoint = getPoint(transform, point);
        float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                webMercatorPoint.getX());
        float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                webMercatorPoint.getY());

        if (pointIcon != null) {
            if (x >= 0 - pointIcon.getWidth() && x <= tileWidth + pointIcon.getWidth() && y >= 0 - pointIcon.getHeight() && y <= tileHeight + pointIcon.getHeight()) {
                canvas.drawBitmap(pointIcon.getIcon(), x - pointIcon.getXOffset(), y - pointIcon.getYOffset(), pointPaint);
            }
        } else {
            if (x >= 0 - pointRadius && x <= tileWidth + pointRadius && y >= 0 - pointRadius && y <= tileHeight + pointRadius) {
                canvas.drawCircle(x, y, pointRadius, pointPaint);
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
