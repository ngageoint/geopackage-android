package mil.nga.geopackage.test;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.attributes.AttributesColumn;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;
import mil.nga.geopackage.attributes.AttributesTable;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.CrsWktExtension;
import mil.nga.geopackage.extension.GeometryExtensions;
import mil.nga.geopackage.extension.RTreeIndexExtension;
import mil.nga.geopackage.extension.WebPExtension;
import mil.nga.geopackage.extension.coverage.CoverageDataPng;
import mil.nga.geopackage.extension.coverage.CoverageDataTiff;
import mil.nga.geopackage.extension.coverage.GriddedCoverage;
import mil.nga.geopackage.extension.coverage.GriddedCoverageDao;
import mil.nga.geopackage.extension.coverage.GriddedCoverageDataType;
import mil.nga.geopackage.extension.coverage.GriddedCoverageEncodingType;
import mil.nga.geopackage.extension.coverage.GriddedTile;
import mil.nga.geopackage.extension.coverage.GriddedTileDao;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.metadata.Metadata;
import mil.nga.geopackage.metadata.MetadataDao;
import mil.nga.geopackage.metadata.MetadataScopeType;
import mil.nga.geopackage.metadata.reference.MetadataReference;
import mil.nga.geopackage.metadata.reference.MetadataReferenceDao;
import mil.nga.geopackage.metadata.reference.ReferenceScopeType;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.schema.columns.DataColumns;
import mil.nga.geopackage.schema.columns.DataColumnsDao;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintType;
import mil.nga.geopackage.schema.constraints.DataColumnConstraints;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintsDao;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.features.DefaultFeatureTiles;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;
import mil.nga.wkb.geom.CircularString;
import mil.nga.wkb.geom.CompoundCurve;
import mil.nga.wkb.geom.CurvePolygon;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryEnvelope;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.MultiLineString;
import mil.nga.wkb.geom.MultiPolygon;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;
import mil.nga.wkb.util.GeometryEnvelopeBuilder;

/**
 * Creates an example GeoPackage file
 *
 * @author osbornb
 */
public class GeoPackageExample extends BaseTestCase {

    private static final String GEOPACKAGE_NAME = "example";

    private static final boolean FEATURES = true;
    private static final boolean TILES = true;
    private static final boolean ATTRIBUTES = true;
    private static final boolean SCHEMA = true;
    private static final boolean NON_LINEAR_GEOMETRY_TYPES = true;
    private static final boolean RTREE_SPATIAL_INDEX = false;
    private static final boolean WEBP = true;
    private static final boolean CRS_WKT = true;
    private static final boolean METADATA = true;
    private static final boolean COVERAGE_DATA = true;
    private static final boolean GEOMETRY_INDEX = true;
    private static final boolean FEATURE_TILE_LINK = true;

    private static final String ID_COLUMN = "id";
    private static final String GEOMETRY_COLUMN = "geometry";
    private static final String TEXT_COLUMN = "text";
    private static final String REAL_COLUMN = "real";
    private static final String BOOLEAN_COLUMN = "boolean";
    private static final String BLOB_COLUMN = "blob";
    private static final String INTEGER_COLUMN = "integer";
    private static final String TEXT_LIMITED_COLUMN = "text_limited";
    private static final String BLOB_LIMITED_COLUMN = "blob_limited";
    private static final String DATE_COLUMN = "date";
    private static final String DATETIME_COLUMN = "datetime";

    private static final String LOG_NAME = GeoPackageExample.class.getSimpleName();

    public void testExample() throws SQLException, IOException {

        Log.i(LOG_NAME, "Creating: " + GEOPACKAGE_NAME);
        GeoPackage geoPackage = createGeoPackage(activity);

        Log.i(LOG_NAME, "CRS WKT Extension: " + CRS_WKT);
        if (CRS_WKT) {
            createCrsWktExtension(geoPackage);
        }

        Log.i(LOG_NAME, "Features: " + FEATURES);
        if (FEATURES) {

            createFeatures(geoPackage);

            Log.i(LOG_NAME, "Schema Extension: " + SCHEMA);
            if (SCHEMA) {
                createSchemaExtension(geoPackage);
            }

            Log.i(LOG_NAME, "Geometry Index Extension: " + GEOMETRY_INDEX);
            if (GEOMETRY_INDEX) {
                createGeometryIndexExtension(activity, geoPackage);
            }

            Log.i(LOG_NAME, "Feature Tile Link Extension: "
                    + FEATURE_TILE_LINK);
            if (FEATURE_TILE_LINK) {
                createFeatureTileLinkExtension(activity, geoPackage);
            }

            Log.i(LOG_NAME, "Non-Linear Geometry Types Extension: "
                    + NON_LINEAR_GEOMETRY_TYPES);
            if (NON_LINEAR_GEOMETRY_TYPES) {
                createNonLinearGeometryTypesExtension(geoPackage);
            }

            Log.i(LOG_NAME, "RTree Spatial Index Extension: "
                    + RTREE_SPATIAL_INDEX);
            if (RTREE_SPATIAL_INDEX) {
                createRTreeSpatialIndexExtension(geoPackage);
            }

        } else {
            Log.i(LOG_NAME, "Schema Extension: " + FEATURES);
            Log.i(LOG_NAME, "Geometry Index Extension: " + FEATURES);
            Log.i(LOG_NAME, "Feature Tile Link Extension: " + FEATURES);
            Log.i(LOG_NAME, "Non-Linear Geometry Types Extension: "
                    + FEATURES);
            Log.i(LOG_NAME, "RTree Spatial Index Extension: "
                    + FEATURES);
        }

        Log.i(LOG_NAME, "Tiles: " + TILES);
        if (TILES) {

            createTiles(activity, geoPackage);

            Log.i(LOG_NAME, "WebP Extension: " + WEBP);
            if (WEBP) {
                createWebPExtension(activity, geoPackage);
            }

        } else {
            Log.i(LOG_NAME, "WebP Extension: " + TILES);
        }

        Log.i(LOG_NAME, "Attributes: " + ATTRIBUTES);
        if (ATTRIBUTES) {
            createAttributes(geoPackage);
        }

        Log.i(LOG_NAME, "Metadata: " + METADATA);
        if (METADATA) {
            createMetadataExtension(geoPackage);
        }

        Log.i(LOG_NAME, "Coverage Data: " + COVERAGE_DATA);
        if (COVERAGE_DATA) {
            createCoverageDataExtension(geoPackage);
        }

        geoPackage.close();
        exportGeoPackage(activity);

    }

