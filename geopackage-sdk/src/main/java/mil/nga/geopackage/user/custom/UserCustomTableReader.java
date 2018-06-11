package mil.nga.geopackage.user.custom;

import java.util.List;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.user.UserTableReader;

/**
 * Reads the metadata from an existing user custom table
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomTableReader
        extends
        UserTableReader<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomCursor> {

    /**
     * Constructor
     *
     * @param tableName table name
     */
    public UserCustomTableReader(String tableName) {
        super(tableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserCustomTable createTable(String tableName,
                                          List<UserCustomColumn> columnList) {
        return new UserCustomTable(tableName, columnList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserCustomColumn createColumn(UserCustomCursor result,
                                            int index, String name, String type, Long max, boolean notNull,
                                            int defaultValueIndex, boolean primaryKey) {

        GeoPackageDataType dataType = getDataType(type);

        Object defaultValue = result.getValue(defaultValueIndex, dataType);

        UserCustomColumn column = new UserCustomColumn(index, name, dataType,
                max, notNull, defaultValue, primaryKey);

        return column;
    }

    /**
     * Read the table
     *
     * @param connection GeoPackage connection
     * @param tableName  table name
     * @return table
     */
    public static UserCustomTable readTable(GeoPackageConnection connection,
                                            String tableName) {
        UserCustomTableReader tableReader = new UserCustomTableReader(tableName);
        UserCustomTable customTable = tableReader.readTable(new UserCustomWrapperConnection(connection));
        return customTable;
    }

}
