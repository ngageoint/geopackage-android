package mil.nga.geopackage.extension.link;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * Feature Tile Table linker, used to link feature and tile tables together when
 * the tiles represent the feature data
 *
 * @author osbornb
 * @since 1.2.6
 */
public class FeatureTileTableLinker extends FeatureTileTableCoreLinker {

    /**
     * GeoPackage
     */
    private final GeoPackage geoPackage;

    /**
     * Constructor
     *
     * @param geoPackage
     */
    public FeatureTileTableLinker(GeoPackage geoPackage) {
        super(geoPackage);
        this.geoPackage = geoPackage;
    }

    /**
     * Query for the tile tables linked to a feature table and return tile DAOs
     * to those tables
     *
     * @param featureTable feature table
     * @return tiles DAOs
     */
    public List<TileDao> getTileDaosForFeatureTable(String featureTable) {

        List<TileDao> tileDaos = new ArrayList<TileDao>();

        List<String> tileTables = getTileTablesForFeatureTable(featureTable);
        for (String tileTable : tileTables) {
            if (geoPackage.isTileTable(tileTable)) {
                TileDao tileDao = geoPackage.getTileDao(tileTable);
                tileDaos.add(tileDao);
            }
        }

        return tileDaos;
    }

    /**
     * Query for the feature tables linked to a tile table and return feature
     * DAOs to those tables
     *
     * @param tileTable tile table
     * @return feature DAOs
     */
    public List<FeatureDao> getFeatureDaosForTileTable(String tileTable) {

        List<FeatureDao> featureDaos = new ArrayList<FeatureDao>();

        List<String> featureTables = getFeatureTablesForTileTable(tileTable);
        for (String featureTable : featureTables) {
            if (geoPackage.isFeatureTable(featureTable)) {
                FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
                featureDaos.add(featureDao);
            }
        }

        return featureDaos;
    }

}
