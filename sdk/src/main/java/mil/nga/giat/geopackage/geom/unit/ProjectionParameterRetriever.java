package mil.nga.giat.geopackage.geom.unit;

/**
 * Interface for retrieving the proj4 projection parameter string for an EPSG
 * code
 * 
 * @author osbornb
 */
public interface ProjectionParameterRetriever {

	/**
	 * Get the proj4 projection string for the EPSG code
	 * 
	 * @param epsg
	 * @return
	 */
	public String getProjection(long epsg);

}
