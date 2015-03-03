package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class MultiLatLng {

	private List<LatLng> latLngs = new ArrayList<LatLng>();

	public void add(LatLng latLng) {
		latLngs.add(latLng);
	}

	public List<LatLng> getLatLngs() {
		return latLngs;
	}

}
