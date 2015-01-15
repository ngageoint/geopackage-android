package mil.nga.giat.geopackage;

import java.sql.SQLException;

import mil.nga.giat.geopackage.data.c1.SfSqlSpatialReferenceSystem;
import mil.nga.giat.geopackage.data.c1.SpatialReferenceSystem;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class GeoPackage {

	private final SQLiteDatabase database;

	private final ConnectionSource connectionSource;

	GeoPackage(SQLiteDatabase database) {
		this.database = database;
		connectionSource = new AndroidConnectionSource(database);
	}

	public void close() {
		connectionSource.closeQuietly();
		database.close();
	}

	public BaseDaoImpl<SpatialReferenceSystem, Integer> spatialReferenceSystemDao()
			throws SQLException {
		return getDao(SpatialReferenceSystem.class);
	}

	public BaseDaoImpl<SfSqlSpatialReferenceSystem, Integer> sfSqlSpatialReferenceSystemDao()
			throws SQLException {
		return getDao(SfSqlSpatialReferenceSystem.class);
	}

	private <Ttype, Tid> BaseDaoImpl<Ttype, Tid> getDao(Class<Ttype> type)
			throws SQLException {
		return DaoManager.createDao(connectionSource, type);
	}

}
