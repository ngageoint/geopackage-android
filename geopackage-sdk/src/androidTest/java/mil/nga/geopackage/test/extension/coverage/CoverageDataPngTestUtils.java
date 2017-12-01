package mil.nga.geopackage.test.extension.coverage;

import junit.framework.TestCase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.ExtensionsDao;
import mil.nga.geopackage.extension.coverage.CoverageDataAlgorithm;
import mil.nga.geopackage.extension.coverage.CoverageDataPng;
import mil.nga.geopackage.extension.coverage.CoverageDataPngImage;
import mil.nga.geopackage.extension.coverage.CoverageDataResults;
import mil.nga.geopackage.extension.coverage.GriddedCoverage;
import mil.nga.geopackage.extension.coverage.GriddedCoverageDataType;
import mil.nga.geopackage.extension.coverage.GriddedCoverageEncodingType;
import mil.nga.geopackage.extension.coverage.GriddedTile;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.test.CreateCoverageDataGeoPackageTestCase.CoverageDataValues;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;

/**
 * Coverage Data PNG test utils
 *
 * @author osbornb
 */
public class CoverageDataPngTestUtils {

    /**
     * Test coverage data GeoPackage
     *
     * @param geoPackage          GeoPackage
     * @param coverageDataValues coverage data values
     * @param algorithm           algorithm
     * @param allowNulls          true if nulls are allowed
     * @throws Exception
     */
    public static void testCoverageData(GeoPackage geoPackage,
                                        CoverageDataValues coverageDataValues,
                                        CoverageDataAlgorithm algorithm, boolean allowNulls)
            throws Exception {

        // Verify the coverage data shows up as a coverage data table and not a tile
        // table
        List<String> tilesTables = geoPackage.getTileTables();
        List<String> coverageDataTables = CoverageDataPng.getTables(geoPackage);
        TestCase.assertFalse(coverageDataTables.isEmpty());
        for (String tilesTable : tilesTables) {
            TestCase.assertFalse(coverageDataTables.contains(tilesTable));
        }

        TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();
        TestCase.assertTrue(dao.isTableExists());

        for (String coverageTable : coverageDataTables) {

            TileMatrixSet tileMatrixSet = dao.queryForId(coverageTable);
            TestCase.assertNotNull(tileMatrixSet);

            // Test the tile matrix set
            TestCase.assertNotNull(tileMatrixSet.getTableName());
            TestCase.assertNotNull(tileMatrixSet.getId());
            TestCase.assertNotNull(tileMatrixSet.getSrsId());
            TestCase.assertNotNull(tileMatrixSet.getMinX());
            TestCase.assertNotNull(tileMatrixSet.getMinY());
            TestCase.assertNotNull(tileMatrixSet.getMaxX());
            TestCase.assertNotNull(tileMatrixSet.getMaxY());

            // Test the tile matrix set SRS
            SpatialReferenceSystem srs = tileMatrixSet.getSrs();
            TestCase.assertNotNull(srs);
            TestCase.assertNotNull(srs.getSrsName());
            TestCase.assertNotNull(srs.getSrsId());
            TestCase.assertTrue(srs.getOrganization().equalsIgnoreCase("epsg"));
            TestCase.assertNotNull(srs.getOrganizationCoordsysId());
            TestCase.assertNotNull(srs.getDefinition());

            // Test the contents
            Contents contents = tileMatrixSet.getContents();
            TestCase.assertNotNull(contents);
            TestCase.assertEquals(tileMatrixSet.getTableName(),
                    contents.getTableName());
            TestCase.assertEquals(ContentsDataType.GRIDDED_COVERAGE,
                    contents.getDataType());
            TestCase.assertEquals(ContentsDataType.GRIDDED_COVERAGE.getName(),
                    contents.getDataTypeString());
            TestCase.assertNotNull(contents.getLastChange());

            // Test the contents SRS
            SpatialReferenceSystem contentsSrs = contents.getSrs();
            TestCase.assertNotNull(contentsSrs);
            TestCase.assertNotNull(contentsSrs.getSrsName());
            TestCase.assertNotNull(contentsSrs.getSrsId());
            TestCase.assertNotNull(contentsSrs.getOrganization());
            TestCase.assertNotNull(contentsSrs.getOrganizationCoordsysId());
            TestCase.assertNotNull(contentsSrs.getDefinition());

            // Test the coverage data tiles extension is on
            TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
            CoverageDataPng coverageData = new CoverageDataPng(geoPackage,
                    tileDao);
            TestCase.assertTrue(coverageData.has());
            coverageData.setAlgorithm(algorithm);
            GriddedCoverageEncodingType encoding = coverageData
                    .getGriddedCoverage().getGridCellEncodingType();
            coverageData.setEncoding(encoding);

            // Test the 3 extension rows
            ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();

            Extensions griddedCoverageExtension = extensionsDao
                    .queryByExtension(CoverageDataPng.EXTENSION_NAME,
                            GriddedCoverage.TABLE_NAME, null);
            TestCase.assertNotNull(griddedCoverageExtension);
            TestCase.assertEquals(GriddedCoverage.TABLE_NAME,
                    griddedCoverageExtension.getTableName());
            TestCase.assertNull(griddedCoverageExtension.getColumnName());
            TestCase.assertEquals(CoverageDataPng.EXTENSION_NAME,
                    griddedCoverageExtension.getExtensionName());
            TestCase.assertEquals(CoverageDataPng.EXTENSION_DEFINITION,
                    griddedCoverageExtension.getDefinition());
            TestCase.assertEquals(ExtensionScopeType.READ_WRITE,
                    griddedCoverageExtension.getScope());

            Extensions griddedTileExtension = extensionsDao
                    .queryByExtension(CoverageDataPng.EXTENSION_NAME,
                            GriddedTile.TABLE_NAME, null);
            TestCase.assertNotNull(griddedTileExtension);
            TestCase.assertEquals(GriddedTile.TABLE_NAME,
                    griddedTileExtension.getTableName());
            TestCase.assertNull(griddedTileExtension.getColumnName());
            TestCase.assertEquals(CoverageDataPng.EXTENSION_NAME,
                    griddedTileExtension.getExtensionName());
            TestCase.assertEquals(CoverageDataPng.EXTENSION_DEFINITION,
                    griddedTileExtension.getDefinition());
            TestCase.assertEquals(ExtensionScopeType.READ_WRITE,
                    griddedTileExtension.getScope());

            Extensions tileTableExtension = extensionsDao.queryByExtension(
                    CoverageDataPng.EXTENSION_NAME,
                    tileMatrixSet.getTableName(), TileTable.COLUMN_TILE_DATA);
            TestCase.assertNotNull(tileTableExtension);
            TestCase.assertEquals(tileMatrixSet.getTableName(),
                    tileTableExtension.getTableName());
            TestCase.assertEquals(TileTable.COLUMN_TILE_DATA,
                    tileTableExtension.getColumnName());
            TestCase.assertEquals(CoverageDataPng.EXTENSION_NAME,
                    tileTableExtension.getExtensionName());
            TestCase.assertEquals(CoverageDataPng.EXTENSION_DEFINITION,
                    tileTableExtension.getDefinition());
            TestCase.assertEquals(ExtensionScopeType.READ_WRITE,
                    tileTableExtension.getScope());

            // Test the Gridded Coverage
            GriddedCoverage griddedCoverage = coverageData
                    .getGriddedCoverage();
            TestCase.assertNotNull(griddedCoverage);
            TestCase.assertTrue(griddedCoverage.getId() >= 0);
            TestCase.assertNotNull(griddedCoverage.getTileMatrixSet());
            TestCase.assertEquals(tileMatrixSet.getTableName(),
                    griddedCoverage.getTileMatrixSetName());
            TestCase.assertEquals(GriddedCoverageDataType.INTEGER,
                    griddedCoverage.getDataType());
            TestCase.assertTrue(griddedCoverage.getScale() >= 0);
            if (coverageDataValues != null) {
                TestCase.assertTrue(griddedCoverage.getOffset() >= 0);
            }
            TestCase.assertTrue(griddedCoverage.getPrecision() >= 0);
            griddedCoverage.getDataNull();
            griddedCoverage.getUom();
            if (coverageDataValues != null) {
                TestCase.assertEquals(encoding,
                        griddedCoverage.getGridCellEncodingType());
                TestCase.assertEquals(encoding.getName(),
                        griddedCoverage.getGridCellEncoding());
                TestCase.assertEquals("Height", griddedCoverage.getFieldName());
                TestCase.assertEquals("Height",
                        griddedCoverage.getQuantityDefinition());
            } else {
                TestCase.assertNotNull(griddedCoverage
                        .getGridCellEncodingType());
                griddedCoverage.getGridCellEncoding();
            }

            // Test the Gridded Tile
            List<GriddedTile> griddedTiles = coverageData.getGriddedTile();
            TestCase.assertNotNull(griddedTiles);
            TestCase.assertFalse(griddedTiles.isEmpty());
            for (GriddedTile griddedTile : griddedTiles) {
                TileRow tileRow = tileDao.queryForIdRow(griddedTile
                        .getTableId());
                testTileRow(geoPackage, coverageDataValues, coverageData,
                        tileMatrixSet, griddedTile, tileRow, algorithm,
                        allowNulls);
            }

            TileCursor tileResultSet = tileDao.queryForAll();
            TestCase.assertNotNull(tileResultSet);
            TestCase.assertTrue(tileResultSet.getCount() > 0);
            while (tileResultSet.moveToNext()) {
                TileRow tileRow = tileResultSet.getRow();
                GriddedTile griddedTile = coverageData.getGriddedTile(tileRow
                        .getId());
                testTileRow(geoPackage, coverageDataValues, coverageData,
                        tileMatrixSet, griddedTile, tileRow, algorithm,
                        allowNulls);
            }
            tileResultSet.close();

            // Perform coverage data query tests
            testCoverageDataQueries(geoPackage, coverageData, tileMatrixSet,
                    algorithm, allowNulls);
        }

    }

