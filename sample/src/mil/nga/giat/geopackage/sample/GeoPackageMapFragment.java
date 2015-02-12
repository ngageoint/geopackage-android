package mil.nga.giat.geopackage.sample;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class GeoPackageMapFragment extends Fragment {

	public static GeoPackageMapFragment newInstance() {
		GeoPackageMapFragment mapFragment = new GeoPackageMapFragment();
		return mapFragment;
	}

	private GoogleMap map;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_map, container, false);
		map = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.fragment_map_view_ui)).getMap();

		map.setMyLocationEnabled(true);

		return view;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		map.setMyLocationEnabled(!hidden);
	}

}
