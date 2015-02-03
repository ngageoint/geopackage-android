package mil.nga.giat.geopackage;

/**
 * GeoPackage exception
 * 
 * @author osbornb
 */
public class GeoPackageException extends RuntimeException {

	/**
	 * Serial version id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public GeoPackageException() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public GeoPackageException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 * @param throwable
	 */
	public GeoPackageException(String message, Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * Constructor
	 * 
	 * @param throwable
	 */
	public GeoPackageException(Throwable throwable) {
		super(throwable);
	}

}
