package mil.nga.giat.geopackage.db.metadata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * GeoPackage Metadata database table
 * 
 * @author osbornb
 */
public class GeoPackageMetadataOpenHelper extends SQLiteOpenHelper {

	/**
	 * Metadata database name
	 */
	public static final String DATABASE_NAME = "geopackage_metadata";

	/**
	 * Metadata database version
	 */
	public static final int DATABASE_VERSION = 1;

	/**
	 * External GeoPackage table for maintaining links to external GeoPackages
	 */
	public static final String TABLE_EXTERNAL_GEOPACKAGE = "external_geopackage";

	/**
	 * Name column
	 */
	public static final String COLUMN_NAME = "name";

	/**
	 * Path column
	 */
	public static final String COLUMN_PATH = "path";

	/**
	 * Create table
	 */
	private static final String TABLE_EXTERNAL_GEOPACKAGE_CREATE = "CREATE TABLE "
			+ TABLE_EXTERNAL_GEOPACKAGE
			+ "("
			+ COLUMN_NAME
			+ " TEXT NOT NULL PRIMARY KEY, " + COLUMN_PATH + " TEXT NOT NULL);";

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public GeoPackageMetadataOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_EXTERNAL_GEOPACKAGE_CREATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXTERNAL_GEOPACKAGE);
		onCreate(db);
	}

}
