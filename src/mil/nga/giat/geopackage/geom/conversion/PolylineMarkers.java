package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

/**
 * Polyline with Markers object
 * 
 * @author osbornb
 */
public class PolylineMarkers {

	private Polyline polyline;

	private List<Marker> markers = new ArrayList<Marker>();

	public Polyline getPolyline() {
		return polyline;
	}

	public void setPolyline(Polyline polyline) {
		this.polyline = polyline;
	}

	public void add(Marker marker) {
		markers.add(marker);
	}

	public List<Marker> getMarkers() {
		return markers;
	}

	public void setMarkers(List<Marker> markers) {
		this.markers = markers;
	}

	/**
	 * Update based upon marker changes
	 */
	public void update() {
		if (polyline != null) {
			List<LatLng> points = new GoogleMapShapeConverter()
					.getPointsFromMarkers(markers);
			polyline.setPoints(points);
		}
	}

	/**
	 * Remove from the map
	 */
	public void remove() {
		if (polyline != null) {
			polyline.remove();
		}
		for (Marker marker : markers) {
			marker.remove();
		}
	}

}
