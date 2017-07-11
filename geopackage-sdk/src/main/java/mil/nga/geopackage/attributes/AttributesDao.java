package mil.nga.geopackage.attributes;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserDao;

/**
 * Attributes DAO for reading attributes user data tables
 *
 * @author osbornb
 * @since 1.3.1
 */
public class AttributesDao
        extends
        UserDao<AttributesColumn, AttributesTable, AttributesRow, AttributesCursor> {

    /**
     * Attributes connection
     */
    private final AttributesConnection attributesDb;

    /**
     * Constructor
     *
     * @param database     database
     * @param db           db connection
     * @param attributesDb attributes connection
     * @param table        attributes table
     */
    public AttributesDao(String database, GeoPackageConnection db,
                         AttributesConnection attributesDb, AttributesTable table) {
        super(database, db, attributesDb, table);

        this.attributesDb = attributesDb;
        if (table.getContents() == null) {
            throw new GeoPackageException(AttributesTable.class.getSimpleName()
                    + " " + table.getTableName() + " has null "
                    + Contents.class.getSimpleName());
        }
    }

    /**
     * {@inheritDoc}
     *
     * Not supported for Attributes
     */
    @Override
    public BoundingBox getBoundingBox() {
        throw new GeoPackageException(
                "Bounding Box not supported for Attributes");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesRow newRow() {
        return new AttributesRow(getTable());
    }

    /**
     * Get the Attributes connection
     *
     * @return attributes connection
     */
    public AttributesConnection getAttributesDb() {
        return attributesDb;
    }

}
