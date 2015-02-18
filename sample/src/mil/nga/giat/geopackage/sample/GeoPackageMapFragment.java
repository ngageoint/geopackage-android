package mil.nga.giat.geopackage.sample;

import java.util.HashMap;
import java.util.Map;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageManager;
import mil.nga.giat.geopackage.factory.GeoPackageFactory;
import mil.nga.giat.geopackage.features.user.FeatureCursor;
import mil.nga.giat.geopackage.features.user.FeatureDao;
import mil.nga.giat.geopackage.features.user.FeatureRow;
import mil.nga.giat.geopackage.geom.Geometry;
import mil.nga.giat.geopackage.geom.conversion.GoogleMapShapeConverter;
import mil.nga.giat.geopackage.geom.data.GeoPackageGeometryData;
import mil.nga.giat.geopackage.tiles.overlay.GoogleAPIGeoPackageOverlay;
import mil.nga.giat.geopackage.tiles.user.TileDao;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.TileOverlayOptions;

/**
 * Map Fragment for showing GeoPackage features and tiles
 * 
 * @author osbornb
 */
public class GeoPackageMapFragment extends Fragment {

	/**
	 * Max features key for saving to preferences
	 */
	private static final String MAX_FEATURES_KEY = "max_features_key";

	/**
	 * Map type key for saving to preferences
	 */
	private static final String MAP_TYPE_KEY = "map_type_key";

	/**
	 * Get a new instance of the fragment
	 * 
	 * @return
	 */
	public static GeoPackageMapFragment newInstance() {
		GeoPackageMapFragment mapFragment = new GeoPackageMapFragment();
		return mapFragment;
	}

	/**
	 * Active GeoPackages
	 */
	private GeoPackageDatabases active;

	/**
	 * Google map
	 */
	private GoogleMap map;

	/**
	 * View
	 */
	private static View view;

	/**
	 * GeoPackage manager
	 */
	private GeoPackageManager manager;

	/**
	 * Update task
	 */
	private MapUpdateTask updateTask;

	/**
	 * Mapping of open GeoPackages by name
	 */
	private Map<String, GeoPackage> geoPackages = new HashMap<String, GeoPackage>();

	/**
	 * Constructor
	 */
	public GeoPackageMapFragment() {

	}

