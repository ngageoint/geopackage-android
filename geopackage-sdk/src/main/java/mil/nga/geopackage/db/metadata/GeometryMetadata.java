package mil.nga.geopackage.db.metadata;

/**
 * Geometry metadata used to index feature bounds
 *
 * @author osbornb
 */
public class GeometryMetadata {

    /**
     * Table name
     */
    public static final String TABLE_NAME = "geom_metadata";

    /**
     * GeoPackage Id column
     */
    public static final String COLUMN_GEOPACKAGE_ID = TableMetadata.COLUMN_GEOPACKAGE_ID;

    /**
     * Table name column
     */
    public static final String COLUMN_TABLE_NAME = TableMetadata.COLUMN_TABLE_NAME;

    /**
     * Geometry Id column
     */
    public static final String COLUMN_ID = "geom_id";

    /**
     * Min X
     */
    public static final String COLUMN_MIN_X = "min_x";

    /**
     * Max X
     */
    public static final String COLUMN_MAX_X = "max_x";

    /**
     * Min Y
     */
    public static final String COLUMN_MIN_Y = "min_y";

    /**
     * Max Y
     */
    public static final String COLUMN_MAX_Y = "max_y";

    /**
     * Min Z
     */
    public static final String COLUMN_MIN_Z = "min_z";

    /**
     * Max Z
     */
    public static final String COLUMN_MAX_Z = "max_z";

    /**
     * Min M
     */
    public static final String COLUMN_MIN_M = "min_m";

    /**
     * Max M
     */
    public static final String COLUMN_MAX_M = "max_m";

    /**
     * Columns
     */
    public static final String[] COLUMNS = {
            COLUMN_GEOPACKAGE_ID,
            COLUMN_TABLE_NAME,
            COLUMN_ID,
            COLUMN_MIN_X,
            COLUMN_MAX_X,
            COLUMN_MIN_Y,
            COLUMN_MAX_Y,
            COLUMN_MIN_Z,
            COLUMN_MAX_Z,
            COLUMN_MIN_M,
            COLUMN_MAX_M};

    /**
     * Create table SQL
     */
    public static final String CREATE_SQL = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + COLUMN_GEOPACKAGE_ID + " INTEGER NOT NULL, "
            + COLUMN_TABLE_NAME + " TEXT NOT NULL, "
            + COLUMN_ID + " INTEGER NOT NULL, "
            + COLUMN_MIN_X + " DOUBLE NOT NULL, "
            + COLUMN_MAX_X + " DOUBLE NOT NULL, "
            + COLUMN_MIN_Y + " DOUBLE NOT NULL, "
            + COLUMN_MAX_Y + " DOUBLE NOT NULL, "
            + COLUMN_MIN_Z + " DOUBLE, "
            + COLUMN_MAX_Z + " DOUBLE, "
            + COLUMN_MIN_M + " DOUBLE, "
            + COLUMN_MAX_M + " DOUBLE, "
            + "CONSTRAINT pk_geom_metadata PRIMARY KEY (" + COLUMN_GEOPACKAGE_ID + ", " + COLUMN_TABLE_NAME + ", " + COLUMN_ID + "), "
            + "CONSTRAINT fk_gm_tm_gp FOREIGN KEY (" + COLUMN_GEOPACKAGE_ID + ") REFERENCES " + TableMetadata.TABLE_NAME + "(" + TableMetadata.COLUMN_GEOPACKAGE_ID + "), "
            + "CONSTRAINT fk_gm_tm FOREIGN KEY (" + COLUMN_TABLE_NAME + ") REFERENCES " + TableMetadata.TABLE_NAME + "(" + TableMetadata.COLUMN_TABLE_NAME + ")"
            + ");";

    /**
     * GeoPackage Id
     */
    public long geoPackageId;

    /**
     * GeoPackage table name
     */
    public String tableName;

    /**
     * Geometry id, "foreign key" to a user table
     */
    public long id;

    /**
     * Min X
     */
    public double minX;

    /**
     * Max X
     */
    public double maxX;

    /**
     * Min Y
     */
    public double minY;

    /**
     * Max Y
     */
    public double maxY;

    /**
     * Min Z
     */
    public Double minZ;

    /**
     * Max Z
     */
    public Double maxZ;

    /**
     * Min M
     */
    public Double minM;

    /**
     * Max M
     */
    public Double maxM;

    /**
     * Get the GeoPackage id
     *
     * @return GeoPackage id
     */
    public long getGeoPackageId() {
        return geoPackageId;
    }

    /**
     * Set the GeoPackage id
     *
     * @param geoPackageId GeoPackage id
     */
    public void setGeoPackageId(long geoPackageId) {
        this.geoPackageId = geoPackageId;
    }

    /**
     * Get the table name
     *
     * @return table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Set the table name
     *
     * @param tableName table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Get the id
     *
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * Set the id
     *
     * @param id id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get min X
     *
     * @return min x
     */
    public double getMinX() {
        return minX;
    }

    /**
     * Set min X
     *
     * @param minX min x
     */
    public void setMinX(double minX) {
        this.minX = minX;
    }

    /**
     * Get max X
     *
     * @return max x
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * Set max X
     *
     * @param maxX max x
     */
    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    /**
     * Get min Y
     *
     * @return min y
     */
    public double getMinY() {
        return minY;
    }

    /**
     * Set min Y
     *
     * @param minY min y
     */
    public void setMinY(double minY) {
        this.minY = minY;
    }

    /**
     * Get max Y
     *
     * @return max y
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * Set max Y
     *
     * @param maxY max y
     */
    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    /**
     * Get min Z
     *
     * @return min z
     */
    public Double getMinZ() {
        return minZ;
    }

    /**
     * Set min Z
     *
     * @param minZ min z
     */
    public void setMinZ(Double minZ) {
        this.minZ = minZ;
    }

    /**
     * Get max Z
     *
     * @return max z
     */
    public Double getMaxZ() {
        return maxZ;
    }

    /**
     * Set max Z
     *
     * @param maxZ max z
     */
    public void setMaxZ(Double maxZ) {
        this.maxZ = maxZ;
    }

    /**
     * Get min M
     *
     * @return min m
     */
    public Double getMinM() {
        return minM;
    }

    /**
     * Set min M
     *
     * @param minM min m
     */
    public void setMinM(Double minM) {
        this.minM = minM;
    }

    /**
     * Get max M
     *
     * @return max m
     */
    public Double getMaxM() {
        return maxM;
    }

    /**
     * Set max M
     *
     * @param maxM max m
     */
    public void setMaxM(Double maxM) {
        this.maxM = maxM;
    }

}
