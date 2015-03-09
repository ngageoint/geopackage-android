package mil.nga.giat.geopackage.tiles.user;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.geom.unit.Projection;
import mil.nga.giat.geopackage.geom.unit.ProjectionConstants;
import mil.nga.giat.geopackage.geom.unit.ProjectionFactory;
import mil.nga.giat.geopackage.geom.unit.ProjectionTransform;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.user.UserDao;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;

/**
 * Tile DAO for reading tile user tables
 * 
 * @author osbornb
 */
public class TileDao extends UserDao<TileTable, TileRow, TileCursor> {

	/**
	 * Tile Matrix Set
	 */
	private final TileMatrixSet tileMatrixSet;

	/**
	 * Tile Matrices
	 */
	private final List<TileMatrix> tileMatrices;

	/**
	 * Mapping between zoom levels and the tile matrix
	 */
	private final LongSparseArray<TileMatrix> zoomLevelToTileMatrix = new LongSparseArray<TileMatrix>();

	/**
	 * Min zoom
	 */
	private final long minZoom;

	/**
	 * Max zoom
	 */
	private final long maxZoom;

	/**
	 * Array of widths of the tiles at each zoom level in meters
	 */
	private final double[] widths;

	/**
	 * Array of heights of the tiles at each zoom level in meters
	 */
	private final double[] heights;

	/**
	 * Matrix width in meters
	 */
	private final double matrixWidth;

	/**
	 * Matrix height in meters
	 */
	private final double matrixHeight;

	/**
	 * Projection
	 */
	private Projection projection;

	/**
	 * Transformation to WGS 84
	 */
	private ProjectionTransform toWgs84;

