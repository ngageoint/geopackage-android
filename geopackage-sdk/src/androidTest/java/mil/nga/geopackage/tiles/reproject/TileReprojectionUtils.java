package mil.nga.geopackage.tiles.reproject;

import android.graphics.Bitmap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.io.TestGeoPackageProgress;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.proj.Projection;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.proj.ProjectionTransform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tile Reprojection Utility test methods
 *
 * @author osbornb
 */
public class TileReprojectionUtils {

    /**
     * Test read
     *
     * @param geoPackage GeoPackage
     */
    public static void testReproject(GeoPackage geoPackage) {

        for (String table : randomTileTables(geoPackage)) {

            String reprojectTable = table + "_reproject";
            Projection projection = geoPackage.getProjection(table);
            Projection reprojectProjection = alternateProjection(projection);

            TileDao tileDao = geoPackage.getTileDao(table);
            int count = tileDao.count();
            Map<Long, Integer> counts = zoomCounts(tileDao);

            int tiles = TileReprojection.reproject(geoPackage, table,
                    reprojectTable, reprojectProjection);

            assertEquals(count > 0, tiles > 0);

            assertTrue(projection.equals(geoPackage.getProjection(table)));
            assertTrue(reprojectProjection
                    .equals(geoPackage.getProjection(reprojectTable)));

            tileDao = geoPackage.getTileDao(table);
            compareZoomCounts(count, counts, tileDao);

            TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
            checkZoomCounts(count, counts, reprojectTileDao, tiles);

            Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
            Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
            zoomLevels.removeAll(reprojectZoomLevels);
            assertEquals(0, zoomLevels.size());

            compareBoundingBox(
                    geoPackage.getBoundingBox(reprojectProjection, table),
                    geoPackage.getContentsBoundingBox(reprojectTable),
                    .0000001);
        }

    }

    /**
     * Test reproject replacing the table
     *
     * @param geoPackage GeoPackage
     */
    public static void testReprojectReplace(GeoPackage geoPackage) {

        for (String table : randomTileTables(geoPackage)) {

            Projection projection = geoPackage.getProjection(table);
            Projection reprojectProjection = alternateProjection(projection);

            BoundingBox boundingBox = geoPackage
                    .getBoundingBox(reprojectProjection, table);

            TileDao tileDao = geoPackage.getTileDao(table);
            Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
            int count = tileDao.count();
            Map<Long, Integer> counts = zoomCounts(tileDao);

            int tiles = TileReprojection.reproject(geoPackage, table,
                    reprojectProjection);

            assertEquals(count > 0, tiles > 0);

            assertTrue(reprojectProjection
                    .equals(geoPackage.getProjection(table)));

            TileDao reprojectTileDao = geoPackage.getTileDao(table);
            checkZoomCounts(count, counts, reprojectTileDao, tiles);

            Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
            zoomLevels.removeAll(reprojectZoomLevels);
            assertEquals(0, zoomLevels.size());

            compareBoundingBox(boundingBox,
                    geoPackage.getContentsBoundingBox(table), .0000001);
        }

    }

    /**
     * Test reproject of individual zoom levels
     *
     * @param geoPackage GeoPackage
     */
    public static void testReprojectZoomLevels(GeoPackage geoPackage) {

        for (String table : randomTileTables(geoPackage)) {

            String reprojectTable = table + "_reproject";
            Projection projection = geoPackage.getProjection(table);
            Projection reprojectProjection = alternateProjection(projection);

            TileDao tileDao = geoPackage.getTileDao(table);
            Map<Long, Integer> counts = zoomCounts(tileDao);

            TileReprojection tileReprojection = TileReprojection.create(
                    geoPackage, table, reprojectTable, reprojectProjection);

            for (long zoom : counts.keySet()) {

                int tiles = tileReprojection.reproject(zoom);
                assertEquals(counts.get(zoom) > 0, tiles > 0);
            }

            assertTrue(projection.equals(geoPackage.getProjection(table)));
            assertTrue(reprojectProjection
                    .equals(geoPackage.getProjection(reprojectTable)));

            Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
            TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
            Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
            zoomLevels.removeAll(reprojectZoomLevels);
            assertEquals(0, zoomLevels.size());

            compareBoundingBox(
                    geoPackage.getBoundingBox(reprojectProjection, table),
                    geoPackage.getContentsBoundingBox(reprojectTable),
                    .0000001);
        }

    }

