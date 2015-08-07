package mil.nga.geopackage.test.tiles.features;

import android.graphics.Bitmap;

import java.sql.SQLException;

import mil.nga.geopackage.db.FeatureIndexer;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.features.FeatureTiles;

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

        FeatureDao featureDao = FeatureTileUtils.createFeatureDao(geoPackage);

        FeatureTileUtils.insertFeatures(geoPackage, featureDao);

        FeatureTiles featureTiles = FeatureTileUtils.createFeatureTiles(activity, geoPackage, featureDao);

        FeatureIndexer indexer = new FeatureIndexer(activity, featureDao);
        indexer.index();

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

}
