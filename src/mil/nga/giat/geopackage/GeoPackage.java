package mil.nga.giat.geopackage;

import java.sql.SQLException;

import mil.nga.giat.geopackage.data.c1.SfSqlSpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SfSqlSpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemDao;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

/**
 * A single GeoPackage database connection
 * 
 * @author osbornb
 */
public class GeoPackage {

	/**
	 * SQLite database
	 */
	private final SQLiteDatabase database;

	/**
	 * Connection source for creating data access objects
	 */
	private final ConnectionSource connectionSource;

	/**
	 * Constructor
	 * 
	 * @param database
	 */
	GeoPackage(SQLiteDatabase database) {
		this.database = database;
		connectionSource = new AndroidConnectionSource(database);
	}

	/**
	 * Close the GeoPackage connection
	 */
	public void close() {
		connectionSource.closeQuietly();
		database.close();
	}

	/**
	 * Get the SQLite database
	 * 
	 * @return
	 */
	public SQLiteDatabase getDatabase() {
		return database;
	}

	/**
	 * Get the connection source
	 * 
	 * @return
	 */
	public ConnectionSource getConnectionSource() {
		return connectionSource;
	}

	/**
	 * Get a Spatial Reference System DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	public SpatialReferenceSystemDao spatialReferenceSystemDao()
			throws SQLException {
		return DaoManager.createDao(connectionSource,
				SpatialReferenceSystem.class);
	}

	/**
	 * Get a SF/SQL Spatial Reference System DAO
	 * 
	 * @return
	 * @throws SQLException
	 */
	public SfSqlSpatialReferenceSystemDao sfSqlSpatialReferenceSystemDao()
			throws SQLException {
		return DaoManager.createDao(connectionSource,
				SfSqlSpatialReferenceSystem.class);
	}

}
