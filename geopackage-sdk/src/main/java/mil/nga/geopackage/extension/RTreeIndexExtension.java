package mil.nga.geopackage.extension;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageConnection;

/**
 * RTree Index Extension
 * TODO Not currently supported for Android, user defined functions are not supported.
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
     * Constructor
     *
     * @param geoPackage GeoPackage
     */
    public RTreeIndexExtension(GeoPackage geoPackage) {
        super(geoPackage);
        connection = geoPackage.getConnection();
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

}
