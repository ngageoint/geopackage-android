package mil.nga.geopackage.test.extension.nga.style;

import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import junit.framework.TestCase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.nga.contents.ContentsId;
import mil.nga.geopackage.extension.nga.contents.ContentsIdExtension;
import mil.nga.geopackage.extension.nga.style.FeatureStyle;
import mil.nga.geopackage.extension.nga.style.FeatureStyleExtension;
import mil.nga.geopackage.extension.nga.style.FeatureStyles;
import mil.nga.geopackage.extension.nga.style.FeatureTableStyles;
import mil.nga.geopackage.extension.nga.style.IconDao;
import mil.nga.geopackage.extension.nga.style.IconRow;
import mil.nga.geopackage.extension.nga.style.IconTable;
import mil.nga.geopackage.extension.nga.style.Icons;
import mil.nga.geopackage.extension.nga.style.StyleDao;
import mil.nga.geopackage.extension.nga.style.StyleRow;
import mil.nga.geopackage.extension.nga.style.StyleTable;
import mil.nga.geopackage.extension.nga.style.Styles;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.style.Color;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.sf.GeometryType;
import mil.nga.sf.util.GeometryUtils;

/**
 * Test Feature Styles Utils
 *
 * @author osbornb
 */
public class FeatureStylesUtils {