    /**
     * Test reproject of overwriting a zoom level
     *
     * @param geoPackage GeoPackage
     * @throws IOException upon error
     */
    public static void testReprojectZoomOverwrite(GeoPackage geoPackage)
            throws IOException {

        for (String table : randomTileTables(geoPackage)) {

            String reprojectTable = table + "_reproject";
            Projection projection = geoPackage.getProjection(table);
            Projection reprojectProjection = alternateProjection(projection);

            TileDao tileDao = geoPackage.getTileDao(table);
            Map<Long, Integer> counts = zoomCounts(tileDao);

            long zoom = (new ArrayList<>(counts.keySet()))
                    .get((int) (Math.random() * counts.size()));
            TileMatrix tileMatrix = tileDao.getTileMatrix(zoom);

            TileReprojection tileReprojection = TileReprojection.create(
                    geoPackage, table, reprojectTable, reprojectProjection);

            int tiles = tileReprojection.reproject(zoom);
            assertEquals(counts.get(zoom) > 0, tiles > 0);

            tileReprojection = TileReprojection.create(geoPackage, table,
                    reprojectTable, reprojectProjection);

            int tiles2 = tileReprojection.reproject(zoom);
            assertEquals(tiles, tiles2);

            tileReprojection = TileReprojection.create(geoPackage, table,
                    reprojectTable, reprojectProjection);
            tileReprojection.setTileWidth(tileMatrix.getTileWidth() * 2);
            tileReprojection.setTileHeight(tileMatrix.getTileHeight() * 2);

            try {
                tileReprojection.reproject(zoom);
                fail("Reprojection of existing zoom level with new geographic properties did not fail");
            } catch (Exception e) {
                // expected
            }

            tileReprojection.setOverwrite(true);
            int tiles3 = tileReprojection.reproject(zoom);
            assertEquals(tiles, tiles3);

            Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
            TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
            Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
            assertTrue(zoomLevels.contains(zoom));
            assertEquals(1, reprojectZoomLevels.size());
            assertTrue(reprojectZoomLevels.contains(zoom));

            TileMatrix reprojectTileMatrix = reprojectTileDao
                    .getTileMatrix(zoom);
            assertEquals(zoom, reprojectTileMatrix.getZoomLevel());
            assertEquals(tileReprojection.getTileWidth().longValue(),
                    reprojectTileMatrix.getTileWidth());
            assertEquals(tileReprojection.getTileHeight().longValue(),
                    reprojectTileMatrix.getTileHeight());
            assertEquals(tileMatrix.getMatrixWidth(),
                    reprojectTileMatrix.getMatrixWidth());
            assertEquals(tileMatrix.getMatrixHeight(),
                    reprojectTileMatrix.getMatrixHeight());

            TileCursor tileCursor = reprojectTileDao.queryForAll();
            assertTrue(tileCursor.moveToNext());
            TileRow tileRow = tileCursor.getRow();
            Bitmap tileImage = tileRow.getTileDataBitmap();
            assertEquals(tileReprojection.getTileWidth().intValue(),
                    tileImage.getWidth());
            assertEquals(tileReprojection.getTileHeight().intValue(),
                    tileImage.getHeight());
            tileCursor.close();

            compareBoundingBox(
                    geoPackage.getBoundingBox(reprojectProjection, table),
                    geoPackage.getContentsBoundingBox(reprojectTable),
                    .0000001);
        }

    }

