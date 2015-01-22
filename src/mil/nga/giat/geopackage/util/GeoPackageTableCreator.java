package mil.nga.giat.geopackage.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import mil.nga.giat.geopackage.R;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Executes database scripts to create GeoPackage tables
 * 
 * @author osbornb
 */
public class GeoPackageTableCreator {

	/**
	 * Log tag
	 */
	private static final String TAG = GeoPackageTableCreator.class
			.getSimpleName();

	/**
	 * Context
	 */
	private final Context context;

	/**
	 * Assest manager
	 */
	private final AssetManager assetManager;

	/**
	 * SQLite database
	 */
	private final SQLiteDatabase db;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param db
	 */
	public GeoPackageTableCreator(Context context, SQLiteDatabase db) {
		this.context = context;
		this.assetManager = context.getAssets();
		this.db = db;
	}

	/**
	 * Create Spatial Reference System table and views
	 * 
	 * @return
	 */
	public int createSpatialReferenceSystem() {
		int statements = 0;
		try {

			InputStream scriptStream = assetManager.open(context
					.getString(R.string.sql_directory)
					+ File.separatorChar
					+ context.getString(R.string.sql_spatial_reference_system));
			statements = runScript(scriptStream);
		} catch (IOException e) {
			Log.e(TAG, "Unable to create spatial reference system tables.", e);
		}
		return statements;
	}

	/**
	 * Create Contents table
	 * 
	 * @return
	 */
	public int createContents() {
		int statements = 0;
		try {

			InputStream scriptStream = assetManager.open(context
					.getString(R.string.sql_directory)
					+ File.separatorChar
					+ context.getString(R.string.sql_contents));
			statements = runScript(scriptStream);
		} catch (IOException e) {
			Log.e(TAG, "Unable to create content tables.", e);
		}
		return statements;
	}

	/**
	 * Create Geometry Columns table
	 * 
	 * @return
	 */
	public int createGeometryColumns() {
		int statements = 0;
		try {

			InputStream scriptStream = assetManager.open(context
					.getString(R.string.sql_directory)
					+ File.separatorChar
					+ context.getString(R.string.sql_geometry_columns));
			statements = runScript(scriptStream);
		} catch (IOException e) {
			Log.e(TAG, "Unable to create geometry columns tables.", e);
		}
		return statements;
	}

	/**
	 * Run the script input stream
	 * 
	 * @param stream
	 * @return
	 */
	private int runScript(final InputStream stream) {
		int count = 0;

		// Use multiple newlines as the delimiter
		Scanner s = new java.util.Scanner(stream).useDelimiter(Pattern
				.compile("\\n\\s*\\n"));

		// Execute each statement
		while (s.hasNext()) {
			String statement = s.next().trim();
			db.execSQL(statement);
			count++;
		}
		return count;
	}

}
