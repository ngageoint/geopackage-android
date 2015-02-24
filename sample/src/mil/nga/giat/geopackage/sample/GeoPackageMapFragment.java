package mil.nga.giat.geopackage.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.giat.geopackage.BoundingBox;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.GeoPackageException;
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

/**
 * Map Fragment for showing GeoPackage features and tiles
 * 
 * @author osbornb
 */
public class GeoPackageMapFragment extends Fragment implements
		OnMapLongClickListener, ILoadTilesTask {

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
	 * Vibrator
	 */
	private Vibrator vibrator;

	/**
	 * Touchable map layout
	 */
	private TouchableMap touch;

	/**
	 * Download tiles mode
	 */
	private boolean downloadTiles = false;

	/**
	 * Bounding box starting corner
	 */
	private LatLng boundingBoxCorner = null;

	/**
	 * Bounding box polygon
	 */
	private Polygon polygon = null;

	/**
	 * Download Tiles menu
	 */
	private MenuItem downloadTilesMenu;

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

		vibrator = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);

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

		touch = new TouchableMap(getActivity());
		touch.addView(view);

		map.setMyLocationEnabled(true);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		int mapType = settings.getInt(MAP_TYPE_KEY, 1);
		map.setMapType(mapType);

		map.setOnMapLongClickListener(this);

		manager = GeoPackageFactory.getManager(getActivity());
		updateInBackground();

		return touch;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public View getView() {
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
	 * Handle the menu reset
	 * 
	 * @param menu
	 */
	public void handleMenu(Menu menu) {
		resetDownloadTiles(false);
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
		case R.id.download_tiles:
			downloadTilesMenu = item;
			if (!downloadTiles) {
				downloadTiles = true;
				downloadTilesMenu.setIcon(R.drawable.ic_linestring);
			} else {
				resetDownloadTiles(true);
			}
			break;
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
	 * Reset the download tiles state
	 * 
	 * @param createTiles
	 */
	private void resetDownloadTiles(boolean createTiles) {
		downloadTiles = false;
		map.getUiSettings().setScrollGesturesEnabled(true);
		if (downloadTilesMenu != null) {
			downloadTilesMenu.setIcon(R.drawable.ic_tiles);
		}
		if (polygon != null) {
			if (createTiles) {
				createTiles(polygon);
			}
			polygon.remove();
		}
		boundingBoxCorner = null;
		polygon = null;
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

			if (featuresLeft <= 0) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(),
								"Max Features Drawn: " + getMaxFeatures(),
								Toast.LENGTH_SHORT).show();
					}
				});
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

	@Override
	public void onMapLongClick(LatLng point) {
		// When downloading tiles, start a new polygon
		if (downloadTiles) {
			vibrator.vibrate(getActivity().getResources().getInteger(
					R.integer.map_tiles_long_click_vibrate));

			if (polygon != null) {
				polygon.remove();
			}
			map.getUiSettings().setScrollGesturesEnabled(false);
			boundingBoxCorner = point;
			PolygonOptions polygonOptions = new PolygonOptions();
			List<LatLng> points = getPolygonPoints(boundingBoxCorner,
					boundingBoxCorner);
			polygonOptions.addAll(points);
			polygon = map.addPolygon(polygonOptions);
		}
	}

	/**
	 * Get a list of the polygon points for the bounding box
	 * 
	 * @param point1
	 * @param point2
	 * @return
	 */
	private List<LatLng> getPolygonPoints(LatLng point1, LatLng point2) {
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(new LatLng(point1.latitude, point1.longitude));
		points.add(new LatLng(point1.latitude, point2.longitude));
		points.add(new LatLng(point2.latitude, point2.longitude));
		points.add(new LatLng(point2.latitude, point1.longitude));
		return points;
	}

	/**
	 * Touchable map layout
	 * 
	 * @author osbornb
	 */
	public class TouchableMap extends FrameLayout {

		public TouchableMap(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				if (downloadTiles && polygon != null) {
					Point point = new Point((int) ev.getX(), (int) ev.getY());
					LatLng latLng = map.getProjection().fromScreenLocation(
							point);
					List<LatLng> points = getPolygonPoints(boundingBoxCorner,
							latLng);
					polygon.setPoints(points);
				}
				break;
			}
			return super.dispatchTouchEvent(ev);
		}

	}

	/**
	 * Create tiles
	 */
	private void createTiles(Polygon polygon) {

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View createTilesView = inflater
				.inflate(R.layout.map_create_tiles, null);
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setView(createTilesView);

		final EditText geopackageInput = (EditText) createTilesView
				.findViewById(R.id.map_create_tiles_geopackage_input);
		final Button geopackagesButton = (Button) createTilesView
				.findViewById(R.id.map_create_tiles_preloaded);
		final EditText nameInput = (EditText) createTilesView
				.findViewById(R.id.create_tiles_name_input);
		final EditText urlInput = (EditText) createTilesView
				.findViewById(R.id.load_tiles_url_input);
		final Button preloadedUrlsButton = (Button) createTilesView
				.findViewById(R.id.load_tiles_preloaded);
		final EditText minZoomInput = (EditText) createTilesView
				.findViewById(R.id.load_tiles_min_zoom_input);
		final EditText maxZoomInput = (EditText) createTilesView
				.findViewById(R.id.load_tiles_max_zoom_input);
		final Spinner compressFormatInput = (Spinner) createTilesView
				.findViewById(R.id.load_tiles_compress_format);
		final EditText compressQualityInput = (EditText) createTilesView
				.findViewById(R.id.load_tiles_compress_quality);
		final EditText minLatInput = (EditText) createTilesView
				.findViewById(R.id.bounding_box_min_latitude_input);
		final EditText maxLatInput = (EditText) createTilesView
				.findViewById(R.id.bounding_box_max_latitude_input);
		final EditText minLonInput = (EditText) createTilesView
				.findViewById(R.id.bounding_box_min_longitude_input);
		final EditText maxLonInput = (EditText) createTilesView
				.findViewById(R.id.bounding_box_max_longitude_input);
		final Button preloadedLocationsButton = (Button) createTilesView
				.findViewById(R.id.bounding_box_preloaded);

		GeoPackageUtils
				.prepareBoundingBoxInputs(getActivity(), minLatInput,
						maxLatInput, minLonInput, maxLonInput,
						preloadedLocationsButton);

		GeoPackageUtils.prepareTileLoadInputs(getActivity(), minZoomInput,
				maxZoomInput, preloadedUrlsButton, nameInput, urlInput,
				compressFormatInput, compressQualityInput);

		double minLat = 90.0;
		double minLon = 180.0;
		double maxLat = -90.0;
		double maxLon = -180.0;
		for (LatLng point : polygon.getPoints()) {
			minLat = Math.min(minLat, point.latitude);
			minLon = Math.min(minLon, point.longitude);
			maxLat = Math.max(maxLat, point.latitude);
			maxLon = Math.max(maxLon, point.longitude);
		}
		minLatInput.setText(String.valueOf(minLat));
		maxLatInput.setText(String.valueOf(maxLat));
		minLonInput.setText(String.valueOf(minLon));
		maxLonInput.setText(String.valueOf(maxLon));

		geopackagesButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						getActivity(), android.R.layout.select_dialog_item);
				final List<String> databases = manager.databases();
				adapter.addAll(databases);
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle(getActivity()
						.getString(
								R.string.map_create_tiles_existing_geopackage_dialog_label));
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								if (item >= 0) {
									String database = databases.get(item);
									geopackageInput.setText(database);
								}
							}
						});

				AlertDialog alert = builder.create();
				alert.show();
			}
		});

		dialog.setPositiveButton(
				getString(R.string.geopackage_create_tiles_label),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {

						try {

							String database = geopackageInput.getText()
									.toString();
							if (database == null || database.isEmpty()) {
								throw new GeoPackageException(
										getString(R.string.map_create_tiles_geopackage_label)
												+ " is required");
							}
							String tableName = nameInput.getText().toString();
							if (tableName == null || tableName.isEmpty()) {
								throw new GeoPackageException(
										getString(R.string.create_tiles_name_label)
												+ " is required");
							}
							String tileUrl = urlInput.getText().toString();
							int minZoom = Integer.valueOf(minZoomInput
									.getText().toString());
							int maxZoom = Integer.valueOf(maxZoomInput
									.getText().toString());
							double minLat = Double.valueOf(minLatInput
									.getText().toString());
							double maxLat = Double.valueOf(maxLatInput
									.getText().toString());
							double minLon = Double.valueOf(minLonInput
									.getText().toString());
							double maxLon = Double.valueOf(maxLonInput
									.getText().toString());

							if (minLat > maxLat) {
								throw new GeoPackageException(
										getString(R.string.bounding_box_min_latitude_label)
												+ " can not be larger than "
												+ getString(R.string.bounding_box_max_latitude_label));
							}

							if (minLon > maxLon) {
								throw new GeoPackageException(
										getString(R.string.bounding_box_min_longitude_label)
												+ " can not be larger than "
												+ getString(R.string.bounding_box_max_longitude_label));
							}

							CompressFormat compressFormat = null;
							Integer compressQuality = null;
							if (compressFormatInput.getSelectedItemPosition() > 0) {
								compressFormat = CompressFormat
										.valueOf(compressFormatInput
												.getSelectedItem().toString());
								compressQuality = Integer
										.valueOf(compressQualityInput.getText()
												.toString());
							}

							BoundingBox boundingBox = new BoundingBox(minLon,
									maxLon, minLat, maxLat);

							// Create the database if it doesn't exist
							if (!manager.exists(database)) {
								manager.create(database);
							}

							GeoPackageTable table = GeoPackageTable.createTile(
									database, tableName, 0);
							active.addTable(table);

							// Load tiles
							LoadTilesTask.loadTiles(getActivity(),
									GeoPackageMapFragment.this, active,
									database, tableName, tileUrl, minZoom,
									maxZoom, compressFormat, compressQuality,
									boundingBox);
						} catch (Exception e) {
							GeoPackageUtils
									.showMessage(
											getActivity(),
											getString(R.string.geopackage_create_tiles_label),
											e.getMessage());
						}
					}
				}).setNegativeButton(getString(R.string.button_cancel_label),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		dialog.show();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLoadTilesCancelled(String result) {
		if (active.isModified()) {
			updateInBackground();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLoadTilesPostExecute(String result) {
		if (active.isModified()) {
			updateInBackground();
		}

	}

}