    /**
     * Test reproject of overwriting a table
     *
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testReprojectOverwrite(GeoPackage geoPackage)
            throws SQLException {

        for (String table : randomTileTables(geoPackage)) {

            String reprojectTable = table + "_reproject";
            Projection projection = geoPackage.getProjection(table);
            Projection reprojectProjection = alternateProjection(projection);

            TileDao tileDao = geoPackage.getTileDao(table);
            int count = tileDao.count();
            Map<Long, Integer> counts = zoomCounts(tileDao);

            int tiles = TileReprojection.reproject(geoPackage, table,
                    reprojectTable, reprojectProjection);

            assertEquals(count > 0, tiles > 0);

            assertTrue(projection.equals(geoPackage.getProjection(table)));
            assertTrue(reprojectProjection
                    .equals(geoPackage.getProjection(reprojectTable)));

            tileDao = geoPackage.getTileDao(table);
            compareZoomCounts(count, counts, tileDao);

            TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
            checkZoomCounts(count, counts, reprojectTileDao, tiles);

            int tiles2 = TileReprojection.reproject(geoPackage, table,
                    reprojectTable, reprojectProjection);

            assertEquals(tiles, tiles2);

            TileMatrixSet tileMatrixSet = reprojectTileDao.getTileMatrixSet();
            double multiplier = 0.5;
            tileMatrixSet.setMinX(tileMatrixSet.getMinX() * multiplier);
            tileMatrixSet.setMinY(tileMatrixSet.getMinY() * multiplier);
            tileMatrixSet.setMaxX(tileMatrixSet.getMaxX() * multiplier);
            tileMatrixSet.setMaxY(tileMatrixSet.getMaxY() * multiplier);
            reprojectTileDao.getTileMatrixSetDao().update(tileMatrixSet);

            TileReprojection tileReprojection = TileReprojection.create(
                    geoPackage, table, reprojectTable, reprojectProjection);

            try {
                tileReprojection.reproject();
                fail("Reprojection of existing table with new geographic properties did not fail");
            } catch (Exception e) {
                // expected
            }

            tileReprojection.setOverwrite(true);
            tileReprojection.reproject();

            tileMatrixSet.setMinX(tileMatrixSet.getMinX() / multiplier);
            tileMatrixSet.setMinY(tileMatrixSet.getMinY() / multiplier);
            tileMatrixSet.setMaxX(tileMatrixSet.getMaxX() / multiplier);
            tileMatrixSet.setMaxY(tileMatrixSet.getMaxY() / multiplier);
            reprojectTileDao.getTileMatrixSetDao().update(tileMatrixSet);

            tileReprojection = TileReprojection.create(geoPackage, table,
                    reprojectTable, reprojectProjection);

            try {
                tileReprojection.reproject();
                fail("Reprojection of existing table with new geographic properties did not fail");
            } catch (Exception e) {
                // expected
            }

            tileReprojection.setOverwrite(true);
            TestGeoPackageProgress progress = new TestGeoPackageProgress();
            tileReprojection.setProgress(progress);
            int tiles3 = tileReprojection.reproject();
            assertEquals(tiles3, progress.getProgress());
            assertEquals(tiles, tiles3);

            Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
            Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
            zoomLevels.removeAll(reprojectZoomLevels);
            assertEquals(0, zoomLevels.size());

            compareBoundingBox(
                    geoPackage.getBoundingBox(reprojectProjection, table),
                    geoPackage.getContentsBoundingBox(reprojectTable),
                    .0000001);
        }

    }

    /**
     * Test reproject with zoom level mappings
     *
     * @param geoPackage GeoPackage
     */
    public static void testReprojectToZoom(GeoPackage geoPackage) {

        for (String table : randomTileTables(geoPackage)) {

            String reprojectTable = table + "_reproject";
            Projection projection = geoPackage.getProjection(table);
            Projection reprojectProjection = alternateProjection(projection);

            TileDao tileDao = geoPackage.getTileDao(table);
            int count = tileDao.count();
            Map<Long, Integer> counts = zoomCounts(tileDao);

            TileReprojection tileReprojection = TileReprojection.create(
                    geoPackage, table, reprojectTable, reprojectProjection);

            Map<Long, Long> zoomMap = new HashMap<>();

            List<Long> zoomLevels = new ArrayList<>(tileDao.getZoomLevels());
            long fromZoom = zoomLevels.get(0);
            long toZoom = Math.max(fromZoom - 2, 0);
            tileReprojection.setToZoom(fromZoom, toZoom);
            zoomMap.put(toZoom, fromZoom);

            for (int i = 1; i < zoomLevels.size(); i++) {
                fromZoom = zoomLevels.get(i);
                toZoom += 2;
                tileReprojection.setToZoom(fromZoom, toZoom);
                zoomMap.put(toZoom, fromZoom);
            }

            int tiles = tileReprojection.reproject();

            assertEquals(count > 0, tiles > 0);

            assertTrue(projection.equals(geoPackage.getProjection(table)));
            assertTrue(reprojectProjection
                    .equals(geoPackage.getProjection(reprojectTable)));

            tileDao = geoPackage.getTileDao(table);
            compareZoomCounts(count, counts, tileDao);

            TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
            assertEquals(count > 0, reprojectTileDao.count() > 0);
            assertEquals(tiles, reprojectTileDao.count());
            Map<Long, Integer> countsAfter = zoomCounts(reprojectTileDao);
            assertEquals(counts.size(), countsAfter.size());
            for (long toZoomLevel : reprojectTileDao.getZoomLevels()) {
                long fromZoomLevel = zoomMap.get(toZoomLevel);
                assertEquals(counts.get(fromZoomLevel) > 0,
                        countsAfter.get(toZoomLevel) > 0);
            }

            List<Long> fromZoomLevels = new ArrayList<>(
                    tileDao.getZoomLevels());
            List<Long> reprojectZoomLevels = new ArrayList<>(
                    reprojectTileDao.getZoomLevels());
            assertEquals(fromZoomLevels.size(), reprojectZoomLevels.size());
            fromZoomLevels.removeAll(zoomMap.values());
            reprojectZoomLevels.removeAll(zoomMap.keySet());
            assertEquals(0, fromZoomLevels.size());
            assertEquals(0, reprojectZoomLevels.size());

            compareBoundingBox(
                    geoPackage.getBoundingBox(reprojectProjection, table),
                    geoPackage.getContentsBoundingBox(reprojectTable),
                    .0000001);
        }

    }

