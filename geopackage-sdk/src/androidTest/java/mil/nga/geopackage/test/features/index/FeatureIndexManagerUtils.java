package mil.nga.geopackage.test.features.index;

import android.app.Activity;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.TableColumnKey;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.test.GeoPackageTestUtils;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.io.TestGeoPackageProgress;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;
import mil.nga.sf.Point;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;
import mil.nga.sf.util.GeometryEnvelopeBuilder;

/**
 * Feature Index Manager Utility test methods
 *
 * @author osbornb
 */
public class FeatureIndexManagerUtils {

    /**
     * Test index
     *
     * @param activity   activity
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testIndex(Activity activity, GeoPackage geoPackage) throws SQLException {
        geoPackage.getCursorFactory().setDebugLogQueries(true);
        testIndex(activity, geoPackage, FeatureIndexType.RTREE, true);
        testIndex(activity, geoPackage, FeatureIndexType.GEOPACKAGE, false);
        testIndex(activity, geoPackage, FeatureIndexType.METADATA, false);
    }

    private static void testIndex(Activity activity, GeoPackage geoPackage, FeatureIndexType type,
                                  boolean includeEmpty) throws SQLException {

        // Test indexing each feature table
        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                    geoPackage, featureDao);
            featureIndexManager.setContinueOnError(false);
            featureIndexManager.setIndexLocation(type);
            if (type == FeatureIndexType.RTREE) {
                if (!featureIndexManager.isIndexed(type)) {
                    featureIndexManager.close();
                    continue;
                }
            } else {
                featureIndexManager.deleteAllIndexes();
            }

            // Determine how many features have geometry envelopes or geometries
            int expectedCount = 0;
            FeatureRow testFeatureRow = null;
            FeatureCursor featureCursor = featureDao.query();
            while (featureCursor.moveToNext()) {
                FeatureRow featureRow = featureCursor.getRow();
                if (featureRow.getGeometryEnvelope() != null) {
                    expectedCount++;
                    // Randomly choose a feature row with Geometry for testing
                    // queries later
                    if (testFeatureRow == null) {
                        testFeatureRow = featureRow;
                    } else if (Math.random() < (1.0 / featureCursor
                            .getCount())) {
                        testFeatureRow = featureRow;
                    }
                } else if (includeEmpty) {
                    expectedCount++;
                }
            }
            featureCursor.close();

            if (type != FeatureIndexType.RTREE) {

                TestCase.assertFalse(featureIndexManager.isIndexed());
                TestCase.assertNull(featureIndexManager.getLastIndexed());
                Date currentDate = new Date();

                // Test indexing
                TestGeoPackageProgress progress = new TestGeoPackageProgress();
                featureIndexManager.setProgress(progress);
                int indexCount = featureIndexManager.index();
                TestCase.assertEquals(expectedCount, indexCount);
                TestCase.assertEquals(featureDao.count(), progress.getProgress());
                TestCase.assertNotNull(featureIndexManager.getLastIndexed());
                Date lastIndexed = featureIndexManager.getLastIndexed();
                TestCase.assertTrue(lastIndexed.getTime() > currentDate.getTime());

                TestCase.assertTrue(featureIndexManager.isIndexed());
                TestCase.assertEquals(expectedCount, featureIndexManager.count());

                // Test re-indexing, both ignored and forced
                TestCase.assertEquals(0, featureIndexManager.index());
                TestCase.assertEquals(expectedCount,
                        featureIndexManager.index(true));
                TestCase.assertTrue(featureIndexManager.getLastIndexed().getTime() > lastIndexed
                        .getTime());

            }

            // Query for all indexed geometries
            int resultCount = 0;
            FeatureIndexResults featureIndexResults = featureIndexManager
                    .query();
            for (FeatureRow featureRow : featureIndexResults) {
                validateFeatureRow(featureIndexManager, featureRow, null,
                        includeEmpty);
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertEquals(expectedCount, resultCount);

            // Query for all indexed geometries with columns
            resultCount = 0;
            featureIndexResults = featureIndexManager
                    .query(featureDao.getIdAndGeometryColumnNames());
            for (FeatureRow featureRow : featureIndexResults) {
                validateFeatureRow(featureIndexManager, featureRow, null,
                        includeEmpty);
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertEquals(expectedCount, resultCount);

            // Test the query by envelope
            GeometryEnvelope envelope = testFeatureRow.getGeometryEnvelope();
            final double difference = .000001;
            envelope.setMinX(envelope.getMinX() - difference);
            envelope.setMaxX(envelope.getMaxX() + difference);
            envelope.setMinY(envelope.getMinY() - difference);
            envelope.setMaxY(envelope.getMaxY() + difference);
            if (envelope.hasZ()) {
                envelope.setMinZ(envelope.getMinZ() - difference);
                envelope.setMaxZ(envelope.getMaxZ() + difference);
            }
            if (envelope.hasM()) {
                envelope.setMinM(envelope.getMinM() - difference);
                envelope.setMaxM(envelope.getMaxM() + difference);
            }
            resultCount = 0;
            boolean featureFound = false;
            TestCase.assertTrue(featureIndexManager.count(envelope) >= 1);
            featureIndexResults = featureIndexManager.query(envelope);
            for (FeatureRow featureRow : featureIndexResults) {
                validateFeatureRow(featureIndexManager, featureRow, envelope,
                        includeEmpty);
                if (featureRow.getId() == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);
            resultCount = 0;
            featureFound = false;
            featureIndexResults = featureIndexManager
                    .query(featureDao.getIdAndGeometryColumnNames(), envelope);
            for (FeatureRow featureRow : featureIndexResults) {
                validateFeatureRow(featureIndexManager, featureRow, envelope,
                        includeEmpty);
                if (featureRow.getId() == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);

            // Test the query by envelope with id iteration
            resultCount = 0;
            featureFound = false;
            TestCase.assertTrue(featureIndexManager.count(envelope) >= 1);
            featureIndexResults = featureIndexManager.query(envelope);
            for (long featureRowId : featureIndexResults.ids()) {
                FeatureRow featureRow = featureDao.queryForIdRow(featureRowId);
                validateFeatureRow(featureIndexManager, featureRow, envelope,
                        includeEmpty);
                if (featureRowId == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);

            // Pick a projection different from the feature dao and project the
            // bounding box
            BoundingBox boundingBox = new BoundingBox(envelope.getMinX() - 1,
                    envelope.getMinY() - 1, envelope.getMaxX() + 1,
                    envelope.getMaxY() + 1);
            Projection projection = null;
            if (!featureDao.getProjection().equals(
                    ProjectionConstants.AUTHORITY_EPSG,
                    ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)) {
                projection = ProjectionFactory
                        .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            } else {
                projection = ProjectionFactory
                        .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
            }
            ProjectionTransform transform = featureDao.getProjection()
                    .getTransformation(projection);
            BoundingBox transformedBoundingBox = boundingBox
                    .transform(transform);

            // Test the query by projected bounding box
            resultCount = 0;
            featureFound = false;
            TestCase.assertTrue(featureIndexManager.count(
                    transformedBoundingBox, projection) >= 1);
            featureIndexResults = featureIndexManager.query(
                    transformedBoundingBox, projection);
            for (FeatureRow featureRow : featureIndexResults) {
                validateFeatureRow(featureIndexManager, featureRow,
                        boundingBox.buildEnvelope(), includeEmpty);
                if (featureRow.getId() == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);
            resultCount = 0;
            featureFound = false;
            featureIndexResults = featureIndexManager.query(
                    featureDao.getIdAndGeometryColumnNames(),
                    transformedBoundingBox, projection);
            for (FeatureRow featureRow : featureIndexResults) {
                validateFeatureRow(featureIndexManager, featureRow,
                        boundingBox.buildEnvelope(), includeEmpty);
                if (featureRow.getId() == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);

            // Test query by criteria
            FeatureTable table = featureDao.getTable();
            List<FeatureColumn> columns = table.getColumns();

            Map<String, Number> numbers = new HashMap<>();
            Map<String, String> strings = new HashMap<>();

            for (FeatureColumn column : columns) {
                if (column.isPrimaryKey() || column.isGeometry()) {
                    continue;
                }
                GeoPackageDataType dataType = column.getDataType();
                switch (dataType) {
                    case DOUBLE:
                    case FLOAT:
                    case INT:
                    case INTEGER:
                    case TINYINT:
                    case SMALLINT:
                    case MEDIUMINT:
                    case REAL:
                        numbers.put(column.getName(), null);
                        break;
                    case TEXT:
                        strings.put(column.getName(), null);
                        break;
                    default:

                }
            }

            for (String number : numbers.keySet()) {
                Object value = testFeatureRow.getValue(number);
                numbers.put(number, (Number) value);
            }

            for (String string : strings.keySet()) {
                String value = testFeatureRow.getValueString(string);
                strings.put(string, value);
            }

            for (Map.Entry<String, Number> number : numbers.entrySet()) {

                String column = number.getKey();
                double value = number.getValue().doubleValue();

                String where = column + " >= ? AND " + column + " <= ?";
                String[] whereArgs = new String[]{
                        String.valueOf(value - 0.001),
                        String.valueOf(value + 0.001)};

                long count = featureIndexManager.count(where, whereArgs);
                TestCase.assertTrue(count >= 1);
                featureIndexResults = featureIndexManager.query(where,
                        whereArgs);
                TestCase.assertEquals(count, featureIndexResults.count());
                for (FeatureRow featureRow : featureIndexResults) {
                    TestCase.assertEquals(value,
                            ((Number) featureRow.getValue(column))
                                    .doubleValue());
                }
                featureIndexResults.close();
                featureIndexResults = featureIndexManager
                        .query(new String[]{column}, where, whereArgs);
                TestCase.assertEquals(count, featureIndexResults.count());
                for (FeatureRow featureRow : featureIndexResults) {
                    TestCase.assertEquals(value,
                            ((Number) featureRow.getValue(column))
                                    .doubleValue());
                }
                featureIndexResults.close();

                resultCount = 0;
                featureFound = false;

                count = featureIndexManager.count(transformedBoundingBox,
                        projection, where, whereArgs);
                TestCase.assertTrue(count >= 1);
                featureIndexResults = featureIndexManager.query(
                        transformedBoundingBox, projection, where, whereArgs);
                TestCase.assertEquals(count, featureIndexResults.count());
                for (FeatureRow featureRow : featureIndexResults) {
                    TestCase.assertEquals(value,
                            ((Number) featureRow.getValue(column))
                                    .doubleValue());
                    validateFeatureRow(featureIndexManager, featureRow,
                            boundingBox.buildEnvelope(), includeEmpty);
                    if (featureRow.getId() == testFeatureRow.getId()) {
                        featureFound = true;
                    }
                    resultCount++;
                }
                featureIndexResults.close();
                TestCase.assertTrue(featureFound);
                TestCase.assertTrue(resultCount >= 1);

                resultCount = 0;
                featureFound = false;

                featureIndexResults = featureIndexManager.query(
                        new String[]{featureDao.getGeometryColumnName(),
                                column, featureDao.getIdColumnName()},
                        transformedBoundingBox, projection, where, whereArgs);
                TestCase.assertEquals(count, featureIndexResults.count());
                for (FeatureRow featureRow : featureIndexResults) {
                    TestCase.assertEquals(value,
                            ((Number) featureRow.getValue(column))
                                    .doubleValue());
                    validateFeatureRow(featureIndexManager, featureRow,
                            boundingBox.buildEnvelope(), includeEmpty);
                    if (featureRow.getId() == testFeatureRow.getId()) {
                        featureFound = true;
                    }
                    resultCount++;
                }
                featureIndexResults.close();
                TestCase.assertTrue(featureFound);
                TestCase.assertTrue(resultCount >= 1);
            }

            for (Map.Entry<String, String> string : strings.entrySet()) {

                String column = string.getKey();
                String value = string.getValue();

                Map<String, Object> fieldValues = new HashMap<>();
                fieldValues.put(column, value);

                long count = featureIndexManager.count(fieldValues);
                TestCase.assertTrue(count >= 1);
                featureIndexResults = featureIndexManager.query(fieldValues);
                TestCase.assertEquals(count, featureIndexResults.count());
                for (FeatureRow featureRow : featureIndexResults) {
                    TestCase.assertEquals(value,
                            featureRow.getValueString(column));
                }
                featureIndexResults.close();
                featureIndexResults = featureIndexManager
                        .query(new String[]{column}, fieldValues);
                TestCase.assertEquals(count, featureIndexResults.count());
                for (FeatureRow featureRow : featureIndexResults) {
                    TestCase.assertEquals(value,
                            featureRow.getValueString(column));
                }
                featureIndexResults.close();

                resultCount = 0;
                featureFound = false;

                count = featureIndexManager.count(transformedBoundingBox,
                        projection, fieldValues);
                TestCase.assertTrue(count >= 1);
                featureIndexResults = featureIndexManager
                        .query(transformedBoundingBox, projection, fieldValues);
                TestCase.assertEquals(count, featureIndexResults.count());
                for (FeatureRow featureRow : featureIndexResults) {
                    TestCase.assertEquals(value,
                            featureRow.getValueString(column));
                    validateFeatureRow(featureIndexManager, featureRow,
                            boundingBox.buildEnvelope(), includeEmpty);
                    if (featureRow.getId() == testFeatureRow.getId()) {
                        featureFound = true;
                    }
                    resultCount++;
                }
                featureIndexResults.close();
                TestCase.assertTrue(featureFound);
                TestCase.assertTrue(resultCount >= 1);

                resultCount = 0;
                featureFound = false;

                featureIndexResults = featureIndexManager.query(
                        new String[]{column, featureDao.getIdColumnName(),
                                featureDao.getGeometryColumnName()},
                        transformedBoundingBox, projection, fieldValues);
                TestCase.assertEquals(count, featureIndexResults.count());
                for (FeatureRow featureRow : featureIndexResults) {
                    TestCase.assertEquals(value,
                            featureRow.getValueString(column));
                    validateFeatureRow(featureIndexManager, featureRow,
                            boundingBox.buildEnvelope(), includeEmpty);
                    if (featureRow.getId() == testFeatureRow.getId()) {
                        featureFound = true;
                    }
                    resultCount++;
                }
                featureIndexResults.close();
                TestCase.assertTrue(featureFound);
                TestCase.assertTrue(resultCount >= 1);

            }

            if (type != FeatureIndexType.RTREE) {

                // Update a Geometry and update the index of a single feature row
                GeoPackageGeometryData geometryData = new GeoPackageGeometryData(
                        featureDao.getGeometryColumns().getSrsId());
                Point point = new Point(5, 5);
                geometryData.setGeometry(point);
                testFeatureRow.setGeometry(geometryData);
                TestCase.assertEquals(1, featureDao.update(testFeatureRow));
                Date lastIndexedBefore = featureIndexManager.getLastIndexed();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }
                TestCase.assertTrue(featureIndexManager.index(testFeatureRow));
                Date lastIndexedAfter = featureIndexManager.getLastIndexed();
                TestCase.assertTrue(lastIndexedAfter.after(lastIndexedBefore));

                // Verify the index was updated for the feature row
                envelope = GeometryEnvelopeBuilder.buildEnvelope(point);
                resultCount = 0;
                featureFound = false;
                TestCase.assertTrue(featureIndexManager.count(envelope) >= 1);
                featureIndexResults = featureIndexManager.query(envelope);
                for (FeatureRow featureRow : featureIndexResults) {
                    validateFeatureRow(featureIndexManager, featureRow, envelope,
                            includeEmpty);
                    if (featureRow.getId() == testFeatureRow.getId()) {
                        featureFound = true;
                    }
                    resultCount++;
                }
                featureIndexResults.close();
                TestCase.assertTrue(featureFound);
                TestCase.assertTrue(resultCount >= 1);

            }

            featureIndexManager.close();
        }

        // Delete the extensions
        boolean everyOther = false;
        for (String featureTable : featureTables) {
            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                    geoPackage, featureDao);
            featureIndexManager.setIndexLocation(type);
            if (type == FeatureIndexType.RTREE) {
                featureIndexManager.close();
                continue;
            }
            TestCase.assertTrue(featureIndexManager.isIndexed());

            // Test deleting a single geometry index
            if (everyOther) {
                FeatureCursor featureCursor = featureDao.query();
                while (featureCursor.moveToNext()) {
                    FeatureRow featureRow = featureCursor.getRow();
                    if (featureRow.getGeometryEnvelope() != null) {
                        featureCursor.close();
                        TestCase.assertTrue(featureIndexManager
                                .deleteIndex(featureRow));
                        break;
                    }
                }
                featureCursor.close();
            }

            featureIndexManager.deleteIndex();

            TestCase.assertFalse(featureIndexManager.isIndexed());
            everyOther = !everyOther;

            featureIndexManager.close();
        }

    }

    /**
     * Validate a Feature Row result
     *
     * @param featureIndexManager
     * @param featureRow
     * @param queryEnvelope
     */
    private static void validateFeatureRow(
            FeatureIndexManager featureIndexManager, FeatureRow featureRow,
            GeometryEnvelope queryEnvelope, boolean includeEmpty) {
        TestCase.assertNotNull(featureRow);
        GeometryEnvelope envelope = featureRow.getGeometryEnvelope();

        if (!includeEmpty) {
            TestCase.assertNotNull(envelope);

            if (queryEnvelope != null) {
                TestCase.assertTrue(envelope.getMinX() <= queryEnvelope
                        .getMaxX());
                TestCase.assertTrue(envelope.getMaxX() >= queryEnvelope
                        .getMinX());
                TestCase.assertTrue(envelope.getMinY() <= queryEnvelope
                        .getMaxY());
                TestCase.assertTrue(envelope.getMaxY() >= queryEnvelope
                        .getMinY());
                if (envelope.isHasZ()) {
                    if (queryEnvelope.hasZ()) {
                        TestCase.assertTrue(envelope.getMinZ() <= queryEnvelope
                                .getMaxZ());
                        TestCase.assertTrue(envelope.getMaxZ() >= queryEnvelope
                                .getMinZ());
                    }
                }
                if (envelope.isHasM()) {
                    if (queryEnvelope.hasM()) {
                        TestCase.assertTrue(envelope.getMinM() <= queryEnvelope
                                .getMaxM());
                        TestCase.assertTrue(envelope.getMaxM() >= queryEnvelope
                                .getMinM());
                    }
                }
            }
        }
    }

