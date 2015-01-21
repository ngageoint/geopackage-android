package mil.nga.giat.geopackage;

import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemSfSqlDao;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemSqlMmDao;
import mil.nga.giat.geopackage.data.c2.ContentsDao;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

/**
 * A single GeoPackage database connection
 * 
 * @author osbornb
 */
public interface GeoPackage {

	/**
	 * Close the GeoPackage connection
	 */
	public void close();

	/**
	 * Get the SQLite database
	 * 
	 * @return
	 */
	public SQLiteDatabase getDatabase();

	/**
	 * Get the connection source
	 * 
	 * @return
	 */
	public ConnectionSource getConnectionSource();

	/**
	 * Get a Spatial Reference System DAO
	 * 
	 * @return
	 * @throws GeoPackageException
	 */
	public SpatialReferenceSystemDao getSpatialReferenceSystemDao()
			throws GeoPackageException;

	/**
	 * Get a SQL/MM Spatial Reference System DAO
	 * 
	 * @return
	 * @throws GeoPackageException
	 */
	public SpatialReferenceSystemSqlMmDao getSpatialReferenceSystemSqlMmDao()
			throws GeoPackageException;

	/**
	 * Get a SF/SQL Spatial Reference System DAO
	 * 
	 * @return
	 * @throws GeoPackageException
	 */
	public SpatialReferenceSystemSfSqlDao getSpatialReferenceSystemSfSqlDao()
			throws GeoPackageException;

	/**
	 * Get a Contents DAO
	 * 
	 * @return
	 * @throws GeoPackageException
	 */
	public ContentsDao getContentsDao() throws GeoPackageException;

	/**
	 * Get a Geometry Columns DAO
	 * 
	 * @return
	 * @throws GeoPackageException
	 */
	public GeometryColumnsDao getGeometryColumnsDao()
			throws GeoPackageException;

	/**
	 * Create the Geometry Columns table if it does not already exist
	 * 
	 * @return true if created
	 * @throws GeoPackageException
	 */
	public boolean createGeometryColumnsTable() throws GeoPackageException;

}
