package mil.nga.giat.geopackage.tiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.R;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.io.BitmapConverter;
import mil.nga.giat.geopackage.io.GeoPackageIOUtils;
import mil.nga.giat.geopackage.io.GeoPackageProgress;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.user.TileDao;
import mil.nga.giat.geopackage.tiles.user.TileRow;
import mil.nga.giat.geopackage.tiles.user.TileTable;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.SparseArray;

/**
 * Creates a set of tiles within a GeoPackage by downloading the tiles from a
 * URL
 * 
 * @author osbornb
 */
public class TileGenerator {

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * GeoPackage
	 */
	private final GeoPackage geoPackage;

	/**
	 * Table Name
	 */
	private final String tableName;

	/**
	 * Tile URL
	 */
	private final String tileUrl;

	/**
	 * Min zoom level
	 */
	private final int minZoom;

	/**
	 * Max zoom level
	 */
	private final int maxZoom;

	/**
	 * Total tile count
	 */
	private Integer tileCount;

	/**
	 * Tile grids by zoom level
	 */
	private final SparseArray<TileGrid> tileGrids = new SparseArray<TileGrid>();

	/**
	 * Tile bounding box
	 */
	private BoundingBox boundingBox = new BoundingBox(-180.0, 180.0,
			-90.0, 90.0);

	/**
	 * Compress format
	 */
	private CompressFormat compressFormat = null;

	/**
	 * Compress quality
	 */
	private int compressQuality = 100;

	/**
	 * GeoPackage progress
	 */
	private GeoPackageProgress progress;

	/**
	 * True if the URL has x, y, or z variables
	 */
	private final boolean urlHasXYZ;

	/**
	 * True if the URL has bounding box variables
	 */
	private final boolean urlHasBoundingBox;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param geoPackage
	 * @param tableName
	 * @param tileUrl
	 * @param minZoom
	 * @param maxZoom
	 */
	public TileGenerator(Context context, GeoPackage geoPackage,
			String tableName, String tileUrl, int minZoom, int maxZoom) {
		this.context = context;
		this.geoPackage = geoPackage;
		this.tableName = tableName;
		try {
			this.tileUrl = URLDecoder.decode(tileUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new GeoPackageException("Failed to decode tile url: "
					+ tileUrl, e);
		}
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
		this.urlHasXYZ = hasXYZ(tileUrl);
		this.urlHasBoundingBox = hasBoundingBox(tileUrl);

		if (!this.urlHasXYZ && !this.urlHasBoundingBox) {
			throw new GeoPackageException(
					"URL does not contain x,y,z or bounding box variables: "
							+ tileUrl);
		}
	}

	/**
	 * Set the tile bounding box
	 * 
	 * @param boundingBox
	 */
	public void setTileBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	/**
	 * Set the compress format
	 * 
	 * @param compressFormat
	 */
	public void setCompressFormat(CompressFormat compressFormat) {
		this.compressFormat = compressFormat;
	}

	/**
	 * Set the compress quality. The Compress format must be set for this to be
	 * used.
	 * 
	 * @param compressQuality
	 */
	public void setCompressQuality(Integer compressQuality) {
		if (compressQuality != null) {
			this.compressQuality = compressQuality;
		}
	}

	/**
	 * Set the progress tracker
	 * 
	 * @param progress
	 */
	public void setProgress(GeoPackageProgress progress) {
		this.progress = progress;
	}

	/**
	 * Get the tile count of tiles to be generated
	 * 
	 * @return
	 */
	public int getTileCount() {
		if (tileCount == null) {
			// Get the tile grids and total tile count
			int count = 0;
			for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
				// Get the tile grid the includes the entire bounding box
				TileGrid tileGrid = TileBoundingBoxAndroidUtils.getTileGrid(
						boundingBox, zoom);
				count += tileGrid.count();
				tileGrids.put(zoom, tileGrid);
			}
			tileCount = count;
		}
		return tileCount;
	}

