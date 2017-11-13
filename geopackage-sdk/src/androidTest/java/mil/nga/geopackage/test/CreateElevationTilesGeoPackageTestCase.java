package mil.nga.geopackage.test;

import junit.framework.TestCase;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.elevation.ElevationTilesPng;
import mil.nga.geopackage.extension.elevation.GriddedCoverage;
import mil.nga.geopackage.extension.elevation.GriddedCoverageDao;
import mil.nga.geopackage.extension.elevation.GriddedCoverageDataType;
import mil.nga.geopackage.extension.elevation.GriddedTile;
import mil.nga.geopackage.extension.elevation.GriddedTileDao;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.test.geom.GeoPackageGeometryDataUtils;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Abstract Test Case for Imported Elevation Tiles GeoPackages
 *
 * @author osbornb
 */
public abstract class CreateElevationTilesGeoPackageTestCase extends
        GeoPackageTestCase {

    public class ElevationTileValues {

        public short[][] tilePixels;

        public int[][] tileUnsignedPixels;

        public Double[][] tileElevations;

        public short[] tilePixelsFlat;

        public int[] tileUnsignedPixelsFlat;

        public Double[] tileElevationsFlat;

    }

    protected ElevationTileValues elevationTileValues = new ElevationTileValues();

    protected final boolean allowNulls;

    /**
     * Constructor
     *
     * @param allowNulls true to allow null elevations
     */
    public CreateElevationTilesGeoPackageTestCase(boolean allowNulls) {
        this.allowNulls = allowNulls;
    }

    @Override
    protected GeoPackage getGeoPackage() throws Exception {

        GeoPackageManager manager = GeoPackageFactory.getManager(activity);

        // Delete
        manager.delete(TestConstants.CREATE_ELEVATION_TILES_DB_NAME);

        // Create
        manager.create(TestConstants.CREATE_ELEVATION_TILES_DB_NAME);

        // Open
        GeoPackage geoPackage = manager.open(TestConstants.CREATE_ELEVATION_TILES_DB_NAME);
        if (geoPackage == null) {
            throw new GeoPackageException("Failed to open database");
        }

        double minLongitude = -180.0 + (360.0 * Math.random());
        double maxLongitude = minLongitude
                + ((180.0 - minLongitude) * Math.random());
        double minLatitude = ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE
                + ((ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE - ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE) * Math
                .random());
        double maxLatitude = minLatitude
                + ((ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE - minLatitude) * Math
                .random());

        BoundingBox bbox = new BoundingBox(minLongitude,
                minLatitude, maxLongitude, maxLatitude);

        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();
        SpatialReferenceSystem contentsSrs = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM_GEOGRAPHICAL_3D);
        SpatialReferenceSystem tileMatrixSrs = srsDao
                .getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        ElevationTilesPng elevationTiles = ElevationTilesPng
                .createTileTableWithMetadata(geoPackage,
                        TestConstants.CREATE_ELEVATION_TILES_DB_TABLE_NAME,
                        bbox, contentsSrs.getId(), bbox, tileMatrixSrs.getId());
        TileDao tileDao = elevationTiles.getTileDao();
        TileMatrixSet tileMatrixSet = elevationTiles.getTileMatrixSet();

        GriddedCoverageDao griddedCoverageDao = elevationTiles
                .getGriddedCoverageDao();

        GriddedCoverage griddedCoverage = new GriddedCoverage();
        griddedCoverage.setTileMatrixSet(tileMatrixSet);
        griddedCoverage.setDataType(GriddedCoverageDataType.INTEGER);
        boolean defaultScale = true;
        if (Math.random() < .5) {
            griddedCoverage.setScale(100.0 * Math.random());
            defaultScale = false;
        }
        boolean defaultOffset = true;
        if (Math.random() < .5) {
            griddedCoverage.setOffset(100.0 * Math.random());
            defaultOffset = false;
        }
        boolean defaultPrecision = true;
        if (Math.random() < .5) {
            griddedCoverage.setPrecision(10.0 * Math.random());
            defaultPrecision = false;
        }
        griddedCoverage.setDataNull(new Double(Short.MAX_VALUE
                - Short.MIN_VALUE));
        TestCase.assertEquals(1, griddedCoverageDao.create(griddedCoverage));

        long gcId = griddedCoverage.getId();
        griddedCoverage = griddedCoverageDao.queryForId(gcId);
        TestCase.assertNotNull(griddedCoverage);
        if (defaultScale) {
            TestCase.assertEquals(1.0, griddedCoverage.getScale());
        } else {
            TestCase.assertTrue(griddedCoverage.getScale() >= 0.0
                    && griddedCoverage.getScale() <= 100.0);
        }
        if (defaultOffset) {
            TestCase.assertEquals(0.0, griddedCoverage.getOffset());
        } else {
            TestCase.assertTrue(griddedCoverage.getOffset() >= 0.0
                    && griddedCoverage.getOffset() <= 100.0);
        }
        if (defaultPrecision) {
            TestCase.assertEquals(1.0, griddedCoverage.getPrecision());
        } else {
            TestCase.assertTrue(griddedCoverage.getPrecision() >= 0.0
                    && griddedCoverage.getPrecision() <= 10.0);
        }

        GriddedTile commonGriddedTile = new GriddedTile();
        commonGriddedTile.setContents(tileMatrixSet.getContents());
        boolean defaultGTScale = true;
        if (Math.random() < .5) {
            commonGriddedTile.setScale(100.0 * Math.random());
            defaultGTScale = false;
        }
        boolean defaultGTOffset = true;
        if (Math.random() < .5) {
            commonGriddedTile.setOffset(100.0 * Math.random());
            defaultGTOffset = false;
        }
        // The min, max, mean, and sd are just for testing and have
        // no association on the test tile created
        boolean defaultGTMin = true;
        if (Math.random() < .5) {
            commonGriddedTile.setMin(1000.0 * Math.random());
            defaultGTMin = false;
        }
        boolean defaultGTMax = true;
        if (Math.random() < .5) {
            commonGriddedTile.setMax(1000.0
                    * Math.random()
                    + (commonGriddedTile.getMin() == null ? 0
                    : commonGriddedTile.getMin()));
            defaultGTMax = false;
        }
        boolean defaultGTMean = true;
        if (Math.random() < .5) {
            double min = commonGriddedTile.getMin() != null ? commonGriddedTile
                    .getMin() : 0;
            double max = commonGriddedTile.getMax() != null ? commonGriddedTile
                    .getMax() : 2000.0;
            commonGriddedTile.setMean(((max - min) * Math.random()) + min);
            defaultGTMean = false;
        }
        boolean defaultGTStandardDeviation = true;
        if (Math.random() < .5) {

            double min = commonGriddedTile.getMin() != null ? commonGriddedTile
                    .getMin() : 0;
            double max = commonGriddedTile.getMax() != null ? commonGriddedTile
                    .getMax() : 2000.0;
            commonGriddedTile.setStandardDeviation((max - min) * Math.random());
            defaultGTStandardDeviation = false;
        }

        GriddedTileDao griddedTileDao = elevationTiles.getGriddedTileDao();

        int width = 1 + (int) Math.floor((Math.random() * 4.0));
        int height = 1 + (int) Math.floor((Math.random() * 4.0));
        int tileWidth = 3 + (int) Math.floor((Math.random() * 126.0));
        int tileHeight = 3 + (int) Math.floor((Math.random() * 126.0));
        int minZoomLevel = (int) Math.floor(Math.random() * 22.0);
        int maxZoomLevel = minZoomLevel + (int) Math.floor(Math.random() * 4.0);

        // Just draw one image and re-use
        elevationTiles = new ElevationTilesPng(geoPackage, tileDao);
        byte[] imageBytes = drawTile(elevationTiles, tileWidth, tileHeight,
                griddedCoverage, commonGriddedTile);

        TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

        for (int zoomLevel = minZoomLevel; zoomLevel <= maxZoomLevel; zoomLevel++) {

            TileMatrix tileMatrix = new TileMatrix();
            tileMatrix.setContents(tileMatrixSet.getContents());
            tileMatrix.setMatrixHeight(height);
            tileMatrix.setMatrixWidth(width);
            tileMatrix.setTileHeight(tileHeight);
            tileMatrix.setTileWidth(tileWidth);
            tileMatrix.setPixelXSize((bbox.getMaxLongitude() - bbox
                    .getMinLongitude()) / width / tileWidth);
            tileMatrix.setPixelYSize((bbox.getMaxLatitude() - bbox
                    .getMinLatitude()) / height / tileHeight);
            tileMatrix.setZoomLevel(zoomLevel);
            TestCase.assertEquals(1, tileMatrixDao.create(tileMatrix));

            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {

                    TileRow tileRow = tileDao.newRow();
                    tileRow.setTileColumn(column);
                    tileRow.setTileRow(row);
                    tileRow.setZoomLevel(zoomLevel);
                    tileRow.setTileData(imageBytes);

                    long tileId = tileDao.create(tileRow);
                    TestCase.assertTrue(tileId >= 0);

                    GriddedTile griddedTile = new GriddedTile();
                    griddedTile.setContents(tileMatrixSet.getContents());
                    griddedTile.setTableId(tileId);
                    griddedTile.setScale(commonGriddedTile.getScale());
                    griddedTile.setOffset(commonGriddedTile.getOffset());
                    griddedTile.setMin(commonGriddedTile.getMin());
                    griddedTile.setMax(commonGriddedTile.getMax());
                    griddedTile.setMean(commonGriddedTile.getMean());
                    griddedTile.setStandardDeviation(commonGriddedTile
                            .getStandardDeviation());

                    TestCase.assertEquals(1, griddedTileDao.create(griddedTile));
                    long gtId = griddedTile.getId();
                    TestCase.assertTrue(gtId >= 0);

                    griddedTile = griddedTileDao.queryForId(gtId);
                    TestCase.assertNotNull(griddedTile);
                    if (defaultGTScale) {
                        TestCase.assertEquals(1.0, griddedTile.getScale());
                    } else {
                        TestCase.assertTrue(griddedTile.getScale() >= 0.0
                                && griddedTile.getScale() <= 100.0);
                    }
                    if (defaultGTOffset) {
                        TestCase.assertEquals(0.0, griddedTile.getOffset());
                    } else {
                        TestCase.assertTrue(griddedTile.getOffset() >= 0.0
                                && griddedTile.getOffset() <= 100.0);
                    }
                    if (defaultGTMin) {
                        TestCase.assertNull(griddedTile.getMin());
                    } else {
                        TestCase.assertTrue(griddedTile.getMin() >= 0.0
                                && griddedTile.getMin() <= 1000.0);
                    }
                    if (defaultGTMax) {
                        TestCase.assertNull(griddedTile.getMax());
                    } else {
                        TestCase.assertTrue(griddedTile.getMax() >= 0.0
                                && griddedTile.getMax() <= 2000.0);
                    }
                    if (defaultGTMean) {
                        TestCase.assertNull(griddedTile.getMean());
                    } else {
                        TestCase.assertTrue(griddedTile.getMean() >= 0.0
                                && griddedTile.getMean() <= 2000.0);
                    }
                    if (defaultGTStandardDeviation) {
                        TestCase.assertNull(griddedTile.getStandardDeviation());
                    } else {
                        TestCase.assertTrue(griddedTile.getStandardDeviation() >= 0.0
                                && griddedTile.getStandardDeviation() <= 2000.0);
                    }
                }

            }
            height *= 2;
            width *= 2;
        }

        return geoPackage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {

        // Close
        if (geoPackage != null) {
            geoPackage.close();
        }

        super.tearDown();
    }

    /**
     * Draw an elevation tile with random values
     *
     * @param elevationTiles
     * @param tileWidth
     * @param tileHeight
     * @param griddedCoverage
     * @param commonGriddedTile
     * @return
     */
    private byte[] drawTile(ElevationTilesPng elevationTiles, int tileWidth,
                            int tileHeight, GriddedCoverage griddedCoverage,
                            GriddedTile commonGriddedTile) {

        elevationTileValues.tilePixels = new short[tileHeight][tileWidth];
        elevationTileValues.tileUnsignedPixels = new int[tileHeight][tileWidth];
        elevationTileValues.tileElevations = new Double[tileHeight][tileWidth];
        elevationTileValues.tilePixelsFlat = new short[tileHeight * tileWidth];
        elevationTileValues.tileUnsignedPixelsFlat = new int[tileHeight
                * tileWidth];
        elevationTileValues.tileElevationsFlat = new Double[tileHeight
                * tileWidth];

        GriddedTile griddedTile = new GriddedTile();
        griddedTile.setScale(commonGriddedTile.getScale());
        griddedTile.setOffset(commonGriddedTile.getOffset());
        griddedTile.setMin(commonGriddedTile.getMin());
        griddedTile.setMax(commonGriddedTile.getMax());
        griddedTile.setMean(commonGriddedTile.getMean());
        griddedTile.setStandardDeviation(commonGriddedTile
                .getStandardDeviation());

        // Create the image and graphics
        for (int x = 0; x < tileWidth; x++) {
            for (int y = 0; y < tileHeight; y++) {
                int unsignedValue;
                if (allowNulls && Math.random() < .05) {
                    unsignedValue = griddedCoverage.getDataNull().intValue();
                } else {
                    unsignedValue = Short.MAX_VALUE - Short.MIN_VALUE - 1;
                    unsignedValue = (int) Math.floor(Math.random()
                            * unsignedValue);
                }
                short value = (short) unsignedValue;

                elevationTileValues.tilePixels[y][x] = value;
                elevationTileValues.tileUnsignedPixels[y][x] = unsignedValue;
                elevationTileValues.tileElevations[y][x] = elevationTiles
                        .getElevationValue(griddedTile, value);

                elevationTileValues.tilePixelsFlat[(y * tileWidth) + x] = elevationTileValues.tilePixels[y][x];
                elevationTileValues.tileUnsignedPixelsFlat[(y * tileWidth) + x] = elevationTileValues.tileUnsignedPixels[y][x];
                elevationTileValues.tileElevationsFlat[(y * tileWidth) + x] = elevationTileValues.tileElevations[y][x];
            }
        }

        byte[] imageData = elevationTiles
                .drawTileData(elevationTileValues.tilePixels);

        GeoPackageGeometryDataUtils.compareByteArrays(imageData, elevationTiles
                .drawTileData(elevationTileValues.tileUnsignedPixels));
        GeoPackageGeometryDataUtils.compareByteArrays(imageData, elevationTiles
                .drawTileData(griddedTile, elevationTileValues.tileElevations));
        GeoPackageGeometryDataUtils.compareByteArrays(imageData, elevationTiles
                .drawTileData(elevationTileValues.tilePixelsFlat, tileWidth,
                        tileHeight));
        GeoPackageGeometryDataUtils.compareByteArrays(imageData, elevationTiles
                .drawTileData(elevationTileValues.tileUnsignedPixelsFlat,
                        tileWidth, tileHeight));
        GeoPackageGeometryDataUtils.compareByteArrays(imageData, elevationTiles
                .drawTileData(griddedTile,
                        elevationTileValues.tileElevationsFlat, tileWidth,
                        tileHeight));

        return imageData;
    }

}
