package mil.nga.giat.geopackage.tiles;

import mil.nga.giat.geopackage.BoundingBox;

/**
 * Tile Bounding Box utility methods
 * 
 * @author osbornb
 */
public class TileBoundingBoxUtils {

	/**
	 * Get the overlapping bounding box between the two bounding boxes
	 * 
	 * @param boundingBox
	 * @param boundingBox2
	 * @return
	 */
	public static BoundingBox overlap(BoundingBox boundingBox,
			BoundingBox boundingBox2) {

		double minLongitude = Math.max(boundingBox.getMinLongitude(),
				boundingBox2.getMinLongitude());
		double maxLongitude = Math.min(boundingBox.getMaxLongitude(),
				boundingBox2.getMaxLongitude());
		double minLatitude = Math.max(boundingBox.getMinLatitude(),
				boundingBox2.getMinLatitude());
		double maxLatitude = Math.min(boundingBox.getMaxLatitude(),
				boundingBox2.getMaxLatitude());

		BoundingBox overlap = null;

		if (minLongitude < maxLongitude && minLatitude < maxLatitude) {
			overlap = new BoundingBox(minLongitude, maxLongitude,
					minLatitude, maxLatitude);
		}

		return overlap;
	}

	/**
	 * Get the X pixel for where the longitude fits into the bounding box
	 * 
	 * @param width
	 * @param boundingBox
	 * @param longitude
	 * @return
	 */
	public static float getXPixel(long width, BoundingBox boundingBox,
			double longitude) {

		double boxWidth = boundingBox.getMaxLongitude()
				- boundingBox.getMinLongitude();
		double offset = longitude - boundingBox.getMinLongitude();
		double percentage = offset / boxWidth;
		float pixel = (float) (percentage * width);

		return pixel;
	}

	/**
	 * Get the Y pixel for where the latitude fits into the bounding box
	 * 
	 * @param height
	 * @param boundingBox
	 * @param tileRowBoundingBox
	 * @return
	 */
	public static float getYPixel(long height, BoundingBox boundingBox,
			double latitude) {

		double boxHeight = boundingBox.getMaxLatitude()
				- boundingBox.getMinLatitude();
		double offset = latitude - boundingBox.getMaxLatitude();
		if (offset < 0) {
			offset *= -1;
		}
		double percentage = offset / boxHeight;
		float pixel = (float) (percentage * height);

		return pixel;
	}

}
