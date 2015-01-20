package mil.nga.giat.geopackage;

import java.sql.SQLException;

import mil.nga.giat.geopackage.data.c1.SfSqlSpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SfSqlSpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystemDao;
import mil.nga.giat.geopackage.data.c1.SqlMmSpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SqlMmSpatialReferenceSystemDao;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

/**
 * A single GeoPackage database connection implementation
 * 
 * @author osbornb
 */
class GeoPackageImpl implements GeoPackage {

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
	GeoPackageImpl(SQLiteDatabase database) {
		this.database = database;
		connectionSource = new AndroidConnectionSource(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		connectionSource.closeQuietly();
		database.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SQLiteDatabase getDatabase() {
		return database;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConnectionSource getConnectionSource() {
		return connectionSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SpatialReferenceSystemDao getSpatialReferenceSystemDao()
			throws SQLException {
		return DaoManager.createDao(connectionSource,
				SpatialReferenceSystem.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SqlMmSpatialReferenceSystemDao getSpatialReferenceSystemSqlMmDao()
			throws SQLException {
		return DaoManager.createDao(connectionSource,
				SqlMmSpatialReferenceSystem.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SfSqlSpatialReferenceSystemDao getSpatialReferenceSystemSfSqlDao()
			throws SQLException {
		return DaoManager.createDao(connectionSource,
				SfSqlSpatialReferenceSystem.class);
	}

}
