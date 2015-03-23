package mil.nga.giat.geopackage.tiles.matrix;

/**
 * Tile Matrix complex primary key
 * 
 * @author osbornb
 */
public class TileMatrixKey {

	/**
	 * Table name
	 */
	private String tableName;

	/**
	 * Zoom level
	 */
	private long zoomLevel;

	/**
	 * Constructor
	 * 
	 * @param tableName
	 * @param zoomLevel
	 */
	public TileMatrixKey(String tableName, long zoomLevel) {
		this.tableName = tableName;
		this.zoomLevel = zoomLevel;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public long getZoomLevel() {
		return zoomLevel;
	}

	public void setZoomLevel(long zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return tableName + ":" + zoomLevel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
		result = prime * result + (int) (zoomLevel ^ (zoomLevel >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TileMatrixKey other = (TileMatrixKey) obj;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		if (zoomLevel != other.zoomLevel)
			return false;
		return true;
	}

}
