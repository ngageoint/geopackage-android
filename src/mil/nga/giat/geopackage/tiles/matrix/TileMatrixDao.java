package mil.nga.giat.geopackage.tiles.matrix;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Tile Matrix Data Access Object
 * 
 * @author osbornb
 */
public class TileMatrixDao extends BaseDaoImpl<TileMatrix, TileMatrixKey> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public TileMatrixDao(ConnectionSource connectionSource,
			Class<TileMatrix> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileMatrix queryForId(TileMatrixKey key) throws SQLException {
		TileMatrix tileMatrix = null;
		if (key != null) {
			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(TileMatrix.COLUMN_TABLE_NAME, key.getTableName());
			fieldValues.put(TileMatrix.COLUMN_ZOOM_LEVEL, key.getZoomLevel());
			List<TileMatrix> results = queryForFieldValues(fieldValues);
			if (!results.isEmpty()) {
				if (results.size() > 1) {
					throw new SQLException(
							"More than one TileMatrix returned for key. Table Name: "
									+ key.getTableName() + ", Zoom Level: "
									+ key.getZoomLevel());
				}
				tileMatrix = results.get(0);
			}
		}
		return tileMatrix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileMatrixKey extractId(TileMatrix data) throws SQLException {
		return data.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean idExists(TileMatrixKey id) throws SQLException {
		return queryForId(id) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileMatrix queryForSameId(TileMatrix data) throws SQLException {
		return queryForId(data.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int updateId(TileMatrix data, TileMatrixKey newId)
			throws SQLException {
		int count = 0;
		TileMatrix readData = queryForId(data.getId());
		if (readData != null && newId != null) {
			readData.setId(newId);
			count = update(readData);
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int deleteById(TileMatrixKey id) throws SQLException {
		int count = 0;
		if (id != null) {
			TileMatrix tileMatrix = queryForId(id);
			if (tileMatrix != null) {
				count = delete(tileMatrix);
			}
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int deleteIds(Collection<TileMatrixKey> idCollection)
			throws SQLException {
		int count = 0;
		if (idCollection != null) {
			for (TileMatrixKey id : idCollection) {
				count += deleteById(id);
			}
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Update using the complex key
	 */
	@Override
	public int update(TileMatrix tileMatrix) throws SQLException {

		UpdateBuilder<TileMatrix, TileMatrixKey> ub = updateBuilder();
		ub.updateColumnValue(TileMatrix.COLUMN_MATRIX_WIDTH,
				tileMatrix.getMatrixWidth());
		ub.updateColumnValue(TileMatrix.COLUMN_MATRIX_HEIGHT,
				tileMatrix.getMatrixHeight());
		ub.updateColumnValue(TileMatrix.COLUMN_TILE_WIDTH,
				tileMatrix.getTileWidth());
		ub.updateColumnValue(TileMatrix.COLUMN_TILE_HEIGHT,
				tileMatrix.getTileHeight());
		ub.updateColumnValue(TileMatrix.COLUMN_PIXEL_X_SIZE,
				tileMatrix.getPixelXSize());
		ub.updateColumnValue(TileMatrix.COLUMN_PIXEL_Y_SIZE,
				tileMatrix.getPixelYSize());

		ub.where().eq(TileMatrix.COLUMN_TABLE_NAME, tileMatrix.getTableName())
				.and()
				.eq(TileMatrix.COLUMN_ZOOM_LEVEL, tileMatrix.getZoomLevel());

		PreparedUpdate<TileMatrix> update = ub.prepare();
		int updated = update(update);

		return updated;
	}

}
