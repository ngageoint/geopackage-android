package mil.nga.giat.geopackage.test.tiles.features;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;

import java.sql.SQLException;

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.features.columns.GeometryColumns;
import mil.nga.giat.geopackage.features.user.FeatureDao;
import mil.nga.giat.geopackage.features.user.FeatureRow;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.projection.ProjectionConstants;
import mil.nga.giat.geopackage.schema.TableColumnKey;
import mil.nga.giat.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.giat.geopackage.tiles.features.FeatureTiles;
import mil.nga.giat.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.giat.wkb.geom.GeometryType;
import mil.nga.giat.wkb.geom.LineString;
import mil.nga.giat.wkb.geom.Point;
import mil.nga.giat.wkb.geom.Polygon;

/**
 * Test GeoPackage Feature Tiles, tiles created from features
 *
 * @author osbornb
 */
public class FeatureTilesTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTilesTest() {

    }

    /**
     * Test feature tiles
     *
     * @throws java.sql.SQLException
     */
    public void testFeatureTiles() throws SQLException {

        BoundingBox boundingBox = new BoundingBox();

        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey("feature_tiles",
                "geom"));
        geometryColumns.setGeometryType(GeometryType.GEOMETRY);
        geometryColumns.setZ((byte) 0);
        geometryColumns.setM((byte) 0);

        geoPackage.createFeatureTableWithMetadata(
                geometryColumns, boundingBox, ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        FeatureDao featureDao = geoPackage.getFeatureDao(geometryColumns);

        insertPoint(featureDao, 0, 0);
        insertPoint(featureDao, 0, 89);
        insertPoint(featureDao, 0, -89);
        insertPoint(featureDao, -179, 0);
        insertPoint(featureDao, 179, 0);

        insertFourPoints(featureDao, 179, 89);
        insertFourPoints(featureDao, 90, 45);

        insertFourLines(featureDao, new double[][]{{135.0, 67.5}, {90.0, 45.0}, {135.0, 45.0}});

        insertFourPolygons(featureDao, new double[][]{{60.0, 35.0}, {65.0, 15.0}, {15.0, 20.0}, {20.0, 40.0}}, new double[][]{{50.0, 30.0}, {48.0, 22.0}, {30.0, 23.0}, {25.0, 34.0}});

        FeatureTiles featureTiles = new FeatureTiles(activity, featureDao);

        Paint pointPaint = featureTiles.getPointPaint();
        pointPaint.setColor(Color.BLUE);

        Paint linePaint = featureTiles.getLinePaint();
        linePaint.setColor(Color.GREEN);

        Paint polygonPaint = featureTiles.getPolygonPaint();
        polygonPaint.setColor(Color.RED);

        featureTiles.setFillPolygon(true);
        Paint polygonFillPaint = featureTiles.getPolygonFillPaint();
        polygonFillPaint.setColor(Color.RED);
        polygonFillPaint.setAlpha(50);

        createTiles(featureTiles, 0, 1);

    }

    private void createTiles(FeatureTiles featureTiles, int minZoom, int maxZoom) {
        for (int i = minZoom; i <= maxZoom; i++) {
            createTiles(featureTiles, i);
        }
    }

    private void createTiles(FeatureTiles featureTiles, int zoom) {
        int tilesPerSide = TileBoundingBoxUtils.tilesPerSide(zoom);
        for (int i = 0; i < tilesPerSide; i++) {
            for (int j = 0; j < tilesPerSide; j++) {
                Bitmap bitmap = featureTiles.drawTile(i, j, zoom);
                assertTrue(bitmap.getByteCount() > 0);
                assertEquals(featureTiles.getTileWidth(), bitmap.getWidth());
                assertEquals(featureTiles.getTileHeight(), bitmap.getHeight());
            }
        }
    }

    private void insertFourPoints(FeatureDao featureDao, double x, double y) {
        insertPoint(featureDao, x, y);
        insertPoint(featureDao, x, -1 * y);
        insertPoint(featureDao, -1 * x, y);
        insertPoint(featureDao, -1 * x, -1 * y);
    }

    private void insertFourLines(FeatureDao featureDao, double[][] points) {
        insertLine(featureDao, convertPoints(points, false, false));
        insertLine(featureDao, convertPoints(points, true, false));
        insertLine(featureDao, convertPoints(points, false, true));
        insertLine(featureDao, convertPoints(points, true, true));
    }

    private void insertFourPolygons(FeatureDao featureDao, double[][]... points) {
        insertPolygon(featureDao, convertPoints(false, false, points));
        insertPolygon(featureDao, convertPoints(true, false, points));
        insertPolygon(featureDao, convertPoints(false, true, points));
        insertPolygon(featureDao, convertPoints(true, true, points));
    }

    private double[][] convertPoints(double[][] points, boolean negativeX, boolean negativeY) {

        double[][] newPoints = new double[points.length][2];
        for (int i = 0; i < points.length; i++) {
            newPoints[i][0] = negativeX ? points[i][0] * -1 : points[i][0];
            newPoints[i][1] = negativeY ? points[i][1] * -1 : points[i][1];
        }

        return newPoints;
    }

    private double[][][] convertPoints(boolean negativeX, boolean negativeY, double[][]... points) {

        double[][][] newPoints = new double[points.length][][];
        for (int i = 0; i < points.length; i++) {
            newPoints[i] = convertPoints(points[i], negativeX, negativeY);
        }

        return newPoints;
    }

    private void insertPoint(FeatureDao featureDao, double x, double y) {
        FeatureRow featureRow = featureDao.newRow();
        GeoPackageGeometryData pointGeomData = new GeoPackageGeometryData(
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        Point point = new Point(false, false, x, y);
        pointGeomData.setGeometry(point);
        featureRow.setGeometry(pointGeomData);
        featureDao.insert(featureRow);
    }

    private void insertLine(FeatureDao featureDao, double[][] points) {
        FeatureRow featureRow = featureDao.newRow();
        GeoPackageGeometryData pointGeomData = new GeoPackageGeometryData(
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        LineString lineString = getLineString(points);
        pointGeomData.setGeometry(lineString);
        featureRow.setGeometry(pointGeomData);
        featureDao.insert(featureRow);
    }

    private LineString getLineString(double[][] points) {
        LineString lineString = new LineString(false, false);
        for (int i = 0; i < points.length; i++) {
            Point point = new Point(false, false, points[i][0], points[i][1]);
            lineString.addPoint(point);
        }
        return lineString;
    }

    private void insertPolygon(FeatureDao featureDao, double[][]... points) {
        FeatureRow featureRow = featureDao.newRow();
        GeoPackageGeometryData pointGeomData = new GeoPackageGeometryData(
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        Polygon polygon = new Polygon(false, false);
        for (double[][] ring : points) {
            LineString lineString = getLineString(ring);
            polygon.addRing(lineString);
        }
        pointGeomData.setGeometry(polygon);
        featureRow.setGeometry(pointGeomData);
        featureDao.insert(featureRow);
    }

}
