package mil.nga.geopackage.test.extension.coverage;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.coverage.CoverageData;
import mil.nga.geopackage.extension.coverage.CoverageDataAlgorithm;
import mil.nga.geopackage.extension.coverage.CoverageDataResults;
import mil.nga.geopackage.extension.coverage.GriddedCoverage;
import mil.nga.geopackage.extension.coverage.GriddedCoverageEncodingType;
import mil.nga.geopackage.extension.coverage.GriddedTile;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Coverage Data test utils
 *
 * @author osbornb
 */
public class CoverageDataTestUtils {

    /**
     * Test performing coverage data queries
     *
     * @param geoPackage
     * @param coverageData
     * @param tileMatrixSet
     * @param algorithm
     * @param allowNulls
     * @throws SQLException
     */
    public static void testCoverageDataQueries(GeoPackage geoPackage,
                                               CoverageData<?> coverageData, TileMatrixSet tileMatrixSet,
                                               CoverageDataAlgorithm algorithm, boolean allowNulls)
            throws SQLException {

        // Determine an alternate projection
        BoundingBox boundingBox = tileMatrixSet.getBoundingBox();
        SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();
        long srsId = tileMatrixSet.getSrsId();
        SpatialReferenceSystem srs = srsDao.queryForId(srsId);

        long epsg = srs.getOrganizationCoordsysId();
        Projection projection = ProjectionFactory.getProjection(srs);
        long requestEpsg = -1;
        if (epsg == ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM) {
            requestEpsg = ProjectionConstants.EPSG_WEB_MERCATOR;
        } else {
            requestEpsg = ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM;
        }
        Projection requestProjection = ProjectionFactory
                .getProjection(requestEpsg);
        ProjectionTransform coverageToRequest = projection
                .getTransformation(requestProjection);
        BoundingBox projectedBoundingBox = coverageToRequest
                .transform(boundingBox);

        // Get a random coordinate
        double latDistance = projectedBoundingBox.getMaxLatitude()
                - projectedBoundingBox.getMinLatitude();
        double latitude = latDistance * .9 * Math.random()
                + projectedBoundingBox.getMinLatitude() + (.05 * latDistance);
        double lonDistance = projectedBoundingBox.getMaxLongitude()
                - projectedBoundingBox.getMinLongitude();
        double longitude = lonDistance * .9 * Math.random()
                + projectedBoundingBox.getMinLongitude() + (.05 * lonDistance);

        // Test getting the coverage data value of a single coordinate
        CoverageData<?> coverageData2 = CoverageData.getCoverageData(geoPackage, coverageData.getTileDao(), requestProjection);
        coverageData2.setAlgorithm(algorithm);
        Double value = coverageData2.getValue(latitude, longitude);
        if (!allowNulls) {
            TestCase.assertNotNull(value);
        }

        // Build a random bounding box
        double minLatitude = (projectedBoundingBox.getMaxLatitude() - projectedBoundingBox
                .getMinLatitude())
                * Math.random()
                + projectedBoundingBox.getMinLatitude();
        double minLongitude = (projectedBoundingBox.getMaxLongitude() - projectedBoundingBox
                .getMinLongitude())
                * Math.random()
                + projectedBoundingBox.getMinLongitude();
        double maxLatitude = (projectedBoundingBox.getMaxLatitude() - minLatitude)
                * Math.random() + minLatitude;
        double maxLongitude = (projectedBoundingBox.getMaxLongitude() - minLongitude)
                * Math.random() + minLongitude;

        BoundingBox requestBoundingBox = new BoundingBox(minLongitude,
                minLatitude, maxLongitude, maxLatitude);
        CoverageDataResults values = coverageData2
                .getValues(requestBoundingBox);
        TestCase.assertNotNull(values);
        TestCase.assertNotNull(values.getValues());
        TestCase.assertEquals(values.getValues()[0].length,
                values.getWidth());
        TestCase.assertEquals(values.getValues().length,
                values.getHeight());
        TestCase.assertNotNull(values.getTileMatrix());
        TestCase.assertTrue(values.getZoomLevel() >= 0);
        TestCase.assertTrue(values.getValues().length > 0);
        TestCase.assertTrue(values.getValues()[0].length > 0);
        for (int y = 0; y < values.getValues().length; y++) {
            for (int x = 0; x < values.getValues()[y].length; x++) {
                TestCase.assertEquals(values.getValues()[y][x],
                        values.getValue(y, x));
            }
        }

        int specifiedWidth = 50;
        int specifiedHeight = 100;
        coverageData2.setWidth(specifiedWidth);
        coverageData2.setHeight(specifiedHeight);

        values = coverageData2.getValues(requestBoundingBox);
        TestCase.assertNotNull(values);
        TestCase.assertNotNull(values.getValues());
        TestCase.assertEquals(values.getValues()[0].length,
                values.getWidth());
        TestCase.assertEquals(values.getValues().length,
                values.getHeight());
        TestCase.assertNotNull(values.getTileMatrix());
        TestCase.assertTrue(values.getZoomLevel() >= 0);
        TestCase.assertTrue(values.getValues().length > 0);
        TestCase.assertTrue(values.getValues()[0].length > 0);
        TestCase.assertEquals(specifiedHeight, values.getHeight());
        TestCase.assertEquals(specifiedWidth, values.getWidth());
        for (int y = 0; y < specifiedHeight; y++) {
            for (int x = 0; x < specifiedWidth; x++) {
                TestCase.assertEquals(values.getValues()[y][x],
                        values.getValue(y, x));
            }
        }

        values = coverageData2.getValuesUnbounded(requestBoundingBox);
        TestCase.assertNotNull(values);
        TestCase.assertNotNull(values.getValues());
        TestCase.assertEquals(values.getValues()[0].length,
                values.getWidth());
        TestCase.assertEquals(values.getValues().length,
                values.getHeight());
        TestCase.assertNotNull(values.getTileMatrix());
        TestCase.assertTrue(values.getZoomLevel() >= 0);
        TestCase.assertTrue(values.getValues().length > 0);
        TestCase.assertTrue(values.getValues()[0].length > 0);
        TestCase.assertEquals(
                values.getValues()[0].length,
                values.getValues()[values.getValues().length - 1].length);
        for (int y = 0; y < values.getValues().length; y++) {
            for (int x = 0; x < values.getValues()[y].length; x++) {
                TestCase.assertEquals(values.getValues()[y][x],
                        values.getValue(y, x));
            }
        }
    }