    /**
     * Test large index
     *
     * @param activity    activity
     * @param geoPackage  GeoPackage
     * @param numFeatures num features
     * @param verbose     verbose printing
     * @throws SQLException upon error
     */
    public static void testLargeIndex(Activity activity, GeoPackage geoPackage, int numFeatures,
                                      boolean verbose) throws SQLException {

        String featureTable = "large_index";

        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey(featureTable, "geom"));
        geometryColumns.setGeometryType(GeometryType.POLYGON);
        geometryColumns.setZ((byte) 0);
        geometryColumns.setM((byte) 0);

        BoundingBox boundingBox = new BoundingBox(-180, -90, 180, 90);

        SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
                .getOrCreateCode(ProjectionConstants.AUTHORITY_EPSG,
                        ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        List<FeatureColumn> additionalColumns = GeoPackageTestUtils
                .getFeatureColumns();
        geometryColumns = geoPackage.createFeatureTableWithMetadata(
                geometryColumns, additionalColumns, boundingBox, srs.getId());

        FeatureDao featureDao = geoPackage.getFeatureDao(geometryColumns);

        System.out.println();
        System.out.println("Inserting Feature Rows: " + numFeatures);
        TestUtils.addRowsToFeatureTable(geoPackage, geometryColumns,
                featureDao.getTable(), numFeatures, false, false, false);

        testTimedIndex(activity, geoPackage, featureTable, true, verbose);
    }

