package mil.nga.geopackage.test.tiles.features;

import android.app.Activity;
import android.graphics.Bitmap;

import java.io.IOException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.TileUtils;
import mil.nga.geopackage.tiles.features.FeaturePreview;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.sf.proj.ProjectionConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test feature preview
 *
 * @author osbornb
 */
public class FeaturePreviewUtils {

    /**
     * Test the GeoPackage draw feature preview
     *
     * @param activity   activity
     * @param geoPackage GeoPackage
     * @throws IOException upon error
     */
    public static void testDraw(Activity activity, GeoPackage geoPackage) throws IOException {

        for (String featureTable : geoPackage.getFeatureTables()) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            int count = featureDao.count(
                    CoreSQLUtils.quoteWrap(featureDao.getGeometryColumnName())
                            + " IS NOT NULL");

            BoundingBox contentsBoundingBox = geoPackage
                    .getContentsBoundingBox(featureTable);
            BoundingBox indexedBoundingBox = geoPackage
                    .getBoundingBox(featureTable);
            boolean expectImage = (contentsBoundingBox != null
                    || indexedBoundingBox != null) && count > 0;
            boolean epsg = featureDao.getProjection().getAuthority()
                    .equalsIgnoreCase(ProjectionConstants.AUTHORITY_EPSG);

            FeaturePreview preview = new FeaturePreview(activity, geoPackage, featureDao);

            Bitmap image = preview.draw();
            if (epsg) {
                assertEquals(expectImage, image != null);
            }

            preview.setBufferPercentage(0.4);
            preview.setLimit((int) Math.ceil(count / 2.0));
            Bitmap imageLimit = preview.draw();
            if (epsg) {
                assertEquals(expectImage, imageLimit != null);
            }

            preview.setManual(true);
            preview.setBufferPercentage(0.05);
            preview.setLimit(null);
            FeatureTiles featureTiles = preview.getFeatureTiles();
            featureTiles.setTileWidth(TileUtils.TILE_PIXELS_DEFAULT);
            featureTiles.setTileHeight(TileUtils.TILE_PIXELS_DEFAULT);
            featureTiles.setDensity(
                    TileUtils.density(TileUtils.TILE_PIXELS_DEFAULT));
            featureTiles.clearIconCache();
            Bitmap imageManual = preview.draw();
            if (epsg) {
                assertNotNull(imageManual);
            }

            preview.setBufferPercentage(0.35);
            preview.setLimit(Math.max(count - 1, 1));
            Bitmap imageManualLimit = preview.draw();
            if (epsg) {
                assertNotNull(imageManualLimit);
            }

            preview.setBufferPercentage(0.15);
            preview.setLimit(null);
            preview.appendWhere(
                    CoreSQLUtils.quoteWrap(featureDao.getIdColumnName()) + " > "
                            + ((int) Math.floor(count / 2.0)));
            Bitmap imageManualWhere = preview.draw();
            if (epsg) {
                assertNotNull(imageManualWhere);
            }

            if(image != null) {
                image.recycle();
            }
            if(imageLimit != null) {
                imageLimit.recycle();
            }
            if(imageManual != null) {
                imageManual.recycle();
            }
            if(imageManualLimit != null) {
                imageManualLimit.recycle();
            }
            if(imageManualWhere != null) {
                imageManualWhere.recycle();
            }

            preview.close();
        }

    }

}
