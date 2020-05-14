package mil.nga.geopackage.test;

import android.util.Log;

import junit.framework.TestCase;

import org.junit.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.db.TableColumnKey;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.sf.Geometry;
import mil.nga.sf.LineString;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.util.GeometryEnvelopeBuilder;

/**
 * For testing performance of feature inserts through duration logging and
 * profiling
 *
 * @author osbornb
 */
public class GeoPackagePerformance extends BaseTestCase {

    private static final String LOG_NAME = GeoPackagePerformance.class.getSimpleName();
    private static final String GEOPACKAGE_NAME = "performance";
    private static final String TABLE_NAME = "features";
    private static final String COLUMN_NAME = "geom";

    /**
     * Test performance without transactions
     *
     * @throws SQLException upon error
     */
    @Test
    public void testPerformance() throws SQLException {
        testPerformance(1000, 100);
    }

    /**
     * Test performance when transaction commits
     *
     * @throws SQLException upon error
     */
    @Test
    public void testPerformanceTransactions() throws SQLException {
        testPerformance(10000, 1000, 1000);
    }

    /**
     * Test performance
     *
     * @param createCount rows to create
     * @param logChunk    log frequency
     * @throws SQLException upon error
     */
    private void testPerformance(final int createCount, final int logChunk) throws SQLException {
        testPerformance(createCount, logChunk, -1);
    }

    /**
     * Test performance
     *
     * @param createCount rows to create
     * @param logChunk    log frequency
     * @param commitChunk commit chunk for transactions
     * @throws SQLException upon error
     */
    private void testPerformance(final int createCount, final int logChunk, final int commitChunk) throws SQLException {

        final boolean transactions = commitChunk > 0;

        //activity.deleteDatabase(GEOPACKAGE_NAME);

        GeoPackageManager manager = GeoPackageFactory.getManager(activity);

        manager.delete(GEOPACKAGE_NAME);

        Log.i(LOG_NAME, "GeoPackage: " + GEOPACKAGE_NAME);
        Log.i(LOG_NAME, "Table Name: " + TABLE_NAME);
        Log.i(LOG_NAME, "Column Name: " + COLUMN_NAME);
        Log.i(LOG_NAME, "Features: " + createCount);
        Log.i(LOG_NAME, "Transactions: " + transactions);
        if (transactions) {
            Log.i(LOG_NAME, "Commit Chunk: " + commitChunk);
        }
        if (logChunk > 0) {
            Log.i(LOG_NAME, "Log Chunk: " + logChunk);
        }

        manager.create(GEOPACKAGE_NAME);

        GeoPackage geoPackage = manager.open(GEOPACKAGE_NAME);

        Geometry geometry = createGeometry();

        GeometryColumns geometryColumns = new GeometryColumns();
        geometryColumns.setId(new TableColumnKey(TABLE_NAME, COLUMN_NAME));
        geometryColumns.setGeometryType(geometry.getGeometryType());
        geometryColumns.setZ((byte) 0);
        geometryColumns.setM((byte) 0);

        BoundingBox boundingBox = new BoundingBox(
                GeometryEnvelopeBuilder.buildEnvelope(geometry));

        SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
                .getOrCreateCode(ProjectionConstants.AUTHORITY_EPSG,
                        ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

        geoPackage.createFeatureTableWithMetadata(geometryColumns, boundingBox,
                srs.getId());

        GeoPackageGeometryData geometryData = new GeoPackageGeometryData(
                srs.getSrsId());
        geometryData.setGeometry(geometry);

        FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

        if (transactions) {
            dao.beginTransaction();
        }

        try {

            Instant startTime = Instant.now();
            Instant logTime = Instant.now();

            for (int count = 1; count <= createCount; count++) {

                FeatureRow newRow = dao.newRow();
                newRow.setGeometry(geometryData);

                dao.create(newRow);

                if (transactions && count % commitChunk == 0) {
                    dao.commit();
                }

                if (logChunk > 0 && count % logChunk == 0) {
                    Instant time = Instant.now();
                    Log.i(LOG_NAME, "Total Count: " + count);
                    Duration duration = Duration.between(logTime, time);
                    Log.i(LOG_NAME, "Chunk Time: "
                            + duration.toString().substring(2));
                    Log.i(LOG_NAME, "Chunk Average: "
                            + (duration.toMillis() / (float) logChunk)
                            + " ms");
                    Duration totalDuration = Duration.between(startTime, time);
                    Log.i(LOG_NAME, "Total Time: "
                            + totalDuration.toString().substring(2));
                    Log.i(LOG_NAME, "Feature Average: "
                            + (totalDuration.toMillis() / (float) count)
                            + " ms");
                    logTime = time;
                }

            }

            if (transactions) {
                dao.endTransaction();
            }

        } catch (Exception e) {
            if (transactions) {
                dao.failTransaction();
            }
            throw e;
        }

        geoPackage.close();

        geoPackage = manager.open(GEOPACKAGE_NAME);
        dao = geoPackage.getFeatureDao(TABLE_NAME);
        int finalCount = dao.count();
        Log.i(LOG_NAME, "Final Count: " + finalCount);
        geoPackage.close();

        TestCase.assertEquals(createCount, finalCount);
    }

    private static Geometry createGeometry() {

        Polygon polygon = new Polygon();
        LineString ring = new LineString();
        ring.addPoint(new Point(-104.802246, 39.720343));
        ring.addPoint(new Point(-104.802246, 39.719753));
        ring.addPoint(new Point(-104.802183, 39.719754));
        ring.addPoint(new Point(-104.802184, 39.719719));
        ring.addPoint(new Point(-104.802138, 39.719694));
        ring.addPoint(new Point(-104.802097, 39.719691));
        ring.addPoint(new Point(-104.802096, 39.719648));
        ring.addPoint(new Point(-104.801646, 39.719648));
        ring.addPoint(new Point(-104.801644, 39.719722));
        ring.addPoint(new Point(-104.801550, 39.719723));
        ring.addPoint(new Point(-104.801549, 39.720207));
        ring.addPoint(new Point(-104.801648, 39.720207));
        ring.addPoint(new Point(-104.801648, 39.720341));
        ring.addPoint(new Point(-104.802246, 39.720343));
        polygon.addRing(ring);

        return polygon;
    }

}
