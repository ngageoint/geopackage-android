package mil.nga.geopackage.extension.nga.style;

import java.util.List;
import java.util.Map;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.tiles.features.PixelBounds;
import mil.nga.sf.GeometryType;

/**
 * Feature Table Styles, styles and icons for an individual feature table
 *
 * @author osbornb
 * @since 3.2.0
 */
public class FeatureTableStyles {

    /**
     * Feature Styles
     */
    private final FeatureStyleExtension featureStyleExtension;

    /**
     * Feature Table name
     */
    private final String tableName;

    /**
     * Cached table feature styles
     */
    private final FeatureStyles cachedTableFeatureStyles = new FeatureStyles();

    /**
     * Constructor
     *
     * @param geoPackage   GeoPackage
     * @param featureTable feature table
     */
    public FeatureTableStyles(GeoPackage geoPackage, FeatureTable featureTable) {
        this(geoPackage, featureTable.getTableName());
    }

    /**
     * Constructor
     *
     * @param geoPackage      GeoPackage
     * @param geometryColumns geometry columns
     */
    public FeatureTableStyles(GeoPackage geoPackage,
                              GeometryColumns geometryColumns) {
        this(geoPackage, geometryColumns.getTableName());
    }

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     * @param contents   feature contents
     */
    public FeatureTableStyles(GeoPackage geoPackage, Contents contents) {
        this(geoPackage, contents.getTableName());
    }

    /**
     * Constructor
     *
     * @param geoPackage   GeoPackage
     * @param featureTable feature table
     */
    public FeatureTableStyles(GeoPackage geoPackage, String featureTable) {
        featureStyleExtension = new FeatureStyleExtension(geoPackage);
        tableName = featureTable;
        if (!geoPackage.isFeatureTable(featureTable)) {
            throw new GeoPackageException(
                    "Table must be a feature table. Table: " + featureTable
                            + ", Actual Type: "
                            + geoPackage.getTableType(featureTable));
        }
    }

    /**
     * Get the feature style extension
     *
     * @return feature style extension
     */
    public FeatureStyleExtension getFeatureStyleExtension() {
        return featureStyleExtension;
    }

    /**
     * Get the feature table name
     *
     * @return feature table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Determine if the GeoPackage has the extension for the table
     *
     * @return true if has extension
     */
    public boolean has() {
        return featureStyleExtension.has(tableName);
    }

    /**
     * Create style, icon, table style, and table icon relationships for the
     * feature table
     */
    public void createRelationships() {
        featureStyleExtension.createRelationships(tableName);
    }

    /**
     * Check if feature table has a style, icon, table style, or table icon
     * relationships
     *
     * @return true if has a relationship
     */
    public boolean hasRelationship() {
        return featureStyleExtension.hasRelationship(tableName);
    }

    /**
     * Create a style relationship for the feature table
     */
    public void createStyleRelationship() {
        featureStyleExtension.createStyleRelationship(tableName);
    }

    /**
     * Determine if a style relationship exists for the feature table
     *
     * @return true if relationship exists
     */
    public boolean hasStyleRelationship() {
        return featureStyleExtension.hasStyleRelationship(tableName);
    }

    /**
     * Create a feature table style relationship
     */
    public void createTableStyleRelationship() {
        featureStyleExtension.createTableStyleRelationship(tableName);
    }

    /**
     * Determine if feature table style relationship exists
     *
     * @return true if relationship exists
     */
    public boolean hasTableStyleRelationship() {
        return featureStyleExtension.hasTableStyleRelationship(tableName);
    }

    /**
     * Create an icon relationship for the feature table
     */
    public void createIconRelationship() {
        featureStyleExtension.createIconRelationship(tableName);
    }

    /**
     * Determine if an icon relationship exists for the feature table
     *
     * @return true if relationship exists
     */
    public boolean hasIconRelationship() {
        return featureStyleExtension.hasIconRelationship(tableName);
    }

    /**
     * Create a feature table icon relationship
     */
    public void createTableIconRelationship() {
        featureStyleExtension.createTableIconRelationship(tableName);
    }