    /**
     * Test a random bounding box query
     *
     * @param geoPackage GeoPackage
     * @param algorithm  algorithm
     * @param allowNulls allow null coverage data values
     * @throws Exception
     */
    public static void testRandomBoundingBox(GeoPackage geoPackage,
                                             CoverageDataAlgorithm algorithm, boolean allowNulls)
            throws Exception {

        // Verify the coverage data shows up as a coverage data table and not a tile
        // table
        List<String> tilesTables = geoPackage.getTileTables();
        List<String> coverageDataTables = CoverageData.getTables(geoPackage);
        TestCase.assertFalse(coverageDataTables.isEmpty());
        for (String tilesTable : tilesTables) {
            TestCase.assertFalse(coverageDataTables.contains(tilesTable));
        }

        TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();
        TestCase.assertTrue(dao.isTableExists());

        for (String coverageTable : coverageDataTables) {

            TileMatrixSet tileMatrixSet = dao.queryForId(coverageTable);

            TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
            CoverageData<?> coverageData = CoverageData.getCoverageData(geoPackage, tileDao);
            coverageData.setAlgorithm(algorithm);

            int specifiedWidth = (int) (Math.random() * 100.0) + 1;
            int specifiedHeight = (int) (Math.random() * 100.0) + 1;
            coverageData.setWidth(specifiedWidth);
            coverageData.setHeight(specifiedHeight);

            BoundingBox boundingBox = tileMatrixSet.getBoundingBox();

            // Build a random bounding box
            double minLatitude = (boundingBox.getMaxLatitude() - boundingBox
                    .getMinLatitude())
                    * Math.random()
                    + boundingBox.getMinLatitude();
            double minLongitude = (boundingBox.getMaxLongitude() - boundingBox
                    .getMinLongitude())
                    * Math.random()
                    + boundingBox.getMinLongitude();
            double maxLatitude = (boundingBox.getMaxLatitude() - minLatitude)
                    * Math.random() + minLatitude;
            double maxLongitude = (boundingBox.getMaxLongitude() - minLongitude)
                    * Math.random() + minLongitude;

            BoundingBox requestBoundingBox = new BoundingBox(minLongitude,
                    minLatitude, maxLongitude, maxLatitude);

            CoverageDataResults values = coverageData
                    .getValues(requestBoundingBox);

            TestCase.assertNotNull(values);
            TestCase.assertNotNull(values.getValues());
            TestCase.assertEquals(values.getValues()[0].length,
                    values.getWidth());
            TestCase.assertEquals(values.getValues().length,
                    values.getHeight());
            TestCase.assertNotNull(values.getTileMatrix());
            TestCase.assertTrue(values.getZoomLevel() >= 0);
            TestCase.assertTrue(values.getValues().length > 0);
            TestCase.assertTrue(values.getValues()[0].length > 0);
            TestCase.assertEquals(specifiedHeight, values.getHeight());
            TestCase.assertEquals(specifiedWidth, values.getWidth());

            for (int y = 0; y < specifiedHeight; y++) {
                boolean nonNullFound = false;
                boolean secondNullsFound = false;
                for (int x = 0; x < specifiedWidth; x++) {
                    TestCase.assertEquals(values.getValues()[y][x],
                            values.getValue(y, x));
                    if (!allowNulls) {
                        if (values.getValues()[y][x] != null) {
                            TestCase.assertFalse(secondNullsFound);
                            nonNullFound = true;
                        } else if (nonNullFound) {
                            secondNullsFound = true;
                        }
                    }
                }
            }

            for (int x = 0; x < specifiedWidth; x++) {
                boolean nonNullFound = false;
                boolean secondNullsFound = false;
                for (int y = 0; y < specifiedHeight; y++) {
                    TestCase.assertEquals(values.getValues()[y][x],
                            values.getValue(y, x));
                    if (!allowNulls) {
                        if (values.getValues()[y][x] != null) {
                            TestCase.assertFalse(secondNullsFound);
                            nonNullFound = true;
                        } else if (nonNullFound) {
                            secondNullsFound = true;
                        }
                    }
                }
            }

        }

    }