    /**
     * Test Feature Styles extension
     *
     * @param geoPackage GeoPackage
     * @throws SQLException          upon error
     * @throws IOException           upon error
     * @throws NameNotFoundException upon error
     */
    public static void testFeatureStyles(GeoPackage geoPackage)
            throws SQLException, IOException, NameNotFoundException {

        geoPackage.getExtensionManager().deleteExtensions();

        FeatureStyleExtension featureStyleExtension = new FeatureStyleExtension(
                geoPackage);

        TestCase.assertFalse(featureStyleExtension.has());

        List<String> featureTables = geoPackage.getFeatureTables();

        if (!featureTables.isEmpty()) {

            TestCase.assertFalse(geoPackage.isTable(StyleTable.TABLE_NAME));
            TestCase.assertFalse(geoPackage.isTable(IconTable.TABLE_NAME));
            TestCase.assertFalse(geoPackage.isTable(ContentsId.TABLE_NAME));

            for (String tableName : featureTables) {

                TestCase.assertFalse(featureStyleExtension.has(tableName));

                FeatureDao featureDao = geoPackage.getFeatureDao(tableName);

                FeatureTableStyles featureTableStyles = new FeatureTableStyles(
                        geoPackage, featureDao.getTable());
                TestCase.assertFalse(featureTableStyles.has());

                GeometryType geometryType = featureDao.getGeometryType();
                Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = GeometryUtils
                        .childHierarchy(geometryType);

                TestCase.assertFalse(featureTableStyles
                        .hasTableStyleRelationship());
                TestCase.assertFalse(featureTableStyles.hasStyleRelationship());
                TestCase.assertFalse(featureTableStyles
                        .hasTableIconRelationship());
                TestCase.assertFalse(featureTableStyles.hasIconRelationship());

                TestCase.assertNotNull(featureTableStyles.getTableName());
                TestCase.assertEquals(tableName,
                        featureTableStyles.getTableName());
                TestCase.assertNotNull(featureTableStyles
                        .getFeatureStyleExtension());

                TestCase.assertNull(featureTableStyles.getTableFeatureStyles());
                TestCase.assertNull(featureTableStyles.getTableStyles());
                TestCase.assertNull(featureTableStyles.getCachedTableStyles());
                TestCase.assertNull(featureTableStyles.getTableStyleDefault());
                TestCase.assertNull(featureTableStyles
                        .getTableStyle(GeometryType.GEOMETRY));
                TestCase.assertNull(featureTableStyles.getTableIcons());
                TestCase.assertNull(featureTableStyles.getCachedTableIcons());
                TestCase.assertNull(featureTableStyles.getTableIconDefault());
                TestCase.assertNull(featureTableStyles
                        .getTableIcon(GeometryType.GEOMETRY));

                FeatureCursor featureCursor = featureDao.queryForAll();
                while (featureCursor.moveToNext()) {
                    FeatureRow featureRow = featureCursor.getRow();

                    TestCase.assertNull(featureTableStyles
                            .getFeatureStyles(featureRow));
                    TestCase.assertNull(featureTableStyles
                            .getFeatureStyles(featureRow.getId()));

                    TestCase.assertNull(featureTableStyles
                            .getFeatureStyle(featureRow));
                    TestCase.assertNull(featureTableStyles
                            .getFeatureStyleDefault(featureRow));
                    TestCase.assertNull(featureTableStyles.getFeatureStyle(
                            featureRow.getId(), featureRow.getGeometryType()));
                    TestCase.assertNull(featureTableStyles
                            .getFeatureStyleDefault(featureRow.getId()));

                    TestCase.assertNull(featureTableStyles
                            .getStyles(featureRow));
                    TestCase.assertNull(featureTableStyles.getStyles(featureRow
                            .getId()));

                    TestCase.assertNull(featureTableStyles.getStyle(featureRow));
                    TestCase.assertNull(featureTableStyles
                            .getStyleDefault(featureRow));
                    TestCase.assertNull(featureTableStyles.getStyle(
                            featureRow.getId(), featureRow.getGeometryType()));
                    TestCase.assertNull(featureTableStyles
                            .getStyleDefault(featureRow.getId()));

                    TestCase.assertNull(featureTableStyles.getIcons(featureRow));
                    TestCase.assertNull(featureTableStyles.getIcons(featureRow
                            .getId()));

                    TestCase.assertNull(featureTableStyles.getIcon(featureRow));
                    TestCase.assertNull(featureTableStyles
                            .getIconDefault(featureRow));
                    TestCase.assertNull(featureTableStyles.getIcon(
                            featureRow.getId(), featureRow.getGeometryType()));
                    TestCase.assertNull(featureTableStyles
                            .getIconDefault(featureRow.getId()));
                }
                featureCursor.close();

                // Table Styles
                TestCase.assertFalse(featureTableStyles
                        .hasTableStyleRelationship());
                TestCase.assertFalse(geoPackage
                        .isTable(featureTableStyles
                                .getFeatureStyleExtension()
                                .getMappingTableName(
                                        FeatureStyleExtension.TABLE_MAPPING_TABLE_STYLE,
                                        tableName)));

                // Add a default table style
                StyleRow tableStyleDefault = randomStyle();
                featureTableStyles.setTableStyleDefault(tableStyleDefault);

                TestCase.assertTrue(featureStyleExtension.has());
                TestCase.assertTrue(featureStyleExtension.has(tableName));
                TestCase.assertTrue(featureTableStyles.has());
                TestCase.assertTrue(featureTableStyles
                        .hasTableStyleRelationship());
                TestCase.assertTrue(geoPackage.isTable(StyleTable.TABLE_NAME));
                TestCase.assertTrue(geoPackage.isTable(ContentsId.TABLE_NAME));
                TestCase.assertTrue(geoPackage
                        .isTable(featureTableStyles
                                .getFeatureStyleExtension()
                                .getMappingTableName(
                                        FeatureStyleExtension.TABLE_MAPPING_TABLE_STYLE,
                                        tableName)));

                // Add geometry type table styles
                Map<GeometryType, StyleRow> geometryTypeTableStyles = randomStyles(childGeometryTypes);
                for (Entry<GeometryType, StyleRow> geometryTypeStyle : geometryTypeTableStyles
                        .entrySet()) {
                    featureTableStyles.setTableStyle(
                            geometryTypeStyle.getKey(),
                            geometryTypeStyle.getValue());
                }

                FeatureStyles featureStyles = featureTableStyles
                        .getTableFeatureStyles();
                TestCase.assertNotNull(featureStyles);
                TestCase.assertNotNull(featureStyles.getStyles());
                TestCase.assertNull(featureStyles.getIcons());

                Styles tableStyles = featureTableStyles.getTableStyles();
                TestCase.assertNotNull(tableStyles);
                TestCase.assertNotNull(tableStyles.getDefault());
                TestCase.assertEquals(tableStyleDefault.getId(), tableStyles
                        .getDefault().getId());
                TestCase.assertEquals(tableStyleDefault.getId(),
                        featureTableStyles.getTableStyle(null).getId());
                TestCase.assertEquals(tableStyleDefault.getId(),
                        featureTableStyles.getTableStyle(geometryType).getId());
                validateTableStyles(featureTableStyles, tableStyleDefault,
                        geometryTypeTableStyles, childGeometryTypes);

                // Table Icons
                TestCase.assertFalse(featureTableStyles
                        .hasTableIconRelationship());
                TestCase.assertFalse(geoPackage.isTable(featureTableStyles
                        .getFeatureStyleExtension().getMappingTableName(
                                FeatureStyleExtension.TABLE_MAPPING_TABLE_ICON,
                                tableName)));

                // Create table icon relationship
                TestCase.assertFalse(featureTableStyles
                        .hasTableIconRelationship());
                featureTableStyles.createTableIconRelationship();
                TestCase.assertTrue(featureTableStyles
                        .hasTableIconRelationship());

                Icons createTableIcons = new Icons();
                IconRow tableIconDefault = randomIcon(geoPackage);
                createTableIcons.setDefault(tableIconDefault);
                Map<GeometryType, IconRow> geometryTypeTableIcons = randomIcons(geoPackage, childGeometryTypes);
                IconRow baseGeometryTypeIcon = randomIcon(geoPackage);
                geometryTypeTableIcons.put(geometryType, baseGeometryTypeIcon);
                for (Entry<GeometryType, IconRow> geometryTypeIcon : geometryTypeTableIcons
                        .entrySet()) {
                    createTableIcons.setIcon(geometryTypeIcon.getValue(),
                            geometryTypeIcon.getKey());
                }

                // Set the table icons
                featureTableStyles.setTableIcons(createTableIcons);

                TestCase.assertTrue(featureTableStyles
                        .hasTableIconRelationship());
                TestCase.assertTrue(geoPackage.isTable(IconTable.TABLE_NAME));
                TestCase.assertTrue(geoPackage.isTable(featureTableStyles
                        .getFeatureStyleExtension().getMappingTableName(
                                FeatureStyleExtension.TABLE_MAPPING_TABLE_ICON,
                                tableName)));

                featureStyles = featureTableStyles.getTableFeatureStyles();
                TestCase.assertNotNull(featureStyles);
                TestCase.assertNotNull(featureStyles.getStyles());
                Icons tableIcons = featureStyles.getIcons();
                TestCase.assertNotNull(tableIcons);

                TestCase.assertNotNull(tableIcons.getDefault());
                TestCase.assertEquals(tableIconDefault.getId(), tableIcons
                        .getDefault().getId());
                TestCase.assertEquals(tableIconDefault.getId(),
                        featureTableStyles.getTableIcon(null).getId());
                TestCase.assertEquals(baseGeometryTypeIcon.getId(),
                        featureTableStyles.getTableIcon(geometryType).getId());
                validateTableIcons(featureTableStyles, baseGeometryTypeIcon,
                        geometryTypeTableIcons, childGeometryTypes);

                TestCase.assertFalse(featureTableStyles.hasStyleRelationship());
                TestCase.assertFalse(geoPackage.isTable(featureTableStyles
                        .getFeatureStyleExtension().getMappingTableName(
                                FeatureStyleExtension.TABLE_MAPPING_STYLE,
                                tableName)));
                TestCase.assertFalse(featureTableStyles.hasIconRelationship());
                TestCase.assertFalse(geoPackage.isTable(featureTableStyles
                        .getFeatureStyleExtension().getMappingTableName(
                                FeatureStyleExtension.TABLE_MAPPING_ICON,
                                tableName)));

                StyleDao styleDao = featureTableStyles.getStyleDao();
                IconDao iconDao = featureTableStyles.getIconDao();

                List<StyleRow> randomStyles = new ArrayList<>();
                List<IconRow> randomIcons = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    StyleRow styleRow = randomStyle();
                    randomStyles.add(styleRow);
                    IconRow iconRow = randomIcon(geoPackage);
                    randomIcons.add(iconRow);

                    if (i % 2 == 0) {
                        styleDao.insert(styleRow);
                        iconDao.insert(iconRow);
                    }
                }

                // Create style and icon relationship
                featureTableStyles.createStyleRelationship();
                TestCase.assertTrue(featureTableStyles.hasStyleRelationship());
                TestCase.assertTrue(geoPackage.isTable(featureTableStyles
                        .getFeatureStyleExtension().getMappingTableName(
                                FeatureStyleExtension.TABLE_MAPPING_STYLE,
                                tableName)));
                featureTableStyles.createIconRelationship();
                TestCase.assertTrue(featureTableStyles.hasIconRelationship());
                TestCase.assertTrue(geoPackage.isTable(featureTableStyles
                        .getFeatureStyleExtension().getMappingTableName(
                                FeatureStyleExtension.TABLE_MAPPING_ICON,
                                tableName)));

                Map<Long, Map<GeometryType, StyleRow>> featureResultsStyles = new HashMap<>();
                Map<Long, Map<GeometryType, IconRow>> featureResultsIcons = new HashMap<>();

                featureCursor = featureDao.queryForAll();
                while (featureCursor.moveToNext()) {

                    double randomFeatureOption = Math.random();

                    if (randomFeatureOption < .25) {
                        continue;
                    }

                    FeatureRow featureRow = featureCursor.getRow();

                    if (randomFeatureOption < .75) {

                        // Feature Styles

                        Map<GeometryType, StyleRow> featureRowStyles = new HashMap<>();
                        featureResultsStyles.put(featureRow.getId(),
                                featureRowStyles);

                        // Add a default style
                        StyleRow styleDefault = randomStyle(randomStyles);
                        featureTableStyles.setStyleDefault(featureRow,
                                styleDefault);
                        featureRowStyles.put(null, styleDefault);

                        // Add geometry type styles
                        Map<GeometryType, StyleRow> geometryTypeStyles = randomStyles(
                                childGeometryTypes, randomStyles);
                        for (Entry<GeometryType, StyleRow> geometryTypeStyle : geometryTypeStyles
                                .entrySet()) {
                            featureTableStyles.setStyle(featureRow,
                                    geometryTypeStyle.getKey(),
                                    geometryTypeStyle.getValue());
                            featureRowStyles.put(geometryTypeStyle.getKey(),
                                    geometryTypeStyle.getValue());
                        }

                    }

                    if (randomFeatureOption >= .5) {

                        // Feature Icons

                        Map<GeometryType, IconRow> featureRowIcons = new HashMap<>();
                        featureResultsIcons.put(featureRow.getId(),
                                featureRowIcons);

                        // Add a default icon
                        IconRow iconDefault = randomIcon(geoPackage, randomIcons);
                        featureTableStyles.setIconDefault(featureRow,
                                iconDefault);
                        featureRowIcons.put(null, iconDefault);

                        // Add geometry type icons
                        Map<GeometryType, IconRow> geometryTypeIcons = randomIcons(geoPackage,
                                childGeometryTypes, randomIcons);
                        for (Entry<GeometryType, IconRow> geometryTypeIcon : geometryTypeIcons
                                .entrySet()) {
                            featureTableStyles.setIcon(featureRow,
                                    geometryTypeIcon.getKey(),
                                    geometryTypeIcon.getValue());
                            featureRowIcons.put(geometryTypeIcon.getKey(),
                                    geometryTypeIcon.getValue());
                        }

                    }

                }
                featureCursor.close();

                featureCursor = featureDao.queryForAll();
                while (featureCursor.moveToNext()) {

                    FeatureRow featureRow = featureCursor.getRow();

                    long featureRowId = featureRow.getId();
                    Map<GeometryType, StyleRow> featureRowStyles = featureResultsStyles
                            .get(featureRowId);
                    boolean hasFeatureRowStyles = featureRowStyles != null;
                    Map<GeometryType, IconRow> featureRowIcons = featureResultsIcons
                            .get(featureRowId);
                    boolean hasFeatureRowIcons = featureRowIcons != null;
                    FeatureStyle featureStyle = featureTableStyles
                            .getFeatureStyle(featureRow);
                    TestCase.assertNotNull(featureStyle);
                    TestCase.assertTrue(featureStyle.hasStyle());
                    TestCase.assertNotNull(featureStyle.getStyle());
                    TestCase.assertEquals(!hasFeatureRowStyles,
                            featureStyle.getStyle().isTableStyle());
                    StyleRow expectedStyleRow = getExpectedRowStyle(featureRow,
                            featureRow.getGeometryType(), tableStyleDefault,
                            geometryTypeTableStyles, featureResultsStyles);
                    TestCase.assertEquals(expectedStyleRow.getId(),
                            featureStyle.getStyle().getId());
                    TestCase.assertTrue(featureStyle.hasIcon());
                    TestCase.assertNotNull(featureStyle.getIcon());
                    TestCase.assertEquals(!hasFeatureRowIcons,
                            featureStyle.getIcon().isTableIcon());
                    IconRow expectedIconRow = getExpectedRowIcon(featureRow,
                            featureRow.getGeometryType(), tableIconDefault,
                            geometryTypeTableIcons, featureResultsIcons);
                    TestCase.assertEquals(expectedIconRow.getId(),
                            featureStyle.getIcon().getId());
                    TestCase.assertEquals(hasFeatureRowIcons || !hasFeatureRowStyles,
                            featureStyle.useIcon());

                    validateRowStyles(featureTableStyles, featureRow,
                            tableStyleDefault, geometryTypeTableStyles,
                            featureResultsStyles);

                    validateRowIcons(featureTableStyles, featureRow,
                            tableIconDefault, geometryTypeTableIcons,
                            featureResultsIcons);

                }
                featureCursor.close();

            }

            List<String> tables = featureStyleExtension.getTables();
            TestCase.assertEquals(featureTables.size(), tables.size());

            for (String tableName : featureTables) {

                TestCase.assertTrue(tables.contains(tableName));

                TestCase.assertNotNull(featureStyleExtension
                        .getTableStyles(tableName));
                TestCase.assertNotNull(featureStyleExtension
                        .getTableIcons(tableName));

                featureStyleExtension.deleteAllFeatureStyles(tableName);

                TestCase.assertNull(featureStyleExtension
                        .getTableStyles(tableName));
                TestCase.assertNull(featureStyleExtension
                        .getTableIcons(tableName));

                FeatureDao featureDao = geoPackage.getFeatureDao(tableName);
                FeatureCursor featureCursor = featureDao.queryForAll();
                while (featureCursor.moveToNext()) {

                    FeatureRow featureRow = featureCursor.getRow();

                    TestCase.assertNull(featureStyleExtension
                            .getStyles(featureRow));
                    TestCase.assertNull(featureStyleExtension
                            .getIcons(featureRow));

                }
                featureCursor.close();

                featureStyleExtension.deleteRelationships(tableName);
                TestCase.assertFalse(featureStyleExtension.has(tableName));

            }

            TestCase.assertFalse(featureStyleExtension.has());

            TestCase.assertTrue(geoPackage.isTable(StyleTable.TABLE_NAME));
            TestCase.assertTrue(geoPackage.isTable(IconTable.TABLE_NAME));
            TestCase.assertTrue(geoPackage.isTable(ContentsId.TABLE_NAME));

            featureStyleExtension.removeExtension();

            TestCase.assertFalse(geoPackage.isTable(StyleTable.TABLE_NAME));
            TestCase.assertFalse(geoPackage.isTable(IconTable.TABLE_NAME));
            TestCase.assertTrue(geoPackage.isTable(ContentsId.TABLE_NAME));

            ContentsIdExtension contentsIdExtension = featureStyleExtension
                    .getContentsId();
            TestCase.assertEquals(featureTables.size(),
                    contentsIdExtension.count());
            TestCase.assertEquals(featureTables.size(),
                    contentsIdExtension.deleteIds());
            contentsIdExtension.removeExtension();
            TestCase.assertFalse(geoPackage.isTable(ContentsId.TABLE_NAME));

        }

    }

    private static void validateTableStyles(
            FeatureTableStyles featureTableStyles, StyleRow styleRow,
            Map<GeometryType, StyleRow> geometryTypeStyles,
            Map<GeometryType, Map<GeometryType, ?>> geometryTypes) {

        if (geometryTypes != null) {
            for (Entry<GeometryType, Map<GeometryType, ?>> type : geometryTypes
                    .entrySet()) {
                StyleRow typeStyleRow = styleRow;
                if (geometryTypeStyles.containsKey(type.getKey())) {
                    typeStyleRow = geometryTypeStyles.get(type.getKey());
                }
                TestCase.assertEquals(typeStyleRow.getId(), featureTableStyles
                        .getTableStyle(type.getKey()).getId());
                @SuppressWarnings("unchecked")
                Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = (Map<GeometryType, Map<GeometryType, ?>>) type
                        .getValue();
                validateTableStyles(featureTableStyles, typeStyleRow,
                        geometryTypeStyles, childGeometryTypes);
            }
        }
    }

    private static void validateTableIcons(
            FeatureTableStyles featureTableStyles, IconRow iconRow,
            Map<GeometryType, IconRow> geometryTypeIcons,
            Map<GeometryType, Map<GeometryType, ?>> geometryTypes)
            throws IOException {

        if (geometryTypes != null) {
            for (Entry<GeometryType, Map<GeometryType, ?>> type : geometryTypes
                    .entrySet()) {
                IconRow typeIconRow = iconRow;
                if (geometryTypeIcons.containsKey(type.getKey())) {
                    typeIconRow = geometryTypeIcons.get(type.getKey());
                    TestCase.assertTrue(typeIconRow.getId() >= 0);
                    TestCase.assertNotNull(typeIconRow.getData());
                    TestCase.assertEquals("image/"
                                    + TestConstants.ICON_POINT_IMAGE_EXTENSION,
                            typeIconRow.getContentType());
                    Bitmap iconImage = typeIconRow.getDataBitmap();
                    TestCase.assertNotNull(iconImage);
                    TestCase.assertTrue(iconImage.getWidth() > 0);
                    TestCase.assertTrue(iconImage.getHeight() > 0);
                }
                TestCase.assertEquals(typeIconRow.getId(), featureTableStyles
                        .getTableIcon(type.getKey()).getId());
                @SuppressWarnings("unchecked")
                Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = (Map<GeometryType, Map<GeometryType, ?>>) type
                        .getValue();
                validateTableIcons(featureTableStyles, typeIconRow,
                        geometryTypeIcons, childGeometryTypes);
            }
        }
    }

    private static void validateRowStyles(
            FeatureTableStyles featureTableStyles, FeatureRow featureRow,
            StyleRow tableStyleDefault,
            Map<GeometryType, StyleRow> geometryTypeTableStyles,
            Map<Long, Map<GeometryType, StyleRow>> featureResultsStyles) {

        GeometryType geometryType = featureRow.getGeometryType();

        validateRowStyles(featureTableStyles, featureRow, null,
                tableStyleDefault, geometryTypeTableStyles,
                featureResultsStyles);

        if (geometryType != null) {

            List<GeometryType> geometryTypes = GeometryUtils
                    .parentHierarchy(geometryType);
            for (GeometryType parentGeometryType : geometryTypes) {
                validateRowStyles(featureTableStyles, featureRow,
                        parentGeometryType, tableStyleDefault,
                        geometryTypeTableStyles, featureResultsStyles);
            }

            List<GeometryType> childTypes = getAllChildTypes(geometryType);
            for (GeometryType childGeometryType : childTypes) {
                validateRowStyles(featureTableStyles, featureRow,
                        childGeometryType, tableStyleDefault,
                        geometryTypeTableStyles, featureResultsStyles);
            }
        }

    }

    private static void validateRowStyles(
            FeatureTableStyles featureTableStyles, FeatureRow featureRow,
            GeometryType geometryType, StyleRow tableStyleDefault,
            Map<GeometryType, StyleRow> geometryTypeTableStyles,
            Map<Long, Map<GeometryType, StyleRow>> featureResultsStyles) {

        StyleRow styleRow = null;
        if (geometryType == null) {
            styleRow = featureTableStyles.getStyle(featureRow);
            geometryType = featureRow.getGeometryType();
        } else {
            styleRow = featureTableStyles.getStyle(featureRow, geometryType);
        }

        StyleRow expectedStyleRow = getExpectedRowStyle(featureRow,
                geometryType, tableStyleDefault, geometryTypeTableStyles,
                featureResultsStyles);

        if (expectedStyleRow != null) {
            TestCase.assertEquals(expectedStyleRow.getId(), styleRow.getId());
            TestCase.assertNotNull(styleRow.getTable());
            TestCase.assertTrue(styleRow.getId() >= 0);
            styleRow.getName();
            styleRow.getDescription();
            styleRow.getColor();
            styleRow.getHexColor();
            styleRow.getOpacity();
            styleRow.getWidth();
            styleRow.getFillColor();
            styleRow.getFillHexColor();
            styleRow.getFillOpacity();
        } else {
            TestCase.assertNull(styleRow);
        }

    }

    private static StyleRow getExpectedRowStyle(FeatureRow featureRow,
                                                GeometryType geometryType, StyleRow tableStyleDefault,
                                                Map<GeometryType, StyleRow> geometryTypeTableStyles,
                                                Map<Long, Map<GeometryType, StyleRow>> featureResultsStyles) {

        List<GeometryType> geometryTypes = null;
        if (geometryType != null) {
            geometryTypes = GeometryUtils.parentHierarchy(geometryType);
            geometryTypes.add(0, geometryType);
        } else {
            geometryTypes = new ArrayList<>();
        }
        geometryTypes.add(null);

        StyleRow expectedStyleRow = null;
        Map<GeometryType, StyleRow> geometryTypeRowStyles = featureResultsStyles
                .get(featureRow.getId());
        if (geometryTypeRowStyles != null) {
            for (GeometryType type : geometryTypes) {
                expectedStyleRow = geometryTypeRowStyles.get(type);
                if (expectedStyleRow != null) {
                    break;
                }
            }
        }

        if (expectedStyleRow == null) {
            for (GeometryType type : geometryTypes) {
                expectedStyleRow = geometryTypeTableStyles.get(type);
                if (expectedStyleRow != null) {
                    break;
                }
            }

            if (expectedStyleRow == null) {
                expectedStyleRow = tableStyleDefault;
            }
        }

        return expectedStyleRow;
    }

    private static void validateRowIcons(FeatureTableStyles featureTableStyles,
                                         FeatureRow featureRow, IconRow tableIconDefault,
                                         Map<GeometryType, IconRow> geometryTypeTableIcons,
                                         Map<Long, Map<GeometryType, IconRow>> featureResultsIcons) {

        GeometryType geometryType = featureRow.getGeometryType();

        validateRowIcons(featureTableStyles, featureRow, null,
                tableIconDefault, geometryTypeTableIcons, featureResultsIcons);

        if (geometryType != null) {

            List<GeometryType> geometryTypes = GeometryUtils
                    .parentHierarchy(geometryType);
            for (GeometryType parentGeometryType : geometryTypes) {
                validateRowIcons(featureTableStyles, featureRow,
                        parentGeometryType, tableIconDefault,
                        geometryTypeTableIcons, featureResultsIcons);
            }

            List<GeometryType> childTypes = getAllChildTypes(geometryType);
            for (GeometryType childGeometryType : childTypes) {
                validateRowIcons(featureTableStyles, featureRow,
                        childGeometryType, tableIconDefault,
                        geometryTypeTableIcons, featureResultsIcons);
            }
        }

    }

    private static void validateRowIcons(FeatureTableStyles featureTableStyles,
                                         FeatureRow featureRow, GeometryType geometryType,
                                         IconRow tableIconDefault,
                                         Map<GeometryType, IconRow> geometryTypeTableIcons,
                                         Map<Long, Map<GeometryType, IconRow>> featureResultsIcons) {

        IconRow iconRow = null;
        if (geometryType == null) {
            iconRow = featureTableStyles.getIcon(featureRow);
            geometryType = featureRow.getGeometryType();
        } else {
            iconRow = featureTableStyles.getIcon(featureRow, geometryType);
        }

        IconRow expectedIconRow = getExpectedRowIcon(featureRow, geometryType,
                tableIconDefault, geometryTypeTableIcons, featureResultsIcons);

        if (expectedIconRow != null) {
            TestCase.assertEquals(expectedIconRow.getId(), iconRow.getId());
            TestCase.assertNotNull(iconRow.getTable());
            TestCase.assertTrue(iconRow.getId() >= 0);
            iconRow.getName();
            iconRow.getDescription();
            iconRow.getWidth();
            iconRow.getHeight();
            iconRow.getAnchorU();
            iconRow.getAnchorV();
        } else {
            TestCase.assertNull(iconRow);
        }

    }

    private static IconRow getExpectedRowIcon(FeatureRow featureRow,
                                              GeometryType geometryType, IconRow tableIconDefault,
                                              Map<GeometryType, IconRow> geometryTypeTableIcons,
                                              Map<Long, Map<GeometryType, IconRow>> featureResultsIcons) {

        List<GeometryType> geometryTypes = null;
        if (geometryType != null) {
            geometryTypes = GeometryUtils.parentHierarchy(geometryType);
            geometryTypes.add(0, geometryType);
        } else {
            geometryTypes = new ArrayList<>();
        }
        geometryTypes.add(null);

        IconRow expectedIconRow = null;
        Map<GeometryType, IconRow> geometryTypeRowIcons = featureResultsIcons
                .get(featureRow.getId());
        if (geometryTypeRowIcons != null) {
            for (GeometryType type : geometryTypes) {
                expectedIconRow = geometryTypeRowIcons.get(type);
                if (expectedIconRow != null) {
                    break;
                }
            }
        }

        if (expectedIconRow == null) {
            for (GeometryType type : geometryTypes) {
                expectedIconRow = geometryTypeTableIcons.get(type);
                if (expectedIconRow != null) {
                    break;
                }
            }

            if (expectedIconRow == null) {
                expectedIconRow = tableIconDefault;
            }
        }

        return expectedIconRow;
    }

    private static StyleRow randomStyle() {
        StyleRow styleRow = new StyleRow();

        if (Math.random() < .5) {
            styleRow.setName("Style Name");
        }
        if (Math.random() < .5) {
            styleRow.setDescription("Style Description");
        }
        styleRow.setColor(randomColor());
        if (Math.random() < .5) {
            styleRow.setWidth(1.0 + (Math.random() * 3));
        }
        styleRow.setFillColor(randomColor());

        return styleRow;
    }

    private static Color randomColor() {

        Color color = null;

        if (Math.random() < .5) {
            color = new Color((int) (Math.random() * 256),
                    (int) (Math.random() * 256), (int) (Math.random() * 256));
            if (Math.random() < .5) {
                color.setOpacity((float) Math.random());
            }
        }

        return color;
    }

    private static IconRow randomIcon(GeoPackage geoPackage) throws IOException, NameNotFoundException {
        IconRow iconRow = new IconRow();

        TestUtils.copyAssetFileToInternalStorage(geoPackage.getContext(), TestUtils.getTestContext(geoPackage.getContext()), TestConstants.ICON_POINT_IMAGE);
        String iconImage = TestUtils.getAssetFileInternalStorageLocation(geoPackage.getContext(), TestConstants.ICON_POINT_IMAGE);
        Bitmap iconBitmap = BitmapFactory.decodeFile(iconImage);

        iconRow.setData(iconBitmap, Bitmap.CompressFormat.PNG);
        iconRow.setContentType("image/"
                + TestConstants.ICON_POINT_IMAGE_EXTENSION);
        if (Math.random() < .5) {
            iconRow.setName("Icon Name");
        }
        if (Math.random() < .5) {
            iconRow.setDescription("Icon Description");
        }
        if (Math.random() < .5) {
            iconRow.setWidth(Math.random() * iconBitmap.getWidth());
        }
        if (Math.random() < .5) {
            iconRow.setHeight(Math.random() * iconBitmap.getHeight());
        }
        if (Math.random() < .5) {
            iconRow.setAnchorU(Math.random());
        }
        if (Math.random() < .5) {
            iconRow.setAnchorV(Math.random());
        }

        return iconRow;
    }

    private static Map<GeometryType, StyleRow> randomStyles(
            Map<GeometryType, Map<GeometryType, ?>> geometryTypes) {
        return randomStyles(geometryTypes, null);
    }

    private static Map<GeometryType, IconRow> randomIcons(GeoPackage geoPackage,
                                                          Map<GeometryType, Map<GeometryType, ?>> geometryTypes)
            throws IOException, NameNotFoundException {
        return randomIcons(geoPackage, geometryTypes, null);
    }

    private static Map<GeometryType, StyleRow> randomStyles(
            Map<GeometryType, Map<GeometryType, ?>> geometryTypes,
            List<StyleRow> randomStyles) {
        Map<GeometryType, StyleRow> rowMap = new HashMap<>();
        if (geometryTypes != null) {
            for (Entry<GeometryType, Map<GeometryType, ?>> type : geometryTypes
                    .entrySet()) {
                if (Math.random() < .5) {
                    rowMap.put(type.getKey(), randomStyle(randomStyles));
                }
                @SuppressWarnings("unchecked")
                Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = (Map<GeometryType, Map<GeometryType, ?>>) type
                        .getValue();
                Map<GeometryType, StyleRow> childRowMap = randomStyles(
                        childGeometryTypes, randomStyles);
                rowMap.putAll(childRowMap);
            }
        }
        return rowMap;
    }

    private static Map<GeometryType, IconRow> randomIcons(GeoPackage geoPackage,
                                                          Map<GeometryType, Map<GeometryType, ?>> geometryTypes,
                                                          List<IconRow> randomIcons) throws IOException, NameNotFoundException {
        Map<GeometryType, IconRow> rowMap = new HashMap<>();
        if (geometryTypes != null) {
            for (Entry<GeometryType, Map<GeometryType, ?>> type : geometryTypes
                    .entrySet()) {
                if (Math.random() < .5) {
                    rowMap.put(type.getKey(), randomIcon(geoPackage, randomIcons));
                }
                @SuppressWarnings("unchecked")
                Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = (Map<GeometryType, Map<GeometryType, ?>>) type
                        .getValue();
                Map<GeometryType, IconRow> childRowMap = randomIcons(geoPackage,
                        childGeometryTypes, randomIcons);
                rowMap.putAll(childRowMap);
            }
        }
        return rowMap;
    }

    private static StyleRow randomStyle(List<StyleRow> randomStyles) {
        StyleRow randomStyle = null;
        if (randomStyles != null) {
            randomStyle = randomStyles.get((int) (Math.random() * randomStyles
                    .size()));
        } else {
            randomStyle = randomStyle();
        }

        return randomStyle;
    }

    private static IconRow randomIcon(GeoPackage geoPackage, List<IconRow> randomIcons)
            throws IOException, NameNotFoundException {
        IconRow randomIcon = null;
        if (randomIcons != null) {
            randomIcon = randomIcons.get((int) (Math.random() * randomIcons
                    .size()));
        } else {
            randomIcon = randomIcon(geoPackage);
        }

        return randomIcon;
    }

    private static List<GeometryType> getAllChildTypes(GeometryType geometryType) {

        List<GeometryType> allChildTypes = new ArrayList<>();

        List<GeometryType> childTypes = GeometryUtils.childTypes(geometryType);
        allChildTypes.addAll(childTypes);

        for (GeometryType childType : childTypes) {
            allChildTypes.addAll(getAllChildTypes(childType));
        }

        return allChildTypes;
    }

}
