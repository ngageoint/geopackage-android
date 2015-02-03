package mil.nga.giat.geopackage.tiles.matrixset;

import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Tile Matrix Set Data Access Object
 * 
 * @author osbornb
 */
public class TileMatrixSetDao extends BaseDaoImpl<TileMatrixSet, String> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public TileMatrixSetDao(ConnectionSource connectionSource,
			Class<TileMatrixSet> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

}
