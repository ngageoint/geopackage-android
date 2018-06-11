package mil.nga.geopackage.user.custom;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage User Custom Connection
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomConnection
        extends
        UserConnection<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomCursor> {

    /**
     * Constructor
     *
     * @param database
     */
    public UserCustomConnection(GeoPackageConnection database) {
        super(database);
    }

}
