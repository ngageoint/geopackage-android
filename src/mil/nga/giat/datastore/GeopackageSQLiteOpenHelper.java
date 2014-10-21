package mil.nga.giat.datastore;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;




public class Initializer {
	
	private static final String TAG = Initializer.class.getSimpleName();
	
	public void initilizeDatabase(final Context pContext) {
		
		
		AssetManager assetManager = pContext.getAssets();
		Log.d(TAG, "Initializing AssetManager...");
		System.out.println("Initializing AssetManager...");
		
		try {
			
			String[] initScripts = assetManager.list("sql");
			Arrays.sort(initScripts, new ScriptComparator());
			
			for (int i = 0; i < initScripts.length; i++) {
				//TODO run insert scripts
				String script = initScripts[i];
				Log.i(TAG,"Script: " + script);
			}
					
		}
		catch (IOException ioe) {
			Log.d(TAG, "Unable to initialize database.",ioe);
		}
		
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
