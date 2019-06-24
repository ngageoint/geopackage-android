package mil.nga.geopackage.test.features;

import junit.framework.TestCase;

import org.junit.Test;

import java.sql.SQLException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.OpenAPIFeatureGenerator;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * WFS Feature Generator Test
 *
 * @author osbornb
 */
public class OpenAPIFeatureGeneratorTest extends CreateGeoPackageTestCase {

    /**
     * GeoPackage save path
     */
    private static final String PATH = "/Users/osbornb/ogc/";

    /**
     * Test opendata_1h
     *
     * @throws SQLException upon failure
     */
    @Test
    public void testOpenData1h() throws SQLException {

        testServer("http://beta.fmi.fi/data/3/wfs/sofp", "opendata_1h", 30, 15,
                new BoundingBox(20.0, 60.0, 22.0, 62.0), "20190519T140000",
                "20190619T140000");

    }

    /**
     * Test flurstueck
     *
     * @throws SQLException upon failure
     */
    @Test
    public void testLakes() throws SQLException {

        testServer("https://demo.pygeoapi.io/master", "lakes", 30, 25, null,
                null, null);

    }

    /**
     * Test flurstueck
     *
     * @throws SQLException upon failure
     */
    @Test
    public void testFlurstueck() throws SQLException {

        testServer("https://www.ldproxy.nrw.de/kataster", "flurstueck", 15,
                1000, new BoundingBox(8.683250427246094, 51.47780990600586,
                        9.093862533569336, 51.520809173583984),
                null, null);

    }

    /**
     * Test rakennus
     *
     * @throws SQLException upon failure
     */
    @Test
    public void testRakennus() throws SQLException {

        testServer(
                "https://beta-paikkatieto.maanmittauslaitos.fi/maastotiedot/wfs3/v1",
                "rakennus", 100, 1000, null, null, null);

    }

    /**
     * Test a WFS server and create a GeoPackage
     *
     * @param server      server url
     * @param collection  collection name
     * @param limit       request limit
     * @param totalLimit  total limit
     * @param boundingBox bounding box
     * @param time        time
     * @param period      period or end time
     * @throws SQLException upon error
     */
    private void testServer(String server, String collection, int limit,
                            int totalLimit, BoundingBox boundingBox, String time, String period)
            throws SQLException {

        GeoPackageManager geoPackageManager = GeoPackageFactory.getManager(activity);

        geoPackageManager.delete(collection);

        geoPackageManager.create(collection);

        GeoPackage geoPackage = geoPackageManager.open(collection);

        OpenAPIFeatureGenerator generator = new OpenAPIFeatureGenerator(geoPackage,
                collection, server, collection);
        generator.setLimit(limit);
        generator.setTotalLimit(totalLimit);
        generator.setBoundingBox(boundingBox);
        generator.setTime(time);
        generator.setPeriod(period);
        generator.setDownloadAttempts(3);

        int count = generator.generateFeatures();
        TestCase.assertEquals(totalLimit, count);

        FeatureDao featureDao = generator.getFeatureDao();
        TestCase.assertEquals(totalLimit, featureDao.count());

        FeatureIndexManager indexer = new FeatureIndexManager(activity, geoPackage, featureDao);
        indexer.setIndexLocation(FeatureIndexType.GEOPACKAGE);
        indexer.index();
        indexer.close();

        BoundingBox dataBounds = geoPackage
                .getBoundingBox(featureDao.getTableName());
        Contents contents = featureDao.getContents();
        contents.setBoundingBox(dataBounds);
        geoPackage.getContentsDao().update(contents);

        geoPackage.close();

    }

}
