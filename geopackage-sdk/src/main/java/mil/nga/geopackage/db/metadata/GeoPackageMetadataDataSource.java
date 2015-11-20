package mil.nga.geopackage.db.metadata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackageException;

/**
 * GeoPackage metadata Data Source
 *
 * @author osbornb
 */
public class GeoPackageMetadataDataSource {

    /**
     * Database
     */
    private SQLiteDatabase db;

    /**
     * Constructor
     *
     * @param db
     */
    public GeoPackageMetadataDataSource(GeoPackageMetadataDb db) {
        this.db = db.getDb();
    }

    /**
     * Constructor
     *
     * @param db
     */
    GeoPackageMetadataDataSource(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Create a new GeoPackage metadata
     *
     * @param metadata
     */
    public void create(GeoPackageMetadata metadata) {
        ContentValues values = new ContentValues();
        values.put(GeoPackageMetadata.COLUMN_NAME, metadata.getName());
        values.put(GeoPackageMetadata.COLUMN_EXTERNAL_PATH, metadata.getExternalPath());
        long insertId = db.insert(
                GeoPackageMetadata.TABLE_NAME, null,
                values);
        if (insertId == -1) {
            throw new GeoPackageException(
                    "Failed to insert GeoPackage metadata. Name: "
                            + metadata.getName() + ", External Path: "
                            + metadata.getExternalPath());
        }
        metadata.setId(insertId);
    }

    /**
     * Delete the GeoPackage metadata
     *
     * @param metadata
     * @return
     */
    public boolean delete(GeoPackageMetadata metadata) {
        return delete(metadata.getName());
    }

    /**
     * Delete the database
     *
     * @param database
     * @return
     */
    public boolean delete(String database) {

        GeoPackageMetadata metadata = get(database);
        if (metadata != null) {
            TableMetadataDataSource tableDs = new TableMetadataDataSource(db);
            tableDs.delete(metadata.getId());
        }

        String whereClause = GeoPackageMetadata.COLUMN_NAME + " = ?";
        String[] whereArgs = new String[]{database};
        int deleteCount = db.delete(
                GeoPackageMetadata.TABLE_NAME,
                whereClause, whereArgs);
        return deleteCount > 0;
    }

    /**
     * Rename the GeoPackage metadata to the new name
     *
     * @param metadata
     * @param newName
     * @return
     */
    public boolean rename(GeoPackageMetadata metadata, String newName) {
        boolean renamed = rename(metadata.getName(), newName);
        if (renamed) {
            metadata.setName(newName);
        }
        return renamed;
    }

    /**
     * Rename the GeoPackage name to the new name
     *
     * @param name
     * @param newName
     * @return
     */
    public boolean rename(String name, String newName) {
        String whereClause = GeoPackageMetadata.COLUMN_NAME + " = ?";
        String[] whereArgs = new String[]{name};
        ContentValues values = new ContentValues();
        values.put(GeoPackageMetadata.COLUMN_NAME, newName);
        int updateCount = db.update(
                GeoPackageMetadata.TABLE_NAME, values,
                whereClause, whereArgs);
        return updateCount > 0;
    }

    /**
     * Get all GeoPackage metadata
     *
     * @return
     */
    public List<GeoPackageMetadata> getAll() {
        List<GeoPackageMetadata> allMetadata = new ArrayList<GeoPackageMetadata>();

        Cursor cursor = db.query(
                GeoPackageMetadata.TABLE_NAME,
                GeoPackageMetadata.COLUMNS, null, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                GeoPackageMetadata metadata = createGeoPackageMetadata(cursor);
                allMetadata.add(metadata);
            }
        } finally {
            cursor.close();
        }
        return allMetadata;
    }

    /**
     * Get all external GeoPackage metadata
     *
     * @return
     */
    public List<GeoPackageMetadata> getAllExternal() {
        List<GeoPackageMetadata> allMetadata = new ArrayList<GeoPackageMetadata>();

        String selection = GeoPackageMetadata.COLUMN_EXTERNAL_PATH + " IS NOT NULL";
        Cursor cursor = db.query(
                GeoPackageMetadata.TABLE_NAME,
                GeoPackageMetadata.COLUMNS, selection, null, null, null, null);

        try {
            while (cursor.moveToNext()) {
                GeoPackageMetadata metadata = createGeoPackageMetadata(cursor);
                allMetadata.add(metadata);
            }
        } finally {
            cursor.close();
        }
        return allMetadata;
    }

    /**
     * Get GeoPackage metadata by name
     *
     * @return
     */
    public GeoPackageMetadata get(String database) {
        GeoPackageMetadata metadata = null;
        String selection = GeoPackageMetadata.COLUMN_NAME + " = ?";
        String[] selectionArgs = new String[]{database};
        Cursor cursor = db.query(
                GeoPackageMetadata.TABLE_NAME,
                GeoPackageMetadata.COLUMNS, selection, selectionArgs, null, null, null);
        try {
            if (cursor.moveToNext()) {
                metadata = createGeoPackageMetadata(cursor);
            }
        } finally {
            cursor.close();
        }
        return metadata;
    }

    /**
     * Get GeoPackage metadata by id
     *
     * @return
     */
    public GeoPackageMetadata get(long id) {
        GeoPackageMetadata metadata = null;
        String selection = GeoPackageMetadata.COLUMN_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        Cursor cursor = db.query(
                GeoPackageMetadata.TABLE_NAME,
                GeoPackageMetadata.COLUMNS, selection, selectionArgs, null, null, null);
        try {
            if (cursor.moveToNext()) {
                metadata = createGeoPackageMetadata(cursor);
            }
        } finally {
            cursor.close();
        }
        return metadata;
    }

    /**
     * Get GeoPackage metadata or create it if it does not exist
     *
     * @return
     */
    public GeoPackageMetadata getOrCreate(String geoPackage) {
        GeoPackageMetadata metadata = get(geoPackage);

        if (metadata == null) {
            metadata = new GeoPackageMetadata();
            metadata.setName(geoPackage);
            create(metadata);
        }

        return metadata;
    }

    /**
     * Determine if the metadata exists
     *
     * @param database
     * @return
     */
    public boolean exists(String database) {
        return get(database) != null;
    }

    /**
     * Determine if the GeoPackage is external
     *
     * @param database
     * @return
     */
    public boolean isExternal(String database) {
        GeoPackageMetadata metadata = get(database);
        return get(database) != null && metadata.getExternalPath() != null;
    }

    /**
     * Get external GeoPackage metadata by external path
     *
     * @return
     */
    public GeoPackageMetadata getExternalAtPath(String path) {
        GeoPackageMetadata metadata = null;
        String selection = GeoPackageMetadata.COLUMN_EXTERNAL_PATH + " = ?";
        String[] selectionArgs = new String[]{path};
        Cursor cursor = db.query(
                GeoPackageMetadata.TABLE_NAME,
                GeoPackageMetadata.COLUMNS, selection, selectionArgs, null, null, null);
        try {
            if (cursor.moveToNext()) {
                metadata = createGeoPackageMetadata(cursor);
            }
        } finally {
            cursor.close();
        }
        return metadata;
    }

    /**
     * Create a GeoPackage metadata from the current cursor location
     *
     * @param cursor
     * @return
     */
    private GeoPackageMetadata createGeoPackageMetadata(Cursor cursor) {
        GeoPackageMetadata metadata = new GeoPackageMetadata();
        metadata.setId(cursor.getLong(0));
        metadata.setName(cursor.getString(1));
        metadata.setExternalPath(cursor.getString(2));
        return metadata;
    }

}
