package mil.nga.geopackage.tiles.features;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * Feature Tile Canvas for creating layered tiles to draw ordered features.
 * Draw Order: polygons, lines, points, icons
 *
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureTileCanvas {

    /**
     * Polygon layer index
     */
    private static final int POLYGON_LAYER = 0;

    /**
     * Line layer index
     */
    private static final int LINE_LAYER = 1;

    /**
     * Point layer index
     */
    private static final int POINT_LAYER = 2;

    /**
     * Icon layer index
     */
    private static final int ICON_LAYER = 3;

    /**
     * Tile width
     */
    private final int tileWidth;

    /**
     * Tile height
     */
    private final int tileHeight;

    /**
     * Layered bitmap
     */
    private final Bitmap[] layeredBitmap = new Bitmap[4];

    /**
     * Layered canvas
     */
    private final Canvas[] layeredCanvas = new Canvas[4];

    /**
     * Constructor
     *
     * @param tileWidth  tile width
     * @param tileHeight tile height
     */
    public FeatureTileCanvas(int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    /**
     * Get the polygon bitmap
     *
     * @return polygon bitmap
     */
    public Bitmap getPolygonBitmap() {
        return getBitmap(POLYGON_LAYER);
    }

    /**
     * Get the polygon canvas
     *
     * @return polygon canvas
     */
    public Canvas getPolygonCanvas() {
        return getCanvas(POLYGON_LAYER);
    }

    /**
     * Get the line bitmap
     *
     * @return line bitmap
     */
    public Bitmap getLineBitmap() {
        return getBitmap(LINE_LAYER);
    }

    /**
     * Get the line canvas
     *
     * @return line canvas
     */
    public Canvas getLineCanvas() {
        return getCanvas(LINE_LAYER);
    }

    /**
     * Get the point bitmap
     *
     * @return point bitmap
     */
    public Bitmap getPointBitmap() {
        return getBitmap(POINT_LAYER);
    }

    /**
     * Get the point canvas
     *
     * @return point canvas
     */
    public Canvas getPointCanvas() {
        return getCanvas(POINT_LAYER);
    }

    /**
     * Get the icon bitmap
     *
     * @return icon bitmap
     */
    public Bitmap getIconBitmap() {
        return getBitmap(ICON_LAYER);
    }

    /**
     * Get the icon canvas
     *
     * @return icon canvas
     */
    public Canvas getIconCanvas() {
        return getCanvas(ICON_LAYER);
    }

    /**
     * Create the final bitmap from the layers, resets the layers
     *
     * @return bitmap
     */
    public Bitmap createBitmap() {

        Bitmap bitmap = null;
        Canvas canvas = null;

        for (int layer = 0; layer < 4; layer++) {

            Bitmap layerBitmap = layeredBitmap[layer];

            if (layerBitmap != null) {

                if (bitmap == null) {
                    bitmap = layerBitmap;
                    canvas = layeredCanvas[layer];
                } else {
                    canvas.drawBitmap(layerBitmap, new Matrix(), null);
                    layerBitmap.recycle();
                }

                layeredBitmap[layer] = null;
                layeredCanvas[layer] = null;
            }
        }

        return bitmap;
    }

    /**
     * Recycle the layered bitmaps
     */
    public void recycle() {
        for (int layer = 0; layer < 4; layer++) {
            Bitmap bitmap = layeredBitmap[layer];
            if (bitmap != null) {
                bitmap.recycle();
                layeredBitmap[layer] = null;
                layeredCanvas[layer] = null;
            }
        }
    }

    /**
     * Get the bitmap for the layer index
     *
     * @param layer layer index
     * @return bitmap
     */
    private Bitmap getBitmap(int layer) {
        Bitmap bitmap = layeredBitmap[layer];
        if (bitmap == null) {
            createBitmapAndCanvas(layer);
            bitmap = layeredBitmap[layer];
        }
        return bitmap;
    }

    /**
     * Get the canvas for the layer index
     *
     * @param layer layer index
     * @return canvas
     */
    private Canvas getCanvas(int layer) {
        Canvas canvas = layeredCanvas[layer];
        if (canvas == null) {
            createBitmapAndCanvas(layer);
            canvas = layeredCanvas[layer];
        }
        return canvas;
    }

    /**
     * Create a new empty Bitmap and Canvas
     *
     * @param layer layer index
     */
    private void createBitmapAndCanvas(int layer) {
        layeredBitmap[layer] = Bitmap.createBitmap(tileWidth,
                tileHeight, Bitmap.Config.ARGB_8888);
        layeredCanvas[layer] = new Canvas(layeredBitmap[layer]);
    }

}
