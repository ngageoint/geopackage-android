package mil.nga.geopackage.tiles.features.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.R;
import mil.nga.geopackage.tiles.features.CustomFeaturesTile;

/**
 * Draws a tile with the the number of features in a circle in the middle of the tile
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
    private Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Circle fill paint object
     */
    private Paint circleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * The percentage of border to include around the edges of the text in the circle
     */
    private float circlePaddingPercentage;

    /**
     * Constructor
     *
     * @param context
     */
    public NumberFeaturesTile(Context context) {
        circlePaint.setStyle(Paint.Style.STROKE);
        circleFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        Resources resources = context.getResources();

        // Set the default text paint values
        textPaint.setColor(resources.getColor(R.color.number_features_tile_text_color));
        TypedValue textSize = new TypedValue();
        resources.getValue(R.dimen.number_features_tile_text_size,
                textSize, true);
        textPaint.setTextSize(textSize.getFloat() * resources.getDisplayMetrics().density);

        // Set the default circle paint values
        circlePaint.setColor(resources.getColor(R.color.number_features_tile_circle_color));
        TypedValue circleStrokeWidth = new TypedValue();
        resources.getValue(R.dimen.number_features_tile_circle_stroke_width,
                circleStrokeWidth, true);
        circlePaint.setStrokeWidth(circleStrokeWidth.getFloat());

        // Set the default circle fill paint values
        circleFillPaint.setColor(resources.getColor(R.color.number_features_tile_circle_fill_color));

        // Set the default circle padding percentage
        TypedValue circlePadding = new TypedValue();
        resources.getValue(R.dimen.number_features_tile_circle_padding_percentage,
                circlePadding, true);
        circlePaddingPercentage = circlePadding.getFloat();
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
     * {@inheritDoc}
     */
    @Override
    public Bitmap drawTile(int tileWidth, int tileHeight, long features) {

        String featureText = String.valueOf(features);

        // Create bitmap and canvas
        Bitmap bitmap = Bitmap.createBitmap(tileWidth,
                tileHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Determine the text bounds
        Rect textBounds = new Rect();
        textPaint.getTextBounds(featureText, 0, featureText.length(), textBounds);

        // Determine the center of the tile
        int x = (bitmap.getWidth() - textBounds.width()) / 2;
        int y = (bitmap.getHeight() + textBounds.height()) / 2;

        // Draw the circle
        if (circlePaint != null || circleFillPaint != null) {
            int diameter = Math.max(textBounds.width(), textBounds.height());
            float radius = diameter / 2.0f;
            radius = radius + (diameter * circlePaddingPercentage);

            // Draw the filled circle
            if (circleFillPaint != null) {
                canvas.drawCircle(x, y, radius, circleFillPaint);
            }

            // Draw the circle
            if (circlePaint != null) {
                canvas.drawCircle(x, y, radius, circlePaint);
            }

        }

        // Draw the text
        canvas.drawText(featureText, x - textBounds.exactCenterX(), y - textBounds.exactCenterY(), textPaint);

        return bitmap;
    }

}
