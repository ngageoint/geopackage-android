package mil.nga.geopackage.test.extension.index;

import com.j256.ormlite.dao.CloseableIterator;

import junit.framework.TestCase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.ExtensionsDao;
import mil.nga.geopackage.extension.GeoPackageExtensions;
import mil.nga.geopackage.extension.index.FeatureTableIndex;
import mil.nga.geopackage.extension.index.GeometryIndex;
import mil.nga.geopackage.extension.index.GeometryIndexDao;
import mil.nga.geopackage.extension.index.TableIndexDao;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.io.TestGeoPackageProgress;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryEnvelope;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.util.GeometryEnvelopeBuilder;

/**
 * Feature Table Index Utility test methods
 *
 * @author osbornb
 */
public class FeatureTableIndexUtils {

    /**
     * Test read
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testIndex(GeoPackage geoPackage) throws SQLException, IOException {

        // Test indexing each feature table
        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureTableIndex featureTableIndex = new FeatureTableIndex(
                    geoPackage, featureDao);

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

            TestCase.assertFalse(featureTableIndex.isIndexed());
            TestCase.assertNull(featureTableIndex.getLastIndexed());
            Date currentDate = new Date();

            TestUtils.validateGeoPackage(geoPackage);

            // Test indexing
            TestGeoPackageProgress progress = new TestGeoPackageProgress();
            featureTableIndex.setProgress(progress);
            int indexCount = featureTableIndex.index();
            TestUtils.validateGeoPackage(geoPackage);

            TestCase.assertEquals(expectedCount, indexCount);
            TestCase.assertEquals(featureDao.count(), progress.getProgress());
            TestCase.assertNotNull(featureTableIndex.getLastIndexed());
            Date lastIndexed = featureTableIndex.getLastIndexed();
            TestCase.assertTrue(lastIndexed.getTime() > currentDate.getTime());

            TestCase.assertTrue(featureTableIndex.isIndexed());
            TestCase.assertEquals(expectedCount, featureTableIndex.count());

            // Test re-indexing, both ignored and forced
            TestCase.assertEquals(0, featureTableIndex.index());
            TestCase.assertEquals(expectedCount, featureTableIndex.index(true));
            TestCase.assertTrue(featureTableIndex.getLastIndexed().getTime() > lastIndexed
                    .getTime());

            // Query for all indexed geometries
            int resultCount = 0;
            CloseableIterator<GeometryIndex> featureTableResults = featureTableIndex
                    .query();
            while (featureTableResults.hasNext()) {
                GeometryIndex geometryIndex = featureTableResults.next();
                validateGeometryIndex(featureTableIndex, geometryIndex);
                resultCount++;
            }
            featureTableResults.close();
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
            TestCase.assertTrue(featureTableIndex.count(envelope) >= 1);
            featureTableResults = featureTableIndex.query(envelope);
            while (featureTableResults.hasNext()) {
                GeometryIndex geometryIndex = featureTableResults.next();
                validateGeometryIndex(featureTableIndex, geometryIndex);
                if (geometryIndex.getGeomId() == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureTableResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);

            // Pick a projection different from the feature dao and project the
            // bounding box
            BoundingBox boundingBox = new BoundingBox(envelope.getMinX() - 1,
                    envelope.getMinY() - 1, envelope.getMaxX() + 1,
                    envelope.getMaxY() + 1);
            Projection projection = null;
            if (!featureDao.getProjection().equals(ProjectionConstants.AUTHORITY_EPSG, ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)) {
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
            TestCase.assertTrue(featureTableIndex.count(transformedBoundingBox,
                    projection) >= 1);
            featureTableResults = featureTableIndex.query(
                    transformedBoundingBox, projection);
            while (featureTableResults.hasNext()) {
                GeometryIndex geometryIndex = featureTableResults.next();
                validateGeometryIndex(featureTableIndex, geometryIndex);
                if (geometryIndex.getGeomId() == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureTableResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);

            // Update a Geometry and update the index of a single feature row
            geometryData = new GeoPackageGeometryData(featureDao
                    .getGeometryColumns().getSrsId());
            Point point = new Point(5, 5);
            geometryData.setGeometry(point);
            testFeatureRow.setGeometry(geometryData);
            TestCase.assertEquals(1, featureDao.update(testFeatureRow));
            Date lastIndexedBefore = featureTableIndex.getLastIndexed();
            TestCase.assertTrue(featureTableIndex.index(testFeatureRow));
            Date lastIndexedAfter = featureTableIndex.getLastIndexed();
            TestCase.assertTrue(lastIndexedAfter.after(lastIndexedBefore));

            // Verify the index was updated for the feature row
            envelope = GeometryEnvelopeBuilder.buildEnvelope(point);
            resultCount = 0;
            featureFound = false;
            TestCase.assertTrue(featureTableIndex.count(envelope) >= 1);
            featureTableResults = featureTableIndex.query(envelope);
            while (featureTableResults.hasNext()) {
                GeometryIndex geometryIndex = featureTableResults.next();
                validateGeometryIndex(featureTableIndex, geometryIndex);
                if (geometryIndex.getGeomId() == testFeatureRow.getId()) {
                    featureFound = true;
                }
                resultCount++;
            }
            featureTableResults.close();
            TestCase.assertTrue(featureFound);
            TestCase.assertTrue(resultCount >= 1);
        }

        ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();
        GeometryIndexDao geometryIndexDao = geoPackage.getGeometryIndexDao();
        TableIndexDao tableIndexDao = geoPackage.getTableIndexDao();

        // Delete the extensions for the first half of the feature tables
        boolean everyOther = false;
        for (String featureTable : featureTables.subList(0,
                (int) Math.ceil(featureTables.size() * .5))) {
            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            int geometryCount = geometryIndexDao
                    .queryForTableName(featureTable).size();
            TestCase.assertTrue(geometryCount > 0);
            TestCase.assertNotNull(tableIndexDao.queryForId(featureTable));
            Extensions extensions = extensionsDao.queryByExtension(
                    FeatureTableIndex.EXTENSION_NAME, featureTable,
                    featureDao.getGeometryColumnName());
            TestCase.assertNotNull(extensions);
            TestCase.assertEquals(extensions.getTableName(), featureTable);
            TestCase.assertEquals(extensions.getColumnName(),
                    featureDao.getGeometryColumnName());
            TestCase.assertEquals(extensions.getExtensionName(),
                    FeatureTableIndex.EXTENSION_NAME);
            TestCase.assertEquals(extensions.getAuthor(),
                    FeatureTableIndex.EXTENSION_AUTHOR);
            TestCase.assertEquals(extensions.getExtensionNameNoAuthor(),
                    FeatureTableIndex.EXTENSION_NAME_NO_AUTHOR);
            TestCase.assertEquals(extensions.getDefinition(),
                    FeatureTableIndex.EXTENSION_DEFINITION);
            TestCase.assertEquals(extensions.getScope(),
                    ExtensionScopeType.READ_WRITE);
            FeatureTableIndex featureTableIndex = new FeatureTableIndex(
                    geoPackage, featureDao);
            TestCase.assertTrue(featureTableIndex.isIndexed());
            TestCase.assertEquals(geometryCount, featureTableIndex.count());

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
                        TestCase.assertEquals(1, featureTableIndex.deleteIndex(featureRow));
                        TestCase.assertEquals(geometryCount - 1, featureTableIndex.count());
                        break;
                    }
                }
                featureCursor.close();
            }

            GeoPackageExtensions.deleteTableExtensions(geoPackage, featureTable);

            TestCase.assertFalse(featureTableIndex.isIndexed());
            TestCase.assertEquals(0,
                    geometryIndexDao.queryForTableName(featureTable).size());
            TestCase.assertNull(tableIndexDao.queryForId(featureTable));
            extensions = extensionsDao.queryByExtension(
                    FeatureTableIndex.EXTENSION_NAME, featureTable,
                    featureDao.getGeometryColumnName());
            TestCase.assertNull(extensions);
            everyOther = !everyOther;
        }

        TestCase.assertTrue(geometryIndexDao.isTableExists());
        TestCase.assertTrue(tableIndexDao.isTableExists());
        TestCase.assertTrue(extensionsDao.queryByExtension(
                FeatureTableIndex.EXTENSION_NAME).size() > 0);

        // Test deleting all NGA extensions
        GeoPackageExtensions.deleteExtensions(geoPackage);

        TestCase.assertFalse(geometryIndexDao.isTableExists());
        TestCase.assertFalse(tableIndexDao.isTableExists());
        TestCase.assertEquals(0,
                extensionsDao
                        .queryByExtension(FeatureTableIndex.EXTENSION_NAME)
                        .size());

    }

    /**
     * Test table index delete all
     *
     * @param geoPackage
     * @throws SQLException
     */
    public static void testDeleteAll(GeoPackage geoPackage) throws SQLException {

        // Test indexing each feature table
        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureTableIndex featureTableIndex = new FeatureTableIndex(
                    geoPackage, featureDao);

            TestCase.assertFalse(featureTableIndex.isIndexed());

            TestUtils.validateGeoPackage(geoPackage);

            // Test indexing
            featureTableIndex.index();
            TestUtils.validateGeoPackage(geoPackage);

            TestCase.assertTrue(featureTableIndex.isIndexed());

        }

        ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();
        GeometryIndexDao geometryIndexDao = geoPackage.getGeometryIndexDao();
        TableIndexDao tableIndexDao = geoPackage.getTableIndexDao();

