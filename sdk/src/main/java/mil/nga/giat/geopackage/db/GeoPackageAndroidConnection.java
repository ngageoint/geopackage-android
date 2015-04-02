package mil.nga.giat.geopackage.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * GeoPackage Android Connection wrapper
 *
 * @author osbornb
 */
public class GeoPackageAndroidConnection implements GeoPackageConnection {

    /**
     * Database connection
     */
    private final SQLiteDatabase db;

    /**
     * Consructor
     *
     * @param db
     */
    public GeoPackageAndroidConnection(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Get the database connection
     *
     * @return
     */
    public SQLiteDatabase getDb() {
        return db;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionSource getConnectionSource() {
        return new AndroidConnectionSource(db);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execSQL(String sql) {
        db.execSQL(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tableExists(String tableName) {
        boolean exists = false;

        Cursor cursor = db
                .rawQuery(
                        "select DISTINCT tbl_name from sqlite_master where tbl_name = ?",
                        new String[]{tableName});
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }

        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        db.close();
    }

}
