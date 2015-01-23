package mil.nga.giat.geopackage.geom;

/**
 * GeoPackage Geometry Envelope
 * 
 * @author osbornb
 */
public class GeoPackageGeometryEnvelope {

	/**
	 * Min X
	 */
	private double minX;

	/**
	 * Max X
	 */
	private double maxX;

	/**
	 * Min Y
	 */
	private double minY;

	/**
	 * Max Y
	 */
	private double maxY;

	/**
	 * Min Z
	 */
	private Double minZ;

	/**
	 * Max Z
	 */
	private Double maxZ;

	/**
	 * Min M
	 */
	private Double minM;

	/**
	 * Max M
	 */
	private Double maxM;

	/**
	 * Constructor
	 * 
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 */
	public GeoPackageGeometryEnvelope(double minX, double maxX, double minY,
			double maxY) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}

	public double getMinX() {
		return minX;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public double getMaxX() {
		return maxX;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}

	public Double getMinZ() {
		return minZ;
	}

	public void setMinZ(Double minZ) {
		this.minZ = minZ;
	}

	public Double getMaxZ() {
		return maxZ;
	}

	public void setMaxZ(Double maxZ) {
		this.maxZ = maxZ;
	}

	public Double getMinM() {
		return minM;
	}

	public void setMinM(Double minM) {
		this.minM = minM;
	}

	public Double getMaxM() {
		return maxM;
	}

	public void setMaxM(Double maxM) {
		this.maxM = maxM;
	}

}
