package mil.nga.geopackage.db.metadata;

import android.content.ContentValues;
import android.database.Cursor;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageDatabase;

/**
 * Table metadata Data Source
 *
 * @author osbornb
 */
public class TableMetadataDataSource {

    /**
     * Database
     */
    private GeoPackageDatabase db;

    /**
     * Constructor
     *
     * @param db
     */
    public TableMetadataDataSource(GeoPackageMetadataDb db) {
        this.db = db.getDb();
    }

    /**
     * Constructor
     *
     * @param db
     */
    TableMetadataDataSource(GeoPackageDatabase db) {
        this.db = db;
    }

    /**
     * Create a new table metadata
     *
     * @param metadata
     */
    public void create(TableMetadata metadata) {
        ContentValues values = new ContentValues();
        values.put(TableMetadata.COLUMN_GEOPACKAGE_ID, metadata.getGeoPackageId());
        values.put(TableMetadata.COLUMN_TABLE_NAME, metadata.getTableName());
        values.put(TableMetadata.COLUMN_LAST_INDEXED, metadata.getLastIndexed());
        long insertId = db.insert(
                TableMetadata.TABLE_NAME, null,
                values);
        if (insertId == -1) {
            throw new GeoPackageException(
                    "Failed to insert table metadata. GeoPackage Id: "
                            + metadata.getGeoPackageId() + ", Table Name: "
                            + metadata.getTableName() + ", Last Indexed: "
                            + metadata.getLastIndexed());
        }
    }

    /**
     * Delete the table metadata
     *
     * @param metadata
     * @return
     */
    public boolean delete(TableMetadata metadata) {
        return delete(metadata.getGeoPackageId(), metadata.getTableName());
    }

    /**
     * Delete the database
     *
     * @param geoPackage
     * @return
     */
    public int delete(String geoPackage) {
        return delete(getGeoPackageId(geoPackage));
    }

    /**
     * Delete the database
     *
     * @param geoPackageId
     * @return
     */
    public int delete(long geoPackageId) {

        GeometryMetadataDataSource geomDs = new GeometryMetadataDataSource(db);
        geomDs.delete(geoPackageId);

        String whereClause = TableMetadata.COLUMN_GEOPACKAGE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(geoPackageId)};
        int deleteCount = db.delete(
                TableMetadata.TABLE_NAME,
                whereClause, whereArgs);
        return deleteCount;
    }

    /**
     * Delete the database table name
     *
     * @param geoPackage
     * @param tableName
     * @return
     */
    public boolean delete(String geoPackage, String tableName) {
        return delete(getGeoPackageId(geoPackage), tableName);
    }

    /**
     * Delete the database table name
     *
     * @param geoPackageId
     * @param tableName
     * @return
     */
    public boolean delete(long geoPackageId, String tableName) {

        GeometryMetadataDataSource geomDs = new GeometryMetadataDataSource(db);
        geomDs.delete(geoPackageId, tableName);

        String whereClause = TableMetadata.COLUMN_GEOPACKAGE_ID
                + " = ? AND " + TableMetadata.COLUMN_TABLE_NAME + " = ?";
        String[] whereArgs = new String[]{String.valueOf(geoPackageId), tableName};
        int deleteCount = db.delete(
                TableMetadata.TABLE_NAME,
                whereClause, whereArgs);
        return deleteCount > 0;
    }

    /**
     * Update the last indexed time
     *
     * @param metadata
     * @param lastIndexed
     * @return
     */
    public boolean updateLastIndexed(TableMetadata metadata, long lastIndexed) {
        boolean updated = updateLastIndexed(metadata.getGeoPackageId(), metadata.getTableName(), lastIndexed);
        if (updated) {
            metadata.setLastIndexed(lastIndexed);
        }
        return updated;
    }

    /**
     * Update the last indexed time
     *
     * @param geoPackage
     * @param tableName
     * @param lastIndexed
     * @return
     */
    public boolean updateLastIndexed(String geoPackage, String tableName, long lastIndexed) {
        return updateLastIndexed(getGeoPackageId(geoPackage), tableName, lastIndexed);
    }

    /**
     * Update the last indexed time
     *
     * @param geoPackageId
     * @param tableName
     * @param lastIndexed
     * @return
     */
    public boolean updateLastIndexed(long geoPackageId, String tableName, long lastIndexed) {
        String whereClause = TableMetadata.COLUMN_GEOPACKAGE_ID
                + " = ? AND " + TableMetadata.COLUMN_TABLE_NAME + " = ?";
        String[] whereArgs = new String[]{String.valueOf(geoPackageId), tableName};
        ContentValues values = new ContentValues();
        values.put(TableMetadata.COLUMN_LAST_INDEXED, lastIndexed);
        int updateCount = db.update(
                TableMetadata.TABLE_NAME, values,
                whereClause, whereArgs);
        return updateCount > 0;
    }

    /**
     * Get a table metadata
     *
     * @param geoPackage
     * @param tableName
     * @return
     */
    public TableMetadata get(String geoPackage, String tableName) {
        return get(getGeoPackageId(geoPackage), tableName);
    }

    /**
     * Get a table metadata
     *
     * @param geoPackageId
     * @param tableName
     * @return
     */
    public TableMetadata get(long geoPackageId, String tableName) {

        String selection = TableMetadata.COLUMN_GEOPACKAGE_ID
                + " = ? AND " + TableMetadata.COLUMN_TABLE_NAME + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(geoPackageId), tableName};
        Cursor cursor = db.query(
                TableMetadata.TABLE_NAME,
                TableMetadata.COLUMNS, selection, selectionArgs, null, null, null);
        TableMetadata metadata = null;
        try {
            if (cursor.moveToNext()) {
                metadata = createTableMetadata(cursor);
            }
        } finally {
            cursor.close();
        }
        return metadata;
    }

    /**
     * Get a table metadata or create if it does not exist
     *
     * @param geoPackage
     * @param tableName
     * @return
     */
    public TableMetadata getOrCreate(String geoPackage, String tableName) {

        GeoPackageMetadataDataSource ds = new GeoPackageMetadataDataSource(db);
        GeoPackageMetadata geoPackageMetadata = ds.getOrCreate(geoPackage);

        TableMetadata metadata = get(geoPackageMetadata.getId(), tableName);

        if(metadata == null){
            metadata = new TableMetadata();
            metadata.setGeoPackageId(geoPackageMetadata.getId());
            metadata.setTableName(tableName);
            create(metadata);
        }
        return metadata;
    }

    /**
     * Get a GeoPackage id from the name
     *
     * @param geoPackage
     * @return
     */
    public long getGeoPackageId(String geoPackage) {
        long id = -1;
        GeoPackageMetadataDataSource ds = new GeoPackageMetadataDataSource(db);
        GeoPackageMetadata metadata = ds.get(geoPackage);
        if (metadata != null) {
            id = metadata.getId();
        }
        return id;
    }

    /**
     * Create a table metadata from the current cursor location
     *
     * @param cursor
     * @return
     */
    private TableMetadata createTableMetadata(Cursor cursor) {
        TableMetadata metadata = new TableMetadata();
        metadata.setGeoPackageId(cursor.getLong(0));
        metadata.setTableName(cursor.getString(1));
        if (!cursor.isNull(2)) {
            metadata.setLastIndexed(cursor.getLong(2));
        }
        return metadata;
    }

}
