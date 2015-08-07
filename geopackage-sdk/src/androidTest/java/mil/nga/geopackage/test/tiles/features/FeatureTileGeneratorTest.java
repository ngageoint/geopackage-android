package mil.nga.geopackage.test.tiles.features;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTiles;

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

        int minZoom = 0;
        int maxZoom = 4;

        FeatureDao featureDao = FeatureTileUtils.createFeatureDao(geoPackage);

        FeatureTileUtils.insertFeatures(geoPackage, featureDao);

        FeatureTiles featureTiles = FeatureTileUtils.createFeatureTiles(activity, geoPackage, featureDao);

        TileGenerator tileGenerator = new FeatureTileGenerator(activity, geoPackage,
                "gen_feature_tiles", featureTiles, minZoom, maxZoom);
        //tileGenerator.setTileBoundingBox(boundingBox);
        tileGenerator.setGoogleTiles(false);

        int tiles = tileGenerator.generateTiles();

        int expectedTiles = 0;
        for (int i = minZoom; i <= maxZoom; i++) {
            int tilesPerSide = TileBoundingBoxUtils.tilesPerSide(i);
            expectedTiles += (tilesPerSide * tilesPerSide);
        }

        assertEquals(expectedTiles, tiles);

    }

}
