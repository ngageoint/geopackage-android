package mil.nga.geopackage.attributes;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserDao;
import mil.nga.proj.Projection;

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
     * @param database database
     * @param db       db connection
     * @param table    attributes table
     */
    public AttributesDao(String database, GeoPackageConnection db,
                         AttributesTable table) {
        super(database, db, new AttributesConnection(db), table);

        this.attributesDb = (AttributesConnection) getUserDb();
        if (table.getContents() == null) {
            throw new GeoPackageException(AttributesTable.class.getSimpleName()
                    + " " + table.getTableName() + " has null "
                    + Contents.class.getSimpleName());
        }
    }

    /**
     * {@inheritDoc}
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
    public BoundingBox getBoundingBox(Projection projection) {
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
