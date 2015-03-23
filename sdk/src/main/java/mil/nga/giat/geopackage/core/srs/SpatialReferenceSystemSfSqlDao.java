package mil.nga.giat.geopackage.core.srs;

import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

/**
 * SF/SQL Spatial Reference System Data Access Object
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemSfSqlDao extends
		BaseDaoImpl<SpatialReferenceSystemSfSql, Integer> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public SpatialReferenceSystemSfSqlDao(ConnectionSource connectionSource,
			Class<SpatialReferenceSystemSfSql> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

}
