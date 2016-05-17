package mil.nga.geopackage.test.tiles.retriever;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import junit.framework.TestCase;

import java.sql.SQLException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.TilesGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.retriever.TileCreator;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * Test Tile Creator image accuracy from a GeoPackage with tiles
 *
 * @author osbornb
 */
public class TileCreatorImageTest extends TilesGeoPackageTestCase {

    /**
     * Constructor
     */
    public TileCreatorImageTest() {
        super(TestConstants.TILES2_DB_NAME, TestConstants.TILES2_DB_FILE_NAME);
    }

    /**
     * Test get tile
     *
     * @throws SQLException
     */
    public void testTileImage() throws SQLException {

        TileDao tileDao = geoPackage.getTileDao(TestConstants.TILES2_DB_TABLE_NAME);
        TestCase.assertEquals(tileDao.getProjection().getEpsg(), ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        Projection webMercator = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        Projection wgs84 = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        SpatialReferenceSystem srs = tileDao.getTileMatrixSet().getSrs();
        Projection projection = ProjectionFactory.getProjection(srs);

        TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();

        int width = 256;
        int height = 256;
        TileCreator webMeractorTileCreator = new TileCreator(tileDao, width, height, tileMatrixSet, webMercator, projection);
        TileCreator wgs84TileCreator = new TileCreator(tileDao, width, height, tileMatrixSet, wgs84, projection);

        BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
                .getWebMercatorBoundingBox(0, 4, 4);
        BoundingBox wgs84BoundingBox = webMercator.getTransformation(wgs84).transform(webMercatorBoundingBox);

        TestCase.assertTrue(webMeractorTileCreator.hasTile(webMercatorBoundingBox));
        TestCase.assertTrue(wgs84TileCreator.hasTile(wgs84BoundingBox));

        GeoPackageTile webMercatorTile = webMeractorTileCreator.getTile(webMercatorBoundingBox);
        GeoPackageTile wgs84Tile = wgs84TileCreator.getTile(wgs84BoundingBox);

        TestCase.assertNotNull(webMercatorTile);
        TestCase.assertEquals(width, webMercatorTile.getWidth());
        TestCase.assertEquals(height, webMercatorTile.getHeight());

        TestCase.assertNotNull(wgs84Tile);
        TestCase.assertEquals(width, wgs84Tile.getWidth());
        TestCase.assertEquals(height, wgs84Tile.getHeight());

        byte[] webMercatorTileBytes = webMercatorTile.getData();
        TestCase.assertNotNull(webMercatorTileBytes);
        Bitmap webMercatorBitmap = BitmapConverter.toBitmap(webMercatorTileBytes);

        byte[] wgs84TileBytes = wgs84Tile.getData();
        TestCase.assertNotNull(wgs84TileBytes);
        Bitmap wgs84Bitmap = BitmapConverter.toBitmap(wgs84TileBytes);

        TestCase.assertEquals(width, webMercatorBitmap.getWidth());
        TestCase.assertEquals(height, webMercatorBitmap.getHeight());
        validateNoTransparency(webMercatorBitmap);

        TestCase.assertEquals(width, wgs84Bitmap.getWidth());
        TestCase.assertEquals(height, wgs84Bitmap.getHeight());
        validateNoTransparency(wgs84Bitmap);

        TestUtils.copyAssetFileToInternalStorage(activity, testContext, TestConstants.TILES2_WEB_MERCATOR_TEST_IMAGE);
        String webMercatorTestImage = TestUtils.getAssetFileInternalStorageLocation(activity, TestConstants.TILES2_WEB_MERCATOR_TEST_IMAGE);
        Bitmap webMercatorTestBitmap = BitmapFactory.decodeFile(webMercatorTestImage);

        TestUtils.copyAssetFileToInternalStorage(activity, testContext, TestConstants.TILES2_WGS84_TEST_IMAGE);
        String wgs84TestImage = TestUtils.getAssetFileInternalStorageLocation(activity, TestConstants.TILES2_WGS84_TEST_IMAGE);
        Bitmap wgs84TestBitmap = BitmapFactory.decodeFile(wgs84TestImage);

        // Compare the image pixels with the expected test image pixels
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int webMercatorPixel = webMercatorBitmap.getPixel(x, y);
                int webMercatorPixelTestPixel = webMercatorTestBitmap.getPixel(x, y);
                TestCase.assertEquals(webMercatorPixelTestPixel, webMercatorPixel);

                int wgs84Pixel = wgs84Bitmap.getPixel(x, y);
                int wgs84PixelTestPixel = wgs84TestBitmap.getPixel(x, y);
                TestCase.assertEquals(wgs84PixelTestPixel, wgs84Pixel);

                TestCase.assertNotSame(webMercatorPixel, wgs84Pixel);
            }
        }

        // To save the images to the external storage if the test images need to change, requires READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions
        /*
        try {
            webMercatorBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(new File(Environment.getExternalStorageDirectory(), TestConstants.TILES2_WEB_MERCATOR_TEST_IMAGE)));
            wgs84Bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(new File(Environment.getExternalStorageDirectory(), TestConstants.TILES2_WGS84_TEST_IMAGE)));
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        */

    }

    /**
     * Validate that the bitmap has no transparency
     *
     * @param bitmap
     */
    private void validateNoTransparency(Bitmap bitmap) {

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                TestCase.assertTrue(bitmap.getPixel(x, y) != 0);
            }
        }

    }

}