    /**
     * Get the coverage data value at the coordinate
     *
     * @param geoPackage GeoPackage
     * @param algorithm  algorithm
     * @param latitude   latitude
     * @param longitude  longitude
     * @param epsg       epsg
     * @return coverage data value
     * @throws Exception
     */
    public static Double getValue(GeoPackage geoPackage,
                                  CoverageDataAlgorithm algorithm, double latitude,
                                  double longitude, long epsg) throws Exception {

        Double value = null;

        List<String> coverageDataTables = CoverageData.getTables(geoPackage);
        TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();

        for (String coverageTable : coverageDataTables) {

            TileMatrixSet tileMatrixSet = dao.queryForId(coverageTable);
            TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

            Projection requestProjection = ProjectionFactory
                    .getProjection(epsg);

            // Test getting the coverage data value of a single coordinate
            CoverageData<?> coverageData = CoverageData.getCoverageData(geoPackage, tileDao, requestProjection);
            coverageData.setAlgorithm(algorithm);
            value = coverageData.getValue(latitude, longitude);
        }

        return value;
    }

    /**
     * Get the coverage data for the bounding box
     *
     * @param geoPackage  GeoPackage
     * @param algorithm   algorithm
     * @param boundingBox bounding box
     * @param width       results width
     * @param height      results height
     * @param epsg        epsg code
     * @return coverage data results
     * @throws Exception
     */
    public static CoverageDataResults getValues(GeoPackage geoPackage,
                                                CoverageDataAlgorithm algorithm, BoundingBox boundingBox,
                                                int width, int height, long epsg) throws Exception {

        CoverageDataResults values = null;

        List<String> coverageDataTables = CoverageData.getTables(geoPackage);
        TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();

        for (String coverageTable : coverageDataTables) {

            TileMatrixSet tileMatrixSet = dao.queryForId(coverageTable);
            TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

            Projection requestProjection = ProjectionFactory
                    .getProjection(epsg);

            // Test getting the coverage data value of a single coordinate
            CoverageData<?> coverageData = CoverageData.getCoverageData(geoPackage, tileDao, requestProjection);
            coverageData.setAlgorithm(algorithm);
            coverageData.setWidth(width);
            coverageData.setHeight(height);
            values = coverageData.getValues(boundingBox);
        }

        return values;
    }