    /**
     * Determine if feature table icon relationship exists
     *
     * @return true if relationship exists
     */
    public boolean hasTableIconRelationship() {
        return featureStyleExtension.hasTableIconRelationship(tableName);
    }

    /**
     * Delete the style and icon table and row relationships for the feature
     * table
     */
    public void deleteRelationships() {
        featureStyleExtension.deleteRelationships(tableName);
    }

    /**
     * Delete a style relationship for the feature table
     */
    public void deleteStyleRelationship() {
        featureStyleExtension.deleteStyleRelationship(tableName);
    }

    /**
     * Delete a table style relationship for the feature table
     */
    public void deleteTableStyleRelationship() {
        featureStyleExtension.deleteTableStyleRelationship(tableName);
    }

    /**
     * Delete a icon relationship for the feature table
     */
    public void deleteIconRelationship() {
        featureStyleExtension.deleteIconRelationship(tableName);
    }

    /**
     * Delete a table icon relationship for the feature table
     */
    public void deleteTableIconRelationship() {
        featureStyleExtension.deleteTableIconRelationship(tableName);
    }

    /**
     * Get a Style Mapping DAO
     *
     * @return style mapping DAO
     */
    public StyleMappingDao getStyleMappingDao() {
        return featureStyleExtension.getStyleMappingDao(tableName);
    }

    /**
     * Get a Table Style Mapping DAO
     *
     * @return table style mapping DAO
     */
    public StyleMappingDao getTableStyleMappingDao() {
        return featureStyleExtension.getTableStyleMappingDao(tableName);
    }

    /**
     * Get a Icon Mapping DAO
     *
     * @return icon mapping DAO
     */
    public StyleMappingDao getIconMappingDao() {
        return featureStyleExtension.getIconMappingDao(tableName);
    }

    /**
     * Get a Table Icon Mapping DAO
     *
     * @return table icon mapping DAO
     */
    public StyleMappingDao getTableIconMappingDao() {
        return featureStyleExtension.getTableIconMappingDao(tableName);
    }

    /**
     * Get a style DAO
     *
     * @return style DAO
     */
    public StyleDao getStyleDao() {
        return featureStyleExtension.getStyleDao();
    }

    /**
     * Get a icon DAO
     *
     * @return icon DAO
     */
    public IconDao getIconDao() {
        return featureStyleExtension.getIconDao();
    }

    /**
     * Get the table feature styles
     *
     * @return table feature styles or null
     */
    public FeatureStyles getTableFeatureStyles() {
        return featureStyleExtension.getTableFeatureStyles(tableName);
    }

    /**
     * Get the table styles
     *
     * @return table styles or null
     */
    public Styles getTableStyles() {
        return featureStyleExtension.getTableStyles(tableName);
    }

    /**
     * Get the cached table styles, querying and caching if needed
     *
     * @return cached table styles
     */
    public Styles getCachedTableStyles() {

        Styles styles = cachedTableFeatureStyles.getStyles();

        if (styles == null) {
            synchronized (cachedTableFeatureStyles) {
                styles = cachedTableFeatureStyles.getStyles();
                if (styles == null) {
                    styles = getTableStyles();
                    if (styles == null) {
                        styles = new Styles(true);
                    }
                    cachedTableFeatureStyles.setStyles(styles);
                }
            }
        }

        if (styles.isEmpty()) {
            styles = null;
        }

        return styles;
    }

    /**
     * Get the table style of the geometry type
     *
     * @param geometryType geometry type
     * @return style row
     */
    public StyleRow getTableStyle(GeometryType geometryType) {
        return featureStyleExtension.getTableStyle(tableName, geometryType);
    }

    /**
     * Get the table style default
     *
     * @return style row
     */
    public StyleRow getTableStyleDefault() {
        return featureStyleExtension.getTableStyleDefault(tableName);
    }

    /**
     * Get the table icons
     *
     * @return table icons or null
     */
    public Icons getTableIcons() {
        return featureStyleExtension.getTableIcons(tableName);
    }