    /**
     * Perform tests on the tile row
     *
     * @param geoPackage
     * @param coverageDataValues
     * @param coverageData
     * @param tileMatrixSet
     * @param griddedTile
     * @param tileRow
     * @param algorithm
     * @param allowNulls          allow nulls
     * @throws IOException
     * @throws SQLException
     */
    private static void testTileRow(GeoPackage geoPackage,
                                    CoverageDataValues coverageDataValues,
                                    CoverageDataPng coverageData, TileMatrixSet tileMatrixSet,
                                    GriddedTile griddedTile, TileRow tileRow,
                                    CoverageDataAlgorithm algorithm, boolean allowNulls)
            throws IOException, SQLException {

        TestCase.assertNotNull(griddedTile);
        TestCase.assertTrue(griddedTile.getId() >= 0);
        TestCase.assertNotNull(griddedTile.getContents());
        TestCase.assertEquals(tileMatrixSet.getTableName(),
                griddedTile.getTableName());
        long tableId = griddedTile.getTableId();
        TestCase.assertTrue(tableId >= 0);
        TestCase.assertTrue(griddedTile.getScale() >= 0);
        if (coverageDataValues != null) {
            TestCase.assertTrue(griddedTile.getOffset() >= 0);
        }
        griddedTile.getMin();
        griddedTile.getMax();
        griddedTile.getMean();
        griddedTile.getStandardDeviation();
        TestCase.assertNotNull(tileRow);

        TestCase.assertNotNull(tileRow);
        byte[] tileData = tileRow.getTileData();
        TestCase.assertTrue(tileData.length > 0);
        CoverageDataPngImage image = new CoverageDataPngImage(tileRow);

        // Get all the pixel values of the image
        int[] pixelValues = coverageData.getPixelValues(tileData);
        if (coverageDataValues != null) {
            for (int i = 0; i < pixelValues.length; i++) {
                TestCase.assertEquals(coverageDataValues.tilePixelsFlat[i],
                        coverageData.getPixelValue(pixelValues[i]));
            }
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // Get each individual image pixel value
        int[] pixelValuesTested = new int[height * width];

        // Get each individual image pixel value
        List<Integer> pixelValuesList = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = image.getPixel(x, y);
                pixelValuesList.add(pixelValue);

                // Test getting the pixel value from the pixel values
                // array
                int pixelValue2 = coverageData.getUnsignedPixelValue(pixelValues,
                        width, x, y);
                TestCase.assertEquals(pixelValue, pixelValue2);

                // Test getting the coverage data value
                Double value = coverageData.getValue(
                        griddedTile, pixelValue);
                GriddedCoverage griddedCoverage = coverageData
                        .getGriddedCoverage();
                if (coverageDataValues != null) {
                    TestCase.assertEquals(coverageDataValues.tilePixels[y][x],
                            coverageData.getPixelValue(pixelValue));
                    TestCase.assertEquals(
                            coverageDataValues.tilePixelsFlat[(y * width) + x],
                            coverageData.getPixelValue(pixelValue));
                    TestCase.assertEquals(
                            coverageDataValues.tileUnsignedPixels[y][x],
                            pixelValue);
                    TestCase.assertEquals(
                            coverageDataValues.tileUnsignedPixelsFlat[(y * width)
                                    + x], pixelValue);
                }
                if (griddedCoverage.getDataNull() != null && pixelValue == griddedCoverage
                        .getDataNull()) {
                    TestCase.assertNull(value);
                } else {
                    TestCase.assertEquals(
                            (pixelValue * griddedTile.getScale() + griddedTile
                                    .getOffset())
                                    * griddedCoverage.getScale()
                                    + griddedCoverage.getOffset(),
                            value);
                }
            }
        }

