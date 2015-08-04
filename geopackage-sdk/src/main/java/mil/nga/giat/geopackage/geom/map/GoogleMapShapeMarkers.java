package mil.nga.giat.geopackage.geom.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

/**
 * Google Map Shape with markers
 * 
 * @author osbornb
 */
public class GoogleMapShapeMarkers {

	/**
	 * Shape
	 */
	private GoogleMapShape shape;

	/**
	 * Map between marker ids and shape markers they belong to (or null for non
	 * shapes)
	 */
	private Map<String, ShapeMarkers> shapeMarkersMap = new HashMap<String, ShapeMarkers>();

	/**
	 * Add the marker to the shape
	 * 
	 * @param marker
	 * @param shapeMarkers
	 */
	public void add(Marker marker, ShapeMarkers shapeMarkers) {
		add(marker.getId(), shapeMarkers);
	}

	/**
	 * Add the marker id to the shape
	 * 
	 * @param markerId
	 * @param shapeMarkers
	 */
	public void add(String markerId, ShapeMarkers shapeMarkers) {
		shapeMarkersMap.put(markerId, shapeMarkers);
	}

	/**
	 * Add all markers in the shape
	 * 
	 * @param shapeMarkers
	 */
	public void add(ShapeMarkers shapeMarkers) {
		for (Marker marker : shapeMarkers.getMarkers()) {
			add(marker, shapeMarkers);
		}
	}

	/**
	 * Add a marker with no shape
	 * 
	 * @param marker
	 */
	public void add(Marker marker) {
		add(marker, null);
	}

	/**
	 * Add a list of markers with no shape
	 * 
	 * @param markers
	 */
	public void add(List<Marker> markers) {
		for (Marker marker : markers) {
			add(marker);
		}
	}

	/**
	 * Add an embedded shape markers
	 * 
	 * @param googleShapeMarkers
	 */
	public void add(GoogleMapShapeMarkers googleShapeMarkers) {
		shapeMarkersMap.putAll(googleShapeMarkers.shapeMarkersMap);
	}

	public GoogleMapShape getShape() {
		return shape;
	}

	public void setShape(GoogleMapShape shape) {
		this.shape = shape;
	}

	/**
	 * Check if contains the marker
	 * 
	 * @param marker
	 * @return
	 */
	public boolean contains(Marker marker) {
		return contains(marker.getId());
	}

	/**
	 * Check if contains the marker id
	 * 
	 * @param markerId
	 * @return
	 */
	public boolean contains(String markerId) {
		return shapeMarkersMap.containsKey(markerId);
	}

	/**
	 * Get the shape markers for a marker, only returns a value of shapes that
	 * can be edited
	 * 
	 * @param marker
	 * @return
	 */
	public ShapeMarkers getShapeMarkers(Marker marker) {
		return getShapeMarkers(marker.getId());
	}

	/**
	 * Get the shape markers for a marker id, only returns a value of shapes
	 * that can be edited
	 * 
	 * @param markerId
	 * @return
	 */
	public ShapeMarkers getShapeMarkers(String markerId) {
		return shapeMarkersMap.get(markerId);
	}

	/**
	 * Get the shape markers for a marker, only returns a value of shapes that
	 * can be edited
	 * 
	 * @param marker
	 * @return
	 */
	public boolean delete(Marker marker) {
		boolean deleted = false;
		if (contains(marker)) {
			deleted = true;
			ShapeMarkers shapeMarkers = shapeMarkersMap.remove(marker.getId());
			if (shapeMarkers != null) {
				shapeMarkers.delete(marker);
			}
			marker.remove();
		}
		return deleted;
	}

	/**
	 * Removes all objects added to the map
	 */
	public void remove() {
		if (shape != null) {
			shape.remove();
		}
	}

	/**
	 * Updates all objects that could have changed from moved markers
	 */
	public void update() {
		if (shape != null) {
			shape.update();
		}
	}

	/**
	 * Determines if the shape is in a valid state
	 */
	public boolean isValid() {
		boolean valid = true;
		if (shape != null) {
			valid = shape.isValid();
		}
		return valid;
	}

	/**
	 * Polygon add a marker in the list of markers to where it is closest to the
	 * the surrounding points
	 * 
	 * @param marker
	 * @param markers
	 */
	public static void addMarkerAsPolygon(Marker marker, List<Marker> markers) {
		LatLng position = marker.getPosition();
		int insertLocation = markers.size();
		if (markers.size() > 2) {
			double[] distances = new double[markers.size()];
			insertLocation = 0;
			distances[0] = SphericalUtil.computeDistanceBetween(position,
					markers.get(0).getPosition());
			for (int i = 1; i < markers.size(); i++) {
				distances[i] = SphericalUtil.computeDistanceBetween(position,
						markers.get(i).getPosition());
				if (distances[i] < distances[insertLocation]) {
					insertLocation = i;
				}
			}

			int beforeLocation = insertLocation > 0 ? insertLocation - 1
					: distances.length - 1;
			int afterLocation = insertLocation < distances.length - 1 ? insertLocation + 1
					: 0;

			if (distances[beforeLocation] > distances[afterLocation]) {
				insertLocation = afterLocation;
			}

		}
		markers.add(insertLocation, marker);
	}

	/**
	 * Polyline add a marker in the list of markers to where it is closest to
	 * the the surrounding points
	 * 
	 * @param marker
	 * @param markers
	 */
	public static void addMarkerAsPolyline(Marker marker, List<Marker> markers) {
		LatLng position = marker.getPosition();
		int insertLocation = markers.size();
		if (markers.size() > 1) {
			double[] distances = new double[markers.size()];
			insertLocation = 0;
			distances[0] = SphericalUtil.computeDistanceBetween(position,
					markers.get(0).getPosition());
			for (int i = 1; i < markers.size(); i++) {
				distances[i] = SphericalUtil.computeDistanceBetween(position,
						markers.get(i).getPosition());
				if (distances[i] < distances[insertLocation]) {
					insertLocation = i;
				}
			}

			Integer beforeLocation = insertLocation > 0 ? insertLocation - 1
					: null;
			Integer afterLocation = insertLocation < distances.length - 1 ? insertLocation + 1
					: null;

			if (beforeLocation != null && afterLocation != null) {
				if (distances[beforeLocation] > distances[afterLocation]) {
					insertLocation = afterLocation;
				}
			} else if (beforeLocation != null) {
				if (distances[beforeLocation] >= SphericalUtil
						.computeDistanceBetween(markers.get(beforeLocation)
								.getPosition(), markers.get(insertLocation)
								.getPosition())) {
					insertLocation++;
				}
			} else {
				if (distances[afterLocation] < SphericalUtil
						.computeDistanceBetween(markers.get(afterLocation)
								.getPosition(), markers.get(insertLocation)
								.getPosition())) {
					insertLocation++;
				}
			}

		}
		markers.add(insertLocation, marker);
	}

}
