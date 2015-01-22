package mil.nga.giat.geopackage;

import java.sql.SQLException;

import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemSfSqlDao;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemSqlMmDao;
import mil.nga.giat.geopackage.data.c2.Contents;
import mil.nga.giat.geopackage.data.c2.ContentsDao;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.data.c4.FeatureDao;
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
	 */
	public SpatialReferenceSystemDao getSpatialReferenceSystemDao();

	/**
	 * Get a SQL/MM Spatial Reference System DAO
	 * 
	 * @return
	 */
	public SpatialReferenceSystemSqlMmDao getSpatialReferenceSystemSqlMmDao();

	/**
	 * Get a SF/SQL Spatial Reference System DAO
	 * 
	 * @return
	 */
	public SpatialReferenceSystemSfSqlDao getSpatialReferenceSystemSfSqlDao();

	/**
	 * Get a Contents DAO
	 * 
	 * @return
	 */
	public ContentsDao getContentsDao();

	/**
	 * Get a Geometry Columns DAO
	 * 
	 * @return
	 */
	public GeometryColumnsDao getGeometryColumnsDao();

	/**
	 * Create the Geometry Columns table if it does not already exist
	 * 
	 * @return true if created
	 */
	public boolean createGeometryColumnsTable();

	/**
	 * Get a Feature DAO from Geometry Columns
	 * 
	 * @param geometryColumns
	 * @return
	 */
	public FeatureDao getFeatureDao(GeometryColumns geometryColumns);

	/**
	 * Get a Feature DAO from Contents
	 * 
	 * @param contents
	 * @return
	 */
	public FeatureDao getFeatureDao(Contents contents);

	/**
	 * Get a Feature DAO from a table name
	 * 
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public FeatureDao getFeatureDao(String tableName) throws SQLException;

}
