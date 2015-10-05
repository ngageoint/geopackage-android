package mil.nga.geopackage.tiles.features;

import android.graphics.Bitmap;

/**
 * Point icon in place of a drawn circle
 *
 * @author osbornb
 */
public class FeatureTilePointIcon {

    /**
     * Bitmap
     */
    private final Bitmap icon;

    /**
     * Point icon width
     */
    private final int width;

    /**
     * Point icon height
     */
    private final int height;

    /**
     * X pixel offset
     */
    private float xOffset = 0;

    /**
     * Y pixel offset
     */
    private float yOffset = 0;

    /**
     * Constructor
     *
     * @param icon
     */
    public FeatureTilePointIcon(Bitmap icon) {
        this.icon = icon;
        this.width = icon.getWidth();
        this.height = icon.getHeight();
        pinIcon();
    }

    /**
     * Pin the icon to the point, lower middle on the point
     */
    public void pinIcon() {
        xOffset = width / 2.0f;
        yOffset = height;
    }

    /**
     * Center the icon on the point
     */
    public void centerIcon() {
        xOffset = width / 2.0f;
        yOffset = height / 2.0f;
    }

    /**
     * Get the icon
     *
     * @return
     */
    public Bitmap getIcon() {
        return icon;
    }

    /**
     * Get the width
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height
     *
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the x offset
     *
     * @return
     */
    public float getXOffset() {
        return xOffset;
    }

    /**
     * Set the x offset
     *
     * @param xOffset
     */
    public void setXOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    /**
     * Get the y offset
     *
     * @return
     */
    public float getYOffset() {
        return yOffset;
    }

    /**
     * Set the y offset
     *
     * @param yOffset
     */
    public void setYOffset(float yOffset) {
        this.yOffset = yOffset;
    }

}
