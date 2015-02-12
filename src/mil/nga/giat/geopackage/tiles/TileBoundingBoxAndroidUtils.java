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
	public static double getLongitudeDistance(TileBoundingBox boundingBox) {
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
	public static double getLatitudeDistance(TileBoundingBox boundingBox) {
		LatLng lowerMiddle = new LatLng(boundingBox.getMinLatitude(), 0);
		LatLng upperMiddle = new LatLng(boundingBox.getMaxLatitude(), 0);
		double latDistance = SphericalUtil.computeDistanceBetween(lowerMiddle,
				upperMiddle);
		return latDistance;
	}

}
