package mil.nga.geopackage.db.metadata;

import android.content.ContentValues;
import android.database.Cursor;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageDatabase;
import mil.nga.wkb.geom.GeometryEnvelope;

/**
 * Table metadata Data Source
 *
 * @author osbornb
 */
public class GeometryMetadataDataSource {

    /**
     * Database
     */
    private GeoPackageDatabase db;

    /**
     * Constructor
     *
     * @param db
     */
    public GeometryMetadataDataSource(GeoPackageMetadataDb db) {
        this.db = db.getDb();
    }

    /**
     * Constructor
     *
     * @param db
     */
    GeometryMetadataDataSource(GeoPackageDatabase db) {
        this.db = db;
    }

    /**
     * Create a new geometry metadata
     *
     * @param metadata
     * @return
     */
    public long create(GeometryMetadata metadata) {
        ContentValues values = new ContentValues();
        values.put(GeometryMetadata.COLUMN_GEOPACKAGE_ID, metadata.getGeoPackageId());
        values.put(GeometryMetadata.COLUMN_TABLE_NAME, metadata.getTableName());
        values.put(GeometryMetadata.COLUMN_ID, metadata.getId());
        values.put(GeometryMetadata.COLUMN_MIN_X, metadata.getMinX());
        values.put(GeometryMetadata.COLUMN_MAX_X, metadata.getMaxX());
        values.put(GeometryMetadata.COLUMN_MIN_Y, metadata.getMinY());
        values.put(GeometryMetadata.COLUMN_MAX_Y, metadata.getMaxY());
        values.put(GeometryMetadata.COLUMN_MIN_Z, metadata.getMinZ());
        values.put(GeometryMetadata.COLUMN_MAX_Z, metadata.getMaxZ());
        values.put(GeometryMetadata.COLUMN_MIN_M, metadata.getMinM());
        values.put(GeometryMetadata.COLUMN_MAX_M, metadata.getMaxM());
        long insertId = db.insert(
                GeometryMetadata.TABLE_NAME, null,
                values);
        if (insertId == -1) {
            throw new GeoPackageException(
                    "Failed to insert geometry metadata. GeoPackage Id: "
                            + metadata.getGeoPackageId() + ", Table Name: "
                            + metadata.getTableName() + ", Geometry Id: "
                            + metadata.getId());
        }
        metadata.setId(insertId);
        return insertId;
    }

    /**
     * Create a new geometry metadata from an envelope
     *
     * @param geoPackage
     * @param tableName
     * @param geomId
     * @param envelope
     * @return
     */
    public GeometryMetadata create(String geoPackage, String tableName, long geomId, GeometryEnvelope envelope) {
        return create(getGeoPackageId(geoPackage), tableName, geomId, envelope);
    }

    /**
     * Create a new geometry metadata from an envelope
     *
     * @param geoPackageId
     * @param tableName
     * @param geomId
     * @param envelope
     * @return
     */
    public GeometryMetadata create(long geoPackageId, String tableName, long geomId, GeometryEnvelope envelope) {

        GeometryMetadata metadata = populate(geoPackageId, tableName, geomId, envelope);
        create(metadata);
        return metadata;
    }

    /**
     * Populate a new geometry metadata from an envelope
     *
     * @param geoPackageId
     * @param tableName
     * @param geomId
     * @param envelope
     * @return
     */
    public GeometryMetadata populate(long geoPackageId, String tableName, long geomId, GeometryEnvelope envelope) {

        GeometryMetadata metadata = new GeometryMetadata();
        metadata.setGeoPackageId(geoPackageId);
        metadata.setTableName(tableName);
        metadata.setId(geomId);
        metadata.setMinX(envelope.getMinX());
        metadata.setMaxX(envelope.getMaxX());
        metadata.setMinY(envelope.getMinY());
        metadata.setMaxY(envelope.getMaxY());
        if (envelope.hasZ()) {
            metadata.setMinZ(envelope.getMinZ());
            metadata.setMaxZ(envelope.getMaxZ());
        }
        if (envelope.hasM()) {
            metadata.setMinM(envelope.getMinM());
            metadata.setMaxM(envelope.getMaxM());
        }
        return metadata;
    }

    /**
     * Delete the geometry metadata
     *
     * @param metadata
     * @return
     */
    public boolean delete(GeometryMetadata metadata) {
        return delete(metadata.getGeoPackageId(), metadata.getTableName(), metadata.getId());
    }

