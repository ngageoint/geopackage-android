package mil.nga.geopackage.extension.style;

/**
 * Feature Style, including a style and icon, for a single feature geometry
 *
 * @author osbornb
 * @since 3.2.0
 */
public class FeatureStyle {

    /**
     * Style
     */
    private StyleRow style;

    /**
     * Icon
     */
    private IconRow icon;

    /**
     * Constructor
     */
    public FeatureStyle() {

    }

    /**
     * Constructor
     *
     * @param style style row
     */
    public FeatureStyle(StyleRow style) {
        this(style, null);
    }

    /**
     * Constructor
     *
     * @param icon icon row
     */
    public FeatureStyle(IconRow icon) {
        this(null, icon);
    }

    /**
     * Constructor
     *
     * @param style style row
     * @param icon  icon row
     */
    public FeatureStyle(StyleRow style, IconRow icon) {
        this.style = style;
        this.icon = icon;
    }

    /**
     * Get the style row
     *
     * @return style row or null
     */
    public StyleRow getStyle() {
        return style;
    }

    /**
     * Set the style row
     *
     * @param style style row
     */
    public void setStyle(StyleRow style) {
        this.style = style;
    }

    /**
     * Get the icon row
     *
     * @return icon row or null
     */
    public IconRow getIcon() {
        return icon;
    }

    /**
     * Set the icon row
     *
     * @param icon icon row
     */
    public void setIcon(IconRow icon) {
        this.icon = icon;
    }

    /**
     * Check if the feature style has a style row
     *
     * @return true if has style row
     */
    public boolean hasStyle() {
        return style != null;
    }

    /**
     * Check if the feature style has an icon row
     *
     * @return true if has icon row
     */
    public boolean hasIcon() {
        return icon != null;
    }

    /**
     * Determine if an icon exists and should be used. Returns false when an
     * icon does not exist or when both a table level icon and row level style
     * exist.
     *
     * @return true if the icon exists and should be used over a style
     * @since 3.5.0
     */
    public boolean useIcon() {
        return hasIcon()
                && (!icon.isTableIcon() || !hasStyle() || style.isTableStyle());
    }

}
