package mil.nga.giat.geopackage.features.columns;

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
 * Geometry Columns Data Access Object
 * 
 * @author osbornb
 */
public class GeometryColumnsDao extends
		BaseDaoImpl<GeometryColumns, GeometryColumnsKey> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public GeometryColumnsDao(ConnectionSource connectionSource,
			Class<GeometryColumns> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeometryColumns queryForId(GeometryColumnsKey key)
			throws SQLException {
		GeometryColumns geometryColumns = null;
		if (key != null) {
			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(GeometryColumns.COLUMN_TABLE_NAME,
					key.getTableName());
			fieldValues.put(GeometryColumns.COLUMN_COLUMN_NAME,
					key.getColumnName());
			List<GeometryColumns> results = super
					.queryForFieldValues(fieldValues);
			if (!results.isEmpty()) {
				if (results.size() > 1) {
					throw new SQLException(
							"More than one GeometryColumns returned for key. Table Name: "
									+ key.getTableName() + ", Column Name: "
									+ key.getColumnName());
				}
				geometryColumns = results.get(0);
			}
		}
		return geometryColumns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeometryColumnsKey extractId(GeometryColumns data)
			throws SQLException {
		return data.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean idExists(GeometryColumnsKey id) throws SQLException {
		return queryForId(id) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeometryColumns queryForSameId(GeometryColumns data)
			throws SQLException {
		return queryForId(data.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int updateId(GeometryColumns data, GeometryColumnsKey newId)
			throws SQLException {
		int count = 0;
		GeometryColumns readData = queryForId(data.getId());
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
	public int deleteById(GeometryColumnsKey id) throws SQLException {
		int count = 0;
		if (id != null) {
			GeometryColumns geometryColumns = queryForId(id);
			if (geometryColumns != null) {
				count = delete(geometryColumns);
			}
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int deleteIds(Collection<GeometryColumnsKey> idCollection)
			throws SQLException {
		int count = 0;
		if (idCollection != null) {
			for (GeometryColumnsKey id : idCollection) {
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
	public int update(GeometryColumns geometryColumns) throws SQLException {

		UpdateBuilder<GeometryColumns, GeometryColumnsKey> ub = updateBuilder();
		ub.updateColumnValue(GeometryColumns.COLUMN_GEOMETRY_TYPE_NAME,
				geometryColumns.getGeometryTypeName());
		ub.updateColumnValue(GeometryColumns.COLUMN_SRS_ID,
				geometryColumns.getSrsId());
		ub.updateColumnValue(GeometryColumns.COLUMN_Z, geometryColumns.getZ());
		ub.updateColumnValue(GeometryColumns.COLUMN_M, geometryColumns.getM());

		ub.where()
				.eq(GeometryColumns.COLUMN_TABLE_NAME,
						geometryColumns.getTableName())
				.and()
				.eq(GeometryColumns.COLUMN_COLUMN_NAME,
						geometryColumns.getColumnName());

		PreparedUpdate<GeometryColumns> update = ub.prepare();
		int updated = update(update);

		return updated;
	}

}