    /**
     * Test reproject with zoom level matrix and tile length configurations
     *
     * @param geoPackage GeoPackage
     * @throws IOException upon error
     */
    public static void testReprojectMatrixAndTileLengths(GeoPackage geoPackage)
            throws IOException {

        for (String table : randomTileTables(geoPackage)) {

            String reprojectTable = table + "_reproject";
            Projection projection = geoPackage.getProjection(table);
            Projection reprojectProjection = alternateProjection(projection);

            TileDao tileDao = geoPackage.getTileDao(table);
            int count = tileDao.count();
            Map<Long, Integer> counts = zoomCounts(tileDao);

            TileReprojection tileReprojection = TileReprojection.create(
                    geoPackage, table, reprojectTable, reprojectProjection);

            for (TileMatrix tileMatrix : tileDao.getTileMatrices()) {
                long zoom = tileMatrix.getZoomLevel();
                tileReprojection.setMatrixWidth(zoom,
                        tileMatrix.getMatrixWidth() * 2);
                tileReprojection.setMatrixHeight(zoom,
                        tileMatrix.getMatrixHeight() * 2);
                tileReprojection.setTileWidth(zoom,
                        tileMatrix.getTileWidth() + zoom);
                tileReprojection.setTileHeight(zoom,
                        tileMatrix.getTileHeight() + zoom);
            }

            int tiles = tileReprojection.reproject();

            assertEquals(count > 0, tiles > 0);

            assertTrue(projection.equals(geoPackage.getProjection(table)));
            assertTrue(reprojectProjection
                    .equals(geoPackage.getProjection(reprojectTable)));

            tileDao = geoPackage.getTileDao(table);
            compareZoomCounts(count, counts, tileDao);

            TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
            assertEquals(count > 0, reprojectTileDao.count() > 0);
            assertEquals(tiles, reprojectTileDao.count());
            Map<Long, Integer> countsAfter = zoomCounts(reprojectTileDao);
            assertEquals(counts.size(), countsAfter.size());

            List<Long> zoomLevels = new ArrayList<>(tileDao.getZoomLevels());
            zoomLevels.removeAll(reprojectTileDao.getZoomLevels());
            assertEquals(0, zoomLevels.size());

            for (TileMatrix reprojectTileMatrix : reprojectTileDao
                    .getTileMatrices()) {
                long zoom = reprojectTileMatrix.getZoomLevel();
                TileMatrix tileMatrix = tileDao.getTileMatrix(zoom);
                assertEquals(tileMatrix.getMatrixWidth() * 2,
                        reprojectTileMatrix.getMatrixWidth());
                assertEquals(tileMatrix.getMatrixHeight() * 2,
                        reprojectTileMatrix.getMatrixHeight());
                assertEquals(tileMatrix.getTileWidth() + zoom,
                        reprojectTileMatrix.getTileWidth());
                assertEquals(tileMatrix.getTileHeight() + zoom,
                        reprojectTileMatrix.getTileHeight());

                TileCursor tileCursor = reprojectTileDao.queryForTile(zoom);
                assertTrue(tileCursor.moveToNext());
                TileRow tileRow = tileCursor.getRow();
                Bitmap tileImage = tileRow.getTileDataBitmap();
                assertEquals(reprojectTileMatrix.getTileWidth(),
                        tileImage.getWidth());
                assertEquals(reprojectTileMatrix.getTileHeight(),
                        tileImage.getHeight());
                tileCursor.close();

                TileGrid tileGrid = tileDao.queryForTileGrid(zoom);
                TileGrid reprojectTileGrid = reprojectTileDao
                        .queryForTileGrid(zoom);
                assertTrue(tileGrid.getMaxX() <= reprojectTileGrid.getMaxX());
                assertTrue(tileGrid.getMaxY() <= reprojectTileGrid.getMaxY());
            }

            compareBoundingBox(
                    geoPackage.getBoundingBox(reprojectProjection, table),
                    geoPackage.getContentsBoundingBox(reprojectTable),
                    .0000001);
        }

    }

