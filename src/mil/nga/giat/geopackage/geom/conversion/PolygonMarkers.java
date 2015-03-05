package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;

/**
 * Polygon with Markers object
 * 
 * @author osbornb
 */
public class PolygonMarkers {

	private Polygon polygon;

	private List<Marker> markers = new ArrayList<Marker>();

	private List<List<Marker>> holes = new ArrayList<List<Marker>>();

	public Polygon getPolygon() {
		return polygon;
	}

	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
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

	public void addHole(List<Marker> hole) {
		holes.add(hole);
	}

	public List<List<Marker>> getHoles() {
		return holes;
	}

	public void setHoles(List<List<Marker>> holes) {
		this.holes = holes;
	}

	/**
	 * Update based upon marker changes
	 */
	public void update() {
		if (polygon != null) {
			GoogleMapShapeConverter converter = new GoogleMapShapeConverter();

			List<LatLng> points = converter.getPointsFromMarkers(markers);
			polygon.setPoints(points);

			List<List<LatLng>> holePointList = new ArrayList<List<LatLng>>();
			for (List<Marker> hole : holes) {
				List<LatLng> holePoints = converter.getPointsFromMarkers(hole);
				holePointList.add(holePoints);
			}
			polygon.setHoles(holePointList);
		}
	}

	/**
	 * Remove from the map
	 */
	public void remove() {
		if (polygon != null) {
			polygon.remove();
		}
		for (Marker marker : markers) {
			marker.remove();
		}
		for (List<Marker> hole : holes) {
			for (Marker marker : hole) {
				marker.remove();
			}
		}
	}

}
