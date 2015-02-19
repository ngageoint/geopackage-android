package mil.nga.giat.geopackage.io;

/**
 * GeoPackage Progress interface for receiving progress information and handling
 * cancellations
 * 
 * @author osbornb
 */
public interface GeoPackageProgress {

	/**
	 * Is the process still active
	 * 
	 * @return true if active, false if cancelled
	 */
	public boolean isActive();

	/**
	 * Set the max progress value
	 * 
	 * @param max
	 */
	public void setMax(int max);

	/**
	 * Add to the total progress
	 * 
	 * @param progress
	 */
	public void addProgress(int progress);

}