    /**
     * Get the cached table icons, querying and caching if needed
     *
     * @return cached table icons
     */
    public Icons getCachedTableIcons() {

        Icons icons = cachedTableFeatureStyles.getIcons();

        if (icons == null) {
            synchronized (cachedTableFeatureStyles) {
                icons = cachedTableFeatureStyles.getIcons();
                if (icons == null) {
                    icons = getTableIcons();
                    if (icons == null) {
                        icons = new Icons(true);
                    }
                    cachedTableFeatureStyles.setIcons(icons);
                }
            }
        }

        if (icons.isEmpty()) {
            icons = null;
        }

        return icons;
    }

    /**
     * Get the table icon of the geometry type
     *
     * @param geometryType geometry type
     * @return icon row
     */
    public IconRow getTableIcon(GeometryType geometryType) {
        return featureStyleExtension.getTableIcon(tableName, geometryType);
    }

    /**
     * Get the table icon default
     *
     * @return icon row
     */
    public IconRow getTableIconDefault() {
        return featureStyleExtension.getTableIconDefault(tableName);
    }

    /**
     * Get all styles used by the feature table
     *
     * @return style rows mapped by ids
     * @since 6.3.0
     */
    public Map<Long, StyleRow> getStyles() {
        return featureStyleExtension.getStyles(tableName);
    }

    /**
     * Get all styles used by feature rows in the table
     *
     * @return style rows mapped by ids
     * @since 6.3.0
     */
    public Map<Long, StyleRow> getFeatureStyles() {
        return featureStyleExtension.getFeatureStyles(tableName);
    }

    /**
     * Get all icons used by the feature table
     *
     * @return icon rows mapped by ids
     * @since 6.3.0
     */
    public Map<Long, IconRow> getIcons() {
        return featureStyleExtension.getIcons(tableName);
    }

    /**
     * Get all icons used by feature rows in the table
     *
     * @return icon rows mapped by ids
     * @since 6.3.0
     */
    public Map<Long, IconRow> getFeatureIcons() {
        return featureStyleExtension.getFeatureIcons(tableName);
    }

    /**
     * Get the feature styles for the feature row
     *
     * @param featureRow feature row
     * @return feature styles or null
     */
    public FeatureStyles getFeatureStyles(FeatureRow featureRow) {
        return featureStyleExtension.getFeatureStyles(featureRow);
    }

    /**
     * Get the feature styles for the feature id
     *
     * @param featureId feature id
     * @return feature styles or null
     */
    public FeatureStyles getFeatureStyles(long featureId) {
        return featureStyleExtension.getFeatureStyles(tableName, featureId);
    }

    /**
     * Get the feature style (style and icon) of the feature row, searching in
     * order: feature geometry type style or icon, feature default style or
     * icon, table geometry type style or icon, table default style or icon
     *
     * @param featureRow feature row
     * @return feature style
     */
    public FeatureStyle getFeatureStyle(FeatureRow featureRow) {
        return getFeatureStyle(featureRow, featureRow.getGeometryType());
    }

    /**
     * Get the feature style (style and icon) of the feature row with the
     * provided geometry type, searching in order: feature geometry type style
     * or icon, feature default style or icon, table geometry type style or
     * icon, table default style or icon
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     * @return feature style
     */
    public FeatureStyle getFeatureStyle(FeatureRow featureRow,
                                        GeometryType geometryType) {
        return getFeatureStyle(featureRow.getId(), geometryType);
    }

    /**
     * Get the feature style default (style and icon) of the feature row,
     * searching in order: feature default style or icon, table default style or
     * icon
     *
     * @param featureRow feature row
     * @return feature style
     */
    public FeatureStyle getFeatureStyleDefault(FeatureRow featureRow) {
        return getFeatureStyle(featureRow.getId(), null);
    }

    /**
     * Get the feature style (style and icon) of the feature, searching in
     * order: feature geometry type style or icon, feature default style or
     * icon, table geometry type style or icon, table default style or icon
     *
     * @param featureId    feature id
     * @param geometryType geometry type
     * @return feature style
     */
    public FeatureStyle getFeatureStyle(long featureId,
                                        GeometryType geometryType) {

        FeatureStyle featureStyle = null;

        StyleRow style = getStyle(featureId, geometryType);
        IconRow icon = getIcon(featureId, geometryType);

        if (style != null || icon != null) {
            featureStyle = new FeatureStyle(style, icon);
        }

        return featureStyle;
    }