    /**
     * Test reproject with tile optimization
     *
     * @param geoPackage GeoPackage
     * @param world      world bounds
     */
    public static void testReprojectOptimize(GeoPackage geoPackage,
                                             boolean world) {

        for (String table : randomTileTables(geoPackage)) {

            String reprojectTable = table + "_reproject";
            Projection projection = geoPackage.getProjection(table);
            Projection reprojectProjection = alternateProjection(projection);

            TileDao tileDao = geoPackage.getTileDao(table);
            int count = tileDao.count();
            Map<Long, Integer> counts = zoomCounts(tileDao);

            TileReprojection tileReprojection = TileReprojection.create(
                    geoPackage, table, reprojectTable, reprojectProjection);

            TileReprojectionOptimize optimize = null;
            boolean webMercator = reprojectProjection.equals(
                    ProjectionConstants.AUTHORITY_EPSG,
                    ProjectionConstants.EPSG_WEB_MERCATOR);
            if (webMercator) {
                if (world) {
                    optimize = WebMercatorOptimize.createWorld();
                } else {
                    optimize = WebMercatorOptimize.create();
                }
            } else {
                if (world) {
                    optimize = PlatteCarreOptimize.createWorld();
                } else {
                    optimize = PlatteCarreOptimize.create();
                }
            }
            tileReprojection.setOptimize(optimize);

            int tiles = tileReprojection.reproject();

            assertEquals(count > 0, tiles > 0);

            assertTrue(projection.equals(geoPackage.getProjection(table)));
            assertTrue(reprojectProjection
                    .equals(geoPackage.getProjection(reprojectTable)));

            tileDao = geoPackage.getTileDao(table);
            compareZoomCounts(count, counts, tileDao);

            TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);

            assertEquals(count > 0, reprojectTileDao.count() > 0);
            assertEquals(tiles, reprojectTileDao.count());
            Map<Long, Integer> countsAfter = zoomCounts(reprojectTileDao);
            assertEquals(counts.size(), countsAfter.size());
            assertEquals(tileDao.getZoomLevels().size(),
                    reprojectTileDao.getZoomLevels().size());
            for (int i = 0; i < tileDao.getZoomLevels().size(); i++) {
                long zoomLevel = new ArrayList<>(tileDao.getZoomLevels())
                        .get(i);
                long toZoomLevel = new ArrayList<>(
                        reprojectTileDao.getZoomLevels()).get(i);
                assertEquals(counts.get(zoomLevel) > 0,
                        countsAfter.get(toZoomLevel) > 0);
            }

            for (long zoom : reprojectTileDao.getZoomLevels()) {
                TileMatrix tileMatrix = reprojectTileDao.getTileMatrix(zoom);
                BoundingBox boundingBox = reprojectTileDao.getBoundingBox();
                TileGrid zoomTileGrid = reprojectTileDao.getTileGrid(zoom);
                TileGrid tileGrid = reprojectTileDao.queryForTileGrid(zoom);
                BoundingBox tilesBoundingBox = TileBoundingBoxUtils
                        .getBoundingBox(boundingBox, tileMatrix, tileGrid);
                assertTrue(tileGrid.getMinX() >= zoomTileGrid.getMinX());
                assertTrue(tileGrid.getMaxX() <= zoomTileGrid.getMaxX());
                assertTrue(tileGrid.getMinY() >= zoomTileGrid.getMinY());
                assertTrue(tileGrid.getMaxY() <= zoomTileGrid.getMaxY());
                TileCursor tileCursor = reprojectTileDao.queryForTile(zoom);
                int tileIndex = (int) (Math.random() * tileCursor.getCount());
                for (int i = 0; i <= tileIndex; i++) {
                    assertTrue(tileCursor.moveToNext());
                }
                TileRow tile = tileCursor.getRow();
                long tileColumn = tile.getTileColumn();
                long tileRow = tile.getTileRow();
                BoundingBox tileBoundingBox = TileBoundingBoxUtils
                        .getBoundingBox(boundingBox, tileMatrix, tileColumn,
                                tileRow);
                tileCursor.close();

                BoundingBox optimizeBoundingBox = tileBoundingBox;
                TileGrid optimizeTileGrid = null;
                TileGrid localTileGrid = null;
                projection = reprojectTileDao.getProjection();

                if (webMercator) {
                    ProjectionTransform transform = projection
                            .getTransformation(
                                    ProjectionConstants.EPSG_WEB_MERCATOR);
                    if (!transform.isSameProjection()) {
                        optimizeBoundingBox = optimizeBoundingBox
                                .transform(transform);
                    }
                    double midLongitude = optimizeBoundingBox.getMinLongitude()
                            + (optimizeBoundingBox.getLongitudeRange() / 2.0);
                    double midLatitude = optimizeBoundingBox.getMinLatitude()
                            + (optimizeBoundingBox.getLatitudeRange() / 2.0);
                    BoundingBox optimizeBoundingBoxPoint = new BoundingBox(
                            midLongitude, midLatitude, midLongitude,
                            midLatitude);
                    optimizeTileGrid = TileBoundingBoxUtils
                            .getTileGrid(optimizeBoundingBoxPoint, zoom);
                    localTileGrid = TileBoundingBoxUtils.getTileGrid(
                            boundingBox, tileMatrix.getMatrixWidth(),
                            tileMatrix.getMatrixHeight(),
                            optimizeBoundingBoxPoint);
                    BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                            .getWebMercatorBoundingBox(optimizeTileGrid, zoom);
                    compareBoundingBox(optimizeBoundingBox,
                            webMercatorBoundingBox, tileMatrix);
                    optimizeBoundingBox = webMercatorBoundingBox;
                    if (world) {
                        TileGrid worldTileGrid = TileBoundingBoxUtils
                                .getTileGrid(boundingBox, zoom);
                        assertEquals(zoomTileGrid.getMinX(),
                                worldTileGrid.getMinX());
                        assertEquals(zoomTileGrid.getMaxX(),
                                worldTileGrid.getMaxX());
                        assertEquals(zoomTileGrid.getMinY(),
                                worldTileGrid.getMinY());
                        assertEquals(zoomTileGrid.getMaxY(),
                                worldTileGrid.getMaxY());
                        TileGrid worldTilesTileGrid = TileBoundingBoxUtils
                                .getTileGrid(tilesBoundingBox, zoom);
                        assertTrue(tileGrid.getMinX() == worldTilesTileGrid
                                .getMinX()
                                || tileGrid.getMinX() - 1 == worldTilesTileGrid
                                .getMinX());
                        assertTrue(tileGrid.getMaxX() == worldTilesTileGrid
                                .getMaxX()
                                || tileGrid.getMaxX() + 1 == worldTilesTileGrid
                                .getMaxX());
                        assertTrue(tileGrid.getMinY() == worldTilesTileGrid
                                .getMinY()
                                || tileGrid.getMinY() - 1 == worldTilesTileGrid
                                .getMinY());
                        assertTrue(tileGrid.getMaxY() == worldTilesTileGrid
                                .getMaxY()
                                || tileGrid.getMaxY() + 1 == worldTilesTileGrid
                                .getMaxY());
                    }
                    if (!transform.isSameProjection()) {
                        optimizeBoundingBox = optimizeBoundingBox
                                .transform(transform);
                    }
                } else {
                    ProjectionTransform transform = projection
                            .getTransformation(
                                    ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                    if (!transform.isSameProjection()) {
                        optimizeBoundingBox = optimizeBoundingBox
                                .transform(transform);
                    }
                    double midLongitude = optimizeBoundingBox.getMinLongitude()
                            + (optimizeBoundingBox.getLongitudeRange() / 2.0);
                    double midLatitude = optimizeBoundingBox.getMinLatitude()
                            + (optimizeBoundingBox.getLatitudeRange() / 2.0);
                    BoundingBox optimizeBoundingBoxPoint = new BoundingBox(
                            midLongitude, midLatitude, midLongitude,
                            midLatitude);
                    optimizeTileGrid = TileBoundingBoxUtils
                            .getTileGridWGS84(optimizeBoundingBoxPoint, zoom);
                    localTileGrid = TileBoundingBoxUtils.getTileGrid(
                            boundingBox, tileMatrix.getMatrixWidth(),
                            tileMatrix.getMatrixHeight(),
                            optimizeBoundingBoxPoint);
                    BoundingBox wgs84BoundingBox = TileBoundingBoxUtils
                            .getWGS84BoundingBox(optimizeTileGrid, zoom);
                    compareBoundingBox(optimizeBoundingBox, wgs84BoundingBox,
                            tileMatrix);
                    optimizeBoundingBox = wgs84BoundingBox;
                    if (world) {
                        TileGrid worldTileGrid = TileBoundingBoxUtils
                                .getTileGridWGS84(boundingBox, zoom);
                        assertEquals(zoomTileGrid.getMinX(),
                                worldTileGrid.getMinX());
                        assertEquals(zoomTileGrid.getMaxX(),
                                worldTileGrid.getMaxX());
                        assertEquals(zoomTileGrid.getMinY(),
                                worldTileGrid.getMinY());
                        assertEquals(zoomTileGrid.getMaxY(),
                                worldTileGrid.getMaxY());
                        TileGrid worldTilesTileGrid = TileBoundingBoxUtils
                                .getTileGridWGS84(tilesBoundingBox, zoom);
                        assertTrue(tileGrid.getMinX() == worldTilesTileGrid
                                .getMinX()
                                || tileGrid.getMinX() - 1 == worldTilesTileGrid
                                .getMinX());
                        assertTrue(tileGrid.getMaxX() == worldTilesTileGrid
                                .getMaxX()
                                || tileGrid.getMaxX() + 1 == worldTilesTileGrid
                                .getMaxX());
                        assertTrue(tileGrid.getMinY() == worldTilesTileGrid
                                .getMinY()
                                || tileGrid.getMinY() - 1 == worldTilesTileGrid
                                .getMinY());
                        assertTrue(tileGrid.getMaxY() == worldTilesTileGrid
                                .getMaxY()
                                || tileGrid.getMaxY() + 1 == worldTilesTileGrid
                                .getMaxY());
                    }
                    if (!transform.isSameProjection()) {
                        optimizeBoundingBox = optimizeBoundingBox
                                .transform(transform);
                    }
                }

                assertNotNull(optimizeTileGrid);
                assertEquals(1, optimizeTileGrid.getWidth());
                assertEquals(1, optimizeTileGrid.getHeight());
                assertNotNull(localTileGrid);
                assertEquals(tileColumn, localTileGrid.getMinX());
                assertEquals(tileRow, localTileGrid.getMinY());
                compareBoundingBox(tileBoundingBox, optimizeBoundingBox,
                        tileMatrix);
            }

            compareBoundingBox(
                    geoPackage.getBoundingBox(reprojectProjection, table),
                    geoPackage.getContentsBoundingBox(reprojectTable),
                    .0000001);
        }

    }

