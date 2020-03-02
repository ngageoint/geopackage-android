package mil.nga.geopackage.extension.style;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.sf.GeometryType;
import mil.nga.sf.util.GeometryUtils;

/**
 * Styles for a single feature geometry or feature table default
 *
 * @author osbornb
 * @since 3.2.0
 */
public class Styles {

    /**
     * Default style
     */
    private StyleRow defaultStyle;

    /**
     * Geometry type to style mapping
     */
    private Map<GeometryType, StyleRow> styles = new HashMap<>();

    /**
     * Table styles flag
     */
    private boolean tableStyles;

    /**
     * Constructor
     */
    public Styles() {
        this(false);
    }

    /**
     * Constructor
     *
     * @param tableStyles table styles
     * @since 3.5.0
     */
    public Styles(boolean tableStyles) {
        this.tableStyles = tableStyles;
    }

    /**
     * Set the default style icon
     *
     * @param styleRow default style
     */
    public void setDefault(StyleRow styleRow) {
        setStyle(styleRow, null);
    }

    /**
     * Set the style for the geometry type
     *
     * @param styleRow     style row
     * @param geometryType geometry type
     */
    public void setStyle(StyleRow styleRow, GeometryType geometryType) {
        if (styleRow != null) {
            styleRow.setTableStyle(tableStyles);
        }
        if (geometryType != null) {
            if (styleRow != null) {
                styles.put(geometryType, styleRow);
            } else {
                styles.remove(geometryType);
            }
        } else {
            defaultStyle = styleRow;
        }
    }

    /**
     * Default style
     *
     * @return default style
     */
    public StyleRow getDefault() {
        return defaultStyle;
    }

    /**
     * Get an unmodifiable mapping between specific geometry types and styles
     *
     * @return geometry types to style mapping
     */
    public Map<GeometryType, StyleRow> getStyles() {
        return Collections.unmodifiableMap(styles);
    }

    /**
     * Get the style, either the default or single geometry type style
     *
     * @return style
     */
    public StyleRow getStyle() {
        return getStyle(null);
    }

    /**
     * Get the style for the geometry type
     *
     * @param geometryType geometry type
     * @return style
     */
    public StyleRow getStyle(GeometryType geometryType) {

        StyleRow styleRow = null;

        if (geometryType != null && !styles.isEmpty()) {
            List<GeometryType> geometryTypes = GeometryUtils
                    .parentHierarchy(geometryType);
            geometryTypes.add(0, geometryType);
            for (GeometryType type : geometryTypes) {
                styleRow = styles.get(type);
                if (styleRow != null) {
                    break;
                }
            }
        }

        if (styleRow == null) {
            styleRow = defaultStyle;
        }

        if (styleRow == null && geometryType == null && styles.size() == 1) {
            styleRow = styles.values().iterator().next();
        }

        return styleRow;
    }

    /**
     * Determine if this styles is empty
     *
     * @return true if empty, false if at least one style
     */
    public boolean isEmpty() {
        return defaultStyle == null && styles.isEmpty();
    }

    /**
     * Determine if there is a default style
     *
     * @return true if default style exists
     */
    public boolean hasDefault() {
        return defaultStyle != null;
    }

    /**
     * Is table styles
     *
     * @return table styles flag
     * @since 3.5.0
     */
    public boolean isTableStyles() {
        return tableStyles;
    }

    /**
     * Set table styles flag
     *
     * @param tableStyles table styles flag
     * @since 3.5.0
     */
    public void setTableStyles(boolean tableStyles) {
        this.tableStyles = tableStyles;
    }

}
