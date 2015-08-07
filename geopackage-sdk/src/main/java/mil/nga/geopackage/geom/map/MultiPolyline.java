package mil.nga.geopackage.geom.map;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.Polyline;

/**
 * Multiple Polyline object
 * 
 * @author osbornb
 */
public class MultiPolyline {

	private List<Polyline> polylines = new ArrayList<Polyline>();

	public void add(Polyline polyline) {
		polylines.add(polyline);
	}

	public List<Polyline> getPolylines() {
		return polylines;
	}

	public void setPolylines(List<Polyline> polylines) {
		this.polylines = polylines;
	}

	/**
	 * Remove from the map
	 */
	public void remove() {
		for (Polyline polyline : polylines) {
			polyline.remove();
		}
	}

}
