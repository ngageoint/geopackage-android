package mil.nga.giat.geopackage;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import mil.nga.giat.geopackage.io.GeoPackageProgress;

/**
 * GeoPackage Database management
 * 
 * @author osbornb
 */
public interface GeoPackageManager {

	/**
	 * List all GeoPackage databases sorted alphabetically
	 * 
	 * @return database list
	 */
	public List<String> databases();

	/**
	 * Get the count of GeoPackage databases
	 * 
	 * @return
	 */
	public int count();

	/**
	 * Set of all GeoPackage databases
	 * 
	 * @return database set
	 */
	public Set<String> databaseSet();

	/**
	 * Determine if the database exists
	 * 
	 * @param database
	 * @return true if exists
	 */
	public boolean exists(String database);

	/**
	 * Size of the database in bytes
	 * 
	 * @param database
	 * @return
	 */
	public long size(String database);

	/**
	 * Determine if the database is a linked external file
	 * 
	 * @param database
	 * @return
	 */
	public boolean isExternal(String database);

	/**
	 * Get the path of the database
	 * 
	 * @param database
	 * @return
	 */
	public String getPath(String database);

	/**
	 * Get the file of the database
	 * 
	 * @param database
	 * @return
	 */
	public File getFile(String database);

	/**
	 * Get a readable version of the database size
	 * 
	 * @param database
	 * @return
	 */
	public String readableSize(String database);

	/**
	 * Delete a database
	 * 
	 * @param database
	 * @return true if deleted
	 */
	public boolean delete(String database);

	/**
	 * Create a new GeoPackage database
	 * 
	 * @param database
	 * @return
	 */
	public boolean create(String database);

	/**
	 * Import a GeoPackage file
	 * 
	 * @param file
	 *            GeoPackage file to import
	 * @return true if loaded
	 */
	public boolean importGeoPackage(File file);

	/**
	 * Import a GeoPackage file
	 * 
	 * @param file
	 *            GeoPackage file to import
	 * @param override
	 *            true to override existing
	 * @return true if created successfully
	 */
	public boolean importGeoPackage(File file, boolean override);

	/**
	 * Import a GeoPackage stream
	 * 
	 * @param database
	 *            database name to save as
	 * @param stream
	 *            GeoPackage stream to import
	 * @return true if loaded
	 */
	public boolean importGeoPackage(String database, InputStream stream);

	/**
	 * Import a GeoPackage stream
	 * 
	 * @param database
	 *            database name to save as
	 * @param stream
	 *            GeoPackage stream to import
	 * @param override
	 *            true to override existing
	 * @return true if created successfully
	 */
	public boolean importGeoPackage(String database, InputStream stream,
			boolean override);

	/**
	 * Import a GeoPackage file
	 * 
	 * @param name
	 *            database name to save as
	 * @param file
	 *            GeoPackage file to import
	 * @return true if created successfully
	 */
	public boolean importGeoPackage(String name, File file);

	/**
	 * Import a GeoPackage file
	 * 
	 * @param name
	 *            database name to save the imported file as
	 * @param file
	 *            GeoPackage file to import
	 * @param override
	 *            true to override existing
	 * @return true if created successfully
	 */
	public boolean importGeoPackage(String name, File file, boolean override);

	/**
	 * Import a GeoPackage file from a URL
	 * 
	 * @param name
	 * @param url
	 * @return true if created successfully
	 */
	public boolean importGeoPackage(String name, URL url);

	/**
	 * Import a GeoPackage file from a URL
	 * 
	 * @param name
	 * @param url
	 * @param progress
	 * @return true if created successfully
	 */
	public boolean importGeoPackage(String name, URL url,
			GeoPackageProgress progress);

	/**
	 * Import a GeoPackage file from a URL
	 * 
	 * @param name
	 * @param url
	 * @param override
	 * @return true if created successfully
	 */
	public boolean importGeoPackage(String name, URL url, boolean override);

	/**
	 * Import a GeoPackage file from a URL
	 * 
	 * @param name
	 * @param url
	 * @param override
	 * @param progress
	 * @return true if created successfully
	 */
	public boolean importGeoPackage(String name, URL url, boolean override,
			GeoPackageProgress progress);

	/**
	 * Export a GeoPackage database to a file
	 * 
	 * @param database
	 * @param directory
	 */
	public void exportGeoPackage(String database, File directory);

	/**
	 * Export a GeoPackage database to a file
	 * 
	 * @param database
	 * @param name
	 * @param directory
	 */
	public void exportGeoPackage(String database, String name, File directory);

	/**
	 * Open the database
	 * 
	 * @param database
	 * @return
	 */
	public GeoPackage open(String database);

	/**
	 * Copy the database
	 * 
	 * @param database
	 * @param databaseCopy
	 * @return
	 */
	public boolean copy(String database, String databaseCopy);

	/**
	 * Rename the database to the new name
	 * 
	 * @param database
	 * @param newDatabase
	 * @return
	 */
	public boolean rename(String database, String newDatabase);

	/**
	 * Import an GeoPackage as an external file link without copying locally
	 * 
	 * @param path
	 *            full file path
	 * @param database
	 *            name to reference the database
	 * @return true if imported successfully
	 */
	public boolean importGeoPackageAsExternalLink(File path, String database);

	/**
	 * Import an GeoPackage as an external file link without copying locally
	 * 
	 * @param path
	 *            full file path
	 * @param database
	 *            name to reference the database
	 * @return true if imported successfully
	 */
	public boolean importGeoPackageAsExternalLink(String path, String database);

}
