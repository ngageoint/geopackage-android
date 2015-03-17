package mil.nga.giat.geopackage.sample;

import mil.nga.giat.geopackage.geom.unit.AndroidProjectionParameterRetriever;
import mil.nga.giat.geopackage.geom.unit.ProjectionFactory;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Main Activity
 * 
 * @author osbornb
 */
public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	public static final int ACTIVITY_CHOOSE_FILE = 3342;
	
	public static final int ACTIVITY_SHARE_FILE = 3343;

	/**
	 * Manager drawer position
	 */
	private static final int MANAGER_POSITION = 0;

	/**
	 * Map drawer position
	 */
	private static final int MAP_POSITION = 1;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment navigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence title;

	/**
	 * Current drawer position
	 */
	private int navigationPosition = MANAGER_POSITION;

	/**
	 * Active or checked databases
	 */
	private GeoPackageDatabases active;

	/**
	 * Map fragment
	 */
	private GeoPackageMapFragment mapFragment = GeoPackageMapFragment
			.newInstance();

	/**
	 * Manager fragment
	 */
	private GeoPackageManagerFragment managerFragment = GeoPackageManagerFragment
			.newInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize the projection factory
		AndroidProjectionParameterRetriever projectionRetriever = new AndroidProjectionParameterRetriever(this);
		ProjectionFactory.initialize(projectionRetriever);
		
		// if (savedInstanceState != null) {
		// mapFragment = (GeoPackageMapFragment) getFragmentManager()
		// .getFragment(savedInstanceState, "mapFragment");
		// managerFragment = (GeoPackageManagerFragment) getFragmentManager()
		// .getFragment(savedInstanceState, "managerFragment");
		// }
		// if (mapFragment == null) {
		// mapFragment = GeoPackageMapFragment.newInstance();
		// }
		// if (managerFragment == null) {
		// managerFragment = GeoPackageManagerFragment.newInstance();
		// }

		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		title = getString(R.string.title_manager);

		// Set up the drawer.
		navigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onResume() {
		active = new GeoPackageDatabases();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		active.setPreferences(settings);
		active.loadFromPreferences();
		mapFragment.setActive(active);
		managerFragment.setActive(active);
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// if (mapFragment.isAdded()) {
		// getFragmentManager().putFragment(outState, "mapFragment",
		// mapFragment);
		// }
		// if (managerFragment.isAdded()) {
		// getFragmentManager().putFragment(outState, "managerFragment",
		// managerFragment);
		// }
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();

		FragmentTransaction transaction = fragmentManager.beginTransaction();

		switch (position) {

		case MANAGER_POSITION:
			if (managerFragment.isAdded()) {
				transaction.show(managerFragment);
			} else {
				transaction.add(R.id.container, managerFragment);
			}
			title = getString(R.string.title_manager);
			break;
		case MAP_POSITION:
			if (mapFragment.isAdded()) {
				transaction.show(mapFragment);
			} else {
				transaction.add(R.id.container, mapFragment);
			}
			title = getString(R.string.title_map);
			break;
		default:

		}

		if (position != MANAGER_POSITION) {
			if (managerFragment.isAdded()) {
				transaction.hide(managerFragment);
			}
		}
		if (position != MAP_POSITION) {
			if (mapFragment.isAdded()) {
				transaction.hide(mapFragment);
			}
		}

		navigationPosition = position;

		transaction.commit();
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(title);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!navigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);

			if (navigationPosition != MANAGER_POSITION) {
				menu.setGroupVisible(R.id.menu_group_list, false);
			}
			if (navigationPosition != MAP_POSITION) {
				menu.setGroupVisible(R.id.menu_group_map, false);
			} else if (mapFragment != null) {
				mapFragment.handleMenu(menu);
			}

			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (mapFragment.handleMenuClick(item)) {
			return true;
		}
		if (managerFragment.handleMenuClick(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
