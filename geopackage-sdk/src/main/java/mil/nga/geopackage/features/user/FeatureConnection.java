package mil.nga.geopackage.features.user;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage Feature Connection
 *
 * @author osbornb
 */
public class FeatureConnection
        extends
        UserConnection<FeatureColumn, FeatureTable, FeatureRow, FeatureCursor> {

    /**
     * Constructor
     *
     * @param database
     */
    public FeatureConnection(GeoPackageConnection database) {
        super(database);
    }

}