	/**
	 * Generate the tiles
	 * 
	 * @return tiles created
	 * @throws SQLException
	 * @throws IOException
	 */
	public int generateTiles() throws SQLException, IOException {

		int totalCount = getTileCount();

		// Set the max progress count
		if (progress != null) {
			progress.setMax(totalCount);
		}

		int count = 0;

		// Create the tile table
		TileMatrixSet tileMatrixSet = geoPackage.createTileTableWithMetadata(
				tableName, boundingBox, (long) context.getResources()
						.getInteger(R.integer.geopackage_srs_epsg_srs_id));

		// Download and create the tiles
		try {
			Contents contents = tileMatrixSet.getContents();
			TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
			TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

			// Create the new matrix tiles
			for (int zoom = minZoom; zoom <= maxZoom
					&& (progress == null || progress.isActive()); zoom++) {
				TileGrid tileGrid = tileGrids.get(zoom);
				count += generateTiles(tileMatrixDao, tileDao, contents, zoom,
						tileGrid);
			}

			// Delete the table if cancelled
			if (progress != null && !progress.isActive()
					&& progress.cleanupOnCancel()) {
				deleteTable(geoPackage, tableName);
				count = 0;
			}
		} catch (RuntimeException e) {
			deleteTable(geoPackage, tableName);
			throw e;
		} catch (SQLException e) {
			deleteTable(geoPackage, tableName);
			throw e;
		} catch (IOException e) {
			deleteTable(geoPackage, tableName);
			throw e;
		}

		return count;
	}

	/**
	 * Close the GeoPackage
	 */
	public void close() {
		if (geoPackage != null) {
			geoPackage.close();
		}
	}

	/**
	 * Attempt to queitly delete the table
	 * 
	 * @param geoPackage
	 * @param tableName
	 */
	private void deleteTable(GeoPackage geoPackage, String tableName) {
		try {
			geoPackage.deleteTable(tableName);
		} catch (Exception e2) {
			// eat
		}
	}

