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
        return getBoundedOverlay(tileDao);
    }

    /**
     * Get a Bounded Overlay Tile Provider for the Tile DAO
     *
     * @param tileDao
     * @return bounded overlay
     * @since 1.2.5
     */
    public static BoundedOverlay getBoundedOverlay(TileDao tileDao) {

        BoundedOverlay overlay = null;

        if (tileDao.isGoogleTiles()) {
            overlay = new GoogleAPIGeoPackageOverlay(tileDao);
        } else {
            overlay = new GeoPackageOverlay(tileDao);
        }

        return overlay;
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
