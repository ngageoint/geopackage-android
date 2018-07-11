package mil.nga.geopackage.user.custom;

import android.database.Cursor;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserWrapperConnection;

/**
 * GeoPackage User Custom Cursor Wrapper Connection
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomWrapperConnection
        extends
        UserWrapperConnection<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomCursor> {

    /**
     * Constructor
     *
     * @param database GeoPackage connection
     */
    public UserCustomWrapperConnection(GeoPackageConnection database) {
        super(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserCustomCursor wrapCursor(Cursor cursor) {
        return new UserCustomCursor(null, cursor);
    }

}
