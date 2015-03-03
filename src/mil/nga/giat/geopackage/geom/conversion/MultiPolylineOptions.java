package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.PolylineOptions;

public class MultiPolylineOptions {

	private List<PolylineOptions> polylineOptions = new ArrayList<PolylineOptions>();

	public void add(PolylineOptions polylineOption) {
		polylineOptions.add(polylineOption);
	}

	public List<PolylineOptions> getPolylineOptions() {
		return polylineOptions;
	}

}
