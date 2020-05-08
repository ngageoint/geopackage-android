package mil.nga.geopackage.test.extension.nga.properties;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageCache;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.extension.nga.properties.PropertiesExtension;
import mil.nga.geopackage.extension.nga.properties.PropertiesManager;
import mil.nga.geopackage.extension.nga.properties.PropertyNames;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.test.BaseTestCase;
import mil.nga.geopackage.test.TestSetupTeardown;

/**
 * Properties Manager Tests
 *
 * @author osbornb
 */
public class PropertiesManagerTest extends BaseTestCase {

    private static final int GEOPACKAGE_COUNT = 12;

    private static final int GEOPACKAGE_WITH_PROPERTIES_COUNT = 10;

    private static final int GEOPACKAGE_WITHOUT_PROPERTIES_COUNT = GEOPACKAGE_COUNT
            - GEOPACKAGE_WITH_PROPERTIES_COUNT;

    private static final String GEOPACKAGE_NAME = "Name";

    private static final String CREATOR = "NGA";

    private static final String EVEN_PROPERTY = "even";

    private static final String ODD_PROPERTY = "odd";

    private static final String COLOR_RED = "Red";
    private static final int COLOR_RED_FREQUENCY = 2;
    private static final int COLOR_RED_COUNT = getCount(COLOR_RED_FREQUENCY);
    private static final String COLOR_GREEN = "Green";
    private static final int COLOR_GREEN_FREQUENCY = 3;
    private static final int COLOR_GREEN_COUNT = getCount(COLOR_GREEN_FREQUENCY);
    private static final String COLOR_BLUE = "Blue";
    private static final int COLOR_BLUE_FREQUENCY = 4;
    private static final int COLOR_BLUE_COUNT = getCount(COLOR_BLUE_FREQUENCY);

    private static int getCount(int frequency) {
        return (int) Math.ceil(GEOPACKAGE_WITH_PROPERTIES_COUNT
                / (double) frequency);
    }

    /**
     * Test properties extension with cache of GeoPackages
     *
     * @throws Exception upon error
     */
    @Test
    public void testPropertiesManagerWithCache() throws Exception {

        GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        GeoPackageCache cache = new GeoPackageCache(manager);

        List<String> geoPackages = createGeoPackageNames();

        for (String name : geoPackages) {
            cache.getOrOpen(name);
        }

        PropertiesManager propertiesManager = new PropertiesManager(cache);
        testPropertiesManager(propertiesManager);
    }

    /**
     * Test properties extension with GeoPackages
     *
     * @throws Exception upon error
     */
    @Test
    public void testPropertiesManagerWithGeoPackages() throws Exception {
        PropertiesManager manager = new PropertiesManager(createGeoPackages());
        testPropertiesManager(manager);
    }

    private List<GeoPackage> createGeoPackages() throws Exception {

        GeoPackageManager manager = GeoPackageFactory.getManager(activity);

        List<GeoPackage> geoPackages = new ArrayList<>();

        List<String> geoPackageNames = createGeoPackageNames();

        for (String name : geoPackageNames) {
            GeoPackage geoPackage = manager.open(name);
            geoPackages.add(geoPackage);
        }

        return geoPackages;
    }

    private List<String> createGeoPackageNames() throws Exception {

        List<String> names = new ArrayList<>();

        for (int i = 0; i < GEOPACKAGE_COUNT; i++) {
            GeoPackage geoPackage = TestSetupTeardown.setUpCreate(activity, testContext,
                    GEOPACKAGE_NAME + (i + 1), true, true, true);
            if (i < GEOPACKAGE_WITH_PROPERTIES_COUNT) {
                addProperties(geoPackage, i);
            }
            names.add(geoPackage.getName());
            geoPackage.close();
        }

        return names;
    }

    private void addProperties(GeoPackage geoPackage, int i) {

        PropertiesExtension properties = new PropertiesExtension(geoPackage);

        properties.addValue(PropertyNames.TITLE, GEOPACKAGE_NAME + (i + 1));
        properties.addValue(PropertyNames.IDENTIFIER, Integer.toString(i));
        properties.addValue(EVEN_PROPERTY, i % 2 == 0 ? Boolean.TRUE.toString()
                : Boolean.FALSE.toString());
        if (i % 2 == 1) {
            properties.addValue(ODD_PROPERTY, Boolean.TRUE.toString());
        }

        if (i % COLOR_RED_FREQUENCY == 0) {
            properties.addValue(PropertyNames.TAG, COLOR_RED);
        }
        if (i % COLOR_GREEN_FREQUENCY == 0) {
            properties.addValue(PropertyNames.TAG, COLOR_GREEN);
        }
        if (i % COLOR_BLUE_FREQUENCY == 0) {
            properties.addValue(PropertyNames.TAG, COLOR_BLUE);
        }

    }

