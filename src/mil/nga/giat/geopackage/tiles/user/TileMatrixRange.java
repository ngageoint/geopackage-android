package mil.nga.giat.geopackage.tiles.user;

/**
 * Tile Matrix Range for rows or columns
 * 
 * @author osbornb
 */
public class TileMatrixRange {

	/**
	 * Minimum
	 */
	private final long min;

	/**
	 * Maximum
	 */
	private final long max;

	/**
	 * Constructor
	 * 
	 * @param min
	 * @param max
	 */
	public TileMatrixRange(long min, long max) {
		super();
		this.min = min;
		this.max = max;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

}
