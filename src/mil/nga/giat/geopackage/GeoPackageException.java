package mil.nga.giat.geopackage;

public class GeoPackageException extends Exception {

	/**
	 * Serial version id
	 */
	private static final long serialVersionUID = 1L;

	public GeoPackageException() {
		super();
	}

	public GeoPackageException(String message) {
		super(message);
	}

	public GeoPackageException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public GeoPackageException(Throwable throwable) {
		super(throwable);
	}

}
