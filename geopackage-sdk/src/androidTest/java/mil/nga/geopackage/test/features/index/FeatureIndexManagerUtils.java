package mil.nga.geopackage.test.features.index;

import android.app.Activity;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.test.io.TestGeoPackageProgress;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryEnvelope;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.util.GeometryEnvelopeBuilder;

/**
 * Feature Index Manager Utility test methods
 *
 * @author osbornb
 */
public class FeatureIndexManagerUtils {

    /**
     * Test read
     *
     * @param activity
     * @param geoPackage
     * @throws SQLException
     */
    public static void testIndex(Activity activity, GeoPackage geoPackage) throws SQLException {
        testIndex(activity, geoPackage, FeatureIndexType.GEOPACKAGE);
        testIndex(activity, geoPackage, FeatureIndexType.METADATA);
    }

    private static void testIndex(Activity activity, GeoPackage geoPackage, FeatureIndexType type) throws SQLException {

        // Test indexing each feature table
        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity, geoPackage, featureDao);
            featureIndexManager.setIndexLocation(type);

            // Determine how many features have geometry envelopes or geometries
            int expectedCount = 0;
            FeatureRow testFeatureRow = null;
            FeatureCursor featureCursor = featureDao.queryForAll();
            while (featureCursor.moveToNext()) {
                FeatureRow featureRow = featureCursor.getRow();
                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                if (geometryData != null
                        && (geometryData.getEnvelope() != null || geometryData
                        .getGeometry() != null)) {
                    expectedCount++;
                    // Randomly choose a feature row with Geometry for testing
                    // queries later
                    if (testFeatureRow == null) {
                        testFeatureRow = featureRow;
                    } else if (Math.random() < (1.0 / featureCursor
                            .getCount())) {
                        testFeatureRow = featureRow;
                    }
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
            TestCase.assertEquals(expectedCount, featureIndexManager.index(true));
            TestCase.assertTrue(featureIndexManager.getLastIndexed().getTime() > lastIndexed
                    .getTime());

            // Query for all indexed geometries
            int resultCount = 0;
            FeatureIndexResults featureIndexResults = featureIndexManager
                    .query();
            for (FeatureRow featureRow : featureIndexResults) {
                validateFeatureRow(featureIndexManager, featureRow, null);
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertEquals(expectedCount, resultCount);

            // Test the query by envelope
            GeoPackageGeometryData geometryData = testFeatureRow.getGeometry();
            GeometryEnvelope envelope = geometryData.getEnvelope();
            if (envelope == null) {
                envelope = GeometryEnvelopeBuilder.buildEnvelope(geometryData
                        .getGeometry());
            }
            envelope.setMinX(envelope.getMinX() - .000001);
            envelope.setMaxX(envelope.getMaxX() + .000001);
            envelope.setMinY(envelope.getMinY() - .000001);
            envelope.setMaxY(envelope.getMaxY() + .000001);
            if (envelope.hasZ()) {
                envelope.setMinZ(envelope.getMinZ() - .000001);
                envelope.setMaxZ(envelope.getMaxZ() + .000001);
            }
            if (envelope.hasM()) {
                envelope.setMinM(envelope.getMinM() - .000001);
                envelope.setMaxM(envelope.getMaxM() + .000001);
            }
            resultCount = 0;
            boolean featureFound = false;
            TestCase.assertTrue(featureIndexManager.count(envelope) >= 1);
            featureIndexResults = featureIndexManager.query(envelope);
            for (FeatureRow featureRow : featureIndexResults) {
                validateFeatureRow(featureIndexManager, featureRow, envelope);
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
            if (!featureDao.getProjection().equals(ProjectionConstants.AUTHORITY_EPSG,
                    ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)) {
                projection = ProjectionFactory
                        .getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            } else {
                projection = ProjectionFactory
                        .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
            }
            ProjectionTransform transform = featureDao.getProjection()
                    .getTransformation(projection);
            BoundingBox transformedBoundingBox = transform
                    .transform(boundingBox);

            // Test the query by projected bounding box
            resultCount = 0;
            featureFound = false;
            TestCase.assertTrue(featureIndexManager.count(transformedBoundingBox,
                    projection) >= 1);
            featureIndexResults = featureIndexManager.query(
                    transformedBoundingBox, projection);
            for (FeatureRow featureRow : featureIndexResults) {
                validateFeatureRow(featureIndexManager, featureRow, boundingBox.buildEnvelope());
                if (featureRow.getId() == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);

            // Update a Geometry and update the index of a single feature row
            geometryData = new GeoPackageGeometryData(featureDao
                    .getGeometryColumns().getSrsId());
            Point point = new Point(5, 5);
            geometryData.setGeometry(point);
            testFeatureRow.setGeometry(geometryData);
            TestCase.assertEquals(1, featureDao.update(testFeatureRow));
            Date lastIndexedBefore = featureIndexManager.getLastIndexed();
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
                validateFeatureRow(featureIndexManager, featureRow, envelope);
                if (featureRow.getId() == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureIndexResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);
        }

        // Delete the extensions
        boolean everyOther = false;
        for (String featureTable : featureTables) {
            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity, geoPackage, featureDao);
            featureIndexManager.setIndexLocation(type);
            TestCase.assertTrue(featureIndexManager.isIndexed());

            // Test deleting a single geometry index
            if (everyOther) {
                FeatureCursor featureCursor = featureDao.queryForAll();
                while (featureCursor.moveToNext()) {
                    FeatureRow featureRow = featureCursor.getRow();
                    GeoPackageGeometryData geometryData = featureRow.getGeometry();
                    if (geometryData != null
                            && (geometryData.getEnvelope() != null || geometryData
                            .getGeometry() != null)) {
                        featureCursor.close();
                        TestCase.assertTrue(featureIndexManager.deleteIndex(featureRow));
                        break;
                    }
                }
                featureCursor.close();
            }

            featureIndexManager.deleteIndex();

            TestCase.assertFalse(featureIndexManager.isIndexed());
            everyOther = !everyOther;
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
            FeatureIndexManager featureIndexManager, FeatureRow featureRow, GeometryEnvelope queryEnvelope) {
        TestCase.assertNotNull(featureRow);
        GeoPackageGeometryData geometryData = featureRow.getGeometry();
        GeometryEnvelope envelope = geometryData.getEnvelope();
        if (envelope == null) {
            Geometry geometry = geometryData.getGeometry();
            if (geometry != null) {
                envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
            }
        }

        TestCase.assertNotNull(envelope);

        if (queryEnvelope != null) {
            TestCase.assertTrue(envelope.getMinX() <= queryEnvelope.getMaxX());
            TestCase.assertTrue(envelope.getMaxX() >= queryEnvelope.getMinX());
            TestCase.assertTrue(envelope.getMinY() <= queryEnvelope.getMaxY());
            TestCase.assertTrue(envelope.getMaxY() >= queryEnvelope.getMinY());
            if (envelope.isHasZ()) {
                if (queryEnvelope.hasZ()) {
                    TestCase.assertTrue(envelope.getMinZ() <= queryEnvelope.getMaxZ());
                    TestCase.assertTrue(envelope.getMaxZ() >= queryEnvelope.getMinZ());
                }
            } else {
                TestCase.assertFalse(queryEnvelope.hasZ());
                TestCase.assertNull(queryEnvelope.getMinZ());
                TestCase.assertNull(queryEnvelope.getMaxZ());
            }
            if (envelope.isHasM()) {
                if (queryEnvelope.hasM()) {
                    TestCase.assertTrue(envelope.getMinM() <= queryEnvelope.getMaxM());
                    TestCase.assertTrue(envelope.getMaxM() >= queryEnvelope.getMinM());
                }
            } else {
                TestCase.assertFalse(queryEnvelope.hasM());
                TestCase.assertNull(queryEnvelope.getMinM());
                TestCase.assertNull(queryEnvelope.getMaxM());
            }
        }
    }

}
