package mil.nga.geopackage.tiles;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.R;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.projection.Projection;

/**
 * Creates a set of tiles within a GeoPackage by downloading the tiles from a
 * URL
 *
 * @author osbornb
 */
public class UrlTileGenerator extends TileGenerator {

    /**
     * Tile URL
     */
    private final String tileUrl;

    /**
     * True if the URL has x, y, or z variables
     */
    private final boolean urlHasXYZ;

    /**
     * True if the URL has bounding box variables
     */
    private final boolean urlHasBoundingBox;

    /**
     * TMS URL flag, when true x,y,z converted to TMS when requesting the tile
     */
    private boolean tms = false;

    /**
     * Constructor
     *
     * @param context     app context
     * @param geoPackage  GeoPackage
     * @param tableName   table name
     * @param tileUrl     tile url
     * @param minZoom     min zoom
     * @param maxZoom     max zoom
     * @param boundingBox tiles bounding box
     * @param projection  tiles projection
     * @since 1.3.0
     */
    public UrlTileGenerator(Context context, GeoPackage geoPackage,
                            String tableName, String tileUrl, int minZoom, int maxZoom, BoundingBox boundingBox, Projection projection) {
        super(context, geoPackage, tableName, minZoom, maxZoom, boundingBox, projection);

        try {
            this.tileUrl = URLDecoder.decode(tileUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new GeoPackageException("Failed to decode tile url: "
                    + tileUrl, e);
        }

        this.urlHasXYZ = hasXYZ(tileUrl);
        this.urlHasBoundingBox = hasBoundingBox(tileUrl);

        if (!this.urlHasXYZ && !this.urlHasBoundingBox) {
            throw new GeoPackageException(
                    "URL does not contain x,y,z or bounding box variables: "
                            + tileUrl);
        }
    }

    /**
     * Is TMS URL
     *
     * @return true if TMS URL
     * @since 1.2.1
     */
    public boolean isTms() {
        return tms;
    }

    /**
     * Set the TMS URL flag
     *
     * @param tms true if a TMS URL
     * @since 1.2.1
     */
    public void setTms(boolean tms) {
        this.tms = tms;
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
     * Replace x, y, and z in the url
     *
     * @param url
     * @param z
     * @param x
     * @param y
     * @return
     */
    private String replaceXYZ(String url, int z, long x, long y) {

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
    private String replaceBoundingBox(String url, int z, long x, long y) {

        BoundingBox boundingBox = TileBoundingBoxUtils.getProjectedBoundingBox(
                projection, x, y, z);

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
     * {@inheritDoc}
     */
    @Override
    protected void preTileGeneration() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte[] createTile(int z, long x, long y) {

        byte[] bytes = null;

        String zoomUrl = tileUrl;

        // Replace x, y, and z
        if (urlHasXYZ) {
            long yRequest = y;

            // If TMS, flip the y value
            if (tms) {
                yRequest = TileBoundingBoxUtils.getYAsOppositeTileFormat(z,
                        (int) y);
            }

            zoomUrl = replaceXYZ(zoomUrl, z, x, yRequest);
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

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER){
                String redirect = connection.getHeaderField("Location");
                connection.disconnect();
                url = new URL(redirect);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
            }

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