    /**
     * Get the feature style (style and icon) of the feature, searching in
     * order: feature geometry type style or icon, feature default style or
     * icon, table geometry type style or icon, table default style or icon
     *
     * @param featureId feature id
     * @return feature style
     */
    public FeatureStyle getFeatureStyleDefault(long featureId) {
        return getFeatureStyle(featureId, null);
    }

    /**
     * Get the styles for the feature row
     *
     * @param featureRow feature row
     * @return styles or null
     */
    public Styles getStyles(FeatureRow featureRow) {
        return featureStyleExtension.getStyles(featureRow);
    }

    /**
     * Get the styles for the feature id
     *
     * @param featureId feature id
     * @return styles or null
     */
    public Styles getStyles(long featureId) {
        return featureStyleExtension.getStyles(tableName, featureId);
    }

    /**
     * Get the style of the feature row, searching in order: feature geometry
     * type style, feature default style, table geometry type style, table
     * default style
     *
     * @param featureRow feature row
     * @return style row
     */
    public StyleRow getStyle(FeatureRow featureRow) {
        return getStyle(featureRow, featureRow.getGeometryType());
    }

    /**
     * Get the style of the feature row with the provided geometry type,
     * searching in order: feature geometry type style, feature default style,
     * table geometry type style, table default style
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     * @return style row
     */
    public StyleRow getStyle(FeatureRow featureRow, GeometryType geometryType) {
        return getStyle(featureRow.getId(), geometryType);
    }

    /**
     * Get the default style of the feature row, searching in order: feature
     * default style, table default style
     *
     * @param featureRow feature row
     * @return style row
     */
    public StyleRow getStyleDefault(FeatureRow featureRow) {
        return getStyle(featureRow.getId(), null);
    }

    /**
     * Get the style of the feature, searching in order: feature geometry type
     * style, feature default style, table geometry type style, table default
     * style
     *
     * @param featureId    feature id
     * @param geometryType geometry type
     * @return style row
     */
    public StyleRow getStyle(long featureId, GeometryType geometryType) {

        StyleRow styleRow = featureStyleExtension.getStyle(tableName,
                featureId, geometryType, false);

        if (styleRow == null) {

            // Table Style
            Styles styles = getCachedTableStyles();
            if (styles != null) {
                styleRow = styles.getStyle(geometryType);
            }

        }

        return styleRow;
    }

    /**
     * Get the default style of the feature, searching in order: feature default
     * style, table default style
     *
     * @param featureId feature id
     * @return style row
     */
    public StyleRow getStyleDefault(long featureId) {
        return getStyle(featureId, null);
    }

    /**
     * Get the icons for the feature row
     *
     * @param featureRow feature row
     * @return icons or null
     */
    public Icons getIcons(FeatureRow featureRow) {
        return featureStyleExtension.getIcons(featureRow);
    }

    /**
     * Get the icons for the feature id
     *
     * @param featureId feature id
     * @return icons or null
     */
    public Icons getIcons(long featureId) {
        return featureStyleExtension.getIcons(tableName, featureId);
    }

    /**
     * Get the icon of the feature row, searching in order: feature geometry
     * type icon, feature default icon, table geometry type icon, table default
     * icon
     *
     * @param featureRow feature row
     * @return icon row
     */
    public IconRow getIcon(FeatureRow featureRow) {
        return getIcon(featureRow, featureRow.getGeometryType());
    }

    /**
     * Get the icon of the feature row with the provided geometry type,
     * searching in order: feature geometry type icon, feature default icon,
     * table geometry type icon, table default icon
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     * @return icon row
     */
    public IconRow getIcon(FeatureRow featureRow, GeometryType geometryType) {
        return getIcon(featureRow.getId(), geometryType);
    }

    /**
     * Get the default icon of the feature row, searching in order: feature
     * default icon, table default icon
     *
     * @param featureRow feature row
     * @return icon row
     */
    public IconRow getIconDefault(FeatureRow featureRow) {
        return getIcon(featureRow.getId(), null);
    }

