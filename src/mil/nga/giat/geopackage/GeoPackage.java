package mil.nga.giat.geopackage;

import java.sql.SQLException;

import mil.nga.giat.geopackage.data.c1.SfSqlSpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c1.SqlMmSpatialReferenceSystemDao;
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
	 * @throws SQLException
	 */
	public SpatialReferenceSystemDao getSpatialReferenceSystemDao()
			throws SQLException;

	/**
	 * Get a SQL/MM Spatial Reference System DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	public SqlMmSpatialReferenceSystemDao getSpatialReferenceSystemSqlMmDao()
			throws SQLException;

	/**
	 * Get a SF/SQL Spatial Reference System DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	public SfSqlSpatialReferenceSystemDao getSpatialReferenceSystemSfSqlDao()
			throws SQLException;

}
