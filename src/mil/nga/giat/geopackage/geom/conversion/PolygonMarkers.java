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

	private List<PolygonHoleMarkers> holes = new ArrayList<PolygonHoleMarkers>();

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

	public void addHole(PolygonHoleMarkers hole) {
		holes.add(hole);
	}

	public List<PolygonHoleMarkers> getHoles() {
		return holes;
	}

	public void setHoles(List<PolygonHoleMarkers> holes) {
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
			for (PolygonHoleMarkers hole : holes) {
				List<LatLng> holePoints = converter.getPointsFromMarkers(hole
						.getMarkers());
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
		for (PolygonHoleMarkers hole : holes) {
			hole.remove();
		}
	}

	/**
	 * Is it valid
	 * 
	 * @return
	 */
	public boolean isValid() {
		boolean valid = markers.isEmpty() || markers.size() >= 3;
		if (valid) {
			for (PolygonHoleMarkers hole : holes) {
				valid = hole.isValid();
				if (!valid) {
					break;
				}
			}
		}
		return valid;
	}

	/**
	 * Is it deleted
	 * 
	 * @return
	 */
	public boolean isDeleted() {
		return markers.isEmpty();
	}

}
