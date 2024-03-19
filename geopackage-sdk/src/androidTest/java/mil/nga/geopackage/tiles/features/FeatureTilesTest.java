package mil.nga.geopackage.tiles.features;

import android.graphics.Bitmap;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.db.FeatureIndexer;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.CreateGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    @Test
    public void testFeatureTiles() throws SQLException {
        testFeatureTiles(false, false);
    }

    /**
     * Test feature tiles
     *
     * @throws java.sql.SQLException
     */
    @Test
    public void testFeatureTilesWithGeodesic() throws SQLException {
        testFeatureTiles(false, true);
    }

    /**
     * Test feature tiles
     *
     * @throws java.sql.SQLException
     */
    @Test
    public void testFeatureTilesWithIcon() throws SQLException {
        testFeatureTiles(true, false);
    }

    /**
     * Test feature tiles
     *
     * @throws java.sql.SQLException
     */
    @Test
    public void testFeatureTilesWithIconGeodesic() throws SQLException {
        testFeatureTiles(true, true);
    }

    /**
     * Test feature tiles
     *
     * @param useIcon true to use an icon instead of the default point
     * @param geodesic draw geometries using geodesic lines
     * @throws java.sql.SQLException
     */
    public void testFeatureTiles(boolean useIcon, boolean geodesic) throws SQLException {

        FeatureDao featureDao = FeatureTileUtils.createFeatureDao(geoPackage);

        int num = FeatureTileUtils.insertFeatures(geoPackage, featureDao);

        FeatureTiles featureTiles = FeatureTileUtils
                .createFeatureTiles(activity, geoPackage, featureDao, useIcon, geodesic);

        try {
            FeatureIndexer indexer = new FeatureIndexer(activity, featureDao, geodesic);
            try {
                indexer.index();
            } finally {
                indexer.close();
            }

            FeatureIndexManager indexManager = new FeatureIndexManager(activity,
                    geoPackage, featureDao, geodesic);
            featureTiles.setIndexManager(indexManager);

            indexManager.setIndexLocation(FeatureIndexType.GEOPACKAGE);
            int indexed = indexManager.index();
            assertEquals(num, indexed);

            createTiles(featureTiles, 0, 3);
        } finally {
            featureTiles.close();
        }
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
                if (bitmap != null) {
                    long count = featureTiles.queryIndexedFeaturesCount(i, j,
                            zoom);
                    assertTrue(count > 0);
                    assertEquals(featureTiles.getTileWidth(),
                            bitmap.getWidth());
                    assertEquals(featureTiles.getTileHeight(),
                            bitmap.getHeight());
                }
            }
        }
    }

}
