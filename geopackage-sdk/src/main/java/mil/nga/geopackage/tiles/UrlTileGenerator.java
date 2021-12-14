package mil.nga.geopackage.tiles;

import android.content.Context;

import org.locationtech.proj4j.units.Units;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.R;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.proj.Projection;

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
     * HTTP request method, when null default is "GET"
     */
    private String httpMethod;

    /**
     * HTTP Header fields and field values
     */
    private Map<String, List<String>> httpHeader;

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
     * Get the HTTP request method, when null default is "GET"
     *
     * @return method
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Set the HTTP request method
     *
     * @param httpMethod method ("GET", "POST")
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * Get the HTTP Header fields and field values
     *
     * @return header map
     */
    public Map<String, List<String>> getHttpHeader() {
        return httpHeader;
    }

    /**
     * Get the HTTP Header field values
     *
     * @param field field name
     * @return field values
     */
    public List<String> getHttpHeaderValues(String field) {
        List<String> fieldValues = null;
        if (httpHeader != null) {
            fieldValues = httpHeader.get(field);
        }
        return fieldValues;
    }

    /**
     * Add a HTTP Header field value, appending to any existing values for the
     * field
     *
     * @param field field name
     * @param value field value
     */
    public void addHTTPHeaderValue(String field, String value) {
        if (httpHeader == null) {
            httpHeader = new HashMap<>();
        }
        List<String> values = httpHeader.get(field);
        if (values == null) {
            values = new ArrayList<>();
            httpHeader.put(field, values);
        }
        values.add(value);
    }

    /**
     * Add HTTP Header field values, appending to any existing values for the
     * field
     *
     * @param field  field name
     * @param values field values
     */
    public void addHTTPHeaderValues(String field, List<String> values) {
        for (String value : values) {
            addHTTPHeaderValue(field, value);
        }
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

        BoundingBox boundingBox = null;

        if (projection.isUnit(Units.DEGREES)) {
            boundingBox = TileBoundingBoxUtils
                    .getProjectedBoundingBoxFromWGS84(projection, x, y, z);
        } else {
            boundingBox = TileBoundingBoxUtils
                    .getProjectedBoundingBox(projection, x, y, z);
        }

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
            configureRequest(connection);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                String redirect = connection.getHeaderField("Location");
                connection.disconnect();
                url = new URL(redirect);
                connection = (HttpURLConnection) url.openConnection();
                configureRequest(connection);
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

    /**
     * Configure the connection HTTP method and header
     *
     * @param connection HTTP URL connection
     * @throws ProtocolException upon configuration failure
     */
    private void configureRequest(HttpURLConnection connection)
            throws ProtocolException {

        if (httpMethod != null) {
            connection.setRequestMethod(httpMethod);
        }

        if (httpHeader != null) {
            for (Map.Entry<String, List<String>> fieldEntry : httpHeader
                    .entrySet()) {
                String field = fieldEntry.getKey();
                List<String> values = fieldEntry.getValue();
                if (values != null) {
                    for (String value : values) {
                        connection.addRequestProperty(field, value);
                    }
                }
            }
        }

    }

}
