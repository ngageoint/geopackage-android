package mil.nga.geopackage.user.custom;

import mil.nga.geopackage.user.UserRowSync;

/**
 * User Custom Row Sync to support reading a single user mapping row copy when
 * multiple near simultaneous asynchronous requests are made
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomRowSync extends
        UserRowSync<UserCustomColumn, UserCustomTable, UserCustomRow> {

    /**
     * Constructor
     */
    public UserCustomRowSync() {

    }

}
