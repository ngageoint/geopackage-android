package mil.nga.geopackage.extension.nga.style;

/**
 * Feature Styles, including styles and icons, for a single feature geometry or
 * feature table default
 *
 * @author osbornb
 * @since 3.2.0
 */
public class FeatureStyles {

    /**
     * Style
     */
    private Styles styles;

    /**
     * Icon
     */
    private Icons icons;

    /**
     * Constructor
     */
    public FeatureStyles() {

    }

    /**
     * Constructor
     *
     * @param styles styles
     */
    public FeatureStyles(Styles styles) {
        this(styles, null);
    }

    /**
     * Constructor
     *
     * @param icons icons
     */
    public FeatureStyles(Icons icons) {
        this(null, icons);
    }

    /**
     * Constructor
     *
     * @param styles styles
     * @param icons  icons
     */
    public FeatureStyles(Styles styles, Icons icons) {
        this.styles = styles;
        this.icons = icons;
    }

    /**
     * Get the styles
     *
     * @return styles or null
     */
    public Styles getStyles() {
        return styles;
    }

    /**
     * Set the styles
     *
     * @param styles styles
     */
    public void setStyles(Styles styles) {
        this.styles = styles;
    }

    /**
     * Get the icons
     *
     * @return icons or null
     */
    public Icons getIcons() {
        return icons;
    }

    /**
     * Set the icons
     *
     * @param icons icons
     */
    public void setIcons(Icons icons) {
        this.icons = icons;
    }

}
