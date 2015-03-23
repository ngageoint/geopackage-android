package mil.nga.giat.geopackage.tiles;

import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.geopackage.BoundingBox;
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
			BoundingBox boundingBox, BoundingBox boundingBoxSection) {

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
			BoundingBox boundingBox, BoundingBox boundingBoxSection) {

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
	 * Get the longitude distance in the middle latitude
	 * 
	 * @param boundingBox
	 * @return
	 */
	public static double getLongitudeDistance(BoundingBox boundingBox) {
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
	public static double getLatitudeDistance(BoundingBox boundingBox) {
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
