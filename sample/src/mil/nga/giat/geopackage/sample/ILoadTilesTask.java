package mil.nga.giat.geopackage.sample;

/**
 * Interface for load tile callbacks
 * 
 * @author osbornb
 */
public interface ILoadTilesTask {

	/**
	 * On cancellation of loading tiles
	 * 
	 * @param result
	 */
	public void onLoadTilesCancelled(String result);

	/**
	 * On completion of loading tiles
	 * 
	 * @param result
	 */
	public void onLoadTilesPostExecute(String result);

}
