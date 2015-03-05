package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Multiple Polyline Options object
 * 
 * @author osbornb
 */
public class MultiPolylineOptions {

	private List<PolylineOptions> polylineOptions = new ArrayList<PolylineOptions>();

	private PolylineOptions options;

	public void add(PolylineOptions polylineOption) {
		polylineOptions.add(polylineOption);
	}

	public List<PolylineOptions> getPolylineOptions() {
		return polylineOptions;
	}

	public PolylineOptions getOptions() {
		return options;
	}

	public void setOptions(PolylineOptions options) {
		this.options = options;
	}

	public void setPolylineOptions(List<PolylineOptions> polylineOptions) {
		this.polylineOptions = polylineOptions;
	}

}
