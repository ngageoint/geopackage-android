package mil.nga.giat.geopackage.geom.conversion;

import java.util.List;

import com.google.android.gms.maps.model.Marker;

/**
 * Shape markers interface for handling marker changes
 * 
 * @author osbornb
 */
public interface ShapeMarkers {

	/**
	 * Get all markers
	 * 
	 * @return
	 */
	public List<Marker> getMarkers();

	/**
	 * Delete the marker
	 * 
	 * @param marker
	 */
	public void delete(Marker marker);

	/**
	 * Add the marker
	 * 
	 * @param marker
	 */
	public void addNew(Marker marker);

}
