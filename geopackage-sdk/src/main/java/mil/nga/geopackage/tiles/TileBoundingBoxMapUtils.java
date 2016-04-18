package mil.nga.geopackage.tiles;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;

/**
 * Tile Bounding Box utility methods relying on Android map libraries
 *
 * @author osbornb
 * @since 1.2.0
 */
public class TileBoundingBoxMapUtils {

    /**
     * Get the longitude distance in the middle latitude
     *
     * @param boundingBox
     * @return
     */
    public static double getLongitudeDistance(BoundingBox boundingBox) {
        return getLongitudeDistance(boundingBox.getMinLongitude(),
                boundingBox.getMaxLongitude(),
                (boundingBox.getMinLatitude() + boundingBox.getMaxLatitude()) / 2.0);
    }

    /**
     * Get the longitude distance in the middle latitude
     *
     * @param minLongitude min longitude
     * @param maxLongitude max longitude
     * @return distance
     */
    public static double getLongitudeDistance(double minLongitude,
                                              double maxLongitude) {
        return getLongitudeDistance(minLongitude, maxLongitude, 0);
    }

    /**
     * Get the longitude distance in the middle latitude
     *
     * @param minLongitude min longitude
     * @param maxLongitude max longitude
     * @param latitude     latitude
     * @return distance
     * @since 1.2.7
     */
    public static double getLongitudeDistance(double minLongitude,
                                              double maxLongitude,
                                              double latitude) {
        LatLng leftMiddle = new LatLng(latitude, minLongitude);
        LatLng middle = new LatLng(latitude, (minLongitude + maxLongitude) / 2.0);
        LatLng rightMiddle = new LatLng(latitude, maxLongitude);

        List<LatLng> path = new ArrayList<LatLng>();
        path.add(leftMiddle);
        path.add(middle);
        path.add(rightMiddle);

        double lonDistance = SphericalUtil.computeLength(path);
        return lonDistance;
    }

    /**
     * Get the latitude distance
     *
     * @param boundingBox
     * @return
     */
    public static double getLatitudeDistance(BoundingBox boundingBox) {
        return getLatitudeDistance(boundingBox.getMinLatitude(),
                boundingBox.getMaxLatitude());
    }

    /**
     * Get the latitude distance
     *
     * @param minLatitude
     * @param maxLatitude
     * @return
     */
    public static double getLatitudeDistance(double minLatitude,
                                             double maxLatitude) {
        LatLng lowerMiddle = new LatLng(minLatitude, 0);
        LatLng upperMiddle = new LatLng(maxLatitude, 0);
        double latDistance = SphericalUtil.computeDistanceBetween(lowerMiddle,
                upperMiddle);
        return latDistance;
    }

}
