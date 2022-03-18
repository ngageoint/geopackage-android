package mil.nga.geopackage.attributes;

import android.database.Cursor;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserWrapperConnection;

/**
 * GeoPackage Attributes Cursor Wrapper Connection
 *
 * @author osbornb
 * @since 1.3.1
 * @deprecated use {@link AttributesDao} to query attributes tables
 */
public class AttributesWrapperConnection
        extends
        UserWrapperConnection<AttributesColumn, AttributesTable, AttributesRow, AttributesCursor> {

    /**
     * Constructor
     *
     * @param database GeoPackage connection
     */
    public AttributesWrapperConnection(GeoPackageConnection database) {
        super(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttributesCursor wrapCursor(Cursor cursor) {
        return new AttributesCursor(null, cursor);
    }
}
