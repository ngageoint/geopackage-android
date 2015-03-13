package mil.nga.giat.geopackage.tiles.overlay;

import mil.nga.giat.geopackage.tiles.user.TileDao;

import com.google.android.gms.maps.model.TileProvider;

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

}
