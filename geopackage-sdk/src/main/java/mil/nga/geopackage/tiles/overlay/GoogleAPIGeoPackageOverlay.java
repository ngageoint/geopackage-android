package mil.nga.geopackage.tiles.overlay;

import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

/**
 * GeoPackage Map Overlay Tile Provider, assumes the Google Maps API zoom level
 * and grid
 * 
 * @author osbornb
 */
public class GoogleAPIGeoPackageOverlay implements TileProvider {

	/**
	 * Tile data access object
	 */
	private final TileDao tileDao;

	/**
	 * Constructor
	 * 
	 * @param tileDao
	 */
	public GoogleAPIGeoPackageOverlay(TileDao tileDao) {
		this.tileDao = tileDao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tile getTile(int x, int y, int zoom) {

		Tile tile = null;

		TileRow tileRow = tileDao.queryForTile(x, y, zoom);
		if (tileRow != null) {
			TileMatrix tileMatrix = tileDao.getTileMatrix(zoom);
			int tileWidth = (int) tileMatrix.getTileWidth();
			int tileHeight = (int) tileMatrix.getTileHeight();
			tile = new Tile(tileWidth, tileHeight, tileRow.getTileData());
		}

		return tile;
	}

}
