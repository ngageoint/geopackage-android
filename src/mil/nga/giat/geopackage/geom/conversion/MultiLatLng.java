package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Multiple LatLng object
 * 
 * @author osbornb
 */
public class MultiLatLng {

	private List<LatLng> latLngs = new ArrayList<LatLng>();

	private MarkerOptions markerOptions;

	public void add(LatLng latLng) {
		latLngs.add(latLng);
	}

	public List<LatLng> getLatLngs() {
		return latLngs;
	}

	public MarkerOptions getMarkerOptions() {
		return markerOptions;
	}

	public void setMarkerOptions(MarkerOptions markerOptions) {
		this.markerOptions = markerOptions;
	}

	public void setLatLngs(List<LatLng> latLngs) {
		this.latLngs = latLngs;
	}

}
