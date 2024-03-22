package mil.nga.geopackage;

import android.content.Context;
import android.graphics.Bitmap;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.extension.ExtensionsDao;
import mil.nga.geopackage.extension.metadata.MetadataDao;
import mil.nga.geopackage.extension.metadata.MetadataExtension;
import mil.nga.geopackage.extension.metadata.reference.MetadataReferenceDao;
import mil.nga.geopackage.extension.schema.SchemaExtension;
import mil.nga.geopackage.extension.schema.columns.DataColumnsDao;
import mil.nga.geopackage.extension.schema.constraints.DataColumnConstraintsDao;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeaturePaginatedCursor;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.UrlTileGenerator;
import mil.nga.geopackage.tiles.features.DefaultFeatureTiles;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.retriever.GeoPackageTileRetriever;
import mil.nga.geopackage.tiles.retriever.TileCreator;
import mil.nga.geopackage.tiles.user.TileCursor;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.proj.Projection;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.sf.Geometry;

/**
 * README example tests
 *
 * @author osbornb
 */
public class ReadmeTest extends ImportGeoPackageTestCase {

    /**
     * Test transform
     *
     * @throws IOException  upon error
     * @throws SQLException upon error
     */
    @Test
    public void testGeoPackage() throws IOException, SQLException {

        geoPackage.close();
        geoPackage = null;
        GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        manager.delete(TestConstants.IMPORT_DB_NAME);

        String importLocation = TestUtils.getAssetFileInternalStorageLocation(
                activity, TestConstants.IMPORT_DB_FILE_NAME);
        File geoPackageFile = new File(importLocation);

        try {
            testGeoPackage(activity, geoPackageFile, TestConstants.IMPORT_DB_NAME);
        } finally {
            manager.delete(TestConstants.IMPORT_DB_NAME);
        }

    }

    /**
     * Test GeoPackage
     *
     * @param context        context
     * @param geoPackageFile GeoPackage file
     * @param name           database name
     * @throws IOException  upon error
     * @throws SQLException upon error
     */
    private void testGeoPackage(Context context, File geoPackageFile, String name) throws IOException, SQLException {

        // Context context = ...;
        // File geoPackageFile = ...;

        // Get a manager
        GeoPackageManager manager = GeoPackageFactory.getManager(context);

        // Import database
        boolean imported = manager.importGeoPackage(geoPackageFile);

        // Available databases
        List<String> databases = manager.databases();

        // Open database
        GeoPackage geoPackage = manager.open(name);

        // GeoPackage Table DAOs
        SpatialReferenceSystemDao srsDao = geoPackage.getSpatialReferenceSystemDao();
        ContentsDao contentsDao = geoPackage.getContentsDao();
        GeometryColumnsDao geomColumnsDao = geoPackage.getGeometryColumnsDao();
        TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
        TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
        SchemaExtension schemaExtension = new SchemaExtension(geoPackage);
        DataColumnsDao dataColumnsDao = schemaExtension.getDataColumnsDao();
        DataColumnConstraintsDao dataColumnConstraintsDao = schemaExtension
                .getDataColumnConstraintsDao();
        MetadataExtension metadataExtension = new MetadataExtension(geoPackage);
        MetadataDao metadataDao = metadataExtension.getMetadataDao();
        MetadataReferenceDao metadataReferenceDao = metadataExtension
                .getMetadataReferenceDao();
        ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();

        // Feature and tile tables
        List<String> features = geoPackage.getFeatureTables();
        List<String> tiles = geoPackage.getTileTables();

        // Query Features
        String featureTable = features.get(0);
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
        FeatureCursor featureCursor = featureDao.queryForAll();
        try {
            for (FeatureRow featureRow : featureCursor) {
                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                if (geometryData != null && !geometryData.isEmpty()) {
                    Geometry geometry = geometryData.getGeometry();
                    // ...
                }
            }
        } finally {
            featureCursor.close();
        }

        // Query Tiles
        String tileTable = tiles.get(0);
        TileDao tileDao = geoPackage.getTileDao(tileTable);
        TileCursor tileCursor = tileDao.queryForAll();
        try {
            for (TileRow tileRow : tileCursor) {
                byte[] tileBytes = tileRow.getTileData();
                Bitmap tileBitmap = tileRow.getTileDataBitmap();
                // ...
            }
        } finally {
            tileCursor.close();
        }

        // Retrieve Tiles by XYZ
        GeoPackageTileRetriever retriever = new GeoPackageTileRetriever(tileDao);
        GeoPackageTile geoPackageTile = retriever.getTile(2, 2, 2);
        if (geoPackageTile != null) {
            byte[] tileBytes = geoPackageTile.getData();
            Bitmap tileBitmap = geoPackageTile.getBitmap();
            // ...
        }

        // Retrieve Tiles by Bounding Box
        TileCreator tileCreator = new TileCreator(
                tileDao, ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM));
        GeoPackageTile geoPackageTile2 = tileCreator.getTile(
                new BoundingBox(-90.0, 0.0, 0.0, 66.513260));
        if (geoPackageTile2 != null) {
            byte[] tileBytes = geoPackageTile2.getData();
            Bitmap tileBitmap = geoPackageTile2.getBitmap();
            // ...
        }

        BoundingBox boundingBox = BoundingBox.worldWebMercator();
        Projection projection = ProjectionFactory
                .getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

        // Index Features
        FeatureIndexManager indexer = new FeatureIndexManager(context, geoPackage, featureDao, false);
        indexer.setIndexLocation(FeatureIndexType.GEOPACKAGE);
        int indexedCount = indexer.index();

        // Query Indexed Features in paginated chunks
        FeatureIndexResults indexResults = indexer.queryForChunk(boundingBox,
                projection, 50);
        FeaturePaginatedCursor paginatedCursor = indexer
                .paginate(indexResults);
        for (FeatureRow featureRow : paginatedCursor) {
            GeoPackageGeometryData geometryData = featureRow.getGeometry();
            if (geometryData != null && !geometryData.isEmpty()) {
                Geometry geometry = geometryData.getGeometry();
                // ...
            }
        }

        // Draw tiles from features
        FeatureTiles featureTiles = new DefaultFeatureTiles(context, featureDao, context.getResources().getDisplayMetrics().density, false);
        featureTiles.setMaxFeaturesPerTile(1000); // Set max features to draw per tile
        NumberFeaturesTile numberFeaturesTile = new NumberFeaturesTile(context); // Custom feature tile implementation
        featureTiles.setMaxFeaturesTileDraw(numberFeaturesTile); // Draw feature count tiles when max features passed
        featureTiles.setIndexManager(indexer); // Set index manager to query feature indices
        Bitmap tile = featureTiles.drawTile(2, 2, 2);

        // URL Tile Generator (generate tiles from a URL)
        TileGenerator urlTileGenerator = new UrlTileGenerator(context, geoPackage,
                "url_tile_table", "http://url/{z}/{x}/{y}.png", 0, 0, boundingBox, projection);
        int urlTileCount = urlTileGenerator.generateTiles();

        // Feature Tile Generator (generate tiles from features)
        TileGenerator featureTileGenerator = new FeatureTileGenerator(context, geoPackage,
                "tiles_" + featureTable, featureTiles, 1, 2, boundingBox, projection);
        int featureTileCount = featureTileGenerator.generateTiles();

        // Close feature tiles (and indexer)
        featureTiles.close();

        // Close database when done
        geoPackage.close();

    }

}
