package mil.nga.giat.geopackage.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import mil.nga.giat.geopackage.datastore.GeopackageSQLiteOpenHelper;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * 
 * @author osbornb
 */
public class GeoPackageScriptExecutor {

	private static final String TAG = GeopackageSQLiteOpenHelper.class
			.getSimpleName();

	private final AssetManager assetManager;

	private final SQLiteDatabase db;

	public GeoPackageScriptExecutor(Context context, SQLiteDatabase db) {
		this.assetManager = context.getAssets();
		this.db = db;
	}

	public int createSpatialReferenceSystem() {
		int statements = 0;
		try {

			InputStream scriptStream = assetManager.open("sql"
					+ File.separatorChar + "C.1. gpkg_spatial_ref_sys");
			statements = runScript(scriptStream);
		} catch (IOException e) {
			Log.e(TAG, "Unable to create spatial reference system tables.", e);
		}
		return statements;
	}

	/**
	 * A convenience method for reading in an InputStream (corresponds to a
	 * single SQL script) and executing the statements within that script.
	 * Individual statements need to be terminated by a semi-colon.
	 * 
	 * @param stream
	 *            An InputStream (should represent a single file in the
	 *            asset/sql directory).
	 * @return The number of sql statements that were successfully run.
	 */
	private int runScript(final InputStream stream) {
		int count = 0;
		Scanner s = new java.util.Scanner(stream).useDelimiter(Pattern
				.compile("\\n\\s*\\n"));
		while (s.hasNext()) {
			String statement = s.next().trim();
			db.execSQL(statement);
			count++;
		}
		return count;
	}

}
