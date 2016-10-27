package mil.nga.geopackage.db.metadata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageDatabase;

/**
 * GeoPackage Metadata database
 *
 * @author osbornb
 */
public class GeoPackageMetadataDb extends SQLiteOpenHelper {

    /**
     * Metadata database name
     */
    public static final String DATABASE_NAME = "geopackage_metadata";

    /**
     * Metadata database version
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Open db
     */
    private GeoPackageDatabase db;

    /**
     * Constructor
     *
     * @param context
     */
    public GeoPackageMetadataDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GeoPackageMetadata.CREATE_SQL);
        db.execSQL(TableMetadata.CREATE_SQL);
        db.execSQL(GeometryMetadata.CREATE_SQL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GeometryMetadata.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TableMetadata.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GeoPackageMetadata.TABLE_NAME);
        onCreate(db);
    }

    /**
     * Open database
     */
    public void open() {
        db = new GeoPackageDatabase(getWritableDatabase());
    }

    /**
     * Get the open database connection
     *
     * @return
     */
    GeoPackageDatabase getDb() {
        if (db == null) {
            throw new GeoPackageException("Database connection is not open");
        }
        return db;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        super.close();
        db = null;
    }

}