    /**
     * Get the icon of the feature, searching in order: feature geometry type
     * icon, feature default icon, table geometry type icon, table default icon
     *
     * @param featureId    feature id
     * @param geometryType geometry type
     * @return icon row
     */
    public IconRow getIcon(long featureId, GeometryType geometryType) {

        IconRow iconRow = featureStyleExtension.getIcon(tableName, featureId,
                geometryType, false);

        if (iconRow == null) {

            // Table Icon
            Icons icons = getCachedTableIcons();
            if (icons != null) {
                iconRow = icons.getIcon(geometryType);
            }

        }

        return iconRow;
    }

    /**
     * Get the default icon of the feature, searching in order: feature default
     * icon, table default icon
     *
     * @param featureId feature id
     * @return icon row
     */
    public IconRow getIconDefault(long featureId) {
        return getIcon(featureId, null);
    }

    /**
     * Set the feature table default feature styles
     *
     * @param featureStyles default feature styles
     */
    public void setTableFeatureStyles(FeatureStyles featureStyles) {
        featureStyleExtension.setTableFeatureStyles(tableName, featureStyles);
        clearCachedTableFeatureStyles();
    }

    /**
     * Set the feature table default styles
     *
     * @param styles default styles
     */
    public void setTableStyles(Styles styles) {
        featureStyleExtension.setTableStyles(tableName, styles);
        clearCachedTableStyles();
    }

    /**
     * Set the feature table style default
     *
     * @param style style row
     */
    public void setTableStyleDefault(StyleRow style) {
        featureStyleExtension.setTableStyleDefault(tableName, style);
        clearCachedTableStyles();
    }

    /**
     * Set the feature table style for the geometry type
     *
     * @param geometryType geometry type
     * @param style        style row
     */
    public void setTableStyle(GeometryType geometryType, StyleRow style) {
        featureStyleExtension.setTableStyle(tableName, geometryType, style);
        clearCachedTableStyles();
    }

    /**
     * Set the feature table default icons
     *
     * @param icons default icons
     */
    public void setTableIcons(Icons icons) {
        featureStyleExtension.setTableIcons(tableName, icons);
        clearCachedTableIcons();
    }

    /**
     * Set the feature table icon default
     *
     * @param icon icon row
     */
    public void setTableIconDefault(IconRow icon) {
        featureStyleExtension.setTableIconDefault(tableName, icon);
        clearCachedTableIcons();
    }

    /**
     * Set the feature table icon for the geometry type
     *
     * @param geometryType geometry type
     * @param icon         icon row
     */
    public void setTableIcon(GeometryType geometryType, IconRow icon) {
        featureStyleExtension.setTableIcon(tableName, geometryType, icon);
        clearCachedTableIcons();
    }

    /**
     * Set the feature styles for the feature row
     *
     * @param featureRow    feature row
     * @param featureStyles feature styles
     */
    public void setFeatureStyles(FeatureRow featureRow,
                                 FeatureStyles featureStyles) {
        featureStyleExtension.setFeatureStyles(featureRow, featureStyles);
    }

    /**
     * Set the feature styles for the feature table and feature id
     *
     * @param featureId     feature id
     * @param featureStyles feature styles
     */
    public void setFeatureStyles(long featureId, FeatureStyles featureStyles) {
        featureStyleExtension.setFeatureStyles(tableName, featureId,
                featureStyles);
    }

    /**
     * Set the feature style (style and icon) of the feature row
     *
     * @param featureRow   feature row
     * @param featureStyle feature style
     */
    public void setFeatureStyle(FeatureRow featureRow, FeatureStyle featureStyle) {
        featureStyleExtension.setFeatureStyle(featureRow, featureStyle);
    }

    /**
     * Set the feature style (style and icon) of the feature row for the
     * specified geometry type
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     * @param featureStyle feature style
     */
    public void setFeatureStyle(FeatureRow featureRow,
                                GeometryType geometryType, FeatureStyle featureStyle) {
        featureStyleExtension.setFeatureStyle(featureRow, geometryType,
                featureStyle);
    }

    /**
     * Set the feature style default (style and icon) of the feature row
     *
     * @param featureRow   feature row
     * @param featureStyle feature style
     */
    public void setFeatureStyleDefault(FeatureRow featureRow,
                                       FeatureStyle featureStyle) {
        featureStyleExtension.setFeatureStyleDefault(featureRow, featureStyle);
    }

