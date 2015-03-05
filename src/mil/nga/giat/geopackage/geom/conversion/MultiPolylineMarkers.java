package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiple Polyline Markers object
 * 
 * @author osbornb
 */
public class MultiPolylineMarkers {

	private List<PolylineMarkers> polylineMarkers = new ArrayList<PolylineMarkers>();

	public void add(PolylineMarkers polylineMarker) {
		polylineMarkers.add(polylineMarker);
	}

	public List<PolylineMarkers> getPolylineMarkers() {
		return polylineMarkers;
	}

	public void setPolylineMarkers(List<PolylineMarkers> polylineMarkers) {
		this.polylineMarkers = polylineMarkers;
	}

	/**
	 * Update based upon marker changes
	 */
	public void update() {
		for (PolylineMarkers polylineMarker : polylineMarkers) {
			polylineMarker.update();
		}
	}

	/**
	 * Remove the polyline and points
	 */
	public void remove() {
		for (PolylineMarkers polylineMarker : polylineMarkers) {
			polylineMarker.remove();
		}
	}

}
