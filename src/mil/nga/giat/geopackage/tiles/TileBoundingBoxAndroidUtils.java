package mil.nga.giat.geopackage.tiles;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.graphics.RectF;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

/**
 * Tile Bounding Box utility methods relying on Android libraries
 * 
 * @author osbornb
 */
public class TileBoundingBoxAndroidUtils {

	/**
	 * Half the world distance in either direction
	 */
	private static double HALF_WORLD_WIDTH = 20037508.34789244;

	/**
	 * Get a rectangle using the tile width, height, bounding box, and the
	 * bounding box section within the outer box to build the rectangle from
	 * 
	 * @param width
	 * @param height
	 * @param boundingBox
	 * @param boundingBoxSection
	 * @return
	 */
	public static Rect getRectangle(long width, long height,
			TileBoundingBox boundingBox, TileBoundingBox boundingBoxSection) {

		RectF rectF = getFloatRectangle(width, height, boundingBox,
				boundingBoxSection);

		Rect rect = new Rect(Math.round(rectF.left), Math.round(rectF.top),
				Math.round(rectF.right), Math.round(rectF.bottom));

		return rect;
	}

	/**
	 * Get a rectangle with floating point boundaries using the tile width,
	 * height, bounding box, and the bounding box section within the outer box
	 * to build the rectangle from
	 * 
	 * @param width
	 * @param height
	 * @param boundingBox
	 * @param boundingBoxSection
	 * @return
	 */
	public static RectF getFloatRectangle(long width, long height,
			TileBoundingBox boundingBox, TileBoundingBox boundingBoxSection) {

		float left = TileBoundingBoxUtils.getXPixel(width, boundingBox,
				boundingBoxSection.getMinLongitude());
		float right = TileBoundingBoxUtils.getXPixel(width, boundingBox,
				boundingBoxSection.getMaxLongitude());
		float top = TileBoundingBoxUtils.getYPixel(height, boundingBox,
				boundingBoxSection.getMaxLatitude());
		float bottom = TileBoundingBoxUtils.getYPixel(height, boundingBox,
				boundingBoxSection.getMinLatitude());

		RectF rect = new RectF(left, top, right, bottom);

		return rect;
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
	public static TileBoundingBox getBoundingBox(int x, int y, int zoom) {

		int tilesPerSide = tilesPerSide(zoom);
		double tileWidthDegrees = tileWidthDegrees(tilesPerSide);
		double tileHeightDegrees = tileHeightDegrees(tilesPerSide);

		double minLon = -180.0 + (x * tileWidthDegrees);
		double maxLon = minLon + tileWidthDegrees;

		double maxLat = 90.0 - (y * tileHeightDegrees);
		double minLat = maxLat - tileHeightDegrees;

		TileBoundingBox box = new TileBoundingBox(minLon, maxLon, minLat,
				maxLat);

		return box;
	}

	/**
	 * Get the Web Mercator tile bounding box from the Google Maps API tile
	 * coordinates and zoom level
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @return
	 */
	public static TileBoundingBox getWebMercatorBoundingBox(int x, int y,
			int zoom) {

		int tilesPerSide = tilesPerSide(zoom);
		double tileSize = tileSize(tilesPerSide);

		double minLon = (-1 * HALF_WORLD_WIDTH) + (x * tileSize);
		double maxLon = (-1 * HALF_WORLD_WIDTH) + ((x + 1) * tileSize);
		double minLat = HALF_WORLD_WIDTH - ((y + 1) * tileSize);
		double maxLat = HALF_WORLD_WIDTH - (y * tileSize);

		TileBoundingBox box = new TileBoundingBox(minLon, maxLon, minLat,
				maxLat);

		return box;
	}

	/**
	 * Get the tile grid that includes the entire tile bounding box
	 * 
	 * @param boundingBox
	 * @param zoom
	 * @return
	 */
	public static TileGrid getTileGrid(TileBoundingBox boundingBox, int zoom) {

		int tilesPerSide = tilesPerSide(zoom);
		double tileWidthDegrees = tileWidthDegrees(tilesPerSide);
		double tileHeightDegrees = tileHeightDegrees(tilesPerSide);

		int minX = (int) ((boundingBox.getMinLongitude() + 180.0) / tileWidthDegrees);
		double tempMaxX = (boundingBox.getMaxLongitude() + 180.0)
				/ tileWidthDegrees;
		int maxX = (int) tempMaxX;
		if (tempMaxX % 1 == 0) {
			maxX--;
		}
		maxX = Math.min(maxX, tilesPerSide - 1);

		int minY = (int) ((90.0 - boundingBox.getMaxLatitude()) / tileHeightDegrees);
		double tempMaxY = (90.0 - boundingBox.getMinLatitude())
				/ tileHeightDegrees;
		int maxY = (int) tempMaxY;
		if (tempMaxY % 1 == 0) {
			maxY--;
		}
		maxY = Math.min(maxY, tilesPerSide - 1);

		TileGrid grid = new TileGrid(minX, maxX, minY, maxY);

		return grid;
	}

	/**
	 * Get the tile size in meters
	 * 
	 * @param tilesPerSide
	 * @return
	 */
	public static double tileSize(int tilesPerSide) {
		return (2 * HALF_WORLD_WIDTH) / tilesPerSide;
	}

	/**
	 * Get the tile width in degrees
	 * 
	 * @param tilesPerSide
	 * @return
	 */
	public static double tileWidthDegrees(int tilesPerSide) {
		return 360.0 / tilesPerSide;
	}

	/**
	 * Get the tile height in degrees
	 * 
	 * @param tilesPerSide
	 * @return
	 */
	public static double tileHeightDegrees(int tilesPerSide) {
		return 180.0 / tilesPerSide;
	}

	/**
	 * Get the tiles per side, width and height, at the zoom level
	 * 
	 * @param zoom
	 * @return
	 */
	public static int tilesPerSide(int zoom) {
		return (int) Math.pow(2, zoom);
	}

	/**
	 * Get the longitude distance in the middle latitude
	 * 
	 * @param boundingBox
	 * @return
	 */
	public static double getLongitudeDistance(TileBoundingBox boundingBox) {
		return getLongitudeDistance(boundingBox.getMinLongitude(),
				boundingBox.getMaxLongitude());
	}

	/**
	 * Get the longitude distance in the middle latitude
	 * 
	 * @param minLongitude
	 * @param maxLongitude
	 * @return
	 */
	public static double getLongitudeDistance(double minLongitude,
			double maxLongitude) {
		LatLng leftMiddle = new LatLng(0, minLongitude);
		LatLng middle = new LatLng(0, maxLongitude - minLongitude);
		LatLng rightMiddle = new LatLng(0, maxLongitude);

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
	public static double getLatitudeDistance(TileBoundingBox boundingBox) {
		return getLatitudeDistance(boundingBox.getMinLatitude(),
				boundingBox.getMaxLatitude());
	}

	/**
	 * Get the latitude distance in the middle longitude
	 * 
	 * @param minLatitude
	 * @param maxLatitude
	 * @return
	 */
	public static double getLatitudeDistance(double minLatitude,
			double maxLatitude) {
		LatLng lowerMiddle = new LatLng(minLatitude, 0);
		LatLng upperMiddle = new LatLng(maxLatitude, 0);
		double latDistance = SphericalUtil.computeDistanceBetween(lowerMiddle,
				upperMiddle);
		return latDistance;
	}

}
