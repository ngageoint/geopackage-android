package mil.nga.geopackage.extension;

import org.sqlite.database.sqlite.SQLiteDatabase;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomTable;

/**
 * RTree Index Extension
 * TODO User defined functions are not currently supported for Android
 *
 * @author osbornb
 * @since 2.0.1
 */
public class RTreeIndexExtension extends RTreeIndexCoreExtension {

    /**
     * GeoPackage connection
     */
    private GeoPackageConnection connection;

    /**
     * SQLite Android Bindings connection
     */
    private final SQLiteDatabase database;

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     */
    public RTreeIndexExtension(GeoPackage geoPackage) {
        super(geoPackage);
        connection = geoPackage.getConnection();
        database = connection.getDb().openOrGetBindingsDb();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoPackage getGeoPackage() {
        return (GeoPackage) super.getGeoPackage();
    }

    /**
     * Get a RTree Index Table DAO for the feature table
     *
     * @param featureTable feature table
     * @return RTree Index Table DAO
     * @since 3.1.0
     */
    public RTreeIndexTableDao getTableDao(String featureTable) {
        return getTableDao(getGeoPackage().getFeatureDao(featureTable));
    }

    /**
     * Get a RTree Index Table DAO for the feature dao
     *
     * @param featureDao feature DAO
     * @return RTree Index Table DAO
     * @since 3.1.0
     */
    public RTreeIndexTableDao getTableDao(FeatureDao featureDao) {

        GeoPackageConnection connection = getGeoPackage().getConnection();
        UserCustomTable userCustomTable = getRTreeTable(featureDao.getTable());
        UserCustomDao userCustomDao = new UserCustomDao(geoPackage.getName(),
                connection, userCustomTable);

        return new RTreeIndexTableDao(this, userCustomDao, featureDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMinXFunction() {
        createFunction(MIN_X_FUNCTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMaxXFunction() {
        createFunction(MAX_X_FUNCTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMinYFunction() {
        createFunction(MIN_Y_FUNCTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMaxYFunction() {
        createFunction(MAX_Y_FUNCTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createIsEmptyFunction() {
        createFunction(IS_EMPTY_FUNCTION);
    }

    /**
     * Create the function for the connection
     *
     * @param name function name
     */
    private void createFunction(String name) {
        throw new UnsupportedOperationException("User defined SQL functions are not supported. name: " + name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeSQL(String sql, boolean trigger) {
        if (trigger) {
            connection.execSQL(sql);
        } else {
            database.execSQL(sql);
        }
    }

}