    /**
     * Delete geometry metadata by database
     *
     * @param geoPackage
     * @return
     */
    public int delete(String geoPackage) {
        return delete(getGeoPackageId(geoPackage));
    }

    /**
     * Delete geometry metadata by database
     *
     * @param geoPackageId
     * @return
     */
    public int delete(long geoPackageId) {
        String whereClause = GeometryMetadata.COLUMN_GEOPACKAGE_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(geoPackageId)};
        int deleteCount = db.delete(
                GeometryMetadata.TABLE_NAME,
                whereClause, whereArgs);
        return deleteCount;
    }

    /**
     * Delete geometry metadata by table name
     *
     * @param geoPackage
     * @param tableName
     * @return
     */
    public int delete(String geoPackage, String tableName) {
        return delete(getGeoPackageId(geoPackage), tableName);
    }

    /**
     * Delete geometry metadata by table name
     *
     * @param geoPackageId
     * @param tableName
     * @return
     */
    public int delete(long geoPackageId, String tableName) {
        String whereClause = GeometryMetadata.COLUMN_GEOPACKAGE_ID
                + " = ? AND " + GeometryMetadata.COLUMN_TABLE_NAME + " = ?";
        String[] whereArgs = new String[]{String.valueOf(geoPackageId), tableName};
        int deleteCount = db.delete(
                GeometryMetadata.TABLE_NAME,
                whereClause, whereArgs);
        return deleteCount;
    }

    /**
     * Delete the geometry metadata
     *
     * @param geoPackage
     * @param tableName
     * @param id
     * @return
     */
    public boolean delete(String geoPackage, String tableName, long id) {
        return delete(getGeoPackageId(geoPackage), tableName, id);
    }

    /**
     * Delete the geometry metadata
     *
     * @param geoPackageId
     * @param tableName
     * @param id
     * @return
     */
    public boolean delete(long geoPackageId, String tableName, long id) {
        String whereClause = GeometryMetadata.COLUMN_GEOPACKAGE_ID
                + " = ? AND " + GeometryMetadata.COLUMN_TABLE_NAME + " = ? AND "
                + GeometryMetadata.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(geoPackageId), tableName, String.valueOf(id)};
        int deleteCount = db.delete(
                GeometryMetadata.TABLE_NAME,
                whereClause, whereArgs);
        return deleteCount > 0;
    }

    /**
     * Create the geometry metadata or update if it already exists
     *
     * @param metadata
     * @return
     */
    public boolean createOrUpdate(GeometryMetadata metadata) {

        boolean success = false;

        if (exists(metadata)) {
            success = update(metadata);
        } else {
            create(metadata);
            success = true;
        }

        return success;
    }

    /**
     * Update the geometry metadata
     *
     * @param metadata
     * @return
     */
    public boolean update(GeometryMetadata metadata) {
        String whereClause = GeometryMetadata.COLUMN_GEOPACKAGE_ID
                + " = ? AND " + GeometryMetadata.COLUMN_TABLE_NAME + " = ? AND "
                + GeometryMetadata.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(metadata.getGeoPackageId()), metadata.getTableName(), String.valueOf(metadata.getId())};
        ContentValues values = new ContentValues();
        values.put(GeometryMetadata.COLUMN_MIN_X, metadata.getMinX());
        values.put(GeometryMetadata.COLUMN_MAX_X, metadata.getMaxX());
        values.put(GeometryMetadata.COLUMN_MIN_Y, metadata.getMinY());
        values.put(GeometryMetadata.COLUMN_MAX_Y, metadata.getMaxY());
        values.put(GeometryMetadata.COLUMN_MIN_Z, metadata.getMinZ());
        values.put(GeometryMetadata.COLUMN_MAX_Z, metadata.getMaxZ());
        values.put(GeometryMetadata.COLUMN_MIN_M, metadata.getMinM());
        values.put(GeometryMetadata.COLUMN_MAX_M, metadata.getMaxM());
        int updateCount = db.update(
                GeometryMetadata.TABLE_NAME, values,
                whereClause, whereArgs);
        return updateCount > 0;
    }

    /**
     * Check if a table metadata exists
     *
     * @param metadata
     * @return
     */
    public boolean exists(GeometryMetadata metadata) {
        return get(metadata) != null;
    }

    /**
     * Get a table metadata
     *
     * @param metadata
     * @return
     */
    public GeometryMetadata get(GeometryMetadata metadata) {
        return get(metadata.getGeoPackageId(), metadata.getTableName(), metadata.getId());
    }


    /**
     * Get a table metadata
     *
     * @param geoPackage
     * @param tableName
     * @param id
     * @return
     */
    public GeometryMetadata get(String geoPackage, String tableName, long id) {
        return get(getGeoPackageId(geoPackage), tableName, id);
    }

    /**
     * Get a table metadata
     *
     * @param geoPackageId
     * @param tableName
     * @param id
     * @return
     */
    public GeometryMetadata get(long geoPackageId, String tableName, long id) {
        String selection = GeometryMetadata.COLUMN_GEOPACKAGE_ID
                + " = ? AND " + GeometryMetadata.COLUMN_TABLE_NAME + " = ? AND "
                + GeometryMetadata.COLUMN_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(geoPackageId), tableName, String.valueOf(id)};
        Cursor cursor = db.query(
                GeometryMetadata.TABLE_NAME,
                GeometryMetadata.COLUMNS, selection, selectionArgs, null, null, null);
        GeometryMetadata metadata = null;
        try {
            if (cursor.moveToNext()) {
                metadata = createGeometryMetadata(cursor);
            }
        } finally {
            cursor.close();
        }
        return metadata;
    }

    /**
     * Query for all table geometry metadata
     *
     * @param geoPackage
     * @param tableName
     * @return cursor that must be closed
     */
    public Cursor query(String geoPackage, String tableName) {
        return query(getGeoPackageId(geoPackage), tableName);
    }

    /**
     * Query for all table geometry metadata count
     *
     * @param geoPackage
     * @param tableName
     * @return count
     * @since 1.1.0
     */
    public int count(String geoPackage, String tableName) {
        return count(getGeoPackageId(geoPackage), tableName);
    }

    /**
     * Query for all table geometry metadata
     *
     * @param geoPackageId
     * @param tableName
     * @return cursor that must be closed
     */
    public Cursor query(long geoPackageId, String tableName) {
        String selection = GeometryMetadata.COLUMN_GEOPACKAGE_ID
                + " = ? AND " + GeometryMetadata.COLUMN_TABLE_NAME + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(geoPackageId), tableName};
        Cursor cursor = db.query(
                GeometryMetadata.TABLE_NAME,
                GeometryMetadata.COLUMNS, selection, selectionArgs, null, null, null);
        return cursor;
    }

    /**
     * Query for all table geometry metadata count
     *
     * @param geoPackageId
     * @param tableName
     * @return count
     * @since 1.1.0
     */
    public int count(long geoPackageId, String tableName) {
        Cursor cursor = query(geoPackageId, tableName);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * Query for all table geometry metadata matching the bounding box in the same projection
     *
     * @param geoPackage
     * @param tableName
     * @param boundingBox
     * @return cursor that must be closed
     */
    public Cursor query(String geoPackage, String tableName, BoundingBox boundingBox) {
        return query(getGeoPackageId(geoPackage), tableName, boundingBox);
    }

    /**
     * Query for all table geometry metadata count matching the bounding box in the same projection
     *
     * @param geoPackage
     * @param tableName
     * @param boundingBox
     * @return count
     * @since 1.1.0
     */
    public int count(String geoPackage, String tableName, BoundingBox boundingBox) {
        return count(getGeoPackageId(geoPackage), tableName, boundingBox);
    }

    /**
     * Query for all table geometry metadata matching the bounding box in the same projection
     *
     * @param geoPackageId
     * @param tableName
     * @param boundingBox
     * @return cursor that must be closed
     */
    public Cursor query(long geoPackageId, String tableName, BoundingBox boundingBox) {
        GeometryEnvelope envelope = new GeometryEnvelope();
        envelope.setMinX(boundingBox.getMinLongitude());
        envelope.setMaxX(boundingBox.getMaxLongitude());
        envelope.setMinY(boundingBox.getMinLatitude());
        envelope.setMaxY(boundingBox.getMaxLatitude());
        return query(geoPackageId, tableName, envelope);
    }

    /**
     * Query for all table geometry metadata count matching the bounding box in the same projection
     *
     * @param geoPackageId
     * @param tableName
     * @param boundingBox
     * @return count
     * @since 1.1.0
     */
    public int count(long geoPackageId, String tableName, BoundingBox boundingBox) {
        Cursor cursor = query(geoPackageId, tableName, boundingBox);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * Query for all table geometry metadata matching the envelope
     *
     * @param geoPackage
     * @param tableName
     * @param envelope
     * @return cursor that must be closed
     */
    public Cursor query(String geoPackage, String tableName, GeometryEnvelope envelope) {
        return query(getGeoPackageId(geoPackage), tableName, envelope);
    }

    /**
     * Query for all table geometry metadata count matching the envelope
     *
     * @param geoPackage
     * @param tableName
     * @param envelope
     * @return count
     * @since 1.1.0
     */
    public int count(String geoPackage, String tableName, GeometryEnvelope envelope) {
        return count(getGeoPackageId(geoPackage), tableName, envelope);
    }

    /**
     * Query for all table geometry metadata matching the envelope
     *
     * @param geoPackageId
     * @param tableName
     * @param envelope
     * @return cursor that must be closed
     */
    public Cursor query(long geoPackageId, String tableName, GeometryEnvelope envelope) {
        StringBuilder selection = new StringBuilder();
        selection.append(GeometryMetadata.COLUMN_GEOPACKAGE_ID).append(" = ? AND ")
                .append(GeometryMetadata.COLUMN_TABLE_NAME).append(" = ?");
        selection.append(" AND ").append(GeometryMetadata.COLUMN_MIN_X).append(" <= ?");
        selection.append(" AND ").append(GeometryMetadata.COLUMN_MAX_X).append(" >= ?");
        selection.append(" AND ").append(GeometryMetadata.COLUMN_MIN_Y).append(" <= ?");
        selection.append(" AND ").append(GeometryMetadata.COLUMN_MAX_Y).append(" >= ?");
        int args = 6;
        if (envelope.hasZ()) {
            args += 2;
            selection.append(" AND ").append(GeometryMetadata.COLUMN_MIN_Z).append(" <= ?");
            selection.append(" AND ").append(GeometryMetadata.COLUMN_MAX_Z).append(" >= ?");
        }
        if (envelope.hasM()) {
            args += 2;
            selection.append(" AND ").append(GeometryMetadata.COLUMN_MIN_M).append(" <= ?");
            selection.append(" AND ").append(GeometryMetadata.COLUMN_MAX_M).append(" >= ?");
        }
        String[] selectionArgs = new String[args];
        int argCount = 0;
        selectionArgs[argCount++] = String.valueOf(geoPackageId);
        selectionArgs[argCount++] = tableName;
        selectionArgs[argCount++] = String.valueOf(envelope.getMaxX());
        selectionArgs[argCount++] = String.valueOf(envelope.getMinX());
        selectionArgs[argCount++] = String.valueOf(envelope.getMaxY());
        selectionArgs[argCount++] = String.valueOf(envelope.getMinY());
        if (envelope.hasZ()) {
            selectionArgs[argCount++] = String.valueOf(envelope.getMaxZ());
            selectionArgs[argCount++] = String.valueOf(envelope.getMinZ());
        }
        if (envelope.hasM()) {
            selectionArgs[argCount++] = String.valueOf(envelope.getMaxM());
            selectionArgs[argCount++] = String.valueOf(envelope.getMinM());
        }
        Cursor cursor = db.query(
                GeometryMetadata.TABLE_NAME,
                GeometryMetadata.COLUMNS, selection.toString(), selectionArgs, null, null, null);
        return cursor;
    }

    /**
     * Query for all table geometry metadata count matching the envelope
     *
     * @param geoPackageId
     * @param tableName
     * @param envelope
     * @return count
     * @since 1.1.0
     */
    public int count(long geoPackageId, String tableName, GeometryEnvelope envelope) {
        Cursor cursor = query(geoPackageId, tableName, envelope);
        int count = cursor.getCount();
        cursor.close();
        return count;
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
     * Create a geometry metadata from the current cursor location
     *
     * @param cursor
     * @return
     */
    public static GeometryMetadata createGeometryMetadata(Cursor cursor) {
        GeometryMetadata metadata = new GeometryMetadata();
        metadata.setGeoPackageId(cursor.getLong(0));
        metadata.setTableName(cursor.getString(1));
        metadata.setId(cursor.getLong(2));
        metadata.setMinX(cursor.getDouble(3));
        metadata.setMaxX(cursor.getDouble(4));
        metadata.setMinY(cursor.getDouble(5));
        metadata.setMaxY(cursor.getDouble(6));
        if (!cursor.isNull(7)) {
            metadata.setMinZ(cursor.getDouble(7));
        }
        if (!cursor.isNull(8)) {
            metadata.setMaxZ(cursor.getDouble(8));
        }
        if (!cursor.isNull(9)) {
            metadata.setMinM(cursor.getDouble(9));
        }
        if (!cursor.isNull(10)) {
            metadata.setMaxM(cursor.getDouble(10));
        }
        return metadata;
    }

}
