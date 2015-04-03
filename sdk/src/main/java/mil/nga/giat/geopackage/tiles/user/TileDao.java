package mil.nga.giat.geopackage.tiles.user;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.projection.ProjectionConstants;
import mil.nga.giat.geopackage.projection.ProjectionFactory;
import mil.nga.giat.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.giat.geopackage.tiles.TileGrid;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.user.UserDao;

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

		projection = ProjectionFactory.getProjection(tileMatrixSet.getSrs()
				.getOrganizationCoordsysId());

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
	 * Adjust the tile matrix lengths if needed. Check if the tile matrix width
	 * and height need to expand to account for pixel * number of pixels fitting
	 * into the tile matrix lengths
	 */
	public void adjustTileMatrixLengths() {
		double tileMatrixWidth = tileMatrixSet.getMaxX()
				- tileMatrixSet.getMinX();
		double tileMatrixHeight = tileMatrixSet.getMaxY()
				- tileMatrixSet.getMinY();
		for (TileMatrix tileMatrix : tileMatrices) {
			int tempMatrixWidth = (int) (tileMatrixWidth / (tileMatrix
					.getPixelXSize() * tileMatrix.getTileWidth()));
			int tempMatrixHeight = (int) (tileMatrixHeight / (tileMatrix
					.getPixelYSize() * tileMatrix.getTileHeight()));
			if (tempMatrixWidth > tileMatrix.getMatrixWidth()) {
				tileMatrix.setMatrixWidth(tempMatrixWidth);
			}
			if (tempMatrixHeight > tileMatrix.getMatrixHeight()) {
				tileMatrix.setMatrixHeight(tempMatrixHeight);
			}
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
	 * Query for Tiles at a zoom level in descending row and column order
	 * 
	 * @param zoomLevel
	 * @return tile cursor, should be closed
	 */
	public TileCursor queryForTileDescending(long zoomLevel) {
		return queryForEq(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel, null, null,
				TileTable.COLUMN_TILE_ROW + " DESC, "
						+ TileTable.COLUMN_TILE_COLUMN + " DESC");
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
	 * @param length
	 *            in meters
	 * @return
	 */
	public Long getZoomLevel(double length) {

		Long zoomLevel = null;

		// Find where the width and height fit in
		int widthIndex = Arrays.binarySearch(widths, length);
		if (widthIndex < 0) {
			widthIndex = (widthIndex + 1) * -1;
		}
		int heightIndex = Arrays.binarySearch(heights, length);
		if (heightIndex < 0) {
			heightIndex = (heightIndex + 1) * -1;
		}

		// Find the closest width or verify it isn't too small or large
		if (widthIndex == 0) {
			if (length < widths[widthIndex] * .51) {
				widthIndex = -1;
			}
		} else if (widthIndex == widths.length) {
			if (length >= widths[widthIndex - 1] / .51) {
				widthIndex = -1;
			} else {
				widthIndex = widthIndex - 1;
			}
		} else if (length - widths[widthIndex - 1] < widths[widthIndex]
				- length) {
			widthIndex--;
		}

		// Find the closest height or verify it isn't too small or large
		if (heightIndex == 0) {
			if (length < heights[heightIndex] * .51) {
				heightIndex = -1;
			}
		} else if (heightIndex == heights.length) {
			if (length >= heights[heightIndex - 1] / .51) {
				heightIndex = -1;
			} else {
				heightIndex = heightIndex - 1;
			}
		} else if (length - heights[heightIndex - 1] < heights[heightIndex]
				- length) {
			heightIndex--;
		}

		if (widthIndex >= 0 && heightIndex >= 0) {

			// Use one zoom size smaller if possible
			int index = Math.min(widthIndex, heightIndex);
			if (index >= 0) {

				TileMatrix tileMatrix = tileMatrices.get(tileMatrices.size()
						- index - 1);
				zoomLevel = tileMatrix.getZoomLevel();
			}
		}

		return zoomLevel;
	}

	/**
	 * Query by tile grid and zoom level
	 * 
	 * @param tileGrid
	 * @param zoomLevel
	 * @return cursor from query or null if the zoom level tile ranges do not
	 *         overlap the bounding box
	 */
	public TileCursor queryByTileGrid(TileGrid tileGrid, long zoomLevel) {

		TileCursor tileCursor = null;

		if (tileGrid != null) {

			StringBuilder where = new StringBuilder();

			where.append(buildWhere(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel));

			where.append(" AND ");
			where.append(buildWhere(TileTable.COLUMN_TILE_COLUMN,
					tileGrid.getMinX(), ">="));

			where.append(" AND ");
			where.append(buildWhere(TileTable.COLUMN_TILE_COLUMN,
					tileGrid.getMaxX(), "<="));

			where.append(" AND ");
			where.append(buildWhere(TileTable.COLUMN_TILE_ROW,
					tileGrid.getMinY(), ">="));

			where.append(" AND ");
			where.append(buildWhere(TileTable.COLUMN_TILE_ROW,
					tileGrid.getMaxY(), "<="));

			String[] whereArgs = buildWhereArgs(new Object[] { zoomLevel,
					tileGrid.getMinX(), tileGrid.getMaxX(), tileGrid.getMinY(),
					tileGrid.getMaxY() });

			tileCursor = query(where.toString(), whereArgs);
		}

		return tileCursor;
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

	/**
	 * Count of Tiles at a zoom level
	 * 
	 * @param zoomLevel
	 * @return count
	 */
	public int count(long zoomLevel) {
		String where = buildWhere(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel);
		String[] whereArgs = buildWhereArgs(zoomLevel);
		return count(where, whereArgs);
	}

	/**
	 * Determine if the tiles are in the Google tile coordinate format
	 * 
	 * @return
	 */
	public boolean isGoogleTiles() {

		// Convert the bounding box to wgs84
		BoundingBox boundingBox = tileMatrixSet.getBoundingBox();
		BoundingBox wgs84BoundingBox = projection.getTransformation(
				ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM).transform(
				boundingBox);

		boolean googleTiles = false;

		// Verify the bounds are the entire world
		if (wgs84BoundingBox.getMinLatitude() <= ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE
				&& wgs84BoundingBox.getMaxLatitude() >= ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE
				&& wgs84BoundingBox.getMinLongitude() <= -180.0
				&& wgs84BoundingBox.getMaxLongitude() >= 180.0) {

			googleTiles = true;

			// Verify each tile matrix is the correct width and height
			for (TileMatrix tileMatrix : tileMatrices) {
				long zoomLevel = tileMatrix.getZoomLevel();
				long tilesPerSide = TileBoundingBoxUtils
						.tilesPerSide((int) zoomLevel);
				if (tileMatrix.getMatrixWidth() != tilesPerSide
						|| tileMatrix.getMatrixHeight() != tilesPerSide) {
					googleTiles = false;
					break;
				}
			}
		}

		return googleTiles;
	}

}