    /**
     * Test large index
     *
     * @param geoPackage              GeoPackage
     * @param compareProjectionCounts compare projection counts and query counts
     * @param verbose                 verbose printing
     * @throws SQLException upon error
     */
    public static void testTimedIndex(Activity activity, GeoPackage geoPackage,
                                      boolean compareProjectionCounts, boolean verbose)
            throws SQLException {
        for (String featureTable : geoPackage.getFeatureTables()) {
            testTimedIndex(activity, geoPackage, featureTable, compareProjectionCounts,
                    verbose);
        }
    }

    /**
     * Test large index
     *
     * @param geoPackage              GeoPackage
     * @param featureTable            feature table
     * @param compareProjectionCounts compare projection counts and query counts
     * @param verbose                 verbose printing
     * @throws SQLException upon error
     */
    public static void testTimedIndex(Activity activity, GeoPackage geoPackage,
                                      String featureTable, boolean compareProjectionCounts,
                                      boolean verbose) throws SQLException {

        FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);

        System.out.println();
        System.out.println("+++++++++++++++++++++++++++++++++++++");
        System.out.println("Timed Index Test");
        System.out.println(featureTable);
        System.out.println("Features: " + featureDao.count() + ", Columns: "
                + featureDao.columnCount());
        System.out.println("+++++++++++++++++++++++++++++++++++++");

