package mil.nga.geopackage.extension.rtree;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageDatabase;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomTable;
import mil.nga.sf.GeometryEnvelope;

/**
 * RTree Index Extension
 * TODO User defined functions that return values are not currently supported for Android
 *
 * https://www.geopackage.org/spec/#extension_rtree
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
     * SQLite Android connection
     */
    private final GeoPackageDatabase database;

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     */
    public RTreeIndexExtension(GeoPackage geoPackage) {
        super(geoPackage);
        connection = geoPackage.getConnection();
        database = connection.getDb().copy();
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

        UserCustomTable userCustomTable = getRTreeTable(featureDao.getTable());
        UserCustomDao userCustomDao = getGeoPackage().getUserCustomDao(userCustomTable);

        return new RTreeIndexTableDao(this, userCustomDao, featureDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMinXFunction() {
        createFunction(new GeometryFunction(MIN_X_FUNCTION) {
            @Override
            public Object execute(GeoPackageGeometryData data) {
                Object value = null;
                GeometryEnvelope envelope = getEnvelope(data);
                if (envelope != null) {
                    value = envelope.getMinX();
                }
                return value;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMaxXFunction() {
        createFunction(new GeometryFunction(MAX_X_FUNCTION) {
            @Override
            public Object execute(GeoPackageGeometryData data) {
                Object value = null;
                GeometryEnvelope envelope = getEnvelope(data);
                if (envelope != null) {
                    value = envelope.getMaxX();
                }
                return value;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMinYFunction() {
        createFunction(new GeometryFunction(MIN_Y_FUNCTION) {
            @Override
            public Object execute(GeoPackageGeometryData data) {
                Object value = null;
                GeometryEnvelope envelope = getEnvelope(data);
                if (envelope != null) {
                    value = envelope.getMinY();
                }
                return value;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMaxYFunction() {
        createFunction(new GeometryFunction(MAX_Y_FUNCTION) {
            @Override
            public Object execute(GeoPackageGeometryData data) {
                Object value = null;
                GeometryEnvelope envelope = getEnvelope(data);
                if (envelope != null) {
                    value = envelope.getMaxY();
                }
                return value;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createIsEmptyFunction() {
        createFunction(new GeometryFunction(IS_EMPTY_FUNCTION) {
            @Override
            public Object execute(GeoPackageGeometryData data) {
                Object value = null;
                if (data != null) {
                    if (data.isEmpty() || data.getGeometry() == null) {
                        value = 1;
                    } else {
                        value = 0;
                    }
                }
                return value;
            }
        });
    }

    /**
     * Create the function for the connection
     *
     * @param function geometry function
     */
    private void createFunction(GeometryFunction function) {
        // TODO User defined functions that return values are not currently supported for Android
        if (true) {
            throw new UnsupportedOperationException(
                    "User defined SQL functions that return values are not supported. name: "
                            + function.getName());
        }
        database.getBindingsDb()
                .addCustomFunction(function.getName(), 1, function);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeSQL(String sql, boolean trigger) {
        database.setUseBindings(!trigger);
        database.execSQL(sql);
    }

}
