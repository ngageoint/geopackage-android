package mil.nga.giat.geopackage.geom.unit;

import java.util.HashMap;
import java.util.Map;

import org.osgeo.proj4j.CoordinateReferenceSystem;

/**
 * Single EPSG Projection
 * 
 * @author osbornb
 */
public class Projection {

	/**
	 * EPSG code
	 */
	private final long epsg;

	/**
	 * Coordinate Reference System
	 */
	private final CoordinateReferenceSystem crs;

	/**
	 * Mapping of transformations to other EPSG codes
	 */
	private final Map<Long, ProjectionTransform> transforms = new HashMap<Long, ProjectionTransform>();

	/**
	 * Constructor
	 * 
	 * @param epsg
	 * @param crs
	 */
	Projection(long epsg, CoordinateReferenceSystem crs) {
		this.epsg = epsg;
		this.crs = crs;
	}

	/**
	 * Get the EPSG code
	 * 
	 * @return
	 */
	public long getEpsg() {
		return epsg;
	}

	/**
	 * Get the Coordinate Reference System
	 * 
	 * @return
	 */
	public CoordinateReferenceSystem getCrs() {
		return crs;
	}

	/**
	 * Get the transformation from this Projection to the EPSG code
	 * 
	 * @param epsg
	 * @return
	 */
	public ProjectionTransform getTransformation(long epsg) {

		ProjectionTransform transform = transforms.get(epsg);

		if (transform == null) {
			Projection projectionTo = ProjectionFactory.getProjection(epsg);
			transform = new ProjectionTransform(this, projectionTo);

			transforms.put(epsg, transform);
		}

		return transform;
	}

	/**
	 * Convert the value to meters
	 * 
	 * @param value
	 * @return
	 */
	public double toMeters(double value) {
		return value / crs.getProjection().getFromMetres();
	}

}
