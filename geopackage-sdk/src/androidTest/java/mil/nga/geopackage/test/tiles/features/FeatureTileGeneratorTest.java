package mil.nga.geopackage.test.tiles.features;

import android.graphics.Bitmap;

import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;

import static org.junit.Assert.assertEquals;

/**
 * Test GeoPackage Feature Tile Generator
 *
 * @author osbornb
 */
public class FeatureTileGeneratorTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureTileGeneratorTest() {

    }

    /**
     * Test tile generator
     *
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    @Test
    public void testTileGenerator() throws IOException, SQLException {
        testTileGenerator(false, false, false);
    }

    /**
     * Test tile generator
     *
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    @Test
    public void testTileGeneratorWithIndex() throws IOException, SQLException {
        testTileGenerator(true, false, false);
    }

    /**
     * Test tile generator
     *
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    @Test
    public void testTileGeneratorWithIcon() throws IOException, SQLException {
        testTileGenerator(false, true, false);
    }

    /**
     * Test tile generator
     *
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    @Test
    public void testTileGeneratorWithMaxFeatures() throws IOException,
            SQLException {
        testTileGenerator(false, false, true);
    }

    /**
     * Test tile generator
     *
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    @Test
    public void testTileGeneratorWithIndexAndIcon() throws IOException,
            SQLException {
        testTileGenerator(true, true, false);
    }

    /**
     * Test tile generator
     *
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    @Test
    public void testTileGeneratorWithIndexAndIconAndMaxFeatures()
            throws IOException, SQLException {
        testTileGenerator(true, true, true);
    }

    /**
     * Test tile generator
     *
     * @param index
     * @param useIcon
     * @param maxFeatures
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public void testTileGenerator(boolean index, boolean useIcon,
                                  boolean maxFeatures) throws IOException, SQLException {

        int minZoom = 0;
        int maxZoom = 4;

        FeatureDao featureDao = FeatureTileUtils.createFeatureDao(geoPackage);

        int num = FeatureTileUtils.insertFeatures(geoPackage, featureDao);

        FeatureTiles featureTiles = FeatureTileUtils.createFeatureTiles(
                activity, geoPackage, featureDao, useIcon);
        try {

            if (index) {
                FeatureIndexManager indexManager = new FeatureIndexManager(activity, geoPackage, featureDao);
                featureTiles.setIndexManager(indexManager);
                indexManager.setIndexLocation(FeatureIndexType.GEOPACKAGE);
                int indexed = indexManager.index();
                assertEquals(num, indexed);
            }

            if (maxFeatures) {
                featureTiles.setMaxFeaturesPerTile(10);
                NumberFeaturesTile numberFeaturesTile = new NumberFeaturesTile(activity);
                if (!index) {
                    numberFeaturesTile.setDrawUnindexedTiles(false);
                }
                featureTiles.setMaxFeaturesTileDraw(numberFeaturesTile);
            }

            TileGenerator tileGenerator = new FeatureTileGenerator(activity, geoPackage,
                    "gen_feature_tiles", featureTiles, minZoom, maxZoom,
                    ProjectionFactory
                            .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR));
            tileGenerator.setGoogleTiles(false);

            int tiles = tileGenerator.generateTiles();

            int expectedTiles = 0;
            if (!maxFeatures || index) {

                if (!index) {
                    FeatureIndexManager indexManager = new FeatureIndexManager(activity, geoPackage, featureDao);
                    featureTiles.setIndexManager(indexManager);
                    indexManager.setIndexLocation(FeatureIndexType.GEOPACKAGE);
                    int indexed = indexManager.index();
                    assertEquals(num, indexed);
                }

                for (int z = minZoom; z <= maxZoom; z++) {

                    TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
                            tileGenerator.getBoundingBox(z), z);

                    for (long x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {
                        for (long y = tileGrid.getMinY(); y <= tileGrid.getMaxY(); y++) {
                            if (featureTiles.queryIndexedFeaturesCount((int) x,
                                    (int) y, z) > 0) {

                                BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                                        .getWebMercatorBoundingBox(x, y, z);
                                FeatureIndexResults results = featureTiles
                                        .queryIndexedFeatures((int) x, (int) y, z);
                                Bitmap image = featureTiles.drawTile(z,
                                        webMercatorBoundingBox, results);
                                if (image != null) {
                                    expectedTiles++;
                                }

                            }
                        }
                    }
                }

            }

            assertEquals(expectedTiles, tiles);

        } finally {
            featureTiles.close();
        }
    }

}
