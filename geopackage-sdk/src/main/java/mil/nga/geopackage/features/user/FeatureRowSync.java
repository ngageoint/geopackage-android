package mil.nga.geopackage.features.user;

import mil.nga.geopackage.user.UserRowSync;

/**
 * Feature Row Sync to support reading a single feature row copy when multiple
 * near simultaneous asynchronous requests are made
 *
 * @author osbornb
 * @since 2.0.0
 */
public class FeatureRowSync extends UserRowSync<FeatureColumn, FeatureTable, FeatureRow> {

    /**
     * Constructor
     */
    public FeatureRowSync() {

    }

}