    /**
     * Set the feature style (style and icon) of the feature
     *
     * @param featureId    feature id
     * @param geometryType geometry type
     * @param featureStyle feature style
     */
    public void setFeatureStyle(long featureId, GeometryType geometryType,
                                FeatureStyle featureStyle) {
        featureStyleExtension.setFeatureStyle(tableName, featureId,
                geometryType, featureStyle);
    }

    /**
     * Set the feature style (style and icon) of the feature
     *
     * @param featureId    feature id
     * @param featureStyle feature style
     */
    public void setFeatureStyleDefault(long featureId, FeatureStyle featureStyle) {
        featureStyleExtension.setFeatureStyleDefault(tableName, featureId,
                featureStyle);
    }

    /**
     * Set the styles for the feature row
     *
     * @param featureRow feature row
     * @param styles     styles
     */
    public void setStyles(FeatureRow featureRow, Styles styles) {
        featureStyleExtension.setStyles(featureRow, styles);
    }

    /**
     * Set the styles for the feature table and feature id
     *
     * @param featureId feature id
     * @param styles    styles
     */
    public void setStyles(long featureId, Styles styles) {
        featureStyleExtension.setStyles(tableName, featureId, styles);
    }

    /**
     * Set the style of the feature row
     *
     * @param featureRow feature row
     * @param style      style row
     */
    public void setStyle(FeatureRow featureRow, StyleRow style) {
        featureStyleExtension.setStyle(featureRow, style);
    }

    /**
     * Set the style of the feature row for the specified geometry type
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     * @param style        style row
     */
    public void setStyle(FeatureRow featureRow, GeometryType geometryType,
                         StyleRow style) {
        featureStyleExtension.setStyle(featureRow, geometryType, style);
    }

    /**
     * Set the default style of the feature row
     *
     * @param featureRow feature row
     * @param style      style row
     */
    public void setStyleDefault(FeatureRow featureRow, StyleRow style) {
        featureStyleExtension.setStyleDefault(featureRow, style);
    }

    /**
     * Set the style of the feature
     *
     * @param featureId    feature id
     * @param geometryType geometry type
     * @param style        style row
     */
    public void setStyle(long featureId, GeometryType geometryType,
                         StyleRow style) {
        featureStyleExtension.setStyle(tableName, featureId, geometryType,
                style);
    }

    /**
     * Set the default style of the feature
     *
     * @param featureId feature id
     * @param style     style row
     */
    public void setStyleDefault(long featureId, StyleRow style) {
        featureStyleExtension.setStyleDefault(tableName, featureId, style);
    }

    /**
     * Set the icons for the feature row
     *
     * @param featureRow feature row
     * @param icons      icons
     */
    public void setIcons(FeatureRow featureRow, Icons icons) {
        featureStyleExtension.setIcons(featureRow, icons);
    }

    /**
     * Set the icons for the feature table and feature id
     *
     * @param featureId feature id
     * @param icons     icons
     */
    public void setIcons(long featureId, Icons icons) {
        featureStyleExtension.setIcons(tableName, featureId, icons);
    }

    /**
     * Set the icon of the feature row
     *
     * @param featureRow feature row
     * @param icon       icon row
     */
    public void setIcon(FeatureRow featureRow, IconRow icon) {
        featureStyleExtension.setIcon(featureRow, icon);
    }

    /**
     * Set the icon of the feature row for the specified geometry type
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     * @param icon         icon row
     */
    public void setIcon(FeatureRow featureRow, GeometryType geometryType,
                        IconRow icon) {
        featureStyleExtension.setIcon(featureRow, geometryType, icon);
    }

    /**
     * Set the default icon of the feature row
     *
     * @param featureRow feature row
     * @param icon       icon row
     */
    public void setIconDefault(FeatureRow featureRow, IconRow icon) {
        featureStyleExtension.setIconDefault(featureRow, icon);
    }

