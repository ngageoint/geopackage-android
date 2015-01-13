package mil.nga.giat.datastore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;




public class GeopackageSQLiteOpenHelper extends SQLiteOpenHelper {
	
	

	private static final String TAG = GeopackageSQLiteOpenHelper.class.getSimpleName();
	
	private AssetManager assetManager;
	
	

	public GeopackageSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
		
		super(context, name, factory, version);
		assetManager = context.getAssets();
		
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		try {
			
			//read in (and sort) the list of init scripts from the assets directory.
			String[] initScripts = assetManager.list("sql");
			Arrays.sort(initScripts, new ScriptComparator());
			
			//for each file in the assets/sql directory.
			for (int i = 0; i < initScripts.length; i++) {
			
				//handle for single script.				
				InputStream scriptStream = 
					assetManager.open("sql" + File.separatorChar + initScripts[i], AssetManager.ACCESS_BUFFER);
				
				//execute all statements in file.
				runScript(scriptStream, db);
				
			}			
			
		}
		catch(IOException ioe) {
			Log.e(TAG, "Unable to create database.", ioe);
		}
		
	
		
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
		//            + newVersion + ", which will destroy all old data");
		//    db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
		//    onCreate(db);		
	}
	
	/**
	 * A convenience method for reading in an InputStream (corresponds to a single SQL script) and executing the statements
	 * within that script.  Individual statements need to be terminated by a semi-colon.
	 * 
	 * @param pScriptStream An InputStream (should represent a single file in the asset/sql directory).
	 * @param pDb A handle for SQLiteDatabase interaction.
	 * @return The number of scripts that were successfully run.
	 */
	private int runScript(final InputStream pScriptStream, final SQLiteDatabase pDb) {
		Scanner s = new java.util.Scanner(pScriptStream).useDelimiter("\\A");
		String fileContents =  s.hasNext() ? s.next() : "";
		
		StringTokenizer scripts = new StringTokenizer(fileContents, ";");
		int j = 0;
		while(scripts.hasMoreElements()) {
			String query = ((String)scripts.nextElement()).trim();
			pDb.execSQL(query);			
			j++;
		}
		return j;
	}
	
	/**
	 * This Comparator is used to sort a list of sql scripts by their annex value and script number.
	 * The geopackage specification lays out the proposed schema generation in the following format:
	 * 
	 *            [annex].[script number].[script name]
	 *            
	 * The scripts are sorted by their annex group first (i.e. annex 'A' scripts run before 
	 * annex 'B' scripts).  After divided into their separate groups, scripts are then ordered by
	 * their script number (i.e. script 1 runs before script 2).           
	 *            
	 * It is worth mentioning that this Comparator will not work with script names that are not
	 * in the format specified above.
	 */
	private class ScriptComparator implements Comparator<String> {

		@Override
		public int compare(String lhs, String rhs) {

			int returnValue = 0;
			
			StringTokenizer stLeft  = new StringTokenizer(lhs, ".");
			StringTokenizer stRight = new StringTokenizer(rhs, ".");
			
			Character leftAnnex = Character.valueOf(stLeft.nextToken().charAt(0));
			Character rightAnnex = Character.valueOf(stRight.nextToken().charAt(0));

			Integer leftScriptNumber = Integer.valueOf(stLeft.nextToken());
			Integer rightScriptNumber = Integer.valueOf(stRight.nextToken());
			
			//compare annex value...
			returnValue = leftAnnex.compareTo(rightAnnex);
			
			//both scripts belong in the same annex.  Further comparison required.
			if(returnValue == 0) {
				returnValue = leftScriptNumber.compareTo(rightScriptNumber);
			}
			return returnValue;
		}
		
	}	
	
}
