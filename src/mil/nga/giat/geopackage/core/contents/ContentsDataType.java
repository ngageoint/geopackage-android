package mil.nga.giat.geopackage.core.contents;

/**
 * Contents data type enumeration
 * 
 * @author osbornb
 */
public enum ContentsDataType {

	/**
	 * Features
	 */
	FEATURES("features"),

	/**
	 * Tiles
	 */
	TILES("tiles");

	/**
	 * Data type name
	 */
	private final String name;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	private ContentsDataType(String name) {
		this.name = name;
	}

	/**
	 * Get the name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the Data Type from the name
	 * 
	 * @param name
	 * @return
	 */
	public static ContentsDataType fromName(String name) {
		ContentsDataType dataType = null;
		if (name != null) {
			for (ContentsDataType type : ContentsDataType.values()) {
				if (name.equals(type.getName())) {
					dataType = type;
					break;
				}
			}
		}
		return dataType;
	}

}
