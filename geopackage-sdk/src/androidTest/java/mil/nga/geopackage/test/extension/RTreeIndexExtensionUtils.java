package mil.nga.geopackage.test.extension;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.RTreeIndexExtension;
import mil.nga.geopackage.extension.RTreeIndexTableDao;
import mil.nga.geopackage.extension.RTreeIndexTableRow;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.user.custom.UserCustomCursor;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;

/**
 * RTree Extension Utility test methods
 *
 * @author osbornb
 */
public class RTreeIndexExtensionUtils {

    /**
     * Test RTree
     *
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testRTree(GeoPackage geoPackage) throws SQLException {

        RTreeIndexExtension extension = new RTreeIndexExtension(geoPackage);

        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureTable table = featureDao.getTable();

            if (!extension.has(table)) {
                // TODO RTree write functionality currently not supported in Android
                if(true) {
                    continue;
                }
                Extensions createdExtension = extension.create(table);
                TestCase.assertNotNull(createdExtension);
            }

            RTreeIndexTableDao tableDao = extension.getTableDao(featureDao);
            TestCase.assertTrue(tableDao.has());

            TestCase.assertEquals(featureDao.count(), tableDao.count());

            GeometryEnvelope totalEnvelope = null;

            int expectedCount = 0;

            UserCustomCursor cursor = tableDao.queryForAll();
            while (cursor.moveToNext()) {

                RTreeIndexTableRow row = tableDao.getRow(cursor);
                TestCase.assertNotNull(row);

                FeatureRow featureRow = tableDao.getFeatureRow(row);
                TestCase.assertNotNull(featureRow);

                TestCase.assertEquals(row.getId(), featureRow.getId());

                double minX = row.getMinX();
                double maxX = row.getMaxX();
                double minY = row.getMinY();
                double maxY = row.getMaxY();

                GeometryEnvelope envelope = featureRow.getGeometryEnvelope();

                if (envelope != null) {
                    TestCase.assertTrue(envelope.getMinX() >= minX);
                    TestCase.assertTrue(envelope.getMaxX() <= maxX);
                    TestCase.assertTrue(envelope.getMinY() >= minY);
                    TestCase.assertTrue(envelope.getMaxY() <= maxY);

                    UserCustomCursor results = tableDao.query(envelope);
                    TestCase.assertTrue(results.getCount() > 0);
                    boolean found = false;
                    while (results.moveToNext()) {
                        FeatureRow queryFeatureRow = tableDao
                                .getFeatureRow(results);
                        if (queryFeatureRow.getId() == featureRow.getId()) {
                            found = true;
                            break;
                        }
                    }
                    TestCase.assertTrue(found);
                    results.close();

                    expectedCount++;
                    if (totalEnvelope == null) {
                        totalEnvelope = envelope;
                    } else {
                        totalEnvelope = totalEnvelope.union(envelope);
                    }
                }

            }
            cursor.close();

            long envelopeCount = tableDao.count(totalEnvelope);
            TestCase.assertTrue(envelopeCount >= expectedCount);
            UserCustomCursor results = tableDao.query(totalEnvelope);
            TestCase.assertEquals(envelopeCount, results.getCount());
            results.close();

            BoundingBox boundingBox = new BoundingBox(totalEnvelope);
            long bboxCount = tableDao.count(boundingBox);
            TestCase.assertTrue(bboxCount >= expectedCount);
            results = tableDao.query(boundingBox);
            TestCase.assertEquals(bboxCount, results.getCount());
            results.close();
            TestCase.assertEquals(envelopeCount, bboxCount);

            Projection projection = featureDao.getProjection();
            if (!projection.getAuthority().equals(
                    ProjectionConstants.AUTHORITY_NONE)) {
                Projection queryProjection = null;
                if (projection.equals(ProjectionConstants.AUTHORITY_EPSG,
                        ProjectionConstants.EPSG_WEB_MERCATOR)) {
                    queryProjection = ProjectionFactory.getProjection(
                            ProjectionConstants.AUTHORITY_EPSG,
                            ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                } else {
                    queryProjection = ProjectionFactory.getProjection(
                            ProjectionConstants.AUTHORITY_EPSG,
                            ProjectionConstants.EPSG_WEB_MERCATOR);
                }
                ProjectionTransform transform = projection
                        .getTransformation(queryProjection);

                BoundingBox projectedBoundingBox = boundingBox
                        .transform(transform);
                long projectedBboxCount = tableDao.count(projectedBoundingBox,
                        queryProjection);
                TestCase.assertTrue(projectedBboxCount >= expectedCount);
                results = tableDao.query(projectedBoundingBox, queryProjection);
                TestCase.assertEquals(projectedBboxCount, results.getCount());
                results.close();
                TestCase.assertTrue(projectedBboxCount >= expectedCount);
            }
        }

    }

}