        GeometryEnvelope envelope = null;
        FeatureCursor cursor = featureDao.query();
        while (cursor.moveToNext()) {
            FeatureRow featureRow = cursor.getRow();
            GeometryEnvelope rowEnvelope = featureRow.getGeometryEnvelope();
            if (envelope == null) {
                envelope = rowEnvelope;
            } else if (rowEnvelope != null) {
                envelope = envelope.union(rowEnvelope);
            }
        }
        cursor.close();

        List<FeatureIndexTestEnvelope> envelopes = createEnvelopes(envelope);

        cursor = featureDao.query();
        while (cursor.moveToNext()) {
            FeatureRow featureRow = cursor.getRow();
            GeometryEnvelope rowEnvelope = featureRow.getGeometryEnvelope();
            if (rowEnvelope != null) {
                BoundingBox rowBoundingBox = new BoundingBox(rowEnvelope);
                for (FeatureIndexTestEnvelope testEnvelope : envelopes) {
                    if (rowBoundingBox.intersects(new BoundingBox(testEnvelope.envelope), true)) {
                        testEnvelope.count++;
                    }
                }
            }
        }
        cursor.close();

        testTimedIndex(activity, geoPackage, FeatureIndexType.GEOPACKAGE, featureDao,
                envelopes, .0000000001, compareProjectionCounts, .001, verbose);
        testTimedIndex(activity, geoPackage, FeatureIndexType.METADATA, featureDao,
                envelopes, .0000000001, compareProjectionCounts, .001, verbose);
        testTimedIndex(activity, geoPackage, FeatureIndexType.RTREE, featureDao,
                envelopes, .0000000001, .0001, compareProjectionCounts, .001, verbose);
        testTimedIndex(activity, geoPackage, FeatureIndexType.NONE, featureDao,
                envelopes, .0000000001, compareProjectionCounts, .001, verbose);
    }

    private static List<FeatureIndexTestEnvelope> createEnvelopes(
            GeometryEnvelope envelope) {
        List<FeatureIndexTestEnvelope> envelopes = new ArrayList<>();
        for (int percentage = 100; percentage >= 0; percentage -= 10) {
            envelopes.add(createEnvelope(envelope, percentage));
        }
        return envelopes;
    }

    private static FeatureIndexTestEnvelope createEnvelope(
            GeometryEnvelope envelope, int percentage) {

        FeatureIndexTestEnvelope testEnvelope = new FeatureIndexTestEnvelope();

        double minX;
        double maxX;
        double minY;
        double maxY;

        if (percentage < 100) {

            float percentageRatio = percentage / 100.0f;

            double width = envelope.getMaxX() - envelope.getMinX();
            double height = envelope.getMaxY() - envelope.getMinY();

            minX = envelope.getMinX()
                    + (Math.random() * width * (1.0 - percentageRatio));
            minY = envelope.getMinY()
                    + (Math.random() * height * (1.0 - percentageRatio));

            maxX = minX + (width * percentageRatio);
            maxY = minY + (height * percentageRatio);

        } else {
            minX = envelope.getMinX();
            maxX = envelope.getMaxX();
            minY = envelope.getMinY();
            maxY = envelope.getMaxY();
        }

        testEnvelope.envelope = new GeometryEnvelope(minX, minY, maxX, maxY);
        testEnvelope.percentage = percentage;

        return testEnvelope;
    }

    private static void testTimedIndex(Activity activity, GeoPackage geoPackage,
                                       FeatureIndexType type, FeatureDao featureDao,
                                       List<FeatureIndexTestEnvelope> envelopes, double precision,
                                       boolean compareProjectionCounts, double projectionPrecision, boolean verbose) {
        testTimedIndex(activity, geoPackage, type, featureDao, envelopes, precision,
                precision, compareProjectionCounts, projectionPrecision, verbose);
    }

    private static void testTimedIndex(Activity activity, GeoPackage geoPackage,
                                       FeatureIndexType type, FeatureDao featureDao,
                                       List<FeatureIndexTestEnvelope> envelopes, double innerPrecision,
                                       double outerPrecision,
                                       boolean compareProjectionCounts, double projectionPrecision, boolean verbose) {

        System.out.println();
        System.out.println("-------------------------------------");
        System.out.println("Type: " + type);
        System.out.println("-------------------------------------");
        System.out.println();

        int geometryFeatureCount = featureDao.count(featureDao.getGeometryColumnName() + " IS NOT NULL");
        int totalFeatureCount = featureDao.count();

        FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                geoPackage, featureDao);
        featureIndexManager.setContinueOnError(false);
        try {

            featureIndexManager.setIndexLocation(type);
            featureIndexManager.prioritizeQueryLocation(type);

            if (type == FeatureIndexType.RTREE) {
                if (!featureIndexManager.isIndexed(FeatureIndexType.RTREE)) {
                    System.out.println("Not Indexed");
                    return;
                }
            } else if (type != FeatureIndexType.NONE) {
                featureIndexManager.deleteIndex(type);
                TestCase.assertFalse(featureIndexManager.isIndexed(type));
            } else {
                featureIndexManager.setIndexLocationOrder(type);
                TestCase.assertFalse(featureIndexManager.isIndexed());
            }

            TestTimer timerQuery = new FeatureIndexManagerUtils().new TestTimer();
            TestTimer timerCount = new FeatureIndexManagerUtils().new TestTimer();
            timerCount.print = verbose;

            if (type != FeatureIndexType.NONE && !featureIndexManager.isIndexed(type)) {
                timerQuery.start();
                int indexCount = featureIndexManager.index();
                timerQuery.end("Index");
                TestCase.assertEquals(geometryFeatureCount, indexCount);
                TestCase.assertTrue(featureIndexManager.isIndexed());
            }

            timerCount.start();
            long queryCount = featureIndexManager.count();
            timerCount.end("Count Query");
            TestCase.assertTrue(queryCount == geometryFeatureCount || queryCount == totalFeatureCount);

            Projection projection = featureDao.getProjection();
            Projection webMercatorProjection = ProjectionFactory.getProjection(
                    ProjectionConstants.AUTHORITY_EPSG,
                    ProjectionConstants.EPSG_WEB_MERCATOR);
            ProjectionTransform transformToWebMercator = projection
                    .getTransformation(webMercatorProjection);
            ProjectionTransform transformToProjection = webMercatorProjection
                    .getTransformation(projection);

            timerCount.start();
            BoundingBox bounds = featureIndexManager.getBoundingBox();
            timerCount.end("Bounds Query");
            TestCase.assertNotNull(bounds);
            FeatureIndexTestEnvelope firstEnvelope = envelopes.get(0);
            BoundingBox firstBounds = new BoundingBox(firstEnvelope.envelope);

            assertRange(firstBounds.getMinLongitude(), bounds.getMinLongitude(),
                    outerPrecision, innerPrecision);
            assertRange(firstBounds.getMinLatitude(), bounds.getMinLatitude(),
                    outerPrecision, innerPrecision);
            assertRange(firstBounds.getMaxLongitude(), bounds.getMaxLongitude(),
                    innerPrecision, outerPrecision);
            assertRange(firstBounds.getMaxLatitude(), bounds.getMaxLatitude(),
                    innerPrecision, outerPrecision);

            timerCount.start();
            BoundingBox projectedBounds = featureIndexManager
                    .getBoundingBox(webMercatorProjection);
            timerCount.end("Bounds Projection Query");
            TestCase.assertNotNull(projectedBounds);
            BoundingBox reprojectedBounds = projectedBounds
                    .transform(transformToProjection);

            assertRange(firstBounds.getMinLongitude(),
                    reprojectedBounds.getMinLongitude(), projectionPrecision,
                    projectionPrecision);
            assertRange(firstBounds.getMinLatitude(),
                    reprojectedBounds.getMinLatitude(), projectionPrecision,
                    projectionPrecision);
            assertRange(firstBounds.getMaxLongitude(),
                    reprojectedBounds.getMaxLongitude(), projectionPrecision,
                    projectionPrecision);
            assertRange(firstBounds.getMaxLatitude(),
                    reprojectedBounds.getMaxLatitude(), projectionPrecision,
                    projectionPrecision);

            timerQuery.reset();
            timerCount.reset();
            TestTimer timerIteration = new FeatureIndexManagerUtils().new TestTimer();
            TestTimer timerColumnsIteration = new FeatureIndexManagerUtils().new TestTimer();

            timerIteration.print = timerCount.print;
            timerColumnsIteration.print = timerCount.print;
            timerQuery.print = timerCount.print;

            String[] columns = featureDao.getIdAndGeometryColumnNames();

            for (FeatureIndexTestEnvelope testEnvelope : envelopes) {

                String percentage = Integer.toString(testEnvelope.percentage);
                GeometryEnvelope envelope = testEnvelope.envelope;
                int expectedCount = testEnvelope.count;

                if (verbose) {
                    System.out.println(percentage + "% Feature Count: "
                            + expectedCount);
                }

                timerCount.start();
                long fullCount = featureIndexManager.count(envelope);
                timerCount.end(percentage + "% Envelope Count Query");
                TestCase.assertEquals(expectedCount, fullCount);

                timerQuery.start();
                FeatureIndexResults results = featureIndexManager.query(envelope);
                timerQuery.end(percentage + "% Envelope Query");
                iterateResults(timerIteration,
                        percentage + "% Envelope Query Iteration", results);
                TestCase.assertEquals(expectedCount, results.count());
                results.close();

                timerQuery.start();
                results = featureIndexManager.query(columns, envelope);
                timerQuery.end(percentage + "% Envelope Columns Query");
                iterateResults(timerColumnsIteration,
                        percentage + "% Envelope Columns Query Iteration",
                        results);
                TestCase.assertEquals(expectedCount, results.count());
                results.close();

                BoundingBox boundingBox = new BoundingBox(envelope);
                timerCount.start();
                fullCount = featureIndexManager.count(boundingBox);
                timerCount.end(percentage + "% Bounding Box Count Query");
                TestCase.assertEquals(expectedCount, fullCount);

                timerQuery.start();
                results = featureIndexManager.query(boundingBox);
                timerQuery.end(percentage + "% Bounding Box Query");
                iterateResults(timerIteration,
                        percentage + "% Bounding Box Query Iteration", results);
                TestCase.assertEquals(expectedCount, results.count());
                results.close();

                timerQuery.start();
                results = featureIndexManager.query(columns, boundingBox);
                timerQuery.end(percentage + "% Bounding Box Columns Query");
                iterateResults(timerColumnsIteration,
                        percentage + "% Bounding Box Columns Query Iteration",
                        results);
                TestCase.assertEquals(expectedCount, results.count());
                results.close();

                BoundingBox webMercatorBoundingBox = boundingBox
                        .transform(transformToWebMercator);
                timerCount.start();
                fullCount = featureIndexManager.count(webMercatorBoundingBox,
                        webMercatorProjection);
                timerCount.end(percentage + "% Projected Bounding Box Count Query");
                if (compareProjectionCounts) {
                    TestCase.assertEquals(expectedCount, fullCount);
                }

                timerQuery.start();
                results = featureIndexManager.query(webMercatorBoundingBox,
                        webMercatorProjection);
                timerQuery.end(percentage + "% Projected Bounding Box Query");
                iterateResults(timerIteration,
                        percentage + "% Projected Bounding Box Query Iteration",
                        results);
                if (compareProjectionCounts) {
                    TestCase.assertEquals(expectedCount, results.count());
                }
                results.close();

                timerQuery.start();
                results = featureIndexManager.query(columns,
                        webMercatorBoundingBox, webMercatorProjection);
                timerQuery.end(
                        percentage + "% Projected Bounding Box Columns Query");
                iterateResults(timerColumnsIteration, percentage
                                + "% Projected Bounding Box Columns Query Iteration",
                        results);
                if (compareProjectionCounts) {
                    TestCase.assertEquals(expectedCount, results.count());
                }
                results.close();
            }

            System.out.println();
            System.out.println("Average Count: " + timerCount.averageString()
                    + " ms");
            System.out.println("Average Query: " + timerQuery.averageString()
                    + " ms");
            System.out.println("Average Iteration: "
                    + timerIteration.averageString() + " ms");
            System.out
                    .println("Average " + columns.length + " Column Iteration: "
                            + timerColumnsIteration.averageString() + " ms");
        } finally {
            featureIndexManager.close();
        }
    }

    private static void iterateResults(TestTimer timerIteration, String message,
                                       FeatureIndexResults results) {
        timerIteration.start();
        for (@SuppressWarnings("unused")
                FeatureRow featureRow : results) {
        }
        timerIteration.end(message);
    }

    private static void assertRange(double expected, double actual,
                                    double lowPrecision, double highPrecision) {
        double low = expected - lowPrecision;
        double high = expected + highPrecision;
        TestCase.assertTrue("Value: " + actual + ", not within range: " + low
                + " - " + high, low <= actual && actual <= high);
    }

    private class TestTimer {

        int count = 0;
        long totalTime = 0;
        Date before;
        boolean print = true;

        /**
         * Start the timer
         */
        public void start() {
            before = new Date();
        }

        /**
         * End the timer and print the output
         *
         * @param output output string
         */
        public void end(String output) {
            long time = new Date().getTime() - before.getTime();
            count++;
            totalTime += time;
            before = null;
            if (print) {
                System.out.println(output + ": " + time + " ms");
            }
        }

        /**
         * Get the average request time
         *
         * @return average milliseconds
         */
        public double average() {
            return (double) totalTime / count;
        }

        /**
         * Get the average request time as a string
         *
         * @return average milliseconds
         */
        public String averageString() {
            DecimalFormat formatter = new DecimalFormat("#.00");
            return formatter.format(average());
        }

        /**
         * Reset the timer
         */
        public void reset() {
            count = 0;
            totalTime = 0;
            before = null;
        }

    }

}
