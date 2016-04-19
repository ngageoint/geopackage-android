package mil.nga.geopackage.db.metadata;

/**
 * GeoPackage metadata
 *
 * @author osbornb
 */
public class GeoPackageMetadata {

    /**
     * Table name
     */
    public static final String TABLE_NAME = "geopackage";

    /**
     * Id column
     */
    public static final String COLUMN_ID = "geopackage_id";

    /**
     * Name column
     */
    public static final String COLUMN_NAME = "name";

    /**
     * Path column
     */
    public static final String COLUMN_EXTERNAL_PATH = "external_path";

    /**
     * Columns
     */
    public static final String[] COLUMNS = {
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_EXTERNAL_PATH};

    /**
     * Create table SQL
     */
    public static final String CREATE_SQL = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL UNIQUE, "
            + COLUMN_EXTERNAL_PATH + " TEXT"
            + ");";

    /**
     * Id
     */
    public long id;

    /**
     * Name
     */
    public String name;

    /**
     * External path when not located in the app space
     */
    public String externalPath;

    /**
     * Get the id
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * Set the id
     *
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get the name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the external path
     *
     * @return
     */
    public String getExternalPath() {
        return externalPath;
    }

    /**
     * Set the external path
     *
     * @param externalPath
     */
    public void setExternalPath(String externalPath) {
        this.externalPath = externalPath;
    }

    /**
     * Is the GeoPackage external
     *
     * @return
     */
    public boolean isExternal() {
        return externalPath != null;
    }

}
