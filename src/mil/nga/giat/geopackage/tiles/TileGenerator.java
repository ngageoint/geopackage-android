package mil.nga.giat.geopackage.tiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.R;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.contents.ContentsDao;
import mil.nga.giat.geopackage.io.BitmapConverter;
import mil.nga.giat.geopackage.io.GeoPackageIOUtils;
import mil.nga.giat.geopackage.io.GeoPackageProgress;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixKey;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.giat.geopackage.tiles.user.TileDao;
import mil.nga.giat.geopackage.tiles.user.TileRow;
import mil.nga.giat.geopackage.tiles.user.TileTable;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.SparseArray;

/**
 * Creates a set of tiles within a GeoPackage by downloading the tiles from a
 * URL
 * 
 * @author osbornb
 */
public class TileGenerator {

	/**
	 * URL EPSG pattern for finding the EPSG code in a url
	 */
	private static final Pattern URL_EPSG_PATTERN = Pattern.compile(
			"EPSG:(\\d+)", Pattern.CASE_INSENSITIVE);

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
	private BoundingBox boundingBox = new BoundingBox(-180.0, 180.0, -90.0,
			90.0);

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
	 * Projection EPSG
	 */
	private Long epsg;

	/**
	 * Compression options
	 */
	private Options options = null;

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
		if (urlHasBoundingBox) {
			Matcher matcher = URL_EPSG_PATTERN.matcher(tileUrl);
			if (matcher.find()) {
				String epsgString = matcher.group(1);
				epsg = Long.valueOf(epsgString);
			}
		}

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
	 * Set the Bitmap Compress Config
	 * 
	 * @param config
	 */
	public void setBitmapCompressionConfig(Config config) {
		if (options == null) {
			options = new Options();
		}
		options.inPreferredConfig = config;
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
			BoundingBox mercatorBox = TileBoundingBoxUtils
					.toWebMercator(boundingBox);
			for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
				// Get the tile grid the includes the entire bounding box
				TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
						mercatorBox, zoom);
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

		boolean update = false;

		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
		ContentsDao contentsDao = geoPackage.getContentsDao();

		TileMatrixSet tileMatrixSet = null;
		if (!tileMatrixSetDao.isTableExists()
				|| !tileMatrixSetDao.idExists(tableName)) {
			// Create the tile table
			tileMatrixSet = geoPackage.createTileTableWithMetadata(
					tableName,
					boundingBox,
					(long) context.getResources().getInteger(
							R.integer.geopackage_srs_epsg_srs_id));
		} else {
			update = true;
			// Retrieve the tile matrix set
			tileMatrixSet = tileMatrixSetDao.queryForId(tableName);
			Contents contents = tileMatrixSet.getContents();

			boolean expandBoundingBox = false;
			if (boundingBox.getMinLongitude() < tileMatrixSet.getMinX()) {
				tileMatrixSet.setMinX(boundingBox.getMinLongitude());
				contents.setMinX(tileMatrixSet.getMinX());
				expandBoundingBox = true;
			}
			if (boundingBox.getMaxLongitude() > tileMatrixSet.getMaxX()) {
				tileMatrixSet.setMaxX(boundingBox.getMaxLongitude());
				contents.setMaxX(tileMatrixSet.getMaxX());
				expandBoundingBox = true;
			}
			if (boundingBox.getMinLatitude() < tileMatrixSet.getMinY()) {
				tileMatrixSet.setMinY(boundingBox.getMinLatitude());
				contents.setMinY(tileMatrixSet.getMinY());
				expandBoundingBox = true;
			}
			if (boundingBox.getMaxLatitude() > tileMatrixSet.getMaxY()) {
				tileMatrixSet.setMaxY(boundingBox.getMaxLatitude());
				contents.setMaxY(tileMatrixSet.getMaxY());
				expandBoundingBox = true;
			}

			// Update the tile matrix set
			if (expandBoundingBox) {
				tileMatrixSetDao.update(tileMatrixSet);
				contentsDao.update(contents);
			}

		}

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
						tileGrid, update);
			}

			// Delete the table if cancelled
			if (progress != null && !progress.isActive()
					&& progress.cleanupOnCancel()) {
				geoPackage.deleteTableQuietly(tableName);
				count = 0;
			} else {
				// Update the contents last modified date
				contents.setLastChange(new Date());
				contentsDao.update(contents);
			}
		} catch (RuntimeException e) {
			geoPackage.deleteTableQuietly(tableName);
			throw e;
		} catch (SQLException e) {
			geoPackage.deleteTableQuietly(tableName);
			throw e;
		} catch (IOException e) {
			geoPackage.deleteTableQuietly(tableName);
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
	 * Generate the tiles for the zoom level
	 * 
	 * @param tileMatrixDao
	 * @param tileDao
	 * @param contents
	 * @param zoomLevel
	 * @param tileGrid
	 * @param update
	 * @return tile count
	 * @throws SQLException
	 * @throws IOException
	 */
	private int generateTiles(TileMatrixDao tileMatrixDao, TileDao tileDao,
			Contents contents, int zoomLevel, TileGrid tileGrid, boolean update)
			throws SQLException, IOException {

		int count = 0;

		Integer tileWidth = null;
		Integer tileHeight = null;

		// Get the full sized matrix grid width and height
		int matrixLength = TileBoundingBoxUtils.tilesPerSide(zoomLevel);

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
						bitmap = BitmapConverter.toBitmap(tileBytes, options);
						if (bitmap != null) {
							tileBytes = BitmapConverter.toBytes(bitmap,
									compressFormat, compressQuality);
						}
					}

					// If an update, delete an existing row
					if (update) {
						tileDao.deleteTile(x, y, zoomLevel);
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
							bitmap = BitmapConverter.toBitmap(tileBytes,
									options);
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

			// Check if the tile matrix already exists
			boolean create = true;
			if (update) {
				create = !tileMatrixDao.idExists(new TileMatrixKey(tableName,
						zoomLevel));
			}

			// Create the tile matrix
			if (create) {
				// Get the tile size
				int tilesPerSide = TileBoundingBoxUtils.tilesPerSide(zoomLevel);
				double tileSize = TileBoundingBoxUtils.tileSize(tilesPerSide);

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

		BoundingBox boundingBox = TileBoundingBoxUtils.getProjectedBoundingBox(
				epsg, x, y, z);

		url = replaceBoundingBox(url, boundingBox);

		return url;
	}

	/**
	 * Replace the url parts with the bounding box
	 * 
	 * @param url
	 * @param boundingBox
	 * @return
	 */
	private String replaceBoundingBox(String url, BoundingBox boundingBox) {

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

		String replacedUrl = replaceBoundingBox(url, boundingBox);
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
