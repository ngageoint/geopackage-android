package mil.nga.giat.geopackage.tiles.user;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.geom.unit.CoordinateConverter;
import mil.nga.giat.geopackage.geom.unit.DegreeConverter;
import mil.nga.giat.geopackage.geom.unit.DistanceConverter;
import mil.nga.giat.geopackage.geom.unit.MeterConverter;
import mil.nga.giat.geopackage.tiles.TileBoundingBox;
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
	 * Coordinate converter
	 */
	private final CoordinateConverter coordinateConverter;

	/**
	 * Distance converter
	 */
	private final DistanceConverter distanceConverter;

	/**
	 * Constructor
	 * 
	 * @param db
	 * @param tileMatrixSet
	 * @param tileMatrices
	 * @param table
	 * @param coordinateConverter
	 * @param distanceConverter
	 */
	public TileDao(SQLiteDatabase db, TileMatrixSet tileMatrixSet,
			List<TileMatrix> tileMatrices, TileTable table,
			CoordinateConverter coordinateConverter,
			DistanceConverter distanceConverter) {
		super(db, table);

		this.tileMatrixSet = tileMatrixSet;
		this.tileMatrices = tileMatrices;
		this.widths = new double[tileMatrices.size()];
		this.heights = new double[tileMatrices.size()];

		// Set the coordinate converters
		if (coordinateConverter == null) {
			coordinateConverter = new DegreeConverter();
		}
		this.coordinateConverter = coordinateConverter;
		if (distanceConverter == null) {
			distanceConverter = new MeterConverter();
		}
		this.distanceConverter = distanceConverter;

		// Set the min and max zoom levels
		minZoom = tileMatrices.get(0).getZoomLevel();
		maxZoom = tileMatrices.get(tileMatrices.size() - 1).getZoomLevel();

		// Populate the zoom level to tile matrix and the sorted tile widths and
		// heights
		for (int i = 0; i < tileMatrices.size(); i++) {
			TileMatrix tileMatrix = tileMatrices.get(i);
			zoomLevelToTileMatrix.put(tileMatrix.getZoomLevel(), tileMatrix);
			widths[tileMatrices.size() - i - 1] = distanceConverter
					.toMeters(tileMatrix.getPixelXSize()
							* tileMatrix.getTileWidth());
			heights[tileMatrices.size() - i - 1] = distanceConverter
					.toMeters(tileMatrix.getPixelYSize()
							* tileMatrix.getTileHeight());
		}

		// Set the matrix width and height
		matrixWidth = distanceConverter.toMeters(tileMatrixSet.getMaxX()
				- tileMatrixSet.getMinX());
		matrixHeight = distanceConverter.toMeters(tileMatrixSet.getMaxY()
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

	public CoordinateConverter getCoordinateConverter() {
		return coordinateConverter;
	}

	public DistanceConverter getDistanceConverter() {
		return distanceConverter;
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
	 * @return
	 */
	public TileCursor queryByBoundingBox(TileBoundingBox boundingBox,
			long zoomLevel) {

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
			TileBoundingBox boundingBox) {
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

		Long minColumn = getTileColumn(tileMatrix, minLongitude);
		Long maxColumn = getTileColumn(tileMatrix, maxLongitude);

		if (minColumn != null || maxColumn != null) {

			if (minColumn == null) {
				minColumn = 0l;
			}
			if (maxColumn == null) {
				maxColumn = tileMatrix.getMatrixWidth() - 1;
			}

			range = new TileMatrixRange(minColumn, maxColumn);
		}

		return range;
	}

	/**
	 * Get the tile column of the value in distance units
	 * 
	 * @param tileMatrix
	 * @param longitude
	 *            in degrees
	 * @return
	 */
	public Long getTileColumn(TileMatrix tileMatrix, double longitude) {

		Long tileId = null;

		double minX = coordinateConverter.toDegrees(tileMatrixSet.getMinX());
		double maxX = coordinateConverter.toDegrees(tileMatrixSet.getMaxX());

		if (longitude >= minX && longitude < maxX) {
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
			TileBoundingBox boundingBox) {
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

		Long maxRow = getTileRow(tileMatrix, minLatitude);
		Long minRow = getTileRow(tileMatrix, maxLatitude);

		if (minRow != null || maxRow != null) {

			if (minRow == null) {
				minRow = 0l;
			}
			if (maxRow == null) {
				maxRow = tileMatrix.getMatrixHeight() - 1;
			}

			range = new TileMatrixRange(minRow, maxRow);
		}

		return range;
	}

	/**
	 * Get the tile row of the value in meters
	 * 
	 * @param tileMatrix
	 * @param latitude
	 *            in degrees
	 * @return
	 */
	public Long getTileRow(TileMatrix tileMatrix, double latitude) {

		Long tileId = null;

		double minY = coordinateConverter.toDegrees(tileMatrixSet.getMinY());
		double maxY = coordinateConverter.toDegrees(tileMatrixSet.getMaxY());

		if (latitude > minY && latitude <= maxY) {
			double tileHeight = getTileHeight(tileMatrix);
			tileId = (long) ((maxY - latitude) / tileHeight);
		}

		return tileId;
	}

	/**
	 * Get the tile height in distance units
	 * 
	 * @param tileMatrix
	 * @return
	 */
	public double getTileHeight(TileMatrix tileMatrix) {
		return matrixHeight / tileMatrix.getMatrixHeight();
	}

}