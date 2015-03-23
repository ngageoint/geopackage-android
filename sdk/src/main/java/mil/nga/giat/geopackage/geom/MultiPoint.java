package mil.nga.giat.geopackage.geom;

import java.util.List;

/**
 * A restricted form of GeometryCollection where each Geometry in the collection
 * must be of type Point.
 * 
 * @author osbornb
 */
public class MultiPoint extends GeometryCollection<Point> {

	/**
	 * Constructor
	 * 
	 * @param hasZ
	 * @param hasM
	 */
	public MultiPoint(boolean hasZ, boolean hasM) {
		super(GeometryType.MULTIPOINT, hasZ, hasM);
	}

	/**
	 * Get the points
	 * 
	 * @return
	 */
	public List<Point> getPoints() {
		return getGeometries();
	}

	/**
	 * Set the points
	 * 
	 * @param points
	 */
	public void setPoints(List<Point> points) {
		setGeometries(points);
	}

	/**
	 * Add a point
	 * 
	 * @param point
	 */
	public void addPoint(Point point) {
		addGeometry(point);
	}

	/**
	 * Get the number of points
	 * 
	 * @return
	 */
	public int numPoints() {
		return numGeometries();
	}

}
