package mil.nga.giat.geopackage.tiles;

/**
 * Tile grid with x and y ranges
 * 
 * @author osbornb
 */
public class TileGrid {

	/**
	 * Min x
	 */
	private long minX;

	/**
	 * Max x
	 */
	private long maxX;

	/**
	 * Min y
	 */
	private long minY;

	/**
	 * Max y
	 */
	private long maxY;

	/**
	 * Constructor
	 * 
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 * @param width
	 * @param height
	 */
	public TileGrid(long minX, long maxX, long minY, long maxY) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}

	public long getMinX() {
		return minX;
	}

	public void setMinX(long minX) {
		this.minX = minX;
	}

	public long getMaxX() {
		return maxX;
	}

	public void setMaxX(long maxX) {
		this.maxX = maxX;
	}

	public long getMinY() {
		return minY;
	}

	public void setMinY(long minY) {
		this.minY = minY;
	}

	public long getMaxY() {
		return maxY;
	}

	public void setMaxY(long maxY) {
		this.maxY = maxY;
	}

	/**
	 * Get the count of tiles in the grid
	 * 
	 * @return
	 */
	public long count() {
		return ((maxX + 1) - minX) * ((maxY + 1) - minY);
	}

}
