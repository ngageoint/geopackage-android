package mil.nga.giat.geopackage.features.columns;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.giat.geopackage.schema.TableColumnKey;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * SQL/MM Geometry Columns Data Access Object
 * 
 * @author osbornb
 */
public class GeometryColumnsSqlMmDao extends
		BaseDaoImpl<GeometryColumnsSqlMm, TableColumnKey> {

	/**
	 * Constructor, required by ORMLite
	 * 
	 * @param connectionSource
	 * @param dataClass
	 * @throws SQLException
	 */
	public GeometryColumnsSqlMmDao(ConnectionSource connectionSource,
			Class<GeometryColumnsSqlMm> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeometryColumnsSqlMm queryForId(TableColumnKey key)
			throws SQLException {
		GeometryColumnsSqlMm geometryColumns = null;
		if (key != null) {
			Map<String, Object> fieldValues = new HashMap<String, Object>();
			fieldValues.put(GeometryColumnsSqlMm.COLUMN_TABLE_NAME,
					key.getTableName());
			fieldValues.put(GeometryColumnsSqlMm.COLUMN_COLUMN_NAME,
					key.getColumnName());
			List<GeometryColumnsSqlMm> results = queryForFieldValues(fieldValues);
			if (!results.isEmpty()) {
				if (results.size() > 1) {
					throw new SQLException("More than one "
							+ GeometryColumnsSqlMm.class.getSimpleName()
							+ " returned for key. Table Name: "
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
	public TableColumnKey extractId(GeometryColumnsSqlMm data)
			throws SQLException {
		return data.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean idExists(TableColumnKey id) throws SQLException {
		return queryForId(id) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeometryColumnsSqlMm queryForSameId(GeometryColumnsSqlMm data)
			throws SQLException {
		return queryForId(data.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int updateId(GeometryColumnsSqlMm data, TableColumnKey newId)
			throws SQLException {
		int count = 0;
		GeometryColumnsSqlMm readData = queryForId(data.getId());
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
	public int deleteById(TableColumnKey id) throws SQLException {
		int count = 0;
		if (id != null) {
			GeometryColumnsSqlMm geometryColumns = queryForId(id);
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
	public int deleteIds(Collection<TableColumnKey> idCollection)
			throws SQLException {
		int count = 0;
		if (idCollection != null) {
			for (TableColumnKey id : idCollection) {
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
	public int update(GeometryColumnsSqlMm geometryColumns) throws SQLException {

		UpdateBuilder<GeometryColumnsSqlMm, TableColumnKey> ub = updateBuilder();
		ub.updateColumnValue(GeometryColumnsSqlMm.COLUMN_GEOMETRY_TYPE_NAME,
				geometryColumns.getGeometryTypeName());
		ub.updateColumnValue(GeometryColumnsSqlMm.COLUMN_SRS_ID,
				geometryColumns.getSrsId());
		// Don't update srs name since it is in another table

		ub.where()
				.eq(GeometryColumnsSqlMm.COLUMN_TABLE_NAME,
						geometryColumns.getTableName())
				.and()
				.eq(GeometryColumnsSqlMm.COLUMN_COLUMN_NAME,
						geometryColumns.getColumnName());

		PreparedUpdate<GeometryColumnsSqlMm> update = ub.prepare();
		int updated = update(update);

		return updated;
	}

}
