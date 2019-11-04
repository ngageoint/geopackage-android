package mil.nga.geopackage.test.features.user;

import android.app.Activity;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureCacheTables;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Feature Cache Utility test methods
 *
 * @author osbornb
 */
public class FeatureCacheUtils {

    /**
     * Test cache
     *
     * @param activity   activity
     * @param geoPackage GeoPackage
     * @throws SQLException upon error
     */
    public static void testCache(Activity activity, GeoPackage geoPackage) throws SQLException {
        testCache(activity, geoPackage, FeatureIndexType.GEOPACKAGE);
        testCache(activity, geoPackage, FeatureIndexType.METADATA);
        testCache(activity, geoPackage, FeatureIndexType.RTREE);
    }

    private static void testCache(Activity activity, GeoPackage geoPackage, FeatureIndexType type) throws SQLException {

        int maxResults = 0;

        final int cacheSize = 1 + (int) (Math.random() * 10);
        FeatureCacheTables featureCache = new FeatureCacheTables(cacheSize);

        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                    geoPackage, featureDao);
            featureIndexManager.setContinueOnError(false);
            featureIndexManager.prioritizeQueryLocation(type);

            FeatureIndexResults featureIndexResults = featureIndexManager
                    .query();
            long resultsCount = featureIndexResults.count();
            maxResults = Math.max(maxResults, (int) resultsCount);
            for (long featureRowId : featureIndexResults.ids()) {
                FeatureRow featureRow = featureCache.get(featureTable, featureRowId);
                TestCase.assertNull(featureRow);
                featureRow = featureDao.queryForIdRow(featureRowId);
                TestCase.assertNotNull(featureRow);
                featureCache.put(featureRow);
                FeatureRow featureRow2 = featureCache.get(featureTable, featureRowId);
                TestCase.assertNotNull(featureRow2);
                TestCase.assertEquals(featureRow, featureRow2);
            }
            featureIndexResults.close();
            featureIndexManager.close();
        }

        FeatureCacheTables featureCache2 = new FeatureCacheTables(maxResults);

        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                    geoPackage, featureDao);
            featureIndexManager.setContinueOnError(false);
            featureIndexManager.prioritizeQueryLocation(type);

            FeatureIndexResults featureIndexResults = featureIndexManager
                    .query();
            int count = 0;
            long resultsCount = featureIndexResults.count();
            for (long featureRowId : featureIndexResults.ids()) {
                FeatureRow featureRow = featureCache.get(featureTable, featureRowId);
                if (count++ >= resultsCount - cacheSize) {
                    TestCase.assertNotNull(featureRow);
                } else {
                    TestCase.assertNull(featureRow);
                    featureRow = featureDao.queryForIdRow(featureRowId);
                    TestCase.assertNotNull(featureRow);
                }
                featureCache2.put(featureRow);
                FeatureRow featureRow2 = featureCache2.get(featureTable, featureRowId);
                TestCase.assertNotNull(featureRow2);
                TestCase.assertEquals(featureRow, featureRow2);
            }
            featureIndexResults.close();
            featureIndexManager.close();
        }

        featureCache.resize(featureCache2.getMaxCacheSize());

        for (String featureTable : featureTables) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                    geoPackage, featureDao);
            featureIndexManager.setContinueOnError(false);
            featureIndexManager.prioritizeQueryLocation(type);

            FeatureIndexResults featureIndexResults = featureIndexManager
                    .query();
            long resultsCount = featureIndexResults.count();
            TestCase.assertEquals(resultsCount, featureCache2.getSize(featureTable));
            for (long featureRowId : featureIndexResults.ids()) {
                FeatureRow featureRow = featureCache2.get(featureTable, featureRowId);
                TestCase.assertNotNull(featureRow);
                featureCache.put(featureRow);
            }
            featureIndexResults.close();
            featureIndexManager.close();

        }

        TestCase.assertEquals(featureTables.size(), featureCache.getTables().size());
        TestCase.assertEquals(featureTables.size(), featureCache2.getTables().size());
        for (String featureTable : featureTables) {

            TestCase.assertEquals(featureCache.getSize(featureTable), featureCache2.getSize(featureTable));

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            FeatureIndexManager featureIndexManager = new FeatureIndexManager(activity,
                    geoPackage, featureDao);
            featureIndexManager.setContinueOnError(false);
            featureIndexManager.prioritizeQueryLocation(type);

            FeatureIndexResults featureIndexResults = featureIndexManager
                    .query();
            long resultsCount = featureIndexResults.count();
            TestCase.assertEquals(resultsCount, featureCache.getSize(featureTable));
            TestCase.assertEquals(resultsCount, featureCache2.getSize(featureTable));
            for (long featureRowId : featureIndexResults.ids()) {
                TestCase.assertNotNull(featureCache.get(featureTable, featureRowId));
                TestCase.assertNotNull(featureCache2.get(featureTable, featureRowId));
            }
            featureIndexResults.close();
            featureIndexManager.close();

            featureCache.clear(featureTable);
            featureCache2.clear(featureTable);
            TestCase.assertEquals(0, featureCache.getSize(featureTable));
            TestCase.assertEquals(0, featureCache2.getSize(featureTable));
        }

        featureCache.clear();
        featureCache2.clear();
        TestCase.assertEquals(0, featureCache.getTables().size());
        TestCase.assertEquals(0, featureCache2.getTables().size());
    }

}