    /**
     * Get the icon of the feature, searching in order: feature geometry type
     * icon, feature default icon, table geometry type icon, table default icon
     *
     * @param featureId    feature id
     * @param geometryType geometry type
     * @param icon         icon row
     */
    public void setIcon(long featureId, GeometryType geometryType, IconRow icon) {
        featureStyleExtension.setIcon(tableName, featureId, geometryType, icon);
    }

    /**
     * Set the default icon of the feature
     *
     * @param featureId feature id
     * @param icon      icon row
     */
    public void setIconDefault(long featureId, IconRow icon) {
        featureStyleExtension.setIconDefault(tableName, featureId, icon);
    }

    /**
     * Delete all feature styles including table styles, table icons, style, and
     * icons
     */
    public void deleteAllFeatureStyles() {
        featureStyleExtension.deleteAllFeatureStyles(tableName);
        clearCachedTableFeatureStyles();
    }

    /**
     * Delete all styles including table styles and feature row styles
     */
    public void deleteAllStyles() {
        featureStyleExtension.deleteAllStyles(tableName);
        clearCachedTableStyles();
    }

    /**
     * Delete all icons including table icons and feature row icons
     */
    public void deleteAllIcons() {
        featureStyleExtension.deleteAllIcons(tableName);
        clearCachedTableIcons();
    }

    /**
     * Delete the feature table feature styles
     */
    public void deleteTableFeatureStyles() {
        featureStyleExtension.deleteTableFeatureStyles(tableName);
        clearCachedTableFeatureStyles();
    }

    /**
     * Delete the feature table styles
     */
    public void deleteTableStyles() {
        featureStyleExtension.deleteTableStyles(tableName);
        clearCachedTableStyles();
    }

    /**
     * Delete the feature table default style
     */
    public void deleteTableStyleDefault() {
        featureStyleExtension.deleteTableStyleDefault(tableName);
        clearCachedTableStyles();
    }

    /**
     * Delete the feature table style for the geometry type
     *
     * @param geometryType geometry type
     */
    public void deleteTableStyle(GeometryType geometryType) {
        featureStyleExtension.deleteTableStyle(tableName, geometryType);
        clearCachedTableStyles();
    }

    /**
     * Delete the feature table icons
     */
    public void deleteTableIcons() {
        featureStyleExtension.deleteTableIcons(tableName);
        clearCachedTableIcons();
    }

    /**
     * Delete the feature table default icon
     */
    public void deleteTableIconDefault() {
        featureStyleExtension.deleteTableIconDefault(tableName);
        clearCachedTableIcons();
    }

    /**
     * Delete the feature table icon for the geometry type
     *
     * @param geometryType geometry type
     */
    public void deleteTableIcon(GeometryType geometryType) {
        featureStyleExtension.deleteTableIcon(tableName, geometryType);
        clearCachedTableIcons();
    }

    /**
     * Clear the cached table feature styles
     */
    public void clearCachedTableFeatureStyles() {
        synchronized (cachedTableFeatureStyles) {
            cachedTableFeatureStyles.setStyles(null);
            cachedTableFeatureStyles.setIcons(null);
        }
    }

    /**
     * Clear the cached table styles
     */
    public void clearCachedTableStyles() {
        synchronized (cachedTableFeatureStyles) {
            cachedTableFeatureStyles.setStyles(null);
        }
    }

    /**
     * Clear the cached table icons
     */
    public void clearCachedTableIcons() {
        synchronized (cachedTableFeatureStyles) {
            cachedTableFeatureStyles.setIcons(null);
        }
    }

    /**
     * Delete all feature styles
     */
    public void deleteFeatureStyles() {
        featureStyleExtension.deleteFeatureStyles(tableName);
    }

    /**
     * Delete all styles
     */
    public void deleteStyles() {
        featureStyleExtension.deleteStyles(tableName);
    }

    /**
     * Delete feature row styles
     *
     * @param featureRow feature row
     */
    public void deleteStyles(FeatureRow featureRow) {
        featureStyleExtension.deleteStyles(featureRow);
    }

    /**
     * Delete feature row styles
     *
     * @param featureId feature id
     */
    public void deleteStyles(long featureId) {
        featureStyleExtension.deleteStyles(tableName, featureId);
    }

    /**
     * Delete the feature row default style
     *
     * @param featureRow feature row
     */
    public void deleteStyleDefault(FeatureRow featureRow) {
        featureStyleExtension.deleteStyleDefault(featureRow);
    }