        TestCase.assertTrue(geometryIndexDao.isTableExists());
        TestCase.assertTrue(tableIndexDao.isTableExists());
        TestCase.assertTrue(extensionsDao.queryByExtension(
                FeatureTableIndex.EXTENSION_NAME).size() > 0);

        TestCase.assertTrue(geometryIndexDao.countOf() > 0);
        long count = tableIndexDao.countOf();
        TestCase.assertTrue(count > 0);

        int deleteCount = tableIndexDao.deleteAllCascade();
        TestCase.assertEquals(count, deleteCount);

        TestCase.assertTrue(geometryIndexDao.countOf() == 0);
        TestCase.assertTrue(tableIndexDao.countOf() == 0);
    }

    /**
     * Validate a Geometry Index result
     *
     * @param featureTableIndex
     * @param geometryIndex
     */
    private static void validateGeometryIndex(
            FeatureTableIndex featureTableIndex, GeometryIndex geometryIndex) {
        FeatureRow featureRow = featureTableIndex.getFeatureRow(geometryIndex);
        TestCase.assertNotNull(featureRow);
        TestCase.assertEquals(featureTableIndex.getTableName(),
                geometryIndex.getTableName());
        TestCase.assertEquals(geometryIndex.getGeomId(), featureRow.getId());
        GeoPackageGeometryData geometryData = featureRow.getGeometry();
        GeometryEnvelope envelope = geometryData.getEnvelope();
        if (envelope == null) {
            Geometry geometry = geometryData.getGeometry();
            if (geometry != null) {
                envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
            }
        }

        TestCase.assertNotNull(envelope);

        TestCase.assertEquals(envelope.getMinX(), geometryIndex.getMinX());
        TestCase.assertEquals(envelope.getMaxX(), geometryIndex.getMaxX());
        TestCase.assertEquals(envelope.getMinY(), geometryIndex.getMinY());
        TestCase.assertEquals(envelope.getMaxY(), geometryIndex.getMaxY());
        if (envelope.isHasZ()) {
            TestCase.assertEquals(envelope.getMinZ(), geometryIndex.getMinZ());
            TestCase.assertEquals(envelope.getMaxZ(), geometryIndex.getMaxZ());
        } else {
            TestCase.assertNull(geometryIndex.getMinZ());
            TestCase.assertNull(geometryIndex.getMaxZ());
        }
        if (envelope.isHasM()) {
            TestCase.assertEquals(envelope.getMinM(), geometryIndex.getMinM());
            TestCase.assertEquals(envelope.getMaxM(), geometryIndex.getMaxM());
        } else {
            TestCase.assertNull(geometryIndex.getMinM());
            TestCase.assertNull(geometryIndex.getMaxM());
        }
    }

}
