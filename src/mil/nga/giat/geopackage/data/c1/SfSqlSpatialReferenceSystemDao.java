package mil.nga.giat.geopackage.data.c1;

import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

/**
 * SF/SQL Spatial Reference System Data Access Object
 * 
 * @author osbornb
 */
public class SfSqlSpatialReferenceSystemDao extends
		BaseDaoImpl<SfSqlSpatialReferenceSystem, Integer> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public SfSqlSpatialReferenceSystemDao(ConnectionSource connectionSource,
			Class<SfSqlSpatialReferenceSystem> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

}
