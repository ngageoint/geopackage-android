package mil.nga.geopackage.attributes;

import java.util.List;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.user.UserTableReader;

/**
 * Reads the metadata from an existing attributes table
 *
 * @author osbornb
 * @since 1.3.1
 */
public class AttributesTableReader
        extends
        UserTableReader<AttributesColumn, AttributesTable, AttributesRow, AttributesCursor> {

    /**
     * Constructor
     *
     * @param tableName table name
     */
    public AttributesTableReader(String tableName) {
        super(tableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttributesTable createTable(String tableName,
                                          List<AttributesColumn> columnList) {
        return new AttributesTable(tableName, columnList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttributesColumn createColumn(AttributesCursor cursor,
                                            int index, String name, String type, Long max, boolean notNull,
                                            int defaultValueIndex, boolean primaryKey) {

        GeoPackageDataType dataType = GeoPackageDataType.fromName(type);

        Object defaultValue = cursor.getValue(defaultValueIndex, dataType);

        AttributesColumn column = new AttributesColumn(index, name, dataType,
                max, notNull, defaultValue, primaryKey);

        return column;
    }

}
