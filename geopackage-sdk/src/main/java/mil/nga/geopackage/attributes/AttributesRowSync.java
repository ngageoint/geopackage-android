package mil.nga.geopackage.attributes;

import mil.nga.geopackage.user.UserRowSync;

/**
 * Attributes Row Sync to support reading a single attributes row copy when multiple
 * near simultaneous asynchronous requests are made
 *
 * @author osbornb
 * @since 2.0.0
 */
public class AttributesRowSync extends UserRowSync<AttributesColumn, AttributesTable, AttributesRow> {

    /**
     * Constructor
     */
    public AttributesRowSync() {

    }

}
