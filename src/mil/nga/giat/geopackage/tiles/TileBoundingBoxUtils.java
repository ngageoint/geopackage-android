package mil.nga.giat.geopackage.tiles;

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.geom.Point;
import mil.nga.giat.geopackage.geom.unit.Projection;
import mil.nga.giat.geopackage.geom.unit.ProjectionConstants;
import mil.nga.giat.geopackage.geom.unit.ProjectionFactory;
import mil.nga.giat.geopackage.geom.unit.ProjectionTransform;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.user.TileMatrixRange;
import mil.nga.giat.geopackage.tiles.user.TileRow;

/**
 * Tile Bounding Box utility methods
 * 
 * @author osbornb
 */
public class TileBoundingBoxUtils {

	/**
	 * Transformation from WGS 84 to Web Mercator
	 */
	private static ProjectionTransform toWebMercator = ProjectionFactory
			.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
			.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);

	/**
	 * Web mercator projection
	 */
	private static Projection webMercator = ProjectionFactory
			.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

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
			overlap = new BoundingBox(minLongitude, maxLongitude, minLatitude,
					maxLatitude);
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

	/**
	 * Get the tile bounding box from the Google Maps API tile coordinates and
	 * zoom level
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @return
	 */
	public static BoundingBox getBoundingBox(int x, int y, int zoom) {

		int tilesPerSide = tilesPerSide(zoom);
		double tileWidthDegrees = tileWidthDegrees(tilesPerSide);
		double tileHeightDegrees = tileHeightDegrees(tilesPerSide);

		double minLon = -180.0 + (x * tileWidthDegrees);
		double maxLon = minLon + tileWidthDegrees;

		double maxLat = 90.0 - (y * tileHeightDegrees);
		double minLat = maxLat - tileHeightDegrees;

		BoundingBox box = new BoundingBox(minLon, maxLon, minLat, maxLat);

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
	public static BoundingBox getWebMercatorBoundingBox(int x, int y, int zoom) {

		int tilesPerSide = tilesPerSide(zoom);
		double tileSize = tileSize(tilesPerSide);

		double minLon = (-1 * ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH)
				+ (x * tileSize);
		double maxLon = (-1 * ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH)
				+ ((x + 1) * tileSize);
		double minLat = ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH
				- ((y + 1) * tileSize);
		double maxLat = ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH
				- (y * tileSize);

		BoundingBox box = new BoundingBox(minLon, maxLon, minLat, maxLat);

		return box;
	}

	/**
	 * Get the Projected tile bounding box from the Google Maps API tile
	 * coordinates and zoom level
	 * 
	 * @param projectionEpsg
	 * @param x
	 * @param y
	 * @param zoom
	 * @return
	 */
	public static BoundingBox getProjectedBoundingBox(Long projectionEpsg,
			int x, int y, int zoom) {

		BoundingBox boundingBox = getWebMercatorBoundingBox(x, y, zoom);

		if (projectionEpsg != null) {
			ProjectionTransform transform = webMercator
					.getTransformation(projectionEpsg);
			boundingBox = transform.transform(boundingBox);
		}

		return boundingBox;
	}

	/**
	 * Get the Projected tile bounding box from the Google Maps API tile
	 * coordinates and zoom level
	 * 
	 * @param projection
	 * @param x
	 * @param y
	 * @param zoom
	 * @return
	 */
	public static BoundingBox getProjectedBoundingBox(Projection projection,
			int x, int y, int zoom) {

		BoundingBox boundingBox = getWebMercatorBoundingBox(x, y, zoom);

		if (projection != null) {
			ProjectionTransform transform = webMercator
					.getTransformation(projection);
			boundingBox = transform.transform(boundingBox);
		}

		return boundingBox;
	}

	/**
	 * Get the tile grid that includes the entire tile bounding box
	 * 
	 * @param webMercatorBoundingBox
	 * @param zoom
	 * @return
	 */
	public static TileGrid getTileGrid(BoundingBox webMercatorBoundingBox,
			int zoom) {

		int tilesPerSide = tilesPerSide(zoom);
		double tileSize = tileSize(tilesPerSide);

		int minX = (int) ((webMercatorBoundingBox.getMinLongitude() + ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH) / tileSize);
		double tempMaxX = (webMercatorBoundingBox.getMaxLongitude() + ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH)
				/ tileSize;
		int maxX = (int) tempMaxX;
		if (tempMaxX % 1 == 0) {
			maxX--;
		}
		maxX = Math.min(maxX, tilesPerSide - 1);

		int minY = (int) (((webMercatorBoundingBox.getMaxLatitude() - ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH) * -1) / tileSize);
		double tempMaxY = ((webMercatorBoundingBox.getMinLatitude() - ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH) * -1)
				/ tileSize;
		int maxY = (int) tempMaxY;
		if (tempMaxY % 1 == 0) {
			maxY--;
		}
		maxX = Math.min(maxX, tilesPerSide - 1);

		TileGrid grid = new TileGrid(minX, maxX, minY, maxY);

		return grid;
	}

	/**
	 * Convert the bounding box coordinates to a new web mercator bounding box
	 * 
	 * @param boundingBox
	 */
	public static BoundingBox toWebMercator(BoundingBox boundingBox) {

		double minLatitude = Math.max(boundingBox.getMinLatitude(),
				ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE);
		double maxLatitude = Math.min(boundingBox.getMaxLatitude(),
				ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE);

		Point lowerLeftPoint = new Point(false, false,
				boundingBox.getMinLongitude(), minLatitude);
		Point upperRightPoint = new Point(false, false,
				boundingBox.getMaxLongitude(), maxLatitude);

		lowerLeftPoint = toWebMercator.transform(lowerLeftPoint);
		upperRightPoint = toWebMercator.transform(upperRightPoint);

		BoundingBox mercatorBox = new BoundingBox(lowerLeftPoint.getX(),
				upperRightPoint.getX(), lowerLeftPoint.getY(),
				upperRightPoint.getY());

		return mercatorBox;
	}

	/**
	 * Get the tile size in meters
	 * 
	 * @param tilesPerSide
	 * @return
	 */
	public static double tileSize(int tilesPerSide) {
		return (2 * ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH)
				/ tilesPerSide;
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
	 * Get the tile column range
	 * 
	 * @param webMercatorTotalBox
	 * @param tileMatrix
	 * @param webMercatorBoundingBox
	 * @return
	 */
	public static TileMatrixRange getTileColumnRange(
			BoundingBox webMercatorTotalBox, TileMatrix tileMatrix,
			BoundingBox webMercatorBoundingBox) {
		return getTileColumnRange(webMercatorTotalBox, tileMatrix,
				webMercatorBoundingBox.getMinLongitude(),
				webMercatorBoundingBox.getMaxLongitude());
	}

	/**
	 * Get the tile getTileColumnRange range
	 * 
	 * @param webMercatorTotalBox
	 * @param tileMatrix
	 * @param minLongitude
	 *            in meters
	 * @param maxLongitude
	 *            in meters
	 * @return
	 */
	private static TileMatrixRange getTileColumnRange(
			BoundingBox webMercatorTotalBox, TileMatrix tileMatrix,
			double minLongitude, double maxLongitude) {

		TileMatrixRange range = null;

		long minColumn = getTileColumn(webMercatorTotalBox, tileMatrix,
				minLongitude);
		long maxColumn = getTileColumn(webMercatorTotalBox, tileMatrix,
				maxLongitude);

		if (minColumn < tileMatrix.getMatrixWidth() && maxColumn >= 0) {

			if (minColumn < 0) {
				minColumn = 0;
			}
			if (maxColumn >= tileMatrix.getMatrixWidth()) {
				maxColumn = tileMatrix.getMatrixWidth() - 1;
			}

			range = new TileMatrixRange(minColumn, maxColumn);
		}

		return range;
	}

	/**
	 * Get the tile column of the longitude in degrees
	 * 
	 * @param webMercatorTotalBox
	 * @param tileMatrix
	 * @param longitude
	 *            in meters
	 * @return tile column if in the range, -1 if before,
	 *         {@link TileMatrix#getMatrixWidth()} if after
	 */
	private static long getTileColumn(BoundingBox webMercatorTotalBox,
			TileMatrix tileMatrix, double longitude) {

		double minX = webMercatorTotalBox.getMinLongitude();
		double maxX = webMercatorTotalBox.getMaxLongitude();

		long tileId;
		if (longitude < minX) {
			tileId = -1;
		} else if (longitude >= maxX) {
			tileId = tileMatrix.getMatrixWidth();
		} else {
			double matrixWidth = webMercatorTotalBox.getMaxLongitude()
					- webMercatorTotalBox.getMinLongitude();
			double tileWidth = matrixWidth / tileMatrix.getMatrixWidth();
			tileId = (long) ((longitude - minX) / tileWidth);
		}

		return tileId;
	}

	/**
	 * Get the tile row range
	 * 
	 * @param webMercatorTotalBox
	 * @param tileMatrix
	 * @param boundingBox
	 * @return
	 */
	public static TileMatrixRange getTileRowRange(
			BoundingBox webMercatorTotalBox, TileMatrix tileMatrix,
			BoundingBox boundingBox) {
		return getTileRowRange(webMercatorTotalBox, tileMatrix,
				boundingBox.getMinLatitude(), boundingBox.getMaxLatitude());
	}

	/**
	 * Get the tile row range
	 * 
	 * @param webMercatorTotalBox
	 * @param tileMatrix
	 * @param minLatitude
	 *            in meters
	 * @param maxLatitude
	 *            in meters
	 * @return
	 */
	private static TileMatrixRange getTileRowRange(
			BoundingBox webMercatorTotalBox, TileMatrix tileMatrix,
			double minLatitude, double maxLatitude) {

		TileMatrixRange range = null;

		long maxRow = getTileRow(webMercatorTotalBox, tileMatrix, minLatitude);
		long minRow = getTileRow(webMercatorTotalBox, tileMatrix, maxLatitude);

		if (minRow < tileMatrix.getMatrixHeight() && maxRow >= 0) {

			if (minRow < 0) {
				minRow = 0;
			}
			if (maxRow >= tileMatrix.getMatrixHeight()) {
				maxRow = tileMatrix.getMatrixHeight() - 1;
			}

			range = new TileMatrixRange(minRow, maxRow);
		}

		return range;
	}

	/**
	 * Get the tile row of the latitude in degrees
	 * 
	 * @param webMercatorTotalBox
	 * @param tileMatrix
	 * @param latitude
	 *            in meters
	 * @return tile row if in the range, -1 if before,
	 *         {@link TileMatrix#getMatrixHeight()} if after
	 */
	private static long getTileRow(BoundingBox webMercatorTotalBox,
			TileMatrix tileMatrix, double latitude) {

		double minY = webMercatorTotalBox.getMinLatitude();
		double maxY = webMercatorTotalBox.getMaxLatitude();

		long tileId;
		if (latitude <= minY) {
			tileId = tileMatrix.getMatrixHeight();
		} else if (latitude > maxY) {
			tileId = -1;
		} else {
			double matrixHeight = webMercatorTotalBox.getMaxLatitude()
					- webMercatorTotalBox.getMinLatitude();
			double tileHeight = matrixHeight / tileMatrix.getMatrixHeight();
			tileId = (long) ((maxY - latitude) / tileHeight);
		}

		return tileId;
	}

	/**
	 * Get the web mercator bounding box of the Tile Row from the Tile Matrix
	 * zoom level
	 * 
	 * @param webMercatorTotalBox
	 * @param tileMatrix
	 * @param tileRow
	 * @return
	 */
	public static BoundingBox getWebMercatorBoundingBox(
			BoundingBox webMercatorTotalBox, TileMatrix tileMatrix,
			TileRow tileRow) {

		long column = tileRow.getTileColumn();
		long row = tileRow.getTileRow();

		// Get the tile width
		double matrixMinX = webMercatorTotalBox.getMinLongitude();
		double matrixMaxX = webMercatorTotalBox.getMaxLongitude();
		double matrixWidth = matrixMaxX - matrixMinX;
		double tileWidth = matrixWidth / tileMatrix.getMatrixWidth();

		// Find the longitude range
		double minLon = matrixMinX + (tileWidth * column);
		double maxLon = minLon + tileWidth;

		// Get the tile height
		double matrixMinY = webMercatorTotalBox.getMinLatitude();
		double matrixMaxY = webMercatorTotalBox.getMaxLatitude();
		double matrixHeight = matrixMaxY - matrixMinY;
		double tileHeight = matrixHeight / tileMatrix.getMatrixHeight();

		// Find the latitude range
		double maxLat = matrixMaxY - (tileHeight * row);
		double minLat = maxLat - tileHeight;

		BoundingBox boundingBox = new BoundingBox(minLon, maxLon, minLat,
				maxLat);

		return boundingBox;
	}

}
