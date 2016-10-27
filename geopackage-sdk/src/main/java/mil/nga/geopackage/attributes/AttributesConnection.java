package mil.nga.geopackage.attributes;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage Attributes Connection
 *
 * @author osbornb
 * @since 1.3.1
 */
public class AttributesConnection
        extends
        UserConnection<AttributesColumn, AttributesTable, AttributesRow, AttributesCursor> {

    /**
     * Constructor
     *
     * @param database
     */
    public AttributesConnection(GeoPackageConnection database) {
        super(database);
    }

}