package mil.nga.giat.geopackage.tiles;

/**
 * Tile gride with x and y ranges
 * 
 * @author osbornb
 */
public class TileGrid {

	/**
	 * Min x
	 */
	private int minX;

	/**
	 * Max x
	 */
	private int maxX;

	/**
	 * Min y
	 */
	private int minY;

	/**
	 * Max y
	 */
	private int maxY;

	/**
	 * Constructor
	 * 
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 */
	public TileGrid(int minX, int maxX, int minY, int maxY) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}

	public int getMinX() {
		return minX;
	}

	public void setMinX(int minX) {
		this.minX = minX;
	}

	public int getMaxX() {
		return maxX;
	}

	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}

	public int getMinY() {
		return minY;
	}

	public void setMinY(int minY) {
		this.minY = minY;
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

}
