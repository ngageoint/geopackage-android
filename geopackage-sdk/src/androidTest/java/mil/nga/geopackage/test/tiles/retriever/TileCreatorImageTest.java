package mil.nga.geopackage.test.tiles.retriever;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import junit.framework.TestCase;

import java.sql.SQLException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.io.BitmapConverter;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.TilesGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.retriever.TileCreator;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * Test Tile Creator image accuracy from a GeoPackage with tiles
 *
 * @author osbornb
 */
public class TileCreatorImageTest extends TilesGeoPackageTestCase {

    private final int COLOR_TOLERANCE = 19;

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
        TestCase.assertEquals(tileDao.getProjection().getAuthority(),
                ProjectionConstants.AUTHORITY_EPSG);
        TestCase.assertEquals(Long.parseLong(tileDao.getProjection().getCode()),
                ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        Projection webMercator = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
        Projection wgs84 = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        int width = 256;
        int height = 256;
        TileCreator webMeractorTileCreator = new TileCreator(tileDao, width, height, webMercator);
        TileCreator wgs84TileCreator = new TileCreator(tileDao, width, height);

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

        int redDiff = 0;
        int greenDiff = 0;
        int blueDiff = 0;

        // Compare the image pixels with the expected test image pixels
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int webMercatorPixel = webMercatorBitmap.getPixel(x, y);
                int webMercatorPixelTestPixel = webMercatorTestBitmap.getPixel(x, y);

                int webMercatorRed = Color.red(webMercatorPixel);
                int webMercatorGreen = Color.green(webMercatorPixel);
                int webMercatorBlue = Color.blue(webMercatorPixel);
                int webMercatorAlpha = Color.alpha(webMercatorPixel);

                int webMercatorTestRed = Color.red(webMercatorPixelTestPixel);
                int webMercatorTestGreen = Color.green(webMercatorPixelTestPixel);
                int webMercatorTestBlue = Color.blue(webMercatorPixelTestPixel);
                int webMercatorTestAlpha = Color.alpha(webMercatorPixelTestPixel);
                
                // Colors differ between phones and emulators, try to validate within a tolerance range the colors are as expected
                TestCase.assertTrue("Web Meractor Red pixel " + webMercatorRed + " is not within the " + COLOR_TOLERANCE + " range of test red pixel " + webMercatorTestRed,
                        Math.abs(webMercatorRed - webMercatorTestRed) <= COLOR_TOLERANCE);
                TestCase.assertTrue("Web Meractor Green pixel " + webMercatorGreen + " is not within the " + COLOR_TOLERANCE + " range of test green pixel " + webMercatorTestGreen,
                        Math.abs(webMercatorGreen - webMercatorTestGreen) <= COLOR_TOLERANCE);
                TestCase.assertTrue("Web Meractor Blue pixel " + webMercatorBlue + " is not within the " + COLOR_TOLERANCE + " range of test blue pixel " + webMercatorTestBlue,
                        Math.abs(webMercatorBlue - webMercatorTestBlue) <= COLOR_TOLERANCE);
                TestCase.assertTrue("Web Meractor Alpha pixel " + webMercatorAlpha + " is not within the " + COLOR_TOLERANCE + " range of test alpha pixel " + webMercatorTestAlpha,
                        Math.abs(webMercatorAlpha - webMercatorTestAlpha) <= COLOR_TOLERANCE);

                int wgs84Pixel = wgs84Bitmap.getPixel(x, y);
                int wgs84PixelTestPixel = wgs84TestBitmap.getPixel(x, y);

                int wgs84Red = Color.red(wgs84Pixel);
                int wgs84Green = Color.green(wgs84Pixel);
                int wgs84Blue = Color.blue(wgs84Pixel);
                int wgs84Alpha = Color.alpha(wgs84Pixel);

                int wgs84TestRed = Color.red(wgs84PixelTestPixel);
                int wgs84TestGreen = Color.green(wgs84PixelTestPixel);
                int wgs84TestBlue = Color.blue(wgs84PixelTestPixel);
                int wgs84TestAlpha = Color.alpha(wgs84PixelTestPixel);

                // Colors differ between phones and emulators, try to validate within a tolerance range the colors are as expected
                TestCase.assertTrue("WGS84 Red pixel " + wgs84Red + " is not within the " + COLOR_TOLERANCE + " range of test red pixel " + wgs84TestRed,
                        Math.abs(wgs84Red - wgs84TestRed) <= COLOR_TOLERANCE);
                TestCase.assertTrue("WGS84 Green pixel " + wgs84Green + " is not within the " + COLOR_TOLERANCE + " range of test green pixel " + wgs84TestGreen,
                        Math.abs(wgs84Green - wgs84TestGreen) <= COLOR_TOLERANCE);
                TestCase.assertTrue("WGS84 Blue pixel " + wgs84Blue + " is not within the " + COLOR_TOLERANCE + " range of test blue pixel " + wgs84TestBlue,
                        Math.abs(wgs84Blue - wgs84TestBlue) <= COLOR_TOLERANCE);
                TestCase.assertTrue("WGS84 Alpha pixel " + wgs84Alpha + " is not within the " + COLOR_TOLERANCE + " range of test alpha pixel " + wgs84TestAlpha,
                        Math.abs(wgs84Alpha - wgs84TestAlpha) <= COLOR_TOLERANCE);

                redDiff = Math.max(redDiff, Math.abs(webMercatorRed - wgs84Red));
                greenDiff = Math.max(greenDiff, Math.abs(webMercatorGreen - wgs84Green));
                blueDiff = Math.max(blueDiff, Math.abs(webMercatorBlue - wgs84Blue));
            }
        }

        // Verify the web mercator and wgs84 images were different
        TestCase.assertTrue(redDiff > COLOR_TOLERANCE);
        TestCase.assertTrue(greenDiff > COLOR_TOLERANCE);
        TestCase.assertTrue(blueDiff > COLOR_TOLERANCE);

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
