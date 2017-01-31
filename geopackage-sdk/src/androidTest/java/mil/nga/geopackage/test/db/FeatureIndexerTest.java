package mil.nga.geopackage.test.db;

import android.database.Cursor;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.Date;

import mil.nga.geopackage.db.FeatureIndexer;
import mil.nga.geopackage.db.metadata.GeoPackageMetadataDb;
import mil.nga.geopackage.db.metadata.GeometryMetadata;
import mil.nga.geopackage.db.metadata.GeometryMetadataDataSource;
import mil.nga.geopackage.db.metadata.TableMetadata;
import mil.nga.geopackage.db.metadata.TableMetadataDataSource;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.test.tiles.features.FeatureTileUtils;
import mil.nga.wkb.geom.GeometryEnvelope;
import mil.nga.wkb.geom.GeometryType;

/**
 * Test table indexer
 *
 * @author osbornb
 */
public class FeatureIndexerTest extends CreateGeoPackageTestCase {

    /**
     * Constructor
     */
    public FeatureIndexerTest() {

    }

    /**
     * Test indexer
     *
     * @throws java.sql.SQLException
     */
    public void testIndexer() throws SQLException {

        FeatureDao featureDao = FeatureTileUtils.createFeatureDao(geoPackage);

        int initialFeatures = FeatureTileUtils.insertFeatures(geoPackage, featureDao);

        FeatureIndexer indexer = new FeatureIndexer(activity, featureDao);

        GeoPackageMetadataDb db = new GeoPackageMetadataDb(activity);
        db.open();
        try {
            TableMetadataDataSource ds = new TableMetadataDataSource(db);
            assertNull(ds.get(geoPackage.getName(), featureDao.getTableName()));
        } finally {
            db.close();
        }

        // Verify not indexed
        assertFalse(indexer.isIndexed());

        long currentTime = (new Date()).getTime();
        Long lastIndexed = null;

        // Index
        indexer.index();

        db = new GeoPackageMetadataDb(activity);
        db.open();
        try {
            TableMetadataDataSource ds = new TableMetadataDataSource(db);
            TableMetadata metadata = ds.get(geoPackage.getName(), featureDao.getTableName());
            assertNotNull(metadata);
            lastIndexed = metadata.getLastIndexed();
            assertNotNull(lastIndexed);
            assertTrue(lastIndexed >= currentTime);
        } finally {
            db.close();
        }

        // Verify indexed
        assertTrue(indexer.isIndexed());

        // Try to index when not needed
        indexer.index();

        db = new GeoPackageMetadataDb(activity);
        db.open();
        try {
            TableMetadataDataSource ds = new TableMetadataDataSource(db);
            TableMetadata metadata = ds.get(geoPackage.getName(), featureDao.getTableName());
            assertNotNull(metadata);
            // Index date should not change
            assertEquals(lastIndexed, metadata.getLastIndexed());
        } finally {
            db.close();
        }

        // Verify indexed
        assertTrue(indexer.isIndexed());

        // Force indexing again
        indexer.index(true);

        db = new GeoPackageMetadataDb(activity);
        db.open();
        try {
            TableMetadataDataSource ds = new TableMetadataDataSource(db);
            TableMetadata metadata = ds.get(geoPackage.getName(), featureDao.getTableName());
            assertNotNull(metadata);
            assertNotNull(metadata.getLastIndexed());
            assertTrue(metadata.getLastIndexed() > lastIndexed);
        } finally {
            db.close();
        }

        assertTrue(indexer.isIndexed());

        // Insert a point and line and make sure it is no longer indexed
        double minX = 5.8921;
        double maxX = 8.38495;
        double minY = 6.82645;
        double maxY = 9.134445;
        long id1 = FeatureTileUtils.insertPoint(featureDao, minX, maxY);
        long id2 = FeatureTileUtils.insertLine(featureDao, new double[][]{{minX, minY}, {maxX, maxY}});
        FeatureTileUtils.updateLastChange(geoPackage, featureDao);

        // Verify no longer indexed
        assertFalse(indexer.isIndexed());

        // Index again
        indexer.index();
        assertTrue(indexer.isIndexed());

        // Insert a polygon and index manually
        long id3 = FeatureTileUtils.insertPolygon(featureDao, new double[][]{{minX, minY}, {maxX, minY}, {maxX, maxY}});
        FeatureTileUtils.updateLastChange(geoPackage, featureDao);
        FeatureRow polygonRow = featureDao.queryForIdRow(id3);
        assertNotNull(polygonRow);
        TestCase.assertTrue(indexer.index(polygonRow));
        assertTrue(indexer.isIndexed());

        // Update the point coordinates
        FeatureRow pointRow = featureDao.queryForIdRow(id1);
        assertNotNull(pointRow);
        FeatureTileUtils.setPoint(pointRow, maxX, minY);
        assertTrue(featureDao.update(pointRow) > 0);
        FeatureTileUtils.updateLastChange(geoPackage, featureDao);
        TestCase.assertTrue(indexer.index(pointRow));
        assertTrue(indexer.isIndexed());

        GeometryEnvelope envelope = new GeometryEnvelope();
        envelope.setMinX(minX);
        envelope.setMaxX(maxX);
        envelope.setMinY(minY);
        envelope.setMaxY(maxY);

        boolean id1Found = false;
        boolean id2Found = false;
        boolean id3Found = false;

        int count = 0;

        db = new GeoPackageMetadataDb(activity);
        db.open();
        try {
            GeometryMetadataDataSource ds = new GeometryMetadataDataSource(db);
            Cursor cursor = ds.query(geoPackage.getName(), featureDao.getTableName(), envelope);
            try {
                assertNotNull(cursor);
                count = cursor.getCount();
                assertTrue(count >= 3);
                while (cursor.moveToNext()) {

                    GeometryMetadata metadata = GeometryMetadataDataSource.createGeometryMetadata(cursor);
                    long id = metadata.getId();

                    FeatureRow queryRow = featureDao.queryForIdRow(id);
                    assertNotNull(queryRow);

                    GeometryType geometryType = queryRow.getGeometry().getGeometry().getGeometryType();

                    if (id == id1) {
                        id1Found = true;
                        assertEquals(GeometryType.POINT, geometryType);
                        assertEquals(maxX, metadata.getMinX());
                        assertEquals(maxX, metadata.getMaxX());
                        assertEquals(minY, metadata.getMinY());
                        assertEquals(minY, metadata.getMaxY());
                    } else if (id == id2) {
                        id2Found = true;
                        assertEquals(GeometryType.LINESTRING, geometryType);
                        assertEquals(minX, metadata.getMinX());
                        assertEquals(maxX, metadata.getMaxX());
                        assertEquals(minY, metadata.getMinY());
                        assertEquals(maxY, metadata.getMaxY());
                    } else if (id == id3) {
                        id3Found = true;
                        assertEquals(GeometryType.POLYGON, geometryType);
                        assertEquals(minX, metadata.getMinX());
                        assertEquals(maxX, metadata.getMaxX());
                        assertEquals(minY, metadata.getMinY());
                        assertEquals(maxY, metadata.getMaxY());
                    }
                }
            } finally {
                cursor.close();
            }
        } finally {
            db.close();
        }

        assertTrue(id1Found);
        assertTrue(id2Found);
        assertTrue(id3Found);

        // Verify querying for all geometry metadata gets more results
        db = new GeoPackageMetadataDb(activity);
        db.open();
        try {
            GeometryMetadataDataSource ds = new GeometryMetadataDataSource(db);
            Cursor cursor = ds.query(geoPackage.getName(), featureDao.getTableName());
            try {
                assertNotNull(cursor);
                assertEquals(initialFeatures + 3, cursor.getCount());
            } finally {
                cursor.close();
            }
        } finally {
            db.close();
        }
    }

}
