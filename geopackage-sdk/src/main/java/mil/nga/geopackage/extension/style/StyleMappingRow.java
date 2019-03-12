package mil.nga.geopackage.extension.style;

import mil.nga.geopackage.extension.related.UserMappingRow;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomRow;
import mil.nga.sf.GeometryType;

/**
 * Style Mapping Row containing the values from a single result set row
 *
 * @author osbornb
 * @since 3.2.0
 */
public class StyleMappingRow extends UserMappingRow {

    /**
     * Constructor to create an empty row
     *
     * @param table style mapping table
     */
    protected StyleMappingRow(StyleMappingTable table) {
        super(table);
    }

    /**
     * Constructor
     *
     * @param userCustomRow user custom row
     */
    public StyleMappingRow(UserCustomRow userCustomRow) {
        super(userCustomRow);
    }

    /**
     * Copy Constructor
     *
     * @param styleMappingRow style mapping row to copy
     */
    public StyleMappingRow(StyleMappingRow styleMappingRow) {
        super(styleMappingRow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StyleMappingTable getTable() {
        return (StyleMappingTable) super.getTable();
    }

    /**
     * Get the geometry type name column index
     *
     * @return geometry type name column index
     */
    public int getGeometryTypeNameColumnIndex() {
        return getTable().getGeometryTypeNameColumnIndex();
    }

    /**
     * Get the geometry type name column
     *
     * @return geometry type name column
     */
    public UserCustomColumn getGeometryTypeNameColumn() {
        return getTable().getGeometryTypeNameColumn();
    }

    /**
     * Get the geometry type name
     *
     * @return geometry type name
     */
    public String getGeometryTypeName() {
        return getValueString(getGeometryTypeNameColumnIndex());
    }

    /**
     * Get the geometry type
     *
     * @return geometry type
     */
    public GeometryType getGeometryType() {
        GeometryType geometryType = null;
        String geometryTypeName = getGeometryTypeName();
        if (geometryTypeName != null) {
            geometryType = GeometryType.fromName(geometryTypeName);
        }
        return geometryType;
    }

    /**
     * Set the geometry type
     *
     * @param geometryType geometry type
     */
    public void setGeometryType(GeometryType geometryType) {
        String geometryTypeName = null;
        if (geometryType != null) {
            geometryTypeName = geometryType.getName();
        }
        setValue(getGeometryTypeNameColumnIndex(), geometryTypeName);
    }

    /**
     * Copy the row
     *
     * @return row copy
     */
    public StyleMappingRow copy() {
        return new StyleMappingRow(this);
    }

}