	/**
	 * Set the active databases
	 * 
	 * @param active
	 */
	public void setActive(GeoPackageDatabases active) {
		this.active = active;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null) {
				parent.removeView(view);
			}
		}

		try {
			view = inflater.inflate(R.layout.fragment_map, container, false);
		} catch (InflateException e) {

		}
		map = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.fragment_map_view_ui)).getMap();

		map.setMyLocationEnabled(true);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		int mapType = settings.getInt(MAP_TYPE_KEY, 1);
		map.setMapType(mapType);

		manager = GeoPackageFactory.getManager(getActivity());
		updateInBackground();

		return view;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		map.setMyLocationEnabled(!hidden);

		if (!hidden && active.isModified()) {
			active.setModified(false);
			updateInBackground();
		}
	}

	/**
	 * Handle map menu clicks
	 * 
	 * @param item
	 * @return
	 */
	public boolean handleMenuClick(MenuItem item) {

		boolean handled = true;

		switch (item.getItemId()) {
		case R.id.max_features:
			setMaxFeatures();
			break;
		case R.id.normal_map:
			setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.satellite_map:
			setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.terrain_map:
			setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		case R.id.hybrid_map:
			setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		default:
			handled = false;
			break;
		}

		return handled;
	}

	/**
	 * Let the user set the max number of features to draw
	 */
	private void setMaxFeatures() {

		final EditText input = new EditText(getActivity());
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		final String maxFeatures = String.valueOf(getMaxFeatures());
		input.setText(maxFeatures);

		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.map_max_features))
				.setMessage(getString(R.string.map_max_features_message))
				.setView(input)
				.setPositiveButton(getString(R.string.button_ok_label),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String value = input.getText().toString();
								if (value != null && !value.equals(maxFeatures)) {
									int maxFeature = Integer.parseInt(value);
									SharedPreferences settings = PreferenceManager
											.getDefaultSharedPreferences(getActivity());
									Editor editor = settings.edit();
									editor.putInt(MAX_FEATURES_KEY, maxFeature);
									editor.commit();
									updateInBackground();
								}
							}
						})
				.setNegativeButton(getString(R.string.button_cancel_label),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						});

		dialog.show();
	}

	/**
	 * Set the map type
	 * 
	 * @param mapType
	 */
	private void setMapType(int mapType) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		Editor editor = settings.edit();
		editor.putInt(MAP_TYPE_KEY, mapType);
		editor.commit();
		if (map != null) {
			map.setMapType(mapType);
		}
	}

	/**
	 * Update the map by kicking off a background task
	 */
	private void updateInBackground() {

		if (updateTask != null) {
			updateTask.cancel(false);
		}
		map.clear();
		for (GeoPackage geoPackage : geoPackages.values()) {
			geoPackage.close();
		}
		geoPackages.clear();
		updateTask = new MapUpdateTask();
		updateTask.execute();

	}

	/**
	 * Update the map in the background
	 */
	private class MapUpdateTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			update(this);
			return null;
		}
	}

	/**
	 * Get the max features
	 * 
	 * @return
	 */
	private int getMaxFeatures() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		int maxFeatures = settings.getInt(MAX_FEATURES_KEY, getResources()
				.getInteger(R.integer.map_max_features_default));
		return maxFeatures;
	}

	/**
	 * Update the map
	 * 
	 * @param task
	 */
	private void update(MapUpdateTask task) {

		if (active != null) {

			// Add tile overlays first
			for (GeoPackageDatabase database : active.getDatabases()) {

				// Open each GeoPackage
				GeoPackage geoPackage = manager.open(database.getDatabase());
				geoPackages.put(database.getDatabase(), geoPackage);

				// Display the tiles
				for (GeoPackageTable tiles : database.getTiles()) {
					displayTiles(task, tiles);
					if (task.isCancelled()) {
						break;
					}
				}

				if (task.isCancelled()) {
					break;
				}
			}

			// Add features
			int featuresLeft = getMaxFeatures();
			for (GeoPackageDatabase database : active.getDatabases()) {

				if (featuresLeft <= 0) {
					break;
				}

				for (GeoPackageTable features : database.getFeatures()) {
					int count = displayFeatures(task, features, featuresLeft);
					featuresLeft -= count;
					if (task.isCancelled() || featuresLeft <= 0) {
						break;
					}
				}

				if (task.isCancelled()) {
					break;
				}
			}

		}

	}

	/**
	 * Display features
	 * 
	 * @param task
	 * @param features
	 * @param maxFeatures
	 * @return count of features added
	 */
	private int displayFeatures(MapUpdateTask task, GeoPackageTable features,
			int maxFeatures) {

		int count = 0;

		GeoPackage geoPackage = geoPackages.get(features.getDatabase());

		FeatureDao featureDao = geoPackage.getFeatureDao(features.getName());

		FeatureCursor cursor = featureDao.queryForAll();
		try {

			final GoogleMapShapeConverter converter = new GoogleMapShapeConverter();

			while (!task.isCancelled() && count < maxFeatures
					&& cursor.moveToNext()) {
				FeatureRow row = cursor.getRow();
				GeoPackageGeometryData geometryData = row.getGeometry();
				if (geometryData != null && !geometryData.isEmpty()) {

					final Geometry geometry = geometryData.getGeometry();

					if (geometry != null) {
						count++;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								converter.addToMap(map, geometry);
							}
						});
					}
				}
			}

		} finally {
			cursor.close();
		}

		return count;
	}

	/**
	 * Display tiles
	 * 
	 * @param task
	 * @param tiles
	 */
	private void displayTiles(MapUpdateTask task, GeoPackageTable tiles) {

		GeoPackage geoPackage = geoPackages.get(tiles.getDatabase());

		TileDao tileDao = geoPackage.getTileDao(tiles.getName());

		GoogleAPIGeoPackageOverlay overlay = new GoogleAPIGeoPackageOverlay(
				tileDao);
		final TileOverlayOptions overlayOptions = new TileOverlayOptions();
		overlayOptions.tileProvider(overlay);
		overlayOptions.zIndex(-1);

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				map.addTileOverlay(overlayOptions);
			}
		});
	}

}
