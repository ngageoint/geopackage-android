package mil.nga.giat.geopackage.tiles;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.R;
import mil.nga.giat.geopackage.core.contents.Contents;
import mil.nga.giat.geopackage.core.contents.ContentsDao;
import mil.nga.giat.geopackage.core.contents.ContentsDataType;
import mil.nga.giat.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.giat.geopackage.io.BitmapConverter;
import mil.nga.giat.geopackage.io.GeoPackageIOUtils;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrix;
import mil.nga.giat.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.giat.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.giat.geopackage.tiles.user.TileColumn;
import mil.nga.giat.geopackage.tiles.user.TileDao;
import mil.nga.giat.geopackage.tiles.user.TileRow;
import mil.nga.giat.geopackage.tiles.user.TileTable;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

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
	 * Tile bounding box
	 */
	private TileBoundingBox boundingBox = new TileBoundingBox(-180.0, 180.0,
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
		this.tileUrl = tileUrl;
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
	}

	/**
	 * Set the tile bounding box
	 * 
	 * @param boundingBox
	 */
	public void setTileBoundingBox(TileBoundingBox boundingBox) {
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
		this.compressQuality = compressQuality;
	}

	/**
	 * Generate the tiles
	 * 
	 * @return tiles created
	 * @throws SQLException
	 * @throws IOException
	 */
	public int generateTiles() throws SQLException, IOException {

		int count = 0;

		SpatialReferenceSystem srs = getSrs();

		// Get the DAOs
		ContentsDao contentsDao = geoPackage.getContentsDao();
		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
		TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

		// Create the Tile Matrix Set and Tile Matrix tables
		geoPackage.createTileMatrixSetTable();
		geoPackage.createTileMatrixTable();

		// Create the user tile table
		List<TileColumn> columns = TileTable.createRequiredColumns();
		TileTable table = new TileTable(tableName, columns);
		geoPackage.createTable(table);

		try {
			// Create the contents
			Contents contents = new Contents();
			contents.setTableName(tableName);
			contents.setDataType(ContentsDataType.TILES);
			contents.setIdentifier(tableName);
			contents.setLastChange(new Date());
			contents.setMinX(boundingBox.getMinLongitude());
			contents.setMinY(boundingBox.getMinLatitude());
			contents.setMaxX(boundingBox.getMaxLongitude());
			contents.setMaxY(boundingBox.getMaxLatitude());
			contents.setSrs(srs);
			contentsDao.create(contents);

			// Create new matrix tile set
			TileMatrixSet tileMatrixSet = new TileMatrixSet();
			tileMatrixSet.setContents(contents);
			tileMatrixSet.setSrs(contents.getSrs());
			tileMatrixSet.setMinX(contents.getMinX());
			tileMatrixSet.setMinY(contents.getMinY());
			tileMatrixSet.setMaxX(contents.getMaxX());
			tileMatrixSet.setMaxY(contents.getMaxY());
			tileMatrixSetDao.create(tileMatrixSet);

			TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

			// Create the new matrix tiles
			for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
				count += generateTiles(tileMatrixDao, tileDao, contents, zoom);
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
	 * @return tile count
	 * @throws SQLException
	 * @throws IOException
	 */
	private int generateTiles(TileMatrixDao tileMatrixDao, TileDao tileDao,
			Contents contents, int zoomLevel) throws SQLException, IOException {

		int count = 0;

		Integer tileWidth = null;
		Integer tileHeight = null;

		// Get the full sized matrix grid width and height
		int matrixLength = TileBoundingBoxAndroidUtils.tilesPerSide(zoomLevel);

		// Get the tile grid the includes the entire bounding box
		TileGrid tileGrid = TileBoundingBoxAndroidUtils.getTileGrid(
				boundingBox, zoomLevel);

		// Download and create the tile and each coordinate
		for (int x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {

			for (int y = tileGrid.getMinY(); y <= tileGrid.getMaxY(); y++) {

				// Download the tile
				byte[] tileBytes = downloadTile(zoomLevel, x, y);

				Bitmap bitmap = null;

				// Compress the image
				if (compressFormat != null) {
					bitmap = BitmapConverter.toBitmap(tileBytes);
					tileBytes = BitmapConverter.toBytes(bitmap, compressFormat,
							compressQuality);
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
					tileWidth = bitmap.getWidth();
					tileHeight = bitmap.getHeight();
				}

			}

		}

		// Get the bounding box of the the middle coordinate
		int middleRowAndColumn = matrixLength / 2;
		TileBoundingBox boundingBox = TileBoundingBoxAndroidUtils
				.getBoundingBox(middleRowAndColumn, middleRowAndColumn,
						zoomLevel);

		// Get the distances from the middle bounding box
		double longitudeDistance = TileBoundingBoxAndroidUtils
				.getLongitudeDistance(boundingBox);
		double latitudeDistance = TileBoundingBoxAndroidUtils
				.getLatitudeDistance(boundingBox);

		// Calculate pixel sizes
		double pixelXSize = longitudeDistance / tileWidth;
		double pixelYSize = latitudeDistance / tileHeight;

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

		return count;
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

		// TODO configure
		zoomUrl = zoomUrl.replaceAll("\\{z\\}", String.valueOf(z));
		zoomUrl = zoomUrl.replaceAll("\\{x\\}", String.valueOf(x));
		zoomUrl = zoomUrl.replaceAll("\\{y\\}", String.valueOf(y));

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

	/**
	 * Get the Spatial Reference System, EPSG
	 * 
	 * @return
	 * @throws SQLException
	 */
	private SpatialReferenceSystem getSrs() throws SQLException {
		SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
				.queryForId(
						(long) context.getResources().getInteger(
								R.integer.geopackage_srs_epsg_srs_id));
		if (srs == null) {
			throw new GeoPackageException(
					"EPSG Spatial Reference System could not be found. SRS ID: "
							+ context.getResources().getInteger(
									R.integer.geopackage_srs_epsg_srs_id));
		}
		return srs;
	}

}