package mil.nga.giat.geopackage.geom.conversion;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.Polygon;

/**
 * Multiple Polygon object
 * 
 * @author osbornb
 */
public class MultiPolygon {

	private List<Polygon> polygons = new ArrayList<Polygon>();

	public void add(Polygon polygon) {
		polygons.add(polygon);
	}

	public List<Polygon> getPolygons() {
		return polygons;
	}

	public void setPolygons(List<Polygon> polygons) {
		this.polygons = polygons;
	}

	/**
	 * Remove from the map
	 */
	public void remove() {
		for (Polygon polygon : polygons) {
			polygon.remove();
		}
	}

}