    private static void exportGeoPackage(Context context) {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            GeoPackageManager manager = GeoPackageFactory.getManager(context);

            File exportDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!exportDirectory.exists()) {
                exportDirectory.mkdir();
            }

            File exportedFile = new File(exportDirectory, GEOPACKAGE_NAME + "."
                    + TestConstants.GEO_PACKAGE_EXTENSION);
            if (exportedFile.exists()) {
                exportedFile.delete();
            }
            manager.exportGeoPackage(GEOPACKAGE_NAME, exportDirectory);

            Log.i(LOG_NAME, "Created: " + exportedFile.getPath());
            Log.i(LOG_NAME, "To copy GeoPackage, run: "
                    + "adb pull /storage/emulated/0/Documents/example.gpkg ~/git/geopackage-android");
        } else {
            Log.w(LOG_NAME,
                    "To export the GeoPackage, grant GeoPackageSDKTests Storage permission on the emulator or phone");
        }

    }

    private static GeoPackage createGeoPackage(Context context) {

        GeoPackageManager manager = GeoPackageFactory.getManager(context);

        manager.delete(GEOPACKAGE_NAME);

        manager.create(GEOPACKAGE_NAME);

        GeoPackage geoPackage = manager.open(GEOPACKAGE_NAME);
        if (geoPackage == null) {
            throw new GeoPackageException("Failed to open database");
        }

        return geoPackage;
    }

    private static void createFeatures(GeoPackage geoPackage)
            throws SQLException {

        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();

        SpatialReferenceSystem srs = srsDao.getOrCreateCode(
                ProjectionConstants.AUTHORITY_EPSG,
                (long) ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        geoPackage.createGeometryColumnsTable();

        Point point1 = new Point(-104.801918, 39.720014);
        String point1Name = "BIT Systems";

        createFeatures(geoPackage, srs, "point1", GeometryType.POINT, point1,
                point1Name);

        Point point2 = new Point(-77.196736, 38.753370);
        String point2Name = "NGA";

        createFeatures(geoPackage, srs, "point2", GeometryType.POINT, point2,
                point2Name);

        LineString line1 = new LineString();
        String line1Name = "East Lockheed Drive";
        line1.addPoint(new Point(-104.800614, 39.720721));
        line1.addPoint(new Point(-104.802174, 39.720726));
        line1.addPoint(new Point(-104.802584, 39.720660));
        line1.addPoint(new Point(-104.803088, 39.720477));
        line1.addPoint(new Point(-104.803474, 39.720209));

        createFeatures(geoPackage, srs, "line1", GeometryType.LINESTRING,
                line1, line1Name);

        LineString line2 = new LineString();
        String line2Name = "NGA";
        line2.addPoint(new Point(-77.196650, 38.756501));
        line2.addPoint(new Point(-77.196414, 38.755979));
        line2.addPoint(new Point(-77.195518, 38.755208));
        line2.addPoint(new Point(-77.195303, 38.755272));
        line2.addPoint(new Point(-77.195351, 38.755459));
        line2.addPoint(new Point(-77.195863, 38.755697));
        line2.addPoint(new Point(-77.196328, 38.756069));
        line2.addPoint(new Point(-77.196568, 38.756526));

        createFeatures(geoPackage, srs, "line2", GeometryType.LINESTRING,
                line2, line2Name);

        Polygon polygon1 = new Polygon();
        String polygon1Name = "BIT Systems";
        LineString ring1 = new LineString();
        ring1.addPoint(new Point(-104.802246, 39.720343));
        ring1.addPoint(new Point(-104.802246, 39.719753));
        ring1.addPoint(new Point(-104.802183, 39.719754));
        ring1.addPoint(new Point(-104.802184, 39.719719));
        ring1.addPoint(new Point(-104.802138, 39.719694));
        ring1.addPoint(new Point(-104.802097, 39.719691));
        ring1.addPoint(new Point(-104.802096, 39.719648));
        ring1.addPoint(new Point(-104.801646, 39.719648));
        ring1.addPoint(new Point(-104.801644, 39.719722));
        ring1.addPoint(new Point(-104.801550, 39.719723));
        ring1.addPoint(new Point(-104.801549, 39.720207));
        ring1.addPoint(new Point(-104.801648, 39.720207));
        ring1.addPoint(new Point(-104.801648, 39.720341));
        ring1.addPoint(new Point(-104.802246, 39.720343));
        polygon1.addRing(ring1);

        createFeatures(geoPackage, srs, "polygon1", GeometryType.POLYGON,
                polygon1, polygon1Name);

        Polygon polygon2 = new Polygon();
        String polygon2Name = "NGA Visitor Center";
        LineString ring2 = new LineString();
        ring2.addPoint(new Point(-77.195299, 38.755159));
        ring2.addPoint(new Point(-77.195203, 38.755080));
        ring2.addPoint(new Point(-77.195410, 38.754930));
        ring2.addPoint(new Point(-77.195350, 38.754884));
        ring2.addPoint(new Point(-77.195228, 38.754966));
        ring2.addPoint(new Point(-77.195135, 38.754889));
        ring2.addPoint(new Point(-77.195048, 38.754956));
        ring2.addPoint(new Point(-77.194986, 38.754906));
        ring2.addPoint(new Point(-77.194897, 38.754976));
        ring2.addPoint(new Point(-77.194953, 38.755025));
        ring2.addPoint(new Point(-77.194763, 38.755173));
        ring2.addPoint(new Point(-77.194827, 38.755224));
        ring2.addPoint(new Point(-77.195012, 38.755082));
        ring2.addPoint(new Point(-77.195041, 38.755104));
        ring2.addPoint(new Point(-77.195028, 38.755116));
        ring2.addPoint(new Point(-77.195090, 38.755167));
        ring2.addPoint(new Point(-77.195106, 38.755154));
        ring2.addPoint(new Point(-77.195205, 38.755233));
        ring2.addPoint(new Point(-77.195299, 38.755159));
        polygon2.addRing(ring2);

        createFeatures(geoPackage, srs, "polygon2", GeometryType.POLYGON,
                polygon2, polygon2Name);

        List<Geometry> geometries1 = new ArrayList<>();
        List<String> geometries1Names = new ArrayList<>();
        geometries1.add(point1);
        geometries1Names.add(point1Name);
        geometries1.add(line1);
        geometries1Names.add(line1Name);
        geometries1.add(polygon1);
        geometries1Names.add(polygon1Name);

        createFeatures(geoPackage, srs, "geometry1", GeometryType.GEOMETRY,
                geometries1, geometries1Names);

        List<Geometry> geometries2 = new ArrayList<>();
        List<String> geometries2Names = new ArrayList<>();
        geometries2.add(point2);
        geometries2Names.add(point2Name);
        geometries2.add(line2);
        geometries2Names.add(line2Name);
        geometries2.add(polygon2);
        geometries2Names.add(polygon2Name);

        createFeatures(geoPackage, srs, "geometry2", GeometryType.GEOMETRY,
                geometries2, geometries2Names);

    }

    private static void createFeatures(GeoPackage geoPackage,
                                       SpatialReferenceSystem srs, String tableName, GeometryType type,
                                       Geometry geometry, String name) throws SQLException {

        List<Geometry> geometries = new ArrayList<>();
        geometries.add(geometry);
        List<String> names = new ArrayList<>();
        names.add(name);

        createFeatures(geoPackage, srs, tableName, type, geometries, names);
    }

    private static void createFeatures(GeoPackage geoPackage,
                                       SpatialReferenceSystem srs, String tableName, GeometryType type,
                                       List<Geometry> geometries, List<String> names) throws SQLException {

        GeometryEnvelope envelope = null;
        for (Geometry geometry : geometries) {
            if (envelope == null) {
                envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
            } else {
                GeometryEnvelopeBuilder.buildEnvelope(geometry, envelope);
            }
        }

        ContentsDao contentsDao = geoPackage.getContentsDao();

        Contents contents = new Contents();
        contents.setTableName(tableName);
        contents.setDataType(ContentsDataType.FEATURES);
        contents.setIdentifier(tableName);
        contents.setDescription("example: " + tableName);
        contents.setMinX(envelope.getMinX());
        contents.setMinY(envelope.getMinY());
        contents.setMaxX(envelope.getMaxX());
        contents.setMaxY(envelope.getMaxY());
        contents.setSrs(srs);

        List<FeatureColumn> columns = new ArrayList<FeatureColumn>();

        int columnNumber = 0;
        columns.add(FeatureColumn.createPrimaryKeyColumn(columnNumber++,
                ID_COLUMN));
        columns.add(FeatureColumn.createGeometryColumn(columnNumber++,
                GEOMETRY_COLUMN, type, false, null));
        columns.add(FeatureColumn.createColumn(columnNumber++, TEXT_COLUMN,
                GeoPackageDataType.TEXT, false, ""));
        columns.add(FeatureColumn.createColumn(columnNumber++, REAL_COLUMN,
                GeoPackageDataType.REAL, false, null));
        columns.add(FeatureColumn.createColumn(columnNumber++, BOOLEAN_COLUMN,
                GeoPackageDataType.BOOLEAN, false, null));
        columns.add(FeatureColumn.createColumn(columnNumber++, BLOB_COLUMN,
                GeoPackageDataType.BLOB, false, null));
        columns.add(FeatureColumn.createColumn(columnNumber++, INTEGER_COLUMN,
                GeoPackageDataType.INTEGER, false, null));
        columns.add(FeatureColumn.createColumn(columnNumber++,
                TEXT_LIMITED_COLUMN, GeoPackageDataType.TEXT, (long) UUID
                        .randomUUID().toString().length(), false, null));
        columns.add(FeatureColumn
                .createColumn(columnNumber++, BLOB_LIMITED_COLUMN,
                        GeoPackageDataType.BLOB, (long) UUID.randomUUID()
                                .toString().getBytes().length, false, null));
        columns.add(FeatureColumn.createColumn(columnNumber++, DATE_COLUMN,
                GeoPackageDataType.DATE, false, null));
        columns.add(FeatureColumn.createColumn(columnNumber++, DATETIME_COLUMN,
                GeoPackageDataType.DATETIME, false, null));

        FeatureTable table = new FeatureTable(tableName, columns);
        geoPackage.createFeatureTable(table);

        contentsDao.create(contents);

        GeometryColumnsDao geometryColumnsDao = geoPackage
                .getGeometryColumnsDao();

        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setContents(contents);
        geometryColumns.setColumnName(GEOMETRY_COLUMN);
        geometryColumns.setGeometryType(type);
        geometryColumns.setSrs(srs);
        geometryColumns.setZ((byte) 0);
        geometryColumns.setM((byte) 0);
        geometryColumnsDao.create(geometryColumns);

        FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

        for (int i = 0; i < geometries.size(); i++) {

            Geometry geometry = geometries.get(i);
            String name;
            if (names != null) {
                name = names.get(i);
            } else {
                name = UUID.randomUUID().toString();
            }

            FeatureRow newRow = dao.newRow();

            GeoPackageGeometryData geometryData = new GeoPackageGeometryData(
                    geometryColumns.getSrsId());
            geometryData.setGeometry(geometry);
            newRow.setGeometry(geometryData);

            newRow.setValue(TEXT_COLUMN, name);
            newRow.setValue(REAL_COLUMN, Math.random() * 5000.0);
            newRow.setValue(BOOLEAN_COLUMN, Math.random() < .5 ? false : true);
            newRow.setValue(BLOB_COLUMN, UUID.randomUUID().toString()
                    .getBytes());
            newRow.setValue(INTEGER_COLUMN, (int) (Math.random() * 500));
            newRow.setValue(TEXT_LIMITED_COLUMN, UUID.randomUUID().toString());
            newRow.setValue(BLOB_LIMITED_COLUMN, UUID.randomUUID().toString()
                    .getBytes());
            newRow.setValue(DATE_COLUMN, new Date());
            newRow.setValue(DATETIME_COLUMN, new Date());

            dao.create(newRow);

        }

    }

    private static void createTiles(Context context, GeoPackage geoPackage) throws IOException,
            SQLException {

        geoPackage.createTileMatrixSetTable();
        geoPackage.createTileMatrixTable();

        BoundingBox bitsBoundingBox = new BoundingBox(-11667347.997449303,
                4824705.2253603265, -11666125.00499674, 4825928.217812888);
        createTiles(context, geoPackage, "bit_systems", bitsBoundingBox, 15, 17, "png");

        BoundingBox ngaBoundingBox = new BoundingBox(-8593967.964158937,
                4685284.085768163, -8592744.971706374, 4687730.070673289);
        createTiles(context, geoPackage, "nga", ngaBoundingBox, 15, 16, "png");

    }

    private static void createTiles(Context context, GeoPackage geoPackage, String name,
                                    BoundingBox boundingBox, int minZoomLevel, int maxZoomLevel,
                                    String extension) throws SQLException, IOException {

        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();
        SpatialReferenceSystem srs = srsDao.getOrCreateCode(
                ProjectionConstants.AUTHORITY_EPSG,
                (long) ProjectionConstants.EPSG_WEB_MERCATOR);

        TileGrid totalTileGrid = TileBoundingBoxUtils.getTileGrid(boundingBox,
                minZoomLevel);
        BoundingBox totalBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(totalTileGrid, minZoomLevel);

        ContentsDao contentsDao = geoPackage.getContentsDao();

        Contents contents = new Contents();
        contents.setTableName(name);
        contents.setDataType(ContentsDataType.TILES);
        contents.setIdentifier(name);
        contents.setDescription(name);
        contents.setMinX(totalBoundingBox.getMinLongitude());
        contents.setMinY(totalBoundingBox.getMinLatitude());
        contents.setMaxX(totalBoundingBox.getMaxLongitude());
        contents.setMaxY(totalBoundingBox.getMaxLatitude());
        contents.setSrs(srs);

        TileTable tileTable = TestUtils.buildTileTable(contents.getTableName());
        geoPackage.createTileTable(tileTable);

        contentsDao.create(contents);

        TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

        TileMatrixSet tileMatrixSet = new TileMatrixSet();
        tileMatrixSet.setContents(contents);
        tileMatrixSet.setSrs(contents.getSrs());
        tileMatrixSet.setMinX(contents.getMinX());
        tileMatrixSet.setMinY(contents.getMinY());
        tileMatrixSet.setMaxX(contents.getMaxX());
        tileMatrixSet.setMaxY(contents.getMaxY());
        tileMatrixSetDao.create(tileMatrixSet);

        TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

        final String tilesPath = "tiles/";

        TileGrid tileGrid = totalTileGrid;

        for (int zoom = minZoomLevel; zoom <= maxZoomLevel; zoom++) {

            final String zoomPath = tilesPath + zoom + "/";

            Integer tileWidth = null;
            Integer tileHeight = null;

            TileDao dao = geoPackage.getTileDao(tileMatrixSet);

            for (long x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {

                final String xPath = zoomPath + x + "/";

                for (long y = tileGrid.getMinY(); y <= tileGrid.getMaxY(); y++) {

                    final String yPath = xPath + y + "." + extension;

                    try {

                        byte[] tileBytes = TestUtils.getAssetFileBytes(context,
                                yPath);

                        if (tileWidth == null || tileHeight == null) {
                            Bitmap tileImage = BitmapConverter.toBitmap(tileBytes);
                            if (tileImage != null) {
                                tileHeight = tileImage.getHeight();
                                tileWidth = tileImage.getWidth();
                            }
                        }

                        TileRow newRow = dao.newRow();

                        newRow.setZoomLevel(zoom);
                        newRow.setTileColumn(x - tileGrid.getMinX());
                        newRow.setTileRow(y - tileGrid.getMinY());
                        newRow.setTileData(tileBytes);

                        dao.create(newRow);

                    } catch (FileNotFoundException e) {
                        // skip tile
                    }

                }
            }

            if (tileWidth == null) {
                tileWidth = 256;
            }
            if (tileHeight == null) {
                tileHeight = 256;
            }

            long matrixWidth = tileGrid.getMaxX() - tileGrid.getMinX() + 1;
            long matrixHeight = tileGrid.getMaxY() - tileGrid.getMinY() + 1;
            double pixelXSize = (tileMatrixSet.getMaxX() - tileMatrixSet
                    .getMinX()) / (matrixWidth * tileWidth);
            double pixelYSize = (tileMatrixSet.getMaxY() - tileMatrixSet
                    .getMinY()) / (matrixHeight * tileHeight);

            TileMatrix tileMatrix = new TileMatrix();
            tileMatrix.setContents(contents);
            tileMatrix.setZoomLevel(zoom);
            tileMatrix.setMatrixWidth(matrixWidth);
            tileMatrix.setMatrixHeight(matrixHeight);
            tileMatrix.setTileWidth(tileWidth);
            tileMatrix.setTileHeight(tileHeight);
            tileMatrix.setPixelXSize(pixelXSize);
            tileMatrix.setPixelYSize(pixelYSize);
            tileMatrixDao.create(tileMatrix);

            tileGrid = TileBoundingBoxUtils.tileGridZoomIncrease(tileGrid, 1);
        }

    }

    private static void createAttributes(GeoPackage geoPackage) {

        List<AttributesColumn> columns = new ArrayList<AttributesColumn>();

        int columnNumber = 1;
        columns.add(AttributesColumn.createColumn(columnNumber++, TEXT_COLUMN,
                GeoPackageDataType.TEXT, false, ""));
        columns.add(AttributesColumn.createColumn(columnNumber++, REAL_COLUMN,
                GeoPackageDataType.REAL, false, null));
        columns.add(AttributesColumn.createColumn(columnNumber++,
                BOOLEAN_COLUMN, GeoPackageDataType.BOOLEAN, false, null));
        columns.add(AttributesColumn.createColumn(columnNumber++, BLOB_COLUMN,
                GeoPackageDataType.BLOB, false, null));
        columns.add(AttributesColumn.createColumn(columnNumber++,
                INTEGER_COLUMN, GeoPackageDataType.INTEGER, false, null));
        columns.add(AttributesColumn.createColumn(columnNumber++,
                TEXT_LIMITED_COLUMN, GeoPackageDataType.TEXT, (long) UUID
                        .randomUUID().toString().length(), false, null));
        columns.add(AttributesColumn
                .createColumn(columnNumber++, BLOB_LIMITED_COLUMN,
                        GeoPackageDataType.BLOB, (long) UUID.randomUUID()
                                .toString().getBytes().length, false, null));
        columns.add(AttributesColumn.createColumn(columnNumber++, DATE_COLUMN,
                GeoPackageDataType.DATE, false, null));
        columns.add(AttributesColumn.createColumn(columnNumber++,
                DATETIME_COLUMN, GeoPackageDataType.DATETIME, false, null));

        AttributesTable attributesTable = geoPackage
                .createAttributesTableWithId("attributes", columns);

        AttributesDao attributesDao = geoPackage
                .getAttributesDao(attributesTable.getTableName());

        for (int i = 0; i < 10; i++) {

            AttributesRow newRow = attributesDao.newRow();

            newRow.setValue(TEXT_COLUMN, UUID.randomUUID().toString());
            newRow.setValue(REAL_COLUMN, Math.random() * 5000.0);
            newRow.setValue(BOOLEAN_COLUMN, Math.random() < .5 ? false : true);
            newRow.setValue(BLOB_COLUMN, UUID.randomUUID().toString()
                    .getBytes());
            newRow.setValue(INTEGER_COLUMN, (int) (Math.random() * 500));
            newRow.setValue(TEXT_LIMITED_COLUMN, UUID.randomUUID().toString());
            newRow.setValue(BLOB_LIMITED_COLUMN, UUID.randomUUID().toString()
                    .getBytes());
            newRow.setValue(DATE_COLUMN, new Date());
            newRow.setValue(DATETIME_COLUMN, new Date());

            attributesDao.create(newRow);

        }
    }

    private static void createGeometryIndexExtension(Context context, GeoPackage geoPackage) {

        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager indexer = new FeatureIndexManager(context, geoPackage, featureDao);
            indexer.setIndexLocation(FeatureIndexType.GEOPACKAGE);
            indexer.index();
        }

    }

    private static void createFeatureTileLinkExtension(Context context, GeoPackage geoPackage)
            throws SQLException, IOException {

        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureTiles featureTiles = new DefaultFeatureTiles(context, featureDao);

            FeatureIndexManager indexer = new FeatureIndexManager(context, geoPackage, featureDao);
            featureTiles.setIndexManager(indexer);

            BoundingBox boundingBox = featureDao.getBoundingBox();
            Projection projection = featureDao.getProjection();

            Projection requestProjection = ProjectionFactory
                    .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
            ProjectionTransform transform = projection
                    .getTransformation(requestProjection);
            BoundingBox requestBoundingBox = transform.transform(boundingBox);

            int zoomLevel = TileBoundingBoxUtils
                    .getZoomLevel(requestBoundingBox);
            zoomLevel = Math.min(zoomLevel, 19);

            int minZoom = zoomLevel - 2;
            int maxZoom = zoomLevel + 2;

            TileGenerator tileGenerator = new FeatureTileGenerator(context, geoPackage,
                    featureTable + "_tiles", featureTiles, minZoom, maxZoom,
                    requestBoundingBox, requestProjection);

            tileGenerator.generateTiles();
        }
    }

    private static int dataColumnConstraintIndex = 0;

    private static void createSchemaExtension(GeoPackage geoPackage)
            throws SQLException {

        geoPackage.createDataColumnConstraintsTable();

        DataColumnConstraintsDao dao = geoPackage.getDataColumnConstraintsDao();

        DataColumnConstraints sampleRange = new DataColumnConstraints();
        sampleRange.setConstraintName("sampleRange");
        sampleRange.setConstraintType(DataColumnConstraintType.RANGE);
        sampleRange.setMin(BigDecimal.ONE);
        sampleRange.setMinIsInclusive(true);
        sampleRange.setMax(BigDecimal.TEN);
        sampleRange.setMaxIsInclusive(true);
        sampleRange.setDescription("sampleRange description");
        dao.create(sampleRange);

        DataColumnConstraints sampleEnum1 = new DataColumnConstraints();
        sampleEnum1.setConstraintName("sampleEnum");
        sampleEnum1.setConstraintType(DataColumnConstraintType.ENUM);
        sampleEnum1.setValue("1");
        sampleEnum1.setDescription("sampleEnum description");
        dao.create(sampleEnum1);

        DataColumnConstraints sampleEnum3 = new DataColumnConstraints();
        sampleEnum3.setConstraintName(sampleEnum1.getConstraintName());
        sampleEnum3.setConstraintType(DataColumnConstraintType.ENUM);
        sampleEnum3.setValue("3");
        sampleEnum3.setDescription("sampleEnum description");
        dao.create(sampleEnum3);

        DataColumnConstraints sampleEnum5 = new DataColumnConstraints();
        sampleEnum5.setConstraintName(sampleEnum1.getConstraintName());
        sampleEnum5.setConstraintType(DataColumnConstraintType.ENUM);
        sampleEnum5.setValue("5");
        sampleEnum5.setDescription("sampleEnum description");
        dao.create(sampleEnum5);

        DataColumnConstraints sampleEnum7 = new DataColumnConstraints();
        sampleEnum7.setConstraintName(sampleEnum1.getConstraintName());
        sampleEnum7.setConstraintType(DataColumnConstraintType.ENUM);
        sampleEnum7.setValue("7");
        sampleEnum7.setDescription("sampleEnum description");
        dao.create(sampleEnum7);

        DataColumnConstraints sampleEnum9 = new DataColumnConstraints();
        sampleEnum9.setConstraintName(sampleEnum1.getConstraintName());
        sampleEnum9.setConstraintType(DataColumnConstraintType.ENUM);
        sampleEnum9.setValue("9");
        sampleEnum9.setDescription("sampleEnum description");
        dao.create(sampleEnum9);

        DataColumnConstraints sampleGlob = new DataColumnConstraints();
        sampleGlob.setConstraintName("sampleGlob");
        sampleGlob.setConstraintType(DataColumnConstraintType.GLOB);
        sampleGlob.setValue("[1-2][0-9][0-9][0-9]");
        sampleGlob.setDescription("sampleGlob description");
        dao.create(sampleGlob);

        geoPackage.createDataColumnsTable();

        DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();

        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);

            FeatureTable table = featureDao.getTable();
            for (FeatureColumn column : table.getColumns()) {

                if (!column.isPrimaryKey()
                        && column.getDataType() == GeoPackageDataType.INTEGER) {

                    DataColumns dataColumns = new DataColumns();
                    dataColumns.setContents(featureDao.getGeometryColumns()
                            .getContents());
                    dataColumns.setColumnName(column.getName());
                    dataColumns.setName(featureTable);
                    dataColumns.setTitle("TEST_TITLE");
                    dataColumns.setDescription("TEST_DESCRIPTION");
                    dataColumns.setMimeType("TEST_MIME_TYPE");

                    DataColumnConstraintType constraintType = DataColumnConstraintType
                            .values()[dataColumnConstraintIndex];
                    dataColumnConstraintIndex++;
                    if (dataColumnConstraintIndex >= DataColumnConstraintType
                            .values().length) {
                        dataColumnConstraintIndex = 0;
                    }

                    int value = 0;

                    String constraintName = null;
                    switch (constraintType) {
                        case RANGE:
                            constraintName = sampleRange.getConstraintName();
                            value = 1 + (int) (Math.random() * 10);
                            break;
                        case ENUM:
                            constraintName = sampleEnum1.getConstraintName();
                            value = 1 + ((int) (Math.random() * 5) * 2);
                            break;
                        case GLOB:
                            constraintName = sampleGlob.getConstraintName();
                            value = 1000 + (int) (Math.random() * 2000);
                            break;
                        default:
                            throw new GeoPackageException(
                                    "Unexpected Constraint Type: " + constraintType);
                    }
                    dataColumns.setConstraintName(constraintName);

                    ContentValues values = new ContentValues();
                    values.put(column.getName(), value);
                    featureDao.update(values, null, null);

                    dataColumnsDao.create(dataColumns);

                    break;
                }
            }
        }
    }

    private static void createNonLinearGeometryTypesExtension(
            GeoPackage geoPackage) throws SQLException {

        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();

        SpatialReferenceSystem srs = srsDao.getOrCreateCode(
                ProjectionConstants.AUTHORITY_EPSG,
                (long) ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        GeometryExtensions extensions = new GeometryExtensions(geoPackage);

        String tableName = "non_linear_geometries";

        List<Geometry> geometries = new ArrayList<>();
        List<String> geometryNames = new ArrayList<>();

        CircularString circularString = new CircularString();
        circularString.addPoint(new Point(-122.358, 47.653));
        circularString.addPoint(new Point(-122.348, 47.649));
        circularString.addPoint(new Point(-122.348, 47.658));
        circularString.addPoint(new Point(-122.358, 47.658));
        circularString.addPoint(new Point(-122.358, 47.653));

        for (int i = GeometryType.CIRCULARSTRING.getCode(); i <= GeometryType.SURFACE
                .getCode(); i++) {

            GeometryType geometryType = GeometryType.fromCode(i);
            extensions.getOrCreate(tableName, GEOMETRY_COLUMN, geometryType);

            Geometry geometry = null;
            String name = geometryType.getName().toLowerCase();

            switch (geometryType) {
                case CIRCULARSTRING:
                    geometry = circularString;
                    break;
                case COMPOUNDCURVE:
                    CompoundCurve compoundCurve = new CompoundCurve();
                    compoundCurve.addLineString(circularString);
                    geometry = compoundCurve;
                    break;
                case CURVEPOLYGON:
                    CurvePolygon<CircularString> curvePolygon = new CurvePolygon<>();
                    curvePolygon.addRing(circularString);
                    geometry = curvePolygon;
                    break;
                case MULTICURVE:
                    MultiLineString multiCurve = new MultiLineString();
                    multiCurve.addLineString(circularString);
                    geometry = multiCurve;
                    break;
                case MULTISURFACE:
                    MultiPolygon multiSurface = new MultiPolygon();
                    Polygon polygon = new Polygon();
                    polygon.addRing(circularString);
                    multiSurface.addPolygon(polygon);
                    geometry = multiSurface;
                    break;
                case CURVE:
                    CompoundCurve curve = new CompoundCurve();
                    curve.addLineString(circularString);
                    geometry = curve;
                    break;
                case SURFACE:
                    CurvePolygon<CircularString> surface = new CurvePolygon<>();
                    surface.addRing(circularString);
                    geometry = surface;
                    break;
                default:
                    throw new GeoPackageException("Unexpected Geometry Type: "
                            + geometryType);
            }

            geometries.add(geometry);
            geometryNames.add(name);

        }

        createFeatures(geoPackage, srs, tableName, GeometryType.GEOMETRY,
                geometries, geometryNames);

    }

    private static void createWebPExtension(Context context, GeoPackage geoPackage)
            throws SQLException, IOException {

        WebPExtension webpExtension = new WebPExtension(geoPackage);
        String tableName = "webp_tiles";
        webpExtension.getOrCreate(tableName);

        geoPackage.createTileMatrixSetTable();
        geoPackage.createTileMatrixTable();

        BoundingBox bitsBoundingBox = new BoundingBox(-11667347.997449303,
                4824705.2253603265, -11666125.00499674, 4825928.217812888);
        createTiles(context, geoPackage, tableName, bitsBoundingBox, 15, 15, "webp");
    }

    private static void createCrsWktExtension(GeoPackage geoPackage)
            throws SQLException {

        CrsWktExtension wktExtension = new CrsWktExtension(geoPackage);
        wktExtension.getOrCreate();

        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();

        SpatialReferenceSystem srs = srsDao.queryForOrganizationCoordsysId(
                ProjectionConstants.AUTHORITY_EPSG,
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        SpatialReferenceSystem testSrs = new SpatialReferenceSystem();
        testSrs.setSrsName("test");
        testSrs.setSrsId(12345);
        testSrs.setOrganization("test_org");
        testSrs.setOrganizationCoordsysId(testSrs.getSrsId());
        testSrs.setDefinition(srs.getDefinition());
        testSrs.setDescription(srs.getDescription());
        testSrs.setDefinition_12_063(srs.getDefinition_12_063());
        srsDao.create(testSrs);

        SpatialReferenceSystem testSrs2 = new SpatialReferenceSystem();
        testSrs2.setSrsName("test2");
        testSrs2.setSrsId(54321);
        testSrs2.setOrganization("test_org");
        testSrs2.setOrganizationCoordsysId(testSrs2.getSrsId());
        testSrs2.setDefinition(srs.getDefinition());
        testSrs2.setDescription(srs.getDescription());
        srsDao.create(testSrs2);

    }

    private static void createMetadataExtension(GeoPackage geoPackage)
            throws SQLException {

        geoPackage.createMetadataTable();
        MetadataDao metadataDao = geoPackage.getMetadataDao();

        Metadata metadata1 = new Metadata();
        metadata1.setId(1);
        metadata1.setMetadataScope(MetadataScopeType.DATASET);
        metadata1.setStandardUri("TEST_URI_1");
        metadata1.setMimeType("text/xml");
        metadata1.setMetadata("TEST METADATA 1");
        metadataDao.create(metadata1);

        Metadata metadata2 = new Metadata();
        metadata2.setId(2);
        metadata2.setMetadataScope(MetadataScopeType.FEATURE_TYPE);
        metadata2.setStandardUri("TEST_URI_2");
        metadata2.setMimeType("text/xml");
        metadata2.setMetadata("TEST METADATA 2");
        metadataDao.create(metadata2);

        Metadata metadata3 = new Metadata();
        metadata3.setId(3);
        metadata3.setMetadataScope(MetadataScopeType.TILE);
        metadata3.setStandardUri("TEST_URI_3");
        metadata3.setMimeType("text/xml");
        metadata3.setMetadata("TEST METADATA 3");
        metadataDao.create(metadata3);

        geoPackage.createMetadataReferenceTable();
        MetadataReferenceDao metadataReferenceDao = geoPackage
                .getMetadataReferenceDao();

        MetadataReference reference1 = new MetadataReference();
        reference1.setReferenceScope(ReferenceScopeType.GEOPACKAGE);
        reference1.setMetadata(metadata1);
        metadataReferenceDao.create(reference1);

        List<String> tileTables = geoPackage.getTileTables();
        if (!tileTables.isEmpty()) {
            String table = tileTables.get(0);
            MetadataReference reference2 = new MetadataReference();
            reference2.setReferenceScope(ReferenceScopeType.TABLE);
            reference2.setTableName(table);
            reference2.setMetadata(metadata2);
            reference2.setParentMetadata(metadata1);
            metadataReferenceDao.create(reference2);
        }

        List<String> featureTables = geoPackage.getFeatureTables();
        if (!featureTables.isEmpty()) {
            String table = featureTables.get(0);
            MetadataReference reference3 = new MetadataReference();
            reference3.setReferenceScope(ReferenceScopeType.ROW_COL);
            reference3.setTableName(table);
            reference3.setColumnName(GEOMETRY_COLUMN);
            reference3.setRowIdValue(1L);
            reference3.setMetadata(metadata3);
            metadataReferenceDao.create(reference3);
        }

    }

    private static void createCoverageDataExtension(GeoPackage geoPackage)
            throws SQLException {

        createCoverageDataPngExtension(geoPackage);
        createCoverageDataTiffExtension(geoPackage);

    }

    private static void createCoverageDataPngExtension(GeoPackage geoPackage)
            throws SQLException {

        BoundingBox bbox = new BoundingBox(-11667347.997449303,
                4824705.2253603265, -11666125.00499674, 4825928.217812888);

        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();
        SpatialReferenceSystem contentsSrs = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM_GEOGRAPHICAL_3D);
        SpatialReferenceSystem tileMatrixSrs = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WEB_MERCATOR);

        ProjectionTransform transform = ProjectionFactory.getProjection(
                ProjectionConstants.EPSG_WEB_MERCATOR).getTransformation(
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM_GEOGRAPHICAL_3D);
        BoundingBox contentsBoundingBox = transform.transform(bbox);

        CoverageDataPng coverageData = CoverageDataPng
                .createTileTableWithMetadata(geoPackage, "coverage_png",
                        contentsBoundingBox, contentsSrs.getId(), bbox,
                        tileMatrixSrs.getId());
        TileDao tileDao = coverageData.getTileDao();
        TileMatrixSet tileMatrixSet = coverageData.getTileMatrixSet();

        GriddedCoverageDao griddedCoverageDao = coverageData
                .getGriddedCoverageDao();

        GriddedCoverage griddedCoverage = new GriddedCoverage();
        griddedCoverage.setTileMatrixSet(tileMatrixSet);
        griddedCoverage.setDataType(GriddedCoverageDataType.INTEGER);
        griddedCoverage.setDataNull(new Double(Short.MAX_VALUE
                - Short.MIN_VALUE));
        griddedCoverage
                .setGridCellEncodingType(GriddedCoverageEncodingType.CENTER);
        griddedCoverageDao.create(griddedCoverage);

        GriddedTileDao griddedTileDao = coverageData.getGriddedTileDao();

        int width = 1;
        int height = 1;
        int tileWidth = 3;
        int tileHeight = 3;

        short[][] tilePixels = new short[tileHeight][tileWidth];

        tilePixels[0][0] = (short) 1661.95;
        tilePixels[0][1] = (short) 1665.40;
        tilePixels[0][2] = (short) 1668.19;
        tilePixels[1][0] = (short) 1657.18;
        tilePixels[1][1] = (short) 1663.39;
        tilePixels[1][2] = (short) 1669.65;
        tilePixels[2][0] = (short) 1654.78;
        tilePixels[2][1] = (short) 1660.31;
        tilePixels[2][2] = (short) 1666.44;

        byte[] imageBytes = coverageData.drawTileData(tilePixels);

        TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

        TileMatrix tileMatrix = new TileMatrix();
        tileMatrix.setContents(tileMatrixSet.getContents());
        tileMatrix.setMatrixHeight(height);
        tileMatrix.setMatrixWidth(width);
        tileMatrix.setTileHeight(tileHeight);
        tileMatrix.setTileWidth(tileWidth);
        tileMatrix.setPixelXSize((bbox.getMaxLongitude() - bbox
                .getMinLongitude()) / width / tileWidth);
        tileMatrix
                .setPixelYSize((bbox.getMaxLatitude() - bbox.getMinLatitude())
                        / height / tileHeight);
        tileMatrix.setZoomLevel(15);
        tileMatrixDao.create(tileMatrix);

        TileRow tileRow = tileDao.newRow();
        tileRow.setTileColumn(0);
        tileRow.setTileRow(0);
        tileRow.setZoomLevel(tileMatrix.getZoomLevel());
        tileRow.setTileData(imageBytes);

        long tileId = tileDao.create(tileRow);

        GriddedTile griddedTile = new GriddedTile();
        griddedTile.setContents(tileMatrixSet.getContents());
        griddedTile.setTableId(tileId);

        griddedTileDao.create(griddedTile);

    }

    private static void createCoverageDataTiffExtension(GeoPackage geoPackage)
            throws SQLException {

        BoundingBox bbox = new BoundingBox(-8593967.964158937,
                4685284.085768163, -8592744.971706374, 4687730.070673289);

        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();
        SpatialReferenceSystem contentsSrs = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM_GEOGRAPHICAL_3D);
        SpatialReferenceSystem tileMatrixSrs = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WEB_MERCATOR);

        ProjectionTransform transform = ProjectionFactory.getProjection(
                ProjectionConstants.EPSG_WEB_MERCATOR).getTransformation(
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM_GEOGRAPHICAL_3D);
        BoundingBox contentsBoundingBox = transform.transform(bbox);

        CoverageDataTiff coverageData = CoverageDataTiff
                .createTileTableWithMetadata(geoPackage, "coverage_tiff",
                        contentsBoundingBox, contentsSrs.getId(), bbox,
                        tileMatrixSrs.getId());
        TileDao tileDao = coverageData.getTileDao();
        TileMatrixSet tileMatrixSet = coverageData.getTileMatrixSet();

        GriddedCoverageDao griddedCoverageDao = coverageData
                .getGriddedCoverageDao();

        GriddedCoverage griddedCoverage = new GriddedCoverage();
        griddedCoverage.setTileMatrixSet(tileMatrixSet);
        griddedCoverage.setDataType(GriddedCoverageDataType.FLOAT);
        griddedCoverage.setDataNull((double) Float.MAX_VALUE);
        griddedCoverage
                .setGridCellEncodingType(GriddedCoverageEncodingType.CENTER);
        griddedCoverageDao.create(griddedCoverage);

        GriddedTileDao griddedTileDao = coverageData.getGriddedTileDao();

        int width = 1;
        int height = 1;
        int tileWidth = 4;
        int tileHeight = 4;

        float[][] tilePixels = new float[tileHeight][tileWidth];

        tilePixels[0][0] = 71.78f;
        tilePixels[0][1] = 74.31f;
        tilePixels[0][2] = 70.19f;
        tilePixels[0][3] = 68.07f;
        tilePixels[1][0] = 61.01f;
        tilePixels[1][1] = 69.66f;
        tilePixels[1][2] = 68.65f;
        tilePixels[1][3] = 72.02f;
        tilePixels[2][0] = 41.58f;
        tilePixels[2][1] = 69.46f;
        tilePixels[2][2] = 67.56f;
        tilePixels[2][3] = 70.42f;
        tilePixels[3][0] = 54.03f;
        tilePixels[3][1] = 71.32f;
        tilePixels[3][2] = 57.61f;
        tilePixels[3][3] = 54.96f;

        byte[] imageBytes = coverageData.drawTileData(tilePixels);

        TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

        TileMatrix tileMatrix = new TileMatrix();
        tileMatrix.setContents(tileMatrixSet.getContents());
        tileMatrix.setMatrixHeight(height);
        tileMatrix.setMatrixWidth(width);
        tileMatrix.setTileHeight(tileHeight);
        tileMatrix.setTileWidth(tileWidth);
        tileMatrix.setPixelXSize((bbox.getMaxLongitude() - bbox
                .getMinLongitude()) / width / tileWidth);
        tileMatrix
                .setPixelYSize((bbox.getMaxLatitude() - bbox.getMinLatitude())
                        / height / tileHeight);
        tileMatrix.setZoomLevel(15);
        tileMatrixDao.create(tileMatrix);

        TileRow tileRow = tileDao.newRow();
        tileRow.setTileColumn(0);
        tileRow.setTileRow(0);
        tileRow.setZoomLevel(tileMatrix.getZoomLevel());
        tileRow.setTileData(imageBytes);

        long tileId = tileDao.create(tileRow);

        GriddedTile griddedTile = new GriddedTile();
        griddedTile.setContents(tileMatrixSet.getContents());
        griddedTile.setTableId(tileId);

        griddedTileDao.create(griddedTile);

    }

    private static void createRTreeSpatialIndexExtension(GeoPackage geoPackage) {

        RTreeIndexExtension extension = new RTreeIndexExtension(geoPackage);

        List<String> featureTables = geoPackage.getFeatureTables();
        for (String tableName : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(tableName);
            FeatureTable featureTable = featureDao.getTable();

            extension.create(featureTable);
        }

    }

}