    /**
     * Delete the feature row default style
     *
     * @param featureId feature id
     */
    public void deleteStyleDefault(long featureId) {
        featureStyleExtension.deleteStyleDefault(tableName, featureId);
    }

    /**
     * Delete the feature row style for the feature row geometry type
     *
     * @param featureRow feature row
     */
    public void deleteStyle(FeatureRow featureRow) {
        featureStyleExtension.deleteStyle(featureRow);
    }

    /**
     * Delete the feature row style for the geometry type
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     */
    public void deleteStyle(FeatureRow featureRow, GeometryType geometryType) {
        featureStyleExtension.deleteStyle(featureRow, geometryType);
    }

    /**
     * Delete the feature row style for the geometry type
     *
     * @param featureId    feature id
     * @param geometryType geometry type
     */
    public void deleteStyle(long featureId, GeometryType geometryType) {
        featureStyleExtension.deleteStyle(tableName, featureId, geometryType);
    }

    /**
     * Delete all icons
     */
    public void deleteIcons() {
        featureStyleExtension.deleteIcons(tableName);
    }

    /**
     * Delete feature row icons
     *
     * @param featureRow feature row
     */
    public void deleteIcons(FeatureRow featureRow) {
        featureStyleExtension.deleteIcons(featureRow);
    }

    /**
     * Delete feature row icons
     *
     * @param featureId feature id
     */
    public void deleteIcons(long featureId) {
        featureStyleExtension.deleteIcons(tableName, featureId);
    }

    /**
     * Delete the feature row default icon
     *
     * @param featureRow feature row
     */
    public void deleteIconDefault(FeatureRow featureRow) {
        featureStyleExtension.deleteIconDefault(featureRow);
    }

    /**
     * Delete the feature row default icon
     *
     * @param featureId feature id
     */
    public void deleteIconDefault(long featureId) {
        featureStyleExtension.deleteIconDefault(tableName, featureId);
    }

    /**
     * Delete the feature row icon for the feature row geometry type
     *
     * @param featureRow feature row
     */
    public void deleteIcon(FeatureRow featureRow) {
        featureStyleExtension.deleteIcon(featureRow);
    }

    /**
     * Delete the feature row icon for the geometry type
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     */
    public void deleteIcon(FeatureRow featureRow, GeometryType geometryType) {
        featureStyleExtension.deleteIcon(featureRow, geometryType);
    }

    /**
     * Delete the feature row icon for the geometry type
     *
     * @param featureId    feature id
     * @param geometryType geometry type
     */
    public void deleteIcon(long featureId, GeometryType geometryType) {
        featureStyleExtension.deleteIcon(tableName, featureId, geometryType);
    }

    /**
     * Get all the unique style row ids the table maps to
     *
     * @return style row ids
     */
    public List<Long> getAllTableStyleIds() {
        return featureStyleExtension.getAllTableStyleIds(tableName);
    }

    /**
     * Get all the unique icon row ids the table maps to
     *
     * @return icon row ids
     */
    public List<Long> getAllTableIconIds() {
        return featureStyleExtension.getAllTableIconIds(tableName);
    }

    /**
     * Get all the unique style row ids the features map to
     *
     * @return style row ids
     */
    public List<Long> getAllStyleIds() {
        return featureStyleExtension.getAllStyleIds(tableName);
    }

    /**
     * Get all the unique icon row ids the features map to
     *
     * @return icon row ids
     */
    public List<Long> getAllIconIds() {
        return featureStyleExtension.getAllIconIds(tableName);
    }

    /**
     * Calculate style pixel bounds
     *
     * @return pixel bounds
     * @since 6.3.0
     */
    public PixelBounds calculatePixelBounds() {
        return calculatePixelBounds(1.0f);
    }

    /**
     * Calculate style pixel bounds
     *
     * @param density display density: {@link android.util.DisplayMetrics#density}
     * @return pixel bounds
     * @since 6.3.0
     */
    public PixelBounds calculatePixelBounds(float density) {
        return featureStyleExtension.calculatePixelBounds(tableName, density);
    }

}