    /**
     * Test reproject with web mercator tile optimization
     *
     * @param geoPackage GeoPackage
     * @param world      world bounds
     */
    public static void testReprojectWebMercator(GeoPackage geoPackage,
                                                boolean world) {

        for (String table : randomTileTables(geoPackage)) {
            TileDao tileDao = geoPackage.getTileDao(table);
            int count = tileDao.count();
            String reprojectTable = table + "_reproject";
            TileReprojectionOptimize optimize = null;
            if (world) {
                optimize = TileReprojectionOptimize.webMercatorWorld();
            } else {
                optimize = TileReprojectionOptimize.webMercator();
            }
            int tiles = TileReprojection.reproject(geoPackage, table,
                    reprojectTable, optimize);
            assertEquals(count > 0, tiles > 0);
            assertTrue(ProjectionFactory
                    .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR)
                    .equals(geoPackage.getProjection(reprojectTable)));
            TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
            for (long zoom : reprojectTileDao.getZoomLevels()) {
                TileMatrix tileMatrix = reprojectTileDao.getTileMatrix(zoom);
                BoundingBox boundingBox = reprojectTileDao.getBoundingBox();
                BoundingBox tileBoundingBox = TileBoundingBoxUtils
                        .getBoundingBox(boundingBox, tileMatrix, 0, 0);
                double midLongitude = tileBoundingBox.getMinLongitude()
                        + (tileBoundingBox.getLongitudeRange() / 2.0);
                double midLatitude = tileBoundingBox.getMinLatitude()
                        + (tileBoundingBox.getLatitudeRange() / 2.0);
                BoundingBox boundingBoxPoint = new BoundingBox(midLongitude,
                        midLatitude, midLongitude, midLatitude);
                TileGrid tileGrid = TileBoundingBoxUtils
                        .getTileGrid(boundingBoxPoint, zoom);
                BoundingBox tileGridBoundingBox = TileBoundingBoxUtils
                        .getWebMercatorBoundingBox(tileGrid, zoom);
                compareBoundingBox(tileBoundingBox, tileGridBoundingBox,
                        tileMatrix);
                if (world) {
                    TileGrid zoomTileGrid = reprojectTileDao.getTileGrid(zoom);
                    TileGrid worldTileGrid = TileBoundingBoxUtils
                            .getTileGrid(boundingBox, zoom);
                    assertEquals(zoomTileGrid.getMinX(),
                            worldTileGrid.getMinX());
                    assertEquals(zoomTileGrid.getMaxX(),
                            worldTileGrid.getMaxX());
                    assertEquals(zoomTileGrid.getMinY(),
                            worldTileGrid.getMinY());
                    assertEquals(zoomTileGrid.getMaxY(),
                            worldTileGrid.getMaxY());
                    BoundingBox tilesBoundingBox = TileBoundingBoxUtils
                            .getBoundingBox(boundingBox, tileMatrix, tileGrid);
                    TileGrid worldTilesTileGrid = TileBoundingBoxUtils
                            .getTileGrid(tilesBoundingBox, zoom);
                    assertTrue(tileGrid.getMinX() == worldTilesTileGrid
                            .getMinX()
                            || tileGrid.getMinX() - 1 == worldTilesTileGrid
                            .getMinX());
                    assertTrue(tileGrid.getMaxX() == worldTilesTileGrid
                            .getMaxX()
                            || tileGrid.getMaxX() + 1 == worldTilesTileGrid
                            .getMaxX());
                    assertTrue(tileGrid.getMinY() == worldTilesTileGrid
                            .getMinY()
                            || tileGrid.getMinY() - 1 == worldTilesTileGrid
                            .getMinY());
                    assertTrue(tileGrid.getMaxY() == worldTilesTileGrid
                            .getMaxY()
                            || tileGrid.getMaxY() + 1 == worldTilesTileGrid
                            .getMaxY());
                }
            }
        }

    }

    /**
     * Test reproject with platte carre tile optimization
     *
     * @param geoPackage GeoPackage
     * @param world      world bounds
     */
    public static void testReprojectPlatteCarre(GeoPackage geoPackage,
                                                boolean world) {

        for (String table : randomTileTables(geoPackage)) {
            TileDao tileDao = geoPackage.getTileDao(table);
            int count = tileDao.count();
            String reprojectTable = table + "_reproject";
            TileReprojectionOptimize optimize = null;
            if (world) {
                optimize = TileReprojectionOptimize.platteCarreWorld();
            } else {
                optimize = TileReprojectionOptimize.platteCarre();
            }
            int tiles = TileReprojection.reproject(geoPackage, table,
                    reprojectTable, optimize);
            assertEquals(count > 0, tiles > 0);
            assertTrue(ProjectionFactory
                    .getProjection(
                            ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                    .equals(geoPackage.getProjection(reprojectTable)));
            TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
            for (long zoom : reprojectTileDao.getZoomLevels()) {
                TileMatrix tileMatrix = reprojectTileDao.getTileMatrix(zoom);
                BoundingBox boundingBox = reprojectTileDao.getBoundingBox();
                BoundingBox tileBoundingBox = TileBoundingBoxUtils
                        .getBoundingBox(boundingBox, tileMatrix, 0, 0);
                double midLongitude = tileBoundingBox.getMinLongitude()
                        + (tileBoundingBox.getLongitudeRange() / 2.0);
                double midLatitude = tileBoundingBox.getMinLatitude()
                        + (tileBoundingBox.getLatitudeRange() / 2.0);
                BoundingBox boundingBoxPoint = new BoundingBox(midLongitude,
                        midLatitude, midLongitude, midLatitude);
                TileGrid tileGrid = TileBoundingBoxUtils
                        .getTileGridWGS84(boundingBoxPoint, zoom);
                BoundingBox tileGridBoundingBox = TileBoundingBoxUtils
                        .getWGS84BoundingBox(tileGrid, zoom);
                compareBoundingBox(tileBoundingBox, tileGridBoundingBox,
                        tileMatrix);
                if (world) {
                    TileGrid zoomTileGrid = reprojectTileDao.getTileGrid(zoom);
                    TileGrid worldTileGrid = TileBoundingBoxUtils
                            .getTileGridWGS84(boundingBox, zoom);
                    assertEquals(zoomTileGrid.getMinX(),
                            worldTileGrid.getMinX());
                    assertEquals(zoomTileGrid.getMaxX(),
                            worldTileGrid.getMaxX());
                    assertEquals(zoomTileGrid.getMinY(),
                            worldTileGrid.getMinY());
                    assertEquals(zoomTileGrid.getMaxY(),
                            worldTileGrid.getMaxY());
                    BoundingBox tilesBoundingBox = TileBoundingBoxUtils
                            .getBoundingBox(boundingBox, tileMatrix, tileGrid);
                    TileGrid worldTilesTileGrid = TileBoundingBoxUtils
                            .getTileGridWGS84(tilesBoundingBox, zoom);
                    assertTrue(tileGrid.getMinX() == worldTilesTileGrid
                            .getMinX()
                            || tileGrid.getMinX() - 1 == worldTilesTileGrid
                            .getMinX());
                    assertTrue(tileGrid.getMaxX() == worldTilesTileGrid
                            .getMaxX()
                            || tileGrid.getMaxX() + 1 == worldTilesTileGrid
                            .getMaxX());
                    assertTrue(tileGrid.getMinY() == worldTilesTileGrid
                            .getMinY()
                            || tileGrid.getMinY() - 1 == worldTilesTileGrid
                            .getMinY());
                    assertTrue(tileGrid.getMaxY() == worldTilesTileGrid
                            .getMaxY()
                            || tileGrid.getMaxY() + 1 == worldTilesTileGrid
                            .getMaxY());
                }
            }
        }

    }

    /**
     * Test reproject cancel
     *
     * @param geoPackage GeoPackage
     */
    public static void testReprojectCancel(GeoPackage geoPackage) {

        String table = randomTileTables(geoPackage).get(0);

        String reprojectTable = table + "_reproject";
        Projection projection = geoPackage.getProjection(table);
        Projection reprojectProjection = alternateProjection(projection);

        TileReprojection tileReprojection = TileReprojection.create(geoPackage,
                table, reprojectTable, reprojectProjection);

        TestGeoPackageProgress progress = new TestGeoPackageProgress();
        tileReprojection.setProgress(progress);
        progress.cancel();

        int tiles = tileReprojection.reproject();

        assertEquals(0, tiles);
        assertEquals(0, progress.getProgress());

    }

    private static void compareBoundingBox(BoundingBox boundingBox1,
                                           BoundingBox boundingBox2, TileMatrix tileMatrix) {
        double longitudeDelta = tileMatrix.getPixelXSize();
        double latitudeDelta = tileMatrix.getPixelYSize();
        compareBoundingBox(boundingBox1, boundingBox2, longitudeDelta,
                latitudeDelta);
    }

    private static void compareBoundingBox(BoundingBox boundingBox1,
                                           BoundingBox boundingBox2, double delta) {
        compareBoundingBox(boundingBox1, boundingBox2, delta, delta);
    }

    private static void compareBoundingBox(BoundingBox boundingBox1,
                                           BoundingBox boundingBox2, double longitudeDelta,
                                           double latitudeDelta) {
        assertEquals(boundingBox1.getMinLongitude(),
                boundingBox2.getMinLongitude(), longitudeDelta);
        assertEquals(boundingBox1.getMinLatitude(),
                boundingBox2.getMinLatitude(), latitudeDelta);
        assertEquals(boundingBox1.getMaxLongitude(),
                boundingBox2.getMaxLongitude(), longitudeDelta);
        assertEquals(boundingBox1.getMaxLatitude(),
                boundingBox2.getMaxLatitude(), latitudeDelta);
    }

    private static List<String> randomTileTables(GeoPackage geoPackage) {
        List<String> tileTables = null;
        List<String> allTileTables = geoPackage.getTileTables();
        allTileTables.remove("webp_tiles");
        int count = allTileTables.size();
        if (count <= 2) {
            tileTables = allTileTables;
        } else {
            int index1 = (int) (Math.random() * count);
            int index2 = (int) (Math.random() * count);
            if (index1 == index2) {
                if (++index2 >= count) {
                    index2 = 0;
                }
            }
            tileTables = new ArrayList<>();
            tileTables.add(allTileTables.get(index1));
            tileTables.add(allTileTables.get(index2));
        }
        return tileTables;
    }

    private static Projection alternateProjection(Projection projection) {
        Projection alternate = null;
        if (projection.equals(ProjectionConstants.AUTHORITY_EPSG,
                ProjectionConstants.EPSG_WEB_MERCATOR)) {
            alternate = ProjectionFactory.getProjection(
                    ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        } else {
            alternate = ProjectionFactory
                    .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        }
        return alternate;
    }

    private static Map<Long, Integer> zoomCounts(TileDao tileDao) {
        Map<Long, Integer> counts = new HashMap<>();
        for (long zoom : tileDao.getZoomLevels()) {
            int zoomCount = tileDao.count(zoom);
            counts.put(zoom, zoomCount);
        }
        return counts;
    }

    private static void compareZoomCounts(int count, Map<Long, Integer> counts,
                                          TileDao tileDao) {
        assertEquals(count, tileDao.count());
        Map<Long, Integer> countsAfter = zoomCounts(tileDao);
        assertEquals(counts.size(), countsAfter.size());
        for (long zoom : tileDao.getZoomLevels()) {
            assertEquals(counts.get(zoom), countsAfter.get(zoom));
        }
    }

    private static void checkZoomCounts(int count, Map<Long, Integer> counts,
                                        TileDao tileDao, int tiles) {
        assertEquals(count > 0, tileDao.count() > 0);
        assertEquals(tiles, tileDao.count());
        Map<Long, Integer> countsAfter = zoomCounts(tileDao);
        assertEquals(counts.size(), countsAfter.size());
        for (long zoom : tileDao.getZoomLevels()) {
            assertEquals(counts.get(zoom) > 0, countsAfter.get(zoom) > 0);
        }
    }

}
