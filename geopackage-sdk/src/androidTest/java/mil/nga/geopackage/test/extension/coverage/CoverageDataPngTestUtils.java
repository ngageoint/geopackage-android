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
     * @param geoPackage         GeoPackage
     * @param coverageDataValues coverage data values
     * @param algorithm          algorithm
     * @param allowNulls         true if nulls are allowed
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
            CoverageDataTestUtils.testCoverageDataQueries(geoPackage, coverageData, tileMatrixSet,
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
     * @param allowNulls         allow nulls
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

}
