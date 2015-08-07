package mil.nga.geopackage.geom.map;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.Marker;

/**
 * Multiple Marker object
 * 
 * @author osbornb
 */
public class MultiMarker implements ShapeMarkers {

	private List<Marker> markers = new ArrayList<Marker>();

	public void add(Marker marker) {
		markers.add(marker);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Marker> getMarkers() {
		return markers;
	}

	public void setMarkers(List<Marker> markers) {
		this.markers = markers;
	}

	/**
	 * Remove from the map
	 */
	public void remove() {
		for (Marker marker : markers) {
			marker.remove();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Marker marker) {
		if (markers.remove(marker)) {
			marker.remove();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addNew(Marker marker) {
		add(marker);
	}

}
