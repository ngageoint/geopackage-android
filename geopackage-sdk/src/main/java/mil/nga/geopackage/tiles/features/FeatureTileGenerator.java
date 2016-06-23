package mil.nga.geopackage.tiles.features;

import android.content.Context;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.link.FeatureTileTableLinker;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.tiles.TileGenerator;

/**
 * Creates a set of tiles within a GeoPackage by generating tiles from features
 *
 * @author osbornb
 */
public class FeatureTileGenerator extends TileGenerator {

    /**
     * Feature tiles
     */
    private final FeatureTiles featureTiles;

    /**
     * Flag indicating whether the feature and tile tables should be linked
     */
    private boolean linkTables = true;

    /**
     * Constructor
     *
     * @param context      app context
     * @param geoPackage   GeoPackage
     * @param tableName    table name
     * @param featureTiles feature tiles
     * @param minZoom      min zoom
     * @param maxZoom      max zoom
     * @param boundingBox  tiles bounding box
     * @param projection   tiles projection
     * @since 1.3.0
     */
    public FeatureTileGenerator(Context context, GeoPackage geoPackage,
                                String tableName, FeatureTiles featureTiles, int minZoom, int maxZoom, BoundingBox boundingBox, Projection projection) {
        super(context, geoPackage, tableName, minZoom, maxZoom, boundingBox, projection);
        this.featureTiles = featureTiles;
    }

    /**
     * Is the feature table going to be linked with the tile table? Defaults to
     * true.
     *
     * @return true if tables will be linked upon generation
     * @since 1.2.5
     */
    public boolean isLinkTables() {
        return linkTables;
    }

    /**
     * Set the link tables flag
     *
     * @param linkTables
     * @since 1.2.5
     */
    public void setLinkTables(boolean linkTables) {
        this.linkTables = linkTables;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preTileGeneration() {

        // Link the feature and tile table if they are in the same GeoPackage
        GeoPackage geoPackage = getGeoPackage();
        String featureTable = featureTiles.getFeatureDao().getTableName();
        String tileTable = getTableName();
        if (linkTables && geoPackage.isFeatureTable(featureTable)
                && geoPackage.isTileTable(tileTable)) {
            FeatureTileTableLinker linker = new FeatureTileTableLinker(
                    geoPackage);
            linker.link(featureTable, tileTable);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte[] createTile(int z, long x, long y) {

        byte[] tileData = featureTiles.drawTileBytes((int) x, (int) y, z);

        return tileData;
    }

}
