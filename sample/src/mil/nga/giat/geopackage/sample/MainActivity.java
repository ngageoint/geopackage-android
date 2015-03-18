package mil.nga.giat.geopackage.sample;

import mil.nga.giat.geopackage.factory.GeoPackageFactory;
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
	 * Map fragment
	 */
	private GeoPackageMapFragment mapFragment;

	/**
	 * Manager fragment
	 */
	private GeoPackageManagerFragment managerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize the active GeoPackages
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		GeoPackageDatabases.initialize(settings);

		// Set the content view
		setContentView(R.layout.activity_main);

		// Retrieve the fragments
		managerFragment = (GeoPackageManagerFragment) getFragmentManager()
				.findFragmentById(R.id.fragment_manager);
		mapFragment = (GeoPackageMapFragment) getFragmentManager()
				.findFragmentById(R.id.fragment_map);
		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);

		title = getString(R.string.title_manager);

		// Set up the drawer.
		navigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		// Set the first position
		onNavigationDrawerItemSelected(navigationPosition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Set the selected position
	 */
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();

		FragmentTransaction transaction = fragmentManager.beginTransaction();

		switch (position) {

		case MANAGER_POSITION:
			if (managerFragment != null) {
				transaction.show(managerFragment);
				title = getString(R.string.title_manager);
			}
			break;
		case MAP_POSITION:
			if (mapFragment != null) {
				transaction.show(mapFragment);
				title = getString(R.string.title_map);
			}
			break;
		default:

		}

		if (position != MANAGER_POSITION) {
			if (managerFragment != null && managerFragment.isAdded()) {
				transaction.hide(managerFragment);
			}
		}
		if (position != MAP_POSITION) {
			if (mapFragment != null && mapFragment.isAdded()) {
				transaction.hide(mapFragment);
			}
		}

		navigationPosition = position;

		transaction.commit();
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Restore the action bar
	 */
	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(title);
	}

	/**
	 * {@inheritDoc}
	 */
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
