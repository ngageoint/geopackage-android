package mil.nga.geopackage.test.db;

import android.database.sqlite.SQLiteDatabase;

import junit.framework.TestCase;

import org.junit.Test;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.contents.ContentsDataType;
import mil.nga.geopackage.db.master.SQLiteMaster;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.sf.Point;

/**
 * Test multiple ways to perform a transaction
 *
 * @author osbornb
 */
public class TransactionTest extends CreateGeoPackageTestCase {

    /**
     * Test transactions on the User DAO
     */
    @Test
    public void testUserDao() {

        final int rows = 500;
        final int chunkSize = 150;

        for (String featureTable : geoPackage.getFeatureTables()) {

            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);

            testUserDao(featureDao, rows, false);
            testUserDao(featureDao, rows, true);

            testUserDaoShortcuts(featureDao, rows, false);
            testUserDaoShortcuts(featureDao, rows, true);

            testUserDaoShortcuts2(featureDao, rows, false);
            testUserDaoShortcuts2(featureDao, rows, true);

            testUserDaoChunks(featureDao, rows, chunkSize, false);
            testUserDaoChunks(featureDao, rows, chunkSize, true);

        }

    }

    /**
     * Test transactions on the GeoPackage
     */
    @Test
    public void testGeoPackage() {

        testGeoPackage(geoPackage, false);
        testGeoPackage(geoPackage, true);

    }

    /**
     * Test ORMLite transactions
     *
     * @throws SQLException upon error
     */
    @Test
    public void testORMLite() throws SQLException {

        testORMLite(geoPackage, false);
        testORMLite(geoPackage, true);

    }

    /**
     * Test a transaction
     *
     * @param featureDao feature dao
     * @param rows       rows to insert
     * @param successful true for a successful transaction
     */
    private void testUserDao(FeatureDao featureDao, int rows, boolean successful) {

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
    private void testUserDaoShortcuts(FeatureDao featureDao, int rows, boolean successful) {

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
    private void testUserDaoShortcuts2(FeatureDao featureDao, int rows, boolean successful) {

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
    private void testUserDaoChunks(FeatureDao featureDao, int rows, int chunkSize, boolean successful) {

        int countBefore = featureDao.count();

        featureDao.beginTransaction();

        try {

            for (int count = 1; count <= rows; count++) {

                insertRow(featureDao);

                if (count % chunkSize == 0) {

                    if (successful) {
                        featureDao.commit();
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
        GeoPackageGeometryData geometry = GeoPackageGeometryData.create(
                featureDao.getSrsId(), new Point(0, 0));
        row.setGeometry(geometry);
        featureDao.insert(row);

    }

    /**
     * Test a transaction on the GeoPackage
     *
     * @param geoPackage GeoPackage
     * @param successful true for a successful transaction
     */
    private void testGeoPackage(GeoPackage geoPackage, boolean successful) {

        int count = SQLiteMaster.countViewsOnTable(geoPackage.getConnection(),
                Contents.TABLE_NAME);

        geoPackage.beginTransaction();

        try {

            geoPackage.execSQL("CREATE VIEW " + Contents.TABLE_NAME
                    + "_view AS SELECT table_name AS tableName FROM "
                    + Contents.TABLE_NAME);

        } catch (Exception e) {

            geoPackage.failTransaction();
            TestCase.fail(e.getMessage());

        } finally {

            if (successful) {
                geoPackage.endTransaction();
            } else {
                geoPackage.failTransaction();
            }

        }

        TestCase.assertEquals(successful ? count + 1 : count, SQLiteMaster
                .countViewsOnTable(geoPackage.getConnection(),
                        Contents.TABLE_NAME));
    }

    /**
     * Test an ORMLite transaction
     *
     * @param geoPackage GeoPackage
     * @param successful true for a successful transaction
     * @throws SQLException upon error
     */
    private void testORMLite(final GeoPackage geoPackage,
                             final boolean successful) throws SQLException {

        final String tableName = "test_table";

        final Contents contents = new Contents();
        contents.setTableName(tableName);
        contents.setDataType(ContentsDataType.ATTRIBUTES);

        if (!geoPackage.isTable(tableName)) {
            geoPackage.execSQL("CREATE TABLE " + tableName
                    + " (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)");
        }

        final SpatialReferenceSystemDao srsDao = geoPackage
                .getSpatialReferenceSystemDao();
        final ContentsDao contentsDao = geoPackage.getContentsDao();

        long srsCount = srsDao.countOf();
        long contentsCount = contentsDao.countOf();

        Callable<Void> callable = new Callable<Void>() {
            public Void call() throws Exception {

                SpatialReferenceSystem srs = srsDao.createWgs84Geographical3D();

                contents.setSrs(srs);
                contentsDao.create(contents);

                if (!successful) {
                    throw new SQLException();
                }

                return null;
            }
        };

        try {
            geoPackage.callInTransaction(callable);
        } catch (SQLException e) {
            if (successful) {
                TestCase.fail(e.getMessage());
            }
        }

        TestCase.assertEquals(successful ? srsCount + 1 : srsCount,
                srsDao.countOf());
        TestCase.assertEquals(successful ? contentsCount + 1 : contentsCount,
                contentsDao.countOf());

        TestCase.assertEquals(successful,
                geoPackage.isAttributeTable(tableName));

    }

}