        // Test the individually built list of pixel values vs the full
        // returned array
        TestCase.assertEquals(pixelValuesList.size(), pixelValues.length);
        for (int i = 0; i < pixelValuesList.size(); i++) {
            TestCase.assertEquals((int) pixelValuesList.get(i),
                    pixelValues[i]);
        }

        TileMatrix tileMatrix = coverageData.getTileDao().getTileMatrix(
                tileRow.getZoomLevel());
        double xDistance = tileMatrixSet.getMaxX() - tileMatrixSet.getMinX();
        double xDistance2 = tileMatrix.getMatrixWidth()
                * tileMatrix.getTileWidth() * tileMatrix.getPixelXSize();
        TestCase.assertEquals(xDistance, xDistance2, .0000000001);
        double yDistance = tileMatrixSet.getMaxY() - tileMatrixSet.getMinY();
        double yDistance2 = tileMatrix.getMatrixHeight()
                * tileMatrix.getTileHeight() * tileMatrix.getPixelYSize();
        TestCase.assertEquals(yDistance, yDistance2, .0000000001);
        BoundingBox boundingBox = TileBoundingBoxUtils.getBoundingBox(
                tileMatrixSet.getBoundingBox(), tileMatrix,
                tileRow.getTileColumn(), tileRow.getTileRow());
        CoverageDataResults coverageDataResults = coverageData
                .getValues(boundingBox);
        if (coverageDataValues != null) {
            TestCase.assertEquals(coverageDataValues.coverageData.length,
                    coverageDataResults.getValues().length);
            TestCase.assertEquals(coverageDataValues.coverageData[0].length,
                    coverageDataResults.getValues()[0].length);
            TestCase.assertEquals(
                    coverageDataValues.coverageDataFlat.length,
                    coverageDataResults.getValues().length
                            * coverageDataResults.getValues()[0].length);
            for (int y = 0; y < coverageDataResults.getValues().length; y++) {
                for (int x = 0; x < coverageDataResults.getValues()[0].length; x++) {
                    switch (algorithm) {
                        case BICUBIC:
                            // Don't test the edges
                            if (y > 1
                                    && y < coverageDataValues.coverageData.length - 2
                                    && x > 1
                                    && x < coverageDataValues.coverageData[0].length - 2) {
                                if (!allowNulls) {
                                    // No nulls allowed, check for equality
                                    TestCase.assertEquals(
                                            coverageDataValues.coverageData[y][x],
                                            coverageDataResults.getValues()[y][x]);
                                } else {
                                    // Verify there is null neighbor value
                                    Double value1 = coverageDataValues.coverageData[y][x];
                                    Double value2 = coverageDataResults
                                            .getValues()[y][x];
                                    if (value1 == null ? value2 != null : !value1
                                            .equals(value2)) {
                                        boolean nullValue = false;
                                        for (int yLocation = y - 2; !nullValue
                                                && yLocation <= y + 2; yLocation++) {
                                            for (int xLocation = x - 2; xLocation <= x + 2; xLocation++) {
                                                if (coverageDataValues.coverageData[yLocation][xLocation] == null) {
                                                    nullValue = true;
                                                    break;
                                                }
                                            }
                                        }
                                        TestCase.assertTrue(nullValue);
                                    }
                                }

                            }
                            break;
                        case BILINEAR:
                            // Don't test the edges
                            if (y > 0
                                    && y < coverageDataValues.coverageData.length - 1
                                    && x > 0
                                    && x < coverageDataValues.coverageData[0].length - 1) {
                                if (!allowNulls) {
                                    // No nulls allowed, check for equality
                                    TestCase.assertEquals(
                                            coverageDataValues.coverageData[y][x],
                                            coverageDataResults.getValues()[y][x]);
                                } else {
                                    // Verify there is null neighbor value
                                    Double value1 = coverageDataValues.coverageData[y][x];
                                    Double value2 = coverageDataResults
                                            .getValues()[y][x];
                                    if (value1 == null ? value2 != null : !value1
                                            .equals(value2)) {
                                        boolean nullValue = false;
                                        for (int yLocation = y - 1; !nullValue
                                                && yLocation <= y + 1; yLocation++) {
                                            for (int xLocation = x - 1; xLocation <= x + 1; xLocation++) {
                                                if (coverageDataValues.coverageData[yLocation][xLocation] == null) {
                                                    nullValue = true;
                                                    break;
                                                }
                                            }
                                        }
                                        TestCase.assertTrue(nullValue);
                                    }
                                }

                            }
                            break;
                        case NEAREST_NEIGHBOR:
                            if (!allowNulls) {
                                TestCase.assertEquals(
                                        coverageDataValues.coverageData[y][x],
                                        coverageDataResults.getValues()[y][x]);
                            } else {
                                Double value1 = coverageDataValues.coverageData[y][x];
                                Double value2 = coverageDataResults
                                        .getValues()[y][x];
                                if (value1 == null ? value2 != null : !value1
                                        .equals(value2)) {
                                    // Find a matching neighbor
                                    boolean nonNull = false;
                                    boolean match = false;
                                    for (int yLocation = Math.max(0, y - 1); !match
                                            && yLocation <= y + 1
                                            && yLocation < coverageDataValues.coverageData.length; yLocation++) {
                                        for (int xLocation = Math.max(0, x - 1); xLocation <= x + 1
                                                && xLocation < coverageDataValues.coverageData[yLocation].length; xLocation++) {
                                            Double value = coverageDataValues.coverageData[yLocation][xLocation];
                                            if (value != null) {
                                                nonNull = true;
                                                match = value.equals(value2);
                                                if (match) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (!match) {
                                        if (nonNull) {
                                            TestCase.assertNotNull(value2);
                                        } else {
                                            TestCase.assertNull(value2);
                                        }
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }

    }

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
    private static void testCoverageDataQueries(GeoPackage geoPackage,
                                                CoverageDataPng coverageData, TileMatrixSet tileMatrixSet,
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
        CoverageDataPng coverageData2 = new CoverageDataPng(geoPackage,
                coverageData.getTileDao(), requestProjection);
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
     * @param geoPackage          GeoPackage
     * @param coverageDataValues coverage data values
     * @param algorithm           algorithm
     * @param allowNulls          allow null coverage data values
     * @throws Exception
     */
    public static void testRandomBoundingBox(GeoPackage geoPackage,
                                             CoverageDataValues coverageDataValues,
                                             CoverageDataAlgorithm algorithm, boolean allowNulls)
            throws Exception {

        // Verify the coverage data shows up as a coverage data table and not a tile
        // table
        List<String> tilesTables = geoPackage.getTileTables();
        List<String> coverageDataTables = CoverageDataPng.getTables(geoPackage);
        TestCase.assertFalse(coverageDataTables.isEmpty());
        for (String tilesTable : tilesTables) {
            TestCase.assertFalse(coverageDataTables.contains(tilesTable));
        }

        TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();
        TestCase.assertTrue(dao.isTableExists());

        for (String coverageTable : coverageDataTables) {

            TileMatrixSet tileMatrixSet = dao.queryForId(coverageTable);

            TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
            CoverageDataPng coverageData = new CoverageDataPng(geoPackage,
                    tileDao);
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

            CoverageDataResults coverageDataResults = coverageData
                    .getValues(requestBoundingBox);

            TestCase.assertNotNull(coverageDataResults);
            TestCase.assertNotNull(coverageDataResults.getValues());
            TestCase.assertEquals(coverageDataResults.getValues()[0].length,
                    coverageDataResults.getWidth());
            TestCase.assertEquals(coverageDataResults.getValues().length,
                    coverageDataResults.getHeight());
            TestCase.assertNotNull(coverageDataResults.getTileMatrix());
            TestCase.assertTrue(coverageDataResults.getZoomLevel() >= 0);
            TestCase.assertTrue(coverageDataResults.getValues().length > 0);
            TestCase.assertTrue(coverageDataResults.getValues()[0].length > 0);
            TestCase.assertEquals(specifiedHeight, coverageDataResults.getHeight());
            TestCase.assertEquals(specifiedWidth, coverageDataResults.getWidth());

            for (int y = 0; y < specifiedHeight; y++) {
                boolean nonNullFound = false;
                boolean secondNullsFound = false;
                for (int x = 0; x < specifiedWidth; x++) {
                    TestCase.assertEquals(coverageDataResults.getValues()[y][x],
                            coverageDataResults.getValue(y, x));
                    if (!allowNulls) {
                        if (coverageDataResults.getValues()[y][x] != null) {
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
                    TestCase.assertEquals(coverageDataResults.getValues()[y][x],
                            coverageDataResults.getValue(y, x));
                    if (!allowNulls) {
                        if (coverageDataResults.getValues()[y][x] != null) {
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
     * @return coverage data value
     * @throws Exception
     */
    public static Double getValue(GeoPackage geoPackage,
                                  CoverageDataAlgorithm algorithm, double latitude,
                                  double longitude, long epsg) throws Exception {

        Double value = null;

        List<String> coverageDataTables = CoverageDataPng.getTables(geoPackage);
        TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();

        for (String coverageTable : coverageDataTables) {

            TileMatrixSet tileMatrixSet = dao.queryForId(coverageTable);
            TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

            Projection requestProjection = ProjectionFactory
                    .getProjection(epsg);

            // Test getting the coverage data value of a single coordinate
            CoverageDataPng coverageDate = new CoverageDataPng(geoPackage,
                    tileDao, requestProjection);
            coverageDate.setAlgorithm(algorithm);
            value = coverageDate.getValue(latitude, longitude);
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
     * @param width       results height
     * @return coverage data results
     * @throws Exception
     */
    public static CoverageDataResults getValues(GeoPackage geoPackage,
                                                CoverageDataAlgorithm algorithm, BoundingBox boundingBox,
                                                int width, int height, long epsg) throws Exception {

        CoverageDataResults values = null;

        List<String> coverageDataTables = CoverageDataPng.getTables(geoPackage);
        TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();

        for (String coverageTable : coverageDataTables) {

            TileMatrixSet tileMatrixSet = dao.queryForId(coverageTable);
            TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

            Projection requestProjection = ProjectionFactory
                    .getProjection(epsg);

            // Test getting the coverage data value of a single coordinate
            CoverageDataPng coverageData = new CoverageDataPng(geoPackage,
                    tileDao, requestProjection);
            coverageData.setAlgorithm(algorithm);
            coverageData.setWidth(width);
            coverageData.setHeight(height);
            values = coverageData.getValues(boundingBox);
        }

        return values;
    }

}
