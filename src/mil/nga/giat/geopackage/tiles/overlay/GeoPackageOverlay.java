package mil.nga.giat.geopackage.tiles.overlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.geopackage.io.BitmapConverter;
import mil.nga.giat.geopackage.tiles.TileBoundingBox;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.user.TileCursor;
import mil.nga.giat.geopackage.tiles.user.TileDao;
import mil.nga.giat.geopackage.tiles.user.TileRow;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.SphericalUtil;

/**
 * GeoPackage Map Overlay Tile Provider
 * 
 * @author osbornb
 */
public class GeoPackageOverlay implements TileProvider {

	/**
	 * Tile data access object
	 */
	private final TileDao tileDao;

	/**
	 * Constructor
	 * 
	 * @param tileDao
	 */
	public GeoPackageOverlay(TileDao tileDao) {
		this.tileDao = tileDao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tile getTile(int x, int y, int zoom) {

		Tile tile = null;

		// Get the bounding box of the requested tile
		TileBoundingBox boundingBox = getBoundingBox(x, y, zoom);

		// Get the lon and lat size in meters
		double lonDistance = getLongitudeDistance(boundingBox);
		double latDistance = getLatitudeDistance(boundingBox);

		// Get the zoom level to request based upon the tile size
		long zoomLevel = tileDao.getZoomLevel(lonDistance, latDistance);

		// Query for tiles in the bounding box at the zoom level
		TileCursor tileCursor = tileDao.queryByBoundingBox(boundingBox,
				zoomLevel);

		if (tileCursor != null) {
			TileMatrix tileMatrix = tileDao.getTileMatrix(zoomLevel);
			int width = (int) tileMatrix.getTileWidth();
			int height = (int) tileMatrix.getTileHeight();
			Bitmap tileBitmap = null;
			Canvas canvas = null;
			Paint paint = null;
			while (tileCursor.moveToNext()) {
				if (tileBitmap == null) {
					tileBitmap = Bitmap.createBitmap(width, height,
							Config.ARGB_8888);
					canvas = new Canvas(tileBitmap);
					paint = new Paint(Paint.ANTI_ALIAS_FLAG);
				}
				TileRow tileRow = tileCursor.getRow();
				Bitmap tileDataBitmap = tileRow.getTileDataBitmap();

				// TODO calculate the pixels
				canvas.drawBitmap(tileDataBitmap, 0, 0, paint);
			}
			if (tileBitmap != null) {
				try {
					byte[] tileData = BitmapConverter.toBytes(tileBitmap,
							CompressFormat.PNG);
					tile = new Tile(width, height, tileData);
				} catch (IOException e) {
					Log.e("Failed to create tile. x: " + x + ", y: " + y
							+ ", zoom: " + zoom, e.getMessage());
				}
			}
			tileCursor.close();
		}

		return tile;
	}

	/**
	 * Get the tile bounding box from the Google Maps API tile coordinates and
	 * zoom level
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @return
	 */
	private TileBoundingBox getBoundingBox(int x, int y, int zoom) {

		int tilesPerSide = (int) Math.pow(2, zoom);
		double tileWidthDegrees = 360.0 / tilesPerSide;
		double tileHeightDegrees = 180.0 / tilesPerSide;

		double minLon = -180.0 + (x * tileWidthDegrees);
		double maxLon = minLon + tileWidthDegrees;

		double maxLat = 90.0 - (y * tileHeightDegrees);
		double minLat = maxLat - tileHeightDegrees;

		TileBoundingBox box = new TileBoundingBox(minLon, maxLon, minLat,
				maxLat);

		return box;
	}

	/**
	 * Get the longitude distance in the middle latitude
	 * 
	 * @param boundingBox
	 * @return
	 */
	private double getLongitudeDistance(TileBoundingBox boundingBox) {
		LatLng leftMiddle = new LatLng(0, boundingBox.getMinLongitude());
		LatLng middle = new LatLng(0, boundingBox.getMaxLongitude()
				- boundingBox.getMinLongitude());
		LatLng rightMiddle = new LatLng(0, boundingBox.getMaxLongitude());

		List<LatLng> path = new ArrayList<LatLng>();
		path.add(leftMiddle);
		path.add(middle);
		path.add(rightMiddle);

		double lonDistance = SphericalUtil.computeLength(path);
		return lonDistance;
	}

	/**
	 * Get the latitude distance in the middle longitude
	 * 
	 * @param boundingBox
	 * @return
	 */
	private double getLatitudeDistance(TileBoundingBox boundingBox) {
		LatLng lowerMiddle = new LatLng(boundingBox.getMinLatitude(), 0);
		LatLng upperMiddle = new LatLng(boundingBox.getMaxLatitude(), 0);
		double latDistance = SphericalUtil.computeDistanceBetween(lowerMiddle,
				upperMiddle);
		return latDistance;
	}

}
