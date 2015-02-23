package mil.nga.giat.geopackage.tiles.overlay;

import java.io.IOException;

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.io.BitmapConverter;
import mil.nga.giat.geopackage.tiles.TileBoundingBoxAndroidUtils;
import mil.nga.giat.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.user.TileCursor;
import mil.nga.giat.geopackage.tiles.user.TileDao;
import mil.nga.giat.geopackage.tiles.user.TileRow;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

/**
 * GeoPackage Map Overlay Tile Provider
 * 
 * @author osbornb
 */
public class GeoPackageOverlay implements TileProvider {

	/**
	 * Compress format
	 */
	private static final CompressFormat COMPRESS_FORMAT = CompressFormat.PNG;

	/**
	 * Tile data access object
	 */
	private final TileDao tileDao;

	/**
	 * Tile width
	 */
	private Integer width;

	/**
	 * Tile height
	 */
	private Integer height;

	/**
	 * Constructor using GeoPackage tile sizes
	 * 
	 * @param tileDao
	 */
	public GeoPackageOverlay(TileDao tileDao) {
		this.tileDao = tileDao;
	}

	/**
	 * Constructor with specified tile size
	 * 
	 * @param tileDao
	 * @param width
	 * @param height
	 */
	public GeoPackageOverlay(TileDao tileDao, int width, int height) {
		this.tileDao = tileDao;
		this.width = width;
		this.height = height;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tile getTile(int x, int y, int zoom) {

		Tile tile = null;

		// Get the bounding box of the requested tile
		BoundingBox boundingBox = TileBoundingBoxAndroidUtils
				.getBoundingBox(x, y, zoom);

		// Get the lon and lat size in meters
		double lonDistance = TileBoundingBoxAndroidUtils
				.getLongitudeDistance(boundingBox);
		double latDistance = TileBoundingBoxAndroidUtils
				.getLatitudeDistance(boundingBox);

		// Get the zoom level to request based upon the tile size
		long zoomLevel = tileDao.getZoomLevel(lonDistance, latDistance);

		// Query for tiles in the bounding box at the zoom level
		TileCursor tileCursor = tileDao.queryByBoundingBox(boundingBox,
				zoomLevel);
		if (tileCursor != null) {

			// Get the tile matrix at the zoom level
			TileMatrix tileMatrix = tileDao.getTileMatrix(zoomLevel);

			// Get the requested tile dimensions
			int tileWidth = width != null ? width : (int) tileMatrix
					.getTileWidth();
			int tileHeight = height != null ? height : (int) tileMatrix
					.getTileHeight();

			// Draw the resulting bitmap
			Bitmap tileBitmap = null;
			Canvas canvas = null;
			Paint paint = null;
			while (tileCursor.moveToNext()) {

				// Get the next tile
				TileRow tileRow = tileCursor.getRow();
				Bitmap tileDataBitmap = tileRow.getTileDataBitmap();

				// Get the bounding box of the tile
				BoundingBox tileBoundingBox = tileDao.getBoundingBox(
						tileMatrix, tileRow);

				// Get the bounding box where the requested image and tile
				// overlap
				BoundingBox overlap = TileBoundingBoxUtils.overlap(
						boundingBox, tileBoundingBox);

				// If the tile overlaps with the requested box
				if (overlap != null) {

					// Get the rectangle of the tile image to draw
					Rect src = TileBoundingBoxAndroidUtils.getRectangle(
							tileMatrix.getTileWidth(),
							tileMatrix.getTileHeight(), tileBoundingBox,
							overlap);

					// Get the rectangle of where to draw the tile in the
					// resulting image
					RectF dest = TileBoundingBoxAndroidUtils.getFloatRectangle(
							tileWidth, tileHeight, boundingBox, overlap);

					// Create the bitmap first time through
					if (tileBitmap == null) {
						tileBitmap = Bitmap.createBitmap(tileWidth, tileHeight,
								Config.ARGB_8888);
						canvas = new Canvas(tileBitmap);
						paint = new Paint(Paint.ANTI_ALIAS_FLAG);
					}

					// Draw the tile to the bitmap
					canvas.drawBitmap(tileDataBitmap, src, dest, paint);
				}
			}
			tileCursor.close();

			// Create the tile
			if (tileBitmap != null) {
				try {
					byte[] tileData = BitmapConverter.toBytes(tileBitmap,
							COMPRESS_FORMAT);
					tile = new Tile(tileWidth, tileHeight, tileData);
				} catch (IOException e) {
					Log.e("Failed to create tile. x: " + x + ", y: " + y
							+ ", zoom: " + zoom, e.getMessage());
				}
			}

		}

		return tile;
	}

}
