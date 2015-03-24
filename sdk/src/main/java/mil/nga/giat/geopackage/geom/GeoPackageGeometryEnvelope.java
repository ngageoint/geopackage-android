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
	 * True if has z coordinates
	 */
	private final boolean hasZ;

	/**
	 * Min Z
	 */
	private Double minZ;

	/**
	 * Max Z
	 */
	private Double maxZ;

	/**
	 * True if has M measurements
	 */
	private final boolean hasM;

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
	 * @param hasZ
	 * @param hasM
	 */
	public GeoPackageGeometryEnvelope(boolean hasZ, boolean hasM) {
		this.hasZ = hasZ;
		this.hasM = hasM;
	}

	/**
	 * True if has Z coordinates
	 * 
	 * @return
	 */
	public boolean hasZ() {
		return hasZ;
	}

	/**
	 * True if has M measurements
	 * 
	 * @return
	 */
	public boolean hasM() {
		return hasM;
	}

	public double getMinX() {
		return minX;
	}

	/**
	 * Get the envelope flag indicator
	 * 
	 * 1 for xy, 2 for xyz, 3 for xym, 4 for xyzm (null would be 0)
	 * 
	 * @return
	 */
	public int getIndicator() {
		int indicator = 1;
		if (hasZ()) {
			indicator++;
		}
		if (hasM()) {
			indicator += 2;
		}
		return indicator;
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
