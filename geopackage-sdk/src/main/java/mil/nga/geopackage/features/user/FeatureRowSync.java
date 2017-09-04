package mil.nga.geopackage.features.user;

import mil.nga.geopackage.user.UserRowSync;

/**
 * Feature Row Sync to support sharing a single feature row read copy when multiple
 * near simultaneous asynchronous requests are made
 *
 * @author osbornb
 * @since 1.4.2
 */
public class FeatureRowSync extends UserRowSync<FeatureColumn, FeatureTable, FeatureRow> {

    /**
     * Constructor
     */
    public FeatureRowSync() {

    }

}
