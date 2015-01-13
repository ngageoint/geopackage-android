package mil.nga.giat;

import mil.nga.giat.datastore.GeopackageSQLiteOpenHelper;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
				
		super.onCreate(savedInstanceState);
				
		int i = 0;
		
		//force the database to init.
	    GeopackageSQLiteOpenHelper database = 
	    		new GeopackageSQLiteOpenHelper(getApplicationContext(), "geopackage.db", null, 1);
	    
	    database.getReadableDatabase();
	    

	}
	
}
