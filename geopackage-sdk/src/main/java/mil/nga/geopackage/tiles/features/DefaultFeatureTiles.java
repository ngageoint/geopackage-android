package mil.nga.geopackage.tiles.features;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.util.LruCache;

import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.style.FeatureStyle;
import mil.nga.geopackage.extension.style.IconRow;
import mil.nga.geopackage.extension.style.StyleRow;
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
     * Default max number of feature geometries to retain in cache
     *
     * @since 3.2.1
     */
    public static final int DEFAULT_GEOMETRY_CACHE_SIZE = 1000;

    /**
     * Geometry cache
     */
    protected final LruCache<Long, GeoPackageGeometryData> geometryCache = new LruCache<>(DEFAULT_GEOMETRY_CACHE_SIZE);

    /**
     * When true, geometries are cached.  Default is true
     */
    protected boolean cacheGeometries = true;

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
     * Constructor
     *
     * @param context    context
     * @param featureDao feature dao
     * @param density    display density: {@link android.util.DisplayMetrics#density}
     * @since 3.2.0
     */
    public DefaultFeatureTiles(Context context, FeatureDao featureDao, float density) {
        super(context, featureDao, density);
    }

    /**
     * Constructor
     *
     * @param context    context
     * @param featureDao feature dao
     * @param width      drawn tile width
     * @param height     drawn tile height
     * @since 3.2.0
     */
    public DefaultFeatureTiles(Context context, FeatureDao featureDao, int width, int height) {
        super(context, featureDao, width, height);
    }

    /**
     * Constructor, auto creates the index manager for indexed tables and feature styles for styled tables
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @param featureDao feature dao
     * @since 3.2.0
     */
    public DefaultFeatureTiles(Context context, GeoPackage geoPackage, FeatureDao featureDao) {
        super(context, geoPackage, featureDao);
    }

    /**
     * Constructor, auto creates the index manager for indexed tables and feature styles for styled tables
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @param featureDao feature dao
     * @param density    display density: {@link android.util.DisplayMetrics#density}
     * @since 3.2.0
     */
    public DefaultFeatureTiles(Context context, GeoPackage geoPackage, FeatureDao featureDao, float density) {
        super(context, geoPackage, featureDao, density);
    }

    /**
     * Constructor, auto creates the index manager for indexed tables and feature styles for styled tables
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @param featureDao feature dao
     * @param width      drawn tile width
     * @param height     drawn tile height
     * @since 3.2.0
     */
    public DefaultFeatureTiles(Context context, GeoPackage geoPackage, FeatureDao featureDao, int width, int height) {
        super(context, geoPackage, featureDao, width, height);
    }

    /**
     * Constructor, auto creates the index manager for indexed tables and feature styles for styled tables
     *
     * @param context    context
     * @param geoPackage GeoPackage
     * @param featureDao feature dao
     * @param density    display density: {@link android.util.DisplayMetrics#density}
     * @param width      drawn tile width
     * @param height     drawn tile height
     * @since 3.2.0
     */
    public DefaultFeatureTiles(Context context, GeoPackage geoPackage, FeatureDao featureDao, float density, int width, int height) {
        super(context, geoPackage, featureDao, density, width, height);
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
     * Is caching geometries enabled?
     *
     * @return true if caching geometries
     * @since 3.2.1
     */
    public boolean isCacheGeometries() {
        return cacheGeometries;
    }

    /**
     * Set the cache geometries flag
     *
     * @param cacheGeometries true to cache geometries
     * @since 3.2.1
     */
    public void setCacheGeometries(boolean cacheGeometries) {
        this.cacheGeometries = cacheGeometries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCache() {
        super.clearCache();
        clearGeometryCache();
    }

    /**
     * Clear the geometry cache
     *
     * @since 3.2.1
     */
    public void clearGeometryCache() {
        geometryCache.evictAll();
    }

    /**
     * Set / resize the geometry cache size
     *
     * @param size new size
     * @since 3.2.1
     */
    @TargetApi(21)
    public void setGeometryCacheSize(int size) {
        geometryCache.resize(size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(int zoom, BoundingBox boundingBox, FeatureIndexResults results) {

        FeatureTileCanvas canvas = new FeatureTileCanvas(tileWidth, tileHeight);

        ProjectionTransform transform = getProjectionToWebMercatorTransform(featureDao.getProjection());
        BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

        boolean drawn = false;
        for (FeatureRow featureRow : results) {
            if (drawFeature(zoom, boundingBox, expandedBoundingBox, transform, canvas, featureRow)) {
                drawn = true;
            }
        }
        results.close();

        Bitmap bitmap = null;
        if (drawn) {
            bitmap = canvas.createBitmap();
            bitmap = checkIfDrawn(bitmap);
        } else {
            canvas.recycle();
        }

        return bitmap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(int zoom, BoundingBox boundingBox, FeatureCursor cursor) {

        FeatureTileCanvas canvas = new FeatureTileCanvas(tileWidth, tileHeight);

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

        Bitmap bitmap = null;
        if (drawn) {
            bitmap = canvas.createBitmap();
            bitmap = checkIfDrawn(bitmap);
        } else {
            canvas.recycle();
        }

        return bitmap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(int zoom, BoundingBox boundingBox, List<FeatureRow> featureRow) {

        FeatureTileCanvas canvas = new FeatureTileCanvas(tileWidth, tileHeight);

        ProjectionTransform transform = getProjectionToWebMercatorTransform(featureDao.getProjection());
        BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

        boolean drawn = false;
        for (FeatureRow row : featureRow) {
            if (drawFeature(zoom, boundingBox, expandedBoundingBox, transform, canvas, row)) {
                drawn = true;
            }
        }

        Bitmap bitmap = null;
        if (drawn) {
            bitmap = canvas.createBitmap();
            bitmap = checkIfDrawn(bitmap);
        } else {
            canvas.recycle();
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
     * @param canvas              feature tile canvas
     * @param row                 feature row
     * @return true if at least one feature was drawn
     */
    private boolean drawFeature(int zoom, BoundingBox boundingBox, BoundingBox expandedBoundingBox, ProjectionTransform transform, FeatureTileCanvas canvas, FeatureRow row) {

        boolean drawn = false;

        try {

            GeoPackageGeometryData geomData = null;
            BoundingBox transformedBoundingBox = null;
            long rowId = -1;

            // Check the cache for the geometry data
            if (cacheGeometries) {
                rowId = row.getId();
                geomData = geometryCache.get(rowId);
                if (geomData != null) {
                    transformedBoundingBox = new BoundingBox(geomData.getEnvelope());
                }
            }

            if (geomData == null) {
                // Read the geometry
                geomData = row.getGeometry();
            }

            if (geomData != null) {
                Geometry geometry = geomData.getGeometry();
                if (geometry != null) {

                    if (transformedBoundingBox == null) {
                        GeometryEnvelope envelope = geomData.getOrBuildEnvelope();
                        BoundingBox geometryBoundingBox = new BoundingBox(envelope);
                        transformedBoundingBox = geometryBoundingBox.transform(transform);

                        if(cacheGeometries){
                            // Set the geometry envelope to the transformed bounding box
                            geomData.setEnvelope(transformedBoundingBox.buildEnvelope());
                        }
                    }

                    if(cacheGeometries){
                        // Cache the geometry
                        geometryCache.put(rowId, geomData);
                    }

                    if (expandedBoundingBox.intersects(transformedBoundingBox, true)) {

                        double simplifyTolerance = TileBoundingBoxUtils.toleranceDistance(zoom, tileWidth, tileHeight);
                        drawn = drawShape(simplifyTolerance, boundingBox, transform, canvas, row, geometry);

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
     * @param canvas            feature tile canvas
     * @param featureRow        feature row
     * @param geometry          feature geometry
     * @return true if drawn
     */
    private boolean drawShape(double simplifyTolerance, BoundingBox boundingBox, ProjectionTransform transform, FeatureTileCanvas canvas, FeatureRow featureRow, Geometry geometry) {

        boolean drawn = false;

        GeometryType geometryType = geometry.getGeometryType();
        FeatureStyle featureStyle = getFeatureStyle(featureRow, geometryType);

        switch (geometryType) {

            case POINT:
                Point point = (Point) geometry;
                drawn = drawPoint(boundingBox, transform, canvas, point, featureStyle);
                break;
            case LINESTRING:
            case CIRCULARSTRING:
                LineString lineString = (LineString) geometry;
                Path linePath = new Path();
                addLineString(simplifyTolerance, boundingBox, transform, linePath, lineString);
                drawn = drawLinePath(canvas, linePath, featureStyle);
                break;
            case POLYGON:
            case TRIANGLE:
                Polygon polygon = (Polygon) geometry;
                Path polygonPath = new Path();
                addPolygon(simplifyTolerance, boundingBox, transform, polygonPath, polygon);
                drawn = drawPolygonPath(canvas, polygonPath, featureStyle);
                break;
            case MULTIPOINT:
                MultiPoint multiPoint = (MultiPoint) geometry;
                for (Point pointFromMulti : multiPoint.getPoints()) {
                    drawn = drawPoint(boundingBox, transform, canvas, pointFromMulti, featureStyle) || drawn;
                }
                break;
            case MULTILINESTRING:
                MultiLineString multiLineString = (MultiLineString) geometry;
                Path multiLinePath = new Path();
                for (LineString lineStringFromMulti : multiLineString.getLineStrings()) {
                    addLineString(simplifyTolerance, boundingBox, transform, multiLinePath, lineStringFromMulti);
                }
                drawn = drawLinePath(canvas, multiLinePath, featureStyle);
                break;
            case MULTIPOLYGON:
                MultiPolygon multiPolygon = (MultiPolygon) geometry;
                Path multiPolygonPath = new Path();
                for (Polygon polygonFromMulti : multiPolygon.getPolygons()) {
                    addPolygon(simplifyTolerance, boundingBox, transform, multiPolygonPath, polygonFromMulti);
                }
                drawn = drawPolygonPath(canvas, multiPolygonPath, featureStyle);
                break;
            case COMPOUNDCURVE:
                CompoundCurve compoundCurve = (CompoundCurve) geometry;
                Path compoundCurvePath = new Path();
                for (LineString lineStringFromCompoundCurve : compoundCurve.getLineStrings()) {
                    addLineString(simplifyTolerance, boundingBox, transform, compoundCurvePath, lineStringFromCompoundCurve);
                }
                drawn = drawLinePath(canvas, compoundCurvePath, featureStyle);
                break;
            case POLYHEDRALSURFACE:
            case TIN:
                PolyhedralSurface polyhedralSurface = (PolyhedralSurface) geometry;
                Path polyhedralSurfacePath = new Path();
                for (Polygon polygonFromPolyhedralSurface : polyhedralSurface.getPolygons()) {
                    addPolygon(simplifyTolerance, boundingBox, transform, polyhedralSurfacePath, polygonFromPolyhedralSurface);
                }
                drawn = drawPolygonPath(canvas, polyhedralSurfacePath, featureStyle);
                break;
            case GEOMETRYCOLLECTION:
                @SuppressWarnings("unchecked")
                GeometryCollection<Geometry> geometryCollection = (GeometryCollection) geometry;
                List<Geometry> geometries = geometryCollection.getGeometries();
                for (Geometry geometryFromCollection : geometries) {
                    drawn = drawShape(simplifyTolerance, boundingBox, transform, canvas, featureRow, geometryFromCollection) || drawn;
                }
                break;
            default:
                throw new GeoPackageException("Unsupported Geometry Type: "
                        + geometry.getGeometryType().getName());
        }

        return drawn;
    }

    /**
     * Draw the line path on the canvas
     *
     * @param canvas       canvas
     * @param path         path
     * @param featureStyle feature style
     * @return true if drawn
     */
    private boolean drawLinePath(FeatureTileCanvas canvas, Path path, FeatureStyle featureStyle) {

        Canvas lineCanvas = canvas.getLineCanvas();

        Paint pathPaint = getLinePaint(featureStyle);
        lineCanvas.drawPath(path, pathPaint);

        return true;
    }

    /**
     * Draw the path on the canvas
     *
     * @param canvas       canvas
     * @param path         path
     * @param featureStyle feature style
     */
    private boolean drawPolygonPath(FeatureTileCanvas canvas, Path path, FeatureStyle featureStyle) {

        Canvas polygonCanvas = canvas.getPolygonCanvas();

        Paint fillPaint = getPolygonFillPaint(featureStyle);
        if (fillPaint != null) {
            path.setFillType(Path.FillType.EVEN_ODD);
            polygonCanvas.drawPath(path, fillPaint);
        }

        Paint pathPaint = getPolygonPaint(featureStyle);
        polygonCanvas.drawPath(path, pathPaint);

        return true;
    }

    /**
     * Add the linestring to the path
     *
     * @param simplifyTolerance simplify tolerance in meters
     * @param boundingBox       bounding box
     * @param transform         projection transform
     * @param path              path
     * @param lineString        line string
     */
    private void addLineString(double simplifyTolerance, BoundingBox boundingBox, ProjectionTransform transform, Path path, LineString lineString) {

        List<Point> points = lineString.getPoints();

        if (points.size() >= 2) {

            // Try to simplify the number of points in the LineString
            points = simplifyPoints(simplifyTolerance, points);

            for (int i = 0; i < points.size(); i++) {
                Point point = points.get(i);
                Point webMercatorPoint = transform.transform(point);
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
     * @param boundingBox       bounding box
     * @param transform         projection transform
     * @param path              path
     * @param polygon           polygon
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
     * @param boundingBox       bounding box
     * @param transform         projection transform
     * @param path              path
     * @param points            points
     */
    private void addRing(double simplifyTolerance, BoundingBox boundingBox, ProjectionTransform transform, Path path, List<Point> points) {

        // Try to simplify the number of points in the LineString
        points = simplifyPoints(simplifyTolerance, points);

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            Point webMercatorPoint = transform.transform(point);
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
     * @param boundingBox  bounding box
     * @param transform    projection transform
     * @param canvas       draw canvas
     * @param point        point
     * @param featureStyle feature style
     * @return true if drawn
     */
    private boolean drawPoint(BoundingBox boundingBox, ProjectionTransform transform, FeatureTileCanvas canvas, Point point, FeatureStyle featureStyle) {

        boolean drawn = false;

        Point webMercatorPoint = transform.transform(point);
        float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
                webMercatorPoint.getX());
        float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
                webMercatorPoint.getY());

        if (featureStyle != null && featureStyle.hasIcon()) {

            IconRow iconRow = featureStyle.getIcon();
            Bitmap icon = getIcon(iconRow);

            int width = icon.getWidth();
            int height = icon.getHeight();

            if (x >= 0 - width && x <= tileWidth + width && y >= 0 - height && y <= tileHeight + height) {

                float anchorU = (float) iconRow.getAnchorUOrDefault();
                float anchorV = (float) iconRow.getAnchorVOrDefault();

                float left = x - (anchorU * width);
                float right = left + width;
                float top = y - (anchorV * height);
                float bottom = top + height;

                RectF destination = new RectF(left, top, right, bottom);

                Canvas iconCanvas = canvas.getIconCanvas();
                iconCanvas.drawBitmap(icon, null, destination, pointPaint);
                drawn = true;

            }

        } else if (pointIcon != null) {

            float width = this.density * pointIcon.getWidth();
            float height = this.density * pointIcon.getHeight();
            if (x >= 0 - width && x <= tileWidth + width && y >= 0 - height && y <= tileHeight + height) {
                Canvas iconCanvas = canvas.getIconCanvas();
                float left = x - this.density * pointIcon.getXOffset();
                float top = y - this.density * pointIcon.getYOffset();
                RectF rect = new RectF(left, top, left + width, top + height);
                iconCanvas.drawBitmap(pointIcon.getIcon(), null, rect, pointPaint);
                drawn = true;
            }

        } else {

            Float radius = null;
            if (featureStyle != null) {
                StyleRow styleRow = featureStyle.getStyle();
                if (styleRow != null) {
                    radius = this.density * (float) (styleRow.getWidthOrDefault() / 2.0f);
                }
            }
            if (radius == null) {
                radius = this.density * pointRadius;
            }
            if (x >= 0 - radius && x <= tileWidth + radius && y >= 0 - radius && y <= tileHeight + radius) {
                Paint pointPaint = getPointPaint(featureStyle);
                Canvas pointCanvas = canvas.getPointCanvas();
                pointCanvas.drawCircle(x, y, radius, pointPaint);
                drawn = true;
            }

        }

        return drawn;
    }

}
