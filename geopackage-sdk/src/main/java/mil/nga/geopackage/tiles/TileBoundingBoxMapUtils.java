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
                boundingBox.getMaxLongitude());
    }

    /**
     * Get the longitude distance in the middle latitude
     *
     * @param minLongitude
     * @param maxLongitude
     * @return
     */
    public static double getLongitudeDistance(double minLongitude,
                                              double maxLongitude) {
        LatLng leftMiddle = new LatLng(0, minLongitude);
        LatLng middle = new LatLng(0, maxLongitude - minLongitude);
        LatLng rightMiddle = new LatLng(0, maxLongitude);

        List<LatLng> path = new ArrayList<LatLng>();
        path.add(leftMiddle);
        path.add(middle);
        path.add(rightMiddle);

        double lonDistance = SphericalUtil.computeLength(path);
        return lonDistance;
    }

    /**
     * Get the latitude distance in the middle longitude
     *
     * @param boundingBox
     * @return
     */
    public static double getLatitudeDistance(BoundingBox boundingBox) {
        return getLatitudeDistance(boundingBox.getMinLatitude(),
                boundingBox.getMaxLatitude());
    }

    /**
     * Get the latitude distance in the middle longitude
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