    /**
     * Test the pixel encoding location
     *
     * @param geoPackage GeoPackage
     * @param allowNulls allow nulls
     * @throws Exception
     */
    public static void testPixelEncoding(GeoPackage geoPackage,
                                         boolean allowNulls) throws Exception {

        List<String> coverageDataTables = CoverageData.getTables(geoPackage);
        TestCase.assertFalse(coverageDataTables.isEmpty());

        TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
        TestCase.assertTrue(tileMatrixSetDao.isTableExists());
        TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
        TestCase.assertTrue(tileMatrixDao.isTableExists());

        for (String coverageTable : coverageDataTables) {

            TileMatrixSet tileMatrixSet = tileMatrixSetDao
                    .queryForId(coverageTable);

            TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
            CoverageData<?> coverageData = CoverageData.getCoverageData(
                    geoPackage, tileDao);
            GriddedCoverage griddedCoverage = coverageData.getGriddedCoverage();
            GriddedCoverageEncodingType encoding = griddedCoverage
                    .getGridCellEncodingType();

            TileCursor tileCursor = tileDao.queryForTile(tileDao
                    .getMaxZoom());
            TestCase.assertNotNull(tileCursor);
            TestCase.assertTrue(tileCursor.getCount() > 0);
            while (tileCursor.moveToNext()) {
                TileRow tileRow = tileCursor.getRow();

                TileMatrix tileMatrix = tileDao.getTileMatrix(tileRow
                        .getZoomLevel());
                TestCase.assertNotNull(tileMatrix);

                GriddedTile griddedTile = coverageData.getGriddedTile(tileRow
                        .getId());
                TestCase.assertNotNull(griddedTile);

                byte[] tileData = tileRow.getTileData();
                TestCase.assertNotNull(tileData);

                BoundingBox boundingBox = TileBoundingBoxUtils.getBoundingBox(
                        tileMatrixSet.getBoundingBox(), tileMatrix,
                        tileRow.getTileColumn(), tileRow.getTileRow());

                int tileHeight = (int) tileMatrix.getTileHeight();
                int tileWidth = (int) tileMatrix.getTileWidth();

                int heightChunk = Math.max(tileHeight / 10, 1);
                int widthChunk = Math.max(tileWidth / 10, 1);

                for (int y = 0; y < tileHeight; y = Math.min(y + heightChunk,
                        y == tileHeight - 1 ? tileHeight : tileHeight - 1)) {
                    for (int x = 0; x < tileWidth; x = Math.min(x + widthChunk,
                            x == tileWidth - 1 ? tileWidth : tileWidth - 1)) {

                        Double pixelValue = coverageData.getValue(griddedTile,
                                tileData, x, y);
                        double pixelLongitude = boundingBox.getMinLongitude()
                                + (x * tileMatrix.getPixelXSize());
                        double pixelLatitude = boundingBox.getMaxLatitude()
                                - (y * tileMatrix.getPixelYSize());
                        switch (encoding) {
                            case CENTER:
                            case AREA:
                                pixelLongitude += (tileMatrix.getPixelXSize() / 2.0);
                                pixelLatitude -= (tileMatrix.getPixelYSize() / 2.0);
                                break;
                            case CORNER:
                                pixelLatitude -= tileMatrix.getPixelYSize();
                                break;
                        }
                        Double value = coverageData.getValue(pixelLatitude,
                                pixelLongitude);

                        if (!allowNulls || pixelValue != null) {
                            TestCase.assertEquals("x: " + x + ", y: " + y
                                            + ", encoding: " + encoding, pixelValue,
                                    value);
                        }
                    }
                }

                break;
            }
            tileCursor.close();
        }

    }

}
