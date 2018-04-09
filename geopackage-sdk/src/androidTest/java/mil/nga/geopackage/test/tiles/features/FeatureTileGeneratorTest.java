package mil.nga.geopackage.test.tiles.features;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;

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
    public void testTileGenerator() throws IOException, SQLException {
        testTileGenerator(false, false, false);
    }

    /**
     * Test tile generator
     *
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public void testTileGeneratorWithIndex() throws IOException, SQLException {
        testTileGenerator(true, false, false);
    }

    /**
     * Test tile generator
     *
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public void testTileGeneratorWithIcon() throws IOException, SQLException {
        testTileGenerator(false, true, false);
    }

    /**
     * Test tile generator
     *
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
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

            BoundingBox boundingBox = new BoundingBox();
            boundingBox = TileBoundingBoxUtils
                    .boundWgs84BoundingBoxWithWebMercatorLimits(boundingBox);
            boundingBox = boundingBox
                    .transform(ProjectionFactory
                            .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
                            .getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR));
            TileGenerator tileGenerator = new FeatureTileGenerator(activity, geoPackage,
                    "gen_feature_tiles", featureTiles, minZoom, maxZoom,
                    boundingBox,
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
                    int tilesPerSide = TileBoundingBoxUtils.tilesPerSide(z);
                    for (int x = 0; x < tilesPerSide; x++) {
                        for (int y = 0; y < tilesPerSide; y++) {
                            if (featureTiles.queryIndexedFeaturesCount(x, y, z) > 0) {
                                expectedTiles++;
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
