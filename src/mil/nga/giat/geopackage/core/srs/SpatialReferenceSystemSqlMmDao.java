package mil.nga.giat.geopackage.core.srs;

import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

/**
 * SQL/MM Spatial Reference System Data Access Object
 * 
 * @author osbornb
 */
public class SpatialReferenceSystemSqlMmDao extends
		BaseDaoImpl<SpatialReferenceSystemSqlMm, Integer> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public SpatialReferenceSystemSqlMmDao(ConnectionSource connectionSource,
			Class<SpatialReferenceSystemSqlMm> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

}
