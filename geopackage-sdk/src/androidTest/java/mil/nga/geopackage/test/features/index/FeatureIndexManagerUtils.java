package mil.nga.geopackage.test.features.index;

import android.app.Activity;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.schema.TableColumnKey;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.io.TestGeoPackageProgress;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
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
        testIndex(activity, geoPackage, FeatureIndexType.GEOPACKAGE, false);
        testIndex(activity, geoPackage, FeatureIndexType.METADATA, false);
        //testIndex(activity, geoPackage, FeatureIndexType.RTREE, true); // TODO RTree not supported
    }

    private static void testIndex(Activity activity, GeoPackage geoPackage, FeatureIndexType type,
                                  boolean includeEmpty) throws SQLException {

        // Test indexing each feature table
        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                    geoPackage, featureDao);
            featureIndexManager.setIndexLocation(type);
            featureIndexManager.deleteAllIndexes();

            // Determine how many features have geometry envelopes or geometries
            int expectedCount = 0;
            FeatureRow testFeatureRow = null;
            FeatureCursor featureCursor = featureDao.queryForAll();
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

            featureIndexManager.close();
        }

        // Delete the extensions
        boolean everyOther = false;
        for (String featureTable : featureTables) {
            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                    geoPackage, featureDao);
            featureIndexManager.setIndexLocation(type);
            TestCase.assertTrue(featureIndexManager.isIndexed());

            // Test deleting a single geometry index
            if (everyOther) {
                FeatureCursor featureCursor = featureDao.queryForAll();
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
     * @throws SQLException upon error
     */
    public static void testLargeIndex(Activity activity, GeoPackage geoPackage, int numFeatures)
            throws SQLException {

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
        geometryColumns = geoPackage.createFeatureTableWithMetadata(
                geometryColumns, boundingBox, srs.getId());

        FeatureDao featureDao = geoPackage.getFeatureDao(geometryColumns);

        System.out.println();
        System.out.println("Inserting Feature Rows: " + numFeatures);
        TestUtils.addRowsToFeatureTable(geoPackage, geometryColumns,
                featureDao.getTable(), numFeatures, false, false, false);

        testLargeIndex(activity, geoPackage, featureTable, true, false);
    }

    /**
     * Test large index
     *
     * @param geoPackage              GeoPackage
     * @param compareProjectionCounts compare projection counts and query counts
     * @param verbose                 verbose printing
     * @throws SQLException upon error
     */
    public static void testLargeIndex(Activity activity, GeoPackage geoPackage,
                                      boolean compareProjectionCounts, boolean verbose)
            throws SQLException {
        for (String featureTable : geoPackage.getFeatureTables()) {
            testLargeIndex(activity, geoPackage, featureTable, compareProjectionCounts,
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
    public static void testLargeIndex(Activity activity, GeoPackage geoPackage,
                                      String featureTable, boolean compareProjectionCounts,
                                      boolean verbose) throws SQLException {

        FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);

        System.out.println();
        System.out.println("+++++++++++++++++++++++++++++++++++++");
        System.out.println("Large Index Test");
        System.out.println("Features: " + featureDao.count());
        System.out.println("+++++++++++++++++++++++++++++++++++++");

        GeometryEnvelope envelope = null;
        FeatureCursor cursor = featureDao.queryForAll();
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

        cursor = featureDao.queryForAll();
        while (cursor.moveToNext()) {
            FeatureRow featureRow = cursor.getRow();
            GeometryEnvelope rowEnvelope = featureRow.getGeometryEnvelope();
            if (rowEnvelope != null) {
                BoundingBox rowBoundingBox = new BoundingBox(rowEnvelope);
                for (FeatureIndexTestEnvelope testEnvelope : envelopes) {
                    if (TileBoundingBoxUtils.overlap(rowBoundingBox,
                            new BoundingBox(testEnvelope.envelope), true) != null) {
                        testEnvelope.count++;
                    }
                }
            }
        }
        cursor.close();

        testLargeIndex(activity, geoPackage, FeatureIndexType.GEOPACKAGE, featureDao,
                envelopes, compareProjectionCounts, verbose);
        testLargeIndex(activity, geoPackage, FeatureIndexType.METADATA, featureDao,
                envelopes, compareProjectionCounts, verbose);
        //testLargeIndex(activity, geoPackage, FeatureIndexType.RTREE, featureDao,
        //        envelopes, compareProjectionCounts, verbose); // TODO RTree not supported
        testLargeIndex(activity, geoPackage, FeatureIndexType.NONE, featureDao,
                envelopes, compareProjectionCounts, verbose);
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

        float percentageRatio = percentage / 100.0f;

        FeatureIndexTestEnvelope testEnvelope = new FeatureIndexTestEnvelope();

        double width = envelope.getMaxX() - envelope.getMinX();
        double height = envelope.getMaxY() - envelope.getMinY();

        double minX = envelope.getMinX()
                + (Math.random() * width * (1.0 - percentageRatio));
        double minY = envelope.getMinY()
                + (Math.random() * height * (1.0 - percentageRatio));

        double maxX = minX + (width * percentageRatio);
        double maxY = minY + (height * percentageRatio);

        testEnvelope.envelope = new GeometryEnvelope(minX, minY, maxX, maxY);
        testEnvelope.percentage = percentage;

        return testEnvelope;
    }

    private static void testLargeIndex(Activity activity, GeoPackage geoPackage,
                                       FeatureIndexType type, FeatureDao featureDao,
                                       List<FeatureIndexTestEnvelope> envelopes,
                                       boolean compareProjectionCounts, boolean verbose) {

        System.out.println();
        System.out.println("-------------------------------------");
        System.out.println("Type: " + type);
        System.out.println("-------------------------------------");
        System.out.println();

        int featureCount = featureDao.count();

        FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                geoPackage, featureDao);
        featureIndexManager.setIndexLocation(type);
        featureIndexManager.deleteAllIndexes();

        TestTimer timerQuery = new FeatureIndexManagerUtils().new TestTimer();
        TestTimer timerCount = new FeatureIndexManagerUtils().new TestTimer();
        timerCount.print = verbose;

        if (type != FeatureIndexType.NONE) {
            timerQuery.start();
            int indexCount = featureIndexManager.index();
            timerQuery.end("Index");
            TestCase.assertEquals(featureCount, indexCount);

            TestCase.assertTrue(featureIndexManager.isIndexed());
        } else {
            TestCase.assertFalse(featureIndexManager.isIndexed());
        }

        timerCount.start();
        TestCase.assertEquals(featureCount, featureIndexManager.count());
        timerCount.end("Count Query");

        timerQuery.reset();
        timerCount.reset();

        timerQuery.print = timerCount.print;

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
            TestCase.assertEquals(expectedCount, results.count());
            results.close();

            Projection projection = featureDao.getProjection();
            Projection webMercatorProjection = ProjectionFactory.getProjection(
                    ProjectionConstants.AUTHORITY_EPSG,
                    ProjectionConstants.EPSG_WEB_MERCATOR);
            ProjectionTransform transformToWebMercator = projection
                    .getTransformation(webMercatorProjection);

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

        featureIndexManager.close();
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