	/**
	 * Generate the tiles for the zoom level
	 * 
	 * @param tileMatrixDao
	 * @param tileDao
	 * @param contents
	 * @param zoomLevel
	 * @param tileGrid
	 * @return tile count
	 * @throws SQLException
	 * @throws IOException
	 */
	private int generateTiles(TileMatrixDao tileMatrixDao, TileDao tileDao,
			Contents contents, int zoomLevel, TileGrid tileGrid)
			throws SQLException, IOException {

		int count = 0;

		Integer tileWidth = null;
		Integer tileHeight = null;

		// Get the full sized matrix grid width and height
		int matrixLength = TileBoundingBoxAndroidUtils.tilesPerSide(zoomLevel);

		// Download and create the tile and each coordinate
		for (int x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {

			// Check if the progress has been cancelled
			if (progress != null && !progress.isActive()) {
				break;
			}

			for (int y = tileGrid.getMinY(); y <= tileGrid.getMaxY(); y++) {

				// Check if the progress has been cancelled
				if (progress != null && !progress.isActive()) {
					break;
				}

				try {
					// Download the tile
					byte[] tileBytes = downloadTile(zoomLevel, x, y);

					Bitmap bitmap = null;

					// Compress the image
					if (compressFormat != null) {
						bitmap = BitmapConverter.toBitmap(tileBytes);
						if (bitmap != null) {
							tileBytes = BitmapConverter.toBytes(bitmap,
									compressFormat, compressQuality);
						}
					}

					// Create a new tile row
					TileRow newRow = tileDao.newRow();
					newRow.setZoomLevel(zoomLevel);
					newRow.setTileColumn(x);
					newRow.setTileRow(y);
					newRow.setTileData(tileBytes);
					tileDao.create(newRow);

					count++;

					// Determine the tile width and height
					if (tileWidth == null) {
						if (bitmap == null) {
							bitmap = BitmapConverter.toBitmap(tileBytes);
						}
						if (bitmap != null) {
							tileWidth = bitmap.getWidth();
							tileHeight = bitmap.getHeight();
						}
					}
				} catch (Exception e) {
					// Skip this tile, don't increase count
				}

				// Update the progress count, even on failures
				if (progress != null) {
					progress.addProgress(1);
				}

			}

		}

		// If none of the tiles were translated into a bitmap with dimensions,
		// delete them
		if (tileWidth == null || tileHeight == null) {
			count = 0;

			StringBuilder where = new StringBuilder();

			where.append(tileDao.buildWhere(TileTable.COLUMN_ZOOM_LEVEL,
					zoomLevel));

			where.append(" AND ");
			where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_COLUMN,
					tileGrid.getMinX(), ">="));

			where.append(" AND ");
			where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_COLUMN,
					tileGrid.getMaxX(), "<="));

			where.append(" AND ");
			where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_ROW,
					tileGrid.getMinY(), ">="));

			where.append(" AND ");
			where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_ROW,
					tileGrid.getMaxY(), "<="));

			String[] whereArgs = tileDao.buildWhereArgs(new Object[] {
					zoomLevel, tileGrid.getMinX(), tileGrid.getMaxX(),
					tileGrid.getMinY(), tileGrid.getMaxY() });

			tileDao.delete(where.toString(), whereArgs);
		} else {

			// Get the tile size
			int tilesPerSide = TileBoundingBoxAndroidUtils
					.tilesPerSide(zoomLevel);
			double tileSize = TileBoundingBoxAndroidUtils
					.tileSize(tilesPerSide);

			// Calculate pixel sizes
			double pixelXSize = tileSize / tileWidth;
			double pixelYSize = tileSize / tileHeight;

			// Create the tile matrix for this zoom level
			TileMatrix tileMatrix = new TileMatrix();
			tileMatrix.setContents(contents);
			tileMatrix.setZoomLevel(zoomLevel);
			tileMatrix.setMatrixWidth(matrixLength);
			tileMatrix.setMatrixHeight(matrixLength);
			tileMatrix.setTileWidth(tileWidth);
			tileMatrix.setTileHeight(tileHeight);
			tileMatrix.setPixelXSize(pixelXSize);
			tileMatrix.setPixelYSize(pixelYSize);
			tileMatrixDao.create(tileMatrix);
		}

		return count;
	}

	/**
	 * Replace x, y, and z in the url
	 * 
	 * @param url
	 * @param z
	 * @param x
	 * @param y
	 * @return
	 */
	private String replaceXYZ(String url, int z, int x, int y) {

		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_z),
				String.valueOf(z));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_x),
				String.valueOf(x));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_y),
				String.valueOf(y));
		return url;
	}

	/**
	 * Determine if the url has x, y, or z variables
	 * 
	 * @param url
	 * @return
	 */
	private boolean hasXYZ(String url) {

		String replacedUrl = replaceXYZ(url, 0, 0, 0);
		boolean hasXYZ = !replacedUrl.equals(url);

		return hasXYZ;
	}

	/**
	 * Replace the bounding box coordinates in the url
	 * 
	 * @param url
	 * @param z
	 * @param x
	 * @param y
	 * @return
	 */
	private String replaceBoundingBox(String url, int z, int x, int y) {

		BoundingBox boundingBox = TileBoundingBoxAndroidUtils
				.getWebMercatorBoundingBox(x, y, z);

		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_min_lat),
				String.valueOf(boundingBox.getMinLatitude()));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_max_lat),
				String.valueOf(boundingBox.getMaxLatitude()));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_min_lon),
				String.valueOf(boundingBox.getMinLongitude()));
		url = url.replaceAll(
				context.getString(R.string.tile_generator_variable_max_lon),
				String.valueOf(boundingBox.getMaxLongitude()));

		return url;
	}

	/**
	 * Determine if the url has bounding box variables
	 * 
	 * @param url
	 * @return
	 */
	private boolean hasBoundingBox(String url) {

		String replacedUrl = replaceBoundingBox(url, 0, 0, 0);
		boolean hasBoundingBox = !replacedUrl.equals(url);

		return hasBoundingBox;
	}

	/**
	 * Download the tile
	 * 
	 * @param z
	 * @param x
	 * @param y
	 * @return
	 */
	private byte[] downloadTile(int z, int x, int y) {

		byte[] bytes = null;

		String zoomUrl = tileUrl;

		// Replace x, y, and z
		if (urlHasXYZ) {
			zoomUrl = replaceXYZ(zoomUrl, z, x, y);
		}

		// Replace bounding box
		if (urlHasBoundingBox) {
			zoomUrl = replaceBoundingBox(zoomUrl, z, x, y);
		}

		URL url;
		try {
			url = new URL(zoomUrl);
		} catch (MalformedURLException e) {
			throw new GeoPackageException("Failed to download tile. URL: "
					+ zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y, e);
		}

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new GeoPackageException("Failed to download tile. URL: "
						+ zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y);
			}

			InputStream geoPackageStream = connection.getInputStream();
			bytes = GeoPackageIOUtils.streamBytes(geoPackageStream);

		} catch (IOException e) {
			throw new GeoPackageException("Failed to download tile. URL: "
					+ zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y, e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return bytes;
	}

}
