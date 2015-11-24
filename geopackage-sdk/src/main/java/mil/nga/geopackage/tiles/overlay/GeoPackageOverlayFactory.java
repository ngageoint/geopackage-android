package mil.nga.geopackage.tiles.overlay;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import mil.nga.geopackage.tiles.retriever.GeoPackageTile;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * Get a tile provider for the Tile DAO
 *
 * @author osbornb
 */
public class GeoPackageOverlayFactory {

    /**
     * Get a Tile Provider for the Tile DAO
     *
     * @param tileDao
     * @return
     */
    public static TileProvider getTileProvider(TileDao tileDao) {

        TileProvider provider = null;

        if (tileDao.isGoogleTiles()) {
            provider = new GoogleAPIGeoPackageOverlay(tileDao);
        } else {
            provider = new GeoPackageOverlay(tileDao);
        }

        return provider;
    }

    /**
     * Get a map tile from the GeoPackage tile
     *
     * @param geoPackageTile
     * @return tile
     */
    public static Tile getTile(GeoPackageTile geoPackageTile) {
        Tile tile = null;
        if (geoPackageTile != null) {
            tile = new Tile(geoPackageTile.getWidth(), geoPackageTile.getHeight(), geoPackageTile.getData());
        }
        return tile;
    }

}
