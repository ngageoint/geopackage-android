package mil.nga.geopackage.tiles.features.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.R;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.tiles.features.CustomFeaturesTile;

/**
 * Draws a tile indicating the number of features that exist within the tile, visible when zoomed
 * in closer. The number is drawn in the center of the tile and by default is surrounded by a colored
 * circle with border.  By default a tile border is drawn and the tile is colored (transparently
 * most likely). The paint objects for each draw type can be modified to or set to null (except for
 * the text paint object).
 *
 * @author osbornb
 * @since 1.1.0
 */
public class NumberFeaturesTile implements CustomFeaturesTile {

    /**
     * Text paint object
     */
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Circle paint object
     */
    private Paint circlePaint = null;

    /**
     * Circle fill paint object
     */
    private Paint circleFillPaint = null;

    /**
     * Tile border paint object
     */
    private Paint tileBorderPaint = null;

    /**
     * Tile fill paint object
     */
    private Paint tileFillPaint = null;

    /**
     * The percentage of border to include around the edges of the text in the circle
     */
    private float circlePaddingPercentage;

    /**
     * Flag indicating whether tiles should be drawn for feature tables that are not indexed
     */
    private boolean drawUnindexedTiles;

    /**
     * Constructor
     *
     * @param context
     */
    public NumberFeaturesTile(Context context) {

        Resources resources = context.getResources();

        // Set the default text paint values
        textPaint.setColor(ContextCompat.getColor(context, R.color.number_features_tile_text_color));
        textPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.number_features_tile_text_size));

        // Set the default circle paint values
        if (resources.getBoolean(R.bool.number_features_tile_circle_draw)) {
            circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setColor(ContextCompat.getColor(context, R.color.number_features_tile_circle_color));
            TypedValue circleStrokeWidth = new TypedValue();
            resources.getValue(R.dimen.number_features_tile_circle_stroke_width,
                    circleStrokeWidth, true);
            circlePaint.setStrokeWidth(circleStrokeWidth.getFloat());
        }

        // Set the default circle fill paint values
        if (resources.getBoolean(R.bool.number_features_tile_circle_fill_draw)) {
            circleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circleFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            circleFillPaint.setColor(ContextCompat.getColor(context, R.color.number_features_tile_circle_fill_color));
        }

        // Set the default tile border paint values
        if (resources.getBoolean(R.bool.number_features_tile_border_draw)) {
            tileBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tileBorderPaint.setStyle(Paint.Style.STROKE);
            tileBorderPaint.setColor(ContextCompat.getColor(context, R.color.number_features_tile_border_color));
            TypedValue tileBorderStrokeWidth = new TypedValue();
            resources.getValue(R.dimen.number_features_tile_border_stroke_width,
                    tileBorderStrokeWidth, true);
            tileBorderPaint.setStrokeWidth(tileBorderStrokeWidth.getFloat());
        }

        // Set the default tile fill paint values
        if (resources.getBoolean(R.bool.number_features_tile_fill_draw)) {
            tileFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tileFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            tileFillPaint.setColor(ContextCompat.getColor(context, R.color.number_features_tile_fill_color));
        }

        // Set the default circle padding percentage
        TypedValue circlePadding = new TypedValue();
        resources.getValue(R.dimen.number_features_tile_circle_padding_percentage,
                circlePadding, true);
        circlePaddingPercentage = circlePadding.getFloat();

        // Set the default draw unindexed tiles value
        drawUnindexedTiles = resources.getBoolean(R.bool.number_features_tile_unindexed_draw);
    }

    /**
     * Get the paint object used to draw the text
     *
     * @return text paint object
     */
    public Paint getTextPaint() {
        return textPaint;
    }

    /**
     * Set the paint object used to draw the text
     *
     * @param textPaint
     */
    public void setTextPaint(Paint textPaint) {
        if (textPaint == null) {
            throw new GeoPackageException("Text Paint can not be null");
        }
        this.textPaint = textPaint;
    }

    /**
     * Get the paint object used to draw the circle
     *
     * @return circle paint object
     */
    public Paint getCirclePaint() {
        return circlePaint;
    }

    /**
     * Set the paint object used to draw the circle
     *
     * @param circlePaint
     */
    public void setCirclePaint(Paint circlePaint) {
        this.circlePaint = circlePaint;
    }

    /**
     * Get the paint object used to draw the filled circle
     *
     * @return circle fill paint object
     */
    public Paint getCircleFillPaint() {
        return circleFillPaint;
    }

    /**
     * Set the paint object used to draw the filled circle
     *
     * @param circleFillPaint
     */
    public void setCircleFillPaint(Paint circleFillPaint) {
        this.circleFillPaint = circleFillPaint;
    }

    /**
     * Get the circle padding percentage around the text
     *
     * @return circle padding percentage, 0.0 to 1.0
     */
    public float getCirclePaddingPercentage() {
        return circlePaddingPercentage;
    }

    /**
     * Set the circle padding percentage to pad around the text, value between 0.0 and 1.0
     *
     * @param circlePaddingPercentage
     */
    public void setCirclePaddingPercentage(float circlePaddingPercentage) {
        if (circlePaddingPercentage < 0.0 || circlePaddingPercentage > 1.0) {
            throw new GeoPackageException("Circle padding percentage must be between 0.0 and 1.0: "
                    + circlePaddingPercentage);
        }
        this.circlePaddingPercentage = circlePaddingPercentage;
    }

    /**
     * Get the tile border paint object used to draw a border around the tile
     *
     * @return tile border paint
     */
    public Paint getTileBorderPaint() {
        return tileBorderPaint;
    }

    /**
     * Set the tile border paint object used to draw a border around the tile
     *
     * @param tileBorderPaint
     */
    public void setTileBorderPaint(Paint tileBorderPaint) {
        this.tileBorderPaint = tileBorderPaint;
    }

    /**
     * Get the tile fill paint object used to color the entire tile
     *
     * @return tile fill paint
     */
    public Paint getTileFillPaint() {
        return tileFillPaint;
    }

    /**
     * Set the tile fill paint object used to color the entire tile
     *
     * @param tileFillPaint
     */
    public void setTileFillPaint(Paint tileFillPaint) {
        this.tileFillPaint = tileFillPaint;
    }

    /**
     * Is the draw unindexed tiles option enabled
     *
     * @return true if drawing unindexed tiles
     */
    public boolean isDrawUnindexedTiles() {
        return drawUnindexedTiles;
    }

    /**
     * Set the draw unindexed tiles option
     *
     * @param drawUnindexedTiles
     */
    public void setDrawUnindexedTiles(boolean drawUnindexedTiles) {
        this.drawUnindexedTiles = drawUnindexedTiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(int tileWidth, int tileHeight, long tileFeatureCount, FeatureIndexResults featureIndexResults) {

        String featureText = String.valueOf(tileFeatureCount);
        Bitmap bitmap = drawTile(tileWidth, tileHeight, featureText);

        return bitmap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawUnindexedTile(int tileWidth, int tileHeight, long totalFeatureCount, FeatureCursor allFeatureResults) {

        Bitmap bitmap = null;

        if (drawUnindexedTiles) {
            // Draw a tile indicating we have no idea if there are features inside.
            // The table is not indexed and more features exist than the max feature count set.
            bitmap = drawTile(tileWidth, tileHeight, "?");
        }

        return bitmap;
    }

    /**
     * Draw a tile with the provided text label in the middle
     *
     * @param tileWidth
     * @param tileHeight
     * @param text
     * @return
     */
    private Bitmap drawTile(int tileWidth, int tileHeight, String text) {

        // Create bitmap and canvas
        Bitmap bitmap = Bitmap.createBitmap(tileWidth,
                tileHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw the tile fill paint
        if (tileFillPaint != null) {
            canvas.drawRect(0, 0, tileWidth, tileHeight, tileFillPaint);
        }

        // Draw the tile border
        if (tileBorderPaint != null) {
            canvas.drawRect(0, 0, tileWidth, tileHeight, tileBorderPaint);
        }

        // Determine the text bounds
        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);

        // Determine the center of the tile
        int centerX = (int) (bitmap.getWidth() / 2.0f);
        int centerY = (int) (bitmap.getHeight() / 2.0f);

        // Draw the circle
        if (circlePaint != null || circleFillPaint != null) {
            int diameter = Math.max(textBounds.width(), textBounds.height());
            float radius = diameter / 2.0f;
            radius = radius + (diameter * circlePaddingPercentage);

            // Draw the filled circle
            if (circleFillPaint != null) {
                canvas.drawCircle(centerX, centerY, radius, circleFillPaint);
            }

            // Draw the circle
            if (circlePaint != null) {
                canvas.drawCircle(centerX, centerY, radius, circlePaint);
            }

        }

        // Draw the text
        canvas.drawText(text, centerX - textBounds.exactCenterX(), centerY - textBounds.exactCenterY(), textPaint);

        return bitmap;
    }

}
