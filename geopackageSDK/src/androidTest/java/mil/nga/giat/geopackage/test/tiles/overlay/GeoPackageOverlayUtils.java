package mil.nga.giat.geopackage.test.tiles.overlay;

import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.giat.geopackage.tiles.overlay.GeoPackageOverlay;
import mil.nga.giat.geopackage.tiles.user.TileDao;

import com.google.android.gms.maps.model.Tile;

/**
 * GeoPackage overlay utils
 * 
 * @author osbornb
 */
public class GeoPackageOverlayUtils {

	/**
	 * Test overlay
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testOverlay(GeoPackage geoPackage) throws SQLException {

		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

		if (tileMatrixSetDao.isTableExists()) {
			List<TileMatrixSet> results = tileMatrixSetDao.queryForAll();

			for (TileMatrixSet tileMatrixSet : results) {

				TileDao dao = geoPackage.getTileDao(tileMatrixSet);

				GeoPackageOverlay overlay = new GeoPackageOverlay(dao);

				for (int zoom = 0; zoom <= 21; zoom++) {
					int tileLength = (int) Math.pow(2, zoom);

					int column = (int) (Math.random() * tileLength);
					int row = (int) (Math.random() * tileLength);

					for (int maxColumns = Math.min(tileLength, column + 2); column < maxColumns; column++) {
						for (int maxRows = Math.min(tileLength, row + 2); row < maxRows; row++) {
							Tile tile = overlay.getTile(column, row, zoom);
							if (tile != null) {
								TestCase.assertTrue(tile.height > 0);
								TestCase.assertTrue(tile.width > 0);
							}
						}
					}

				}

			}

		}

	}

}
