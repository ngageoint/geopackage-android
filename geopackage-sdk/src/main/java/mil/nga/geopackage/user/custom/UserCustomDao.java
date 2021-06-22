package mil.nga.geopackage.user.custom;

import android.database.Cursor;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageCursorWrapper;
import mil.nga.geopackage.user.UserDao;
import mil.nga.proj.Projection;

/**
 * User Custom DAO for reading user custom data tables
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomDao
        extends
        UserDao<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomCursor> {

    /**
     * User Custom connection
     */
    protected final UserCustomConnection userDb;

    /**
     * Constructor
     *
     * @param database database name
     * @param db       database connection
     * @param table    user custom table
     */
    public UserCustomDao(String database, GeoPackageConnection db,
                         UserCustomTable table) {
        super(database, db, new UserCustomConnection(db), table);

        this.userDb = (UserCustomConnection) getUserDb();
    }

    /**
     * Constructor
     *
     * @param dao user custom data access object
     */
    public UserCustomDao(UserCustomDao dao) {
        this(dao, dao.getTable());
    }

    /**
     * Constructor
     *
     * @param dao             user custom data access object
     * @param userCustomTable user custom table
     */
    public UserCustomDao(UserCustomDao dao, UserCustomTable userCustomTable) {
        this(dao.getDatabase(), dao.getDb(), userCustomTable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingBox getBoundingBox() {
        throw new GeoPackageException(
                "Bounding Box not supported for User Custom");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingBox getBoundingBox(Projection projection) {
        throw new GeoPackageException(
                "Bounding Box not supported for User Custom");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserCustomRow newRow() {
        return new UserCustomRow(getTable());
    }

    /**
     * Get the User Custom connection
     *
     * @return user custom connection
     */
    public UserCustomConnection getUserDb() {
        return userDb;
    }

    /**
     * Get the count of the cursor and close it
     *
     * @param cursor cursor
     * @return count
     */
    protected int count(UserCustomCursor cursor) {
        int count = 0;
        try {
            count = cursor.getCount();
        } finally {
            cursor.close();
        }
        return count;
    }

    /**
     * Register the cursor wrapper into the GeoPackage
     *
     * @param geoPackage GeoPackage
     */
    public void registerCursorWrapper(GeoPackage geoPackage) {
        geoPackage.registerCursorWrapper(getTableName(), new GeoPackageCursorWrapper() {
            @Override
            public Cursor wrapCursor(Cursor cursor) {
                return new UserCustomCursor(getTable(), cursor);
            }
        });
    }

    /**
     * Read the database table and create a DAO
     *
     * @param geoPackage GeoPackage
     * @param tableName  table name
     * @return user custom DAO
     */
    public static UserCustomDao readTable(GeoPackage geoPackage, String tableName) {

        UserCustomTable userCustomTable = UserCustomTableReader.readTable(
                geoPackage.getConnection(), tableName);
        UserCustomDao dao = new UserCustomDao(geoPackage.getName(), geoPackage.getConnection(),
                userCustomTable);

        dao.registerCursorWrapper(geoPackage);

        return dao;
    }

}
