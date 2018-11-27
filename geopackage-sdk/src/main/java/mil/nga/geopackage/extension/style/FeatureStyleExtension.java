package mil.nga.geopackage.extension.style;

import java.util.List;
import java.util.Map.Entry;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.sf.GeometryType;

/**
 * Feature Style extension for styling features
 *
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureStyleExtension extends FeatureCoreStyleExtension {

    /**
     * Related Tables extension
     */
    protected final RelatedTablesExtension relatedTables;

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     */
    public FeatureStyleExtension(GeoPackage geoPackage) {
        super(geoPackage, new RelatedTablesExtension(geoPackage));
        this.relatedTables = (RelatedTablesExtension) super.getRelatedTables();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoPackage getGeoPackage() {
        return (GeoPackage) geoPackage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelatedTablesExtension getRelatedTables() {
        return relatedTables;
    }

    /**
     * Get a Style Mapping DAO
     *
     * @param featureTable feature table
     * @return style mapping DAO
     */
    public StyleMappingDao getStyleMappingDao(String featureTable) {
        return getMappingDao(TABLE_MAPPING_STYLE, featureTable);
    }

    /**
     * Get a Table Style Mapping DAO
     *
     * @param featureTable feature table
     * @return table style mapping DAO
     */
    public StyleMappingDao getTableStyleMappingDao(String featureTable) {
        return getMappingDao(TABLE_MAPPING_TABLE_STYLE, featureTable);
    }

    /**
     * Get a Icon Mapping DAO
     *
     * @param featureTable feature table
     * @return icon mapping DAO
     */
    public StyleMappingDao getIconMappingDao(String featureTable) {
        return getMappingDao(TABLE_MAPPING_ICON, featureTable);
    }

    /**
     * Get a Table Icon Mapping DAO
     *
     * @param featureTable feature table
     * @return table icon mapping DAO
     */
    public StyleMappingDao getTableIconMappingDao(String featureTable) {
        return getMappingDao(TABLE_MAPPING_TABLE_ICON, featureTable);
    }

    /**
     * Get a Style Mapping DAO from a table name
     *
     * @param tablePrefix  table name prefix
     * @param featureTable feature table
     * @return style mapping dao
     */
    private StyleMappingDao getMappingDao(String tablePrefix,
                                          String featureTable) {
        String tableName = tablePrefix + featureTable;
        StyleMappingDao dao = null;
        if (geoPackage.isTable(tableName)) {
            dao = new StyleMappingDao(relatedTables.getUserDao(tableName));
        }
        return dao;
    }

    /**
     * Get a style DAO
     *
     * @return style DAO
     */
    public StyleDao getStyleDao() {
        StyleDao styleDao = null;
        if (geoPackage.isTable(StyleTable.TABLE_NAME)) {
            AttributesDao attributesDao = getGeoPackage().getAttributesDao(
                    StyleTable.TABLE_NAME);
            styleDao = new StyleDao(attributesDao);
            relatedTables.setContents(styleDao.getTable());
        }
        return styleDao;
    }

    /**
     * Get a icon DAO
     *
     * @return icon DAO
     */
    public IconDao getIconDao() {
        IconDao iconDao = null;
        if (geoPackage.isTable(IconTable.TABLE_NAME)) {
            iconDao = new IconDao(
                    relatedTables.getUserDao(IconTable.TABLE_NAME));
            relatedTables.setContents(iconDao.getTable());
        }
        return iconDao;
    }

    /**
     * Get the feature table default feature styles
     *
     * @param featureTable feature table
     * @return table feature styles or null
     */
    public FeatureStyles getTableFeatureStyles(FeatureTable featureTable) {
        return getTableFeatureStyles(featureTable.getTableName());
    }

    /**
     * Get the feature table default feature styles
     *
     * @param featureTable feature table
     * @return table feature styles or null
     */
    public FeatureStyles getTableFeatureStyles(String featureTable) {

        FeatureStyles featureStyles = null;

        Long id = contentsId.getId(featureTable);
        if (id != null) {

            Styles styles = getTableStyles(featureTable, id);
            Icons icons = getTableIcons(featureTable, id);

            if (styles != null || icons != null) {
                featureStyles = new FeatureStyles(styles, icons);
            }

        }

        return featureStyles;
    }

    /**
     * Get the feature table default styles
     *
     * @param featureTable feature table
     * @return table styles or null
     */
    public Styles getTableStyles(FeatureTable featureTable) {
        return getTableStyles(featureTable.getTableName());
    }

    /**
     * Get the feature table default styles
     *
     * @param featureTable feature table
     * @return table styles or null
     */
    public Styles getTableStyles(String featureTable) {
        Styles styles = null;
        Long id = contentsId.getId(featureTable);
        if (id != null) {
            styles = getTableStyles(featureTable, id);
        }
        return styles;
    }

    /**
     * Get the feature table default styles
     *
     * @param featureTable feature table
     * @param contentsId   contents id
     * @return table styles or null
     */
    private Styles getTableStyles(String featureTable, long contentsId) {
        return getStyles(contentsId, getTableStyleMappingDao(featureTable));
    }

    /**
     * Get the style of the feature table and geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     * @return style row
     */
    public StyleRow getTableStyle(String featureTable, GeometryType geometryType) {
        StyleRow styleRow = null;
        Styles tableStyles = getTableStyles(featureTable);
        if (tableStyles != null) {
            styleRow = tableStyles.getStyle(geometryType);
        }
        return styleRow;
    }

    /**
     * Get the default style of the feature table
     *
     * @param featureTable feature table
     * @return style row
     */
    public StyleRow getTableStyleDefault(String featureTable) {
        return getTableStyle(featureTable, null);
    }

    /**
     * Get the feature table default icons
     *
     * @param featureTable feature table
     * @return table icons or null
     */
    public Icons getTableIcons(FeatureTable featureTable) {
        return getTableIcons(featureTable.getTableName());
    }

    /**
     * Get the feature table default icons
     *
     * @param featureTable feature table
     * @return table icons or null
     */
    public Icons getTableIcons(String featureTable) {
        Icons icons = null;
        Long id = contentsId.getId(featureTable);
        if (id != null) {
            icons = getTableIcons(featureTable, id);
        }
        return icons;
    }

    /**
     * Get the feature table default icons
     *
     * @param featureTable feature table
     * @param contentsId   contents id
     * @return table icons or null
     */
    private Icons getTableIcons(String featureTable, long contentsId) {
        return getIcons(contentsId, getTableIconMappingDao(featureTable));
    }

    /**
     * Get the default icon of the feature table
     *
     * @param featureTable feature table
     * @return icon row
     */
    public IconRow getTableIconDefault(String featureTable) {
        return getTableIcon(featureTable, null);
    }

    /**
     * Get the icon of the feature table and geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     * @return icon row
     */
    public IconRow getTableIcon(String featureTable, GeometryType geometryType) {
        IconRow iconRow = null;
        Icons tableIcons = getTableIcons(featureTable);
        if (tableIcons != null) {
            iconRow = tableIcons.getIcon(geometryType);
        }
        return iconRow;
    }

    /**
     * Get the feature styles for the feature row
     *
     * @param featureRow feature row
     * @return feature styles or null
     */
    public FeatureStyles getFeatureStyles(FeatureRow featureRow) {
        return getFeatureStyles(featureRow.getTable().getTableName(),
                featureRow.getId());
    }

    /**
     * Get the feature styles for the feature table and feature id
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @return feature styles or null
     */
    public FeatureStyles getFeatureStyles(String featureTable, long featureId) {

        Styles styles = getStyles(featureTable, featureId);
        Icons icons = getIcons(featureTable, featureId);

        FeatureStyles featureStyles = null;
        if (styles != null || icons != null) {
            featureStyles = new FeatureStyles(styles, icons);
        }

        return featureStyles;
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
        return getFeatureStyle(featureRow.getTable().getTableName(),
                featureRow.getId(), geometryType);
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
        return getFeatureStyle(featureRow.getTable().getTableName(),
                featureRow.getId(), null);
    }

    /**
     * Get the feature style (style and icon) of the feature, searching in
     * order: feature geometry type style or icon, feature default style or
     * icon, table geometry type style or icon, table default style or icon
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     * @return feature style
     */
    public FeatureStyle getFeatureStyle(String featureTable, long featureId,
                                        GeometryType geometryType) {

        FeatureStyle featureStyle = null;

        StyleRow style = getStyle(featureTable, featureId, geometryType);
        IconRow icon = getIcon(featureTable, featureId, geometryType);

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
     * @param featureTable feature table
     * @param featureId    feature id
     * @return feature style
     */
    public FeatureStyle getFeatureStyleDefault(String featureTable,
                                               long featureId) {
        return getFeatureStyle(featureTable, featureId, null);
    }

    /**
     * Get the styles for the feature row
     *
     * @param featureRow feature row
     * @return styles or null
     */
    public Styles getStyles(FeatureRow featureRow) {
        return getStyles(featureRow.getTable().getTableName(),
                featureRow.getId());
    }

    /**
     * Get the styles for the feature table and feature id
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @return styles or null
     */
    public Styles getStyles(String featureTable, long featureId) {
        return getStyles(featureId, getStyleMappingDao(featureTable));
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
        return getStyle(featureRow.getTable().getTableName(),
                featureRow.getId(), geometryType);
    }

    /**
     * Get the default style of the feature row, searching in order: feature
     * default style, table default style
     *
     * @param featureRow feature row
     * @return style row
     */
    public StyleRow getStyleDefault(FeatureRow featureRow) {
        return getStyle(featureRow.getTable().getTableName(),
                featureRow.getId(), null);
    }

    /**
     * Get the style of the feature, searching in order: feature geometry type
     * style, feature default style, table geometry type style, table default
     * style
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     * @return style row
     */
    public StyleRow getStyle(String featureTable, long featureId,
                             GeometryType geometryType) {
        return getStyle(featureTable, featureId, geometryType, true);
    }

    /**
     * Get the default style of the feature, searching in order: feature default
     * style, table default style
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @return style row
     */
    public StyleRow getStyleDefault(String featureTable, long featureId) {
        return getStyle(featureTable, featureId, null, true);
    }

    /**
     * Get the style of the feature, searching in order: feature geometry type
     * style, feature default style, when tableStyle enabled continue searching:
     * table geometry type style, table default style
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     * @param tableStyle   when true and a feature style is not found, query for a
     *                     matching table style
     * @return style row
     */
    public StyleRow getStyle(String featureTable, long featureId,
                             GeometryType geometryType, boolean tableStyle) {

        StyleRow styleRow = null;

        // Feature Style
        Styles styles = getStyles(featureTable, featureId);
        if (styles != null) {
            styleRow = styles.getStyle(geometryType);
        }

        if (styleRow == null && tableStyle) {

            // Table Style
            styleRow = getTableStyle(featureTable, geometryType);

        }

        return styleRow;
    }

    /**
     * Get the default style of the feature, searching in order: feature default
     * style, when tableStyle enabled continue searching: table default style
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param tableStyle   when true and a feature style is not found, query for a
     *                     matching table style
     * @return style row
     */
    public StyleRow getStyleDefault(String featureTable, long featureId,
                                    boolean tableStyle) {
        return getStyle(featureTable, featureId, null, tableStyle);
    }

    /**
     * Get the icons for the feature row
     *
     * @param featureRow feature row
     * @return icons or null
     */
    public Icons getIcons(FeatureRow featureRow) {
        return getIcons(featureRow.getTable().getTableName(),
                featureRow.getId());
    }

    /**
     * Get the icons for the feature table and feature id
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @return icons or null
     */
    public Icons getIcons(String featureTable, long featureId) {
        return getIcons(featureId, getIconMappingDao(featureTable));
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
        return getIcon(featureRow.getTable().getTableName(),
                featureRow.getId(), geometryType);
    }

    /**
     * Get the default icon of the feature row, searching in order: feature
     * default icon, table default icon
     *
     * @param featureRow feature row
     * @return icon row
     */
    public IconRow getIconDefault(FeatureRow featureRow) {
        return getIcon(featureRow.getTable().getTableName(),
                featureRow.getId(), null);
    }

    /**
     * Get the icon of the feature, searching in order: feature geometry type
     * icon, feature default icon, table geometry type icon, table default icon
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     * @return icon row
     */
    public IconRow getIcon(String featureTable, long featureId,
                           GeometryType geometryType) {
        return getIcon(featureTable, featureId, geometryType, true);
    }

    /**
     * Get the default icon of the feature, searching in order: feature default
     * icon, table default icon
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @return icon row
     */
    public IconRow getIconDefault(String featureTable, long featureId) {
        return getIcon(featureTable, featureId, null, true);
    }

    /**
     * Get the icon of the feature, searching in order: feature geometry type
     * icon, feature default icon, when tableIcon enabled continue searching:
     * table geometry type icon, table default icon
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     * @param tableIcon    when true and a feature icon is not found, query for a
     *                     matching table icon
     * @return icon row
     */
    public IconRow getIcon(String featureTable, long featureId,
                           GeometryType geometryType, boolean tableIcon) {

        IconRow iconRow = null;

        // Feature Icon
        Icons icons = getIcons(featureTable, featureId);
        if (icons != null) {
            iconRow = icons.getIcon(geometryType);
        }

        if (iconRow == null && tableIcon) {

            // Table Icon
            iconRow = getTableIcon(featureTable, geometryType);

        }

        return iconRow;
    }

    /**
     * Get the default icon of the feature, searching in order: feature default
     * icon, when tableIcon enabled continue searching: table default icon
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param tableIcon    when true and a feature icon is not found, query for a
     *                     matching table icon
     * @return icon row
     */
    public IconRow getIconDefault(String featureTable, long featureId,
                                  boolean tableIcon) {
        return getIcon(featureTable, featureId, null, tableIcon);
    }

    /**
     * Get the styles for feature id from the style mapping dao
     *
     * @param featureId  geometry feature id or feature table id
     * @param mappingDao style mapping dao
     * @return styles
     */
    private Styles getStyles(long featureId, StyleMappingDao mappingDao) {

        Styles styles = null;

        if (mappingDao != null) {

            StyleDao styleDao = getStyleDao();

            if (styleDao != null) {

                List<StyleMappingRow> styleMappingRows = mappingDao
                        .queryByBaseFeatureId(featureId);
                if (!styleMappingRows.isEmpty()) {

                    for (StyleMappingRow styleMappingRow : styleMappingRows) {

                        StyleRow styleRow = styleDao
                                .queryForRow(styleMappingRow);
                        if (styleRow != null) {
                            if (styles == null) {
                                styles = new Styles();
                            }
                            styles.setStyle(styleRow,
                                    styleMappingRow.getGeometryType());
                        }
                    }
                }
            }
        }

        return styles;
    }

    /**
     * Get the icons for feature id from the icon mapping dao
     *
     * @param featureId  geometry feature id or feature table id
     * @param mappingDao icon mapping dao
     * @return icons
     */
    private Icons getIcons(long featureId, StyleMappingDao mappingDao) {

        Icons icons = null;

        if (mappingDao != null) {

            IconDao iconDao = getIconDao();
            if (iconDao != null) {

                List<StyleMappingRow> styleMappingRows = mappingDao
                        .queryByBaseFeatureId(featureId);
                if (!styleMappingRows.isEmpty()) {

                    for (StyleMappingRow styleMappingRow : styleMappingRows) {

                        IconRow iconRow = iconDao.queryForRow(styleMappingRow);
                        if (iconRow != null) {
                            if (icons == null) {
                                icons = new Icons();
                            }
                            icons.setIcon(iconRow,
                                    styleMappingRow.getGeometryType());
                        }
                    }
                }
            }
        }

        return icons;
    }

    /**
     * Set the feature table default feature styles
     *
     * @param featureTable  feature table
     * @param featureStyles default feature styles
     */
    public void setTableFeatureStyles(FeatureTable featureTable,
                                      FeatureStyles featureStyles) {
        setTableFeatureStyles(featureTable.getTableName(), featureStyles);
    }

    /**
     * Set the feature table default feature styles
     *
     * @param featureTable  feature table
     * @param featureStyles default feature styles
     */
    public void setTableFeatureStyles(String featureTable,
                                      FeatureStyles featureStyles) {
        if (featureStyles != null) {
            setTableStyles(featureTable, featureStyles.getStyles());
            setTableIcons(featureTable, featureStyles.getIcons());
        } else {
            deleteTableFeatureStyles(featureTable);
        }
    }

    /**
     * Set the feature table default styles
     *
     * @param featureTable feature table
     * @param styles       default styles
     */
    public void setTableStyles(FeatureTable featureTable, Styles styles) {
        setTableStyles(featureTable.getTableName(), styles);
    }

    /**
     * Set the feature table default styles
     *
     * @param featureTable feature table
     * @param styles       default styles
     */
    public void setTableStyles(String featureTable, Styles styles) {

        deleteTableStyles(featureTable);

        if (styles != null) {

            if (styles.getDefault() != null) {
                setTableStyleDefault(featureTable, styles.getDefault());
            }

            for (Entry<GeometryType, StyleRow> style : styles.getStyles()
                    .entrySet()) {
                setTableStyle(featureTable, style.getKey(), style.getValue());
            }

        }
    }

    /**
     * Set the feature table style default
     *
     * @param featureTable feature table
     * @param style        style row
     */
    public void setTableStyleDefault(FeatureTable featureTable, StyleRow style) {
        setTableStyleDefault(featureTable.getTableName(), style);
    }

    /**
     * Set the feature table style default
     *
     * @param featureTable feature table
     * @param style        style row
     */
    public void setTableStyleDefault(String featureTable, StyleRow style) {
        setTableStyle(featureTable, null, style);
    }

    /**
     * Set the feature table style for the geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     * @param style        style row
     */
    public void setTableStyle(FeatureTable featureTable,
                              GeometryType geometryType, StyleRow style) {
        setTableStyle(featureTable.getTableName(), geometryType, style);
    }

    /**
     * Set the feature table style for the geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     * @param style        style row
     */
    public void setTableStyle(String featureTable, GeometryType geometryType,
                              StyleRow style) {

        deleteTableStyle(featureTable, geometryType);

        if (style != null) {

            createTableStyleRelationship(featureTable);

            long featureContentsId = contentsId.getOrCreateId(featureTable);

            long styleId = getOrInsertStyle(style);

            StyleMappingDao mappingDao = getTableStyleMappingDao(featureTable);
            insertStyleMapping(mappingDao, featureContentsId, styleId,
                    geometryType);

        }

    }

    /**
     * Set the feature table default icons
     *
     * @param featureTable feature table
     * @param icons        default icons
     */
    public void setTableIcon(FeatureTable featureTable, Icons icons) {
        setTableIcons(featureTable.getTableName(), icons);
    }

    /**
     * Set the feature table default icons
     *
     * @param featureTable feature table
     * @param icons        default icons
     */
    public void setTableIcons(String featureTable, Icons icons) {

        deleteTableIcons(featureTable);

        if (icons != null) {

            if (icons.getDefault() != null) {
                setTableIconDefault(featureTable, icons.getDefault());
            }

            for (Entry<GeometryType, IconRow> icon : icons.getIcons()
                    .entrySet()) {
                setTableIcon(featureTable, icon.getKey(), icon.getValue());
            }

        }

    }

    /**
     * Set the feature table icon default
     *
     * @param featureTable feature table
     * @param icon         icon row
     */
    public void setTableIconDefault(FeatureTable featureTable, IconRow icon) {
        setTableIconDefault(featureTable.getTableName(), icon);
    }

    /**
     * Set the feature table icon default
     *
     * @param featureTable feature table
     * @param icon         icon row
     */
    public void setTableIconDefault(String featureTable, IconRow icon) {
        setTableIcon(featureTable, null, icon);
    }

    /**
     * Set the feature table icon for the geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     * @param icon         icon row
     */
    public void setTableIcon(FeatureTable featureTable,
                             GeometryType geometryType, IconRow icon) {
        setTableIcon(featureTable.getTableName(), geometryType, icon);
    }

    /**
     * Set the feature table icon for the geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     * @param icon         icon row
     */
    public void setTableIcon(String featureTable, GeometryType geometryType,
                             IconRow icon) {

        deleteTableIcon(featureTable, geometryType);

        if (icon != null) {

            createTableIconRelationship(featureTable);

            long featureContentsId = contentsId.getOrCreateId(featureTable);

            long iconId = getOrInsertIcon(icon);

            StyleMappingDao mappingDao = getTableIconMappingDao(featureTable);
            insertStyleMapping(mappingDao, featureContentsId, iconId,
                    geometryType);

        }
    }

    /**
     * Set the feature styles for the feature row
     *
     * @param featureRow    feature row
     * @param featureStyles feature styles
     */
    public void setFeatureStyles(FeatureRow featureRow,
                                 FeatureStyles featureStyles) {
        setFeatureStyles(featureRow.getTable().getTableName(),
                featureRow.getId(), featureStyles);
    }

    /**
     * Set the feature styles for the feature table and feature id
     *
     * @param featureTable  feature table
     * @param featureId     feature id
     * @param featureStyles feature styles
     */
    public void setFeatureStyles(String featureTable, long featureId,
                                 FeatureStyles featureStyles) {
        if (featureStyles != null) {
            setStyles(featureTable, featureId, featureStyles.getStyles());
            setIcons(featureTable, featureId, featureStyles.getIcons());
        } else {
            deleteStyles(featureTable, featureId);
            deleteIcons(featureTable, featureId);
        }
    }

    /**
     * Set the feature style (style and icon) of the feature row
     *
     * @param featureRow   feature row
     * @param featureStyle feature style
     */
    public void setFeatureStyle(FeatureRow featureRow, FeatureStyle featureStyle) {
        setFeatureStyle(featureRow, featureRow.getGeometryType(), featureStyle);
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
        setFeatureStyle(featureRow.getTable().getTableName(),
                featureRow.getId(), geometryType, featureStyle);
    }

    /**
     * Set the feature style default (style and icon) of the feature row
     *
     * @param featureRow   feature row
     * @param featureStyle feature style
     */
    public void setFeatureStyleDefault(FeatureRow featureRow,
                                       FeatureStyle featureStyle) {
        setFeatureStyle(featureRow.getTable().getTableName(),
                featureRow.getId(), null, featureStyle);
    }

    /**
     * Set the feature style (style and icon) of the feature
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     * @param featureStyle feature style
     */
    public void setFeatureStyle(String featureTable, long featureId,
                                GeometryType geometryType, FeatureStyle featureStyle) {
        if (featureStyle != null) {
            setStyle(featureTable, featureId, geometryType,
                    featureStyle.getStyle());
            setIcon(featureTable, featureId, geometryType,
                    featureStyle.getIcon());
        } else {
            deleteStyle(featureTable, featureId, geometryType);
            deleteIcon(featureTable, featureId, geometryType);
        }
    }

    /**
     * Set the feature style (style and icon) of the feature
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param featureStyle feature style
     */
    public void setFeatureStyleDefault(String featureTable, long featureId,
                                       FeatureStyle featureStyle) {
        setFeatureStyle(featureTable, featureId, null, featureStyle);
    }

    /**
     * Set the styles for the feature row
     *
     * @param featureRow feature row
     * @param styles     styles
     */
    public void setStyles(FeatureRow featureRow, Styles styles) {
        setStyles(featureRow.getTable().getTableName(), featureRow.getId(),
                styles);
    }

    /**
     * Set the styles for the feature table and feature id
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param styles       styles
     */
    public void setStyles(String featureTable, long featureId, Styles styles) {
        deleteStyles(featureTable, featureId);

        if (styles != null) {

            if (styles.getDefault() != null) {
                setStyleDefault(featureTable, featureId, styles.getDefault());
            }

            for (Entry<GeometryType, StyleRow> style : styles.getStyles()
                    .entrySet()) {
                setStyle(featureTable, featureId, style.getKey(),
                        style.getValue());
            }

        }
    }

    /**
     * Set the style of the feature row
     *
     * @param featureRow feature row
     * @param style      style row
     */
    public void setStyle(FeatureRow featureRow, StyleRow style) {
        setStyle(featureRow, featureRow.getGeometryType(), style);
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
        setStyle(featureRow.getTable().getTableName(), featureRow.getId(),
                geometryType, style);
    }

    /**
     * Set the default style of the feature row
     *
     * @param featureRow feature row
     * @param style      style row
     */
    public void setStyleDefault(FeatureRow featureRow, StyleRow style) {
        setStyle(featureRow.getTable().getTableName(), featureRow.getId(),
                null, style);
    }

    /**
     * Set the style of the feature
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     * @param style        style row
     */
    public void setStyle(String featureTable, long featureId,
                         GeometryType geometryType, StyleRow style) {
        deleteStyle(featureTable, featureId, geometryType);
        if (style != null) {

            createStyleRelationship(featureTable);

            long styleId = getOrInsertStyle(style);

            StyleMappingDao mappingDao = getStyleMappingDao(featureTable);
            insertStyleMapping(mappingDao, featureId, styleId, geometryType);

        }
    }

    /**
     * Set the default style of the feature
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param style        style row
     */
    public void setStyleDefault(String featureTable, long featureId,
                                StyleRow style) {
        setStyle(featureTable, featureId, null, style);
    }

    /**
     * Set the icons for the feature row
     *
     * @param featureRow feature row
     * @param icons      icons
     */
    public void setIcons(FeatureRow featureRow, Icons icons) {
        setIcons(featureRow.getTable().getTableName(), featureRow.getId(),
                icons);
    }

    /**
     * Set the icons for the feature table and feature id
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param icons        icons
     */
    public void setIcons(String featureTable, long featureId, Icons icons) {
        deleteIcons(featureTable, featureId);

        if (icons != null) {

            if (icons.getDefault() != null) {
                setIconDefault(featureTable, featureId, icons.getDefault());
            }

            for (Entry<GeometryType, IconRow> icon : icons.getIcons()
                    .entrySet()) {
                setIcon(featureTable, featureId, icon.getKey(), icon.getValue());
            }

        }
    }

    /**
     * Set the icon of the feature row
     *
     * @param featureRow feature row
     * @param icon       icon row
     */
    public void setIcon(FeatureRow featureRow, IconRow icon) {
        setIcon(featureRow, featureRow.getGeometryType(), icon);
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
        setIcon(featureRow.getTable().getTableName(), featureRow.getId(),
                geometryType, icon);
    }

    /**
     * Set the default icon of the feature row
     *
     * @param featureRow feature row
     * @param icon       icon row
     */
    public void setIconDefault(FeatureRow featureRow, IconRow icon) {
        setIcon(featureRow.getTable().getTableName(), featureRow.getId(), null,
                icon);
    }

    /**
     * Get the icon of the feature, searching in order: feature geometry type
     * icon, feature default icon, table geometry type icon, table default icon
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     * @param icon         icon row
     */
    public void setIcon(String featureTable, long featureId,
                        GeometryType geometryType, IconRow icon) {
        deleteIcon(featureTable, featureId, geometryType);
        if (icon != null) {

            createIconRelationship(featureTable);

            long iconId = getOrInsertIcon(icon);

            StyleMappingDao mappingDao = getIconMappingDao(featureTable);
            insertStyleMapping(mappingDao, featureId, iconId, geometryType);

        }
    }

    /**
     * Set the default icon of the feature
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param icon         icon row
     */
    public void setIconDefault(String featureTable, long featureId, IconRow icon) {
        setIcon(featureTable, featureId, null, icon);
    }

    /**
     * Get the style id, either from the existing style or by inserting a new
     * one
     *
     * @param style style row
     * @return style id
     */
    private long getOrInsertStyle(StyleRow style) {
        long styleId;
        if (style.hasId()) {
            styleId = style.getId();
        } else {
            StyleDao styleDao = getStyleDao();
            styleId = styleDao.create(style);
        }
        return styleId;
    }

    /**
     * Get the icon id, either from the existing icon or by inserting a new one
     *
     * @param icon icon row
     * @return icon id
     */
    private long getOrInsertIcon(IconRow icon) {
        long iconId;
        if (icon.hasId()) {
            iconId = icon.getId();
        } else {
            IconDao iconDao = getIconDao();
            iconId = iconDao.create(icon);
        }
        return iconId;
    }

    /**
     * Insert a style mapping row
     *
     * @param mappingDao   mapping dao
     * @param baseId       base id, either contents id or feature id
     * @param relatedId    related id, either style or icon id
     * @param geometryType geometry type or null
     */
    private void insertStyleMapping(StyleMappingDao mappingDao, long baseId,
                                    long relatedId, GeometryType geometryType) {

        StyleMappingRow row = mappingDao.newRow();

        row.setBaseId(baseId);
        row.setRelatedId(relatedId);
        row.setGeometryType(geometryType);

        mappingDao.insert(row);
    }

    /**
     * Delete all feature styles including table styles, table icons, style, and
     * icons
     *
     * @param featureTable feature table
     */
    public void deleteAllFeatureStyles(FeatureTable featureTable) {
        deleteAllFeatureStyles(featureTable.getTableName());
    }

    /**
     * Delete all feature styles including table styles, table icons, style, and
     * icons
     *
     * @param featureTable feature table
     */
    public void deleteAllFeatureStyles(String featureTable) {
        deleteTableFeatureStyles(featureTable);
        deleteFeatureStyles(featureTable);
    }

    /**
     * Delete all styles including table styles and feature row styles
     *
     * @param featureTable feature table
     */
    public void deleteAllStyles(FeatureTable featureTable) {
        deleteAllStyles(featureTable.getTableName());
    }

    /**
     * Delete all styles including table styles and feature row styles
     *
     * @param featureTable feature table
     */
    public void deleteAllStyles(String featureTable) {
        deleteTableStyles(featureTable);
        deleteStyles(featureTable);
    }

    /**
     * Delete all icons including table icons and feature row icons
     *
     * @param featureTable feature table
     */
    public void deleteAllIcons(FeatureTable featureTable) {
        deleteAllIcons(featureTable.getTableName());
    }

    /**
     * Delete all icons including table icons and feature row icons
     *
     * @param featureTable feature table
     */
    public void deleteAllIcons(String featureTable) {
        deleteTableIcons(featureTable);
        deleteIcons(featureTable);
    }

    /**
     * Delete the feature table feature styles
     *
     * @param featureTable feature table
     */
    public void deleteTableFeatureStyles(FeatureTable featureTable) {
        deleteTableFeatureStyles(featureTable.getTableName());
    }

    /**
     * Delete the feature table feature styles
     *
     * @param featureTable feature table
     */
    public void deleteTableFeatureStyles(String featureTable) {
        deleteTableStyles(featureTable);
        deleteTableIcons(featureTable);
    }

    /**
     * Delete the feature table styles
     *
     * @param featureTable feature table
     */
    public void deleteTableStyles(FeatureTable featureTable) {
        deleteTableStyles(featureTable.getTableName());
    }

    /**
     * Delete the feature table styles
     *
     * @param featureTable feature table
     */
    public void deleteTableStyles(String featureTable) {
        deleteTableMappings(getTableStyleMappingDao(featureTable), featureTable);
    }

    /**
     * Delete the feature table default style
     *
     * @param featureTable feature table
     */
    public void deleteTableStyleDefault(FeatureTable featureTable) {
        deleteTableStyleDefault(featureTable.getTableName());
    }

    /**
     * Delete the feature table default style
     *
     * @param featureTable feature table
     */
    public void deleteTableStyleDefault(String featureTable) {
        deleteTableStyle(featureTable, null);
    }

    /**
     * Delete the feature table style for the geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     */
    public void deleteTableStyle(FeatureTable featureTable,
                                 GeometryType geometryType) {
        deleteTableStyle(featureTable.getTableName(), geometryType);
    }

    /**
     * Delete the feature table style for the geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     */
    public void deleteTableStyle(String featureTable, GeometryType geometryType) {
        deleteTableMapping(getTableStyleMappingDao(featureTable), featureTable,
                geometryType);
    }

    /**
     * Delete the feature table icons
     *
     * @param featureTable feature table
     */
    public void deleteTableIcons(FeatureTable featureTable) {
        deleteTableIcons(featureTable.getTableName());
    }

    /**
     * Delete the feature table icons
     *
     * @param featureTable feature table
     */
    public void deleteTableIcons(String featureTable) {
        deleteTableMappings(getTableIconMappingDao(featureTable), featureTable);
    }

    /**
     * Delete the feature table default icon
     *
     * @param featureTable feature table
     */
    public void deleteTableIconDefault(FeatureTable featureTable) {
        deleteTableIconDefault(featureTable.getTableName());
    }

    /**
     * Delete the feature table default icon
     *
     * @param featureTable feature table
     */
    public void deleteTableIconDefault(String featureTable) {
        deleteTableIcon(featureTable, null);
    }

    /**
     * Delete the feature table icon for the geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     */
    public void deleteTableIcon(FeatureTable featureTable,
                                GeometryType geometryType) {
        deleteTableIcon(featureTable.getTableName(), geometryType);
    }

    /**
     * Delete the feature table icon for the geometry type
     *
     * @param featureTable feature table
     * @param geometryType geometry type
     */
    public void deleteTableIcon(String featureTable, GeometryType geometryType) {
        deleteTableMapping(getTableIconMappingDao(featureTable), featureTable,
                geometryType);
    }

    /**
     * Delete the table style mappings
     *
     * @param mappingDao   mapping dao
     * @param featureTable feature table
     */
    private void deleteTableMappings(StyleMappingDao mappingDao,
                                     String featureTable) {
        if (mappingDao != null) {
            Long featureContentsId = contentsId.getId(featureTable);
            if (featureContentsId != null) {
                mappingDao.deleteByBaseId(featureContentsId);
            }
        }
    }

    /**
     * Delete the table style mapping with the geometry type value
     *
     * @param mappingDao   mapping dao
     * @param featureTable feature table
     * @param geometryType geometry type
     */
    private void deleteTableMapping(StyleMappingDao mappingDao,
                                    String featureTable, GeometryType geometryType) {
        if (mappingDao != null) {
            Long featureContentsId = contentsId.getId(featureTable);
            if (featureContentsId != null) {
                mappingDao.deleteByBaseId(featureContentsId, geometryType);
            }
        }
    }

    /**
     * Delete all feature styles
     *
     * @param featureTable feature table
     */
    public void deleteFeatureStyles(FeatureTable featureTable) {
        deleteFeatureStyles(featureTable.getTableName());
    }

    /**
     * Delete all feature styles
     *
     * @param featureTable feature table
     */
    public void deleteFeatureStyles(String featureTable) {
        deleteStyles(featureTable);
        deleteIcons(featureTable);
    }

    /**
     * Delete all styles
     *
     * @param featureTable feature table
     */
    public void deleteStyles(FeatureTable featureTable) {
        deleteStyles(featureTable.getTableName());
    }

    /**
     * Delete all styles
     *
     * @param featureTable feature table
     */
    public void deleteStyles(String featureTable) {
        deleteMappings(getStyleMappingDao(featureTable));
    }

    /**
     * Delete feature row styles
     *
     * @param featureRow feature row
     */
    public void deleteStyles(FeatureRow featureRow) {
        deleteStyles(featureRow.getTable().getTableName(), featureRow.getId());
    }

    /**
     * Delete feature row styles
     *
     * @param featureTable feature table
     * @param featureId    feature id
     */
    public void deleteStyles(String featureTable, long featureId) {
        deleteMappings(getStyleMappingDao(featureTable), featureId);
    }

    /**
     * Delete the feature row default style
     *
     * @param featureRow feature row
     */
    public void deleteStyleDefault(FeatureRow featureRow) {
        deleteStyleDefault(featureRow.getTable().getTableName(),
                featureRow.getId());
    }

    /**
     * Delete the feature row default style
     *
     * @param featureTable feature table
     * @param featureId    feature id
     */
    public void deleteStyleDefault(String featureTable, long featureId) {
        deleteStyle(featureTable, featureId, null);
    }

    /**
     * Delete the feature row style for the feature row geometry type
     *
     * @param featureRow feature row
     */
    public void deleteStyle(FeatureRow featureRow) {
        deleteStyle(featureRow, featureRow.getGeometryType());
    }

    /**
     * Delete the feature row style for the geometry type
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     */
    public void deleteStyle(FeatureRow featureRow, GeometryType geometryType) {
        deleteStyle(featureRow.getTable().getTableName(), featureRow.getId(),
                geometryType);
    }

    /**
     * Delete the feature row style for the geometry type
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     */
    public void deleteStyle(String featureTable, long featureId,
                            GeometryType geometryType) {
        deleteMapping(getStyleMappingDao(featureTable), featureId, geometryType);
    }

    /**
     * Delete all icons
     *
     * @param featureTable feature table
     */
    public void deleteIcons(FeatureTable featureTable) {
        deleteIcons(featureTable.getTableName());
    }

    /**
     * Delete all icons
     *
     * @param featureTable feature table
     */
    public void deleteIcons(String featureTable) {
        deleteMappings(getIconMappingDao(featureTable));
    }

    /**
     * Delete feature row icons
     *
     * @param featureRow feature row
     */
    public void deleteIcons(FeatureRow featureRow) {
        deleteIcons(featureRow.getTable().getTableName(), featureRow.getId());
    }

    /**
     * Delete feature row icons
     *
     * @param featureTable feature table
     * @param featureId    feature id
     */
    public void deleteIcons(String featureTable, long featureId) {
        deleteMappings(getIconMappingDao(featureTable), featureId);
    }

    /**
     * Delete the feature row default icon
     *
     * @param featureRow feature row
     */
    public void deleteIconDefault(FeatureRow featureRow) {
        deleteIconDefault(featureRow.getTable().getTableName(),
                featureRow.getId());
    }

    /**
     * Delete the feature row default icon
     *
     * @param featureTable feature table
     * @param featureId    feature id
     */
    public void deleteIconDefault(String featureTable, long featureId) {
        deleteIcon(featureTable, featureId, null);
    }

    /**
     * Delete the feature row icon for the feature row geometry type
     *
     * @param featureRow feature row
     */
    public void deleteIcon(FeatureRow featureRow) {
        deleteIcon(featureRow, featureRow.getGeometryType());
    }

    /**
     * Delete the feature row icon for the geometry type
     *
     * @param featureRow   feature row
     * @param geometryType geometry type
     */
    public void deleteIcon(FeatureRow featureRow, GeometryType geometryType) {
        deleteIcon(featureRow.getTable().getTableName(), featureRow.getId(),
                geometryType);
    }

    /**
     * Delete the feature row icon for the geometry type
     *
     * @param featureTable feature table
     * @param featureId    feature id
     * @param geometryType geometry type
     */
    public void deleteIcon(String featureTable, long featureId,
                           GeometryType geometryType) {
        deleteMapping(getIconMappingDao(featureTable), featureId, geometryType);
    }

    /**
     * Delete all style mappings
     *
     * @param mappingDao mapping dao
     */
    private void deleteMappings(StyleMappingDao mappingDao) {
        if (mappingDao != null) {
            mappingDao.deleteAll();
        }
    }

    /**
     * Delete the style mappings
     *
     * @param mappingDao mapping dao
     * @param featureId  feature id
     */
    private void deleteMappings(StyleMappingDao mappingDao, long featureId) {
        if (mappingDao != null) {
            mappingDao.deleteByBaseId(featureId);
        }
    }

    /**
     * Delete the style mapping with the geometry type value
     *
     * @param mappingDao   mapping dao
     * @param featureId    feature id
     * @param geometryType geometry type
     */
    private void deleteMapping(StyleMappingDao mappingDao, long featureId,
                               GeometryType geometryType) {
        if (mappingDao != null) {
            mappingDao.deleteByBaseId(featureId, geometryType);
        }
    }

    /**
     * Get all the unique style row ids the table maps to
     *
     * @param featureTable feature table
     * @return style row ids
     */
    public List<Long> getAllTableStyleIds(FeatureTable featureTable) {
        return getAllTableStyleIds(featureTable.getTableName());
    }

    /**
     * Get all the unique style row ids the table maps to
     *
     * @param featureTable feature table
     * @return style row ids
     */
    public List<Long> getAllTableStyleIds(String featureTable) {
        List<Long> styleIds = null;
        StyleMappingDao mappingDao = getTableStyleMappingDao(featureTable);
        if (mappingDao != null) {
            styleIds = mappingDao.uniqueRelatedIds();
        }
        return styleIds;
    }

    /**
     * Get all the unique icon row ids the table maps to
     *
     * @param featureTable feature table
     * @return icon row ids
     */
    public List<Long> getAllTableIconIds(FeatureTable featureTable) {
        return getAllTableIconIds(featureTable.getTableName());
    }

    /**
     * Get all the unique icon row ids the table maps to
     *
     * @param featureTable feature table
     * @return icon row ids
     */
    public List<Long> getAllTableIconIds(String featureTable) {
        List<Long> iconIds = null;
        StyleMappingDao mappingDao = getTableIconMappingDao(featureTable);
        if (mappingDao != null) {
            iconIds = mappingDao.uniqueRelatedIds();
        }
        return iconIds;
    }

    /**
     * Get all the unique style row ids the features map to
     *
     * @param featureTable feature table
     * @return style row ids
     */
    public List<Long> getAllStyleIds(FeatureTable featureTable) {
        return getAllStyleIds(featureTable.getTableName());
    }

    /**
     * Get all the unique style row ids the features map to
     *
     * @param featureTable feature table
     * @return style row ids
     */
    public List<Long> getAllStyleIds(String featureTable) {
        List<Long> styleIds = null;
        StyleMappingDao mappingDao = getStyleMappingDao(featureTable);
        if (mappingDao != null) {
            styleIds = mappingDao.uniqueRelatedIds();
        }
        return styleIds;
    }

    /**
     * Get all the unique icon row ids the features map to
     *
     * @param featureTable feature table
     * @return icon row ids
     */
    public List<Long> getAllIconIds(FeatureTable featureTable) {
        return getAllIconIds(featureTable.getTableName());
    }

    /**
     * Get all the unique icon row ids the features map to
     *
     * @param featureTable feature table
     * @return icon row ids
     */
    public List<Long> getAllIconIds(String featureTable) {
        List<Long> iconIds = null;
        StyleMappingDao mappingDao = getIconMappingDao(featureTable);
        if (mappingDao != null) {
            iconIds = mappingDao.uniqueRelatedIds();
        }
        return iconIds;
    }

}
