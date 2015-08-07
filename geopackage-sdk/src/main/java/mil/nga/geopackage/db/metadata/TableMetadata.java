package mil.nga.geopackage.db.metadata;

/**
 * Metadata about tables within a GeoPackage
 *
 * @author osbornb
 */
public class TableMetadata {

    /**
     * Table name
     */
    public static final String TABLE_NAME = "geopackage_table";

    /**
     * GeoPackage Id column
     */
    public static final String COLUMN_GEOPACKAGE_ID = GeoPackageMetadata.COLUMN_ID;

    /**
     * Table name column
     */
    public static final String COLUMN_TABLE_NAME = "table_name";

    /**
     * Last indexed column
     */
    public static final String COLUMN_LAST_INDEXED = "last_indexed";

    /**
     * Columns
     */
    public static final String[] COLUMNS = {
            COLUMN_GEOPACKAGE_ID,
            COLUMN_TABLE_NAME,
            COLUMN_LAST_INDEXED};

    /**
     * Create table SQL
     */
    public static final String CREATE_SQL = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + COLUMN_GEOPACKAGE_ID + " INTEGER NOT NULL, "
            + COLUMN_TABLE_NAME + " TEXT NOT NULL, "
            + COLUMN_LAST_INDEXED + " INTEGER, "
            + "CONSTRAINT pk_table_metadata PRIMARY KEY (" + COLUMN_GEOPACKAGE_ID + ", " + COLUMN_TABLE_NAME + "), "
            + "CONSTRAINT fk_tm_gp FOREIGN KEY (" + COLUMN_GEOPACKAGE_ID + ") REFERENCES " + GeoPackageMetadata.TABLE_NAME + "(" + GeoPackageMetadata.COLUMN_ID + ")"
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
     * Time in milliseconds since epoch when the table was last indexed
     */
    public Long lastIndexed;

    /**
     * Get the GeoPackage id
     *
     * @return
     */
    public long getGeoPackageId() {
        return geoPackageId;
    }

    /**
     * Set the GeoPackage id
     *
     * @param geoPackageId
     */
    public void setGeoPackageId(long geoPackageId) {
        this.geoPackageId = geoPackageId;
    }

    /**
     * Get the table name
     *
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Set the table name
     *
     * @param tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Get the last indexed date
     *
     * @return
     */
    public Long getLastIndexed() {
        return lastIndexed;
    }

    /**
     * Set the last indexed date
     *
     * @param lastIndexed
     */
    public void setLastIndexed(Long lastIndexed) {
        this.lastIndexed = lastIndexed;
    }

}
