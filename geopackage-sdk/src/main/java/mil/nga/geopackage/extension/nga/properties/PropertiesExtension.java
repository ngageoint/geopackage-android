package mil.nga.geopackage.extension.nga.properties;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;

/**
 * GeoPackage properties extension for defining GeoPackage specific properties,
 * attributes, and metadata
 * <p>
 * <a href="http://ngageoint.github.io/GeoPackage/docs/extensions/properties.html">http://ngageoint.github.io/GeoPackage/docs/extensions/properties.html</a>
 *
 * @author osbornb
 * @since 3.0.2
 */
public class PropertiesExtension
        extends
        PropertiesCoreExtension<GeoPackage, AttributesRow, AttributesCursor, AttributesDao> {

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     */
    public PropertiesExtension(GeoPackage geoPackage) {
        super(geoPackage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttributesDao getDao() {
        return getGeoPackage().getAttributesDao(TABLE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttributesRow newRow() {
        return getDao().newRow();
    }

}
