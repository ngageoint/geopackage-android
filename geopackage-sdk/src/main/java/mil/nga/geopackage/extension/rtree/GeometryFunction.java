package mil.nga.geopackage.extension.rtree;

import org.sqlite.database.SQLException;
import org.sqlite.database.sqlite.SQLiteDatabase;

import mil.nga.geopackage.geom.GeoPackageGeometryData;

/**
 * Geometry Function for reading Geometry Data from a geometry column blob
 *
 * @author osbornb
 * @since 6.0.0
 */
public abstract class GeometryFunction implements SQLiteDatabase.CustomFunction {

    /**
     * Function name
     */
    private String name;

    /**
     * Constructor
     *
     * @param name function name
     */
    public GeometryFunction(String name) {
        this.name = name;
    }

    /**
     * Get the function name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Execute the function
     *
     * @param geometryData geometry data
     * @return function result
     */
    public abstract Object execute(GeoPackageGeometryData geometryData);

    /**
     * {@inheritDoc}
     */
    @Override
    public void callback(String[] args) {

        int argCount = args.length;
        if (argCount != 1) {
            throw new SQLException(
                    "Single argument is required. args: " + argCount);
        }

        String arg = args[0];
        // TODO Get user defined function argument as pre-decoded blob bytes
        // https://sqlite.org/forum/forumpost/faa2dbfd13

        GeoPackageGeometryData geometryData = null;
        if (arg != null && arg.length() > 0) {
            byte[] bytes = arg.getBytes();
            geometryData = GeoPackageGeometryData.create(bytes);
        }

        Object response = execute(geometryData);

        // TODO User defined functions that return values are not currently supported for Android
        // https://sqlite.org/forum/forumpost/faa2dbfd13
        // https://sqlite.org/android/file?name=sqlite3/src/main/jni/sqlite/android_database_SQLiteConnection.cpp&ln=250
        // "TODO: Support functions that return values."

        if (response == null) {
            //result();
        } else if (response instanceof Double) {
            //result((Double) response);
        } else if (response instanceof Integer) {
            //result((Integer) response);
        } else {
            throw new SQLException("Unexpected response value: " + response
                    + ", of type: " + response.getClass().getSimpleName());
        }

    }

}
