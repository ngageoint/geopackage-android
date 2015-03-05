package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiple Polygon Markers object
 * 
 * @author osbornb
 */
public class MultiPolygonMarkers {

	private List<PolygonMarkers> polygonMarkers = new ArrayList<PolygonMarkers>();

	public void add(PolygonMarkers polygonMarker) {
		polygonMarkers.add(polygonMarker);
	}

	public List<PolygonMarkers> getPolygonMarkers() {
		return polygonMarkers;
	}

	public void setPolygonMarkers(List<PolygonMarkers> polygonMarkers) {
		this.polygonMarkers = polygonMarkers;
	}

	/**
	 * Update based upon marker changes
	 */
	public void update() {
		for (PolygonMarkers polygonMarker : polygonMarkers) {
			polygonMarker.update();
		}
	}

	/**
	 * Remove the polygon and points
	 */
	public void remove() {
		for (PolygonMarkers polygonMarker : polygonMarkers) {
			polygonMarker.remove();
		}
	}

}
