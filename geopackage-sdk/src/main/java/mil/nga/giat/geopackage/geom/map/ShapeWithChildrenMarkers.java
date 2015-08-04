package mil.nga.giat.geopackage.geom.map;

/**
 * Shape markers interface for handling marker changes on shapes that have
 * children
 * 
 * @author osbornb
 */
public interface ShapeWithChildrenMarkers extends ShapeMarkers {

	/**
	 * Create a child shape
	 * 
	 * @return
	 */
	public ShapeMarkers createChild();

}
