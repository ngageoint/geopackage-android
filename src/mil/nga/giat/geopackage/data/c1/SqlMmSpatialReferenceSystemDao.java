package mil.nga.giat.geopackage.data.c1;

import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

/**
 * SQL/MM Spatial Reference System Data Access Object
 * 
 * @author osbornb
 */
public class SqlMmSpatialReferenceSystemDao extends
		BaseDaoImpl<SqlMmSpatialReferenceSystem, Integer> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public SqlMmSpatialReferenceSystemDao(ConnectionSource connectionSource,
			Class<SqlMmSpatialReferenceSystem> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

}
