package mil.nga.giat;

import mil.nga.giat.datastore.Initializer;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Initializer initializer = new Initializer();
		initializer.initilizeDatabase(getApplicationContext());
		
		
	}
	
}
