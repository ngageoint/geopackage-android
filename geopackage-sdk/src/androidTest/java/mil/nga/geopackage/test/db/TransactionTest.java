package mil.nga.geopackage.test.db;

import android.database.sqlite.SQLiteDatabase;

import junit.framework.TestCase;

import org.junit.Test;

import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.sf.Point;

/**
 * Test multiple ways to perform a transaction
 *
 * @author osbornb
 */
public class TransactionTest extends CreateGeoPackageTestCase {

    /**
     * Test transactions
     */
    @Test
    public void testTransactions() {

        final int rows = 500;
        final int chunkSize = 150;

        for (String featureTable : geoPackage.getFeatureTables()) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);

            testTransaction(featureDao, rows, false);
            testTransaction(featureDao, rows, true);

            testTransactionShortcuts(featureDao, rows, false);
            testTransactionShortcuts(featureDao, rows, true);

            testTransactionShortcuts2(featureDao, rows, false);
            testTransactionShortcuts2(featureDao, rows, true);

            testTransactionChunks(featureDao, rows, chunkSize, false);
            testTransactionChunks(featureDao, rows, chunkSize, true);

        }

    }

    /**
     * Test a transaction
     *
     * @param featureDao feature dao
     * @param rows       rows to insert
     * @param successful true for a successful transaction
     */
    private void testTransaction(FeatureDao featureDao, int rows, boolean successful) {

        int countBefore = featureDao.count();

        SQLiteDatabase db = featureDao.getDatabaseConnection().getDb();
        db.beginTransaction();

        try {

            insertRows(featureDao, rows);

            if (successful) {
                db.setTransactionSuccessful();
            }

        } catch (Exception e) {

            db.endTransaction();
            TestCase.fail(e.getMessage());

        } finally {

            db.endTransaction();

        }

        TestCase.assertEquals(successful ? countBefore + rows : countBefore, featureDao.count());

    }

    /**
     * Test a transaction using shortcut methods
     *
     * @param featureDao feature dao
     * @param rows       rows to insert
     * @param successful true for a successful transaction
     */
    private void testTransactionShortcuts(FeatureDao featureDao, int rows, boolean successful) {

        int countBefore = featureDao.count();

        featureDao.beginTransaction();

        try {

            insertRows(featureDao, rows);

        } catch (Exception e) {

            featureDao.failTransaction();
            TestCase.fail(e.getMessage());

        } finally {

            if (successful) {
                featureDao.endTransaction();
            } else {
                featureDao.failTransaction();
            }

        }

        TestCase.assertEquals(successful ? countBefore + rows : countBefore, featureDao.count());

    }

    /**
     * Test a transaction using shortcut methods
     *
     * @param featureDao feature dao
     * @param rows       rows to insert
     * @param successful true for a successful transaction
     */
    private void testTransactionShortcuts2(FeatureDao featureDao, int rows, boolean successful) {

        int countBefore = featureDao.count();

        featureDao.beginTransaction();

        try {

            insertRows(featureDao, rows);

        } catch (Exception e) {

            featureDao.endTransaction(false);
            TestCase.fail(e.getMessage());

        } finally {

            featureDao.endTransaction(successful);

        }

        TestCase.assertEquals(successful ? countBefore + rows : countBefore, featureDao.count());

    }

    /**
     * Test a transaction with chunked inserts
     *
     * @param featureDao feature dao
     * @param rows       rows to insert
     * @param chunkSize  chunk size
     * @param successful true for a successful transaction
     */
    private void testTransactionChunks(FeatureDao featureDao, int rows, int chunkSize, boolean successful) {

        int countBefore = featureDao.count();

        featureDao.beginTransaction();

        try {

            for (int count = 1; count <= rows; count++) {

                insertRow(featureDao);

                if (count % chunkSize == 0) {

                    if (successful) {
                        featureDao.endAndBeginTransaction();
                    } else {
                        featureDao.failTransaction();
                        featureDao.beginTransaction();
                    }

                }
            }

        } catch (Exception e) {

            featureDao.failTransaction();
            TestCase.fail(e.getMessage());

        } finally {
            if (successful) {
                featureDao.endTransaction();
            } else {
                featureDao.failTransaction();
            }
        }

        TestCase.assertEquals(successful ? countBefore + rows : countBefore, featureDao.count());

    }

    /**
     * Insert rows into the feature table
     *
     * @param featureDao feature dao
     * @param rows       number of rows
     */
    private void insertRows(FeatureDao featureDao, int rows) {

        for (int count = 0; count < rows; count++) {
            insertRow(featureDao);
        }

    }

    /**
     * Insert a row into the feature table
     *
     * @param featureDao feature dao
     */
    private void insertRow(FeatureDao featureDao) {

        FeatureRow row = featureDao.newRow();
        GeoPackageGeometryData geometry = new GeoPackageGeometryData(
                featureDao.getGeometryColumns().getSrsId());
        geometry.setGeometry(new Point(0, 0));
        row.setGeometry(geometry);
        featureDao.insert(row);

    }

}