	/**
	 * Constructor
	 * 
	 * @param db
	 * @param tileMatrixSet
	 * @param tileMatrices
	 * @param table
	 */
	public TileDao(SQLiteDatabase db, TileMatrixSet tileMatrixSet,
			List<TileMatrix> tileMatrices, TileTable table) {
		super(db, table);

		this.tileMatrixSet = tileMatrixSet;
		this.tileMatrices = tileMatrices;
		this.widths = new double[tileMatrices.size()];
		this.heights = new double[tileMatrices.size()];

		projection = ProjectionFactory.getProjection(tileMatrixSet.getSrsId());
		toWgs84 = projection
				.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		// Set the min and max zoom levels
		if (!tileMatrices.isEmpty()) {
			minZoom = tileMatrices.get(0).getZoomLevel();
			maxZoom = tileMatrices.get(tileMatrices.size() - 1).getZoomLevel();
		} else {
			minZoom = 0;
			maxZoom = 0;
		}

		// Populate the zoom level to tile matrix and the sorted tile widths and
		// heights
		for (int i = 0; i < tileMatrices.size(); i++) {
			TileMatrix tileMatrix = tileMatrices.get(i);
			zoomLevelToTileMatrix.put(tileMatrix.getZoomLevel(), tileMatrix);
			widths[tileMatrices.size() - i - 1] = projection
					.toMeters(tileMatrix.getPixelXSize()
							* tileMatrix.getTileWidth());
			heights[tileMatrices.size() - i - 1] = projection
					.toMeters(tileMatrix.getPixelYSize()
							* tileMatrix.getTileHeight());
		}

		// Set the matrix width and height
		matrixWidth = projection.toMeters(tileMatrixSet.getMaxX()
				- tileMatrixSet.getMinX());
		matrixHeight = projection.toMeters(tileMatrixSet.getMaxY()
				- tileMatrixSet.getMinY());

		if (tileMatrixSet.getContents() == null) {
			throw new GeoPackageException(TileMatrixSet.class.getSimpleName()
					+ " " + tileMatrixSet.getId() + " has null "
					+ Contents.class.getSimpleName());
		}
		if (tileMatrixSet.getSrs() == null) {
			throw new GeoPackageException(TileMatrixSet.class.getSimpleName()
					+ " " + tileMatrixSet.getId() + " has null "
					+ SpatialReferenceSystem.class.getSimpleName());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileRow newRow() {
		return new TileRow(getTable());
	}

	/**
	 * Get the tile matrix set
	 * 
	 * @return
	 */
	public TileMatrixSet getTileMatrixSet() {
		return tileMatrixSet;
	}

	/**
	 * Get the tile matrices
	 * 
	 * @return
	 */
	public List<TileMatrix> getTileMatrices() {
		return tileMatrices;
	}

	/**
	 * Get the tile matrix at the zoom level
	 * 
	 * @param zoomLevel
	 * @return
	 */
	public TileMatrix getTileMatrix(long zoomLevel) {
		return zoomLevelToTileMatrix.get(zoomLevel);
	}

	/**
	 * Get the min zoom
	 * 
	 * @return
	 */
	public long getMinZoom() {
		return minZoom;
	}

	/**
	 * Get the max zoom
	 * 
	 * @return
	 */
	public long getMaxZoom() {
		return maxZoom;
	}

	/**
	 * Get the projection
	 * 
	 * @return
	 */
	public Projection getProjection() {
		return projection;
	}

	/**
	 * Query for a Tile
	 * 
	 * @param column
	 * @param row
	 * @param zoomLevel
	 * @return
	 */
	public TileRow queryForTile(long column, long row, long zoomLevel) {

		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put(TileTable.COLUMN_TILE_COLUMN, column);
		fieldValues.put(TileTable.COLUMN_TILE_ROW, row);
		fieldValues.put(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel);

		TileCursor cursor = queryForFieldValues(fieldValues);
		TileRow tileRow = null;
		try {
			if (cursor.moveToNext()) {
				tileRow = cursor.getRow();
			}
		} finally {
			cursor.close();
		}

		return tileRow;
	}

	/**
	 * Query for Tiles at a zoom level
	 * 
	 * @param zoomLevel
	 * @return tile cursor, should be closed
	 */
	public TileCursor queryForTile(long zoomLevel) {
		return queryForEq(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel);
	}

	/**
	 * Query for Tiles at a zoom level and column
	 * 
	 * @param column
	 * @param zoomLevel
	 * @return
	 */
	public TileCursor queryForTilesInColumn(long column, long zoomLevel) {

		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put(TileTable.COLUMN_TILE_COLUMN, column);
		fieldValues.put(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel);

		return queryForFieldValues(fieldValues);
	}

	/**
	 * Query for Tiles at a zoom level and row
	 * 
	 * @param row
	 * @param zoomLevel
	 * @return
	 */
	public TileCursor queryForTilesInRow(long row, long zoomLevel) {

		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put(TileTable.COLUMN_TILE_ROW, row);
		fieldValues.put(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel);

		return queryForFieldValues(fieldValues);
	}

	/**
	 * Get the zoom level for the provided width and height in the default units
	 * 
	 * @param width
	 *            in meters
	 * @param height
	 *            in meters
	 * @return
	 */
	public long getZoomLevel(double width, double height) {

		// Find where the width and height fit in
		int widthIndex = Arrays.binarySearch(widths, width);
		if (widthIndex < 0) {
			widthIndex = (widthIndex * -1) - 2;
		}
		int heightIndex = Arrays.binarySearch(heights, height);
		if (heightIndex < 0) {
			heightIndex = (heightIndex * -1) - 2;
		}

		// Use one zoom size smaller if possible
		int index = Math.min(widthIndex, heightIndex);
		index = Math.max(0, index);

		TileMatrix tileMatrix = tileMatrices.get(tileMatrices.size() - index
				- 1);

		return tileMatrix.getZoomLevel();
	}

	/**
	 * Query by bounding box and zoom level
	 * 
	 * @param boundingBox
	 * @param zoomLevel
	 * @return cursor from query or null if the zoom level tile ranges do not
	 *         overlap the bounding box
	 */
	public TileCursor queryByBoundingBox(BoundingBox boundingBox, long zoomLevel) {

		TileCursor tileCursor = null;

		// Get the tile matrix at the zoom level
		TileMatrix tileMatrix = getTileMatrix(zoomLevel);

		// Find the column and row ranges including in the bounding box
		TileMatrixRange columnRange = getTileColumnRange(tileMatrix,
				boundingBox);
		TileMatrixRange rowRange = getTileRowRange(tileMatrix, boundingBox);

		if (columnRange != null && rowRange != null) {

			StringBuilder where = new StringBuilder();

			where.append(buildWhere(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel));

			where.append(" AND ");
			where.append(buildWhere(TileTable.COLUMN_TILE_COLUMN,
					columnRange.getMin(), ">="));

			where.append(" AND ");
			where.append(buildWhere(TileTable.COLUMN_TILE_COLUMN,
					columnRange.getMax(), "<="));

			where.append(" AND ");
			where.append(buildWhere(TileTable.COLUMN_TILE_ROW,
					rowRange.getMin(), ">="));

			where.append(" AND ");
			where.append(buildWhere(TileTable.COLUMN_TILE_ROW,
					rowRange.getMax(), "<="));

			String[] whereArgs = buildWhereArgs(new Object[] { zoomLevel,
					columnRange.getMin(), columnRange.getMax(),
					rowRange.getMin(), rowRange.getMax() });

			tileCursor = query(where.toString(), whereArgs);
		}

		return tileCursor;
	}

	/**
	 * Get the tile column range
	 * 
	 * @param tileMatrix
	 * @param boundingBox
	 * @return
	 */
	public TileMatrixRange getTileColumnRange(TileMatrix tileMatrix,
			BoundingBox boundingBox) {
		return getTileColumnRange(tileMatrix, boundingBox.getMinLongitude(),
				boundingBox.getMaxLongitude());
	}

	/**
	 * Get the tile getTileColumnRange range
	 * 
	 * @param tileMatrix
	 * @param minLongitude
	 *            in degrees
	 * @param maxLongitude
	 *            in degrees
	 * @return
	 */
	public TileMatrixRange getTileColumnRange(TileMatrix tileMatrix,
			double minLongitude, double maxLongitude) {

		TileMatrixRange range = null;

		long minColumn = getTileColumn(tileMatrix, minLongitude);
		long maxColumn = getTileColumn(tileMatrix, maxLongitude);

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
	 * @param tileMatrix
	 * @param longitude
	 *            in degrees
	 * @return tile column if in the range, -1 if before,
	 *         {@link TileMatrix#getMatrixWidth()} if after
	 */
	public long getTileColumn(TileMatrix tileMatrix, double longitude) {

		double minX = toWgs84.transformLongitude(tileMatrixSet.getMinX());
		double maxX = toWgs84.transformLongitude(tileMatrixSet.getMaxX());

		long tileId;
		if (longitude < minX) {
			tileId = -1;
		} else if (longitude >= maxX) {
			tileId = tileMatrix.getMatrixWidth();
		} else {
			double tileWidth = getTileWidth(tileMatrix);
			tileId = (long) ((longitude - minX) / tileWidth);
		}

		return tileId;
	}

	/**
	 * Get the tile width in meters
	 * 
	 * @param tileMatrix
	 * @return
	 */
	public double getTileWidth(TileMatrix tileMatrix) {
		return matrixWidth / tileMatrix.getMatrixWidth();
	}

	/**
	 * Get the tile row range
	 * 
	 * @param tileMatrix
	 * @param boundingBox
	 * @return
	 */
	public TileMatrixRange getTileRowRange(TileMatrix tileMatrix,
			BoundingBox boundingBox) {
		return getTileRowRange(tileMatrix, boundingBox.getMinLatitude(),
				boundingBox.getMaxLatitude());
	}

	/**
	 * Get the tile row range
	 * 
	 * @param tileMatrix
	 * @param minLatitude
	 *            in degrees
	 * @param maxLatitude
	 *            in degrees
	 * @return
	 */
	public TileMatrixRange getTileRowRange(TileMatrix tileMatrix,
			double minLatitude, double maxLatitude) {

		TileMatrixRange range = null;

		long maxRow = getTileRow(tileMatrix, minLatitude);
		long minRow = getTileRow(tileMatrix, maxLatitude);

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
	 * @param tileMatrix
	 * @param latitude
	 *            in degrees
	 * @return tile row if in the range, -1 if before,
	 *         {@link TileMatrix#getMatrixHeight()} if after
	 */
	public long getTileRow(TileMatrix tileMatrix, double latitude) {

		double minY = toWgs84.transformLatitude(tileMatrixSet.getMinY());
		double maxY = toWgs84.transformLatitude(tileMatrixSet.getMaxY());

		long tileId;
		if (latitude <= minY) {
			tileId = tileMatrix.getMatrixHeight();
		} else if (latitude > maxY) {
			tileId = -1;
		} else {
			double tileHeight = getTileHeight(tileMatrix);
			tileId = (long) ((maxY - latitude) / tileHeight);
		}

		return tileId;
	}

	/**
	 * Get the tile height in meters
	 * 
	 * @param tileMatrix
	 * @return
	 */
	public double getTileHeight(TileMatrix tileMatrix) {
		return matrixHeight / tileMatrix.getMatrixHeight();
	}

	/**
	 * Get the bounding box of the tile row
	 * 
	 * @param tileRow
	 * @return
	 */
	public BoundingBox getBoundingBox(TileRow tileRow) {

		// Get the tile matrix at the zoom level
		TileMatrix tileMatrix = getTileMatrix(tileRow.getZoomLevel());

		return getBoundingBox(tileMatrix, tileRow);
	}

	/**
	 * Get the bounding box of the tile row at the known zoom level
	 * 
	 * @param zoomLevel
	 * @param tileRow
	 * @return
	 */
	public BoundingBox getBoundingBox(long zoomLevel, TileRow tileRow) {

		// Get the tile matrix at the zoom level
		TileMatrix tileMatrix = getTileMatrix(zoomLevel);

		return getBoundingBox(tileMatrix, tileRow);
	}

	/**
	 * Get the bounding box of the Tile Row from the Tile Matrix zoom level
	 * 
	 * @param tileMatrix
	 * @param tileRow
	 * @return
	 */
	public BoundingBox getBoundingBox(TileMatrix tileMatrix, TileRow tileRow) {

		long column = tileRow.getTileColumn();
		long row = tileRow.getTileRow();

		// Get the tile width in degrees
		double matrixMinX = toWgs84.transformLongitude(tileMatrixSet.getMinX());
		double matrixMaxX = toWgs84.transformLongitude(tileMatrixSet.getMaxX());
		double matrixWidth = matrixMaxX - matrixMinX;
		double tileWidth = matrixWidth / tileMatrix.getMatrixWidth();

		// Find the longitude range
		double minLon = matrixMinX + (tileWidth * column);
		double maxLon = minLon + tileWidth;

		// Get the tile height in degrees
		double matrixMinY = toWgs84.transformLatitude(tileMatrixSet.getMinY());
		double matrixMaxY = toWgs84.transformLatitude(tileMatrixSet.getMaxY());
		double matrixHeight = matrixMaxY - matrixMinY;
		double tileHeight = matrixHeight / tileMatrix.getMatrixHeight();

		// Find the latitude range
		double maxLat = matrixMaxY - (tileHeight * row);
		double minLat = maxLat - tileHeight;

		BoundingBox boundingBox = new BoundingBox(minLon, maxLon, minLat,
				maxLat);

		return boundingBox;
	}

	/**
	 * Delete a Tile
	 * 
	 * @param column
	 * @param row
	 * @param zoomLevel
	 * @return number deleted, should be 0 or 1
	 */
	public int deleteTile(long column, long row, long zoomLevel) {

		StringBuilder where = new StringBuilder();

		where.append(buildWhere(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel));

		where.append(" AND ");
		where.append(buildWhere(TileTable.COLUMN_TILE_COLUMN, column));

		where.append(" AND ");
		where.append(buildWhere(TileTable.COLUMN_TILE_ROW, row));

		String[] whereArgs = buildWhereArgs(new Object[] { zoomLevel, column,
				row });

		int deleted = delete(where.toString(), whereArgs);

		return deleted;
	}

}
