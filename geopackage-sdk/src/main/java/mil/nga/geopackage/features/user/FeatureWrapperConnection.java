package mil.nga.geopackage.features.user;

import android.database.Cursor;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserWrapperConnection;

/**
 * GeoPackage Feature Cursor Wrapper Connection
 *
 * @author osbornb
 */
public class FeatureWrapperConnection
        extends
        UserWrapperConnection<FeatureColumn, FeatureTable, FeatureRow, FeatureCursor> {

    /**
     * Constructor
     *
     * @param database
     */
    public FeatureWrapperConnection(GeoPackageConnection database) {
        super(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FeatureCursor wrapCursor(Cursor cursor) {
        return new FeatureCursor(null, cursor);
    }
}
