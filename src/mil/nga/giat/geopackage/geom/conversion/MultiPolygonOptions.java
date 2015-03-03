package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.PolygonOptions;

public class MultiPolygonOptions {

	private List<PolygonOptions> polygonOptions = new ArrayList<PolygonOptions>();

	public void add(PolygonOptions polygonOption) {
		polygonOptions.add(polygonOption);
	}

	public List<PolygonOptions> getPolygonOptions() {
		return polygonOptions;
	}

}