    private void testPropertiesManager(PropertiesManager manager) {

        int numProperties = 5;
        int numTagged = 7;

        // getNames
        Set<String> names = manager.getGeoPackageNames();
        TestCase.assertEquals(GEOPACKAGE_COUNT, names.size());
        for (int i = 1; i <= names.size(); i++) {
            String name = GEOPACKAGE_NAME + i;
            TestCase.assertTrue(names.contains(name));
            TestCase.assertNotNull(manager.getGeoPackage(name));
        }
        TestCase.assertEquals(GEOPACKAGE_COUNT, manager.numGeoPackages());

        // numProperties
        TestCase.assertEquals(numProperties, manager.numProperties());

        // getProperties
        Set<String> properties = manager.getProperties();
        TestCase.assertEquals(numProperties, properties.size());
        TestCase.assertTrue(properties.contains(PropertyNames.TITLE));
        TestCase.assertTrue(properties.contains(PropertyNames.IDENTIFIER));
        TestCase.assertTrue(properties.contains(EVEN_PROPERTY));
        TestCase.assertTrue(properties.contains(ODD_PROPERTY));
        TestCase.assertTrue(properties.contains(PropertyNames.TAG));

        // hasProperty
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT, manager
                .hasProperty(PropertyNames.TITLE).size());
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT, manager
                .hasProperty(PropertyNames.IDENTIFIER).size());
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT, manager
                .hasProperty(EVEN_PROPERTY).size());
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT / 2, manager
                .hasProperty(ODD_PROPERTY).size());
        TestCase.assertEquals(numTagged, manager.hasProperty(PropertyNames.TAG)
                .size());

        // missingProperty
        TestCase.assertEquals(GEOPACKAGE_WITHOUT_PROPERTIES_COUNT, manager
                .missingProperty(PropertyNames.TITLE).size());
        TestCase.assertEquals(GEOPACKAGE_WITHOUT_PROPERTIES_COUNT, manager
                .missingProperty(PropertyNames.IDENTIFIER).size());
        TestCase.assertEquals(GEOPACKAGE_WITHOUT_PROPERTIES_COUNT, manager
                .missingProperty(EVEN_PROPERTY).size());
        TestCase.assertEquals(GEOPACKAGE_WITHOUT_PROPERTIES_COUNT
                + GEOPACKAGE_WITH_PROPERTIES_COUNT / 2, manager
                .missingProperty(ODD_PROPERTY).size());
        TestCase.assertEquals(GEOPACKAGE_WITHOUT_PROPERTIES_COUNT
                + (GEOPACKAGE_WITH_PROPERTIES_COUNT - numTagged), manager
                .missingProperty(PropertyNames.TAG).size());

        // numValues
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT,
                manager.numValues(PropertyNames.TITLE));
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT,
                manager.numValues(PropertyNames.IDENTIFIER));
        TestCase.assertEquals(2, manager.numValues(EVEN_PROPERTY));
        TestCase.assertEquals(1, manager.numValues(ODD_PROPERTY));
        TestCase.assertEquals(3, manager.numValues(PropertyNames.TAG));
        TestCase.assertEquals(0, manager.numValues(PropertyNames.CREATOR));

        // hasValues
        TestCase.assertTrue(manager.hasValues(PropertyNames.TITLE));
        TestCase.assertTrue(manager.hasValues(PropertyNames.IDENTIFIER));
        TestCase.assertTrue(manager.hasValues(EVEN_PROPERTY));
        TestCase.assertTrue(manager.hasValues(ODD_PROPERTY));
        TestCase.assertTrue(manager.hasValues(PropertyNames.TAG));
        TestCase.assertFalse(manager.hasValues(PropertyNames.CREATOR));

        // getValues
        Set<String> titles = manager.getValues(PropertyNames.TITLE);
        Set<String> identifiers = manager.getValues(PropertyNames.IDENTIFIER);
        for (int i = 0; i < GEOPACKAGE_WITH_PROPERTIES_COUNT; i++) {
            TestCase.assertTrue(titles.contains(GEOPACKAGE_NAME + (i + 1)));
            TestCase.assertTrue(identifiers.contains(Integer.toString(i)));
        }
        Set<String> evenValues = manager.getValues(EVEN_PROPERTY);
        TestCase.assertTrue(evenValues.contains(Boolean.TRUE.toString()));
        TestCase.assertTrue(evenValues.contains(Boolean.FALSE.toString()));
        Set<String> oddValues = manager.getValues(ODD_PROPERTY);
        TestCase.assertTrue(oddValues.contains(Boolean.TRUE.toString()));
        Set<String> tags = manager.getValues(PropertyNames.TAG);
        TestCase.assertTrue(tags.contains(COLOR_RED));
        TestCase.assertTrue(tags.contains(COLOR_GREEN));
        TestCase.assertTrue(tags.contains(COLOR_BLUE));
        TestCase.assertTrue(manager.getValues(PropertyNames.CREATOR).isEmpty());

        // hasValue
        for (int i = 0; i < GEOPACKAGE_WITH_PROPERTIES_COUNT; i++) {
            TestCase.assertEquals(
                    1,
                    manager.hasValue(PropertyNames.TITLE,
                            GEOPACKAGE_NAME + (i + 1)).size());
            TestCase.assertEquals(
                    1,
                    manager.hasValue(PropertyNames.IDENTIFIER,
                            Integer.toString(i)).size());
        }
        TestCase.assertEquals(
                0,
                manager.hasValue(
                        PropertyNames.TITLE,
                        GEOPACKAGE_NAME
                                + (GEOPACKAGE_WITH_PROPERTIES_COUNT + 1))
                        .size());
        TestCase.assertEquals(
                0,
                manager.hasValue(PropertyNames.IDENTIFIER,
                        Integer.toString(GEOPACKAGE_WITH_PROPERTIES_COUNT))
                        .size());
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT / 2, manager
                .hasValue(EVEN_PROPERTY, Boolean.TRUE.toString()).size());
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT / 2, manager
                .hasValue(EVEN_PROPERTY, Boolean.FALSE.toString()).size());
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT / 2, manager
                .hasValue(ODD_PROPERTY, Boolean.TRUE.toString()).size());
        TestCase.assertEquals(0,
                manager.hasValue(ODD_PROPERTY, Boolean.FALSE.toString()).size());
        TestCase.assertEquals(COLOR_RED_COUNT,
                manager.hasValue(PropertyNames.TAG, COLOR_RED).size());
        TestCase.assertEquals(COLOR_GREEN_COUNT,
                manager.hasValue(PropertyNames.TAG, COLOR_GREEN).size());
        TestCase.assertEquals(COLOR_BLUE_COUNT,
                manager.hasValue(PropertyNames.TAG, COLOR_BLUE).size());
        TestCase.assertEquals(0, manager.hasValue(PropertyNames.TAG, "Yellow")
                .size());
        TestCase.assertEquals(0,
                manager.hasValue(PropertyNames.CREATOR, CREATOR).size());

        // missingValue
        TestCase.assertEquals(GEOPACKAGE_WITHOUT_PROPERTIES_COUNT
                        + GEOPACKAGE_WITH_PROPERTIES_COUNT / 2,
                manager.missingValue(EVEN_PROPERTY, Boolean.TRUE.toString())
                        .size());
        TestCase.assertEquals(GEOPACKAGE_WITHOUT_PROPERTIES_COUNT
                        + GEOPACKAGE_WITH_PROPERTIES_COUNT / 2,
                manager.missingValue(EVEN_PROPERTY, Boolean.FALSE.toString())
                        .size());
        TestCase.assertEquals(GEOPACKAGE_WITHOUT_PROPERTIES_COUNT
                        + GEOPACKAGE_WITH_PROPERTIES_COUNT / 2,
                manager.missingValue(ODD_PROPERTY, Boolean.TRUE.toString())
                        .size());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingValue(ODD_PROPERTY, Boolean.FALSE.toString())
                        .size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - COLOR_RED_COUNT, manager
                .missingValue(PropertyNames.TAG, COLOR_RED).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - COLOR_GREEN_COUNT, manager
                .missingValue(PropertyNames.TAG, COLOR_GREEN).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - COLOR_BLUE_COUNT, manager
                .missingValue(PropertyNames.TAG, COLOR_BLUE).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingValue(PropertyNames.TAG, "Yellow").size());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingValue(PropertyNames.CREATOR, CREATOR).size());

        // Add a property value to all GeoPackages
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.addValue(PropertyNames.CREATOR, CREATOR));
        TestCase.assertEquals(++numProperties, manager.numProperties());
        properties = manager.getProperties();
        TestCase.assertEquals(numProperties, properties.size());
        TestCase.assertTrue(properties.contains(PropertyNames.CREATOR));
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.hasProperty(PropertyNames.CREATOR).size());
        TestCase.assertEquals(0, manager.missingProperty(PropertyNames.CREATOR)
                .size());
        TestCase.assertEquals(1, manager.numValues(PropertyNames.CREATOR));
        TestCase.assertTrue(manager.hasValues(PropertyNames.CREATOR));
        TestCase.assertTrue(manager.getValues(PropertyNames.CREATOR).contains(
                CREATOR));
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.hasValue(PropertyNames.CREATOR, CREATOR).size());
        TestCase.assertEquals(0,
                manager.missingValue(PropertyNames.CREATOR, CREATOR).size());

        // Add a property value to a single GeoPackage
        TestCase.assertFalse(manager.addValue(GEOPACKAGE_NAME
                + GEOPACKAGE_COUNT, PropertyNames.CREATOR, CREATOR));
        TestCase.assertTrue(manager.addValue(
                GEOPACKAGE_NAME + GEOPACKAGE_COUNT, PropertyNames.CONTRIBUTOR,
                CREATOR));
        TestCase.assertEquals(++numProperties, manager.numProperties());
        properties = manager.getProperties();
        TestCase.assertEquals(numProperties, properties.size());
        TestCase.assertTrue(properties.contains(PropertyNames.CONTRIBUTOR));
        TestCase.assertEquals(1, manager.hasProperty(PropertyNames.CONTRIBUTOR)
                .size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - 1,
                manager.missingProperty(PropertyNames.CONTRIBUTOR).size());
        TestCase.assertEquals(1, manager.numValues(PropertyNames.CONTRIBUTOR));
        TestCase.assertTrue(manager.hasValues(PropertyNames.CONTRIBUTOR));
        TestCase.assertTrue(manager.getValues(PropertyNames.CONTRIBUTOR)
                .contains(CREATOR));
        TestCase.assertEquals(1,
                manager.hasValue(PropertyNames.CONTRIBUTOR, CREATOR).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - 1,
                manager.missingValue(PropertyNames.CONTRIBUTOR, CREATOR).size());

        // Add an identifier to GeoPackages without one, one at a time
        List<GeoPackage> missingIdentifiers = manager
                .missingProperty(PropertyNames.IDENTIFIER);
        TestCase.assertEquals(GEOPACKAGE_WITHOUT_PROPERTIES_COUNT,
                missingIdentifiers.size());
        int indentifierIndex = 100;
        for (GeoPackage missingIdentifierGeoPackage : missingIdentifiers) {
            TestCase.assertTrue(manager.addValue(
                    missingIdentifierGeoPackage.getName(),
                    PropertyNames.IDENTIFIER,
                    Integer.toString(indentifierIndex++)));
        }
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.hasProperty(PropertyNames.IDENTIFIER).size());
        TestCase.assertEquals(0,
                manager.missingProperty(PropertyNames.IDENTIFIER).size());

        // Add an identifier to GeoPackages without one, all at once
        TestCase.assertEquals(GEOPACKAGE_COUNT - 1,
                manager.addValue(PropertyNames.CONTRIBUTOR, CREATOR));
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.hasProperty(PropertyNames.CONTRIBUTOR).size());
        TestCase.assertEquals(0,
                manager.missingProperty(PropertyNames.CONTRIBUTOR).size());

        // Delete a property from all GeoPackages
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.deleteProperty(PropertyNames.IDENTIFIER));
        TestCase.assertEquals(--numProperties, manager.numProperties());
        properties = manager.getProperties();
        TestCase.assertEquals(numProperties, properties.size());
        TestCase.assertFalse(properties.contains(PropertyNames.IDENTIFIER));
        TestCase.assertEquals(0, manager.hasProperty(PropertyNames.IDENTIFIER)
                .size());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingProperty(PropertyNames.IDENTIFIER).size());
        TestCase.assertEquals(0, manager.numValues(PropertyNames.IDENTIFIER));
        TestCase.assertFalse(manager.hasValues(PropertyNames.IDENTIFIER));
        TestCase.assertEquals(0, manager.getValues(PropertyNames.IDENTIFIER)
                .size());
        TestCase.assertEquals(0, manager
                .hasValue(PropertyNames.IDENTIFIER, "1").size());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingValue(PropertyNames.IDENTIFIER, "1").size());

        // Delete a property from a single GeoPackage
        TestCase.assertTrue(manager.deleteProperty(GEOPACKAGE_NAME + "1",
                PropertyNames.TAG));
        TestCase.assertEquals(numProperties, manager.numProperties());
        properties = manager.getProperties();
        TestCase.assertEquals(numProperties, properties.size());
        TestCase.assertTrue(properties.contains(PropertyNames.TAG));
        TestCase.assertEquals(--numTagged,
                manager.hasProperty(PropertyNames.TAG).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - numTagged, manager
                .missingProperty(PropertyNames.TAG).size());
        TestCase.assertEquals(3, manager.numValues(PropertyNames.TAG));
        TestCase.assertTrue(manager.hasValues(PropertyNames.TAG));
        TestCase.assertTrue(manager.getValues(PropertyNames.TAG).contains(
                COLOR_RED));
        TestCase.assertEquals(COLOR_RED_COUNT - 1,
                manager.hasValue(PropertyNames.TAG, COLOR_RED).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - (COLOR_RED_COUNT - 1), manager
                .missingValue(PropertyNames.TAG, COLOR_RED).size());

        // Delete a property value from all GeoPackages
        TestCase.assertEquals(COLOR_RED_COUNT - 1,
                manager.deleteValue(PropertyNames.TAG, COLOR_RED));
        TestCase.assertEquals(numProperties, manager.numProperties());
        properties = manager.getProperties();
        TestCase.assertEquals(numProperties, properties.size());
        TestCase.assertTrue(properties.contains(PropertyNames.TAG));
        TestCase.assertEquals(--numTagged,
                manager.hasProperty(PropertyNames.TAG).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - numTagged, manager
                .missingProperty(PropertyNames.TAG).size());
        TestCase.assertEquals(2, manager.numValues(PropertyNames.TAG));
        TestCase.assertTrue(manager.hasValues(PropertyNames.TAG));
        TestCase.assertFalse(manager.getValues(PropertyNames.TAG).contains(
                COLOR_RED));
        TestCase.assertTrue(manager.getValues(PropertyNames.TAG).contains(
                COLOR_GREEN));
        TestCase.assertEquals(0, manager.hasValue(PropertyNames.TAG, COLOR_RED)
                .size());
        TestCase.assertEquals(COLOR_GREEN_COUNT - 1,
                manager.hasValue(PropertyNames.TAG, COLOR_GREEN).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingValue(PropertyNames.TAG, COLOR_RED).size());

        // Delete a property value from a single GeoPackage
        TestCase.assertTrue(manager.deleteValue(GEOPACKAGE_NAME
                + (COLOR_GREEN_FREQUENCY + 1), PropertyNames.TAG, COLOR_GREEN));
        TestCase.assertEquals(numProperties, manager.numProperties());
        properties = manager.getProperties();
        TestCase.assertEquals(numProperties, properties.size());
        TestCase.assertTrue(properties.contains(PropertyNames.TAG));
        TestCase.assertEquals(--numTagged,
                manager.hasProperty(PropertyNames.TAG).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - numTagged, manager
                .missingProperty(PropertyNames.TAG).size());
        TestCase.assertEquals(2, manager.numValues(PropertyNames.TAG));
        TestCase.assertTrue(manager.hasValues(PropertyNames.TAG));
        TestCase.assertTrue(manager.getValues(PropertyNames.TAG).contains(
                COLOR_GREEN));
        TestCase.assertEquals(COLOR_GREEN_COUNT - 2,
                manager.hasValue(PropertyNames.TAG, COLOR_GREEN).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT - (COLOR_GREEN_COUNT - 2),
                manager.missingValue(PropertyNames.TAG, COLOR_GREEN).size());

        // Delete all properties from a single GeoPackage
        TestCase.assertTrue(manager.deleteAll(GEOPACKAGE_NAME + 2));
        TestCase.assertEquals(numProperties, manager.numProperties());
        properties = manager.getProperties();
        TestCase.assertEquals(numProperties, properties.size());
        TestCase.assertTrue(properties.contains(PropertyNames.TITLE));
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT - 1, manager
                .hasProperty(PropertyNames.TITLE).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT
                - (GEOPACKAGE_WITH_PROPERTIES_COUNT - 1), manager
                .missingProperty(PropertyNames.TITLE).size());
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT - 1,
                manager.numValues(PropertyNames.TITLE));
        TestCase.assertTrue(manager.hasValues(PropertyNames.TITLE));
        TestCase.assertFalse(manager.getValues(PropertyNames.TITLE).contains(
                GEOPACKAGE_NAME + 2));
        TestCase.assertTrue(manager.getValues(PropertyNames.TITLE).contains(
                GEOPACKAGE_NAME + 3));
        TestCase.assertEquals(0,
                manager.hasValue(PropertyNames.TITLE, GEOPACKAGE_NAME + 2)
                        .size());
        TestCase.assertEquals(1,
                manager.hasValue(PropertyNames.TITLE, GEOPACKAGE_NAME + 3)
                        .size());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingValue(PropertyNames.TITLE, GEOPACKAGE_NAME + 2)
                        .size());

        // Remove the extension from a single GeoPackage
        manager.removeExtension(GEOPACKAGE_NAME + 4);
        TestCase.assertEquals(numProperties, manager.numProperties());
        properties = manager.getProperties();
        TestCase.assertEquals(numProperties, properties.size());
        TestCase.assertTrue(properties.contains(PropertyNames.TITLE));
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT - 2, manager
                .hasProperty(PropertyNames.TITLE).size());
        TestCase.assertEquals(GEOPACKAGE_COUNT
                - (GEOPACKAGE_WITH_PROPERTIES_COUNT - 2), manager
                .missingProperty(PropertyNames.TITLE).size());
        TestCase.assertEquals(GEOPACKAGE_WITH_PROPERTIES_COUNT - 2,
                manager.numValues(PropertyNames.TITLE));
        TestCase.assertTrue(manager.hasValues(PropertyNames.TITLE));
        TestCase.assertFalse(manager.getValues(PropertyNames.TITLE).contains(
                GEOPACKAGE_NAME + 4));
        TestCase.assertTrue(manager.getValues(PropertyNames.TITLE).contains(
                GEOPACKAGE_NAME + 3));
        TestCase.assertEquals(0,
                manager.hasValue(PropertyNames.TITLE, GEOPACKAGE_NAME + 4)
                        .size());
        TestCase.assertEquals(1,
                manager.hasValue(PropertyNames.TITLE, GEOPACKAGE_NAME + 3)
                        .size());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingValue(PropertyNames.TITLE, GEOPACKAGE_NAME + 4)
                        .size());

        // Delete all properties from all GeoPackages
        TestCase.assertEquals(GEOPACKAGE_COUNT - 2, manager.deleteAll());
        TestCase.assertEquals(0, manager.numProperties());
        TestCase.assertTrue(manager.getProperties().isEmpty());
        TestCase.assertTrue(manager.hasProperty(PropertyNames.TITLE).isEmpty());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingProperty(PropertyNames.TITLE).size());
        TestCase.assertEquals(0, manager.numValues(PropertyNames.TITLE));
        TestCase.assertFalse(manager.hasValues(PropertyNames.TITLE));
        TestCase.assertTrue(manager.getValues(PropertyNames.TITLE).isEmpty());
        TestCase.assertTrue(manager.hasValue(PropertyNames.TITLE,
                GEOPACKAGE_NAME + 3).isEmpty());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingValue(PropertyNames.TITLE, GEOPACKAGE_NAME + 3)
                        .size());

        // Remove the extension from all GeoPackages
        manager.removeExtension();
        TestCase.assertEquals(0, manager.numProperties());
        TestCase.assertTrue(manager.getProperties().isEmpty());
        TestCase.assertTrue(manager.hasProperty(PropertyNames.TITLE).isEmpty());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingProperty(PropertyNames.TITLE).size());
        TestCase.assertEquals(0, manager.numValues(PropertyNames.TITLE));
        TestCase.assertFalse(manager.hasValues(PropertyNames.TITLE));
        TestCase.assertTrue(manager.getValues(PropertyNames.TITLE).isEmpty());
        TestCase.assertTrue(manager.hasValue(PropertyNames.TITLE,
                GEOPACKAGE_NAME + 3).isEmpty());
        TestCase.assertEquals(GEOPACKAGE_COUNT,
                manager.missingValue(PropertyNames.TITLE, GEOPACKAGE_NAME + 3)
                        .size());

        // Close the GeoPackages
        manager.closeGeoPackages();
        TestCase.assertEquals(0, manager.numGeoPackages());
    }
}
